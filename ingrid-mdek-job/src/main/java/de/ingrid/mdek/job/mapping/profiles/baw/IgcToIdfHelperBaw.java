/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DOMUtils.IdfElement;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.iplug.dsc.utils.TransformationUtils;
import de.ingrid.mdek.job.Configuration;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static de.ingrid.mdek.job.mapping.profiles.baw.BawConstants.*;

class IgcToIdfHelperBaw {

    private static final Logger LOG = Logger.getLogger(IgcToIdfHelperBaw.class);

    private Configuration igeConfig;

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private static final String CODELIST_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#";
    private static final String UDUNITS_CODESPACE_VALUE = "https://www.unidata.ucar.edu/software/udunits/";

    private static final String GCO_CHARACTER_STRING_QNAME = "gco:CharacterString";
    private static final String VALUE_UNIT_ID_PREFIX = "valueUnit_";

    private static final String LITERATURE_OBJ_CLASS = "2";
    private static final String PROJECT_OBJ_CLASS = "4";

    private final JSONParser jsonParser = new JSONParser();

    private DOMUtils domUtil;
    private SQLUtils sqlUtils;
    private TransformationUtils trafoUtil;

    Long objId;
    String objClass;
    private Element mdMetadata;
    private IdfElement mdIdentification;
    private Map<String, Object> idxDoc;

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
    private static final List<String> CI_CITATION_CHILDREN = Arrays.asList(
            "gmd:title",
            "gmd:alternateTitle",
            "gmd:date",
            "gmd:edition",
            "gmd:editionDate",
            "gmd:identifier",
            "gmd:citedResponsibleParty",
            "gmd:presentationForm",
            "gmd:series",
            "gmd:otherCitationDetails",
            "gmd:collectiveTitle",
            "gmd:ISBN",
            "gmd:ISSN"
    );

    IgcToIdfHelperBaw(SourceRecord sourceRecord, Document target, Configuration igeConfig) throws SQLException {

        domUtil = new DOMUtils(target, XPATH);
        domUtil.addNS("idf", "http://www.portalu.de/IDF/1.0");
        domUtil.addNS("gmd", Csw202NamespaceContext.NAMESPACE_URI_GMD);
        domUtil.addNS("gco", Csw202NamespaceContext.NAMESPACE_URI_GCO);
        domUtil.addNS("gml", Csw202NamespaceContext.NAMESPACE_URI_GML);
        domUtil.addNS("xlink", Csw202NamespaceContext.NAMESPACE_URI_XLINK);

        Connection connection = (Connection) sourceRecord.get(DatabaseSourceRecord.CONNECTION);
        sqlUtils = new SQLUtils(connection);
        trafoUtil = new TransformationUtils(sqlUtils);

        // Fetch elements for use later on
        mdMetadata = (Element) XPATH.getNode(target, "/idf:html/idf:body/idf:idfMdMetadata");

        String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification|./gmd:identificationInfo/srv:SV_ServiceIdentification";
        mdIdentification = domUtil.getElement(mdMetadata, xpath);

        idxDoc = (Map<String, Object>) sourceRecord.get("idxDoc");

        // id is primary key and cannot be duplicate. Fetch the only record from the database
        objId = Long.parseLong((String) sourceRecord.get("id"));

        Map<String, String> objClassRow = sqlUtils.first("SELECT obj_class FROM t01_object WHERE id=?", new Object[]{objId});
        objClass = objClassRow.get("obj_class");

        this.igeConfig = igeConfig;
    }

    void addLfsLinks() throws SQLException {
        Map<Integer, Map<String, String>> rows = getOrderedAdditionalFieldDataTableRows(objId, "lfsLinkTable");
        if (rows.isEmpty()) return;

        String distInfoQname = "gmd:distributionInfo";
        String mdDistPath = distInfoQname + "/gmd:MD_Distribution";
        String transferOptionsQname = "gmd:transferOptions";
        String onlineRelPath = "gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource";

        Element mdDistribDomElement = (Element) XPATH.getNode(mdMetadata, mdDistPath);
        IdfElement mdDistribIdfElement;
        if (mdDistribDomElement == null) {
            IdfElement previousSibling = findPreviousSibling(distInfoQname, mdMetadata, MD_METADATA_CHILDREN);
            if (previousSibling == null) {
                mdDistribIdfElement = domUtil.addElement(mdMetadata, mdDistPath);
            } else {
                mdDistribIdfElement = previousSibling.addElementAsSibling(mdDistPath);
            }
        } else {
            mdDistribIdfElement = domUtil.new IdfElement(mdDistribDomElement);
        }

        IdfElement previousSibling;
        // Check if gmd:distributor exists
        previousSibling = domUtil.getElement(mdDistribIdfElement.getElement(), "gmd:distributor[last()]");
        if (previousSibling == null) {
            // Check if gmd:distributionFormat exists
            previousSibling = domUtil.getElement(mdDistribIdfElement.getElement(), "gmd:distributionFormat[last()]");
        }

        for(Map.Entry<Integer, Map<String, String>> entry: rows.entrySet()) {
            Map<String, String> row = entry.getValue();

            String url = row.get("link");
            String appProfile = row.get("fileFormat");
            String name = row.get("name");
            String desc = row.get("explanation");

            IdfElement transferOptionsElement;
            if (previousSibling == null) {
                transferOptionsElement = mdDistribIdfElement.addElementAsFirst(transferOptionsQname);
            } else {
                transferOptionsElement = previousSibling.addElementAsSibling(transferOptionsQname);
            }
            previousSibling = transferOptionsElement;
            IdfElement onlineResourceElement = transferOptionsElement.addElement(onlineRelPath);

            if (url != null && !url.trim().isEmpty()) {
                onlineResourceElement.addElement("gmd:linkage/gmd:URL")
                        .addText(igeConfig.bawLfsBaseURL + '/' + url);
            }
            if (appProfile != null && !appProfile.trim().isEmpty()) {
                onlineResourceElement.addElement("gmd:applicationProfile/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(appProfile);
            }
            if (name != null && !name.trim().isEmpty()) {
                onlineResourceElement.addElement("gmd:name/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(name);
            }
            if (desc != null && !desc.trim().isEmpty()) {
                onlineResourceElement.addElement("gmd:description/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(desc);
            }
            onlineResourceElement.addElement("gmd:function/gmd:CI_OnLineFunctionCode")
                    .addAttribute("codeList", CODELIST_URL + "gmd:CI_OnLineFunctionCode")
                    .addAttribute("codeListValue", "download")
                    .addText("publication");
        }
    }

    void logMissingMetadataContact() {
        if (!XPATH.nodeExists(mdMetadata, "gmd:contact")) {
            LOG.error("No responsible party for metadata found!");
        }
    }

    void addAuthorsAndPublishersNotInCatalogue() throws SQLException {
        // Only continue if objectClass is literature
        if (!Objects.equals(objClass, LITERATURE_OBJ_CLASS)) return;

        String contactQname = "gmd:pointOfContact";

        Map<Integer, Map<String, String>> authorsRows = getOrderedAdditionalFieldDataTableRows(objId, "bawLiteratureAuthorsTable");
        for(Map.Entry<Integer, Map<String, String>> entry: authorsRows.entrySet()) {
            Map<String, String> row = entry.getValue();

            String givenName = row.get("authorGivenName");
            String familyName = row.get("authorFamilyName");
            String organisation = row.get("authorOrganisation");

            IdfElement contact;
            IdfElement previousSibling = findPreviousSibling(contactQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);
            if (previousSibling == null) {
                contact = domUtil.addElement(mdIdentification.getElement(), contactQname);
            } else {
                contact = previousSibling.addElementAsSibling(contactQname);
            }

            IdfElement ciResponsibleParty = contact.addElement("gmd:CI_ResponsibleParty");
            if (givenName != null && familyName != null) {
                ciResponsibleParty.addElement("gmd:individualName/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(familyName + ", " + givenName);
            }
            if (organisation != null) {
                ciResponsibleParty.addElement("gmd:organisationName/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(organisation);
            }
            ciResponsibleParty.addElement("gmd:role/gmd:CI_RoleCode")
                    .addAttribute("codeList", CODELIST_URL + "gmd:CI_RoleCode")
                    .addAttribute("codeListValue", "author")
                    .addText("author");
        }

        String publisher = getFirstAdditionalFieldValue(objId, "bawLiteraturePublisher");
        if (publisher != null) {
            IdfElement contact;
            IdfElement previousSibling = findPreviousSibling(contactQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);
            if (previousSibling == null) {
                contact = domUtil.addElement(mdIdentification.getElement(), contactQname);
            } else {
                contact = previousSibling.addElementAsSibling(contactQname);
            }

            IdfElement ciResponsibleParty = contact.addElement("gmd:CI_ResponsibleParty");
            ciResponsibleParty.addElement("gmd:organisationName/" + GCO_CHARACTER_STRING_QNAME)
                    .addText(publisher);
            ciResponsibleParty.addElement("gmd:role/gmd:CI_RoleCode")
                    .addAttribute("codeList", CODELIST_URL + "gmd:CI_RoleCode")
                    .addAttribute("codeListValue", "publisher")
                    .addText("publisher");
        }
    }

    void addLiteratureCrossReference() throws SQLException {
        Map<Integer, Map<String, String>> rows = getOrderedAdditionalFieldDataTableRows(objId, "bawLiteratureXrefTable");
        for(Map.Entry<Integer, Map<String, String>> entry: rows.entrySet()) {
            Map<String, String> row = entry.getValue();

            String uuid = row.get("bawLiteratureXrefUuid");
            if (uuid == null) {
                LOG.warn("Literature cross-reference table has an entry without a UUID");
                return;
            }

            Map<String, String> litRow = sqlUtils.first("SELECT id, obj_name FROM t01_object WHERE obj_uuid = ? AND work_state = 'V'", new Object[]{uuid});
            if (litRow == null || litRow.isEmpty()) {
                LOG.warn("Couldn't find a published literature cross-reference object with UUID " + uuid);
                return;
            }

            Long litObjId = Long.parseLong(litRow.get("id"));
            String litTitle = litRow.get("obj_name");

            Map<String, String> pubDateRow = sqlUtils.first("SELECT reference_date FROM t0113_dataset_reference WHERE obj_id = ? AND type = 2", new Object[]{litObjId});
            if (pubDateRow == null || pubDateRow.isEmpty()) {
                LOG.warn("Cross-referenced literature object with UUID " + uuid + " doesn't have a publication date. Cross reference won't be added to the XML output.");
                return;
            }
            String pubDate = trafoUtil.getISODateFromIGCDate(pubDateRow.get("reference_date"));
            pubDate = pubDate.substring(0, pubDate.indexOf('T'));

            String aggrInfoQname = "gmd:aggregationInfo";
            String mdAggrInfoQname = aggrInfoQname + "/gmd:MD_AggregateInformation";
            IdfElement mdAggregateInformation;

            IdfElement previousSibling = findPreviousSibling(aggrInfoQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);
            if (previousSibling == null) {
                mdAggregateInformation = domUtil.addElement(mdIdentification.getElement(), mdAggrInfoQname);
            } else {
                mdAggregateInformation = previousSibling.addElementAsSibling(mdAggrInfoQname);
            }

            IdfElement ciCitation = mdAggregateInformation.addElement("gmd:aggregateDataSetName/gmd:CI_Citation");

            // Add uuidref attribute to gmd:
            ciCitation.getParent().addAttribute("uuidref", uuid);

            ciCitation.addElement("gmd:title/" + GCO_CHARACTER_STRING_QNAME)
                    .addText(litTitle);

            IdfElement ciDate = ciCitation.addElement("gmd:date/gmd:CI_Date");
            ciDate.addElement("gmd:date/gco:Date")
                    .addText(pubDate);
            ciDate.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                    .addAttribute("codeList", CODELIST_URL + "gmd:CI_DateTypeCode")
                    .addAttribute("codeListValue", "publication")
                    .addText("publication");

            List<String> identifiers = new ArrayList<>();
            Map<String, String> doiRow = sqlUtils.first("SELECT * FROM additional_field_data fd WHERE fd.obj_id=? AND fd.field_key = 'doiId'", new Object[]{litObjId});
            if (doiRow != null) {
                identifiers.add("https://doi.org/" + doiRow.get("data"));
            }
            String handle = getFirstAdditionalFieldValue(litObjId, "bawLiteratureHandle");
            if (handle != null) {
                identifiers.add(handle);
            }

            for(String identifier: identifiers) {
                ciCitation.addElement("gmd:identifier/gmd:MD_Identifier/gmd:code/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(identifier);
            }

            String query = "SELECT t02_address.*, t012_obj_adr.type, t012_obj_adr.special_name " +
                    "FROM t012_obj_adr, t02_address " +
                    "WHERE t012_obj_adr.adr_uuid=t02_address.adr_uuid " +
                    "      AND t02_address.work_state='V'" +
                    "      AND t012_obj_adr.obj_id=?" +
                    "      AND t012_obj_adr.type=?" +
                    "      AND t012_obj_adr.special_ref=505 " +
                    "ORDER BY line";

            final int authorKey = 11;
            final int publisherKey = 10;

            String uuidKey = "uuid";
            String firstNameKey = "firstName";
            String lastNameKey = "lastName";
            String orgKey = "organisation";
            String roleKey = "role";

            List<Map<String, String>> contacts = new ArrayList<>();
            for(Map<String, String> author: sqlUtils.all(query, new Object[] {litObjId, authorKey})) {
                Map<String, String> map = new HashMap<>();

                map.put(uuidKey, author.get("adr_uuid"));
                map.put(firstNameKey, author.get("firstname"));
                map.put(lastNameKey, author.get("lastname"));
                map.put(orgKey, author.get("institution"));
                map.put(roleKey, "author");

                contacts.add(map);
            }
            for(Map.Entry<Integer, Map<String, String>> authorEntry: getOrderedAdditionalFieldDataTableRows(litObjId, "bawLiteratureAuthorsTable").entrySet()) {
                Map<String, String> author = authorEntry.getValue();
                Map<String, String> map = new HashMap<>();

                map.put(firstNameKey, author.get("authorGivenName"));
                map.put(lastNameKey, author.get("authorFamilyName"));
                map.put(orgKey, author.get("authorOrganisation"));
                map.put(roleKey, "author");

                contacts.add(map);
            }

            Map<String, String> publisher = sqlUtils.first(query, new Object[]{litObjId, publisherKey});
            if (publisher != null) {
                Map<String, String> map = new HashMap<>();

                map.put(uuidKey, publisher.get("adr_uuid"));
                map.put(firstNameKey, publisher.get("firstname"));
                map.put(lastNameKey, publisher.get("lastname"));
                map.put(orgKey, publisher.get("institution"));
                map.put(roleKey, "publisher");

                contacts.add(map);
            }
            String publisherOrg = getFirstAdditionalFieldValue(litObjId, "bawLiteraturePublisher");
            if (publisherOrg != null) {
                Map<String, String> map = new HashMap<>();

                map.put(orgKey, publisherOrg);
                map.put(roleKey, "publisher");

                contacts.add(map);
            }

            for(Map<String, String> contact: contacts) {
                String contactUuid = contact.get(uuidKey);
                String firstName = contact.get(firstNameKey);
                String lastName = contact.get(lastNameKey);
                String organisation = contact.get(orgKey);
                String role = contact.get(roleKey);

                IdfElement ciResponsibleParty = ciCitation.addElement("gmd:citedResponsibleParty/gmd:CI_ResponsibleParty");

                if (contactUuid != null) {
                    ciResponsibleParty.addAttribute("uuid", contactUuid);
                }

                if (firstName != null && lastName != null) {
                    ciResponsibleParty.addElement("gmd:individualName/" + GCO_CHARACTER_STRING_QNAME)
                            .addText(lastName + ", " + firstName);
                }

                if (organisation != null) {
                    ciResponsibleParty.addElement("gmd:organisationName/" + GCO_CHARACTER_STRING_QNAME)
                            .addText(organisation);
                }

                ciResponsibleParty.addElement("gmd:role/gmd:CI_RoleCode")
                        .addAttribute("codeList", CODELIST_URL + "CI_RoleCode")
                        .addAttribute("codeListValue", role)
                        .addText(role);
            }

            mdAggregateInformation.addElement("gmd:associationType/gmd:DS_AssociationTypeCode")
                    .addAttribute("codeList", CODELIST_URL + "DS_AssociationTypeCode")
                    .addAttribute("codeListValue", "crossReference")
                    .addText("crossReference");
        }
    }

    void addHandleInformation() throws SQLException {
        String handle = getFirstAdditionalFieldValue(objId, "bawLiteratureHandle");
        if (handle == null || handle.trim().isEmpty()) return;

        String identifierQname = "gmd:identifier";
        IdfElement ciCitation = domUtil.getElement(mdIdentification, "gmd:citation/gmd:CI_Citation");

        IdfElement identifier;
        IdfElement previousSibling = findPreviousSibling(identifierQname, ciCitation.getElement(), CI_CITATION_CHILDREN);
        if (previousSibling == null) {
            identifier = domUtil.addElement(mdIdentification.getElement(), identifierQname);
        } else {
            identifier = previousSibling.addElementAsSibling(identifierQname);
        }

        identifier.addElement("gmd:MD_Identifier/gmd:code/" + GCO_CHARACTER_STRING_QNAME)
                .addText(handle);
    }

    void setHierarchyLevelName() throws SQLException {
        String hlName = getFirstAdditionalFieldValue(objId, "bawHierarchyLevelName");
        if (hlName == null || hlName.trim().isEmpty()) return;

        String hlNameQname = "gmd:hierarchyLevelName";
        String hlNamePath = hlNameQname + '/' + GCO_CHARACTER_STRING_QNAME;

        Element existingNode = (Element) XPATH.getNode(mdMetadata, hlNameQname);
        if (existingNode == null) {
            IdfElement previousSibling = findPreviousSibling(hlNameQname, mdMetadata, MD_METADATA_CHILDREN);
            IdfElement hlNameElement;
            if (previousSibling == null) {
                hlNameElement = domUtil.addElement(mdMetadata, hlNamePath);
            } else {
                hlNameElement = previousSibling.addElementAsSibling(hlNamePath);
            }
            hlNameElement.addText(hlName);
        } else {
            domUtil.addText(existingNode, hlName);
        }
    }

    void addAuftragsInfos() throws SQLException {
        String number = getFirstAdditionalFieldValue(objId, "bawAuftragsnummer");
        String title = getFirstAdditionalFieldValue(objId, "bawAuftragstitel");


        if (Objects.equals(objClass, PROJECT_OBJ_CLASS)) {
            if (number != null && !number.trim().isEmpty()) {
                String identifierQname = "gmd:identifier";
                IdfElement ciCitation = domUtil.getElement(mdIdentification, "gmd:citation/gmd:CI_Citation");

                IdfElement identifier;
                IdfElement previousSibling = findPreviousSibling(identifierQname, ciCitation.getElement(), CI_CITATION_CHILDREN);
                if (previousSibling == null) {
                    identifier = domUtil.addElement(mdIdentification.getElement(), identifierQname);
                } else {
                    identifier = previousSibling.addElementAsSibling(identifierQname);
                }

                identifier.addElement("gmd:MD_Identifier/gmd:code/" + GCO_CHARACTER_STRING_QNAME)
                        .addText(number);
            }
        } else {
            if (number == null && title != null) {
                LOG.error("Auftragstitel is defined but no Auftragsnummer found for object with id: " + objId);
            }
            if (number != null && title == null) {
                LOG.error("Auftragsnummer is defined but no Auftragstitel found for object with id: " + objId);
            }
            if (number == null || title == null) return;

            String aggInfoQname = "gmd:aggregationInfo";
            IdfElement previousSibling = findPreviousSibling(aggInfoQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

            String mdAggregateInfoPath = aggInfoQname + "/gmd:MD_AggregateInformation";
            IdfElement mdAggregateInfoElement;
            if (previousSibling == null) {
                mdAggregateInfoElement = mdIdentification.addElement(mdAggregateInfoPath);
            } else {
                mdAggregateInfoElement = previousSibling.addElementAsSibling(mdAggregateInfoPath);
            }

            IdfElement aggInfoCitationElement = mdAggregateInfoElement.addElement("gmd:aggregateDataSetName/gmd:CI_Citation");
            aggInfoCitationElement.addElement("gmd:title/gco:CharacterString")
                    .addText(title);
            aggInfoCitationElement.addElement("gmd:date")
                    .addAttribute("gco:nilReason", "unknown");
            aggInfoCitationElement.addElement("gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
                    .addText(number);

            mdAggregateInfoElement.addElement("gmd:associationType/gmd:DS_AssociationTypeCode")
                    .addAttribute("codeList", CODELIST_URL + "DS_AssociationTypeCode")
                    .addAttribute("codeListValue", "largerWorkCitation");
        }
    }

    void addBWaStrIdentifiers() throws SQLException {
        Map<Integer, Map<String, String>> rows = getOrderedAdditionalFieldDataTableRows(objId, "bwastrTable");

        String extentQname = "gmd:extent";
        IdfElement previousSibling = findPreviousSibling(extentQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

        for(Map.Entry<Integer, Map<String, String>> entry: rows.entrySet()) { // Sorted by index of table row
            Map<String, String> currentRow = entry.getValue();
            LOG.debug("Current BWaStr. Table Row: " + currentRow);

            String bwastrIdString = currentRow.get("bwastr_name");
            String bwastrKmStart = currentRow.get("bwastr_km_start");
            String bwastrKmEnd = currentRow.get("bwastr_km_end");


            int entryId = Integer.parseInt(bwastrIdString);
            String identifier = trafoUtil.getIGCSyslistEntryName(VV_1103_CODELIST_ID, entryId);
            if (identifier != null && bwastrKmStart != null && bwastrKmEnd != null) {
                previousSibling = addBWaStrExtentElement(mdIdentification, previousSibling, String.format("%04d-%s-%s", entryId, bwastrKmStart, bwastrKmEnd));
            } else if (identifier != null) {
                previousSibling = addBWaStrExtentElement(mdIdentification, previousSibling, identifier);
            }

        }
    }

    private IdfElement addBWaStrExtentElement(IdfElement mdIdentification, IdfElement previousSibling, String identifier) {
        String extentQname = "gmd:extent";
        IdfElement extentElement;
        if (previousSibling == null) {
            extentElement = mdIdentification.addElement(extentQname);
        } else {
            extentElement = previousSibling.addElementAsSibling(extentQname);
        }

        IdfElement exGeographicExtentElement = extentElement.addElement("gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription");
        exGeographicExtentElement.addElement("gmd:extentTypeCode/gco:Boolean")
                .addText("true");

        IdfElement mdIdentifierElement = exGeographicExtentElement.addElement("gmd:geographicIdentifier/gmd:MD_Identifier");

        IdfElement ciCitationElement = mdIdentifierElement.addElement("gmd:authority/gmd:CI_Citation");
        ciCitationElement.addElement("gmd:title/gco:CharacterString")
                .addText(VV_WSV_1103_TITLE);

        IdfElement ciDateElement = ciCitationElement.addElement("gmd:date/gmd:CI_Date");
        ciDateElement.addElement("gmd:date/gco:Date")
                .addText(VV_WSV_1103_DATE);
        ciDateElement.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                .addAttribute("codeList", CODELIST_URL + "CI_DateTypeCode")
                .addAttribute("codeListValue", VV_WSV_1103_DATE_TYPE);

        mdIdentifierElement.addElement("gmd:code/gco:CharacterString")
                .addText(identifier);

        return extentElement;
    }

    void addBawKewordCatalogeKeywords() throws SQLException {
        List<Map<String, String>> rows = getOrderedAdditionalFieldDataTableRowData(objId, "bawKeywordCatalogueEntry");

        // Collect keywords to add them to the same descriptiveKeywords element
        List<String> allValues = new ArrayList<>(rows.size());
        for(Map<String, String> row: rows) {
            String entryId = row.get("data");
            if (entryId == null) continue;

            String value = trafoUtil.getIGCSyslistEntryName(BAW_KEYWORD_CATALOGUE_CODELIST_ID, Integer.parseInt(entryId));
            allValues.add(value);
        }

        if (!allValues.isEmpty()) {
            addKeyword(
                    mdIdentification,
                    BAW_DEFAULT_KEYWORD_TYPE,
                    BAW_KEYWORD_CATALOGUE_TITLE,
                    BAW_KEYWORD_CATALOGUE_DATE,
                    BAW_DEFAULT_THESAURUS_DATE_TYPE,
                    allValues.toArray(new String[0])
            );
        }
    }

    void addSimSpatialDimensionKeyword() throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "simSpatialDimension");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation spatial dimensionality keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "dimensionality";

        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                value);
    }

    void addSimModelMethodKeyword() throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "simProcess");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation modelling method keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "method";

        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                value);
    }

    void addSimModelTypeKeywords() throws SQLException {
        List<Map<String, String>> rows = getOrderedAdditionalFieldDataTableRowData(objId, "simModelType");
        if (rows.isEmpty()) return;

        // Collect keywords to add them to the same descriptiveKeywords element
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
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                allValues.toArray(new String[allValues.size()]));
    }

    void changeMetadataDateAsDateTime() throws SQLException {
        Map<String, String> objRow = sqlUtils.first("SELECT * FROM t01_object WHERE id=?", new Object[]{objId});
        if (objRow == null || objRow.isEmpty()) {
            LOG.info("No database record found in table t01_object for id: " + objId);
            return;
        }

        String dateString = objRow.get("mod_time");
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

    void addTimestepSizeElement() throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "dqAccTimeMeas");
        if (value == null || "NaN".equals(value)) return; // There's nothing to do if there is no value

        IdfElement dqElement = modelScopedDqDataQualityElement(mdMetadata);

        IdfElement dqQuantitativeResult = dqElement.addElement("gmd:report/gmd:DQ_AccuracyOfATimeMeasurement/gmd:result/gmd:DQ_QuantitativeResult");
        addElementWithUnits(mdMetadata, dqQuantitativeResult, "gmd:valueUnit", "s");

        dqQuantitativeResult.addElement("gmd:value/gco:Record")
                .addAttribute("xsi:type", "xs:double")
                .addText(String.format("%.1f", Double.parseDouble(value)));
    }

    void addDgsValues() throws SQLException, ParseException {
        Map<Integer, Map<String, String>> groupedRows = getOrderedAdditionalFieldDataTableRows(objId, "simParamTable");

        for(Map.Entry<Integer, Map<String, String>> entry: groupedRows.entrySet()) {
            Map<String, String> row = entry.getValue();

            String paramName = row.get("simParamName");
            String paramType = trafoUtil.getIGCSyslistEntryName(BAW_SIMULATION_PARAMETER_TYPE_CODELIST_ID, Integer.parseInt(row.get("simParamType")));
            String paramUnits = row.get("simParamUnit");
            String valueType = row.get("simParamValueType");
            String simParamValues = row.get("simParamValues") == null ? "[]" : row.get("simParamValues");

            JSONArray valuesJsonArray = (JSONArray) jsonParser.parse(simParamValues);

            boolean areValuesIntegers = valuesJsonArray.stream().allMatch(e -> e instanceof Long);

            List<String> values = (List<String>) valuesJsonArray.stream()
                    .map(e -> e.toString())
                    .collect(Collectors.toList());

            IdfElement dqElement = modelScopedDqDataQualityElement(mdMetadata);
            IdfElement dqQuantitativeResult = dqElement.addElement("gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result/gmd:DQ_QuantitativeResult");

            dqQuantitativeResult.addElement("gmd:valueType/gco:RecordType")
                    .addText(paramName);

            addElementWithUnits(mdMetadata, dqQuantitativeResult, "gmd:valueUnit", paramUnits);

            if (values.isEmpty()) {
                dqQuantitativeResult.addElement("gmd:value")
                        .addAttribute("gco:nilReason", "unknown");
            } else {
                boolean areValuesDiscrete = VALUE_TYPE_DISCRETE_NUMERIC.equals(valueType)
                        || VALUE_TYPE_DISCRETE_STRING.equals(valueType);

                if (areValuesDiscrete) {
                    String typeAttr;
                    if (areValuesIntegers) {
                        typeAttr = "xs:integer";
                    } else if (VALUE_TYPE_DISCRETE_STRING.equals(valueType)) {
                        typeAttr = "xs:string";
                    } else {
                        typeAttr = "xs:double";
                    }
                    for (String val : values) {
                        dqQuantitativeResult.addElement("gmd:value/gco:Record")
                                .addAttribute("xsi:type", typeAttr)
                                .addText(val);
                    }
                } else {
                    String typeAttr;
                    if (areValuesIntegers) {
                        typeAttr = "gml:integerList";
                    } else {
                        typeAttr = "gml:doubleList";
                    }
                    String val = String.format("%s %s", values.get(0), values.get(1));
                    dqQuantitativeResult.addElement("gmd:value/gco:Record")
                            .addAttribute("xsi:type", typeAttr)
                            .addText(val);
                }
            }

            dqElement.addElement("gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString")
                    .addText(paramType);
        }
    }

    private IdfElement modelScopedDqDataQualityElement(Element mdMetadata) {
        String dqInfoQname = "gmd:dataQualityInfo";
        String dqInfoPath = dqInfoQname + "/gmd:DQ_DataQuality";

        IdfElement previousSibling = findPreviousSibling(dqInfoQname, mdMetadata, MD_METADATA_CHILDREN);

        IdfElement dqElement;
        if (previousSibling == null) {
            dqElement = domUtil.addElement(mdMetadata, dqInfoPath);
        } else {
            dqElement = previousSibling.addElementAsSibling(dqInfoPath);
        }

        dqElement.addElement("gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode")
                .addAttribute("codeList", CODELIST_URL + "MD_ScopeCode")
                .addAttribute("codeListValue", "model");

        return dqElement;
    }

    void addWaterwayInformation() {
        IdfElement additionalDataSection = domUtil.addElement(mdMetadata, "idf:additionalDataSection")
                .addAttribute("id", "bawDmqsAdditionalFields");
        additionalDataSection.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("BAW DMQS Zusatzfelder");

        // bwstr-bwastr_name (Bundeswasserstrassen Name)
        Object wwayName = idxDoc.get("bwstr-bwastr_name");
        Object wwayStretchName = idxDoc.get("bwstr-strecken_name");
        Object centreLon = idxDoc.get("bwstr-center-lon");
        Object centreLat = idxDoc.get("bwstr-center-lat");

        if (wwayName != null) {
            IdfElement field = additionalDataSection.addElement("idf:additionalDataField")
                    .addAttribute("id", "bwstr-bwastr_name");
            field.addElement("idf:title")
                    .addAttribute("lang", "de")
                    .addText("Bwstr Name");
            field.addElement("idf:data")
                    .addText(wwayName.toString());

            // bwstr-strecken_name (Streckenname des Abschnitts)
            if (wwayStretchName != null) {
                field = additionalDataSection.addElement("idf:additionalDataField")
                        .addAttribute("id", "bwstr-strecken_name");
                field.addElement("idf:title")
                        .addAttribute("lang", "de")
                        .addText("Bwstr Streckenname");
                field.addElement("idf:data")
                        .addText(wwayStretchName.toString());
            }

            // bwstr-center-lon (Longitude des Zentrums des Abschnitts)
            if (centreLon != null) {
                field = additionalDataSection.addElement("idf:additionalDataField")
                        .addAttribute("id", "bwstr-center-lon");
                field.addElement("idf:title")
                        .addAttribute("lang", "de")
                        .addText("Longitude des Zentrums des Abschnitts");
                field.addElement("idf:data")
                        .addText(centreLon.toString());
            }

            // bwstr-center-lat (Latitude des Zentrums des Abschnitts)
            if (centreLat != null) {
                field = additionalDataSection.addElement("idf:additionalDataField")
                        .addAttribute("id", "bwstr-center-lat");
                field.addElement("idf:title").addAttribute("lang", "de")
                        .addText("Latitude des Zentrums des Abschnitts");
                field.addElement("idf:data")
                        .addText(centreLat.toString());
            }
        }
    }

    private void addKeyword(
            IdfElement mdIdentification,
            String keywordType,
            String thesuarusName,
            String thesaurusDate,
            String thesaurusDateType,
            String... keywords) {
        String keywordQname = "gmd:descriptiveKeywords";

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

        IdfElement thesaurusElement = mdKeywordElement.addElement("gmd:thesaurusName/gmd:CI_Citation");
        thesaurusElement.addElement("gmd:title/gco:CharacterString")
                .addText(thesuarusName);

        IdfElement thesaurusDateElement = thesaurusElement.addElement("gmd:date/gmd:CI_Date");
        thesaurusDateElement.addElement("gmd:date/gco:Date")
                .addText(thesaurusDate);
        thesaurusDateElement.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                .addAttribute("codeList", CODELIST_URL + "CI_DateTypeCode")
                .addAttribute("codeListValue", thesaurusDateType);
    }

    private String getFirstAdditionalFieldValue(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj WHERE obj.obj_id=? AND obj.field_key=?";
        Map<String, String> row = sqlUtils.first(query, new Object[]{objId, fieldKey});
        if (row == null) {
            return null;
        } else {
            return row.get("data");
        }
    }

    private List<Map<String, String>> getOrderedAdditionalFieldDataTableRowData(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj " +
                "JOIN additional_field_data obj_parent ON obj_parent.id = obj.parent_field_id " +
                "WHERE obj_parent.obj_id=? AND obj.field_key=? " +
                "ORDER BY obj_parent.sort";
        List<Map<String, String>> result = sqlUtils.all(query, new Object[]{objId, fieldKey});
        return result == null ? Collections.emptyList() : result;
    }

    private Map<Integer, Map<String, String>> getOrderedAdditionalFieldDataTableRows(Long objId, String fieldKey) throws SQLException {
        String query;
        query = "SELECT obj.sort, obj.field_key, obj.data FROM additional_field_data obj " +
                "JOIN additional_field_data obj_parent ON obj_parent.id = obj.parent_field_id " +
                "WHERE obj_parent.obj_id=? AND obj_parent.field_key=? " +
                "ORDER BY obj.sort";
        List<Map<String, String>> allRows = sqlUtils.all(query, new Object[]{objId, fieldKey});
        if (allRows == null) return Collections.emptyMap();

        Map<Integer, Map<String, String>> groupedRows = new TreeMap<>(); // Keys are sorted
        for(Map<String, String> row: allRows) {
            Integer sort = Integer.valueOf(row.get("sort"));
            groupedRows.putIfAbsent(sort, new HashMap<>());
            groupedRows.get(sort).put(row.get("field_key"), row.get("data"));
        }
        return groupedRows;
    }

    private void addElementWithUnits(Element mdMetadata, IdfElement parent, String qname, String units) {
        if (units == null || units.trim().isEmpty()) {
            parent.addElement(qname)
                    .addAttribute("gco:nilReason", "inapplicable");
            return;
        }

        String unitsIdentifierText = units
                .replaceAll("μ", "mu")
                .replaceAll("Ω", "OMEGA")
                .replaceAll("°", "degrees")
                .replaceAll("′", "arc_minutes")
                .replaceAll("″", "arc_seconds")
                .replaceAll("%", "percent")
                .replaceAll("‰", "per_mille")
                .replaceAll(" +", "_");
        String unitsGmlId = VALUE_UNIT_ID_PREFIX + unitsIdentifierText;
        boolean nodeExists = XPATH.nodeExists(mdMetadata, "//*[@id='" + unitsGmlId + "']");

        if (nodeExists) {
            parent.addElement(qname)
                    .addAttribute("xlink:href", "#" + unitsGmlId);
        } else {
            IdfElement unitDefinitionElement = parent.addElement(qname + "/gml:UnitDefinition");
            unitDefinitionElement.addAttribute("gml:id", unitsGmlId);

            unitDefinitionElement.addElement("gml:identifier")
                    .addAttribute("codeSpace", UDUNITS_CODESPACE_VALUE)
                    .addText(unitsIdentifierText);
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

