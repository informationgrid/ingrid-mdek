package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

/**
 * Hibernate-specific implementation of the <tt>IIdcGroupDao</tt> non-CRUD
 * (Create, Read, Update, Delete) data access object.
 */
public class IdcGroupDaoHibernate
	extends GenericHibernateDao<IdcGroup>
	implements IIdcGroupDao {

	public IdcGroupDaoHibernate(SessionFactory factory) {
		super(factory, IdcGroup.class);
	}

	public IdcGroup loadByName(String name) {
		if (name == null) {
			return null;
		}

		Session session = getSession();

		IdcGroup grp = (IdcGroup) session.createQuery("from IdcGroup grp " +
			"where grp.name = ?")
			.setString(0, name)
			.uniqueResult();
		
		return grp;
	}

	public List<IdcGroup> getGroups() {
		Session session = getSession();
		String query = "select group from IdcGroup group " +
			"order by group.name";
		
		List<IdcGroup> groups = session.createQuery(query).list();

		return groups;
	}

	public IdcGroup getGroupDetails(String name) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		IdcGroup grp = (IdcGroup) session.createQuery(
			createGroupDetailsQueryString() +
			"where grp.name = ?")
			.setString(0, name)
			.uniqueResult();

		return grp;
	}

	public IdcGroup getGroupDetails(Long groupId) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		IdcGroup grp = (IdcGroup) session.createQuery(
			createGroupDetailsQueryString() +
			"where grp.id = ?")
			.setLong(0, groupId)
			.uniqueResult();

		return grp;
	}
	
	private String createGroupDetailsQueryString() {
		String qString = "from IdcGroup grp " +
			"left join fetch grp.permissionAddrs permAddr " +
			"left join fetch permAddr.permission permA " +
			"left join fetch permAddr.addressNode aNode " +
			"left join fetch aNode.t02AddressWork a " +
			"left join fetch grp.permissionObjs permObj " +
			"left join fetch permObj.permission permO " +
			"left join fetch permObj.objectNode oNode " +
			"left join fetch oNode.t01ObjectWork o " +
			"left join fetch grp.idcUserPermissions permUser " +
			"left join fetch permUser.permission permU ";
		
		return qString;
	}

	public List<Map> getGroupUsersWithObjectsInGivenState(String groupName,
			WorkState objWorkState) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"o.objUuid as " + KEY_ENTITY_UUID + 
				", o.modUuid as " + KEY_USER_UUID + 
			") " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork o " +
			"where " +
			"o.workState = '" + objWorkState.getDbValue() + "' " +
			"and o.modUuid in (" +
				"select u.addrUuid from IdcUser u inner join u.idcGroup grp " +
				"where grp.name='" + groupName + "'" +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}

	public List<Map> getGroupUsersWithAddressesInGivenState(String groupName,
			WorkState addrWorkState) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"a.adrUuid as " + KEY_ENTITY_UUID + 
				", a.modUuid as " + KEY_USER_UUID + 
			") " +
			"from AddressNode aNode " +
			"inner join aNode.t02AddressWork a " +
			"where " +
			"a.workState = '" + addrWorkState.getDbValue() + "' " +
			"and a.modUuid in (" +
				"select u.addrUuid from IdcUser u inner join u.idcGroup grp " +
				"where grp.name='" + groupName + "'" +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}
}
