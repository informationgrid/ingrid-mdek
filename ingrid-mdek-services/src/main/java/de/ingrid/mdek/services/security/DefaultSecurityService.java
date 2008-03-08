/**
 * 
 */
package de.ingrid.mdek.services.security;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author Administrator
 * 
 */
public class DefaultSecurityService implements ISecurityService {

	protected Logger log;
	protected DaoFactory daoFactory;

	public DefaultSecurityService(Logger log, DaoFactory daoFactory) {
		this.log = log;
		this.daoFactory = daoFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.ISecurityService#hasPermissionForAddress(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForAddress(String addrId, String uuid, Permission p) {
		IPermissionDao daoPermission = daoFactory.getPermissionDao();
		
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.ISecurityService#hasPermissionForObject(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForObject(String addrId, String uuid, Permission p) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.ISecurityService#hasUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasUserPermission(String addrId, Permission p) {
		// TODO Auto-generated method stub
		return false;
	}

	public void grantAddressPermission(String addrId, String uuid, Permission p) {
		// TODO Auto-generated method stub
		
	}

	public void grantObjectPermission(String addrId, String uuid, Permission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeAddressPermission(String addrId, String uuid, Permission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeObjectPermission(String addrId, String uuid, Permission p) {
		// TODO Auto-generated method stub
		
	}

}
