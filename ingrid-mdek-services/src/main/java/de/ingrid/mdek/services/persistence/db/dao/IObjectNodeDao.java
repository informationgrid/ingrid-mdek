package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;



/**
 * Business DAO operations related to the <tt>ObjectNode</tt> entity.
 * 
 * @author Martin
 */
public interface IObjectNodeDao
	extends IGenericDao<ObjectNode> {
	
	/** Load object with given uuid. Returns null if not found.	 */
	ObjectNode loadByUuid(String uuid);

	List<ObjectNode> getTopObjects();

	/** Fetches sub objects of parent with given uuid (only next level) */
	List<ObjectNode> getSubObjects(String parentUuid);

	/** Get sub uuids of parent with given uuid (only next level) */
	List<String> getSubObjectUuids(String parentUuid);

	/** Checks whether the given uuid is ANYWHERE below the given parent uuid. */
	boolean isSubNode(String uuidToCheck, String uuidParent);

	/** Fetches object with given uuid containing all detailed object data. */
	ObjectNode getObjDetails(String uuid);

	/** Fetch Objects which reference the object with the passed uuid */
	List<ObjectNode> getObjectReferencesFrom(String uuid);
}
