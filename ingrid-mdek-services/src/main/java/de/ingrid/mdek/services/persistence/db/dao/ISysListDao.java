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

	/** returns all syslist IDs ordered by id */
	List<Integer> getSysListIds();
	
	/** returns all entries of the given list ordered by entry id */
	List<SysList> getSysList(int lstId, String language);
	
	/**
	 * Get a specific entry. DON'T PASS NULL VALUES !
	 * @param lstId id of list -> NOT NULL.
	 * @param entryId id of list entry -> NOT NULL
	 * @param language language of list/entry -> NOT NULL 
	 * @return the list entry or null if not found !
	 */
	SysList getSysListEntry(int lstId, int entryId, String language);
}
