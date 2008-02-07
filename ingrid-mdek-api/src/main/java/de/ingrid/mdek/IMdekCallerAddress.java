package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 */
public interface IMdekCallerAddress {

	/**
	 * Fetch all top addresses.
	 * @return response containing result: map containing representations of all root addresses
	 */
	IngridDocument fetchTopAddresses(String userId);
}
