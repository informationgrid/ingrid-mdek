package de.ingrid.mdek.xml.importer.mapper.version105;

import org.w3c.dom.Document;

import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapper;
import de.ingrid.utils.IngridDocument;

public class IngridXMLMapperImpl implements IngridXMLMapper {

	@Override
	public IngridDocument mapAddress(Document document) {
		return XMLAddressToDocMapper.map(document);
	}

	@Override
	public IngridDocument mapDataSource(Document document) {
		return XMLDatasourceToDocMapper.map(document);
	}

	@Override
	public IngridDocument mapAdditionalFields(Document document) {
		return XMLAdditionalFieldsToDocMapper.map(document);
	}

	@Override
	public boolean canReadVersion(String version) {
		return "1.0.5".equals(version);
	}
}
