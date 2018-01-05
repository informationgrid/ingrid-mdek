/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
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
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents
 * concerning SECURITY / USER MANAGEMENT.
 */
public class BeanToDocMapperSecurity implements IMapper {

	private static BeanToDocMapperSecurity myInstance;

	private static BeanToDocMapper beanToDocMapper;
	private static IPermissionService permService;

	/** Get The Singleton */
	public static synchronized BeanToDocMapperSecurity getInstance(
			DaoFactory daoFactory, 
			IPermissionService permService)
	{
		if (myInstance == null) {
	        myInstance = new BeanToDocMapperSecurity(daoFactory, permService);
	      }
		return myInstance;
	}

	private BeanToDocMapperSecurity(DaoFactory daoFactory, IPermissionService inPermService) {
		permService = inPermService;
        beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
	}

	/**
	 * Transfer data of passed bean to passed doc.
	 * Also includes all related data (associations/collections) dependent from MappingQuantity.
	 * @return doc containing mapped data.
	 */
	public IngridDocument mapIdcGroup(IdcGroup group, IngridDocument groupDoc,
			MappingQuantity howMuch) {
		if (group == null) {
			return groupDoc;
		}

		// also ID, is needed when name is changed !!!
		groupDoc.put(MdekKeysSecurity.ID, group.getId());
		groupDoc.put(MdekKeysSecurity.NAME, group.getName());
		
		if (howMuch == MappingQuantity.DETAIL_ENTITY) 
		{
			// visible in client ?
			groupDoc.put(MdekKeysSecurity.DATE_OF_CREATION, group.getCreateTime());
			groupDoc.put(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION, group.getModTime());

			// map associations
			mapPermissionAddrs(group.getPermissionAddrs(), groupDoc, howMuch);
			mapPermissionObjs(group.getPermissionObjs(), groupDoc, howMuch);
			mapIdcUserPermissions(group.getIdcUserPermissions(), groupDoc);

			// map only with initial data ! call mapping method explicitly if more data wanted.
			beanToDocMapper.mapModUser(group.getModUuid(), groupDoc, MappingQuantity.INITIAL_ENTITY);
		}

		return groupDoc;
	}

	/**
	 * Transfer data of passed bean to passed doc.
	 * @return doc containing mapped data.
	 */
	public IngridDocument mapIdcUser(IdcUser user, IngridDocument userDoc,
			MappingQuantity howMuch) {
		if (user == null) {
			return userDoc;
		}

		// also ID, is needed when addr uuid is changed !!!
		userDoc.put(MdekKeysSecurity.IDC_USER_ID, user.getId());
		userDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, user.getAddrUuid());
		mapIdcUserGroups(user.getIdcUserGroups(), userDoc);
		userDoc.put(MdekKeysSecurity.IDC_ROLE, user.getIdcRole());
		userDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, user.getParentId());
		
		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY)
		{
			// child info
	    	boolean hasChild = (user.getIdcUsers().size() > 0) ? true : false;
	    	userDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);

			beanToDocMapper.mapT02Address(user.getAddressNode().getT02AddressWork(), userDoc, MappingQuantity.BASIC_ENTITY);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) 
		{
			// not visible in client
			userDoc.put(MdekKeysSecurity.DATE_OF_CREATION, user.getCreateTime());
			userDoc.put(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION, user.getModTime());

			// map only with initial data ! call mapping method explicitly if more data wanted.
			beanToDocMapper.mapModUser(user.getModUuid(), userDoc, MappingQuantity.INITIAL_ENTITY);
		}

		return userDoc;
	}

	/**
	 * Map given userGroup beans to document.
	 * @return doc with mapped userGroup ids
	 */
	public IngridDocument mapIdcUserGroups(Set<IdcUserGroup> inRefs, IngridDocument inDoc) {
		ArrayList<Long> refList = new ArrayList<Long>(inRefs.size());

		for (IdcUserGroup inRef : inRefs) {
			refList.add(inRef.getIdcGroupId());
		}

		inDoc.put(MdekKeysSecurity.IDC_GROUP_IDS, refList.toArray(new Long[refList.size()]));
		
		return inDoc;
	}

	/**
	 * Map given user beans to document. With or without CatAdmins ...
	 * @param inRefs list of user beans
	 * @param inDoc document to map to
	 * @param filterCatAdmin true=user with role CatalogAdmin aren't added, false=all users are added !
	 * @return doc with mapped users
	 */
	public IngridDocument mapIdcUserList(List<IdcUser> inRefs, IngridDocument inDoc, boolean filterCatAdmin) {
		if (inRefs == null) {
			inRefs = new ArrayList<IdcUser>(0); 
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(inRefs.size());
		for (IdcUser inRef : inRefs) {
			if (filterCatAdmin) {
				// skip Catalog Administrator
				if (IdcRole.CATALOG_ADMINISTRATOR.getDbValue().equals(inRef.getIdcRole())) {
					continue;
				}
			}

			IngridDocument refDoc = new IngridDocument();
			mapIdcUser(inRef, refDoc, MappingQuantity.DETAIL_ENTITY);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeysSecurity.IDC_USERS, refList);
		
		return inDoc;
	}

	public IngridDocument mapPermissionAddr(PermissionAddr inRef, IngridDocument inDoc,
			MappingQuantity howMuch) {
		if (inRef == null) {
			return inDoc;
		}

		inDoc.put(MdekKeysSecurity.UUID, inRef.getUuid());
		mapPermission(inRef.getPermission(), inDoc);

		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
			beanToDocMapper.mapT02Address(inRef.getAddressNode().getT02AddressWork(), inDoc, MappingQuantity.BASIC_ENTITY);
			
			// also child data, node could be shown in "permission tree"
	    	boolean hasChild = (inRef.getAddressNode().getAddressNodeChildren().size() > 0) ? true : false;
	    	inDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
		}

		return inDoc;
	}
	public IngridDocument mapPermissionObj(PermissionObj inRef, IngridDocument inDoc,
			MappingQuantity howMuch) {
		if (inRef == null) {
			return inDoc;
		}

		inDoc.put(MdekKeysSecurity.UUID, inRef.getUuid());
		mapPermission(inRef.getPermission(), inDoc);

		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
			beanToDocMapper.mapT01Object(inRef.getObjectNode().getT01ObjectWork(), inDoc, MappingQuantity.BASIC_ENTITY);
			
			// also child data, node could be shown in "permission tree"
	    	boolean hasChild = (inRef.getObjectNode().getObjectNodeChildren().size() > 0) ? true : false;
	    	inDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
		}

		return inDoc;
	}
	public IngridDocument mapIdcUserPermission(IdcUserPermission inRef, IngridDocument inDoc) {
		if (inRef == null) {
			return inDoc;
		}

		mapPermission(inRef.getPermission(), inDoc);

		return inDoc;
	}

	public IngridDocument mapPermission(Permission inRef, IngridDocument inDoc) {
		if (inRef == null) {
			return inDoc;
		}

		inDoc.put(MdekKeysSecurity.IDC_PERMISSION, permService.getPermIdClientByPermission(inRef));

		return inDoc;
	}

	public IngridDocument mapPermissionList(List<Permission> inRefs, IngridDocument inDoc) {
		if (inRefs == null) {
			inRefs = new ArrayList<Permission>(0); 
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(inRefs.size());
		for (Permission inRef : inRefs) {
			IngridDocument refDoc = new IngridDocument();
			mapPermission(inRef, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeysSecurity.IDC_PERMISSIONS, refList);
		
		return inDoc;
	}

	private IngridDocument mapPermissionAddrs(Set<PermissionAddr> inRefs, IngridDocument inDoc,
			MappingQuantity howMuch) {
		if (inRefs == null) {
			inRefs = new HashSet<PermissionAddr>(0); 
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(inRefs.size());
		for (PermissionAddr inRef : inRefs) {
			IngridDocument refDoc = new IngridDocument();
			mapPermissionAddr(inRef, refDoc, howMuch);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, refList);
		
		return inDoc;
	}
	private IngridDocument mapPermissionObjs(Set<PermissionObj> inRefs, IngridDocument inDoc,
			MappingQuantity howMuch) {
		if (inRefs == null) {
			inRefs = new HashSet<PermissionObj>(0); 
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(inRefs.size());
		for (PermissionObj inRef : inRefs) {
			IngridDocument refDoc = new IngridDocument();
			mapPermissionObj(inRef, refDoc, howMuch);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, refList);
		
		return inDoc;
	}
	private IngridDocument mapIdcUserPermissions(Set<IdcUserPermission> inRefs, IngridDocument inDoc) {
		if (inRefs == null) {
			inRefs = new HashSet<IdcUserPermission>(0); 
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(inRefs.size());
		for (IdcUserPermission inRef : inRefs) {
			IngridDocument refDoc = new IngridDocument();
			mapIdcUserPermission(inRef, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, refList);
		
		return inDoc;
	}
}
