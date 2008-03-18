package de.ingrid.mdek;

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
	 * @param languageCode which language
	 * @param userId calling user
	 * @return response containing result: map with list entries
	 */
	IngridDocument getSysLists(String plugId, Integer[] listIds, Integer languageCode,
			String userId);

	/**
	 * Fetch The catalog object, represented by an CATALOG_MAP type in xsd.
	 * @param plugId which mdek server (iplug)
	 * @return response containing result: map representation of the catalog object
	 */
	IngridDocument fetchCatalog(String plugId, String userId);
}
