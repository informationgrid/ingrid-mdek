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

	/**
	 * Create or store address INTO WORKING COPY !
	 * @param adr map representation of address.
	 * 		If no id/uuid is set address will be created else updated.
	 * @param refetchAfterStore immediately refetch address after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of created/updated address when refetching,
	 * 		otherwise map containing uuid of stored address (was generated when new address)  
	 */
	IngridDocument storeAddress(IngridDocument adr,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Get initial data for a new address. Pass data needed to determine initial data (e.g. uuid of parent).
	 * @param newBasicAddress basic new address with data needed to determine initial data, e.g. parent uuid ...
	 * @return extended newAddress, e.g. containing terms of parent etc.
	 */
	IngridDocument getInitialAddress(IngridDocument newBasicAddress,
			String userId);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the address is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 * @param uuid object uuid
	 * @return response containing result: map containing info whether address was fully deleted
	 */
	IngridDocument deleteAddressWorkingCopy(String uuid,
			String userId);
}
