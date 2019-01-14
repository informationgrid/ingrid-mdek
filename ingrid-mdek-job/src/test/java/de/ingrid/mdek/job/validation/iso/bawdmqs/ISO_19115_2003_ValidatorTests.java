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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static de.ingrid.mdek.job.validation.iso.bawdmqs.ValidatorTestsTemplateHelper.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Vikram Notay
 */
public class ISO_19115_2003_ValidatorTests {
    private static final String FILE_PATH = "src/test/resources/de/ingrid/mdek/job/validation/iso/bawdmqs/";

    private ISO_19115_2003_ConditionsValidator validator;

    @Before
    public void init() {
        validator = new ISO_19115_2003_ConditionsValidator();
    }

    @After
    public void logMessages() {
    }

    @Test
    public void testReferenceDocumentValidity() throws Exception {
        Document validDoc = defaultDocument();
        assertTrue(isValid(validDoc));
    }

    @Test
    public void testSchemaValidity() throws Exception {
        ISO_19115_2003_SchemaValidator schemaValidator = new ISO_19115_2003_SchemaValidator();
        Document validDoc = defaultDocument();
        Document invalidDoc = fetchXmlDocument("iso_test_sparse_invalid.xml");

        assertTrue(isValid(validDoc, schemaValidator));
        assertFalse(isValid(invalidDoc, schemaValidator));
    }

    @Test
    public void testMissingDataQualityForScopeEqualsDataset() throws IOException, ParserConfigurationException, SAXException {
        String element = String.format("<!-- scope code test -->\n"
                        + "\t\t\t<gmd:lineage>\n"
                        + "\t\t\t\t<gmd:LI_Lineage>\n"
                        + "\t\t\t\t\t<gmd:statement>\n"
                        + "\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t</gmd:statement>\n"
                        + "\t\t\t\t\t<gmd:source>\n"
                        + "\t\t\t\t\t\t<gmd:LI_Source>\n"
                        + "\t\t\t\t\t\t\t<gmd:description>\n"
                        + "\t\t\t\t\t\t\t\t<gco:CharacterString>Description</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t</gmd:description>\n"
                        + "\t\t\t\t\t\t</gmd:LI_Source>\n"
                        + "\t\t\t\t\t</gmd:source>\n"
                        + "\t\t\t\t</gmd:LI_Lineage>\n"
                        + "\t\t\t</gmd:lineage>",
                IDX_LINEAGE_STATEMENT);
        String template = fetchTemplateString();
        template = template.replace(element, "");
        Document doc = documentFromTemplate(template);
        assertFalse(isValid(doc));
    }

    @Test
    public void testHierarchyDatasetHasGeographicElement() throws IOException, ParserConfigurationException, SAXException {
        String template = fetchTemplateString();
        template = template.replaceFirst("(?s)<gmd:extent>.*?</gmd:extent>", "");

        Document document = documentFromTemplate(template);
        assertFalse(isValid(document));
    }

    @Test
    public void testDataQualityWithScopeDatasetOrSeriesHasStatement() throws IOException, ParserConfigurationException, SAXException {
        String element = String.format("<gmd:statement>\n"
                        + "\t\t\t\t\t\t<gco:CharacterString>{%d}</gco:CharacterString>\n"
                        + "\t\t\t\t\t</gmd:statement>\n",
                IDX_LINEAGE_STATEMENT);
        String template = fetchTemplateString();
        template = template.replace(element, "");
        Document doc = documentFromTemplate(template);
        assertFalse(isValid(doc));
    }

    private Document fetchXmlDocument(String fileName) throws SAXException, IOException, ParserConfigurationException {
        String path = FILE_PATH + fileName;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(path));
    }

    @Test
    public void testLineageChildren() throws ParserConfigurationException, SAXException, IOException {
        String element = "<gmd:source>\n"
                        + "\t\t\t\t\t\t<gmd:LI_Source>\n"
                        + "\t\t\t\t\t\t\t<gmd:description>\n"
                        + "\t\t\t\t\t\t\t\t<gco:CharacterString>Lineage Test</gco:CharacterString>\n"
                        + "\t\t\t\t\t\t\t</gmd:description>\n"
                        + "\t\t\t\t\t\t</gmd:LI_Source>\n"
                        + "\t\t\t\t\t</gmd:source>";
        String template = fetchTemplateString();
        template = template.replace(element, "");
        Document doc = documentFromTemplate(template);
        assertFalse(isValid(doc));
    }

    private boolean isValid(Document document) {
        return isValid(document, validator);
    }

    private boolean isValid(Document document, AbstractIsoValidator validator) {
        return validator.validate(document)
                .stream()
                .noneMatch(e -> e.getLevel() == ValidationReportItem.ReportLevel.FAIL);
    }
}
