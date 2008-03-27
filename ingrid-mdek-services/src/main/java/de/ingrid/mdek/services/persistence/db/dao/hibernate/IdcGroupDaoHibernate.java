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

	public List<IdcGroup> getGroups() {
		Session session = getSession();
		String query = "select group from IdcGroup group " +
			"order by group.name";
		
		List<IdcGroup> groups = session.createQuery(query).list();

		return groups;
	}
}
