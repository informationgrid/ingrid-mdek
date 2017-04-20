/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans
 * concerning SECURITY / USER MANAGEMENT.
 */
public class DocToBeanMapperSecurity implements IMapper {

	private static final Logger LOG = LogManager.getLogger(DocToBeanMapperSecurity.class);

	private static DocToBeanMapperSecurity myInstance;

	private static DocToBeanMapper docToBeanMapper;
	private static IPermissionService permService;

	/** Generic dao for class unspecific operations !!! */
	private IGenericDao<IEntity> dao;

	/** Get The Singleton */
	public static synchronized DocToBeanMapperSecurity getInstance(
			DaoFactory daoFactory, 
			IPermissionService permService)
	{
		if (myInstance == null) {
	        myInstance = new DocToBeanMapperSecurity(daoFactory, permService);
	      }
		return myInstance;
	}

	private DocToBeanMapperSecurity(DaoFactory daoFactory, IPermissionService inPermService) {
		permService = inPermService;
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);

		dao = daoFactory.getDao(IEntity.class);
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping quantity.
	 */
	public IdcGroup mapIdcGroup(IngridDocument docIn, IdcGroup grpIn, MappingQuantity howMuch) {

		grpIn.setName(docIn.getString(MdekKeysSecurity.NAME));

		String creationDate = docIn.getString(MdekKeysSecurity.DATE_OF_CREATION);
		if (creationDate != null) {
			grpIn.setCreateTime(creationDate);				
		}
		grpIn.setModTime(docIn.getString(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION));
		grpIn.setModUuid(docToBeanMapper.extractModUserUuid(docIn));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			// update associations
			updatePermissionAddrs(docIn, grpIn);
			updatePermissionObjs(docIn, grpIn);
			updateIdcUserPermissions(docIn, grpIn);
		}

		return grpIn;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping quantity.
	 */
	public IdcUser mapIdcUser(IngridDocument docIn, IdcUser userIn) {

		userIn.setAddrUuid(docIn.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID));
		userIn.setIdcRole((Integer)docIn.get(MdekKeysSecurity.IDC_ROLE));
		Long parentId = (Long)docIn.get(MdekKeysSecurity.PARENT_IDC_USER_ID); 
		if (parentId == null && !IdcRole.CATALOG_ADMINISTRATOR.getDbValue().equals(userIn.getIdcRole())) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_VALID_PARENT));
		}
		userIn.setParentId(parentId);

		String creationDate = docIn.getString(MdekKeysSecurity.DATE_OF_CREATION);
		if (creationDate != null) {
			userIn.setCreateTime(creationDate);				
		}
		userIn.setModTime(docIn.getString(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION));
		userIn.setModUuid(docToBeanMapper.extractModUserUuid(docIn));

		// update associations
		updateIdcUserGroups(docIn, userIn);

		return userIn;
	}

	private IdcUserGroup mapIdcUserGroup(Long userId,
			Long groupId,
			IdcUserGroup ref)
	{
		ref.setIdcUserId(userId);
		ref.setIdcGroupId(groupId);

		return ref;
	}

	private void updateIdcUserGroups(IngridDocument docIn, IdcUser userIn) {
		Long[] docGroupIds = (Long[]) docIn.get(MdekKeysSecurity.IDC_GROUP_IDS);
		if (docGroupIds == null) {
			docGroupIds = new Long[] {};
		}
		Set<IdcUserGroup> refs = userIn.getIdcUserGroups();
		ArrayList<IdcUserGroup> refs_unprocessed = new ArrayList<IdcUserGroup>(refs);
		for (Long docGroupId : docGroupIds) {
			boolean found = false;
			for (IdcUserGroup ref : refs) {
				if (docGroupId.equals(ref.getIdcGroupId())) {
					refs_unprocessed.remove(ref);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				IdcUserGroup ref = mapIdcUserGroup(userIn.getId(), docGroupId, new IdcUserGroup());
				refs.add(ref);
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (IdcUserGroup ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);
		}
	}

	/**
	 * Remove the given group from the given user ! NOTICE: does NOT persist user !
	 */
	public void removeIdcUserGroup(long grpIdToRemove, IdcUser userIn) {
		Set<IdcUserGroup> userGrps = userIn.getIdcUserGroups();
		ArrayList<IdcUserGroup> userGrpsToRemove = new ArrayList<IdcUserGroup>();

		for (IdcUserGroup userGrp : userGrps) {
			if (grpIdToRemove == userGrp.getIdcGroupId()) {
				userGrpsToRemove.add(userGrp);
			}
		}

		// remove, will be deleted by hibernate (delete-orphan set in parent)
		for (IdcUserGroup userGrpToRemove : userGrpsToRemove) {
			userGrps.remove(userGrpToRemove);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(userGrpToRemove);
		}
	}

	private PermissionObj mapPermissionObj(Long groupId,
			String objUuid,
			Permission perm,
			PermissionObj ref)
	{
		ref.setIdcGroupId(groupId);
		ref.setUuid(objUuid);
		ref.setPermissionId(perm.getId());
		ref.setPermission(perm);

		return ref;
	}

	private PermissionAddr mapPermissionAddr(Long groupId,
			String addrUuid,
			Permission perm,
			PermissionAddr ref)
	{
		ref.setIdcGroupId(groupId);
		ref.setUuid(addrUuid);
		ref.setPermissionId(perm.getId());
		ref.setPermission(perm);

		return ref;
	}

	private IdcUserPermission mapIdcUserPermission(Long groupId,
			Permission perm,
			IdcUserPermission ref)
	{
		ref.setIdcGroupId(groupId);
		ref.setPermissionId(perm.getId());
		ref.setPermission(perm);

		return ref;
	}

	/**
	 * Transfer OBJECT permissions from given doc to given group bean.
	 * NOTICE: also deletes removed permissions in bean/database.
	 */
	public void updatePermissionObjs(IngridDocument gDocIn, IdcGroup gIn) {
		List<IngridDocument> refDocs = (List) gDocIn.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<PermissionObj> refs = gIn.getPermissionObjs();
		ArrayList<PermissionObj> refs_unprocessed = new ArrayList<PermissionObj>(refs);
		for (IngridDocument refDoc : refDocs) {
			String inUuid = refDoc.getString(MdekKeys.UUID);
			String inPermId = refDoc.getString(MdekKeysSecurity.IDC_PERMISSION);
			Permission inPerm = permService.getPermissionByPermIdClient(inPermId);
			boolean found = false;
			for (PermissionObj ref : refs) {
				String refUuid = ref.getUuid();
				Permission refPerm = ref.getPermission();
				boolean samePermission = permService.isEqualPermission(inPerm, refPerm);
				if (inUuid.equals(refUuid) && samePermission) {
					refs_unprocessed.remove(ref);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				PermissionObj ref = mapPermissionObj(gIn.getId(), inUuid, inPerm, new PermissionObj());
				refs.add(ref);
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (PermissionObj ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);
		}
	}

	/**
	 * Transfer ADDRESS permissions from given doc to given group bean.
	 * NOTICE: also deletes removed permissions in bean/database.
	 */
	public void updatePermissionAddrs(IngridDocument gDocIn, IdcGroup gIn) {
		List<IngridDocument> refDocs = (List) gDocIn.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<PermissionAddr> refs = gIn.getPermissionAddrs();
		ArrayList<PermissionAddr> refs_unprocessed = new ArrayList<PermissionAddr>(refs);
		for (IngridDocument refDoc : refDocs) {
			String inUuid = refDoc.getString(MdekKeys.UUID);
			String inPermId = refDoc.getString(MdekKeysSecurity.IDC_PERMISSION);
			Permission inPerm = permService.getPermissionByPermIdClient(inPermId);
			boolean found = false;
			for (PermissionAddr ref : refs) {
				String refUuid = ref.getUuid();
				Permission refPerm = ref.getPermission();
				boolean samePermission = permService.isEqualPermission(inPerm, refPerm);
				if (inUuid.equals(refUuid) && samePermission) {
					refs_unprocessed.remove(ref);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				PermissionAddr ref = mapPermissionAddr(gIn.getId(), inUuid, inPerm, new PermissionAddr());
				refs.add(ref);
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (PermissionAddr ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);
		}
	}

	/**
	 * Transfer USER permissions from given doc to given group bean.
	 * NOTICE: also deletes removed permissions in bean/database.
	 */
	public void updateIdcUserPermissions(IngridDocument uDocIn, IdcGroup gIn) {
		List<IngridDocument> refDocs = (List) uDocIn.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<IdcUserPermission> refs = gIn.getIdcUserPermissions();
		ArrayList<IdcUserPermission> refs_unprocessed = new ArrayList<IdcUserPermission>(refs);
		for (IngridDocument refDoc : refDocs) {
			String inPermId = refDoc.getString(MdekKeysSecurity.IDC_PERMISSION);
			Permission inPerm = permService.getPermissionByPermIdClient(inPermId);
			boolean found = false;
			for (IdcUserPermission ref : refs) {
				Permission refPerm = ref.getPermission();
				boolean samePermission = permService.isEqualPermission(inPerm, refPerm);
				if (samePermission) {
					refs_unprocessed.remove(ref);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				IdcUserPermission ref = mapIdcUserPermission(gIn.getId(), inPerm, new IdcUserPermission());
				refs.add(ref);
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (IdcUserPermission ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);
		}
	}
}
