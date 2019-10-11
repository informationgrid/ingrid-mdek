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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static de.ingrid.mdek.job.validation.iso.bawdmqs.ValidatorTestsTemplateHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 *
 * @author Vikram Notay
 */
public class BawMetadataProfileValidatorTests {

    private BawMetadataProfileValidator validator;
    private Document defaultDocument;
    private static final Logger LOG = Logger.getLogger(BawMetadataProfileValidatorTests.class);

    public BawMetadataProfileValidatorTests() {
        try {
            defaultDocument = ValidatorTestsTemplateHelper.defaultDocument();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.error("Error while reading and creating default template document for validation.", e);
        }
    }

    @Before
    public void init() {
        validator = new BawMetadataProfileValidator();
    }

    @Test
    public void testControlDocumentValidity() {
        assertTrue(isValid(defaultDocument));
    }

    @Test
    public void testFileIdentifier() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";
        setTextForElementAtXpath(doc, xpath, "Not a valid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMissingFileIdentifier() {
        String xpath = "/gmd:MD_Metadata/gmd:fileIdentifier";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMissingMetadataLanguageElement() {
        String xpath = "/gmd:MD_Metadata/gmd:language";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMissingMdIdentifierInfoLanguageElement() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:language";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMissingMetadataCharsetValue() {
        String xpath = "/gmd:MD_Metadata/gmd:characterSet";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMissingMdIdentifierInfoCharsetElement() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:characterSet";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testHierarchyLevelValue() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode";
        String newVal = "collectionHardware";
        setAttributeForElementAtXpath(doc, xpath, "codeListValue", newVal);
        assertFalse(isValid(doc));
    }
    
    @Test
    public void testMissingHierarchyLevelElement() {
        String xpath = "/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    @Ignore("Validation of parent identifier disabled")
    public void testParentIdentifierAuftragUndefined0() {
        /*
        Document doc = documentWithReplacedValues(IDX_MD_HIERARCHY_LEVEL_NAME_VALUE, "Auftrag");

        // Type "Auftrag" shouldn't have a parent
        assertFalse(isValid(doc));
        */
    }

    @Test
    @Ignore("Validation of parent identifier disabled")
    public void testParentIdentifierAuftragUndefined1() {
        /*
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
        / *
        vals[IDX_MD_HIERARCHY_LEVEL_NAME_VALUE] = "Simulationslauf";
        assertFalse(validator.isValid(doc));
        * /
         */
    }

    @Test
    @Ignore("Validation of parent identifier disabled")
    public void testParentIdentifierNotAuftragDefined() {
        /*
        Document doc = documentWithReplacedValues(IDX_MD_HIERARCHY_LEVEL_NAME_VALUE, "Simulationslauf");
        assertTrue(isValid(doc));
        */
    }

    @Test
    @Ignore("Validation of parent identifier disabled")
    public void testParentIdentifierIsValidUuid() {
        /*
        // Parent ID should be a valid UUID
        Document doc = documentWithReplacedValues(IDX_PARENT_IDENTIFIER, "Invalid UUID");
        assertFalse(isValid(doc));
        */
    }

    @Test
    public void testMetadataContactUuidIsValid() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty";
        setAttributeForElementAtXpath(doc, xpath, "uuid", "An invalid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMetadataContactHasEmail() {
        String xpath = "/gmd:MD_Metadata/gmd:contact//gmd:electronicMailAddress";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMdContactHasOrganisationName() {
        String xpath = "/gmd:MD_Metadata/gmd:contact/*/gmd:organisationName";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMetadataContactHasUrl() {
        String xpath = "/gmd:MD_Metadata/gmd:contact//gmd:onlineResource";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMetadataStandardNameExists() {
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardName";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMetadataStandardNameIsValid() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardName";
        setTextForElementAtXpath(doc, xpath, "invalid standard name");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMetadataStandardVersionExists() {
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardVersion";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMetadataStandardVersionIsValid() {
        String xpath = "/gmd:MD_Metadata/gmd:metadataStandardVersion";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testIdInfoContactUuidIsValid() throws ParserConfigurationException, SAXException, IOException {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty";
        setAttributeForElementAtXpath(doc, xpath, "uuid", "An invalid UUID");
        assertFalse(isValid(doc));
    }

    @Test
    public void testIdInfoContactHasOrganisationName() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/*/gmd:organisationName";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testIdInfoContactHasEmail() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact//gmd:electronicMailAddress";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testIdInfoContactHasUrl() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact//gmd:onlineResource";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    @Ignore("Needs to be updated according to new requirements")
    public void testMissingAuftragsNummer() {
        /*
        String template = fetchTemplateString();
        template = template.replaceFirst("(?s)<gmd:descriptiveKeywords>.*?DEBUNDBAWAUFTRAGNR.*?</gmd:descriptiveKeywords>", "");

        Document document = documentFromTemplate(template);
        assertFalse(isValid(document));
        */
    }

    @Test
    @Ignore("Needs to be updated according to new requirements")
    public void testInvalidAuftragsNummer() {
        //checkFailureForInvalidValue(IDX_BAW_AUFTRAGS_NR, "B3950.");
    }

    @Test
    public void testMissingGeographicIdentifier() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement[./gmd:EX_GeographicDescription]";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testMissingBoundingBox() {
        String xpath = "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement[./gmd:EX_GeographicBoundingBox]";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testInvalidBoundingBoxWorld0() throws IOException, SAXException, ParserConfigurationException {
        Document doc = defaultDocument;
        String xpath = "//gmd:eastBoundLongitude/gco:Decimal";
        setTextForElementAtXpath(doc, xpath, "200");
        assertFalse(isValid(doc));
    }

    @Test
    public void testInvalidBoundingBoxWorld1() throws IOException, SAXException, ParserConfigurationException {
        Document doc = defaultDocument;
        String xpath = "//gmd:southBoundLatitude/gco:Decimal";
        setTextForElementAtXpath(doc, xpath, "200");
        assertFalse(isValid(doc));
    }

    @Test
    public void testMissingTemporalResolution() {
        String xpath = "//gmd:report[./gmd:DQ_AccuracyOfATimeMeasurement]";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    @Ignore("Needs to be updated for xlink:href")
    public void testInvalidTemporalResolutionUnits() throws IOException, SAXException, ParserConfigurationException {
        //checkFailureForInvalidValue(IDX_TEMPORAL_RESOLUTION_UNITS, "m");
    }

    @Test
    public void testDGSMissingParameterName() {
        String xpath = "//gmd:valueType";
        checkFailureForDeletedElement(xpath);
    }

    @Test
    public void testDGSMissingRole() {
        String xpath = "//gmd:DQ_DataQuality[./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy]/gmd:lineage";
        checkFailureForDeletedElement(xpath);
    }

    private void checkFailureForDeletedElement(String xpath) {
        Document doc = defaultDocument;
        Node removed = removeElementAtXpath(doc, xpath);

        assertThat(removed, is(notNullValue())); // Check that a node was really removed
        assertFalse(isValid(doc)); // Check that the document is no longer valid
    }

    /*
    private void checkFailureForInvalidValue(int index, String value) throws ParserConfigurationException, SAXException, IOException {
        Document doc = documentWithReplacedValues(index, value);
        assertFalse(isValid(doc));
    }
    */

    private boolean isValid(Document document) {
        return validator.validate(document)
                .stream()
                .noneMatch(e -> e.getLevel() == ValidationReportItem.ReportLevel.FAIL);
    }

}
