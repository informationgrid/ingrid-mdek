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

	/** Get a IdcUser by it's addrUuid. The addrUuid is unique for all IdcUsers in this catalog. */
	IdcUser getIdcUserByAddrUuid(String addrUuid);

	/** Get the catalog administrator for this catalog. */
	IdcUser getCatalogAdmin();
	
	/** Returns all users belonging to a group defined by groupId. */
	List<IdcUser> getIdcUsersByGroupId(Long groupId);

	/** Returns all users belonging to a group defined by groupName. */
	List<IdcUser> getIdcUsersByGroupName(String groupName);

	/** Returns all subusers of user with given userId. */
	List<IdcUser> getSubUsers(Long parentIdcUserId);
}
