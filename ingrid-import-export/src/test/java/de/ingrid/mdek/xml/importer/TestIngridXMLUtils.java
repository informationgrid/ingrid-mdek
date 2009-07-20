package de.ingrid.mdek.xml.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import de.ingrid.mdek.xml.util.IngridXMLUtils;

public class TestIngridXMLUtils {

	private final static String FILE_NAME = "src/test/resources/test.xml"; 

	@Test
	public void testGetVersionReturnsValueForValidDoc() throws XMLStreamException, IOException {
		Reader reader = new FileReader(FILE_NAME);
		String version = IngridXMLUtils.getVersion(reader);
		reader.close();
		assertNotNull(version);
	}

	@Test
	public void testGetVersionReturnsCorrectValue() throws XMLStreamException, IOException {
		Reader reader = new StringReader("<igc xmlns='http://www.portalu.de/igc-import' exchange-format='1.0'>bla</igc>");
		String version = IngridXMLUtils.getVersion(reader);
		assertEquals("1.0", version);
	}

	@Test
	public void testGetVersionReturnsEmptyAttributeIfNotFound() throws XMLStreamException, IOException {
		Reader reader = new StringReader("<igc xmlns='http://www.portalu.de/igc-import'>bla</igc>");
		String version = IngridXMLUtils.getVersion(reader);
		assertNull(version);
	}

	@Test(expected=XMLStreamException.class)
	public void testGetVersionThrowsXMLStreamException() throws XMLStreamException, IOException {
		Reader reader = new StringReader("<invalid xml>... >");
		String version = IngridXMLUtils.getVersion(reader);
		reader.close();
	}
}
