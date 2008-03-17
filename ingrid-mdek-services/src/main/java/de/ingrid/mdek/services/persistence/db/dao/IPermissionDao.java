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

	public List<Permission> getObjectPermissions(String addrUuid, String uuid);

	public List<Permission> getAddressPermissions(String addrUuid, String uuid);

	public List<Permission> getUserPermissions(String addrUuid);

}
