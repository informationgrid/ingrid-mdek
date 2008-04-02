package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

/**
 * Business DAO operations related to the <tt>IdcGroup</tt> entity.
 */
public interface IIdcGroupDao extends IGenericDao<IdcGroup> {

	/** Load group with given name. Returns null if not found.	 */
	IdcGroup loadByName(String name);

	/** Get ALL groups. */
	List<IdcGroup> getGroups();

	/** Fetches group with given name containing all detailed group data. */
	IdcGroup getGroupDetails(String name);


}
