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
import java.sql.SQLException;
import java.util.*;

@Order(2)
public class IgcToIdfMapperBaw implements IIdfMapper {

    private static final Logger LOG = Logger.getLogger(IgcToIdfMapperBaw.class);

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private static final String CODELIST_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#";
    private static final String UDUNITS_CODESPACE_VALUE = "https://www.unidata.ucar.edu/software/udunits/";

    private static final String BAW_MODEL_KEYWORD_TYPE = "discipline";
    private static final String BAW_MODEL_THESAURUS_TITLE_PREFIX = "de.baw.codelist.model.";
    private static final String BAW_MODEL_THESAURUS_DATE = "2017-01-17";
    private static final String BAW_MODEL_THESAURUS_DATE_TYPE = "publication";

    private static final int BAW_MODEL_TYPE_CODELIST_ID = 3950003;

    private static final String VALUE_UNIT_ID_PREFIX = "valueUnit_";

    private DOMUtils domUtil;
    private SQLUtils sqlUtils;
    private TransformationUtils trafoUtil;

    private static final List<String> MD_METADATA_CHILDREN = Arrays.asList(
            "gmd:fileIdentifier",
            "gmd:language",
            "gmd:characterSet",
            "gmd:parentIdentifier",
            "gmd:hierarchyLevel",
            "gmd:hierarchyLevelName",
            "gmd:contact",
            "gmd:dateStamp",
            "gmd:metadataStandardName",
            "gmd:metadataStandardVersion",
            "gmd:dataSetURI",
            "gmd:locale",
            "gmd:spatialRepresentationInfo",
            "gmd:referenceSystemInfo",
            "gmd:metadataExtensionInfo",
            "gmd:identificationInfo",
            "gmd:contentInfo",
            "gmd:distributionInfo",
            "gmd:dataQualityInfo",
            "gmd:portrayalCatalogueInfo",
            "gmd:metadataConstraints",
            "gmd:applicationSchemaInfo",
            "gmd:metadataMaintenance",
            "gmd:series",
            "gmd:describes",
            "gmd:propertyType",
            "gmd:featureType",
            "gmd:featureAttribute"
    );
    private static final List<String> MD_IDENTIFICATION_CHILDREN = Arrays.asList(
            "gmd:citation",
            "gmd:abstract",
            "gmd:purpose",
            "gmd:credit",
            "gmd:status",
            "gmd:pointOfContact",
            "gmd:resourceMaintenance",
            "gmd:graphicOverview",
            "gmd:resourceFormat",
            "gmd:descriptiveKeywords",
            "gmd:resourceSpecificUsage",
            "gmd:resourceConstraints",
            "gmd:aggregationInfo",
            "gmd:spatialRepresentationType",
            "gmd:spatialResolution",
            "gmd:language",
            "gmd:characterSet",
            "gmd:topicCategory",
            "gmd:environmentDescription",
            "gmd:extent",
            "gmd:supplementalInformation"
    );

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
        domUtil.addNS("gml", Csw202NamespaceContext.NAMESPACE_URI_GML);
        domUtil.addNS("xlink", Csw202NamespaceContext.NAMESPACE_URI_XLINK);

        try {
            Connection connection = (Connection) sourceRecord.get(DatabaseSourceRecord.CONNECTION);
            sqlUtils = new SQLUtils(connection);
            trafoUtil = new TransformationUtils(sqlUtils);

            // Fetch elements for use later on
            Element mdMetadata = (Element) XPATH.getNode(target, "/idf:html/idf:body/idf:idfMdMetadata");

            @SuppressWarnings("unchecked")
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

            addSimSpatialDimensionKeyword(mdMetadata, objId);
            addSimModelMethodKeyword(mdMetadata, objId);
            addSimModelTypeKeywords(mdMetadata, objId);
            addTimestepSizeElement(mdMetadata, objId);
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

    private void addSimSpatialDimensionKeyword(Element mdMetadata, Long objId) throws SQLException {
        String value = getAdditionalFieldValue(objId, "simSpatialDimension");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation spatial dimensionality keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "dimensionality";

        addKeyword(
                mdMetadata,
                BAW_MODEL_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_MODEL_THESAURUS_DATE_TYPE,
                value);
    }

    private void addSimModelMethodKeyword(Element mdMetadata, Long objId) throws SQLException {
        String value = getAdditionalFieldValue(objId, "simProcess");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation modelling method keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "method";

        addKeyword(
                mdMetadata,
                BAW_MODEL_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_MODEL_THESAURUS_DATE_TYPE,
                value);
    }

    private void addSimModelTypeKeywords(Element mdMetadata, Long objId) throws SQLException {
        List<Map<String, String>> rows = getL2AdditionalFieldDataRows(objId, "simModelType");
        if (rows.isEmpty()) return;

        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "type";
        List<String> allValues = new ArrayList<>(rows.size());
        for(Map<String, String> row: rows) {
            String entryId = row.get("data");
            if (entryId == null) continue;

            String value = trafoUtil.getIGCSyslistEntryName(BAW_MODEL_TYPE_CODELIST_ID, Integer.parseInt(entryId));
            LOG.debug("Adding BAW simulation model type keyword. Value found is: " + value);

            allValues.add(value);
        }
        addKeyword(
                mdMetadata,
                BAW_MODEL_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_MODEL_THESAURUS_DATE_TYPE,
                allValues.toArray(new String[allValues.size()]));
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

    private void addTimestepSizeElement(Element mdMetadata, Long objId) throws SQLException {
        String value = getAdditionalFieldValue(objId, "dqAccTimeMeas");
        if (value == null) return; // There's nothing to do if there is no value

        if (domUtil.getNS("xs") == null) {
            domUtil.addNS("xs", "http://www.w3.org/2001/XMLSchema");
        }

        String dqInfoQname = "gmd:dataQualityInfo";

        IdfElement previousSibling = findPreviousSibling(dqInfoQname, mdMetadata, MD_METADATA_CHILDREN);

        IdfElement dqInfoElement;
        if (previousSibling == null) {
            dqInfoElement = domUtil.addElement(mdMetadata, dqInfoQname);
        } else {
            dqInfoElement = previousSibling.addElementAsSibling(dqInfoQname);
        }
        IdfElement dqElement = dqInfoElement.addElement("gmd:DQ_DataQuality");
        dqElement.addElement("gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode")
                .addAttribute("codeList", CODELIST_URL + "MD_ScopeCode")
                .addAttribute("codeListValue", "model");

        IdfElement dqQuantitativeResult = dqElement.addElement("gmd:report/gmd:DQ_AccuracyOfATimeMeasurement/gmd:result/gmd:DQ_QuantitativeResult");
        addElementWithUnits(mdMetadata, dqQuantitativeResult, "gmd:valueUnit", "s");

        dqQuantitativeResult.addElement("gmd:value/gco:Record")
                .addAttribute("xsi:type", "xs:double")
                .addText(String.format("%.1f", Double.parseDouble(value)));
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

    private void addKeyword(
            Element mdMetadata,
            String keywordType,
            String thesuarusName,
            String thesaurusDate,
            String thesaurusDateType,
            String... keywords) {
        String keywordQname = "gmd:descriptiveKeywords";

        String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification|./gmd:identificationInfo/srv:SV_ServiceIdentification";
        IdfElement mdIdentification = domUtil.getElement(mdMetadata, xpath);

        IdfElement previousSibling = findPreviousSibling(keywordQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

        IdfElement keywordElement;
        if (previousSibling == null) {
            keywordElement = mdIdentification.addElement(keywordQname);
        } else {
            keywordElement = previousSibling.addElementAsSibling(keywordQname);
        }

        IdfElement mdKeywordElement = keywordElement.addElement("gmd:MD_Keywords");
        for(String keyword: keywords) {
            mdKeywordElement.addElement("gmd:keyword/gco:CharacterString")
                    .addText(keyword);
        }
        mdKeywordElement.addElement("gmd:type/gmd:MD_KeywordTypeCode")
                .addAttribute("codeList", CODELIST_URL + "MD_KeywordTypeCode")
                .addAttribute("codeListValue", keywordType);

        IdfElement thesaurusElement = keywordElement.addElement("gmd:thesaurusName/gmd:CI_Citation");
        thesaurusElement.addElement("gmd:title/gco:CharacterString")
                .addText(thesuarusName);

        IdfElement thesaurusDateElement = thesaurusElement.addElement("gmd:date/gmd:CI_Date");
        thesaurusDateElement.addElement("gmd:date/gco:Date")
                .addText(thesaurusDate);
        thesaurusDateElement.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                .addAttribute("codeList", CODELIST_URL + "CI_DateTypeCode")
                .addAttribute("codeListValue", thesaurusDateType);
    }

    private String getAdditionalFieldValue(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj WHERE obj.obj_id=? AND obj.field_key=?";
        Map<String, String> row = sqlUtils.first(query, new Object[]{objId, fieldKey});
        if (row == null) {
            return null;
        } else {
            return row.get("data");
        }
    }

    private List<Map<String, String>> getL2AdditionalFieldDataRows(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj " +
                "JOIN additional_field_data obj_parent ON obj_parent.id = obj.parent_field_id " +
                "WHERE obj_parent.obj_id=? AND obj.field_key=?";
        List<Map<String, String>> result = sqlUtils.all(query, new Object[]{objId, fieldKey});
        return result == null ? Collections.emptyList() : result;
    }

    private void addElementWithUnits(Element mdMetadata, IdfElement parent, String qname, String units) {
        String unitsId = VALUE_UNIT_ID_PREFIX + units.replaceAll(" +", "_");
        boolean nodeExists = XPATH.nodeExists(mdMetadata, ".//*[@gml:id='" + unitsId + "']");

        if (nodeExists) {
            parent.addElement(qname)
                    .addAttribute("xlink:href", "#" + unitsId);
        } else {
            IdfElement unitDefinitionElement = parent.addElement(qname + "/gml:UnitDefinition");
            unitDefinitionElement.addAttribute("gml:id", unitsId);

            unitDefinitionElement.addElement("gml:identifier")
                    .addAttribute("codeSpace", UDUNITS_CODESPACE_VALUE)
                    .addText(units);
            unitDefinitionElement.addElement("gml:catalogSymbol")
                    .addText(units);
        }
    }

    private IdfElement findPreviousSibling(String qname, Element parent, List<String> allSiblingQnames) {
        int idxStart = allSiblingQnames.indexOf(qname);
        IdfElement previousSibling = null;
        for(int i=idxStart; i>=0 && previousSibling == null; i--) { // breaks as soon as previousSibling is found (!= null)
            previousSibling = domUtil.getElement(parent, allSiblingQnames.get(i) + "[last()]");
        }
        return previousSibling;
    }
}

