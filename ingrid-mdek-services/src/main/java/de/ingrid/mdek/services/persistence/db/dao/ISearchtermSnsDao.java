package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;



/**
 * Business DAO operations related to the <tt>SearchtermSns</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermSnsDao
	extends IGenericDao<SearchtermSns> {

	/** Load SearchtermSns with given snsId. Returns null if not found.	 */
	SearchtermSns load(String snsId);

	/** Load SearchtermSns with given snsId. If not found create (including gemtId) AND save it ! */
	SearchtermSns loadOrCreate(String snsId, String gemetId);
}
