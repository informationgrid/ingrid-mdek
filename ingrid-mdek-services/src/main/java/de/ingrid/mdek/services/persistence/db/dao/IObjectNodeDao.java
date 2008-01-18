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

	/** Fetches sub object nodes of parent with given uuid.
	 * @param parentUuid uuid of parent
	 * @param fetchObjectLevel also fetch T01Object level encapsulated by ObjectNode ?
	 * @return
	 */
	List<ObjectNode> getSubObjects(String parentUuid, boolean fetchObjectLevel);

	/** Get sub uuids of parent with given uuid (only next level) */
	List<String> getSubObjectUuids(String parentUuid);

	/** Get Path (list of uuids) of object in tree starting at root. */
	List<String> getObjectPath(String uuid);

	/** Checks whether the given uuid is ANYWHERE below the given parent uuid. */
	boolean isSubNode(String uuidToCheck, String uuidParent);

	/** Fetches object with given uuid containing all detailed object data. */
	ObjectNode getObjDetails(String uuid);

	/** Fetch Objects which reference the object with the passed uuid */
	List<ObjectNode> getObjectReferencesFrom(String uuid);
}
