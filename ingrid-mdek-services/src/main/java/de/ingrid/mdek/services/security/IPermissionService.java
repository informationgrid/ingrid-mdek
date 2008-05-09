/**
 * 
 */
package de.ingrid.mdek.services.security;

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
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForObject(String userUuid, EntityPermission ep);

	/**
	 * Checks whether the user has a "direct" permission on an address.
	 * NO CHECK OF INHERITED PERMISSIONS !!!
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForAddress(String userUuid, EntityPermission ep);

	/**
	 * Checks whether the user has a "direct" or "inherited" permission on an object.
	 * ALSO CHECKS INHERITED PERMISSIONS !!!
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForObject(String userUuid, EntityPermission ep);

	/**
	 * Checks whether the user has a "direct" or "inherited" permission on an address.
	 * ALSO CHECKS INHERITED PERMISSIONS !!!
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForAddress(String userUuid, EntityPermission ep);

	/**
	 * Checks whether a user has the given UserPermission.
	 * @return True if the permission exists, false if not.
	 */
	boolean hasUserPermission(String userUuid, Permission p);

	/**
	 * Grants a specific object EntityPermission for user (better group)
	 */
	void grantObjectPermission(String userUuid, EntityPermission ep);

	/**
	 * Grants a specific address EntityPermission for user (better group)
	 */
	void grantAddressPermission(String userUuid, EntityPermission ep);

	/**
	 * Grants a specific User Permission for user (better group)
	 */
	void grantUserPermission(String userUuid, Permission p);

	/**
	 * Revokes a specific object EntityPermission for user (better group)
	 */
	void revokeObjectPermission(String userUuid, EntityPermission p);

	/**
	 * Revokes a specific address EntityPermission for user (better group)
	 */
	void revokeAddressPermission(String userUuid, EntityPermission ep);

	/**
	 * Revokes a specific user Permission for user (better group)
	 */
	void revokeUserPermission(String userUuid, Permission p);

	/**
	 * Delete all existing permissions for the given object (called when object is deleted ...).
	 */
	void deleteObjectPermissions(String objUuid);

	/**
	 * Delete all existing permissions for the given address (called when address is deleted ...).
	 */
	void deleteAddressPermissions(String addrUuid);

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
	boolean isEqualPermissions(Permission p1, Permission p2);

	/**
	 * Get the catalog administrator.
	 */
	IdcUser getCatalogAdmin();

	/**
	 * Check whether the given User is the catalog admin !
	 */
	boolean isCatalogAdmin(String userAddrUuid);
}
