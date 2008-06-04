package de.ingrid.mdek.caller;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning CATALOG data.
 */
public interface IMdekCallerCatalog {

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
}
