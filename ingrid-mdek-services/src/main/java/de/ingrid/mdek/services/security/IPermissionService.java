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
	 * Checks an object defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid.
	 * 
	 * @param objUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForObject(String objUuid, EntityPermission ep);

	/**
	 * Checks an object defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid. The permission can also be inherited by one
	 * of it's parent.
	 * 
	 * @param objUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForObject(String objUuid, EntityPermission ep);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	boolean hasPermissionForAddress(String addrUuid, EntityPermission ep);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid. The permission can also be inherited by one
	 * of it's parent.
	 * 
	 * @param addrUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	boolean hasInheritedPermissionForAddress(String addrUuid, EntityPermission ep);

	/**
	 * Checks user defined by addrUuid for a Permission permission.
	 * 
	 * @param addrUuid
	 * @param permission
	 * @return True if the permission exists, false if not.
	 */
	boolean hasUserPermission(String addrUuid, Permission p);

	/**
	 * Grants a specific EntityPermission on object defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	void grantObjectPermission(String addrUuid, EntityPermission ep);

	/**
	 * Grants a specific EntityPermission on address defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	void grantAddressPermission(String addrUuid, EntityPermission ep);

	/**
	 * Grants a specific Permission for user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param p
	 */
	void grantUserPermission(String addrUuid, Permission p);

	/**
	 * Revokes a specific EntityPermission on object defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	void revokeObjectPermission(String addrUuid, EntityPermission p);

	/**
	 * Revokes a specific EntityPermission on address defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	void revokeAddressPermission(String addrUuid, EntityPermission ep);

	/**
	 * Revokes a specific Permission for user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param p
	 */
	void revokeUserPermission(String addrUuid, Permission p);

	/**
	 * Loads a Permission from database identified by its identification used by client.
	 * @param permIdClient permission identification used by client
	 * @return
	 */
	Permission getPermissionByPermIdClient(String permIdClient);

	/**
	 * Maps a permission to its identification used by client !
	 * @param p
	 * @return
	 */
	String getPermIdClientByPermission(Permission p);
	
	/**
	 * Compares two Permission objects, return true if they are equal, false if
	 * they are not equal.
	 * 
	 * @param p1
	 * @param p2
	 * @return
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
