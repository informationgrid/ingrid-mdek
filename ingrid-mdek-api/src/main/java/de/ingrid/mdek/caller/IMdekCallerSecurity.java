/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.caller;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning SECURITY / USER MANAGEMENT.
 */
public interface IMdekCallerSecurity extends IMdekCaller {

	/**
	 * Get all groups.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @param includeCatAdminGroup true=group of catalog administrator is included<br>
	 * false=skip group of catalog administrator, all other groups are returned
	 * @return response containing result: map containing groups
	 */
	IngridDocument getGroups(String plugId,
			String userId,
			boolean includeCatAdminGroup);

	/**
	 * Get details of group (NOT including users, use separate method to extract users of group ...)
	 * @param plugId which mdek server (iplug)
	 * @param groupName name of group
	 * @param userId calling user
	 * @return response containing result: map representation of group with permissions
	 */
	IngridDocument getGroupDetails(String plugId,
			String groupName,
			String userId);

	/**
	 * Get users of group.
	 * @param plugId which mdek server (iplug)
	 * @param groupName name of group
	 * @param userId calling user
	 * @return response containing result: map representation of users in group
	 */
	IngridDocument getUsersOfGroup(String plugId,
			String groupName,
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
	 * @throws MdekException if needed permission removed: group user still editing object (MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user responsible for object (MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user still editing address (MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user responsible for address (MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING). 
	 * @throws MdekException if no right to remove object permission (MdekErrorType.NO_RIGHT_TO_REMOVE_OBJECT_PERMISSION). 
	 * @throws MdekException if no right to remove address permission (MdekErrorType.NO_RIGHT_TO_REMOVE_ADDRESS_PERMISSION). 
	 * @throws MdekException if no right to remove user permission (MdekErrorType.NO_RIGHT_TO_REMOVE_USER_PERMISSION). 
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
	 * @throws MdekException if group user still editing object (MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user responsible for object (MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING). 
	 * @throws MdekException if group user still editing address (MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING). 
	 * @throws MdekException if needed permission removed: group user responsible for address (MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING). 
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
	 * Get permissions of calling user on given address ("write-tree", "write" ...)
	 * Evaluates permissions in tree and returns found permissions (also inherited "write-tree").  
	 * @param plugId which mdek server (iplug)
	 * @param addrUuid uuid of Address Entity to check
	 * @param userAddrUuid calling user
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
	 * 		entity is in state "Q" and user is NOT QA !
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getAddressPermissions(String plugId,
			String addrUuid,
			String userAddrUuid,
			boolean checkWorkflow);

	/**
	 * Get permissions of calling user on given object ("write-tree", "write" ...)
	 * Evaluates permissions in tree and returns found permissions (also inherited "write-tree").  
	 * @param plugId which mdek server (iplug)
	 * @param objUuid uuid of Object Entity to check
	 * @param userAddrUuid calling user
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
	 * 		entity is in state "Q" and user is NOT QA !
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getObjectPermissions(String plugId,
			String objUuid,
			String userAddrUuid,
			boolean checkWorkflow);

	/**
     * Get the permissions of a user.
     * @param plugId which mdek server (iplug)
     * @param addrUuid uuid of the users address
     * @param userId calling user
	 * @return response containing result: map representation of permissions
	 */
	IngridDocument getUserPermissions(String plugId,
			String addrUuid, String userId);

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
	 * @throws MdekException if calling user role not "above" role of new user (MdekErrorType.USER_HAS_WRONG_ROLE). 
	 * @throws MdekException if calling user not parent of new user (MdekErrorType.USER_HIERARCHY_WRONG). 
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
	 * @throws MdekException if updated role differs from former role OR calling user role not "above" role of user to update (MdekErrorType.USER_HAS_WRONG_ROLE). 
	 * @throws MdekException if updated parent differs from former parent OR calling user not parent of user to update (MdekErrorType.USER_HIERARCHY_WRONG). 
	 */
	IngridDocument storeUser(String plugId,
			IngridDocument userDoc,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Deletes an existing user.<br>
	 * NOTICE: users parent user is set in all entities where user is responsible user !
	 * @param plugId which mdek server (iplug)
	 * @param idcUserId user id.
	 * @return response containing result: success and error  
	 * @throws MdekException if the user not exists (MdekErrorType.ENTITY_NOT_FOUND). 
	 * @throws MdekException if user to delete is catalog admin (MdekErrorType.USER_IS_CATALOG_ADMIN). 
	 * @throws MdekException if user to delete has subusers (MdekErrorType.USER_HAS_SUBUSERS). 
	 * @throws MdekException if calling user role not "above" role of user to delete (MdekErrorType.USER_HAS_WRONG_ROLE). 
	 * @throws MdekException if calling user not parent of user to delete (MdekErrorType.USER_HIERARCHY_WRONG). 
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

	/**
	 * Get all users who have write permission for the given object.
	 * @param plugId which mdek server (iplug)
	 * @param objectUuid object to get "write users" for
	 * @param userId calling user
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
	 * 		entity is in state "Q" and user is NOT QA !
	 * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
	 * false=only users are returned without detailed permissions
	 * @return response containing result: map containing basic representations of users
	 */
	IngridDocument getUsersWithWritePermissionForObject(String plugId,
			String objectUuid,
			String userId,
			boolean checkWorkflow,
			boolean getDetailedPermissions);

	/**
	 * Get all users who have write permission for the given address.
	 * @param plugId which mdek server (iplug)
	 * @param addressUuid address to get "write users" for
	 * @param userId calling user
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
	 * 		entity is in state "Q" and user is NOT QA !
	 * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
	 * false=only users are returned without detailed permissions
	 * @return response containing result: map containing basic representations of users
	 */
	IngridDocument getUsersWithWritePermissionForAddress(String plugId,
			String addressUuid,
			String userId,
			boolean checkWorkflow,
			boolean getDetailedPermissions);

    /**
     * Get all users who have write-tree permission on new object. If object is created underneath parent with
     * write-subnode, then all users in write-subnode group OF CALLING USER are added. 
     * The group with the write-subnode permission will receive an additional write-tree permission on the new node !
     * This is called when a NEW node is created under the given object for determining responsible users.
     * @param plugId which mdek server (iplug)
     * @param parentObjectUuid object where new object is created underneath = object to get "write users" for
     * @param userId calling user
     * @param checkWorkflow false=workflow state is ignored, only check tree permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
     *      entity is in state "Q" and user is NOT QA !
     * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
     * false=only users are returned without detailed permissions
     * @return response containing result: map containing basic representations of users
     */
    IngridDocument getResponsibleUsersForNewObject(String plugId,
            String parentObjectUuid,
            String userId,
            boolean checkWorkflow,
            boolean getDetailedPermissions);

    /**
     * Get all users who have write-tree permission on new address. If address is created underneath parent with
     * write-subnode, then all users in write-subnode group of calling user are added. 
     * The group with the write-subnode permission will receive an additional write-tree permission on the new node !
     * This is called when a NEW node is created under the given address for determining responsible users.
     * @param plugId which mdek server (iplug)
     * @param parentAddressUuid address where new address is created underneath = address to get "write users" for
     * @param userId calling user
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
     *      entity is in state "Q" and user is NOT QA !
     * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
     * false=only users are returned without detailed permissions
     * @return response containing result: map containing basic representations of users
     */
    IngridDocument getResponsibleUsersForNewAddress(String plugId,
            String parentAddressUuid,
            String userId,
            boolean checkWorkflow,
            boolean getDetailedPermissions);

    /**
     * Get all users who have any entity permission for the given object.
     * @param plugId which mdek server (iplug)
     * @param objectUuid object to get "tree users" for
     * @param userId calling user
     * @param checkWorkflow false=workflow state is ignored, only check tree permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
     *      entity is in state "Q" and user is NOT QA !
     * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
     * false=only users are returned without detailed permissions
     * @return response containing result: map containing basic representations of users
     */
    IngridDocument getUsersWithPermissionForObject(String plugId,
            String objectUuid,
            String userId,
            boolean checkWorkflow,
            boolean getDetailedPermissions);

    /**
     * Get all users who have any permission for the given address.
     * @param plugId which mdek server (iplug)
     * @param addressUuid address to get "write users" for
     * @param userId calling user
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return no write permission if 
     *      entity is in state "Q" and user is NOT QA !
     * @param getDetailedPermissions true=every user contains his detailed permissions (write-tree, qa, ...)<br>
     * false=only users are returned without detailed permissions
     * @return response containing result: map containing basic representations of users
     */
    IngridDocument getUsersWithPermissionForAddress(String plugId,
            String addressUuid,
            String userId,
            boolean checkWorkflow,
            boolean getDetailedPermissions);
    
}
