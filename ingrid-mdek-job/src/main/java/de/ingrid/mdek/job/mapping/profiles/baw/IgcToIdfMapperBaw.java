package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DOMUtils.IdfElement;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.iplug.dsc.utils.TransformationUtils;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.Connection;
import java.util.Map;

@Order(2)
public class IgcToIdfMapperBaw implements IIdfMapper {

    private static final Logger LOG = Logger.getLogger(IgcToIdfMapperBaw.class);

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private DOMUtils domUtil;
    private SQLUtils sqlUtils;
    private TransformationUtils trafoUtil;

    @Override
    public void map(SourceRecord sourceRecord, Document target) throws Exception {
        if (!(sourceRecord instanceof DatabaseSourceRecord)) {
            throw new IllegalArgumentException("Record is no DatabaseRecord!");
        }

        LOG.debug("Additional BAW specific mapping from source record to idf document: " + sourceRecord.toString());

        domUtil = new DOMUtils(target, XPATH);
        domUtil.addNS("idf", "http://www.portalu.de/IDF/1.0");
        domUtil.addNS("gmd", Csw202NamespaceContext.NAMESPACE_URI_GMD);
        domUtil.addNS("gco", Csw202NamespaceContext.NAMESPACE_URI_GCO);

        try {
            Connection connection = (Connection) sourceRecord.get(DatabaseSourceRecord.CONNECTION);
            sqlUtils = new SQLUtils(connection);
            trafoUtil = new TransformationUtils(sqlUtils);

            // Fetch elements for use later on
            Element mdMetadata = (Element) XPATH.getNode(target, "/idf:html/idf:body/idf:idfMdMetadata");
            Map<String, String> idxDoc = (Map<String, String>) sourceRecord.get("idxDoc");

            // ===== Operations that don't require a database data =====
            logMissingMetadataContact(mdMetadata);
            addWaterwayInformation(mdMetadata, idxDoc);

            // ===== Operations that require a database data =====

            // id is primary key and cannot be duplicate. Fetch the only record from the database
            Long objId = Long.parseLong((String) sourceRecord.get("id"));
            Map<String, String> objRow = sqlUtils.first("SELECT * FROM t01_object WHERE id=?", new Object[]{objId});
            if (objRow == null || objRow.isEmpty()) {
                LOG.info("No database record found in table t01_object for id: " + objId);
                return;
            }

            changeMetadataDateAsDateTime(mdMetadata, objRow.get("mod_time"));
        } catch (Exception e) {
            LOG.error("Error mapping source record to idf document.", e);
            throw e;
        }
    }

    private void logMissingMetadataContact(Node mdMetadata) {
        if (!XPATH.nodeExists(mdMetadata, "gmd:contact")) {
            LOG.error("No responsible party for metadata found!");
        }
    }

    private void changeMetadataDateAsDateTime(Node mdMetadata, String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            LOG.info("Database entry doesn't have a modified time.");
            return;
        }

        // We assume that a dateStamp node has been created by the previous mapper
        Element dateStampNode = (Element) XPATH.getNode(mdMetadata, "gmd:dateStamp");
        String isoDate = trafoUtil.getISODateFromIGCDate(dateString);
        if (isoDate != null && isoDate.contains("T")) { // Replacing Date to DateTime is really necessary
            XPATH.removeElementAtXPath(dateStampNode, "gco:Date");
            domUtil.addElement(dateStampNode, "gco:DateTime")
                    .addText(isoDate);
        }
    }

    private void addWaterwayInformation(Element mdMetadata, Map<String, String> idxDoc) {
        IdfElement additionalDataSection = domUtil.addElement(mdMetadata, "idf:additionalDataSection")
                .addAttribute("id", "bawDmqsAdditionalFields");
        additionalDataSection.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("BAW DMQS Zusatzfelder");

        // bwstr-bwastr_name (Bundeswasserstrassen Name)
        IdfElement field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-bwastr_name");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Bwstr Name");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-bwastr_name"));

        // bwstr-strecken_name (Streckenname des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-strecken_name");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Bwstr Streckenname");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-strecken_name"));

        // bwstr-center-lon (Longitude des Zentrums des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-center-lon");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Longitude des Zentrums des Abschnitts");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-center-lon"));

        // bwstr-center-lat (Latitude des Zentrums des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-center-lat");
        field.addElement("idf:title").addAttribute("lang", "de")
                .addText("Latitude des Zentrums des Abschnitts");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-center-lat"));
    }
}

