package de.ingrid.mdek;

import de.ingrid.mdek.IMdekCallerCommon.Quantity;
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

	/**
	 * Fetch all sub addresses of address with given uuid
	 * @param uuid address uuid
	 * @return response containing result: map containing representations of sub addresses
	 */
	IngridDocument fetchSubAddresses(String uuid,
			String userId);

	/**
	 * Fetch single address with given uuid.
	 * @param uuid address uuid
	 * @param howMuch how much data to fetch from address
	 * @return response containing result: map representation of address containing requested data
	 */
	IngridDocument fetchAddress(String uuid, Quantity howMuch,
			String userId);

}
