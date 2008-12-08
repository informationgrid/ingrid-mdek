package de.ingrid.mdek.caller;

import java.util.List;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning CATALOG data.
 */
public interface IMdekCallerCatalog extends IMdekCaller {

	/**
	 * Returns a map containing the entries of lists with given ids.
	 * Pass null as languageCode if it doesn't matter.
	 * @param plugId which mdek server (iplug)
	 * @param listIds which lists
	 * @param language which language
	 * @param userId calling user
	 * @return response containing result: map with list entries
	 */
	IngridDocument getSysLists(String plugId, Integer[] listIds, String language,
			String userId);

	/**
	 * Returns a map containing details of the gui elements with the given ids.
	 * Pass null if all gui elements are requested.
	 * @param plugId which mdek server (iplug)
	 * @param guiIds which gui elements, PASS NULL IF ALL ELEMENTS REQUESTED
	 * @param userId calling user
	 * @return response containing result: map with requested gui elements.
	 * 		gui ids are keys into map extracting detailed map.
	 */
	IngridDocument getSysGuis(String plugId, String[] guiIds, String userId);

	/**
	 * Change details of gui elements. NOTICE: also stores non existent sysGuis ! 
	 * @param plugId which mdek server (iplug)
	 * @param sysGuis list of gui elements as maps.
	 * @param refetchAfterStore immediately refetch gui elements after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map with updated elements when refetching,
	 * 		otherwise just a map. NOTICE: result is null if problems occured
	 */
	IngridDocument storeSysGuis(String plugId, List<IngridDocument> sysGuis,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Returns a map containing definitions of additional fields.
	 * @param plugId which mdek server (iplug)
	 * @param fieldIds which fields, pass identifiers
	 * @param language which language, pass null if all languages (language of selection list of field if present)
	 * @param userId calling user
	 * @return response containing result: map with definitions of additional fields
	 */
	IngridDocument getSysAdditionalFields(String plugId, Long[] fieldIds, String language,
			String userId);

	/**
	 * Fetch The catalog object, represented by an CATALOG_MAP type in xsd.
	 * @param plugId which mdek server (iplug)
	 * @return response containing result: map representation of the catalog object
	 */
	IngridDocument fetchCatalog(String plugId, String userId);

	/**
	 * Change catalog data.
	 * @param plugId which mdek server (iplug)
	 * @param catalogDoc map representation of catalog.
	 * @param refetchAfterStore immediately refetch catalog after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: full map representation of updated catalog when refetching,
	 * 		otherwise map containing basic data of stored catalog  
	 */
	IngridDocument storeCatalog(String plugId, IngridDocument catalogDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Export given object branch to XML file.
	 * @param plugId which mdek server (iplug)
	 * @param rootUuid object uuid of branch to export (root of branch)
	 * @param exportOnlyRoot export only the given node, NO sub nodes
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId);

	/**
	 * Export all objects marked with the given criterion.
	 * @param plugId which mdek server (iplug)
	 * @param exportCriterion "tagged value". objects marked with this string are exported.
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjects(String plugId, String exportCriterion, String userId);

	/**
	 * Export given address branch to XML file.
	 * @param plugId which mdek server (iplug)
	 * @param rootUuid address uuid of branch to export (root of branch)
	 * @param exportOnlyRoot export only the given node, NO sub nodes
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId);

	/** Returns information about the current/last export executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing export information
	 */
	IngridDocument getExportInfo(String plugId, String userId);

	/**
	 * Import the given data (import/export format) and update existing or create new entities.
	 * @param plugId which mdek server (iplug)
	 * @param importData entities to import in import/export format
	 * @param targetObjectUuid object node where all new objects are created underneath
	 * @param targetAddressUuid address node (institution !) where all new addresses are 
	 * 		created underneath.<br>
	 * 		NOTICE: new free Addresses are ALWAYS created as new free TOP addresses
	 * @param publishImmediately publish imported data immediately<br>
	 * 		NOTICE: if data is missing, entities are stored in working version ! 
	 * @param userId calling user
	 * @return response containing result: map containing import information
	 */
	IngridDocument importEntities(String plugId, Byte[] importData,
			String targetObjectUuid, String targetAddressUuid,
			boolean publishImmediately,
			String userId);

	/** Returns information about the current/last import executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing import information
	 */
	IngridDocument getImportInfo(String plugId, String userId);
}
