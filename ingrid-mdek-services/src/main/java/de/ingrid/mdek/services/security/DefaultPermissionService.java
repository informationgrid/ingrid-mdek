/**
 * 
 */
package de.ingrid.mdek.services.security;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.hdd.HddPersistenceService;

/**
 * @author Administrator
 * 
 */
public class DefaultPermissionService implements IPermissionService {

	private static final Logger LOG = Logger.getLogger(HddPersistenceService.class);	
	protected DaoFactory daoFactory;

	public DefaultPermissionService(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasPermissionForAddress(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForAddress(String addrId, EntityPermission ep) {
		
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		daoPermissionDao.beginTransaction();
		List<Permission> l;
		l = daoPermissionDao.getAddressPermissions(addrId, ep.getUuid());
		daoPermissionDao.commitTransaction();
		for (Permission p : l) {
			if (ep.equalsPermission(p)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasPermissionForObject(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForObject(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasUserPermission(String addrId, Permission p) {
		// TODO Auto-generated method stub
		return false;
	}

	public void grantAddressPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

	public void grantObjectPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeAddressPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeObjectPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

}
