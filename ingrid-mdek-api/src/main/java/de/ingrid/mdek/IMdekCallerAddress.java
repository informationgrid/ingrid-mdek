package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 */
public interface IMdekCallerAddress {

	/**
	 * Fetch top addresses.
	 * @param userId
	 * @param onlyFreeAddresses true= only free top addresses, false=only NOT free top addresses
	 * @return response containing result: map containing representations of root addresses
	 */
	IngridDocument fetchTopAddresses(String userId, boolean onlyFreeAddresses);
}
