package de.ingrid.mdek.xml.exporter;

import java.util.List;


/**
 * Interface for calling exporter (e.g. from Job).
 */
public interface IExporter {

	/**
	 * Export given object(s) to XML file.
	 * @param objUuids uuids of objects to export
	 * @param includeSubnodes also export subnodes ? true=yes
	 * @param userUuid calling user, needed for callbacks
	 * @return objects in export format
	 */
	byte[] exportObjects(List<String> objUuids, boolean includeSubnodes,
			String userUuid);

	/**
	 * Export given address(es) to XML file.
	 * @param addrUuids uuids of addresses to export
	 * @param includeSubnodes also export subnodes ? true=yes
	 * @param userUuid calling user, needed for callbacks
	 * @return addresses in export format
	 */
	byte[] exportAddresses(List<String> addrUuids, boolean includeSubnodes,
			String userUuid);
}
