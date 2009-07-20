package de.ingrid.mdek.xml.importer.mapper;

import org.w3c.dom.Document;

import de.ingrid.utils.IngridDocument;

public interface IngridXMLMapper {
	public IngridDocument mapDataSource(Document document);
	public IngridDocument mapAddress(Document document);
	public IngridDocument mapAdditionalFields(Document document);
	public boolean canReadVersion(String version);
}
