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
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
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
	public boolean hasPermissionForAddress(String addrId, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getAddressPermissions(addrId, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermissions(p, ep)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForAddress(String addrId, EntityPermission ep) {
		String uuid = addrId;
		IAddressNodeDao addressNodeDao = daoFactory.getAddressNodeDao();
		AddressNode addressNode;
		do {
			if (hasPermissionForAddress(uuid, ep)) {
				return true;
			}
			addressNode = addressNodeDao.loadByUuid(uuid);
			uuid = addressNode.getFkAddrUuid();
		} while(uuid != null);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasPermissionForObject(java.lang.String,
	 *      java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasPermissionForObject(String addrId, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getObjectPermissions(addrId, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermissions(p, ep)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForObject(String objId, EntityPermission ep) {
		String uuid = objId;
		IObjectNodeDao objectNodeDao = daoFactory.getObjectNodeDao();
		ObjectNode objectNode;
		do {
			if (hasPermissionForObject(uuid, ep)) {
				return true;
			}
			objectNode = objectNodeDao.loadByUuid(uuid);
			uuid = objectNode.getFkObjUuid();
		} while(uuid != null);
		return false;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.IPermissionService#hasUserPermission(java.lang.String,
	 *      de.ingrid.mdek.services.persistence.db.model.Permission)
	 */
	public boolean hasUserPermission(String addrId, Permission pIn) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l = daoPermissionDao.getUserPermissions(addrId);
		for (Permission p : l) {
			if (isEqualPermissions(p, pIn)) {
				return true;
			}
		}
		return false;
	}

	public void grantAddressPermission(String addrId, EntityPermission ep) {
		// TODO: refactor this
		IPermissionDao permissionDao = daoFactory.getPermissionDao();
		IGenericDao<IEntity> idcUserDao = daoFactory.getDao(IdcUser.class);
		IdcUser idcUser = new IdcUser();
		idcUser.setAddrUuid(addrId);
		idcUser = (IdcUser)idcUserDao.findUniqueByExample(idcUser);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());
		
		PermissionAddr pa = new PermissionAddr();
		pa.setPermission(permission);
		pa.setIdcGroupId(idcUser.getIdcGroupId());
		pa.setUuid(ep.getUuid());
		
		IGenericDao<IEntity> permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		permissionAddrDao.makePersistent(pa);
	}

	public void grantObjectPermission(String addrId, EntityPermission ep) {
		// TODO: refactor this
		IPermissionDao permissionDao = daoFactory.getPermissionDao();
		IGenericDao<IEntity> idcUserDao = daoFactory.getDao(IdcUser.class);
		IdcUser idcUser = new IdcUser();
		idcUser.setAddrUuid(addrId);
		idcUser = (IdcUser)idcUserDao.findUniqueByExample(idcUser);
		Permission permission = permissionDao.findUniqueByExample(ep.getPermission());
		
		PermissionObj po = new PermissionObj();
		po.setPermission(permission);
		po.setIdcGroupId(idcUser.getIdcGroupId());
		po.setUuid(ep.getUuid());
		
		IGenericDao<IEntity> permissionObjDao = daoFactory.getDao(PermissionObj.class);
		permissionObjDao.makePersistent(po);
	}

	public void revokeAddressPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeObjectPermission(String addrId, EntityPermission p) {
		// TODO Auto-generated method stub
		
	}

	public void grantUserPermission(String addrId, Permission p) {
		// TODO Auto-generated method stub
		
	}

	public void revokeUserPermission(String addrId, Permission p) {
		// TODO Auto-generated method stub
		
	}

	public boolean isEqualPermissions(Permission p1, Permission p2) {
		if (p1.getAction().equals(p2.getAction())
				&& p1.getClassName().equals(p2.getClassName())
				&& p1.getName().equals(p2.getName())
				) {
			return true;
		} else {
			return false;
		}
	}


}
