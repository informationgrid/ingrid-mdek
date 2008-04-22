/**
 * 
 */
package de.ingrid.mdek.services.security;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
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
	public boolean hasPermissionForAddress(String addrUuid, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getAddressPermissions(addrUuid, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermissions(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasInheritedPermissionForAddress(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public boolean hasInheritedPermissionForAddress(String addrUuid, EntityPermission ep) {
		EntityPermission localPermission = new EntityPermission(ep.permission, ep.getUuid());
		IAddressNodeDao addressNodeDao = daoFactory.getAddressNodeDao();
		AddressNode addressNode;
		do {
			if (hasPermissionForAddress(addrUuid, localPermission)) {
				return true;
			}
			addressNode = addressNodeDao.loadByUuid(localPermission.getUuid());
			localPermission.setUuid(addressNode.getFkAddrUuid());
		} while (localPermission.getUuid() != null);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasPermissionForObject(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForObject(String addrUuid, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getObjectPermissions(addrUuid, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermissions(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasInheritedPermissionForObject(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public boolean hasInheritedPermissionForObject(String addrUuid, EntityPermission ep) {
		EntityPermission localPermission = new EntityPermission(ep.getPermission(), ep.getUuid());
		IObjectNodeDao objectNodeDao = daoFactory.getObjectNodeDao();
		ObjectNode objectNode;
		do {
			if (hasPermissionForObject(addrUuid, localPermission)) {
				return true;
			}
			objectNode = objectNodeDao.loadByUuid(localPermission.getUuid());
			localPermission.setUuid(objectNode.getFkObjUuid());
		} while (localPermission.getUuid() != null);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasUserPermission(String addrUuid, Permission pIn) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l = daoPermissionDao.getUserPermissions(addrUuid);
		for (Permission p : l) {
			if (isEqualPermissions(p, pIn)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#grantAddressPermission(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public void grantAddressPermission(String addrUuid, EntityPermission ep) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();

		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());

		PermissionAddr pa = new PermissionAddr();
		pa.setPermissionId(permission.getId());
		pa.setIdcGroupId(idcUser.getIdcGroupId());
		pa.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		permissionAddrDao.makePersistent(pa);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#grantObjectPermission(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public void grantObjectPermission(String addrUuid, EntityPermission ep) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();

		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());

		PermissionObj po = new PermissionObj();
		po.setPermissionId(permission.getId());
		po.setIdcGroupId(idcUser.getIdcGroupId());
		po.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionObjDao = daoFactory.getDao(PermissionObj.class);
		permissionObjDao.makePersistent(po);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#revokeAddressPermission(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public void revokeAddressPermission(String addrUuid, EntityPermission ep) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();
		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());

		PermissionAddr pa = new PermissionAddr();
		pa.setPermissionId(permission.getId());
		pa.setIdcGroupId(idcUser.getIdcGroupId());
		pa.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		List<IEntity> iel = permissionAddrDao.findByExample(pa);
		for (IEntity ie : iel) {
			permissionAddrDao.makeTransient(ie);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#revokeObjectPermission(java.lang.String,
	 *      de.ingrid.mdek.services.security.EntityPermission)
	 */
	public void revokeObjectPermission(String addrUuid, EntityPermission ep) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();
		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());

		PermissionObj po = new PermissionObj();
		po.setPermissionId(permission.getId());
		po.setIdcGroupId(idcUser.getIdcGroupId());
		po.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionObjDao = daoFactory.getDao(PermissionObj.class);
		List<IEntity> iel = permissionObjDao.findByExample(po);
		for (IEntity ie : iel) {
			permissionObjDao.makeTransient(ie);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#grantUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public void grantUserPermission(String addrUuid, Permission p) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();

		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(p);

		IdcUserPermission iup = new IdcUserPermission();
		iup.setPermissionId(permission.getId());
		iup.setIdcUserId(idcUser.getId());

		IGenericDao<IEntity> idcUserPermissionDao = daoFactory.getDao(IdcUserPermission.class);
		idcUserPermissionDao.makePersistent(iup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#revokeUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public void revokeUserPermission(String addrUuid, Permission p) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();

		IdcUser idcUser = getUserByAddrUuid(addrUuid);
		Permission permission = permissionDao.findUniqueByExample(p);

		IdcUserPermission iup = new IdcUserPermission();
		iup.setPermissionId(permission.getId());
		iup.setIdcUserId(idcUser.getId());

		IGenericDao<IEntity> idcUserPermissionDao = daoFactory.getDao(IdcUserPermission.class);
		List<IEntity> iel = idcUserPermissionDao.findByExample(iup);
		for (IEntity ie : iel) {
			idcUserPermissionDao.makeTransient(ie);
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.security.IPermissionService#getCatalogAdmin()
	 */
	public IdcUser getCatalogAdmin() {
		IIdcUserDao idcUserDao = daoFactory.getIdcUserDao();
		return idcUserDao.getCatalogAdmin();
	}
	
	
	/**
	 * Compares two Permission objects, return true if they are equal, false if
	 * they are not equal.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private boolean isEqualPermissions(Permission p1, Permission p2) {
		if (p1 == null || p2 == null) {
			return false;
		}
		if (p1.getAction().equals(p2.getAction()) && p1.getClassName().equals(p2.getClassName())
				&& p1.getName().equals(p2.getName())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get a IdcUser by it's addrUuid.
	 * 
	 * @param addrUuid
	 * @return
	 */
	private IdcUser getUserByAddrUuid(String addrUuid) {
		IIdcUserDao idcUserDao = daoFactory.getIdcUserDao();
		return idcUserDao.getIdcUserByAddrUuid(addrUuid);
	}

}
