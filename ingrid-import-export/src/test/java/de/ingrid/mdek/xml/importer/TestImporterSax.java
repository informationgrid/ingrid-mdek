/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.ingrid.mdek.xml.importer.util.PrintToStreamErrorHandler;

public class TestImporterSax {

	private static final String IMPORT_XML_FILENAME = "src/test/resources/test.xml";

	private XMLReader xmlReader;
	private InputSource in;

	@Before
	public void setup() throws SAXException, FileNotFoundException {
		setupXMLReader();
		setupInputSource();
	}

	private void setupXMLReader() throws SAXException {
		xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setErrorHandler(new PrintToStreamErrorHandler(System.err));

		// Activate xml and xsd validation
		xmlReader.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
		xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
		xmlReader.setFeature("http://xml.org/sax/features/validation", true);
		xmlReader.setFeature("http://apache.org/xml/features/validation/schema", true);
	}

	private void setupInputSource() throws FileNotFoundException {
		in = new InputSource(new FileInputStream(IMPORT_XML_FILENAME));
	}

	@Test
	public void testDocumentCanBeParsed() throws IOException, SAXException {
		xmlReader.parse(in);
	}
}
