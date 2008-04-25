package de.ingrid.mdek.caller;

import de.ingrid.mdek.job.MdekException;
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
	 * @return response containing result: map representation of group with permissions
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
	 * 		group when refetching otherwise map containing basic data (generated id)  
	 */
	IngridDocument createGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Store an existing group. NOTICE: Passed groupDoc must contain ID of group ! 
	 * @param plugId which mdek server (iplug)
	 * @param groupDoc map representation of group, CONTAINS ALSO ID OF GROUP !
	 * @param refetchAfterStore immediately refetch group after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: detailed map representation of
	 * 		group when refetching otherwise map containing basic data (id)  
	 */
	IngridDocument storeGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Deletes an existing group. NOTICE: Passed groupDoc must contain ID of user ! Throws exceptions if the group still has Users or Permissions attached.
	 * 
	 * @param plugId which mdek server (iplug)
	 * @param groupId The group id.
	 * @return response containing result: success and error  
	 * @throws MdekException if the group not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 * @throws MdekException if the group has permissions attached (MdekErrorType.GROUP_HAS_PERMISSIONS). 
	 * @throws MdekException if the group has users attached (MdekErrorType.GROUP_HAS_USERS). 
	 */
	IngridDocument deleteGroup(String plugId,
			Long idcGroupId,
			String userId);
	
	
	/**
	 * Get a user.
	 * @param plugId which mdek server (iplug)
	 * @param addrUuid uuid of the users address
	 * @param userId calling user
	 * @return response containing result: map representation of group
	 * @throws MdekException if the user not exists ((MdekErrorType.ENTITY_NOT_FOUND)). 
	 * @throws MdekException if the addrUuid has no address node ((MdekErrorType.ENTITY_NOT_FOUND)). 
	 */
	IngridDocument getUserDetails(String plugId,
			String addrUuid,
			String userId);
	
	
	/**
	 * Create new user.
	 * @param plugId which mdek server (iplug)
	 * @param userDoc map representation of new user
	 * @param refetchAfterStore immediately refetch user after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: detailed map representation of created
	 * 		user when refetching otherwise map containing basic data (generated id) 
	 * @throws MdekException if the user already exists ((MdekErrorType.ENTITY_ALREADY_EXISTS)). 
	 * @throws MdekException if the user has no parent id set AND is has NOT the catalog admin role ((MdekErrorType.USER_HAS_NO_VALID_PARENT)). 
	 */
	IngridDocument createUser(String plugId,
			IngridDocument userDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Store an existing user. NOTICE: Passed userDoc must contain ID of user ! 
	 * @param plugId which mdek server (iplug)
	 * @param userDoc map representation of user, CONTAINS ALSO ID OF USER !
	 * @param refetchAfterStore immediately refetch user after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: detailed map representation of
	 * 		user when refetching otherwise map containing basic data (id)  
	 * @throws MdekException if the user not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 * @throws MdekException if the user has no parent id set AND is has NOT the catalog admin role ((MdekErrorType.USER_HAS_NO_VALID_PARENT)). 
	 */
	IngridDocument storeUser(String plugId,
			IngridDocument userDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Deletes an existing user. NOTICE: Passed userDoc must contain ID of user ! 
	 * @param plugId which mdek server (iplug)
	 * @param idcUserId user id.
	 * @return response containing result: success and error  
	 * @throws MdekException if the user not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	IngridDocument deleteUser(String plugId,
			Long idcUserId,
			String userId);
	
	/**
	 * Get the calatog admin.
	 * @param plugId which mdek server (iplug)
	 * @return response containing result: detailed map representation of
	 * 		catalog admin  
	 * @throws MdekException if the catalog admin not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	IngridDocument getCatalogAdmin(String plugId,
			String userId);

	/**
	 * Get all sub users of given "parent user"
	 * @param plugId which mdek server (iplug)
	 * @param parentIdcUserId "parent user"
	 * @param userId calling user
	 * @return response containing result: map containing basic representations of sub users
	 * @throws MdekException if the "parent user" not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	IngridDocument getSubUsers(String plugId,
			Long parentIdcUserId,
			String userId);

}
