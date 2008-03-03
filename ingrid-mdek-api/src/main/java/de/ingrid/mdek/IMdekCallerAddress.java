package de.ingrid.mdek;

import de.ingrid.mdek.IMdekCallerAbstract.Quantity;
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
	 * Get Path of address in tree starting at root
	 * @param uuid address uuid = end node in path (included in path !)
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getAddressPath(String uuid,
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
	 * Create or store address INTO PUBLISHED VERSION ! PERFORMS CHECKS ON DATA !
	 * @param addrDoc map representation of address.
	 * 		If no id/uuid is set address will be created else updated.
	 * @param refetchAfterStore true=fetch and return address after store, false=no fetch, just store
	 * @return response containing result: map representation of created/updated address when refetching,
	 * 		otherwise map containing uuid of stored address (was generated when new address)  
	 */
	IngridDocument publishAddress(IngridDocument addrDoc,
			boolean refetchAfterStore, String userId);

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
	 * (including all subaddresses !)
	 * @param uuid address uuid
	 * @return response containing result: map containing info whether address was fully deleted
	 */
	IngridDocument deleteAddressWorkingCopy(String uuid,
			String userId);

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subaddresses !
	 * Address non existent afterwards !
	 * @param uuid address uuid
	 * @return response containing result: map containing info about success
	 */
	IngridDocument deleteAddress(String uuid,
			String userId);

	/**
	 * Copy an address to another parent.
	 * @param fromUuid uuid of node to copy
	 * @param toUuid uuid of parent where to copy to (new subnode)
	 * @param copySubtree<br>
	 * 		true=also copy subtree<br>
	 * 		false=only address without subAddresses
	 * @param copyToFreeAddress<br>
	 * 		true=copied node is free address, parent has to be null<br>
	 * 		false=copied node is NOT free address, parent can be set, when parent is null
	 * 		copy is "normal" top address
	 * @param userId current user to track jobs of user
	 * @return response containing result: map containing basic data of copied address
	 * and additional info (number of copied addresses ...)
	 */
	IngridDocument copyAddress(String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress,
			String userId);

	/**
	 * Move an address with its subtree to another parent.
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent
	 * @param performSubtreeCheck
	 * 	true=check whether move is possible (e.g. subtree contains no working copies)<br>
	 * 	false=no check, move subtree as it is -> <code>checkAddressSubTree</code> should be
	 * 	called before moving !
	 * @param moveToFreeAddress<br>
	 * 		true=moved node is free address, parent has to be null<br>
	 * 		false=moved node is NOT free address, parent can be set, when parent is null
	 * 		copy is "normal" top address
	 * @return response containing result: map containing info (number of moved addresses ...)
	 */
	IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean performSubtreeCheck, boolean moveToFreeAddress,
			String userId);

	/**
	 * Check whether operations (move,copy) with the subtree of the given address
	 * are permitted or prohibited (e.g. no rights, subtree has working copies ...)
	 * @param uuid address uuid of top node
	 * @return response containing result: map containing info about examination
	 * (has working copies, uuid of found working copy, number checked addresses ...)
	 */
	IngridDocument checkAddressSubTree(String uuid,
			String userId);

	/**
	 * Search Addresses according to given parameters in map.
	 * @param searchParams search parameters (Key:Value pairs in map)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument searchAddresses(IngridDocument searchParams,
			int startHit, int numHits,
			String userId);
}
