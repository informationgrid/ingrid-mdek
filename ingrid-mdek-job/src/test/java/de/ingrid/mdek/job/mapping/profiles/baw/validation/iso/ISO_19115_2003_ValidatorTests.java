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
package de.ingrid.mdek.job.mapping.profiles.baw.validation.iso;

import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.validation.iso.ISO_19115_2003_ConditionsValidator;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.mapping.validation.iso.ISO_19115_2003_SchemaValidator;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static de.ingrid.mdek.job.mapping.profiles.baw.validation.iso.ValidatorTestsTemplateHelper.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Vikram Notay
 */
public class ISO_19115_2003_ValidatorTests {
    private static final Logger LOG = Logger.getLogger(ISO_19115_2003_ValidatorTests.class);

    private static final String FILE_PATH = "src/test/resources/de/ingrid/mdek/job/validation/iso/bawdmqs/";

    private ISO_19115_2003_ConditionsValidator validator;
    private Document defaultDocument;

    public ISO_19115_2003_ValidatorTests() {
        validator = new ISO_19115_2003_ConditionsValidator();
    }

    @BeforeEach
    public void init() {
        try {
            defaultDocument = ValidatorTestsTemplateHelper.defaultDocument();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.error("Error while reading and creating default template document for validation.", e);
        }
    }

    @AfterEach
    public void logMessages() {
    }

    @Test
    public void testReferenceDocumentValidity() throws Exception {
        assertIsValid(defaultDocument);
    }

    @Test
    public void testSchemaValidity() throws Exception {
        ISO_19115_2003_SchemaValidator schemaValidator = new ISO_19115_2003_SchemaValidator();
        Document validDoc = defaultDocument();
        Document invalidDoc = fetchXmlDocument("iso_test_sparse_invalid.xml");

        assertIsValid(validDoc, schemaValidator);
        assertIsInvalid(invalidDoc, schemaValidator);
    }

    @Test
    public void testMissingDataQualityForScopeEqualsDataset() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/*/gmd:DQ_DataQuality[.//gmd:MD_ScopeCode/@codeListValue='dataset']/gmd:lineage";
        removeElementAtXpath(doc, xpath);
        assertIsInvalid(doc);
    }

    @Test
    public void testHierarchyDatasetHasGeographicElement() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/*/gmd:MD_DataIdentification/gmd:extent";
        removeElementsAtXpath(doc, xpath);
        assertIsInvalid(doc);
    }

    @Test
    public void testDataQualityWithScopeDatasetOrSeriesHasStatement() {
        Document doc = defaultDocument;
        String statementXpath = "/gmd:MD_Metadata/*/gmd:DQ_DataQuality[.//gmd:MD_ScopeCode/@codeListValue='dataset']/gmd:lineage/*/gmd:statement";
        String sourceXpath = "/gmd:MD_Metadata/*/gmd:DQ_DataQuality[.//gmd:MD_ScopeCode/@codeListValue='dataset']/gmd:lineage/*/gmd:source";
        removeElementAtXpath(doc, statementXpath);
        removeElementAtXpath(doc, sourceXpath);
        assertIsInvalid(doc);
    }

    private Document fetchXmlDocument(String fileName) throws SAXException, IOException, ParserConfigurationException {
        String path = FILE_PATH + fileName;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(path));
    }

    @Test
    public void testLineageChildren() {
        Document doc = defaultDocument;
        String xpath = "/gmd:MD_Metadata/gmd:dataQualityInfo[last()]/*/gmd:lineage/*/gmd:source";
        removeElementAtXpath(doc, xpath);
        assertIsInvalid(doc);
    }

    private void assertIsValid(Document doc) {
        assertIsValid(doc, validator);
    }

    private void assertIsValid(Document document, ImportDataMapper<Document, Document> validator) {
        ProtocolHandler mockPh = Mockito.mock(ProtocolHandler.class);
        Document mockDoc = Mockito.mock(Document.class);
        validator.convert(document, mockDoc, mockPh);
        verify(mockPh, never())
                .addMessage(eq(ProtocolHandler.Type.ERROR), anyString());
    }

    private void assertIsInvalid(Document doc) {
        assertIsInvalid(doc, validator);
    }

    private void assertIsInvalid(Document doc, ImportDataMapper<Document, Document> validator) {
        ProtocolHandler mockPh = Mockito.mock(ProtocolHandler.class);
        Document mockDoc = Mockito.mock(Document.class);
        validator.convert(doc, mockDoc, mockPh);
        verify(mockPh, atLeastOnce())
                .addMessage(eq(ProtocolHandler.Type.ERROR), anyString());
    }
}
