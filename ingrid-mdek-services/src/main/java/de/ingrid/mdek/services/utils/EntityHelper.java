package de.ingrid.mdek.services.utils;

import java.util.UUID;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * Singleton encapsulating Utility Methods concerning IGC Entities (Objects, Addresses ...).
 * 
 * @author Martin
 */
public class EntityHelper {

	private static final Logger LOG = Logger.getLogger(EntityHelper.class);

	private static EntityHelper myInstance;

	/** Get The Singleton */
	public static synchronized EntityHelper getInstance() {
		if (myInstance == null) {
	        myInstance = new EntityHelper();
	      }
		return myInstance;
	}

	private EntityHelper() {}
	
	public String generateUuid() {
		UUID uuid = java.util.UUID.randomUUID();
		StringBuffer idcUuid = new StringBuffer(uuid.toString().toUpperCase());
		while (idcUuid.length() < 36) {
			idcUuid.append("0");
		}

		return idcUuid.toString();
	}

	/** Extract UUID of node given as IEntity. Pass Type of Entity. */
	public static String getUuidFromNode(IdcEntityType whichType, IEntity nodeEntity) {
		if (whichType == IdcEntityType.OBJECT) {
			return ((ObjectNode)nodeEntity).getObjUuid();
		} else if (whichType == IdcEntityType.ADDRESS) {
			return ((AddressNode)nodeEntity).getAddrUuid();
		}
		
		return null;
	}

	/** Extract Parent UUID of node given as IEntity. Pass Type of Entity. */
	public static String getParentUuidFromNode(IdcEntityType whichType, IEntity nodeEntity) {
		if (whichType == IdcEntityType.OBJECT) {
			return ((ObjectNode)nodeEntity).getFkObjUuid();
		} else if (whichType == IdcEntityType.ADDRESS) {
			return ((AddressNode)nodeEntity).getFkAddrUuid();
		}
		
		return null;
	}
}
