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
	 * Returns a map containing values of given generic keys.
	 * Pass null if all generic keys are requested.
	 * @param plugId which mdek server (iplug)
	 * @param keyNames which generic keys, PASS NULL IF ALL KEYS REQUESTED
	 * @param userId calling user
	 * @return response containing result: map with requested generic keys (as keys into map !)
	 */
	IngridDocument getSysGenericKeys(String plugId, String[] keyNames, String userId);

	/**
	 * Create/update given generic keys with the given values. 
	 * @param plugId which mdek server (iplug)
	 * @param keyNames names of generic keys
	 * @param keyValues values of generic keys
	 * @return response containing result: map containing updated keys. NOTICE: result is null if problems occured
	 */
	IngridDocument storeSysGenericKeys(String plugId, String[] keyNames, String[] keyValues,
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
	 * Export given object branch to XML file. Also multiple "top branches" can be exported if virtual
	 * top node (no uuid) is selected. 
	 * @param plugId which mdek server (iplug)
	 * @param rootUuid object uuid of branch to export (root of branch), pass NULL if ALL objects !
	 * @param exportOnlyRoot export only the given node, NO sub nodes.
	 * 		if virtual top node (no uuid) is selected, this determines whether ONLY TOP NODES (true)
	 * 		or ALL NODES (false) are exported.
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId);

	/**
	 * Export given address branch to XML file. Also multiple "top branches" can be exported,
	 * dependent from selected virtual top node (ALL ADRESSES or ALL FREE ADDRESSES ...)
	 * @param plugId which mdek server (iplug)
	 * @param rootUuid address uuid of branch to export (root of branch),
	 * 		pass NULL if virtual top node (no uuid)
	 * @param exportOnlyRoot export only the given node, NO sub nodes.
	 * 		if virtual top node (no uuid) is selected, this determines whether ONLY TOP NODES (true)
	 * 		or ALL NODES (false) underneath selected virtual top node are exported.
	 * @param addressArea only relevant if virtual top node (no uuid): determines which address "area" to export.
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			AddressArea addressArea,
			String userId);

	/**
	 * Export all objects marked with the given criterion.
	 * @param plugId which mdek server (iplug)
	 * @param exportCriterion "tagged value". objects marked with this string are exported.
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjects(String plugId, String exportCriterion, String userId);

	/** Returns information about the current/last export executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param includeExportData true=export result data is also returned<br>
	 * 		false=no export data !
	 * @param userId calling user
	 * @return response containing result: map containing export information
	 */
	IngridDocument getExportInfo(String plugId, boolean includeExportData, String userId);

	/**
	 * Import the given data (import/export format) and update existing or create new entities.
	 * @param plugId which mdek server (iplug)
	 * @param importData entities to import in import/export format
	 * @param targetObjectUuid object node where new objects are created underneath
	 * 		("object import node").
	 * @param targetAddressUuid address node (institution !) where new addresses are 
	 * 		created underneath  ("address import node").<br>
	 * @param publishImmediately publish imported data immediately<br>
	 * 		NOTICE: if data is missing, entities are stored in working version ! 
	 * @param doSeparateImport separate all imported entities underneath the "import nodes".
	 * 		If an imported entity already exists in catalog, a new uuid is created, so it becomes
	 * 		a NEW entity.
	 * @param userId calling user
	 * @return response containing result: map containing import information
	 */
	IngridDocument importEntities(String plugId, byte[] importData,
			String targetObjectUuid, String targetAddressUuid,
			boolean publishImmediately,
			boolean doSeparateImport,
			String userId);

	/** Returns information about the current/last import executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing import information
	 */
	IngridDocument getImportInfo(String plugId, String userId);

	/** Returns information about the last url job executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing url information
	 */
	IngridDocument getURLInfo(String plugId, String userId);

	/** Sets the result for the last url job executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param urlInfo job info to be stored in the db
	 * @param userId calling user
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument setURLInfo(String plugId, IngridDocument urlInfo, String userId);

	/** Updates the URL job info in the db
	 * @param plugId which mdek server (iplug)
	 * @param urlList list containing urls which are being replaced and their corresponding object uuids
	 * @param targetUrl the url which should replace the sourceURLs
	 * @param userId calling user
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument updateURLInfo(String plugId, List<IngridDocument> urlList, String targetUrl, String userId);

	/** Replaces the given urls with the target url.
	 * @param plugId which mdek server (iplug)
	 * @param urlList list containing urls and their corresponding object uuids
	 * @param targetUrl the url which should replace the sourceURLs
	 * @param userId calling user
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument replaceURLs(String plugId, List<IngridDocument> urlList, String targetUrl, String userId);

	/** starts the db consistency checker job
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing consistency check result
	 */
	IngridDocument analyze(String plugId, String userId);
}
