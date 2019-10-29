/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.mapping.profiles.baw.validation.iso;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import org.apache.log4j.Logger;
import org.dom4j.Node;

import java.util.List;

import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.*;
import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ValidationType.*;

/**
 * Validator for ISO-XML Metadata using the BAW Metadata profile.
 *
 * @author Vikram Notay
 */
public final class BawMetadataProfileValidator implements ImportDataMapper<org.w3c.dom.Document, org.w3c.dom.Document> {

    private static final Logger LOG = Logger.getLogger(BawMetadataProfileValidator.class);

    private static final String BAW_URL = "(?:https?://)?(?:www.)?baw.de/?";
    private static final String BAW_NAME = "Bundesanstalt für Wasserbau";
    private static final String BAW_MD_STANDARD_NAME = "ISO 19115; ?GDI-BAW";
    private static final String BAW_MD_STANDARD_VERSION = "2003\\(E\\)/Cor.1:2006\\(E\\); ?1.3:2019";
    private static final String BAW_AUFTRAGSNR = "(?:B\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{5}|A\\d{11})";
    private static final String BWASTR_KM_KM = "\\d{4}-\\d{1,4}-\\d{1,4}";

    private static final String GERMANY_WEST_BOUND_LONGITUDE = "5.0";
    private static final String GERMANY_EAST_BOUND_LONGITUDE = "16.0";
    private static final String GERMANY_SOUTH_BOUND_LATITUDE = "47.0";
    private static final String GERMANY_NORTH_BOUND_LATITUDE = "56.0";


    public BawMetadataProfileValidator() {
    }

    @Override
    public void convert(org.w3c.dom.Document sourceIso, org.w3c.dom.Document igcIgnored, ProtocolHandler ph) throws MdekException {
        IsoImportValidationUtil validator = new IsoImportValidationUtil(sourceIso, ph, ISO_ELEMENTS_RESOURCE_BUNDLE, ISO_MESSAGES_RESOURCE_BUNDLE);

        validateFileIdentifier(validator);
        validateMetadataLanguage(validator);
        validateDatasetLanguage(validator);
        validateMetadataCharset(validator);
        validateDatasetCharset(validator);
        validateHierarchyLevel(validator);
        //// TODO hierarchy level name
        ////validateParentIdentifier(dom4jDoc, reportHelper);
        validateMdContactDetails(validator);
        validateDatasetContactDetails(validator);
        validateMdDatestamp(validator);
        validateDatasetDatestamp(validator);
        validateMdStandardName(validator);
        validateMdStandardVersion(validator);
        //validateAuftragsNummer(dom4jDoc, reportHelper);
        validateGeographicIdentifier(validator);
        validateGeographicBoundingBox(validator);
        //validateTemporalResolution(dom4jDoc, reportHelper);
        //validateDgsParameterName(dom4jDoc, reportHelper);
        //validateDgsRole(dom4jDoc, reportHelper);
    }

    private void validateFileIdentifier(IsoImportValidationUtil validator) {
        String tagKey = "iso.fileIdentifier.2";
        String xpath = "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";
        validator.validate(xpath, tagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_UUID);
    }

    private void validateMetadataLanguage(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:language/gmd:LanguageCode";
        String tagKey = "iso.language.3";
        validateLanguageElement(validator, xpath, tagKey);
    }

    private void validateDatasetLanguage(IsoImportValidationUtil validator) {
        String xpath = "//gmd:identificationInfo/*/gmd:language/gmd:LanguageCode";
        String tagKey = "iso.language.39";
        validateLanguageElement(validator, xpath, tagKey);
    }

    private void validateLanguageElement(IsoImportValidationUtil validator, String xpath, String tagKey) {
        validator.validate(xpath, tagKey, "", ONE_OR_MORE_NODES_EXIST);
        validateCodeListUri(validator, xpath, tagKey, "LanguageCode");

        // TODO add tests for values not matching pattern
        String codeListValueXpath = xpath + "/@codeListValue";
        String expectedPattern = "(ger|eng)";
        validator.validate(codeListValueXpath, tagKey, expectedPattern, TEXT_CONTENT_MATCHES_PATTERN_FOR_ALL_INSTANCES);
    }

    private void validateMetadataCharset(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode";
        String tagKey = "iso.characterSet.4";
        validateCharsetElement(validator, xpath, tagKey);
    }

    private void validateDatasetCharset(IsoImportValidationUtil validator) {
        String xpath = "//gmd:identificationInfo/*/gmd:characterSet/gmd:MD_CharacterSetCode";
        String tagKey = "iso.characterSet.40";
        validateCharsetElement(validator, xpath, tagKey);
    }

    private void validateCharsetElement(IsoImportValidationUtil validator, String xpath, String tagKey) {
        validator.validate(xpath, tagKey, "", ONE_OR_MORE_NODES_EXIST);
        validateCodeListUri(validator, xpath, tagKey, "MD_CharacterSetCode");

        // TODO add tests for differing values
        String codeListValueXpath = xpath + "/@codeListValue";
        String expectedValue = "utf8";
        validator.validate(codeListValueXpath, tagKey, expectedValue, Type.WARN, TEXT_CONTENT_EQUALS);
    }

    private void validateCodeListUri(IsoImportValidationUtil validator, String xpath, String tagKey, String codelistFragment) {
        String codeListXpath = xpath + "/@codeList";
        String codelistUri = CODELIST_BASE_URL + '#' + codelistFragment;
        validator.validate(codeListXpath, tagKey, codelistUri, TEXT_CONTENT_EQUALS);
    }

    private void validateHierarchyLevel(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode";
        String tagKey = "iso.hierarchyLevel.6";
        validateCodeListUri(validator, xpath, tagKey, "MD_ScopeCode");

        String codeListValueXpath = xpath + "/@codeListValue";
        String expectedValue = "dataset";
        validator.validate(codeListValueXpath, tagKey, expectedValue, EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_EQUALS);
    }

    /*
    private void validateParentIdentifier(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpathPid = "/gmd:MD_Metadata/gmd:parentIdentifier/gco:CharacterString";
        String xpathHln = "/gmd:MD_Metadata/gmd:hierarchyLevelName/gco:CharacterString";

        Element pid = (Element) dom4jDoc.selectSingleNode(xpathPid);
        Element hln = (Element) dom4jDoc.selectSingleNode(xpathHln);

        if (hln == null) {
            reportHelper.warn(
                    "validation.iso.baw.warn.parent_identifier.missing_hierarchy_level_name",
                    "Hierarchy level name missing. Cannot check parent identifier");
        } else if (hln.getText().equals("Auftrag") && pid != null) {
            reportHelper.fail(
                    "validation.iso.invalid.parent_identifier.auftrag",
                    "Parent identifier cannot be defined if hierarchy level name is 'Auftrag'.");
        } else if (pid == null) {
            reportHelper.fail(
                    "validation.iso.invalid.parent_identifier.not_auftrag",
                    "Parent identifier must be defined if hierarchyLevelName isn't 'Auftrag'.",
                    hln.getText());
        } else if (!ValidationReportHelper.isValidUuid(pid.getText())) {
            reportHelper.fail(
                    "validation.iso.invalid.parent_identifier.uuid",
                    "Parent identifier is not a valid UUID",
                    pid.getText());
        } else {
            reportHelper.pass(
                    "validation.iso.baw.passed.parent_identifier",
                    "Parent identifier conforms to the BAW standard.");
        }
    }
    */

    private void validateMdContactDetails(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:contact";
        String tagKey = "iso.contact.8";
        validateContactDetails(validator, xpath, tagKey);
    }

    private void validateDatasetContactDetails(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact";
        String tagKey = "iso.pointOfContact.29";
        validateContactDetails(validator, xpath, tagKey);
    }

    private void validateContactDetails(IsoImportValidationUtil validator, String xpath, String tagKey) {
        validator.validate(xpath, tagKey, "", ONE_OR_MORE_NODES_EXIST);
        // TODO check that first contact is BAW with email address info@baw.de

        List<Node> contacts = validator.selectNodes(xpath);
        for(Node c: contacts) {
            String contactUuidXpath = "./gmd:CI_ResponsibleParty/@uuid";
            validator.validate(c, contactUuidXpath, tagKey, "", TEXT_CONTENT_IS_UUID);

            String orgNameXpath = "./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
            String orgTagKey = "iso.organisationName.376";
            validator.validate(c, orgNameXpath, orgTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_NOT_EMPTY);

            String emailXpath = ".//gmd:electronicMailAddress/gco:CharacterString";
            String emailTagKey = "iso.electronicMailAddress.386";
            validator.validate(c, emailXpath, emailTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_NOT_EMPTY);

            String urlXpath = ".//gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
            String urlTagKey = "iso.onlineResource.390";
            validator.validate(c, urlXpath, urlTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_NOT_EMPTY);
        }
    }

    private void validateMdDatestamp(IsoImportValidationUtil validator) {
        String tagKey = "iso.dateStamp.9";
        String xpath = dateOrDateTimeXpath("/gmd:MD_Metadata/gmd:dateStamp");
        validator.validate(xpath, tagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_ISO_8601_STRING);
    }

    private void validateDatasetDatestamp(IsoImportValidationUtil validator) {
        String tagKey = "iso.dateStamp.394";
        String xpath = dateOrDateTimeXpath("/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:date");
        validator.validate(xpath, tagKey, "", TEXT_CONTENT_IS_ISO_8601_STRING);
    }

    private String dateOrDateTimeXpath(String parentXpath) {
        return String.format("%s/gco:Date|%s/gco:DateTime", parentXpath, parentXpath);
    }

    private void validateMdStandardName(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString";
        String tagKey = "iso.metadataStandardName.10";
        validator.validate(xpath, tagKey, BAW_MD_STANDARD_NAME, EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_MATCHES_PATTERN_FOR_ALL_INSTANCES);
    }

    private void validateMdStandardVersion(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardVersion/gco:CharacterString";
        String tagKey = "iso.metadataStandardVersion.11";
        validator.validate(xpath, tagKey, BAW_MD_STANDARD_VERSION, EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_MATCHES_PATTERN_FOR_ALL_INSTANCES);
    }

    /*
    private void validateAuftragsNummer(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.auftragsnr", "BAW Auftragsnr.");
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/* /gmd:descriptiveKeywords/*[./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/text()='DEBUNDBAWAUFTRAGNR']/gmd:keyword/gco:CharacterString";
        Element element = getElementOrReportError(
                dom4jdoc,
                xpath,
                "validation.baw.tag.auftragsnr",
                reportHelper);
        if (element != null) {
            String auftrag = element.getTextTrim();
            if (auftrag.matches(BAW_AUFTRAGSNR)) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Invalid metadata standard version",
                        tagName,
                        auftrag,
                        element.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.element.value.invalid",
                        "Valid metadata standard version",
                        tagName,
                        auftrag,
                        element.getUniquePath(),
                        BAW_MD_STANDARD_VERSION);
            }
        }
    }
    */

    private void validateGeographicIdentifier(IsoImportValidationUtil validator) {
        String tagKey = "iso.geographicIdentifier.349";
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/*/gmd:code/gco:CharacterString";
        validator.validate(xpath, tagKey, "", ONE_OR_MORE_NODES_EXIST, TEXT_CONTENT_NOT_EMPTY);
        validator.validate(xpath, tagKey, BWASTR_KM_KM, TEXT_CONTENT_MATCHES_PATTERN_AT_LEAST_ONCE);
    }

    private void validateGeographicBoundingBox(IsoImportValidationUtil validator) {
        String tagKey = "iso.EX_GeographicBoundingBox.343";
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
        validator.validate(xpath, tagKey, "", ONE_OR_MORE_NODES_EXIST);

        List<Node> nodes = validator.selectNodes(xpath);
        for(Node n: nodes) {
            String westXpath = "./gmd:westBoundLongitude/gco:Decimal";
            String eastXpath = "./gmd:eastBoundLongitude/gco:Decimal";
            String southXpath = "./gmd:southBoundLatitude/gco:Decimal";
            String northXpath = "./gmd:northBoundLatitude/gco:Decimal";

            String westTagKey = "iso.westBoundLongitude.344";
            String eastTagKey = "iso.eastBoundLongitude.345";
            String southTagKey = "iso.southBoundLatitude.346";
            String northTagKey = "iso.northBoundLatitude.347";

            validator.validate(n, westXpath, westTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_FLOATING_POINT_NUMBER);
            validator.validate(n, eastXpath, eastTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_FLOATING_POINT_NUMBER);
            validator.validate(n, southXpath, southTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_FLOATING_POINT_NUMBER);
            validator.validate(n, northXpath, northTagKey, "", EXACTLY_ONE_NODE_EXISTS, TEXT_CONTENT_IS_FLOATING_POINT_NUMBER);

            validator.validate(n, westXpath, westTagKey, "-180", TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);
            validator.validate(n, eastXpath, eastTagKey, "-180", TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);
            validator.validate(n, southXpath, southTagKey, "-90", TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);
            validator.validate(n, northXpath, northTagKey, "-90", TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);

            validator.validate(n, westXpath, westTagKey, "180", TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);
            validator.validate(n, eastXpath, eastTagKey, "180", TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);
            validator.validate(n, southXpath, southTagKey, "90", TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);
            validator.validate(n, northXpath, northTagKey, "90", TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);

            validator.validate(n, westXpath, westTagKey, GERMANY_WEST_BOUND_LONGITUDE, Type.WARN, TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);
            validator.validate(n, eastXpath, eastTagKey, GERMANY_EAST_BOUND_LONGITUDE, Type.WARN, TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);
            validator.validate(n, southXpath, southTagKey, GERMANY_SOUTH_BOUND_LATITUDE, Type.WARN, TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO);
            validator.validate(n, northXpath, northTagKey, GERMANY_NORTH_BOUND_LATITUDE, Type.WARN, TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO);
        }
    }

    /*
    private void validateTemporalResolution(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.temporal_resolution", "Temporal resolution");
        String accuracyXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/* /gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_AccuracyOfATimeMeasurement";
        List<Node> accuracyNodes = dom4jDoc.selectNodes(accuracyXpath);
        if (accuracyNodes == null || accuracyNodes.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    tagName + ": Element missing.",
                    tagName,
                    accuracyXpath);
            return;
        }

        // Validate the units
        String unitsXpath = "./gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/* /gml:catalogSymbol";
        accuracyNodes.forEach(e -> {
            String unitsTagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.temporal_resolution.units", "Temporal resolution units");
            Node unitsTag = e.selectSingleNode(unitsXpath);
            if (unitsTag == null) return;

            String units = unitsTag.getText().trim();
            if (units.equals("s")) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Valid metadata standard version",
                        unitsTagName,
                        units,
                        unitsTag.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.element.value.invalid",
                        "Invalid metadata standard version",
                        unitsTagName,
                        units,
                        unitsTag.getUniquePath(),
                        "s");
            }
        });
    }

    private void validateDgsParameterName(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.dgs.parameter_name", "Parameter or variable name");
        String accuracyXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/* /gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result/gmd:DQ_QuantitativeResult";
        List<Node> accuracyNodes = dom4jDoc.selectNodes(accuracyXpath);
        if (accuracyNodes == null) return; // nothing to do if no DGS elements present

        accuracyNodes.forEach(e -> {
            String paramXpath = "./gmd:valueType/gco:RecordType";
            Node paramNode = e.selectSingleNode(paramXpath);
            if (paramNode == null) {
                reportHelper.fail(
                        "validation.baw.element.value.dgs.parameter_name.missing",
                        "DGS parameter/variable name missing",
                        tagName,
                        e.getUniquePath());
            } else {
                reportHelper.pass(
                        "validation.baw.element.value.dgs.parameter_name.found",
                        "DGS parameter/variable name found",
                        tagName,
                        paramNode.getText(),
                        paramNode.getUniquePath());
            }
        });
    }

    private void validateDgsRole(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.dgs.role", "Role");
        String dqXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/* /gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/../..";
        List<Node> dqNodes = dom4jDoc.selectNodes(dqXpath);
        if (dqNodes == null) return; // nothing to do if no DGS elements present

        dqNodes.forEach(e -> {
            String roleXpath = "./gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString";
            Node roleNode = e.selectSingleNode(roleXpath);
            if (roleNode == null) {
                reportHelper.fail(
                        "validation.baw.element.value.dgs.role.missing",
                        "DGS role missing",
                        tagName,
                        e.getUniquePath());
            } else {
                reportHelper.pass(
                        "validation.baw.element.value.dgs.role.found",
                        "DGS role found",
                        tagName,
                        roleNode.getText(),
                        roleNode.getUniquePath());
            }
        });
        // TODO validate role values come from an enumeration after codelist exists
    }

    private Element getElementOrReportError(
            Document dom4jDoc,
            String xpath,
            String tagKey,
            ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString(tagKey, tagKey);
        Element elem = (Element) dom4jDoc.selectSingleNode(xpath);
        if (elem == null) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    tagName + ": Element missing.",
                    tagName,
                    xpath);
        }
        return elem;
    }
    */

}
