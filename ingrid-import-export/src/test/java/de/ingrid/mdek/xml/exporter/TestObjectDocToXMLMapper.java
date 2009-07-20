package de.ingrid.mdek.xml.exporter;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.mdek.xml.exporter.mapper.DatasourceDocToXMLMapper;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;

public class TestObjectDocToXMLMapper {

	private DatasourceDocToXMLMapper emptyDocMapper;

	@Before
	public void setupIngridDocToXMLMapper() {
		createEmptyDocumentMapper();
	}

	private void createEmptyDocumentMapper() {
		emptyDocMapper = new DatasourceDocToXMLMapper(new IngridDocument());
	}

	@Test
	public void testCreateDataSourceRunsOnEmptyDoc() {
		emptyDocMapper.createDataSource();
	}

	@Test
	public void testEmptyDocMapsToEmptyRoot() {
		XMLElement dataSourceRoot = emptyDocMapper.createDataSource();
		assertTrue("Mapping an empty doc must result in an empty 'data-source' XMLElement.",
				isEmptyXMLElement(dataSourceRoot) && XMLKeys.DATA_SOURCE.equals(dataSourceRoot.getName()));
	}

	private boolean isEmptyXMLElement(XMLElement element) {
		return !element.hasChildren() && !element.hasAttributes();
	}
}
