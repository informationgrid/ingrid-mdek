package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

/**
 * Business DAO operations related to the <tt>IdcGroup</tt> entity.
 */
public interface IIdcGroupDao extends IGenericDao<IdcGroup> {

	/** Key used for uuid of object/address when delivering data in map	 */ 
	static String KEY_ENTITY_UUID = "ENTITY_UUID";
	/** Key used for uuid of user when delivering data in map	 */ 
	static String KEY_USER_UUID = "USER_UUID";

	/** Load group with given name. Returns null if not found.	 */
	IdcGroup loadByName(String name);

	/** Get ALL groups. */
	List<IdcGroup> getGroups();

	/** Get group with given name containing all prefetched group data. */
	IdcGroup getGroupDetails(String name);
	
	/** Get group with given id containing all prefetched group data. */
	IdcGroup getGroupDetails(Long id);

	/** Get the users of the given group who currently have objects in given work state.  
	 * Returns a list of maps. Map contains objUuid and the userUuid who currently owns 
	 * (modifies) the object. */
	List<Map> getGroupUsersWithObjectsInGivenState(String groupName, WorkState objWorkState);

	/** Get the users of the given group who currently have addresses in given work state.  
	 * Returns a list of maps. Map contains addrUuid and the userUuid who currently owns 
	 * (modifies) the object. */
	List<Map> getGroupUsersWithAddressesInGivenState(String groupName, WorkState addrWorkState);
}
