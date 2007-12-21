package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.model.T012ObjObj;
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
		ArrayList<T01Object> retList = new ArrayList<T01Object>();

		// only "strukturverweise"
		session.enableFilter("objObjTypeFilter").setParameter("type", new Integer(0));

		// fetch all at once (one select with outer joins)
		// NOTICE: we already fetch associated toT01Objects because Hibernate executes subselects
		// for every toT01Object ! Found no way to instruct hibernate to load
		// the toT01Object lazy (even lazy="proxy" in association doesn't help, see mapping file) !
		// so we FETCH ALL SUBOBJECTS IN ONE SELECT TO AVOID FURTHER SUBSELECTS !
		// -> 3 to 4 times faster than executing subselects !
		List<T01Object> objs = session.createQuery("from T01Object o " +
			"left join fetch o.t012ObjObjs oOs " +
			"left join fetch oOs.toT01Object o1 " +
			"where o.root = 1 " +
			"order by o.objName")
			.list();

		session.disableFilter("objObjTypeFilter");

		// NOTICE: upper query returns objects multiple times, filter them !
		for (T01Object o : objs) {
			if (!retList.contains(o)) {
				retList.add(o);
			}
		}
		return retList;
	}

	public List<T01Object> getSubObjects(String uuid) {
		Session session = getSession();
		ArrayList<T01Object> retList = new ArrayList<T01Object>();

		// only "strukturverweise"
		session.enableFilter("objObjTypeFilter").setParameter("type", new Integer(0));

		// fetch all at once (one select with outer joins)
		// NOTICE: we already fetch associated toT01Objects because Hibernate executes subselects
		// for every toT01Object ! Found no way to instruct hibernate to load
		// the toT01Object lazy (even lazy="proxy" in association doesn't help, see mapping file) !
		// so we FETCH ALL SUBOBJECTS IN ONE SELECT TO AVOID FURTHER SUBSELECTS !
		// -> 3 to 4 times faster than executing subselects !
		T01Object o = (T01Object) session.createQuery("from T01Object o " +
			"left join fetch o.t012ObjObjs oOs1 " +
			"left join fetch oOs1.toT01Object o1 " +
			"left join fetch o1.t012ObjObjs oOs2 " +
			"left join fetch oOs2.toT01Object o2 " +
			"where o.objUuid = ?")
		// order by doesn't work ! set in mapping file !
//			"order by oOs1.line")
			.setString(0, uuid)
			.uniqueResult();

		session.disableFilter("objObjTypeFilter");

		if (o != null) {
			Set<T012ObjObj> oOs = o.getT012ObjObjs();
			for (T012ObjObj oO : oOs) {
				retList.add(oO.getToT01Object());
			}			
		}

		return retList;
	}

	public T01Object getObjDetails(String uuid) {
		Session session = getSession();

		// enable filter ?
/*
		0 == Auskunft
		1 == Datenhalter
		2 == Datenverantwortlicher
		999 == Freier Eintrag
*/
//		session.enableFilter("objAdrTypeFilter").setParameter("type", new Integer(0));

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createQuery("from T01Object o " +
			"left join fetch o.t012ObjAdrs oAs " +
			"left join fetch oAs.t02Address a " +
			"left join fetch a.t021Communications " +
			"where o.objUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

//		session.disableFilter("objAdrTypeFilter");
		
		return o;
	}
}
