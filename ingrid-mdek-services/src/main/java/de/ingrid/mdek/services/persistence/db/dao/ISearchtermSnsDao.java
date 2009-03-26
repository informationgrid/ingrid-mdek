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

	/** Load SearchtermSns with given snsId. If not found create AND save it !
	 * @param snsId NEVER NULL ! used for loading term !
	 * @param gemetId GEMET ID of term. Ignored for loading term, but set when creating term !
	 * @return persisted SearchtermSns (with Id)
	 */
	SearchtermSns loadOrCreate(String snsId, String gemetId);
}
