package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.IMapper.T012ObjObjRelationType;



/**
 * Business DAO operations related to the <tt>T01Object</tt> entity.
 * 
 * @author Martin
 */
public interface IT01ObjectDao
	extends IGenericDao<T01Object> {
	
	/** Load object with given uuid. Returns null if not found.	 */
	T01Object loadByUuid(String uuid);

	List<T01Object> getTopObjects();

	/** Fetches sub objects of object with given id */
	List<T01Object> getSubObjects(String uuid);

	/** Fetches object with given uuid containing all detailed object data.
	 * @param uuid uuid of object to fetch
	 * @param objObjTypeFilter which types of obj obj relations to fetch
	 * @return obj bean containing data
	 */
	T01Object getObjDetails(String uuid, T012ObjObjRelationType objObjTypeFilter);
}
