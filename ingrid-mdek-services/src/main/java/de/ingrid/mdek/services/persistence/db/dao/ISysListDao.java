package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;



/**
 * Business DAO operations related to the <tt>SysList</tt> entity.
 * 
 * @author Martin
 */
public interface ISysListDao
	extends IGenericDao<SysList> {

	/** returns all entries of the given list ordered by entry id */
	List<SysList> getSysList(int lstId, String language);

}
