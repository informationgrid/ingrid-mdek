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
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param permission
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForObject(String addrId, EntityPermission p);

	/**
	 * Checks an address defined by uuid for a EntityPermission permission for a user represented by addrId.
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param permission
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForAddress(String addrId, EntityPermission p);
	
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
	 * @param permission
	 */
	public void grantObjectPermission(String addrId, EntityPermission p);

	/**
	 * Grants a specific EntityPermission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param permission
	 */
	public void grantAddressPermission(String addrId, EntityPermission p);

	
	/**
	 * Revokes a specific EntityPermission on object defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param permission
	 */
	public void revokeObjectPermission(String addrId, EntityPermission p);

	/**
	 * Revokes a specific EntityPermission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param permission
	 */
	public void revokeAddressPermission(String addrId, EntityPermission p);

	
}
