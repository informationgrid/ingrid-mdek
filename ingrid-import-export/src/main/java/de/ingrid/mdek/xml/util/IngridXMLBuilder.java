package de.ingrid.mdek.xml.util;

import de.ingrid.mdek.xml.exporter.mapper.AddressDocToXMLMapper;
import de.ingrid.mdek.xml.exporter.mapper.DatasourceDocToXMLMapper;
import de.ingrid.utils.IngridDocument;

public class IngridXMLBuilder {

	private IngridXMLBuilder() {}

	public static XMLElement createXMLForObject(IngridDocument objectRoot) {
		DatasourceDocToXMLMapper mapper = new DatasourceDocToXMLMapper(objectRoot);
		return mapper.createDataSource();
	}
	public static XMLElement createXMLForAddress(IngridDocument addressRoot) {
		AddressDocToXMLMapper mapper = new AddressDocToXMLMapper(addressRoot);
		return mapper.createAddress();
	}
}
