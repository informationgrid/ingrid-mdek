package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.model.T012ObjObj;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.IMapper.T012ObjObjRelationType;

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
		session.enableFilter("objObjTypeFilter").
			setParameter("type", T012ObjObjRelationType.STRUKTURBAUM.getDbValue());

		List<T01Object> objs = session.createQuery("from T01Object o " +
			"left join fetch o.t012ObjObjs oO " +
			// request eager fetching of subobject itself (cause hibernate will fetch that one anyway due to property-ref assoziation :(
			"left join fetch oO.toT01Object oTo " +
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
		session.enableFilter("objObjTypeFilter").
			setParameter("type", T012ObjObjRelationType.STRUKTURBAUM.getDbValue());

		List<T012ObjObj> oOs = session.createQuery("from T012ObjObj oO " +
				"left join fetch oO.toT01Object oTo " +
				// don't fetch next level, causes duplicate subobjects ("cartesian product")
//				"left join fetch oTo.t012ObjObjs oO2 " +
				"where oO.objectFromUuid = ?" +
				"order by oTo.objName")
				.setString(0, uuid)
				.list();

		session.disableFilter("objObjTypeFilter");

		for (T012ObjObj oO : oOs) {
			retList.add(oO.getToT01Object());
		}			

		return retList;
	}

	public T01Object getObjDetails(String uuid, 
		T012ObjObjRelationType objObjTypeFilter) {
		Session session = getSession();

		// enable object object relations filter !
		if (objObjTypeFilter == T012ObjObjRelationType.ALLE) {
			session.disableFilter("objObjTypeFilter");
		} else {
			session.enableFilter("objObjTypeFilter").setParameter("type", objObjTypeFilter.getDbValue());
		}

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createQuery("from T01Object o " +
			"left join fetch o.t012ObjObjs oOs " +
//			"left join fetch oOs.fromT01Object oFrom " +
			"left join fetch oOs.toT01Object oTo " +
			"left join fetch o.t012ObjAdrs oAs " +
			"left join fetch oAs.t02Address a " +
			"left join fetch a.t021Communications " +
			"where o.objUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

		session.disableFilter("objObjTypeFilter");
		
		return o;
	}
}
