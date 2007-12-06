package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

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

	/** Fetches object with given id AND sub objects (2 levels) IN ONE SELECT */
	T01Object getObjectWithSubObjects(String uuid);

	/** Fetches object with given id AND connected addresses IN ONE SELECT */
	T01Object getObjWithAddresses(String uuid);
}
