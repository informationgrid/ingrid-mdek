/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static de.ingrid.mdek.job.validation.iso.bawdmqs.ValidatorTestsTemplateHelper.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Vikram Notay
 */
public class BawMetadataProfileValidatorTests {

    private BawMetadataProfileValidator validator;

    @Before
    public void init() {
        validator = new BawMetadataProfileValidator();
    }

    @Test
    public void testControlDocumentValidity() throws ParserConfigurationException, SAXException, IOException {
        Document validDoc = defaultDocument();
        assertTrue(isValid(validDoc));
    }

    @Test
    public void testFileIdentifier() throws Exception {
        Document invalid = documentWithReplacedValues(IDX_FILE_IDENTIFIER, "Not a valid UUID");
        assertFalse(isValid(invalid));
    }

    @Test
    public void testMissingFileIdentifier() throws Exception {
        String element = String.format(
                "<gmd:fileIdentifier>\n"
                        + "\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t</gmd:fileIdentifier>\n",
                IDX_FILE_IDENTIFIER);
        String template = fetchTemplateString()
                .replace(element, "");
        Document invalid = documentFromTemplate(template);
        assertFalse(isValid(invalid));
    }

    @Test
    public void testMissingMetadataLanguageElement() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:language>\n\t\t<gmd:LanguageCode codeList=\"{%d}\" codeListValue=\"{%d}\">{%d}</gmd:LanguageCode>\n\t</gmd:language>",
                IDX_MD_LANG_CODELIST,
                IDX_MD_LANG_VALUE,
                IDX_MD_LANG_VALUE);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMissingMdIdentifierInfoLanguageElement() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:language>\n"
                        + "\t\t\t\t<gmd:LanguageCode codeList=\"{%d}\" codeListValue=\"{%d}\">{%d}</gmd:LanguageCode>\n"
                        + "\t\t\t</gmd:language>",
                IDX_DS_LANG_CODELIST,
                IDX_DS_LANG_VALUE,
                IDX_DS_LANG_VALUE);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMissingMetadataCharsetValue() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:characterSet>\n"
                        + "\t\t<gmd:MD_CharacterSetCode codeList=\"{%d}\" codeListValue=\"{%d}\"/>\n"
                        + "\t</gmd:characterSet>",
                IDX_MD_CHARSET_CODELIST,
                IDX_MD_CHARSET_VALUE);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMissingMdIdentifierInfoCharsetElement() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:characterSet>\n"
                        + "\t\t\t\t<gmd:MD_CharacterSetCode codeList=\"{%d}\" codeListValue=\"{%d}\"/>\n"
                        + "\t\t\t</gmd:characterSet>",
                IDX_DS_CHARSET_CODELIST,
                IDX_DS_CHARSET_VALUE);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testHierarchyLevelValue() throws ParserConfigurationException, SAXException, IOException {
        Document invalidDoc = documentWithReplacedValues(IDX_MD_HIERARCHY_LEVEL_VALUE, "collectionHardware");
        assertFalse(isValid(invalidDoc));
    }
    
    @Test
    public void testMissingHierarchyLevelElement() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:hierarchyLevel>\n"
                        + "\t\t<gmd:MD_ScopeCode codeList=\"{%d}\" codeListValue=\"{%d}\">{%d}</gmd:MD_ScopeCode>\n"
                        + "\t</gmd:hierarchyLevel>",
                IDX_MD_HIERARCHY_LEVEL_CODELIST,
                IDX_MD_HIERARCHY_LEVEL_VALUE,
                IDX_MD_HIERARCHY_LEVEL_VALUE);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testParentIdentifierAuftragUndefined0() throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(IDX_MD_HIERARCHY_LEVEL_NAME_VALUE, "Auftrag");

        // Type "Auftrag" shouldn't have a parent
        assertFalse(isValid(doc));
    }

    @Test
    public void testParentIdentifierAuftragUndefined1() throws IOException, ParserConfigurationException, SAXException {
        String template = fetchTemplateString();

        // Delete the parent id element
        String element = String.format(
                "<gmd:hierarchyLevelName>\n"
                        + "\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t</gmd:hierarchyLevelName>",
                IDX_MD_HIERARCHY_LEVEL_NAME_VALUE);
        template = template.replace(element, "");
        Document doc = documentFromTemplateWithReplacedValues(template, IDX_MD_HIERARCHY_LEVEL_NAME_VALUE, "Auftrag");
        assertTrue(isValid(doc));

        // For all others, a parent identifier should be defined in v 1.3
        /*
        vals[IDX_MD_HIERARCHY_LEVEL_NAME_VALUE] = "Simulationslauf";
        assertFalse(validator.isValid(doc));
        */
    }

    @Test
    public void testParentIdentifierNotAuftragDefined() throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(IDX_MD_HIERARCHY_LEVEL_NAME_VALUE, "Simulationslauf");
        assertTrue(isValid(doc));
    }

    @Test
    public void testParentIdentifierIsValidUuid() throws ParserConfigurationException, SAXException, IOException {
        // Parent ID should be a valid UUID
        Document doc = documentWithReplacedValues(IDX_PARENT_IDENTIFIER, "Invalid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMetadataContactUuidIsValid() throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(IDX_MD_CONTACT_UUID, "An invalid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMetadataContactHasEmail() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:electronicMailAddress>\n"
                        + "\t\t\t\t\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t\t\t</gmd:electronicMailAddress>\n",
                IDX_DS_CONTACT_ORG_EMAIL);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMdContactHasOrganisationName() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:organisationName>\n"
                        + "\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t</gmd:organisationName>\n",
               IDX_MD_CONTACT_ORG_NAME);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMetadataContactHasUrl() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:onlineResource>\n"
                        + "\t\t\t\t\t\t<gmd:CI_OnlineResource>\n"
                        + "\t\t\t\t\t\t\t<gmd:linkage>\n"
                        + "\t\t\t\t\t\t\t\t<gmd:URL>{%d}</gmd:URL>\n"
                        + "\t\t\t\t\t\t\t</gmd:linkage>\n"
                        + "\t\t\t\t\t\t</gmd:CI_OnlineResource>\n"
                        + "\t\t\t\t\t</gmd:onlineResource>\n",
                IDX_MD_CONTACT_ORG_URL);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMetadataStandardNameExists() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:metadataStandardName>\n"
                        + "\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t</gmd:metadataStandardName>\n",
                IDX_MD_STANDARD_NAME);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMetadataStandardNameIsValid() throws ParserConfigurationException, SAXException, IOException {
        checkFailureForInvalidValue(IDX_MD_STANDARD_NAME, "invalid standard name");
    }

    @Test
    public void testMetadataStandardVersionExists() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:metadataStandardVersion>\n"
                        + "\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t</gmd:metadataStandardVersion>\n",
                IDX_MD_STANDARD_VERSION);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMetadataStandardVersionIsValid() throws ParserConfigurationException, SAXException, IOException {
        checkFailureForInvalidValue(IDX_MD_STANDARD_VERSION, "invalid standard version");
    }

    @Test
    public void testIdInfoContactUuidIsValid() throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(IDX_DS_CONTACT_UUID, "An invalid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testIdInfoContactHasOrganisationName() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:organisationName>\n"
                        + "\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t</gmd:organisationName>\n",
                IDX_DS_CONTACT_ORG_NAME);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testIdInfoContactHasEmail() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:electronicMailAddress>\n"
                        + "\t\t\t\t\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t\t\t</gmd:electronicMailAddress>\n",
                IDX_DS_CONTACT_ORG_EMAIL);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testIdInfoContactHasUrl() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format(
                "<gmd:onlineResource>\n"
                        + "\t\t\t\t\t\t\t\t<gmd:CI_OnlineResource>\n"
                        + "\t\t\t\t\t\t\t\t\t<gmd:linkage>\n"
                        + "\t\t\t\t\t\t\t\t\t\t<gmd:URL>{%d}</gmd:URL>\n"
                        + "\t\t\t\t\t\t\t\t\t</gmd:linkage>\n"
                        + "\t\t\t\t\t\t\t\t</gmd:CI_OnlineResource>\n"
                        + "\t\t\t\t\t\t\t</gmd:onlineResource>\n",
                IDX_DS_CONTACT_ORG_URL);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMissingAuftragsNummer() throws IOException, ParserConfigurationException, SAXException {
        String template = fetchTemplateString();
        template = template.replaceFirst("(?s)<gmd:descriptiveKeywords>.*?DEBUNDBAWAUFTRAGNR.*?</gmd:descriptiveKeywords>", "");

        Document document = documentFromTemplate(template);
        assertFalse(isValid(document));
    }

    @Test
    public void testInvalidAuftragsNummer() throws IOException, SAXException, ParserConfigurationException {
        checkFailureForInvalidValue(IDX_BAW_AUFTRAGS_NR, "B3950.");
    }

    @Test
    public void testMissingGeographicIdentifier() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format("<gmd:geographicElement>\n"
                        + "\t\t\t\t\t\t<gmd:EX_GeographicDescription>\n"
                        + "\t\t\t\t\t\t\t<gmd:extentTypeCode>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Boolean>true</gco:Boolean>\n"
                        + "\t\t\t\t\t\t\t</gmd:extentTypeCode>\n"
                        + "\t\t\t\t\t\t\t<gmd:geographicIdentifier>\n"
                        + "\t\t\t\t\t\t\t\t<gmd:RS_Identifier>\n"
                        + "\t\t\t\t\t\t\t\t\t<gmd:code>\n"
                        + "\t\t\t\t\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t\t\t</gmd:code>\n"
                        + "\t\t\t\t\t\t\t\t</gmd:RS_Identifier>\n"
                        + "\t\t\t\t\t\t\t</gmd:geographicIdentifier>\n"
                        + "\t\t\t\t\t\t</gmd:EX_GeographicDescription>\n"
                        + "\t\t\t\t\t</gmd:geographicElement>\n",
                IDX_DS_GEOGRAPHIC_IDENTIFIER);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testMissingBoundingBox() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format("<gmd:geographicElement>\n"
                        + "\t\t\t\t\t\t<gmd:EX_GeographicBoundingBox>\n"
                        + "\t\t\t\t\t\t\t<gmd:extentTypeCode>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Boolean>true</gco:Boolean>\n"
                        + "\t\t\t\t\t\t\t</gmd:extentTypeCode>\n"
                        + "\t\t\t\t\t\t\t<gmd:westBoundLongitude>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Decimal>6.70249535409269</gco:Decimal>\n"
                        + "\t\t\t\t\t\t\t</gmd:westBoundLongitude>\n"
                        + "\t\t\t\t\t\t\t<gmd:eastBoundLongitude>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Decimal>{%d}</gco:Decimal>\n"
                        + "\t\t\t\t\t\t\t</gmd:eastBoundLongitude>\n"
                        + "\t\t\t\t\t\t\t<gmd:southBoundLatitude>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Decimal>{%d}</gco:Decimal>\n"
                        + "\t\t\t\t\t\t\t</gmd:southBoundLatitude>\n"
                        + "\t\t\t\t\t\t\t<gmd:northBoundLatitude>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Decimal>51.3270606108592</gco:Decimal>\n"
                        + "\t\t\t\t\t\t\t</gmd:northBoundLatitude>\n"
                        + "\t\t\t\t\t\t</gmd:EX_GeographicBoundingBox>\n"
                        + "\t\t\t\t\t</gmd:geographicElement>\n",
                IDX_DS_EAST_BOUND,
                IDX_DS_SOUTH_BOUND);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testInvalidBoundingBoxWorld0() throws IOException, SAXException, ParserConfigurationException {
        checkFailureForInvalidValue(IDX_DS_EAST_BOUND, "200");
    }

    @Test
    public void testInvalidBoundingBoxWorld1() throws IOException, SAXException, ParserConfigurationException {
        checkFailureForInvalidValue(IDX_DS_SOUTH_BOUND, "200");
    }

    @Test
    public void testMissingTemporalResolution() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format("<gmd:report>\n"
                        + "\t\t\t\t<gmd:DQ_AccuracyOfATimeMeasurement>\n"
                        + "\t\t\t\t\t<gmd:result>\n"
                        + "\t\t\t\t\t\t<gmd:DQ_QuantitativeResult>\n"
                        + "\t\t\t\t\t\t\t<gmd:valueUnit>\n"
                        + "\t\t\t\t\t\t\t\t<gml:UnitDefinition gml:id=\"seconds\">\n"
                        + "\t\t\t\t\t\t\t\t\t<gml:identifier codeSpace=\"\"/>\n"
                        + "\t\t\t\t\t\t\t\t\t<gml:catalogSymbol>{%d}</gml:catalogSymbol>\n"
                        + "\t\t\t\t\t\t\t\t</gml:UnitDefinition>\n"
                        + "\t\t\t\t\t\t\t</gmd:valueUnit>\n"
                        + "\t\t\t\t\t\t\t<gmd:value>\n"
                        + "\t\t\t\t\t\t\t\t<gco:Record>1.0</gco:Record>\n"
                        + "\t\t\t\t\t\t\t</gmd:value>\n"
                        + "\t\t\t\t\t\t</gmd:DQ_QuantitativeResult>\n"
                        + "\t\t\t\t\t</gmd:result>\n"
                        + "\t\t\t\t</gmd:DQ_AccuracyOfATimeMeasurement>\n"
                        + "\t\t\t</gmd:report>",
                IDX_TEMPORAL_RESOLUTION_UNITS);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testInvalidTemporalResolutionUnits() throws IOException, SAXException, ParserConfigurationException {
        checkFailureForInvalidValue(IDX_TEMPORAL_RESOLUTION_UNITS, "m");
    }

    @Test
    public void testDGSMissingParameterName() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format("<!-- Parametername -->\n"
                        + "\t\t\t\t\t\t\t<gmd:valueType>\n"
                        + "\t\t\t\t\t\t\t\t<gco:RecordType>{%d}</gco:RecordType>\n"
                        + "\t\t\t\t\t\t\t</gmd:valueType>",
                IDX_BAW_DGS_PARAMETER_NAME);
        checkFailureForDeletedElement(element);
    }

    @Test
    public void testDGSMissingRole() throws ParserConfigurationException, SAXException, IOException {
        String element = String.format("<!--Rolle-->\n"
                        + "\t\t\t<gmd:lineage>\n"
                        + "\t\t\t\t<gmd:LI_Lineage>\n"
                        + "\t\t\t\t\t<gmd:source xlink:href=\"http://wserv6x.baw.de/downloads/dummy-md-reference\">\n"
                        + "\t\t\t\t\t\t<gmd:LI_Source>\n"
                        + "\t\t\t\t\t\t\t<gmd:description>\n"
                        + "\t\t\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t</gmd:description>\n"
                        + "\t\t\t\t\t\t</gmd:LI_Source>\n"
                        + "\t\t\t\t\t</gmd:source>\n"
                        + "\t\t\t\t</gmd:LI_Lineage>\n"
                        + "\t\t\t</gmd:lineage>",
                IDX_BAW_DGS_ROLE);
        checkFailureForDeletedElement(element);
    }

    private void checkFailureForDeletedElement(String element) throws IOException, ParserConfigurationException, SAXException {
        String template = fetchTemplateString();
        template = template.replace(element, "");

        Document doc = documentFromTemplate(template);
        assertFalse(isValid(doc));
    }

    private void checkFailureForInvalidValue(int index, String value) throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(index, value);
        assertFalse(isValid(doc));
    }

    private boolean isValid(Document document) {
        return validator.validate(document)
                .stream()
                .noneMatch(e -> e.getLevel() == ValidationReportItem.ReportLevel.FAIL);
    }

}
