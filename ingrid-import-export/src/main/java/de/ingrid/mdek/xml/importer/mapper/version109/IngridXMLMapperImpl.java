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
package de.ingrid.mdek.xml.importer.mapper.version109;

import org.w3c.dom.Document;

import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapper;
import de.ingrid.mdek.xml.importer.mapper.version105.XMLAddressToDocMapper;
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
	public boolean canReadVersion(String version) {
		return "1.0.9".equals(version);
	}
}
