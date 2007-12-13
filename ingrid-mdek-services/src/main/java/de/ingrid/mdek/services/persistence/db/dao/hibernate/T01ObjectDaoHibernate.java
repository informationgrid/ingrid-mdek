package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * Hibernate-specific implementation of the <tt>IT01ObjectDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T01ObjectDaoHibernate
	extends GenericHibernateDao<T01Object>
	implements  IT01ObjectDao {

    public T01ObjectDaoHibernate(SessionFactory factory) {
        super(factory, T01Object.class);
    }

	public List<T01Object> getTopObjects() {
		Session session = getSession();

		List objs = session.createQuery("from T01Object obj " +
			"where obj.root = 1")
			.list();
		
		return objs;
	}

	public Set<T01Object> getSubObjects(String uuid) {
		Session session = getSession();

		// enable filter -> fetch only hierarchical relations
		session.enableFilter("relationTypeFilter").setParameter("relationType", new Integer(0));

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createQuery("from T01Object obj " +
			"left join fetch obj.t012ObjObjs child " +
			"left join fetch child.t012ObjObjs " +
			"where obj.id = ?")
			.setString(0, uuid)
			.uniqueResult();

		session.disableFilter("relationTypeFilter");

		Set<T01Object> objs = o.getT012ObjObjs();

		return objs;
	}

	public T01Object getObjDetails(String uuid) {
		Session session = getSession();

		// enable filter -> fetch only hierarchical relations
/*
		0 == Auskunft
		1 == Datenhalter
		2 == Datenverantwortlicher
		999 == Freier Eintrag
*/
		session.enableFilter("relationTypeFilter").setParameter("relationType", new Integer(0));

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createQuery("from T01Object obj " +
			"left join fetch obj.t012ObjAdrs adrs " +
			"left join fetch adrs.t021Communications " +
			"where obj.id = ?")
			.setString(0, uuid)
			.uniqueResult();

		session.disableFilter("relationTypeFilter");
		
		return o;
	}
}
