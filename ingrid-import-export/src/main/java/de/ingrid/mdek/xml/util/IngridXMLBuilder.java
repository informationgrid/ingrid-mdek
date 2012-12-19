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
