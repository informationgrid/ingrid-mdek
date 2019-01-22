/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.mdek.services.security;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Describes all public methods of the security service.
 * 
 * 
 * @author joachim
 * 
 */
public interface IPermissionService {

	/**
	 * Checks whether the user has a "direct" permission on an object.
	 * NO CHECK OF INHERITED PERMISSIONS !!!
	 * @param userUuid uuid of user
	 * @param ep permission on object to search for
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForObject(String userUuid, EntityPermission ep, Long groupId);

	/**
	 * Checks whether the user has a "direct" permission on an address.
	 * NO CHECK OF INHERITED PERMISSIONS !!!
	 * @param userUuid uuid of user
	 * @param ep permission on address to search for
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForAddress(String userUuid, EntityPermission ep, Long groupId);

	/**
	 * Checks whether the user has a "direct" or "inherited" permission on an object.
	 * ALSO CHECKS INHERITED PERMISSIONS !!!
	 * @param userUuid uuid of user
	 * @param ep permission on object to search for
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForObject(String userUuid, EntityPermission ep, Long groupId);

	/**
	 * Checks whether the user has a "direct" or "inherited" permission on an address.
	 * ALSO CHECKS INHERITED PERMISSIONS !!!
	 * @param userUuid uuid of user
	 * @param ep permission on address to search for
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForAddress(String userUuid, EntityPermission ep, Long groupId);

	/**
	 * Checks whether a user has the given UserPermission.
	 * @param userUuid uuid of user
	 * @param p user permission to search for
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return True if the permission exists, false if not.
	 */
	boolean hasUserPermission(String userUuid, Permission p, Long groupId);

	/**
	 * Grants a specific object EntityPermission for user (better groups).
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is granted on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param ep
	 * @param groupIds groups where the permission is added, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void grantObjectPermission(String userUuid, EntityPermission ep, List<Long> groupIds);

	/**
	 * Grants a specific address EntityPermission for user (better groups).
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is granted on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param ep
	 * @param groupIds groups where the permission is added, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void grantAddressPermission(String userUuid, EntityPermission ep, List<Long> groupIds);

	/**
	 * Grants a specific User Permission for user (better groups).
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is granted on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param ep
	 * @param groupIds groups where the permission is added, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void grantUserPermission(String userUuid, Permission p, List<Long> groupIds);

	/**
	 * Revokes a specific object EntityPermission for user (better groups)
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is revoked on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param ep
	 * @param groupIds groups where the permission is revoked, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void revokeObjectPermission(String userUuid, EntityPermission p, List<Long> groupIds);

	/**
	 * Revokes a specific address EntityPermission for user (better groups)
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is revoked on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param ep
	 * @param groupIds groups where the permission is revoked, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void revokeAddressPermission(String userUuid, EntityPermission ep, List<Long> groupIds);

	/**
	 * Revokes a specific user Permission for user (better groups)
	 * <br>NOTICE: User has now MULTIPLE groups ! Permission is revoked on ALL groups
	 * of user if no specific groups are passed !
	 * <br><b>NOTICE: NO CHECK ON PASSED GROUPS, whether user belongs to them !</b>
	 * @param userUuid
	 * @param p Permission Template
	 * @param groupIds groups where the permission is revoked, pass NULL for all groups of user.
	 * No check whether user is connected to passed groups !
	 */
	void revokeUserPermission(String userUuid, Permission p, List<Long> groupIds);

	/**
	 * Get ids of groups containing the given user permission connected to given user.  
	 * @param userUuid user the groups are connected to
	 * @param p Permission Template
	 * @return ids of groups or empty list
	 */
	public List<Long> getGroupIdsContainingUserPermission(String userUuid, Permission p);

	/**
	 * Get ids of groups (of given user) containing the given object entity permission.  
	 * @param userUuid user the groups are connected to
	 * @param ep object permission the group contains
	 * @return ids of groups or empty list
	 */
	public List<Long> getGroupIdsContainingObjectPermission(String userUuid, EntityPermission ep);

	/**
	 * Get ids of groups (of given user) containing the given address entity permission.  
	 * @param userUuid user the groups are connected to
	 * @param ep address permission the group contains
	 * @return ids of groups or empty list
	 */
	public List<Long> getGroupIdsContainingAddressPermission(String userUuid, EntityPermission ep);

	/**
	 * Loads a Permission from database identified by its identification used by client.
	 * @param permIdClient permission identification used by client
	 * @return
	 */
	Permission getPermissionByPermIdClient(String permIdClient);

	/**
	 * Maps a permission to its identification used by client !
	 */
	String getPermIdClientByPermission(Permission p);
	
	/**
	 * Compares two Permission objects, return true if they are equal, false if
	 * they are not equal.
	 */
	boolean isEqualPermission(Permission p1, Permission p2);

	/**
	 * Get the catalog administrator.
	 */
	IdcUser getCatalogAdminUser();

	/**
	 * Get the administrator group.
	 */
	IdcGroup getCatalogAdminGroup();

	/**
	 * Check whether the given User is the catalog admin !
	 */
	boolean isCatalogAdmin(String userAddrUuid);
}
