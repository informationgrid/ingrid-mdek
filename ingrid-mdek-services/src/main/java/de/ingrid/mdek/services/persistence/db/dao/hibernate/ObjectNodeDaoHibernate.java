package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.utils.IngridDocument;

/**
 * Hibernate-specific implementation of the <tt>IObjectNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class ObjectNodeDaoHibernate
	extends GenericHibernateDao<ObjectNode>
	implements  IObjectNodeDao {

	private static final Logger LOG = Logger.getLogger(ObjectNodeDaoHibernate.class);

    public ObjectNodeDaoHibernate(SessionFactory factory) {
        super(factory, ObjectNode.class);
    }

	public ObjectNode loadByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}

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

	public List<ObjectNode> getSubObjects(String parentUuid, boolean fetchObjectLevel) {
		Session session = getSession();
		ArrayList<ObjectNode> retList = new ArrayList<ObjectNode>();

		String q = "from ObjectNode oNd ";
		if (fetchObjectLevel) {
			q += "left join fetch oNd.t01ObjectWork o " +
				 "left join fetch oNd.objectNodeChildren oChildren ";
		}
		q += "where oNd.fkObjUuid = ? ";
		if (fetchObjectLevel) {
			q += "order by o.objName"; 
		}
		
		List<ObjectNode> oNs = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		// NOTICE: upper query returns objects multiple times, filter them !
		for (ObjectNode oN : oNs) {
			if (!retList.contains(oN)) {
				retList.add(oN);
			}
		}
		return retList;
	}

	public List<String> getSubObjectUuids(String parentUuid) {
		Session session = getSession();

		List<String> childUuids = session.createQuery("select oNd.objUuid " +
				"from ObjectNode oNd " +
				"where oNd.fkObjUuid = ?")
				.setString(0, parentUuid)
				.list();
		
		return childUuids;
	}

	public int countSubObjects(String parentUuid) {
		int totalNum = 0;

		Stack<String> uuidStack = new Stack<String>();
		uuidStack.push(parentUuid);

		while (!uuidStack.isEmpty()) {
			String uuid = uuidStack.pop();
			if (!uuid.equals(parentUuid)) {
				totalNum++;
			}
			List<String> subUuids = getSubObjectUuids(uuid);
			for (String subUuid : subUuids) {
				uuidStack.push(subUuid);
			}
		}
		
		return totalNum;
	}

	public boolean isSubNode(String uuidToCheck, String uuidParent) {
		boolean isSubNode = false;

		List<String> path = getObjectPath(uuidToCheck);
		
		if (path != null) {
			if (path.contains(uuidParent)) {
				isSubNode = true;
			}
		}
		
		return isSubNode;
	}
	
	public ObjectNode getParent(String uuid) {
		ObjectNode parentNode = null;
		ObjectNode oN = loadByUuid(uuid);
		if (oN != null && oN.getFkObjUuid() != null) {
			parentNode = loadByUuid(oN.getFkObjUuid());
		}
		
		return parentNode;
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

// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !

		// referenced addresses
//			"left join fetch oWork.t012ObjAdrs objAdr " +
//			"left join fetch objAdr.addressNode aNode " +
//			"left join fetch aNode.t02AddressWork aWork " +
//			"left join fetch aWork.t021Communications aComm " +
		// spatial references 
//			"left join fetch oWork.spatialReferences spatRef " +
//			"left join fetch spatRef.spatialRefValue spatialRefVal " +
//			"left join fetch spatialRefVal.spatialRefSns " +
		// url refs 
//			"left join fetch oWork.t017UrlRefs urlRef " +
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

	public List<String> getObjectPath(String uuid) {
		ArrayList<String> uuidList = new ArrayList<String>();
		while(uuid != null) {
			ObjectNode oN = loadByUuid(uuid);
			if (oN == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
			}
			uuidList.add(0, uuid);
			uuid = oN.getFkObjUuid();
		}

		return uuidList;
	}

	public List<IngridDocument> getObjectThesaurusTerms(String uuid) {
		Session session = getSession();
		ArrayList<IngridDocument> retList = new ArrayList<IngridDocument>();

		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery("from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork oWork " +
			"left join fetch oWork.searchtermObjs termObj " +
			"left join fetch termObj.searchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +
			"where oNode.objUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

		if (oN == null) {
			throw new MdekException(MdekError.UUID_NOT_FOUND);
		}

		// map ALL searchterms via mapper
		Set<SearchtermObj> searchtermObjs = oN.getT01ObjectWork().getSearchtermObjs();
		BeanToDocMapper beanToDocMapper = BeanToDocMapper.getInstance();
		IngridDocument tmpDoc = new IngridDocument();
		beanToDocMapper.mapSearchtermObjs(searchtermObjs, tmpDoc);

		// then filter: get thesaurus terms
		ArrayList<IngridDocument> termDocs =
			(ArrayList<IngridDocument>) tmpDoc.get(MdekKeys.SUBJECT_TERMS);
		String THESAURUS_TYPE = MdekUtils.SearchtermType.THESAURUS.getDbValue();
		for (IngridDocument termDoc : termDocs) {
			if (THESAURUS_TYPE.equals(termDoc.getString(MdekKeys.TERM_TYPE))) {
				retList.add(termDoc);
			}
		}
		
		return retList;
	}
}
