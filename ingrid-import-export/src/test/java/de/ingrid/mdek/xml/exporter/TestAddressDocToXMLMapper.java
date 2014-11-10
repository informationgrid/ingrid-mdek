/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.mdek.xml.exporter.mapper.AddressDocToXMLMapper;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;

public class TestAddressDocToXMLMapper {

	private AddressDocToXMLMapper emptyDocMapper;
	
	@Before
	public void setupIngridDocToXMLMapper() {
		createEmptyDocumentMapper();
	}

	private void createEmptyDocumentMapper() {
		emptyDocMapper = new AddressDocToXMLMapper(new IngridDocument());
	}

	@Test
	public void testCreateAddressRunsOnEmptyDoc() {
		emptyDocMapper.createAddressInstance();
	}

	@Test
	public void testEmptyDocMapsToEmptyRoot() {
		XMLElement addressRoot = emptyDocMapper.createAddressInstance();
		assertTrue("Mapping an empty doc must result in an empty 'address' XMLElement.",
				isEmptyXMLElement(addressRoot) && XMLKeys.ADDRESS_INSTANCE.equals(addressRoot.getName()));
	}

	private boolean isEmptyXMLElement(XMLElement element) {
		return !element.hasChildren() && !element.hasAttributes();
	}
}
