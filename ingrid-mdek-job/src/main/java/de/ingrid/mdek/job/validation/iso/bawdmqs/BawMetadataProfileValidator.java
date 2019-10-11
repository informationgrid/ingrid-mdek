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
package de.ingrid.mdek.job.validation.iso.bawdmqs;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQuery;
import java.util.List;

/**
 * Interface for defining rules for validating ISO 19115:2003/Corrigendum
 * 1:2006(E) XML-files.
 *
 * @author Vikram Notay
 */
public final class BawMetadataProfileValidator extends  AbstractIsoValidator {

    private static final Logger LOG = Logger.getLogger(BawMetadataProfileValidator.class);

    private static final String CODELIST_BASE_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml";

    private static final String BAW_EMAIL = "info@baw.de";
    private static final String BAW_URL = "(?:https?://)?(?:www.)?baw.de/?";
    private static final String BAW_NAME = "Bundesanstalt für Wasserbau";
    private static final String BAW_MD_STANDARD_NAME = "ISO 19115; ?GDI-BAW";
    private static final String BAW_MD_STANDARD_VERSION = "2003\\(E\\)/Cor.1:2006\\(E\\); ?1.3:2019";
    private static final String BAW_AUFTRAGSNR = "(?:B\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{5}|A\\d{11})";
    private static final String BWASTR_KM_KM = "\\d{4}-\\d{1,4}-\\d{1,4}";


    public BawMetadataProfileValidator() {
    }

    @Override
    List<ValidationReportItem> validate(org.w3c.dom.Document w3cDoc) {
        DOMReader reader = new DOMReader();
        Document dom4jDoc = reader.read(w3cDoc);
        ValidationReportHelper reportHelper = new ValidationReportHelper();

        validateFileIdentifier(dom4jDoc, reportHelper);
        validateMetadataLanguage(dom4jDoc, reportHelper);
        validateDatasetLanguage(dom4jDoc, reportHelper);
        validateMetadataCharset(dom4jDoc, reportHelper);
        validateDatasetCharset(dom4jDoc, reportHelper);
        validateHierarchyLevel(dom4jDoc, reportHelper);
        // TODO hierarchy level name
        //validateParentIdentifier(dom4jDoc, reportHelper);
        validateMdContactDetails(dom4jDoc, reportHelper);
        validateDatsetContactDetails(dom4jDoc, reportHelper);
        validateMdDatestamp(dom4jDoc, reportHelper);
        validateDatasetDatestamp(dom4jDoc, reportHelper);
        validateMdStandardName(dom4jDoc, reportHelper);
        validateMdStandardVersion(dom4jDoc, reportHelper);
        validateAuftragsNummer(dom4jDoc, reportHelper);
        validateGeographicIdentifier(dom4jDoc, reportHelper);
        validateGeographicBoundingBox(dom4jDoc, reportHelper);
        validateTemporalResolution(dom4jDoc, reportHelper);
        validateDgsParameterName(dom4jDoc, reportHelper);
        validateDgsRole(dom4jDoc, reportHelper);

        return reportHelper.getReport();
    }

    private void validateFileIdentifier(Document document, ValidationReportHelper reportHelper) {
        String name = ValidationReportHelper.getLocalisedString("validation.iso.tag.fileIdentifier", "fileIdentifier");
        String xpath = "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    "File identifier missing",
                    name,
                    xpath);
            return;
        }
        String fileIdentifier = node.getText();

        if (ValidationReportHelper.isValidUuid(fileIdentifier)) {
            reportHelper.pass(
                    "validation.iso.element.valid",
                    "File identifier valid",
                    name);
        } else {
            reportHelper.fail(
                    "validation.iso.element.uuid.invalid",
                    "Invalid file identifier",
                    name,
                    fileIdentifier,
                    xpath);
        }
    }

    private void validateMetadataLanguage(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpath = "/gmd:MD_Metadata/gmd:language/gmd:LanguageCode";
        validateLanguageCode(dom4jDoc, xpath, reportHelper);
    }

    private void validateDatasetLanguage(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpath = "//gmd:identificationInfo/*/gmd:language/gmd:LanguageCode";
        validateLanguageCode(dom4jDoc, xpath, reportHelper);
    }

    private void validateLanguageCode(Document dom4jDoc, String xpath, ValidationReportHelper reportHelper) {
        String tagKey = "validation.iso.tag.language";
        String langCodelist = CODELIST_BASE_URL + "#LanguageCode";
        new CodelistValidator(tagKey, dom4jDoc, xpath, reportHelper)
                .reportAs(ValidationReportItem.ReportLevel.WARN)
                .validCodelistLocation(langCodelist)
                .validCodelistValuePattern("(ger|eng)")
                .validate();
    }

    private void validateMetadataCharset(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpath = "/gmd:MD_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode";
        validateCharset(dom4jDoc, xpath, reportHelper);
    }

    private void validateDatasetCharset(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpath = "//gmd:identificationInfo/*/gmd:characterSet/gmd:MD_CharacterSetCode";
        validateCharset(dom4jDoc, xpath, reportHelper);
    }

    private void validateCharset(Document dom4jDoc, String xpath, ValidationReportHelper reportHelper) {
        String tagKey = "validation.iso.tag.charset";
        String charsetCodelist = CODELIST_BASE_URL + "#MD_CharacterSetCode";
        new CodelistValidator(tagKey, dom4jDoc, xpath, reportHelper)
                .reportAs(ValidationReportItem.ReportLevel.WARN)
                .validCodelistLocation(charsetCodelist)
                .validCodelistValuePattern("(?i:utf-?8)")
                .validate();
    }

    private void validateHierarchyLevel(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String xpath = "/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode";
        String tagKey = "validation.iso.tag.hierarchy_level";
        String hierarchyLevelCodelist = CODELIST_BASE_URL + "#MD_ScopeCode";
        new CodelistValidator(tagKey, dom4jDoc, xpath, reportHelper)
                .reportAs(ValidationReportItem.ReportLevel.FAIL)
                .validCodelistLocation(hierarchyLevelCodelist)
                .validCodelistValuePattern("(dataset|model)")
                .validate();
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

    private void validateMdContactDetails(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String xpath = "/gmd:MD_Metadata/gmd:contact";
        validateContactDetails(dom4jdoc, xpath, reportHelper);
    }

    private void validateDatsetContactDetails(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact";
        validateContactDetails(dom4jdoc, xpath, reportHelper);
    }

    private void validateContactDetails(Document dom4jdoc, String xpath, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.contact", "contact");
        List<Node> mdContacts = dom4jdoc.selectNodes(xpath);
        if (mdContacts == null || mdContacts.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    "Metadata contact missing",
                    tagName,
                    xpath);
        } else {
            for(int i=1; i<=mdContacts.size(); i++) {
                validateContactUuid(dom4jdoc, xpath, i, reportHelper);
                validateContactEmailAddress(dom4jdoc, xpath, i, reportHelper);
                validateContactUrl(dom4jdoc, xpath, i, reportHelper);
                validateContactOrganisationName(dom4jdoc, xpath, i, reportHelper);
            }
        }
    }

    private void validateContactUuid(Document dom4jdoc, String xpath, int index, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.ci_responsible_party", "CI_ResponsibleParty");
        xpath = String.format("%s[%d]/gmd:CI_ResponsibleParty/@uuid", xpath, index);
        Node node = dom4jdoc.selectSingleNode(xpath);
        if (node == null) {
            reportHelper.warn(
                    "validation.iso.element.uuid.missing",
                    "Missing contact UUID",
                    tagName,
                    xpath);
        } else {
            String uuid = node.getText();
            if (ValidationReportHelper.isValidUuid(uuid)) {
                reportHelper.pass(
                        "validation.iso.element.uuid.valid",
                        "Valid contact UUID",
                        tagName,
                        node.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.element.uuid.invalid",
                        "Invalid contact UUID",
                        tagName,
                        uuid,
                        node.getUniquePath());
            }
        }
    }

    private void validateContactEmailAddress(Document dom4jdoc, String xpath, int index, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.email", "electronicMailAddress");
        String xp = String.format(
                "%s[%d]/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString",
                xpath,
                index);

        List<Node> nodes = dom4jdoc.selectNodes(xp);
        if (nodes == null || nodes.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    "E-Mail address is missing",
                    tagName,
                    xp);
        } else {
            nodes.forEach(e -> {
                String email = e.getText();
                if (email == null || email.isEmpty()) {
                    reportHelper.fail(
                            "validation.iso.element.missing",
                            "E-Mail address is missing",
                            tagName,
                            e.getUniquePath());
                } else if (BAW_EMAIL.equals(email)) {
                    reportHelper.pass(
                            "validation.iso.element.valid",
                            "E-Mail address is valid",
                            tagName,
                            email,
                            e.getUniquePath());
                } else {
                    reportHelper.warn(
                            "validation.iso.element.value.unexpected",
                            "Unexpected E-Mail address",
                            tagName,
                            email,
                            e.getUniquePath(),
                            BAW_EMAIL);
                }
            });
        }
    }

    private void validateContactUrl(Document dom4jdoc, String xpath, int index, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.url", "URL");
        xpath = String.format(
                "%s[%d]/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
                xpath,
                index);

        Element element = (Element) dom4jdoc.selectSingleNode(xpath);
        if (element == null) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    "E-Mail address is missing",
                    tagName,
                    xpath);
        } else {
            String url = element.getText();
            if (url != null && url.matches(BAW_URL)) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "E-Mail address is valid",
                        tagName,
                        url,
                        element.getUniquePath());
            } else {
                reportHelper.warn(
                        "validation.iso.element.value.unexpected",
                        "Unexpected E-Mail address",
                        tagName,
                        url,
                        xpath,
                        BAW_URL);
            }
        }
    }

    private void validateContactOrganisationName(Document dom4jdoc, String xpath, int index, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.organisation_name", "Organisation name");
        xpath = String.format(
                "%s[%d]/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString",
                xpath,
                index);

        Element element = (Element) dom4jdoc.selectSingleNode(xpath);
        if (element == null) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    "Organisation name is missing",
                    tagName,
                    xpath);
        } else {
            String org = element.getText();
            if (org != null && org.matches(BAW_NAME)) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Organisation name is valid",
                        tagName,
                        org,
                        element.getUniquePath());
            } else {
                reportHelper.warn(
                        "validation.iso.element.value.unexpected",
                        "Unexpected organisation name",
                        tagName,
                        org,
                        element.getUniquePath(),
                        BAW_URL);
            }
        }
    }

    private void validateMdDatestamp(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.dateStamp", "dateStamp");
        validateDatestamp(dom4jdoc, "/gmd:MD_Metadata/gmd:dateStamp", tagName, reportHelper);
    }

    private void validateDatasetDatestamp(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.date", "date");
        validateDatestamp(dom4jdoc, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:date", tagName, reportHelper);
    }

    private void validateDatestamp(Document dom4jdoc, String xpath, String tagName, ValidationReportHelper reportHelper) {
        String xp = String.format("%s/gco:Date|%s/gco:DateTime", xpath, xpath);
        List<Node> nodes = dom4jdoc.selectNodes(xp);
        for (int i=0; nodes != null && i<nodes.size(); i++) {
            boolean valid = false;
            String dt = nodes.get(i).getText();

            // Dates without time
            valid = valid || canParse(
                    dt,
                    LocalDate::from,
                    DateTimeFormatter.ISO_DATE,
                    DateTimeFormatter.BASIC_ISO_DATE);
            // Dates with time without time zone
            valid = valid || canParse(
                    dt,
                    LocalDateTime::from,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // Date + time + timezone
            valid = valid || canParse(
                    dt,
                    ZonedDateTime::from,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                    DateTimeFormatter.ISO_ZONED_DATE_TIME,
                    DateTimeFormatter.ISO_INSTANT);

            if (valid) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Date/DateTime is valid",
                        tagName,
                        dt,
                        nodes.get(i).getUniquePath());
            } else {
                reportHelper.warn(
                        "validation.iso.element.date.invalid",
                        "Unexpected organisation name",
                        tagName,
                        dt,
                        nodes.get(i).getUniquePath());
            }
        }
    }

    private boolean canParse(String dt, TemporalQuery<?> query, DateTimeFormatter... formatters) {
        boolean valid = false;
        for (int i=0; !valid && i<formatters.length; i++) {
            valid = canParse(dt, formatters[i], query);
        }
        return valid;
    }

    private boolean canParse(String date, DateTimeFormatter formatter, TemporalQuery<?> query) {
        try {
            formatter.parse(date, query);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void validateMdStandardName(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.metadata_standard_name", "metadataStandardName");
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString";
        Element element = getElementOrReportError(dom4jdoc, xpath, tagName, reportHelper);
        if (element != null) {
            String name = element.getTextTrim();
            if (name.matches(BAW_MD_STANDARD_NAME)) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Valid metadata standard name",
                        tagName,
                        name,
                        element.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.element.value.invalid",
                        "Invalid metadata standard name",
                        tagName,
                        name,
                        element.getUniquePath(),
                        BAW_MD_STANDARD_NAME);
            }
        }
    }

    private void validateMdStandardVersion(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.metadata_standard_version", "metadataStandardVersion");
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardVersion/gco:CharacterString";
        Element element = getElementOrReportError(dom4jdoc, xpath, "validation.iso.tag.metadata_standard_version", reportHelper);
        if (element != null) {
            String version = element.getTextTrim();
            if (version.matches(BAW_MD_STANDARD_VERSION)) {
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Invalid metadata standard version",
                        tagName,
                        version,
                        element.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.element.value.invalid",
                        "Valid metadata standard version",
                        tagName,
                        version,
                        element.getUniquePath(),
                        BAW_MD_STANDARD_VERSION);
            }
        }
    }

    private void validateAuftragsNummer(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.auftragsnr", "BAW Auftragsnr.");
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/*[./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/text()='DEBUNDBAWAUFTRAGNR']/gmd:keyword/gco:CharacterString";
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

    private void validateGeographicIdentifier(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.geographic_identifier", "geographicIdentifier");
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
        List<Node> nodes = dom4jdoc.selectNodes(xpath);
        if (nodes == null || nodes.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    tagName + ": Element missing.",
                    tagName,
                    xpath);
            return;
        }
        // Check within the found geographic identifiers for pattern BWaStr-Km-Km
        boolean found = false;
        for(int i=0; !found && i<nodes.size(); i++) {
            String ident = nodes.get(i).getText();
            found = found || ident.matches(BWASTR_KM_KM);

            if (found) {
                reportHelper.pass(
                        "validation.baw.element.value.bwastr_km_km.found",
                        "Geographic identifier valid",
                        tagName,
                        ident,
                        nodes.get(i).getUniquePath());
            }
            if (!found) {
                reportHelper.warn(
                        "validation.baw.element.value.bwastr_km_km.missing",
                        "Geographic identifier for pattern 'BWastr.-Km-Km' missing",
                        tagName,
                        xpath);
            }
        }
    }

    private void validateGeographicBoundingBox(Document dom4jdoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.geographic_bounding_box", "EX_GeographicBoundingBox");
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
        List<Node> nodes = dom4jdoc.selectNodes(xpath);
        if (nodes == null || nodes.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    tagName + ": Element missing.",
                    tagName,
                    xpath);
            return;
        }
        // Test if values within the bounding box are valid lat/long vlaues
        for(int i=0; i<nodes.size(); i++) {
            validateGeographicBound(nodes.get(i), "westBoundLongitude", "validation.iso.tag.bbox.west_bound", GeographicBoundType.LONGITUDE, reportHelper);
            validateGeographicBound(nodes.get(i), "eastBoundLongitude", "validation.iso.tag.bbox.east_bound", GeographicBoundType.LONGITUDE, reportHelper);
            validateGeographicBound(nodes.get(i), "southBoundLatitude", "validation.iso.tag.bbox.south_bound", GeographicBoundType.LATITUDE, reportHelper);
            validateGeographicBound(nodes.get(i), "northBoundLatitude", "validation.iso.tag.bbox.north_bound", GeographicBoundType.LATITUDE, reportHelper);
        }
    }

    private void validateGeographicBound(Node ancestor, String tag, String tagKey, GeographicBoundType type, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString(tagKey, tagKey);
        String xpath = String.format(
                "%s/gmd:%s/gco:Decimal",
                "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                tag);
        Node node = ancestor.selectSingleNode(xpath);
        if (node == null) {
            reportHelper.fail(
                    "validation.iso.element.missing",
                    tag + ": Geographic bound missing.",
                    tagName,
                    xpath);
            return;
        } else {
            String valueString = node.getText().trim();
            double value = Double.parseDouble(valueString);

            // Check absolute bounds
            if (value < type.getMinValue() || value > type.getMaxValue()) {
                reportHelper.fail(
                        "validation.iso.element.value.invalid",
                        "Invalid value",
                        tagName,
                        valueString,
                        node.getUniquePath(),
                        type.getValidBoundsString());
            } else if (value < type.getGermanMinValue() || value > type.getGermanMaxValue()) { // Absolute bounds are okay. Check German bounds
                reportHelper.warn(
                        "validation.iso.bbox.value.outside_germany",
                        "Geographic bounds outside Germany",
                        tagName,
                        valueString,
                        node.getUniquePath(),
                        type.getGermanBoundsString());
            } else { // Value is okay
                reportHelper.pass(
                        "validation.iso.element.valid",
                        "Geographic bounding box valid",
                        tagName,
                        valueString,
                        node.getUniquePath());
            }
        }
    }

    private void validateTemporalResolution(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.baw.tag.temporal_resolution", "Temporal resolution");
        String accuracyXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/*/gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_AccuracyOfATimeMeasurement";
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
        String unitsXpath = "./gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/*/gml:catalogSymbol";
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
        String accuracyXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/*/gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result/gmd:DQ_QuantitativeResult";
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
        String dqXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/*/gmd:level/gmd:MD_ScopeCode/@codeListValue='model']/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/../..";
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

    private class CodelistValidator {
        private final String tagName;
        private final Document document;
        private final String xpath;
        private final ValidationReportHelper reportHelper;

        private ValidationReportItem.ReportLevel level;
        private String validCodelistLocation;
        private String validCodelistValuePattern;

        private CodelistValidator(String tagKey, Document document, String xpath, ValidationReportHelper reportHelper) {
            this.tagName = ValidationReportHelper.getLocalisedString(tagKey, tagKey);
            this.document = document;
            this.xpath = xpath;
            this.reportHelper = reportHelper;
        }

        private void validate() {
            Element elem = getElementOrReportError(
                    document,
                    xpath,
                    tagName,
                    reportHelper);

            if (elem != null) {
                validateCodelistLocation(elem);
                validateCodelistValue(elem);
            }
        }

        private void validateCodelistLocation(Element element) {
            // Validate the "codeList" attribute
            String codeList = element.attributeValue("codeList");
            if (validCodelistLocation.equals(codeList)) {
                reportHelper.pass(
                        "validation.iso.codelist.valid.location",
                        "Valid codelist",
                        tagName,
                        codeList,
                        element.getUniquePath());
            } else {
                reportHelper.fail(
                        "validation.iso.codelist.invalid.location",
                        "Invalid codelist",
                        tagName,
                        codeList,
                        element.getUniquePath(),
                        validCodelistLocation);
            }
        }

        private void validateCodelistValue(Element element) {
            String codeListValue = element.attributeValue("codeListValue");
            if (codeListValue.matches(validCodelistValuePattern)) {
                reportHelper.pass(
                        "validation.iso.codelist.valid.value",
                        "Valid codelist value",
                        tagName,
                        codeListValue,
                        element.getUniquePath());
            } else if (level == ValidationReportItem.ReportLevel.FAIL) {
                reportHelper.fail(
                        "validation.iso.codelist.invalid.value",
                        "Invalid codelist value",
                        tagName,
                        codeListValue,
                        element.getUniquePath(),
                        validCodelistValuePattern);
            } else {
                reportHelper.warn(
                        "validation.iso.codelist.unexpected.value",
                        "Unexpected codelist value",
                        tagName,
                        codeListValue,
                        element.getUniquePath(),
                        validCodelistValuePattern);
            }
        }

        public CodelistValidator reportAs(ValidationReportItem.ReportLevel level) {
            this.level = level;
            return this;
        }

        public CodelistValidator validCodelistLocation(String codelist) {
            this.validCodelistLocation = codelist;
            return this;
        }

        public CodelistValidator validCodelistValuePattern(String codelistValue) {
            this.validCodelistValuePattern = codelistValue;
            return this;
        }
    }

    private enum GeographicBoundType {
        LATITUDE,
        LONGITUDE;

        public double getMinValue() {
            if (this == LATITUDE) {
                return -90.0;
            } else {
                return -180.0;
            }
        }

        public double getMaxValue() {
            if (this == LATITUDE) {
                return 90.0;
            } else {
                return 180.0;
            }
        }

        public double getGermanMinValue() {
            if (this == LATITUDE) {
                return 47.0;
            } else {
                return 5.0;
            }
        }

        public double getGermanMaxValue() {
            if (this == LATITUDE) {
                return 56.0;
            } else {
                return 16.0;
            }
        }

        public String getValidBoundsString() {
            return String.format("[%f, %f]", getMinValue(), getMaxValue());
        }

        public String getGermanBoundsString() {
            return String.format("[%f, %f]", getGermanMinValue(), getGermanMaxValue());
        }
    }

}
