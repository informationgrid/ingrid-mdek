/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public interface ISecurityService {

	/**
	 * Checks an object defined by uuid for a Permission p for a user represented by addrId. 
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param uuid
	 * @param p
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForObject(String addrId, String uuid, Permission p);

	/**
	 * Checks an address defined by uuid for a Permission p for a user represented by addrId.
	 * The permission can also be inherited by one of it's parent.
	 *  
	 * @param addrId
	 * @param uuid
	 * @param p
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasPermissionForAddress(String addrId, String uuid, Permission p);
	
	/**
	 * Checks user defined by addrId for a Permission p.
	 * 
	 * @param addrId
	 * @param p
	 * @return True if the permission exists, false if not.
	 */
	public boolean hasUserPermission(String addrId, Permission p);
	
	/**
	 * Grants a specific Permission on object defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param uuid
	 * @param p
	 */
	public void grantObjectPermission(String addrId, String uuid, Permission p);

	/**
	 * Grants a specific Permission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param uuid
	 * @param p
	 */
	public void grantAddressPermission(String addrId, String uuid, Permission p);

	
	/**
	 * Revokes a specific Permission on object defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param uuid
	 * @param p
	 */
	public void revokeObjectPermission(String addrId, String uuid, Permission p);

	/**
	 * Revokes a specific Permission on address defined by uuid for user represented by addrId.
	 * 
	 * @param addrId
	 * @param uuid
	 * @param p
	 */
	public void revokeAddressPermission(String addrId, String uuid, Permission p);

	
}
