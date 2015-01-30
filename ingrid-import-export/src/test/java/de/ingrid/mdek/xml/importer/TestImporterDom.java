/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.importer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.ingrid.mdek.xml.importer.util.PrintToStreamErrorHandler;

public class TestImporterDom {

	private static final String IMPORT_XML_FILENAME = "src/test/resources/test.xml";
	private static final String IMPORT_XSD_FILENAME = "src/test/resources/test.xsd";

	private XPath xpath; 
	private Schema schema;
	private InputSource xsdSource;
	private InputSource xmlSource;

	@Before
	public void setup() throws SAXException, FileNotFoundException {
		setupSources();
		setupSchema();
		setupXpath();
	}

	private void setupSources() throws FileNotFoundException {
		xsdSource = new InputSource(new FileReader(IMPORT_XSD_FILENAME));
		xmlSource = new InputSource(new FileReader(IMPORT_XML_FILENAME));
	}

	private void setupSchema() throws SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schema = schemaFactory.newSchema(new SAXSource(xsdSource));
	}

	private void setupXpath() {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
	}


	@Test
	public void testDocumentCanBeValidated() throws IOException, SAXException {
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new PrintToStreamErrorHandler(System.err));

		validator.validate(new SAXSource(xmlSource));
	}

	@Ignore("needs lots of heap space (~10x file size)")
	@Test
	public void testDocumentCanBeCreated() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		long startTime = System.currentTimeMillis();
		Document document = documentBuilder.parse(xmlSource);
		long endTime = System.currentTimeMillis();
		System.out.println("Parsing took "+(endTime - startTime)+" milliseconds.");

		startTime = System.currentTimeMillis();
		NodeList nodeList = (NodeList) xpath.evaluate("//igc/data-sources/data-source/general/object-identifier/text()", document, XPathConstants.NODESET);
		endTime = System.currentTimeMillis();
		System.out.println("Xpath Eval took "+(endTime - startTime)+" milliseconds.");

		sleep(3000);
	}

	private void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
