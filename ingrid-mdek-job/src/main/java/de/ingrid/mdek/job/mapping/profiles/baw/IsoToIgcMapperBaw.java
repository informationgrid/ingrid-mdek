package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DOMUtils.IdfElement;
import de.ingrid.iplug.dsc.utils.DatabaseConnectionUtils;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.iplug.dsc.utils.TransformationUtils;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.ingrid.mdek.job.mapping.profiles.baw.BawConstants.BAW_HIERARCHY_LEVEL_NAME_CODELIST_ID;

public class IsoToIgcMapperBaw implements ImportDataMapper<Document, Document> {

    private static final Logger LOG = Logger.getLogger(IsoToIgcMapperBaw.class);

    /**
     * [^0-9]*: any character, as long as it isn't a number
     * ([0-9]{4}): First capture group with exactly 4 digits
     * (-[0-9.]+)?: Second capture group starting with a hyphen and followed by a number (int or float). The whole capture group is optional (Question mark at the end)
     * ditto
     * .*: any trailing character
     */
    private static final Pattern BWASTR_PATTERN = Pattern.compile("[^0-9]*([0-9]{4})(-[0-9.]+)?(-[0-9.]+)?.*");

    private MdekCatalogService catalogService;

    private XPathUtils igcXpathUtil;
    private XPathUtils isoXpathUtil;

    private DOMUtils igcDomUtil;
    private DOMUtils isoDomUtil;

    private SQLUtils sqlUtils;
    private TransformationUtils trafoUtil;

    @Autowired
    public IsoToIgcMapperBaw(DaoFactory daoFactory) {
        catalogService = MdekCatalogService.getInstance(daoFactory);
    }

    @Override
    public void convert(Document sourceIso, Document targetIgc, ProtocolHandler protocolHandler) throws MdekException {

        try {
            ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
            cnc.addNamespaceContext(new IDFNamespaceContext());
            cnc.addNamespaceContext(new IgcProfileNamespaceContext());
            igcXpathUtil = new XPathUtils(cnc);
            igcDomUtil = new DOMUtils(targetIgc, igcXpathUtil);

            isoXpathUtil = new XPathUtils(new IDFNamespaceContext());
            isoDomUtil = new DOMUtils(sourceIso, isoXpathUtil);
            isoDomUtil.addNS("gmd", Csw202NamespaceContext.NAMESPACE_URI_GMD);
            isoDomUtil.addNS("gco", Csw202NamespaceContext.NAMESPACE_URI_GCO);
            isoDomUtil.addNS("gml", Csw202NamespaceContext.NAMESPACE_URI_GML);
            isoDomUtil.addNS("xlink", Csw202NamespaceContext.NAMESPACE_URI_XLINK);

            Element mdMetadata = (Element) isoXpathUtil.getNode(sourceIso, "/gmd:MD_Metadata");
            String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification|./gmd:identificationInfo/srv:SV_ServiceIdentification";
            Element mdIdentification = (Element) isoXpathUtil.getNode(mdMetadata, xpath);

            String addnValuesXpath = "/igc/data-sources/data-source/data-source-instance/general/general-additional-values";
            Element additionalValues = (Element) igcXpathUtil.createElementFromXPath(targetIgc.getDocumentElement(), addnValuesXpath);

            mapAuftragInfos(mdIdentification, additionalValues);
            mapHierarchyLevelName(mdMetadata, additionalValues, protocolHandler);
            mapBWaStrIdentifiers(mdIdentification, additionalValues, protocolHandler);

        } catch (MdekException e) {
            protocolHandler.addMessage(Type.ERROR, e.getMessage());
            throw e;
        }
    }

    private void mapHierarchyLevelName(Element mdMetadata, Element additionalValues, ProtocolHandler ph) {
        String xpath = "./gmd:hierarchyLevelName/gco:CharacterString";
        Node hlNameElement = isoXpathUtil.getNode(mdMetadata, xpath);
        String hlName = getNodeText(hlNameElement);
        if (!hlName.isEmpty()) {
            LOG.debug("Found BAW hierarchy level name: " + hlName);

            Integer key = catalogService.getSysListEntryKey(BAW_HIERARCHY_LEVEL_NAME_CODELIST_ID, hlName, "", false);
            if (key == null || key < 0) {
                ph.addMessage(Type.WARN, "Hierarchy level name not found in BAW codelist: " + hlName);
                key = -1;
            }

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
            additionalValue.addElement("field-key")
                    .addText("bawHierarchyLevelName");
            additionalValue.addElement("field-data")
                    .addAttribute("id", key.toString())
                    .addText(hlName);
        }
    }

    private void mapAuftragInfos(Element mdIdentification, Element additionalValues) {
        String xpath = "./gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation";
        Element ciCitation = (Element) isoXpathUtil.getNode(mdIdentification, xpath);
        if (ciCitation == null) return;

        String titleXpath = "./gmd:title/gco:CharacterString";
        Element titleElement = (Element) isoXpathUtil.getNode(ciCitation, titleXpath);
        String auftragsTitel = getNodeText(titleElement);
        if (!auftragsTitel.isEmpty()) {
            LOG.debug("Found BAW Auftragstitel: " + auftragsTitel);

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
            additionalValue.addElement("field-key").
                    addText("bawAuftragstitel");
            additionalValue.addElement("field-data").
                    addText(auftragsTitel);
        }

        String pspXpath = "./gmd:identifier/gmd:MD_Identifier/gmd:code";
        Element pspElement = (Element) isoXpathUtil.getNode(ciCitation, pspXpath);
        String pspNumber = getNodeText(pspElement);
        if (!pspNumber.isEmpty()) {
            LOG.debug("Found BAW PSP-Number: " + pspNumber);

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
            additionalValue.addElement("field-key")
                    .addText("bawAuftragsnummer");
            additionalValue.addElement("field-data").
                    addText(pspNumber);
        }
    }

    private void mapBWaStrIdentifiers(Element mdIdentification, Element additionalValues, ProtocolHandler ph) {
        String codeXpath = "./gmd:extent//gmd:geographicIdentifier/gmd:MD_Identifier[./gmd:authority/gmd:CI_Citation/gmd:title/gco:CharacterString/text()='VV-WSV 1103']/gmd:code/gco:CharacterString";
        NodeList codeNodes = isoXpathUtil.getNodeList(mdIdentification, codeXpath);
        for(int i=0; i<codeNodes.getLength(); i++) {
            String code = getNodeText(codeNodes.item(i));
            if (!code.isEmpty()) {
                LOG.debug("Found BWaStr. Section identifier: " + code);

                // Add the code as additional value
                String idx = Integer.toString(i+1); // Line numbers start with 1

                String[] components = toBWaStrComponents(code);
                String bwastrId = components[0];
                String kmStart = components[1];
                String kmEnd = components[2];

                if (bwastrId == null) {
                    ph.addMessage(Type.WARN, "No BWaStr. ID detected for code: " + code);
                    continue;
                }

                IdfElement idAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", idx);
                idAddnValue.addElement("field-key")
                        .addText("bwastr_name");
                idAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(Integer.toString(Integer.parseInt(bwastrId))); // Get rid of leading zeros
                idAddnValue.addElement("field-key-parent")
                        .addText("bwastrTable");

                if (kmStart != null) {
                    IdfElement kmStartAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                            .addAttribute("line", idx);
                    kmStartAddnValue.addElement("field-key")
                            .addText("bwastr_km_start");
                    kmStartAddnValue.addElement("field-data")
                            .addAttribute("id", "-1")
                            .addText(kmStart);
                    kmStartAddnValue.addElement("field-key-parent")
                            .addText("bwastrTable");
                }

                if (kmEnd != null) {
                    IdfElement kmStartAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                            .addAttribute("line", idx);
                    kmStartAddnValue.addElement("field-key")
                            .addText("bwastr_km_end");
                    kmStartAddnValue.addElement("field-data")
                            .addAttribute("id", "-1")
                            .addText(kmEnd);
                    kmStartAddnValue.addElement("field-key-parent")
                            .addText("bwastrTable");
                }


                // Delete from uncontrolled geolocations
                String locXpath = "../../spatial-domain/geo-location[./uncontrolled-location/location-name/text()='" + code + "']";
                igcXpathUtil.removeElementAtXPath(additionalValues, locXpath);
            }

        }
    }

    private String[] toBWaStrComponents(String code) {
        Matcher matcher = BWASTR_PATTERN.matcher(code);
        matcher.find(); // Only consider the first match

        String id = matcher.group(1);
        String start = matcher.group(2) == null ? null : matcher.group(2).substring(1); // Trim the hyphen at the start
        String end = matcher.group(3) == null ? null : matcher.group(3).substring(1);

        return new String[] { id, start, end };
    }

    private String getNodeText(Node node) {
        if (node == null) {
            return "";
        }

        String text = node.getTextContent();
        if (text == null) {
            return "";
        } else {
            return text;
        }
    }
}

