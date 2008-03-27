package de.ingrid.mdek.services.persistence.db.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents
 * concerning USER MANAGEMENT.
 */
public class BeanToDocMapperUser implements IMapper {

	private static final Logger LOG = Logger.getLogger(BeanToDocMapperUser.class);

	private static BeanToDocMapperUser myInstance;

	/** Get The Singleton */
	public static synchronized BeanToDocMapperUser getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapperUser();
	      }
		return myInstance;
	}

	private BeanToDocMapperUser() {}

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
		groupDoc.put(MdekKeys.ID, group.getId());
		groupDoc.put(MdekKeys.NAME, group.getName());
		
		if (howMuch == MappingQuantity.DETAIL_ENTITY) 
		{
			groupDoc.put(MdekKeys.DATE_OF_CREATION, group.getCreateTime());
			groupDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, group.getModTime());
			groupDoc.put(MdekKeys.MOD_UUID, group.getModUuid());

			// map associations
			// TODO: map associations in IdcGroup
//			mapPermissionAddrs(group.getPermissionAddrs(), groupDoc);
//			mapPermissionObjs(group.getPermissionObjs(), groupDoc);
		}

		return groupDoc;
	}
}
