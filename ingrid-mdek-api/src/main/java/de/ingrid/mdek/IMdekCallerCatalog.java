package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning CATALOG data.
 */
public interface IMdekCallerCatalog {

	/** Returns a map containing the entries of lists with given ids.
	 * Pass null as languageCode if it doesn't matter. */
	IngridDocument getSysLists(Integer[] listIds, Integer languageCode,
			String userId);

	/**
	 * Fetch The catalog object, represented by an CATALOG_MAP type in xsd.
	 * @return response containing result: map representation of the catalog object
	 */
	IngridDocument fetchCatalog(String userId);
}
