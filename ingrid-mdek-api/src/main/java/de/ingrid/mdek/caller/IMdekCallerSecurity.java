package de.ingrid.mdek.caller;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning SECURITY / USER MANAGEMENT.
 */
public interface IMdekCallerSecurity {

	/**
	 * Get all groups.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing groups
	 */
	IngridDocument getGroups(String plugId,
			String userId);

	/**
	 * Get a group.
	 * @param plugId which mdek server (iplug)
	 * @param name name of group
	 * @param userId calling user
	 * @return response containing result: map representation of group
	 */
	IngridDocument getGroupDetails(String plugId,
			String name,
			String userId);

	/**
	 * Create new group.
	 * @param plugId which mdek server (iplug)
	 * @param groupDoc map representation of new group
	 * @param refetchAfterStore immediately refetch group after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: detailed map representation of created
	 * 		group when refetching otherwise map containing basic data (name)  
	 */
	IngridDocument createGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId);
}
