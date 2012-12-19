package de.ingrid.mdek.caller;

import java.util.List;

import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning CATALOG data.
 */
public interface IMdekCallerCatalog extends IMdekCaller {

	/**
	 * Returns a map containing the entries of lists with given ids.
	 * Pass null as languageShortcut if catalog language should be used.
	 * Pass null as list ids if you just want to query ALL existing list ids !
	 * @param plugId which mdek server (iplug)
	 * @param listIds which lists. Pass NULL if querying all list ids.
	 * @param languageShortcut which language, e.g. "de", "en", pass null if catalog language !
	 * @param userId calling user
	 * @return response containing result: map with list entries per list
	 * 		or map with all list ids
	 */
	IngridDocument getSysLists(String plugId, Integer[] listIds, String languageShortcut,
			String userId);

	/** Store syslist THE OLD WAY (e.g. from catalog codelist admin page).
	 * NOTICE: all arrays describing entries have to be of same length.
	 * @param plugId which mdek server (iplug)
	 * @param listId id of the list
	 * @param maintainable is this list maintainable (true) or not (false=ISO Codelist)
	 * @param defaultEntryIndex which entry is the default (starting at 0), pass NULL if no default
	 * @param entryIds ids of entries, pass entry id NULL in array if new entry
	 * @param entryNames_de german names of entries, pass name NULL in array, if no name 
	 * @param entryNames_en english names of entries, pass name NULL in array, if no name 
	 * @param userId calling user
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument storeSysList(String plugId,
			int listId, boolean maintainable, Integer defaultEntryIndex,
			Integer[] entryIds, String[] entryNames_de, String[] entryNames_en, 
			String[] data, String userId);

	/**
	 * Store all syslists synchronized with the codelist repository. This function normally
	 * is called by a repeating job, which gets all connected iPlugs and its meta data
	 * administrators for authentication.
	 * @param plugId which mdek server (iplug)
	 * @param syslistDoc contains the syslists to update
	 * @param userId calling user
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument storeSysLists(String plugId, List<IngridDocument> syslistDoc, Long timestamp, String userId);
	
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
	 * @param includeWorkingCopies false=only published versions are exported ! If no published version
	 * 		object is skipped !<br>
	 * 		true=published and working versions (if present) of an object are exported !
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			boolean includeWorkingCopies,
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
	 * @param includeWorkingCopies false=only published versions are exported ! If no published version
	 * 		object is skipped !<br>
	 * 		true=published and working versions (if present) of an object are exported !
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			AddressArea addressArea,
			boolean includeWorkingCopies,
			String userId);

	/**
	 * Export all objects marked with the given criterion.
	 * @param plugId which mdek server (iplug)
	 * @param exportCriterion "tagged value". objects marked with this string are exported.
	 * @param includeWorkingCopies false=only published versions are exported ! If no published version
	 * 		object is skipped !<br>
	 * 		true=published and working versions (if present) of an object are exported !
	 * @param userId calling user
	 * @return response containing result: map containing xml data and export info
	 */
	IngridDocument exportObjects(String plugId, String exportCriterion, 
			boolean includeWorkingCopies,
			String userId);

	/** Returns information about the current/last export executed by the given user.
	 * @param plugId which mdek server (iplug)
	 * @param includeExportData true=export result data is also returned<br>
	 * 		false=no export data !
	 * @param userId calling user
	 * @return response containing result: map containing export information
	 */
	IngridDocument getExportInfo(String plugId, boolean includeExportData, String userId);

	/**
	 * Import the given MULTIPLE NUMBER OF FILES (import/export format) and update existing 
	 * or create new entities. FURTHER ADD PROTOCOL OF MAPPING to backend job info !
	 * @param plugId which mdek server (iplug)
	 * @param importData entities to import in import/export format IN MULTIPLE FILES !
	 * @param targetObjectUuid object node where new objects are created underneath
	 * 		("object import node").
	 * @param targetAddressUuid address node (institution !) where new addresses are 
	 * 		created underneath  ("address import node").<br>
	 * @param publishImmediately publish imported data immediately<br>
	 * 		NOTICE: if data is missing, entities are stored in working version ! 
	 * @param doSeparateImport separate all imported entities underneath the "import nodes".
	 * 		Further behavior dependent from copyNodeIfPresent !
	 * @param copyNodeIfPresent when doing separate import, should the node be copied, if
	 * 		UUID already exists ?<br>
	 * 		true = a new uuid is created, so it becomes a NEW entity.<br>
	 * 		false = exception is thrown containing UUIDs already present
	 * @param frontendMappingProtocol the protocol from frontend (e.g. mapping messages) to add to backend job info. Pass
	 * 		null or empty string if no Protocol.
	 * @param userId calling user
	 * @return response containing result: map containing import information
	 * @throws MdekException MdekErrorType.IMPORT_OBJECTS_ALREADY_EXIST: objects to import
	 * 		already do exist and copy is NOT allowed, MdekError contains objects info 
	 */
	IngridDocument importEntities(String plugId, List<byte[]> importData,
			String targetObjectUuid, String targetAddressUuid,
			boolean publishImmediately,
			boolean doSeparateImport,
			boolean copyNodeIfPresent,
			String frontendMappingProtocol,
			String userId);

	/** Returns DEFAULT information about the current/last job of given type executed by the given user.
	 * NOTICE: if special info should be delivered call separate method (e.g. getExportInfo for fetching
	 * export file).
	 * @param plugId which mdek server (iplug)
	 * @param jobType job type to get info about
	 * @param userId calling user
	 * @return response containing result: map containing job information
	 */
	IngridDocument getJobInfo(String plugId, JobType jobType, String userId);

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

	/** Replace an address with another address. Not possible if address to replace is user address.
	 * All addresses are replaced ! All responsible addresses are set to admin address.
	 * Modification addresses (mod user) are NOT replaced !
	 * @param oldUuid address to replace
	 * @param newUuid with this address
	 * @return response containing result: empty IngridDocument on success
	 */
	IngridDocument replaceAddress(String plugId, String oldUuid, String newUuid, String userId);

	/** Return all objects where given address uuid is referenced from with given type relation.
	 * If the referenceTypeId is null, all objects that reference the address with any type will 
	 * be returned.
	 * @param addressUuid uuid of address object is related to  
   * @param referenceTypeId type of the relation from syslist
	 * @param maxNum maximum number to fetch, pass null to fetch ALL objects
	 * @return response containing result: list of objects where given uuid is referenced address
	 */
	IngridDocument getObjectsOfAddressByType(String plugId,
			String addressUuid, Integer referenceTypeId, Integer maxNum, String userId);

	/** Return all objects where given address uuid is responsible user.
	 * @param responsibleUserUuid address uuid of responsible user
	 * @param maxNum maximum number to fetch, pass null to fetch ALL objects
	 * @return response containing result: list of objects where given uuid is responsible user
	 */
	IngridDocument getObjectsOfResponsibleUser(String plugId,
			String responsibleUserUuid, Integer maxNum, String userId);

	/** Return all addresses where given address uuid is responsible user.
	 * @param responsibleUserUuid address uuid of responsible user 
	 * @param maxNum maximum number to fetch, pass null to fetch ALL addresses
	 * @return response containing result: list of addresses where given uuid is responsible user
	 */
	IngridDocument getAddressesOfResponsibleUser(String plugId,
			String responsibleUserUuid, Integer maxNum, String userId);

	/**
	 * Request csv data.
	 * @param csvType specifies what kind of csv data to fetch
	 * @param uuid additional data specifying entity (if needed, else pass null)
	 * @return response containing result: csv data as zipped string
	 */
	IngridDocument getCsvData(String plugId, MdekUtils.CsvRequestType csvType,
			String uuid, String userId);

	/**
	 * Get all free entries of entities where given syslist is used, e.g. if passed
	 * syslist is MdekSysList.LEGIST then all free entries in entity T015Legist
	 * are returned (distinct). NOTICE: only works for subset of syslists (not all implemented) !!!
	 * @param sysLst specifies syslist and according entity(ies) where syslist is used ! 
	 * @return response containing result: distinct free entries (strings) or empty list OR NULL !
	 * 		NULL means backend functionality for passed list NOT IMPLEMENTED yet !
	 */
	IngridDocument getFreeListEntries(String plugId, MdekSysList sysLst, String userId);

	/** Replace the given free entry with the given syslist entry.
	 * @param freeEntry entry name of free entry
	 * @param sysLst specifies syslist and according entities
	 * @param sysLstEntryId syslist entry id
	 * @param sysLstEntryName entry name of syslist entry
	 * @return response containing result: number of replaced free entries
	 */
	IngridDocument replaceFreeEntryWithSyslistEntry(String plugId, String freeEntry,
		MdekSysList sysLst, int sysLstEntryId, String sysLstEntryName,
		String userId);

	/** Update all syslist data according to current syslists (values in entities, index ...).
	 * Should be called after syslists were changed !
	 * @return response containing result: map containing rebuild information
	 */
	IngridDocument rebuildSyslistData(String plugId, String userId);

	/** Get REFERENCED search terms (distinct!) of requested type !
	 * NOTICE: only returns terms REFERENCED by Objects/Addresses (e.g. NOT unused thesaurus terms) !
	 * @param termTypes pass the types of terms to fetch. Pass null or empty array if all types !
	 * @return response containing result: distinct searchterms accessable via MdekKeys.SUBJECT_TERMS
	 */
	IngridDocument getSearchTerms(String plugId, SearchtermType[] termTypes, String userId);

	/** Update searchterms (SNS Update). Passed lists have to have same size
	 * determining "mapping" of old to new (updated) term.
	 * @param oldTerms former searchterms before update
	 * @param newTerms searchterm after update
	 * @return response containing result: map containing job info
	 */
	IngridDocument updateSearchTerms(String plugId,
			List<IngridDocument> oldTerms, List<IngridDocument> newTerms,
			String userId);

	/** Get REFERENCED NON EXPIRED spatial references (distinct!) of requested type.
	 * NOTICE: only returns spatial references REFERENCED by Objects/Addresses (NOT unused ones) !
	 * @param spatialRefTypes types of spatial references to fetch. Pass null or empty array if all types !
	 * @return response containing result: distinct spatial references accessable via MdekKeys.LOCATIONS
	 */
	IngridDocument getSpatialReferences(String plugId, SpatialReferenceType[] spatialRefTypes, String userId);

	/** Update SNS spatial references (SNS Update). Passed lists have to have same size
	 * determining "mapping" of old to new (updated) spatial reference.
	 * @param oldSpatialRefs former spatial references before update
	 * @param newSpatialRefs spatial references  after update
	 * @return response containing result: map containing job info
	 */
	IngridDocument updateSpatialReferences(String plugId,
			List<IngridDocument> oldSpatialRefs, List<IngridDocument> newSpatialRefs,
			String userId);

	/**
	 * Get the timestamp of the most current syslist that has been changed (in the repository).
	 * This timestamp is used for requesting newer syslists from the repository and to receive
	 * only those syslists that have been changed since the last update.
	 * @param plugId is the id of the iplug to request the timestamp from
	 * @param userId is the user for authorization
	 * @return response with the timestamp
	 */
	IngridDocument getLastModifiedTimestampOfSyslists(String plugId, String userId);
	
}
