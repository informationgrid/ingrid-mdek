package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;



/**
 * Business DAO operations related to the <tt>T01Object</tt> entity.
 * 
 * @author Martin
 */
public interface IT01ObjectDao
	extends IGenericDao<T01Object> {
	
	List<T01Object> getTopObjects();

	/** Fetches sub objects of object with given id */
	Set<T01Object> getSubObjects(String uuid);

	/** Fetches object with given id containing all detailed object data. */
	T01Object getObjDetails(String uuid);
}
