/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.xml;

/**
 * Helper class encapsulating versioning stuff of Export/Import Format.<br/>
 */
public class Versioning extends de.ingrid.mdek.Versioning {
	/** Current Version of XML Exchange Format.
	 *  Only change, if InGrid XML structure really changes, so current importer cannot read former version or former importer cannot read this version ! */
	public static final String CURRENT_IMPORT_EXPORT_VERSION = "5.6.0";

	/** Current Mapper for importing current format ! */
	public static final Class CURRENT_IMPORT_MAPPER_CLASS =
		de.ingrid.mdek.xml.importer.mapper.version560.IngridXMLMapperImpl.class;
}
