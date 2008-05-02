package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
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
		groupDoc.put(MdekKeysSecurity.IDC_GROUP_ID, group.getId());
		groupDoc.put(MdekKeysSecurity.NAME, group.getName());
		
		if (howMuch == MappingQuantity.DETAIL_ENTITY) 
		{
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
		userDoc.put(MdekKeysSecurity.IDC_GROUP_ID, user.getIdcGroupId());
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

	public IngridDocument mapPermissionAddr(PermissionAddr inRef, IngridDocument inDoc,
			MappingQuantity howMuch) {
		if (inRef == null) {
			return inDoc;
		}

		inDoc.put(MdekKeysSecurity.UUID, inRef.getUuid());
		mapPermission(inRef.getPermission(), inDoc);

		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
			beanToDocMapper.mapT02Address(inRef.getAddressNode().getT02AddressWork(), inDoc, MappingQuantity.BASIC_ENTITY);
			
			// also child data node could be shown in "permission tree"
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
			
			// also child data node could be shown in "permission tree"
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
