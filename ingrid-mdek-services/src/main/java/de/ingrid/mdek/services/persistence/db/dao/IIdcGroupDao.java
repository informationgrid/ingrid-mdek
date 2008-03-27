package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

/**
 * Business DAO operations related to the <tt>IdcGroup</tt> entity.
 */
public interface IIdcGroupDao extends IGenericDao<IdcGroup> {

	/** Get ALL groups. */
	List<IdcGroup> getGroups();
}
