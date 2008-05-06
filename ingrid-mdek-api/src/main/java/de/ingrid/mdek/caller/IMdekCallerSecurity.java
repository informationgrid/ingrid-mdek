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
	 * @param userId calling user
	 * @return response containing result: detailed map representation of created
	 * 		group when refetching otherwise map containing basic data (generated id)  
	 * @throws MdekException group already exists (MdekErrorType.ENTITY_ALREADY_EXISTS). 
	 * @throws MdekException wrong permissions set (MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT). 
	 * @throws MdekException wrong permissions set (MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS). 
	 * @throws MdekException wrong permissions set (MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION). 
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
	 * @param userId calling user
	 * @return response containing result: detailed map representation of
	 * 		group when refetching otherwise map containing basic data (id)  
	 * @throws MdekException wrong permissions set (MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT). 
	 * @throws MdekException wrong permissions set (MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS). 
	 * @throws MdekException wrong permissions set (MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION). 
	 * @throws MdekException wrong permissions set (MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION). 
	 * @throws MdekException if needed permission removed: group user still editing object (MdekErrorType.USER_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user still editing address (MdekErrorType.USER_ADDRESS_PERMISSION_MISSING). 
	 */
	IngridDocument storeGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Deletes an existing group.
	 * @param plugId which mdek server (iplug)
	 * @param groupId The group id.
	 * @param forceDeleteGroupWhenUsers only relevant when group has users<br>
	 * 		true=delete group, remove group data from group users<br>
	 * 		false=error if group has users, error contains the users
	 * @param userId calling user
	 * @return response containing result: map containing former group users (having no permissions now)  
	 * @throws MdekException if the group not exists (MdekErrorType.ENTITY_NOT_FOUND).
	 * @throws MdekException if group has users and no forceDeleteGroupWhenUsers (MdekErrorType.GROUP_HAS_USERS).
	 * @throws MdekException if group user still editing object (MdekErrorType.USER_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if group user still editing address (MdekErrorType.USER_ADDRESS_PERMISSION_MISSING). 
	 */
	IngridDocument deleteGroup(String plugId,
			Long idcGroupId,
			boolean forceDeleteGroupWhenUsers,
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
	 * Get permissions of calling user on given address ("writeTree", "write" ...)
	 * Evaluates permissions in tree and returns found permissions (also inherited "writeTree").  
	 * @param plugId which mdek server (iplug)
	 * @param addrUuid uuid of Address Entity to check
	 * @param userAddrUuid calling user
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getAddressPermissions(String plugId,
			String addrUuid,
			String userAddrUuid);

	/**
	 * Get permissions of calling user on given object ("writeTree", "write" ...)
	 * Evaluates permissions in tree and returns found permissions (also inherited "writeTree").  
	 * @param plugId which mdek server (iplug)
	 * @param objUuid uuid of Object Entity to check
	 * @param userAddrUuid calling user
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getObjectPermissions(String plugId,
			String objUuid,
			String userAddrUuid);

	/**
	 * Get permissions of calling user ("createRoot", "QA", ...).
	 * @param plugId which mdek server (iplug)
	 * @param userAddrUuid calling user
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getUserPermissions(String plugId,
			String userAddrUuid);

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
