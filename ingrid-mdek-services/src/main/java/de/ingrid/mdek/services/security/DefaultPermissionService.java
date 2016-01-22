/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/**
 * 
 */
package de.ingrid.mdek.services.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserGroup;
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
@Service
public class DefaultPermissionService implements IPermissionService {

	private static final Logger LOG = Logger.getLogger(HddPersistenceService.class);

	protected IPermissionDao permissionDao;
	protected IGenericDao<IEntity> permissionObjDao;
	protected IGenericDao<IEntity> permissionAddrDao;
	protected IGenericDao<IEntity> idcUserPermissionDao;

	protected IIdcUserDao idcUserDao;
	protected IIdcGroupDao idcGroupDao;
	protected IObjectNodeDao objectNodeDao;
	protected IAddressNodeDao addressNodeDao;

	@Autowired
	public DefaultPermissionService(DaoFactory daoFactory) {
		permissionDao = daoFactory.getPermissionDao();
		permissionObjDao = daoFactory.getDao(PermissionObj.class);
		permissionAddrDao = daoFactory.getDao(PermissionAddr.class);
		idcUserPermissionDao = daoFactory.getDao(IdcUserPermission.class);

		idcUserDao = daoFactory.getIdcUserDao();
		idcGroupDao = daoFactory.getIdcGroupDao();
		objectNodeDao = daoFactory.getObjectNodeDao();
		addressNodeDao = daoFactory.getAddressNodeDao();
	}

	public boolean hasPermissionForAddress(String userUuid, EntityPermission ep, Long groupId) {
		List<Permission> l;
		l = permissionDao.getAddressPermissions(userUuid, ep.getUuid(), groupId);
		for (Permission p : l) {
			if (isEqualPermission(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForAddress(String userUuid, EntityPermission ep, Long groupId) {
		// NOTICE: Also virtual parent of all IGE users may be passed ! Then we return NO permissions !
		if (MdekUtils.AddressType.getIGEUserParentUuid().equals(ep.getUuid())) {
			return false;
		}

		EntityPermission localPermission = new EntityPermission(ep.permission, ep.getUuid());
		AddressNode addressNode;
		do {
			if (hasPermissionForAddress(userUuid, localPermission, groupId)) {
				return true;
			}
			addressNode = addressNodeDao.loadByUuid(localPermission.getUuid(), null);
			if (addressNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			localPermission.setUuid(addressNode.getFkAddrUuid());
		} while (MdekUtils.isValidUuid(localPermission.getUuid()));
		return false;
	}

	public boolean hasPermissionForObject(String userUuid, EntityPermission ep, Long groupId) {
		List<Permission> l;
		l = permissionDao.getObjectPermissions(userUuid, ep.getUuid(), groupId);
		for (Permission p : l) {
			if (isEqualPermission(p, ep.getPermission())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInheritedPermissionForObject(String userUuid, EntityPermission ep, Long groupId) {
		EntityPermission localPermission = new EntityPermission(ep.getPermission(), ep.getUuid());
		ObjectNode objectNode;
		do {
			if (hasPermissionForObject(userUuid, localPermission, groupId)) {
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

	public boolean hasUserPermission(String userUuid, Permission pIn, Long groupId) {
		List<Permission> l = permissionDao.getUserPermissions(userUuid, groupId);
		for (Permission p : l) {
			if (isEqualPermission(p, pIn)) {
				return true;
			}
		}
		return false;
	}

	public void grantAddressPermission(String userUuid, EntityPermission ep, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			PermissionAddr pa = new PermissionAddr();
			pa.setPermissionId(permission.getId());
			pa.setIdcGroupId(groupId);
			pa.setUuid(ep.getUuid());

			permissionAddrDao.makePersistent(pa);
		}
	}

	public void grantObjectPermission(String userUuid, EntityPermission ep, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			PermissionObj po = new PermissionObj();
			po.setPermissionId(permission.getId());
			po.setIdcGroupId(groupId);
			po.setUuid(ep.getUuid());

			permissionObjDao.makePersistent(po);
		}
	}

	public void grantUserPermission(String userUuid, Permission p, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(p);

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			IdcUserPermission iup = new IdcUserPermission();
			iup.setPermissionId(permission.getId());
			iup.setIdcGroupId(groupId);

			idcUserPermissionDao.makePersistent(iup);
		}
	}

	public void revokeAddressPermission(String userUuid, EntityPermission ep, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			PermissionAddr pa = new PermissionAddr();
			pa.setPermissionId(permission.getId());
			pa.setIdcGroupId(groupId);
			pa.setUuid(ep.getUuid());

			List<IEntity> iel = permissionAddrDao.findByExample(pa);
			for (IEntity ie : iel) {
				permissionAddrDao.makeTransient(ie);
			}
		}
	}

	public void revokeObjectPermission(String userUuid, EntityPermission ep, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			PermissionObj po = new PermissionObj();
			po.setPermissionId(permission.getId());
			po.setIdcGroupId(groupId);
			po.setUuid(ep.getUuid());

			List<IEntity> iel = permissionObjDao.findByExample(po);
			for (IEntity ie : iel) {
				permissionObjDao.makeTransient(ie);
			}
		}
	}

	public void revokeUserPermission(String userUuid, Permission p, List<Long> groupIds) {
		Permission permission = findUniquePermissionByExample(p);

		// if no explicit groups are passed, use all groups of user !
		if (groupIds == null) {
			groupIds = getGroupIdsOfUser(getUserByAddrUuid(userUuid));
		}

		for (Long groupId : groupIds) {
			IdcUserPermission iup = new IdcUserPermission();
			iup.setPermissionId(permission.getId());
			iup.setIdcGroupId(groupId);

			List<IEntity> iel = idcUserPermissionDao.findByExample(iup);
			for (IEntity ie : iel) {
				idcUserPermissionDao.makeTransient(ie);
			}
		}
	}

	public List<Long> getGroupIdsContainingUserPermission(String userUuid, Permission p) {
		Permission permission = findUniquePermissionByExample(p);
		
		return idcGroupDao.getGroupIdsContainingUserPermission(userUuid, permission.getId());
	}
	
	public List<Long> getGroupIdsContainingObjectPermission(String userUuid, EntityPermission ep) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());
		
		return idcGroupDao.getGroupIdsContainingObjectPermission(userUuid, permission.getId(), ep.getUuid());
	}
	
	public List<Long> getGroupIdsContainingAddressPermission(String userUuid, EntityPermission ep) {
		Permission permission = findUniquePermissionByExample(ep.getPermission());
		
		return idcGroupDao.getGroupIdsContainingAddressPermission(userUuid, permission.getId(), ep.getUuid());
	}
	
	public Permission getPermissionByPermIdClient(String permIdClient) {
		IdcPermission pClientEnumConst = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, permIdClient);
		Permission pTemplate = null;
		if (IdcPermission.WRITE_SINGLE == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateSingle();
		} else if (IdcPermission.WRITE_TREE == pClientEnumConst) {
			pTemplate = PermissionFactory.getPermissionTemplateTree();
        } else if (IdcPermission.WRITE_SUBNODE == pClientEnumConst) {
            pTemplate = PermissionFactory.getPermissionTemplateSubNode();
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
		} else if (isEqualPermission(p, PermissionFactory.getPermissionTemplateSubNode())) {
			pIdClient = IdcPermission.WRITE_SUBNODE.getDbValue();
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

	public IdcUser getCatalogAdminUser() {
		return idcUserDao.getCatalogAdmin();
	}

	public IdcGroup getCatalogAdminGroup() {
		return idcGroupDao.loadByName(MdekUtilsSecurity.GROUP_NAME_ADMINISTRATORS);
	}

	public boolean isCatalogAdmin(String userAddrUuid) {
		IdcUser catAdmin = getCatalogAdminUser();
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
		return idcUserDao.getIdcUserByAddrUuid(addrUuid);
	}

	private List<Long> getGroupIdsOfUser(IdcUser idcUser) {
		List<Long> groupIds = new ArrayList<Long>();
		for (Object userGroup : idcUser.getIdcUserGroups()) {
			groupIds.add(((IdcUserGroup)userGroup).getIdcGroupId());
		}
		return groupIds;
	}

}
