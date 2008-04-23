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
	public boolean hasPermissionForObject(String objUuid, EntityPermission ep);

	/**
	 * Checks an object defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid. The permission can also be inherited by one
	 * of it's parent.
	 * 
	 * @param objUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasInheritedPermissionForObject(String objUuid, EntityPermission ep);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForAddress(String addrUuid, EntityPermission ep);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a
	 * user represented by addrUuid. The permission can also be inherited by one
	 * of it's parent.
	 * 
	 * @param addrUuid
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasInheritedPermissionForAddress(String addrUuid, EntityPermission ep);

	/**
	 * Checks user defined by addrUuid for a Permission permission.
	 * 
	 * @param addrUuid
	 * @param permission
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasUserPermission(String addrUuid, Permission p);

	/**
	 * Grants a specific EntityPermission on object defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	public void grantObjectPermission(String addrUuid, EntityPermission ep);

	/**
	 * Grants a specific EntityPermission on address defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	public void grantAddressPermission(String addrUuid, EntityPermission ep);

	/**
	 * Grants a specific Permission for user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param p
	 */
	public void grantUserPermission(String addrUuid, Permission p);

	/**
	 * Revokes a specific EntityPermission on object defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	public void revokeObjectPermission(String addrUuid, EntityPermission p);

	/**
	 * Revokes a specific EntityPermission on address defined by uuid for user
	 * represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param ep
	 */
	public void revokeAddressPermission(String addrUuid, EntityPermission ep);

	/**
	 * Revokes a specific Permission for user represented by addrUuid.
	 * 
	 * @param addrUuid
	 * @param p
	 */
	public void revokeUserPermission(String addrUuid, Permission p);

	/**
	 * Get the catalog administrator.
	 * 
	 */
	public IdcUser getCatalogAdmin();

}
