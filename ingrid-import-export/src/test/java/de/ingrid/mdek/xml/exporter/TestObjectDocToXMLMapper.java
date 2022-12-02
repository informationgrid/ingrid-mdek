/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.xml.exporter;

import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.mdek.xml.exporter.mapper.DatasourceDocToXMLMapper;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

public class TestObjectDocToXMLMapper {

	private DatasourceDocToXMLMapper emptyDocMapper;

	@BeforeEach
	public void setupIngridDocToXMLMapper() {
		createEmptyDocumentMapper();
	}

	private void createEmptyDocumentMapper() {
		emptyDocMapper = new DatasourceDocToXMLMapper(new IngridDocument());
	}

	@Test
	public void testCreateDataSourceRunsOnEmptyDoc() {
		emptyDocMapper.createDataSourceInstance();
	}

	@Test
	public void testEmptyDocMapsToEmptyRoot() {
		XMLElement dataSourceRoot = emptyDocMapper.createDataSourceInstance();
		assertThat("Mapping an empty doc must result in an empty 'data-source' XMLElement.",
				isEmptyXMLElement(dataSourceRoot) && XMLKeys.DATA_SOURCE_INSTANCE.equals(dataSourceRoot.getName()), is(true));
	}

	private boolean isEmptyXMLElement(XMLElement element) {
		return !element.hasChildren() && !element.hasAttributes();
	}
}
