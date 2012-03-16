package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.services.persistence.db.IEntity;
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
	
	/** returns all entries of the given list (ordered by LINE(!!!), entryId).
	 * Pass NULL for language if all languages */
	List<SysList> getSysList(int lstId, String language);
	
	/**
	 * Get all free entries of entities where given syslist is used.
	 * @param sysLst specifies syslist and according entity(ies) where syslist is used ! 
	 * @return distinct free entries (strings)
	 */
	List<String> getFreeListEntries(MdekSysList sysLst);

	/**
	 * Get entities containing given syslist free entry.
	 * @param sysLst specifies syslist and according entity(ies) where syslist is used ! 
	 * @param freeEntry name of free syslist entry 
	 * @return all entities containing given free entry
	 */
	List<IEntity> getEntitiesOfFreeListEntry(MdekSysList sysLst, String freeEntry);
}
