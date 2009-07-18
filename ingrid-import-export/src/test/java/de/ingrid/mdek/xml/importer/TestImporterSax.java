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
