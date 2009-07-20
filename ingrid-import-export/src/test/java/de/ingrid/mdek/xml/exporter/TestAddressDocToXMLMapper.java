package de.ingrid.mdek.xml.exporter;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.mdek.xml.exporter.mapper.AddressDocToXMLMapper;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;

public class TestAddressDocToXMLMapper {

	private AddressDocToXMLMapper emptyDocMapper;
	private AddressDocToXMLMapper fullDocMapper;
	
	@Before
	public void setupIngridDocToXMLMapper() {
		createEmptyDocumentMapper();
	}

	private void createEmptyDocumentMapper() {
		emptyDocMapper = new AddressDocToXMLMapper(new IngridDocument());
	}

	@Test
	public void testCreateAddressRunsOnEmptyDoc() {
		emptyDocMapper.createAddress();
	}

	@Test
	public void testEmptyDocMapsToEmptyRoot() {
		XMLElement addressRoot = emptyDocMapper.createAddress();
		assertTrue("Mapping an empty doc must result in an empty 'address' XMLElement.",
				isEmptyXMLElement(addressRoot) && XMLKeys.ADDRESS.equals(addressRoot.getName()));
	}

	private boolean isEmptyXMLElement(XMLElement element) {
		return !element.hasChildren() && !element.hasAttributes();
	}
}
