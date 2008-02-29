package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;



/**
 * Business DAO operations related to the <tt>SearchtermValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermValueDao
	extends IGenericDao<SearchtermValue> {

	/** Load SearchtermValue according to given values. If not found create AND save it !
	 * @param type
	 * @param term
	 * @param termSns according bean (or null)
	 * @param entityId connected to this entity (object or address)
	 * @param entityType type of entity (object or address)
	 * @return persisted SearchtermValue (with Id)
	 */
	SearchtermValue loadOrCreate(String type, String term, SearchtermSns termSns,
		Long entityId, IdcEntityType entityType);
}
