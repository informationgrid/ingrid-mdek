/**
 * 
 */
package de.ingrid.mdek.services.security;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
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

	public boolean hasPermissionForAddress(String userUuid, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getAddressPermissions(userUuid, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermission(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForAddress(String userUuid, EntityPermission ep) {
		EntityPermission localPermission = new EntityPermission(ep.permission, ep.getUuid());
		IAddressNodeDao addressNodeDao = daoFactory.getAddressNodeDao();
		AddressNode addressNode;
		do {
			if (hasPermissionForAddress(userUuid, localPermission)) {
				return true;
			}
			addressNode = addressNodeDao.loadByUuid(localPermission.getUuid(), null);
			if (addressNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			localPermission.setUuid(addressNode.getFkAddrUuid());
		} while (localPermission.getUuid() != null);
		return false;
	}

	public boolean hasPermissionForObject(String userUuid, EntityPermission ep) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l;
		l = daoPermissionDao.getObjectPermissions(userUuid, ep.getUuid());
		for (Permission p : l) {
			if (isEqualPermission(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForObject(String userUuid, EntityPermission ep) {
		EntityPermission localPermission = new EntityPermission(ep.getPermission(), ep.getUuid());
		IObjectNodeDao objectNodeDao = daoFactory.getObjectNodeDao();
		ObjectNode objectNode;
		do {
			if (hasPermissionForObject(userUuid, localPermission)) {
				return true;
			}
			objectNode = objectNodeDao.loadByUuid(localPermission.getUuid(), null);
			if (objectNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			localPermission.setUuid(objectNode.getFkObjUuid());
		} while (localPermission.getUuid() != null);
		return false;
	}

	public boolean hasUserPermission(String userUuid, Permission pIn) {
		IPermissionDao daoPermissionDao = daoFactory.getPermissionDao();
		List<Permission> l = daoPermissionDao.getUserPermissions(userUuid);
		for (Permission p : l) {
			if (isEqualPermission(p, pIn)) {
				return true;
			}
		}
		return false;
	}

	public void grantAddressPermission(String userUuid, EntityPermission ep) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		PermissionAddr pa = new PermissionAddr();
		pa.setPermissionId(permission.getId());
		pa.setIdcGroupId(idcUser.getIdcGroupId());
		pa.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		permissionAddrDao.makePersistent(pa);
	}

	public void grantObjectPermission(String userUuid, EntityPermission ep) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		PermissionObj po = new PermissionObj();
		po.setPermissionId(permission.getId());
		po.setIdcGroupId(idcUser.getIdcGroupId());
		po.setUuid(ep.getUuid());

		IGenericDao<IEntity> permissionObjDao = daoFactory.getDao(PermissionObj.class);
		permissionObjDao.makePersistent(po);
	}

	public void grantUserPermission(String userUuid, Permission p) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(p);

		IdcUserPermission iup = new IdcUserPermission();
		iup.setPermissionId(permission.getId());
		iup.setIdcGroupId(idcUser.getIdcGroupId());

		IGenericDao<IEntity> idcUserPermissionDao = daoFactory.getDao(IdcUserPermission.class);
		idcUserPermissionDao.makePersistent(iup);
	}

	public void revokeAddressPermission(String userUuid, EntityPermission ep) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(ep.getPermission());

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

	public void revokeObjectPermission(String userUuid, EntityPermission ep) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(ep.getPermission());

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

	public void revokeUserPermission(String userUuid, Permission p) {
		IdcUser idcUser = getUserByAddrUuid(userUuid);
		Permission permission = findUniquePermissionByExample(p);

		IdcUserPermission iup = new IdcUserPermission();
		iup.setPermissionId(permission.getId());
		iup.setIdcGroupId(idcUser.getIdcGroupId());

		IGenericDao<IEntity> idcUserPermissionDao = daoFactory.getDao(IdcUserPermission.class);
		List<IEntity> iel = idcUserPermissionDao.findByExample(iup);
		for (IEntity ie : iel) {
			idcUserPermissionDao.makeTransient(ie);
		}
	}

	public void deleteAddressPermissions(String addrUuid) {
		PermissionAddr pTemplate = new PermissionAddr();
		pTemplate.setUuid(addrUuid);

		IGenericDao<IEntity> permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		List<IEntity> iel = permissionAddrDao.findByExample(pTemplate);
		for (IEntity ie : iel) {
			permissionAddrDao.makeTransient(ie);
		}
	}

	public void deleteObjectPermissions(String objUuid) {
		PermissionObj pTemplate = new PermissionObj();
		pTemplate.setUuid(objUuid);

		IGenericDao<IEntity> permissionObjDao = daoFactory.getDao(PermissionObj.class);
		List<IEntity> iel = permissionObjDao.findByExample(pTemplate);
		for (IEntity ie : iel) {
			permissionObjDao.makeTransient(ie);
		}
	}

	public Permission getPermissionByPermIdClient(String permIdClient) {
		IdcPermission pClientEnumConst = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, permIdClient);
		Permission pTemplate = null;
		if (IdcPermission.WRITE_SINGLE == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateSingle();
		} else if (IdcPermission.WRITE_TREE == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateTree();
		} else if (IdcPermission.CREATE_ROOT == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateCreateRoot();
		} else if (IdcPermission.QUALITY_ASSURANCE == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateQA();
		}

		return findUniquePermissionByExample(pTemplate);
	}

	public String getPermIdClientByPermission(Permission p) {
		String pIdClient = null;
		if (isEqualPermission(p, PermissionFactory.getPermissionTemplateSingle())) {
			pIdClient = IdcPermission.WRITE_SINGLE.getDbValue();
		} else if (isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
			pIdClient = IdcPermission.WRITE_TREE.getDbValue();
		} else if (isEqualPermission(p, PermissionFactory.getPermissionTemplateCreateRoot())) {
			pIdClient = IdcPermission.CREATE_ROOT.getDbValue();
		} else if (isEqualPermission(p, PermissionFactory.getPermissionTemplateQA())) {
			pIdClient = IdcPermission.QUALITY_ASSURANCE.getDbValue();
		} else if (isEqualPermission(p, PermissionFactory.getDummyPermissionSubTree())) {
			pIdClient = IdcPermission.DUMMY_WRITE_SUBTREE.getDbValue();
		}
	
		return pIdClient;
	}

	/** returns false if a passed permission is null. don't pass empty permissions */
	public boolean isEqualPermission(Permission p1, Permission p2) {
		if (p1 == null || p2 == null) {
			return false;
		}
		if (p1.getAction().equals(p2.getAction()) && p1.getClassName().equals(p2.getClassName())
				&& p1.getName().equals(p2.getName())) {
			return true;
		}
		return false;
	}

	public IdcUser getCatalogAdmin() {
		IIdcUserDao idcUserDao = daoFactory.getIdcUserDao();
		return idcUserDao.getCatalogAdmin();
	}

	public boolean isCatalogAdmin(String userAddrUuid) {
		IdcUser catAdmin = getCatalogAdmin();
		return catAdmin.getAddrUuid().equals(userAddrUuid);
	}

	/**
	 * Loads a Permission Entry from Database dependent from passed selection
	 * criteria (example).
	 * NOTICE: Throws Exception if multiple permissions found.
	 * @param exampleInstance selection data
	 * @return
	 */
	private Permission findUniquePermissionByExample(Permission exampleInstance) {
		IPermissionDao permissionDao = daoFactory.getPermissionDao();
		Permission permission = permissionDao.findUniqueByExample(exampleInstance);
		
		return permission;
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
