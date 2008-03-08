package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.SysList;



/**
 * Business DAO operations related to the <tt>Permission</tt> entity.
 * 
 * @author Joachim
 */
public interface IPermissionDao
	extends IGenericDao<SysList> {

	public List<Permission> getObjectPermissions(String addrId, String uuid);
	
	public List<Permission> getAddressPermissions(String addrId, String uuid);

	public List<Permission> getUserPermissions(String addrId);
	
}
