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
	 * @return response containing result: map containing xml data
	 */
	IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId);

	/**
	 * Export all objects marked with the given criteria.
	 * @param plugId which mdek server (iplug)
	 * @param exportCriteria criteria "tagged value". objects marked with this string are exported.
	 * @param userId calling user
	 * @return response containing result: map containing xml data
	 */
	IngridDocument exportObjects(String plugId, String exportCriteria, String userId);

	/**
	 * Export given address branch to XML file.
	 * @param plugId which mdek server (iplug)
	 * @param rootUuid address uuid of branch to export (root of branch)
	 * @param exportOnlyRoot export only the given node, NO sub nodes
	 * @param userId calling user
	 * @return response containing result: map containing xml data
	 */
	IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId);
}
