package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
		IdcGroup grp = (IdcGroup) session.createQuery("from IdcGroup grp " +
//			"left join fetch aNode.t02AddressWork aWork " +
//			"left join fetch aWork.t021Communications aComm " +

// TODO: FETCH ASSOCIATIONS

			"where grp.name = ?")
			.setString(0, name)
			.uniqueResult();

		return grp;
	}

	public IdcGroup getGroupDetails(Long groupId) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		IdcGroup grp = (IdcGroup) session.createQuery("from IdcGroup grp " +
//			"left join fetch aNode.t02AddressWork aWork " +
//			"left join fetch aWork.t021Communications aComm " +

// TODO: FETCH ASSOCIATIONS

			"where grp.id = ?")
			.setLong(0, groupId)
			.uniqueResult();

		return grp;
	}
}
