package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;

/**
 * Business DAO operations related to the <tt>IdcUser</tt> entity.
 * 
 * @author Joachim
 */
public interface IIdcUserDao extends IGenericDao<IdcUser> {

	/**
	 * Get a IdcUser by it's addrUuid. The addrUuid is unique for all IdcUsers in this catalog.
	 * 
	 * @param addrUuid
	 * @return
	 */
	public IdcUser getIdcUserByAddrUuid(String addrUuid);

	/**
	 * Get the catalog administrator for this catalog.
	 * 
	 * @return
	 */
	public IdcUser getCatalogAdmin();
	
	/**
	 * Returns all users belonging to a group.
	 * 
	 * @param groupId
	 * @return
	 */
	public List<IdcUser> getIdcUsersByGroupId(Long groupId);

}
