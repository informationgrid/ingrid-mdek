/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public interface IPermissionService {

	/**
	 * Checks an object defined by uuid for a EntityPermission permission for a user represented by addrId. 
	 *  
	 * @param addrId
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForObject(String addrId, EntityPermission ep);

	/**
	 * Checks an object defined by uuid for a EntityPermission permission for a user represented by addrId. 
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasInheritedPermissionForObject(String addrId, EntityPermission ep);
	
	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a user represented by addrId.
	 *  
	 * @param addrId
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForAddress(String addrId, EntityPermission ep);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a user represented by addrId.
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param ep
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasInheritedPermissionForAddress(String addrId, EntityPermission ep);
	
	/**
	 * Checks user defined by addrId for a Permission permission.
	 * 
	 * @param addrId
	 * @param permission
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasUserPermission(String addrId, Permission p);
	
	/**
	 * Grants a specific EntityPermission on object defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param ep
	 */
	public void grantObjectPermission(String addrId, EntityPermission ep);

	/**
	 * Grants a specific EntityPermission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param ep
	 */
	public void grantAddressPermission(String addrId, EntityPermission ep);

	/**
	 * Grants a specific Permission for user represented by addrId.
	 * 
	 * @param addrId
	 * @param p
	 */
	public void grantUserPermission(String addrId, Permission p);
	
	/**
	 * Revokes a specific EntityPermission on object defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param ep
	 */
	public void revokeObjectPermission(String addrId, EntityPermission p);

	/**
	 * Revokes a specific EntityPermission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param ep
	 */
	public void revokeAddressPermission(String addrId, EntityPermission ep);

	/**
	 * Revokes a specific Permission for user represented by addrId.
	 * 
	 * @param addrId
	 * @param p
	 */
	public void revokeUserPermission(String addrId, Permission p);
	
	
	/** 
	 * Compares two Permission objects, return true if they are equal, false if they are not equal.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean isEqualPermissions(Permission p1, Permission p2);
	
}
