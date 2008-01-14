package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * Hibernate-specific implementation of the <tt>IObjectNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class ObjectNodeDaoHibernate
	extends GenericHibernateDao<ObjectNode>
	implements  IObjectNodeDao {

    public ObjectNodeDaoHibernate(SessionFactory factory) {
        super(factory, T01Object.class);
    }

	public ObjectNode loadByUuid(String uuid) {
		Session session = getSession();

		ObjectNode oN = (ObjectNode) session.createQuery("from ObjectNode oNode " +
			"where oNode.objUuid = ?")
			.setString(0, uuid)
			.uniqueResult();
		
		return oN;
	}

	public List<ObjectNode> getTopObjects() {
		Session session = getSession();
		ArrayList<ObjectNode> retList = new ArrayList<ObjectNode>();

		List<ObjectNode> oNs = session.createQuery("from ObjectNode oNd " +
				"left join fetch oNd.t01ObjectWork o " +
				"left join fetch oNd.objectNodeChildren oChildren " +
				"where oNd.fkObjUuid is null " +
				"order by o.objName")
				.list();

		// NOTICE: upper query returns objects multiple times, filter them !
		for (ObjectNode oN : oNs) {
			if (!retList.contains(oN)) {
				retList.add(oN);
			}
		}
		return retList;
	}

	public List<ObjectNode> getSubObjects(String uuid) {
		Session session = getSession();
		ArrayList<ObjectNode> retList = new ArrayList<ObjectNode>();

		List<ObjectNode> oNs = session.createQuery("from ObjectNode oNd " +
				"left join fetch oNd.t01ObjectWork o " +
				"left join fetch oNd.objectNodeChildren oChildren " +
				"where oNd.fkObjUuid = ? " +
				"order by o.objName")
				.setString(0, uuid)
				.list();

		// NOTICE: upper query returns objects multiple times, filter them !
		for (ObjectNode oN : oNs) {
			if (!retList.contains(oN)) {
				retList.add(oN);
			}
		}
		return retList;
	}

	public ObjectNode getObjDetails(String uuid) {
		Session session = getSession();

		// enable address filter ?
//		session.enableFilter("t012ObjAdrFilter").setParameter("type", 1);

		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery("from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork oWork " +
		// referenced objects (to) 
			"left join fetch oWork.objectReferences oRef " +
			"left join fetch oRef.objectNode oRefNode " +
			"left join fetch oRefNode.t01ObjectWork oRefObj " +
		// referenced addresses
			"left join fetch oWork.t012ObjAdrs objAdr " +
			"left join fetch objAdr.addressNode aNode " +
			"left join fetch aNode.t02AddressWork aWork " +
			"left join fetch aWork.t021Communications aComm " +
		// spatial references 
			"left join fetch oWork.spatialReferences spatRef " +
			"left join fetch spatRef.spatialRefValue spatialRefVal " +
			"left join fetch spatialRefVal.spatialRefSns " +
			"where oNode.objUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

//		session.disableFilter("t012ObjAdrFilter");

		return oN;
	}

	public List<ObjectNode> getObjectReferencesFrom(String uuid) {
		Session session = getSession();
		ArrayList<ObjectNode> retList = new ArrayList<ObjectNode>();

		// fetch all at once (one select with outer joins)
		List<ObjectNode> oNs = session.createQuery("from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork oWork " +
			"left join fetch oWork.objectReferences oRef " +
			"where oRef.objToUuid = ?")
			.setString(0, uuid)
			.list();

		// NOTICE: upper query returns objects multiple times, filter them !
		for (ObjectNode oN : oNs) {
			if (!retList.contains(oN)) {
				retList.add(oN);
			}
		}
		return retList;
	}
}
