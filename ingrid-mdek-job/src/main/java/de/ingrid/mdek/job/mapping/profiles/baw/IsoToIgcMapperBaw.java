/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DOMUtils.IdfElement;
import de.ingrid.mdek.job.Configuration;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;

import static de.ingrid.mdek.job.mapping.profiles.baw.BawConstants.*;

public class IsoToIgcMapperBaw implements ImportDataMapper<Document, Document> {

    private static final Logger LOG = Logger.getLogger(IsoToIgcMapperBaw.class);

    @Autowired
    private Configuration igeConfig;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

    private MdekCatalogService catalogService;

    private XPathUtils igcXpathUtil;
    private XPathUtils isoXpathUtil;

    private DOMUtils igcDomUtil;

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

            Element mdMetadata = (Element) isoXpathUtil.getNode(sourceIso, "/gmd:MD_Metadata");
            String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification|./gmd:identificationInfo/srv:SV_ServiceIdentification";
            Element mdIdentification = (Element) isoXpathUtil.getNode(mdMetadata, xpath);

            Element igcRoot = targetIgc.getDocumentElement();

            String addnValuesXpath = "/igc/data-sources/data-source/data-source-instance/general/general-additional-values";
            Element additionalValues = (Element) igcXpathUtil.createElementFromXPath(igcRoot, addnValuesXpath);

            mapLFSLinks(igcRoot, additionalValues);
            mapCrossReferences(mdIdentification, additionalValues);

            mapAuftragInfos(mdIdentification, additionalValues);
            mapHierarchyLevelName(mdMetadata, additionalValues, protocolHandler);
            mapBWaStrIdentifiers(mdIdentification, additionalValues, protocolHandler);
            mapKeywordCatalogueKeywords(mdIdentification, additionalValues, protocolHandler);
            mapSimSpatialDimensions(mdIdentification, additionalValues, protocolHandler);
            mapSimModelMethod(mdIdentification, additionalValues, protocolHandler);
            mapSimModelTypes(mdIdentification, additionalValues, protocolHandler);
            mapTimestepSize(mdMetadata, additionalValues);
            mapDgsValues(mdMetadata, additionalValues, protocolHandler);

            mapOnlineFunctionCode(mdMetadata, igcRoot);
            checkAddressTypes(igcRoot);

        } catch (MdekException e) {
            protocolHandler.addMessage(Type.ERROR, e.getMessage());
            throw e;
        }
    }

    private void mapLFSLinks(Element igcRoot, Element additionalValues) {
        String linkageXpath = "//data-source-instance/available-linkage[starts-with(./linkage-url/text(), '" + igeConfig.bawLfsBaseURL + "')]";
        NodeList nodes = igcXpathUtil.getNodeList(igcRoot, linkageXpath);

        for(int i=0; i<nodes.getLength(); i++) {
            Node linkageNode = nodes.item(i);

            Node linkageNameNode = igcXpathUtil.getNode(linkageNode, "linkage-name");
            Node linkageUrlNode = igcXpathUtil.getNode(linkageNode, "linkage-url");
            Node linkageUrlTypeNode = igcXpathUtil.getNode(linkageNode, "linkage-url-type");
            Node linkageDatatypeNode = igcXpathUtil.getNode(linkageNode, "linkage-datatype");
            Node linkageDescriptionNode = igcXpathUtil.getNode(linkageNode, "linkage-description");

            String linkageName = getNodeText(linkageNameNode);
            String linkageUrl = getNodeText(linkageUrlNode);
            String linkageUrlType = getNodeText(linkageUrlTypeNode);
            String linkageDatatype = getNodeText(linkageDatatypeNode);
            String linkageDescription = getNodeText(linkageDescriptionNode);

            String line = Integer.toString(i + 1);

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", line);
            additionalValue.addElement("field-key")
                    .addText("name");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(linkageName);
            additionalValue.addElement("field-key-parent")
                    .addText("lfsLinkTable");

            additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", line);
            additionalValue.addElement("field-key")
                    .addText("link");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(linkageUrl.replace(igeConfig.bawLfsBaseURL + '/', ""));
            additionalValue.addElement("field-key-parent")
                    .addText("lfsLinkTable");

            additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", line);
            additionalValue.addElement("field-key")
                    .addText("urlType");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(linkageUrlType);
            additionalValue.addElement("field-key-parent")
                    .addText("lfsLinkTable");

            additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", line);
            additionalValue.addElement("field-key")
                    .addText("fileFormat");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(linkageDatatype);
            additionalValue.addElement("field-key-parent")
                    .addText("lfsLinkTable");

            additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", line);
            additionalValue.addElement("field-key")
                    .addText("explanation");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(linkageDescription);
            additionalValue.addElement("field-key-parent")
                    .addText("lfsLinkTable");


            // Finally, remove the original node
            linkageNode.getParentNode().removeChild(linkageNode);
        }
    }

    private void mapCrossReferences(Element mdIdentification, Element additionalValues) {
        String xpath = "./gmd:aggregationInfo/gmd:MD_AggregateInformation[./gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue=\"crossReference\"]/gmd:aggregateDataSetName/gmd:CI_Citation";
        List<String> allAuthors = new ArrayList<>();
        List<String> allPublishers = new ArrayList<>();

        NodeList rows = isoXpathUtil.getNodeList(mdIdentification, xpath);
        for(int i=0; i<rows.getLength(); i++) {
            Element ciCitation = (Element) rows.item(i);

            String titleXpath = "./gmd:title/gco:CharacterString";
            Element titleElement = (Element) isoXpathUtil.getNode(ciCitation, titleXpath);
            String xrefTitle = getNodeText(titleElement);
            String line = Integer.toString(i + 1);
            if (!xrefTitle.isEmpty()) {
                LOG.debug("Found title for cross-reference: " + xrefTitle);

                IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", line);
                additionalValue.addElement("field-key")
                        .addText("doiCrossReferenceTitle");
                additionalValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(xrefTitle);
                additionalValue.addElement("field-key-parent")
                        .addText("doiCrossReferenceTable");
            }

            String dateXpath = "./gmd:date/gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue=\"publication\"]/gmd:date/gco:Date";
            Element dateElement = (Element) isoXpathUtil.getNode(ciCitation, dateXpath);
            String xrefDate = getNodeText(dateElement);
            if (!xrefDate.isEmpty()) {
                LOG.debug("Found date for cross-reference: " + xrefDate);
                long epochDateMillis = LocalDate.parse(xrefDate)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC)
                        .toInstant().toEpochMilli();

                IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", line);
                additionalValue.addElement("field-key")
                        .addText("doiCrossReferenceDate");
                additionalValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(Long.toString(epochDateMillis));
                additionalValue.addElement("field-key-parent")
                        .addText("doiCrossReferenceTable");
            }

            String identifierXpath = "./gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
            Element identifierElement = (Element) isoXpathUtil.getNode(ciCitation, identifierXpath);
            String xrefIdentifier = getNodeText(identifierElement);
            if (!xrefIdentifier.isEmpty()) {
                LOG.debug("Found identifier (e.g. DOI) for cross-reference: " + xrefIdentifier);

                IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", line);
                additionalValue.addElement("field-key")
                       .addText("doiCrossReferenceIdentifier");
                additionalValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(xrefIdentifier);
                additionalValue.addElement("field-key-parent")
                        .addText("doiCrossReferenceTable");
            }

            String authorXpath = "./gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[./gmd:role/gmd:CI_RoleCode/@codeListValue=\"author\"]/gmd:individualName/gco:CharacterString";
            NodeList authorNodes = isoXpathUtil.getNodeList(ciCitation, authorXpath);
            List<String> authorsList = new ArrayList<>();
            for (int j=0; j<authorNodes.getLength(); j++) {
                authorsList.add(getNodeText(authorNodes.item(j)));
            }
            allAuthors.addAll(authorsList);
            if (authorsList.size() > 0) {
                String xrefAuthor = String.join("; ", authorsList);

                LOG.debug("Found " + authorsList.size() + " author(s): " + xrefAuthor);

                IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", line);
                additionalValue.addElement("field-key")
                        .addText("doiCrossReferenceAuthor");
                additionalValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(xrefAuthor);
                additionalValue.addElement("field-key-parent")
                        .addText("doiCrossReferenceTable");
            }

            String publisherXpath = "./gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[./gmd:role/gmd:CI_RoleCode/@codeListValue=\"publisher\"]/gmd:organisationName/gco:CharacterString";
            Element publisherElement = (Element) isoXpathUtil.getNode(ciCitation, publisherXpath);
            String xrefPublisher = getNodeText(publisherElement);
            allPublishers.add(xrefPublisher);
            if (!xrefPublisher.isEmpty()) {
                LOG.debug("Found publisher for cross-reference: " + xrefPublisher);

                IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", line);
                additionalValue.addElement("field-key").
                        addText("doiCrossReferencePublisher");
                additionalValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(xrefPublisher);
                additionalValue.addElement("field-key-parent")
                        .addText("doiCrossReferenceTable");
            }
        }

        // Remove authors and publishers for cross-references
        String addressIdXpath = "//related-address[./type-of-relation/@entry-id=\"10\" or ./type-of-relation/@entry-id=\"11\"]/address-identifier";
        String addressInstanceXpathPattern = "/igc/addresses/address/address-instance[./address-identifier/text()=\"%s\"]";

        List<Node> nodesToRemove = new ArrayList<>();
        NodeList addressIdNodes = igcXpathUtil.getNodeList(additionalValues, addressIdXpath);
        for(int i=0; i<addressIdNodes.getLength(); i++) {
            Node addressIdNode = addressIdNodes.item(i);
            String addressUuid = getNodeText(addressIdNode);
            if (addressUuid.isEmpty()) continue;

            String addressInstanceXpath = String.format(addressInstanceXpathPattern, addressUuid);
            Node addressInstance = igcXpathUtil.getNode(additionalValues, addressInstanceXpath);
            String name = getNodeText(igcXpathUtil.getNode(addressInstance, "./name"));
            String organisation = getNodeText(igcXpathUtil.getNode(addressInstance, "./organisation"));
            Node communicationNode = igcXpathUtil.getNode(addressInstance, "./communication");
            if (communicationNode == null && (allAuthors.contains(name) || allPublishers.contains(organisation))) {
                nodesToRemove.add(addressInstance);
                nodesToRemove.add(addressIdNode.getParentNode());
            }
        }
        for(Node node: nodesToRemove) {
            node.getParentNode().removeChild(node);
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
                ph.addMessage(Type.WARN, "Hierarchy level name not found in BAW codelist: '" + hlName + '\'');
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
        String xpath = "./gmd:aggregationInfo/gmd:MD_AggregateInformation[./gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue=\"largerWorkCitation\"]/gmd:aggregateDataSetName/gmd:CI_Citation";
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
            additionalValue.addElement("field-data")
                    .addText(auftragsTitel);
        }

        String pspXpath = "./gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
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
        String codeXpath = "./gmd:extent//gmd:geographicIdentifier/gmd:MD_Identifier[./gmd:authority/gmd:CI_Citation/gmd:title/gco:CharacterString/text()='" + VV_WSV_1103_TITLE + "']/gmd:code/gco:CharacterString";
        NodeList codeNodes = isoXpathUtil.getNodeList(mdIdentification, codeXpath);
        for(int i=0; i<codeNodes.getLength(); i++) {
            String code = getNodeText(codeNodes.item(i));
            if (!code.isEmpty()) {
                LOG.debug("Found BWaStr. Section identifier: " + code);

                // Add the code as additional value
                String idx = Integer.toString(i+1); // Line numbers start with 1

                String[] components = toBWaStrComponents(code);
                if (components == null) {
                    LOG.debug("Failed to compute BWaStr. section from identifier: " + code);
                    continue;
                }
                String bwastrId = components[0];
                String kmStart = components[1];
                String kmEnd = components[2];

                if (bwastrId == null) {
                    ph.addMessage(Type.WARN, "No BWaStr. ID detected for code: '" + code + '\'');
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
        if (matcher.find()) { // Only consider the first match

            String id = matcher.group(1);
            String start = matcher.group(2) == null ? null : matcher.group(2).substring(1); // Trim the hyphen at the start
            String end = matcher.group(3) == null ? null : matcher.group(3).substring(1);

            return new String[]{id, start, end};
        } else {
            return null;
        }
    }

    private void mapKeywordCatalogueKeywords(Element mdIdentification, Element additionalValues, ProtocolHandler ph) {
        int line = 0;
        for(String kw: getKeywordsForThesaurus(mdIdentification, BAW_KEYWORD_CATALOGUE_TITLE)) {
            Integer key = catalogService.getSysListEntryKey(BAW_KEYWORD_CATALOGUE_CODELIST_ID, kw, "", false);
            if (key == null || key < 0) {
                ph.addMessage(Type.WARN, "Keyword not found in BAW Keyword Catalogue (2012): '" + kw + '\'');
                continue;
            }

            line++;

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", Integer.toString(line));
            additionalValue.addElement("field-key")
                    .addText("bawKeywordCatalogueEntry");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(key.toString());
            additionalValue.addElement("field-key-parent")
                    .addText("bawKeywordCatalogueTable");

            removeFromUncontrolledTerms(additionalValues, kw);
        }
    }

    private void mapSimSpatialDimensions(Element mdIdentification, Element additionalValues, ProtocolHandler ph) {
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "dimensionality";
        List<String> kwList = getKeywordsForThesaurus(mdIdentification, thesaurusTitle);

        if (kwList.size() == 0) return;

        String kw = kwList.get(0); // There should be only one such keyword
        Integer key = catalogService.getSysListEntryKey(BAW_DIMENSIONALITY_CODELIST_ID, kw, "", false);
        if (key == null || key < 1) {
            ph.addMessage(Type.ERROR, "Spatial dimensionality code wasn't found in the available codelist: '" + kw + '\'');
        } else {
            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
            additionalValue.addElement("field-key")
                    .addText("simSpatialDimension");
            additionalValue.addElement("field-data")
                    .addAttribute("id", key.toString())
                    .addText(kw);

            removeFromUncontrolledTerms(additionalValues, kw);
        }
    }

    private void mapSimModelMethod(Element mdIdentification, Element additionalValues, ProtocolHandler ph) {
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "method";
        List<String> kwList = getKeywordsForThesaurus(mdIdentification, thesaurusTitle);

        if (kwList.size() == 0) return;

        String kw = kwList.get(0); // There should be only one such keyword
        Integer key = catalogService.getSysListEntryKey(BAW_MODEL_METHOD_CODELIST_ID, kw, "", false);
        if (key == null || key < 1) {
            ph.addMessage(Type.ERROR, "Modelling method code wasn't found in the available codelist: '" + kw + '\'');
        } else {
            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
            additionalValue.addElement("field-key")
                    .addText("simProcess");
            additionalValue.addElement("field-data")
                    .addAttribute("id", key.toString())
                    .addText(kw);

            removeFromUncontrolledTerms(additionalValues, kw);
        }
    }

    private void mapSimModelTypes(Element mdIdentification, Element additionalValues, ProtocolHandler ph) {
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "type";

        int line = 0;
        for(String kw: getKeywordsForThesaurus(mdIdentification, thesaurusTitle)) {
            Integer key = catalogService.getSysListEntryKey(BAW_MODEL_TYPE_CODELIST_ID, kw, "", false);
            if (key == null || key < 0) {
                ph.addMessage(Type.WARN, "Simulation model type not found in available codelist: '" + kw + '\'');
                continue;
            }

            line++;

            IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                    .addAttribute("line", Integer.toString(line));
            additionalValue.addElement("field-key")
                    .addText("simModelType");
            additionalValue.addElement("field-data")
                    .addAttribute("id", "-1")
                    .addText(key.toString());
            additionalValue.addElement("field-key-parent")
                    .addText("simModelTypeTable");

            removeFromUncontrolledTerms(additionalValues, kw);
        }
    }

    private List<String> getKeywordsForThesaurus(Element mdIdentification, String thesaurusTitle) {
        String xpath = "gmd:descriptiveKeywords/gmd:MD_Keywords[./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/text()='" + thesaurusTitle + "']/gmd:keyword/gco:CharacterString";
        NodeList allKeywordNodes = isoXpathUtil.getNodeList(mdIdentification, xpath);

        if (allKeywordNodes.getLength() == 0) {
            return Collections.emptyList();
        }
        List<String> keywordTexts = new ArrayList<>(allKeywordNodes.getLength());
        for(int i=0; i<allKeywordNodes.getLength(); i++) {
            String kw = getNodeText(allKeywordNodes.item(i));
            if (!kw.isEmpty()) {
                keywordTexts.add(kw);
            }
        }

        return keywordTexts;
    }

    private void removeFromUncontrolledTerms(Element additionalValues, String keyword) {
        Element igcRoot = additionalValues.getOwnerDocument().getDocumentElement();
        String xpath = "//uncontrolled-term[.='" + keyword + "']";
        igcXpathUtil.removeElementAtXPath(igcRoot, xpath);
    }

    private void mapTimestepSize(Element mdMetadata, Element additionalValues) {
        String xpath = ".//gmd:DQ_AccuracyOfATimeMeasurement/gmd:result/gmd:DQ_QuantitativeResult"; // There is only one DQ_AccuracyOfATimeMeasurement Element
        Node resultNode = isoXpathUtil.getNode(mdMetadata, xpath);

        if (resultNode == null) return;

        String valueXpath = "./gmd:value/gco:Record";
        Node valueNode = isoXpathUtil.getNode(resultNode, valueXpath);
        String value = getNodeText(valueNode);

        IdfElement additionalValue = igcDomUtil.addElement(additionalValues, "general-additional-value");
        additionalValue.addElement("field-key")
                .addText("dqAccTimeMeas");
        additionalValue.addElement("field-data")
                .addText(value);
    }

    private void mapDgsValues(Element mdMetadata, Element additionalValues, ProtocolHandler ph) {
        /*
         * Index of the table rows have to be tracked independently because
         * the number of records in a dataQualityInfo element can vary.
         */
        int line = 0;

        String dqXpath = "./gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy and ./gmd:scope/*/gmd:level/gmd:MD_ScopeCode/@codeListValue='model']";

        String resultXpath = "./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result/gmd:DQ_QuantitativeResult";

        String paramNameXpath = "./gmd:valueType/gco:RecordType";
        String valueXpath = "./gmd:value/gco:Record";
        String typeAttrXpath = valueXpath + "[@xsi:type='gml:integerList' or @xsi:type='gml:doubleList']";

        String paramTypeXpath = "./gmd:lineage/*/gmd:source/*/gmd:description/gco:CharacterString";

        String simParamTable = "simParamTable";

        NodeList allDqNodes = isoXpathUtil.getNodeList(mdMetadata, dqXpath);
        for(int i=0; i<allDqNodes.getLength(); i++) {
            LOG.debug(String.format("Importing %d of %d DGS values", i+1, allDqNodes.getLength()));

            Node dqNode = allDqNodes.item(i);
            NodeList allResultNodes = isoXpathUtil.getNodeList(dqNode, resultXpath);

            Node paramTypeNode = isoXpathUtil.getNode(dqNode, paramTypeXpath);
            String paramType = getNodeText(paramTypeNode);
            Integer paramTypeKey = catalogService.getSysListEntryKey(BAW_SIMULATION_PARAMETER_TYPE_CODELIST_ID, paramType, "", false);
            if (paramTypeKey == null || paramTypeKey < -1) {
                ph.addMessage(Type.ERROR, "Simulation parameter type not found in the available codelist: '" + paramType + '\'');
                continue;
            }
            LOG.debug("DGS parameter type can be imported: " + paramType);

            for (int j = 0; j < allResultNodes.getLength(); j++) {

                line++;
                String lineStr = Integer.toString(line);

                Node resultNode = allResultNodes.item(j);
                Node paramNameNode = isoXpathUtil.getNode(resultNode, paramNameXpath);
                String paramName = getNodeText(paramNameNode);
                LOG.debug("DGS parameter name found: " + paramName);

                IdfElement paramTypeAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", lineStr);
                paramTypeAddnValue.addElement("field-key")
                        .addText("simParamType");
                paramTypeAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(paramTypeKey.toString());
                paramTypeAddnValue.addElement("field-key-parent")
                        .addText(simParamTable);

                IdfElement paramNameAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", lineStr);
                paramNameAddnValue.addElement("field-key")
                        .addText("simParamName");
                paramNameAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(paramName);
                paramNameAddnValue.addElement("field-key-parent")
                        .addText(simParamTable);

                String paramUnits = getValueUnits(resultNode);
                if (!paramUnits.isEmpty()) {
                    LOG.debug("DGS parameter units found: " + paramUnits);

                    IdfElement paramUnitsAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                            .addAttribute("line", lineStr);
                    paramUnitsAddnValue.addElement("field-key")
                            .addText("simParamUnit");
                    paramUnitsAddnValue.addElement("field-data")
                            .addAttribute("id", "-1")
                            .addText(paramUnits);
                    paramUnitsAddnValue.addElement("field-key-parent")
                            .addText(simParamTable);
                }

                NodeList allValueNodes = isoXpathUtil.getNodeList(resultNode, valueXpath);
                List<String> values = new ArrayList<>();

                for (int k = 0; k < allValueNodes.getLength(); k++) {
                    String valueText = getNodeText(allValueNodes.item(k));
                    if (valueText.indexOf(' ') < 0) { // Single discrete value
                        values.add(valueText);
                    } else { // value range (gml list)
                        for (String val : valueText.split(" +")) {
                            values.add(val);
                        }
                    }
                }
                LOG.debug(String.format("%d values found to be imported in the current DGS block", values.size()));

                boolean hasNumericValues = true;
                JSONArray jsonArray = new JSONArray();
                if (hasOnlyIntegerValues(values)) {
                    for(String val: values) {
                        jsonArray.add(Long.parseLong(val));
                    }
                } else if (hasOnlyDoubleValues(values)) {
                    for(String val: values) {
                        jsonArray.add(Double.parseDouble(val));
                    }
                } else {
                    hasNumericValues = false;
                    for(String val: values) {
                        jsonArray.add(val);
                    }
                }

                IdfElement valueAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", lineStr);
                valueAddnValue.addElement("field-key")
                        .addText("simParamValues");
                valueAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(jsonArray.toJSONString());
                valueAddnValue.addElement("field-key-parent")
                        .addText(simParamTable);

                boolean hasDiscreteValues = !isoXpathUtil.nodeExists(resultNode, typeAttrXpath); // Discrete values DON'T have values in the gml namespace for the type attribute
                String valueType;
                if (values.isEmpty()) {
                    valueType = VALUE_TYPE_DISCRETE_STRING;
                } else if (hasDiscreteValues && hasNumericValues) {
                    valueType = VALUE_TYPE_DISCRETE_NUMERIC;
                } else if (hasDiscreteValues) {
                    valueType = VALUE_TYPE_DISCRETE_STRING;
                } else {
                    valueType = VALUE_TYPE_RANGE_NUMERIC;
                }

                String valuesFormatted;
                if (VALUE_TYPE_RANGE_NUMERIC.equals(valueType)) {
                    valuesFormatted = String.format("[ %s .. %s ]",
                            numberFormat.format(jsonArray.get(0)),
                            numberFormat.format(jsonArray.get(1)));
                } else if (values.size() == 1) {
                    valuesFormatted = values.get(0);
                } else {
                    int maxLength = 30;
                    valuesFormatted = "[ ";
                    for(int k=0; k<jsonArray.size(); k++) {
                        if (k > 0) valuesFormatted += " | ";
                        if (valuesFormatted.length() > maxLength) {
                            valuesFormatted += "...";
                            break;
                        } else if(hasNumericValues) {
                            valuesFormatted += numberFormat.format(jsonArray.get(k));
                        } else {
                            valuesFormatted += jsonArray.get(k);
                        }
                    }
                    valuesFormatted += " ]";
                }

                IdfElement valueFormattedAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", lineStr);
                valueFormattedAddnValue.addElement("field-key")
                        .addText("simParamValuesFormatted");
                valueFormattedAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(valuesFormatted);
                valueFormattedAddnValue.addElement("field-key-parent")
                        .addText(simParamTable);

                IdfElement valueTypeAddnValue = igcDomUtil.addElement(additionalValues, "general-additional-value")
                        .addAttribute("line", lineStr);
                valueTypeAddnValue.addElement("field-key")
                        .addText("simParamValueType");
                valueTypeAddnValue.addElement("field-data")
                        .addAttribute("id", "-1")
                        .addText(valueType);
                valueTypeAddnValue.addElement("field-key-parent")
                        .addText(simParamTable);
            }
        }
    }

    private String getValueUnits(Node quantitativeResultNode) {
        Node symbolNode = null;
        if (isoXpathUtil.nodeExists(quantitativeResultNode, ".//gml:catalogSymbol")) {
            symbolNode = isoXpathUtil.getNode(quantitativeResultNode, ".//gml:catalogSymbol");
        } else {
            Node hrefNode = isoXpathUtil.getNode(quantitativeResultNode, "./gmd:valueUnit/@xlink:href");
            if (!getNodeText(hrefNode).isEmpty()) {
                String href = getNodeText(hrefNode).substring(1); // Remove the leading #
                String xpath = "//gml:UnitDefinition[@gml:id='" + href + "']//gml:catalogSymbol";
                symbolNode = isoXpathUtil.getNode(quantitativeResultNode, xpath);
            }
        }
        return getNodeText(symbolNode);
    }

    private boolean hasOnlyIntegerValues(List<String> values) {
        try {
            for(String v: values) {
                Long.parseLong(v);
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    private boolean hasOnlyDoubleValues(List<String> values) {
        try {
            for(String v: values) {
                Double.parseDouble(v);
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    private void mapOnlineFunctionCode(Element mdMetadata, Element igcRoot) {
        String linkageXpath = "/igc/data-sources/data-source/data-source-instance/available-linkage";
        NodeList allIgcLinkages = igcXpathUtil.getNodeList(igcRoot, linkageXpath);

        Map<String, Integer> counts = new HashMap<>();

        String urlXpath = "./linkage-url";
        String functionNodeXpathPattern = "./gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource[./gmd:linkage/gmd:URL/text()='%s']/gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue";
        for(int i=0; i<allIgcLinkages.getLength(); i++) {
            Element linkageNode = (Element) allIgcLinkages.item(i);
            String url = getNodeText(igcXpathUtil.getNode(linkageNode, urlXpath));

            if (url.isEmpty()) continue;

            Integer n = counts.getOrDefault(url, 0);
            counts.put(url, n+1);

            NodeList codeListNodes = isoXpathUtil.getNodeList(mdMetadata, String.format(functionNodeXpathPattern, url));
            String codeListValue = getNodeText(codeListNodes.item(n));
            if ("download".equals(codeListValue)) {
                igcDomUtil.addElement(linkageNode, "linkage-reference")
                        .addAttribute("id", "9990")
                        .addText("Datendownload");
            }
        }
    }

    private void checkAddressTypes(Element igcRoot) {
        // Fix info@baw.de
        String bawXpath = "//address-instance[./address-identifier/text() = '891d8fdf-e6cf-3f61-9ca4-668880483ca8']";
        NodeList bawAddressNodes = igcXpathUtil.getNodeList(igcRoot, bawXpath);

        for(int i=0; i<bawAddressNodes.getLength(); i++) {
            Node bawAddress = bawAddressNodes.item(i);
            Element adminArea = (Element) igcXpathUtil.getNode(bawAddress, "./administrative-area");
            adminArea.setAttribute("id", "1");

            igcXpathUtil.getNode(bawAddress, "postal-code").setTextContent("76187");
            igcXpathUtil.getNode(bawAddress, "street").setTextContent("Ku\u00DFmaulstr. 17");
            igcXpathUtil.getNode(bawAddress, "administrativeArea").setTextContent("Karlsruhe");
            igcXpathUtil.getNode(bawAddress, "city").setTextContent("Karlsruhe");
        }

        // Fix address types for sections
        String addressXpath = "//address-instance[./type-of-address/@id='0' and starts-with(./communication/communication-value/text(), 'daten-') and contains(./communication/communication-value/text(), '@baw.de')]";
        NodeList addressNodes = igcXpathUtil.getNodeList(igcRoot, addressXpath);

        for(int i=0; i<addressNodes.getLength(); i++) {
            Element element = (Element) igcXpathUtil.getNode(addressNodes.item(i), "type-of-address");
            element.setAttribute("id", "1");
        }
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

