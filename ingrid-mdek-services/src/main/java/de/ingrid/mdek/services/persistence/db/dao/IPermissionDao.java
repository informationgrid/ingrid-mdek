package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Business DAO operations related to the <tt>Permission</tt> entity.
 * 
 * @author Joachim
 */
public interface IPermissionDao extends IGenericDao<Permission> {

	/**
	 * Get "directly" set permissions of given user on given object entity (via group of user).
	 * NO INHERITED PERMISSIONS.
	 * @param userUuid address uuid of user
	 * @param objUuid uuid of object entity to check
	 * @return list of permissions set for object (in group of user).
	 */
	public List<Permission> getObjectPermissions(String userUuid, String objUuid);

	/**
	 * Get "directly" set permissions of given user on given address entity (via group of user).
	 * NO INHERITED PERMISSIONS.
	 * @param userUuid address uuid of user
	 * @param addrUuid uuid of address entity to check
	 * @return list of permissions set for address (in group of user).
	 */
	public List<Permission> getAddressPermissions(String userUuid, String addrUuid);

	/**
	 * Get user permissions of given user.
	 * @param userUuid address uuid of user
	 * @return list of permissions set for user (in group of user).
	 */
	public List<Permission> getUserPermissions(String userUuid);
}
