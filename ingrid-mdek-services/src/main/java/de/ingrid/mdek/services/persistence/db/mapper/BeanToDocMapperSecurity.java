package de.ingrid.mdek.services.persistence.db.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
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

		// also ID, just to track ID in test suite !
		groupDoc.put(MdekKeysSecurity.ID, group.getId());
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
}
