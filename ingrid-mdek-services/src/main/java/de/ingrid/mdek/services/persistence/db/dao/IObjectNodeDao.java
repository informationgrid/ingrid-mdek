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

	/** Fetches sub objects of object with given id */
	List<ObjectNode> getSubObjects(String uuid);

	/** Fetches object with given uuid containing all detailed object data.
	 * @param uuid uuid of object to fetch
	 * @return bean containing data
	 */
	ObjectNode getObjDetails(String uuid);
}
