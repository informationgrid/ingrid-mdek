package de.ingrid.mdek.caller;

import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 */
public interface IMdekCallerAddress {

	/**
	 * Fetch top addresses.
	 * @param plugId which mdek server (iplug)
	 * @param userId
	 * @param onlyFreeAddresses true= only free top addresses, false=only NOT free top addresses
	 * @return response containing result: map containing representations of root addresses
	 */
	IngridDocument fetchTopAddresses(String plugId,
			String userId, boolean onlyFreeAddresses);

	/**
	 * Fetch all sub addresses of address with given uuid
	 * @param plugId which mdek server (iplug)
	 * @param uuid address uuid
	 * @return response containing result: map containing representations of sub addresses
	 */
	IngridDocument fetchSubAddresses(String plugId, String uuid,
			String userId);

	/**
	 * Get Path of address in tree starting at root
	 * @param plugId which mdek server (iplug)
	 * @param uuid address uuid = end node in path (included in path !)
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getAddressPath(String plugId, String uuid,
			String userId);

	/**
	 * Fetch single address with given uuid. Pass parameters for paging mechanism of object references.
	 * @param plugId which mdek server (iplug)
	 * @param addrUuid address uuid
	 * @param howMuch how much data to fetch from address
	 * @param objRefsStartIndex objects referencing the given address, object to start with (first object is 0)
	 * @param objRefsMaxNum objects referencing the given address, maximum number to fetch starting at index
	 * @param userId
	 * @return response containing result: map representation of address containing requested data
	 */
	IngridDocument fetchAddress(String plugId, String addrUuid, Quantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum,
			String userId);

	/**
	 * Fetch objects referencing the given address. Pass parameters for paging mechanism.
	 * @param plugId which mdek server (iplug)
	 * @param addrUuid address uuid
	 * @param objRefsStartIndex object to start with (first object is 0)
	 * @param objRefsMaxNum maximum number of objects to fetch starting at index
	 * @param userId
	 * @return response containing result: map representation of objects referencing address
	 */
	IngridDocument fetchAddressObjectReferences(String plugId, String addrUuid, 
			int objRefsStartIndex, int objRefsMaxNum,
			String userId);

	/**
	 * Create or store address INTO WORKING COPY !
	 * @param plugId which mdek server (iplug)
	 * @param adr map representation of address.
	 * 		If no id/uuid is set address will be created else updated.
	 * @param refetchAfterStore immediately refetch address after store (true)
	 * 		or just store without refetching (false)
	 * @param objRefsStartIndex objects referencing the given address, object to start with (first object is 0)
	 * @param objRefsMaxNum objects referencing the given address, maximum number to fetch starting at index
	 * @param userId
	 * @return response containing result: map representation of created/updated address when refetching,
	 * 		otherwise map containing uuid of stored address (was generated when new address)  
	 */
	IngridDocument storeAddress(String plugId, IngridDocument adr,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId);

	/**
	 * Create or store address INTO PUBLISHED VERSION ! PERFORMS CHECKS ON DATA !
	 * @param plugId which mdek server (iplug)
	 * @param addrDoc map representation of address.
	 * 		If no id/uuid is set address will be created else updated.
	 * @param refetchAfterStore true=fetch and return address after store, false=no fetch, just store
	 * @param objRefsStartIndex objects referencing the given address, object to start with (first object is 0)
	 * @param objRefsMaxNum objects referencing the given address, maximum number to fetch starting at index
	 * @param userId
	 * @return response containing result: map representation of created/updated address when refetching,
	 * 		otherwise map containing uuid of stored address (was generated when new address)  
	 * @throws MdekException group already exists (MdekErrorType.ADDRESS_HAS_NO_EMAIL).
	 * @throws MdekException group already exists (MdekErrorType.FREE_ADDRESS_WITH_PARENT).
	 * @throws MdekException group already exists (MdekErrorType.FREE_ADDRESS_WITH_SUBTREE).
	 * @throws MdekException group already exists (MdekErrorType.PARENT_NOT_PUBLISHED).
	 * @throws MdekException group already exists (MdekErrorType.ADDRESS_TYPE_CONFLICT).
	 */
	IngridDocument publishAddress(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId);

	/**
	 * Get initial data for a new address. Pass data needed to determine initial data (e.g. uuid of parent).
	 * @param plugId which mdek server (iplug)
	 * @param newBasicAddress basic new address with data needed to determine initial data, e.g. parent uuid ...
	 * @return extended newAddress, e.g. containing terms of parent etc.
	 */
	IngridDocument getInitialAddress(String plugId, IngridDocument newBasicAddress,
			String userId);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the address is deleted completely, meaning non existent afterwards
	 * (including all subaddresses !)
	 * @param plugId which mdek server (iplug)
	 * @param uuid address uuid
	 * @param forceDeleteReferences only relevant if deletion of working copy causes FULL DELETION (no published version !)<br>
	 * 		true=all references to this address are also deleted
	 * 		false=error if references to this address exist
	 * @return response containing result: map containing info whether address was fully deleted
	 */
	IngridDocument deleteAddressWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId);

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subaddresses !
	 * Address non existent afterwards !
	 * @param plugId which mdek server (iplug)
	 * @param uuid address uuid
	 * @param forceDeleteReferences how to handle references to this address ?<br>
	 * 		true=all references to this address are also deleted
	 * 		false=error if references to this address exist
	 * @return response containing result: map containing info about success
	 */
	IngridDocument deleteAddress(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId);

	/**
	 * Copy an address to another parent.
	 * @param plugId which mdek server (iplug)
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
	IngridDocument copyAddress(String plugId, String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress,
			String userId);

	/**
	 * Move an address with its subtree to another parent.
	 * @param plugId which mdek server (iplug)
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent
	 * @param moveToFreeAddress<br>
	 * 		true=moved node is free address, parent has to be null<br>
	 * 		false=moved node is NOT free address, parent can be set, when parent is null
	 * 		copy is "normal" top address
	 * @return response containing result: map containing info (number of moved addresses ...)
	 */
	IngridDocument moveAddress(String plugId, String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			String userId);

	/**
	 * Check whether operations (move,copy) with the subtree of the given address
	 * are permitted or prohibited (e.g. no rights, subtree has working copies ...)
	 * @param plugId which mdek server (iplug)
	 * @param uuid address uuid of top node
	 * @return response containing result: map containing info about examination
	 * (has working copies, uuid of found working copy, number checked addresses ...)
	 */
	IngridDocument checkAddressSubTree(String plugId, String uuid,
			String userId);

	/**
	 * Search Addresses according to given parameters in map.
	 * @param plugId which mdek server (iplug)
	 * @param searchParams search parameters (Key:Value pairs in map)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument searchAddresses(String plugId, IngridDocument searchParams,
			int startHit, int numHits,
			String userId);
}
