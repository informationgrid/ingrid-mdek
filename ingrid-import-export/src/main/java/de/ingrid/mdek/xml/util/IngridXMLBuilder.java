/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.util;

import java.util.List;

import de.ingrid.mdek.xml.exporter.mapper.AddressDocToXMLMapper;
import de.ingrid.mdek.xml.exporter.mapper.DatasourceDocToXMLMapper;
import de.ingrid.utils.IngridDocument;

public class IngridXMLBuilder {

	private IngridXMLBuilder() {}

	/** Create XML for different instances of object.
	 * @param objInstances Pass instances, may also contain only one instance !
	 * @return
	 */
	public static XMLElement createXMLForObject(List<IngridDocument> objInstances) {
		XMLElement retElement = DatasourceDocToXMLMapper.createDataSource();
		for (IngridDocument objInstance : objInstances) {
			DatasourceDocToXMLMapper mapper = new DatasourceDocToXMLMapper(objInstance);
			retElement.addChild(mapper.createDataSourceInstance());
		}

		return retElement;
	}
	/** Create XML for different instances of address.
	 * @param objInstances Pass instances, may also contain only one instance !
	 * @return
	 */
	public static XMLElement createXMLForAddress(List<IngridDocument> addrInstances) {
		XMLElement retElement = AddressDocToXMLMapper.createAddress();
		for (IngridDocument addrInstance : addrInstances) {
			AddressDocToXMLMapper mapper = new AddressDocToXMLMapper(addrInstance);
			retElement.addChild(mapper.createAddressInstance());
		}

		return retElement;
	}
}
