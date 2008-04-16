package de.ingrid.mdek.services.persistence.db.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents
 * concerning SECURITY / USER MANAGEMENT.
 */
public class BeanToDocMapperSecurity implements IMapper {

	private static final Logger LOG = Logger.getLogger(BeanToDocMapperSecurity.class);

	private static BeanToDocMapperSecurity myInstance;

	/** Get The Singleton */
	public static synchronized BeanToDocMapperSecurity getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapperSecurity();
	      }
		return myInstance;
	}

	private BeanToDocMapperSecurity() {}

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
			groupDoc.put(MdekKeysSecurity.MOD_UUID, group.getModUuid());

			// map associations
			// TODO: map associations in IdcGroup
//			mapPermissionAddrs(group.getPermissionAddrs(), groupDoc);
//			mapPermissionObjs(group.getPermissionObjs(), groupDoc);
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
		
		if (howMuch == MappingQuantity.DETAIL_ENTITY) 
		{
			userDoc.put(MdekKeysSecurity.DATE_OF_CREATION, user.getCreateTime());
			userDoc.put(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION, user.getModTime());
			userDoc.put(MdekKeysSecurity.MOD_UUID, user.getModUuid());
		}

		return userDoc;
	}

}
