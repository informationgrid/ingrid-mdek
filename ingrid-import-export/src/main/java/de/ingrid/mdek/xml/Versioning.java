package de.ingrid.mdek.xml;

public class Versioning {
	/** Current Version of XML Exchange Format */
	public static final String CURRENT_VERSION = "1.0.8";

	/** Current Mapper for importing current format ! */
	public static final Class CURRENT_IMPORT_MAPPER_CLASS =
		de.ingrid.mdek.xml.importer.mapper.version108.IngridXMLMapperImpl.class;
}
