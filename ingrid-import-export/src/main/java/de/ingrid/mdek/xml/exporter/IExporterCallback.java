package de.ingrid.mdek.xml.exporter;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;


/**
 * Interface for callbacks used by exporter.
 */
public interface IExporterCallback {

	/**
	 * Get total number of objects to export. Pass data passed to exporter.
	 * @param objUuids object uuids to export
	 * @param includeSubnodes also export subnodes
	 * @param userUuid calling user
	 * @return total number to export
	 */
	int getTotalNumObjectsToExport(List<String> objUuids,
			boolean includeSubnodes,
			String userUuid);

	/**
	 * Get PUBLISHED object details in doc.
	 * @param objUuid uuid of object
	 * @param userUuid calling user
	 * @return doc encapsulating object data
	 */
	IngridDocument getObjectDetails(String objUuid, String userUuid);

	/**
	 * Get uuids of PUBLISHED sub objects (only next level)
	 * @param parentUuid uuid of parent
	 * @param userUuid calling user
	 * @return list of uuids or empty list
	 */
	List<String> getSubObjects(String parentUuid, String userUuid);

	/**
	 * Get total number of addresses to export. Pass data passed to exporter.
	 * @param addrUuids address uuids to export
	 * @param includeSubnodes also export subnodes
	 * @param userUuid calling user
	 * @return total number to export
	 */
	int getTotalNumAddressesToExport(List<String> addrUuids,
			boolean includeSubnodes,
			String userUuid);

	/**
	 * Get PUBLISHED address details in doc.
	 * @param addrUuid uuid of address
	 * @param userUuid calling user
	 * @return doc encapsulating address data
	 */
	IngridDocument getAddressDetails(String addrUuid, String userUuid);

	/**
	 * Get uuids of PUBLISHED sub addresses (only next level)
	 * @param parentUuid uuid of parent
	 * @param userUuid calling user
	 * @return list of uuids or empty list
	 */
	List<String> getSubAddresses(String parentUuid, String userUuid);

	/** 
	 * Get doc representations of DEFINITIONS of additional fields of given ids.
	 * @param fieldIds ids of additional field definitions
	 * @return field definitions encapsulated in doc (access with 
	 * 		MdekKeys.SYS_ADDITIONAL_FIELD_KEY_PREFIX + fieldId)
	 */
	IngridDocument getSysAdditionalFields(Long[] fieldIds);

	/**
	 * Update basic information of export process.
	 * @param whichType address or object info ?
	 * @param numExported num exported entities so far
	 * @param totalNum total number to export
	 * @param userUuid calling user
	 */
	void writeExportInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid);

	/**
	 * Add a message to message information of export process.
	 * @param newMessage message to add
	 * @param userUuid calling user
	 */
	void writeExportInfoMessage(String newMessage, String userUuid);
}
