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
	 * CHECKS ONLY WORK VERSION OF OBJECTS !!!
	 * Returns a list of maps. Map contains objUuid and the userUuid who currently owns 
	 * (modifies) the object. */
	List<Map> getGroupUsersWithObjectsNotInGivenState(String groupName, WorkState objWorkState);

	/** Get the users of the given group who currently have addresses in given work state.  
	 * CHECKS ONLY WORK VERSION OF ADDRESSES !!!
	 * Returns a list of maps. Map contains addrUuid and the userUuid who currently owns 
	 * (modifies) the object. */
	List<Map> getGroupUsersWithAddressesNotInGivenState(String groupName, WorkState addrWorkState);

	/** Get the users of the given group who currently are responsible for objects.
	 * CHECKS ONLY WORK VERSION OF OBJECTS !!!
	 * Returns a list of maps. Map contains entityUuid and the userUuid who currently is
	 * responsible for the entity. */
	List<Map> getGroupUsersResponsibleForObjects(String groupName);

	/** Get the users of the given group who currently are responsible for addresses.
	 * CHECKS ONLY WORK VERSION OF ADDRESSES !!!
	 * Returns a list of maps. Map contains entityUuid and the userUuid who currently is
	 * responsible for the entity. */
	List<Map> getGroupUsersResponsibleForAddresses(String groupName);

	/** Get ids of groups (of given user) containing the given user permission.  
	 * @param userUuid address uuid of user to get groups from
	 * @param permId id of user permission to search for in groups
	 * @return ids of groups or empty list
	 */
	List<Long> getGroupIdsContainingUserPermission(String userUuid, Long permId);

	/** Get ids of groups (of given user) containing the given object permission.  
	 * @param userUuid address uuid of user to get groups from
	 * @param permId id of permission on object
	 * @param objUuid uuid of object the permission belongs to
	 * @return ids of groups or empty list
	 */
	List<Long> getGroupIdsContainingObjectPermission(String userUuid, Long permId, String objUuid);

	/** Get ids of groups (of given user) containing the given address permission.  
	 * @param userUuid address uuid of user to get groups from
	 * @param permId id of permission on object
	 * @param addrUuid uuid of address the permission belongs to
	 * @return ids of groups or empty list
	 */
	List<Long> getGroupIdsContainingAddressPermission(String userUuid, Long permId, String addrUuid);
}
