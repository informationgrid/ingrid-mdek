package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
import de.ingrid.utils.IngridDocument;

/**
 * Hibernate-specific implementation of the <tt>IObjectNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class ObjectNodeDaoHibernate
	extends GenericHibernateDao<ObjectNode>
	implements  IObjectNodeDao, IFullIndexAccess {

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
		List<ObjectNode> oNodes = session.createQuery(
				"select distinct oNode from ObjectNode oNode " +
				"left join fetch oNode.t01ObjectWork o " +
				"left join fetch oNode.objectNodeChildren oChildren " +
				"where oNode.fkObjUuid is null " +
				"order by o.objName")
				.list();

		return oNodes;
	}

	public List<ObjectNode> getSubObjects(String parentUuid, boolean fetchObjectLevel) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		if (fetchObjectLevel) {
			q += "left join fetch oNode.t01ObjectWork o " +
				 "left join fetch oNode.objectNodeChildren oChildren ";
		}
		q += "where oNode.fkObjUuid = ? ";
		if (fetchObjectLevel) {
			q += "order by o.objName"; 
		}
		
		List<ObjectNode> oNodes = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		return oNodes;
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

	public List<ObjectNode>[] getObjectReferencesFrom(String uuid) {
		Session session = getSession();

		// first select all references from working copies (node ids)
		// NOTICE: working copy == published one if not "in Bearbeitung" !
		List<Long> nodeIdsWork = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectWork oWork " +
				"left join oWork.objectReferences oRef " +
				"where oRef.objToUuid = ?")
				.setString(0, uuid)
				.list();

		// then select all references from published ones
		List<Long> nodeIdsPub = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectPublished oPub " +
				"left join oPub.objectReferences oRef " +
				"where oRef.objToUuid = ?")
				.setString(0, uuid)
				.list();

		// then remove all published references also contained in working references.
		// we get the ones only in published version, meaning they were deleted in the
		// working copies !
		List<Long> nodeIdsPubOnly = new ArrayList<Long>();
		for (Long idPub : nodeIdsPub) {
			if (!nodeIdsWork.contains(idPub)) {
				nodeIdsPubOnly.add(idPub);
			}
		}
		
		// fetch all "nodes with work references"
		List<ObjectNode> nodesWork = new ArrayList<ObjectNode>();
		if (nodeIdsWork.size() > 0) {
			nodesWork = session.createQuery(
					"select distinct oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectWork oWork " +
					"where oNode.id in (:idList)")
					.setParameterList("idList", nodeIdsWork)
					.list();			
		}

		// fetch all "nodes with only publish references"
		List<ObjectNode> nodesPubOnly = new ArrayList<ObjectNode>();
		if (nodeIdsPubOnly.size() > 0) {
			nodesPubOnly = session.createQuery(
					"select distinct oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectPublished oPub " +
					"where oNode.id in (:idList)")
					.setParameterList("idList", nodeIdsPubOnly)
					.list();			
		}
		
		List<ObjectNode>[] retObjects = new List[] {
			nodesPubOnly,
			nodesWork
		};

		return retObjects;
	}

	public List<String> getObjectPath(String uuid) {
		ArrayList<String> uuidList = new ArrayList<String>();
		while(uuid != null) {
			ObjectNode oN = loadByUuid(uuid);
			if (oN == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			uuidList.add(0, uuid);
			uuid = oN.getFkObjUuid();
		}

		return uuidList;
	}

	public long queryObjectsThesaurusTermTotalNum(String termSnsId) {

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<ObjectNode> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return retList;
		}

		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
	}
	
	/**
	 * Create basic query string for querying objects associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param isCountQuery<br>
	 * 		true=create query for counting total results<br>
	 * 		false=create query for fetching results
	 * @return basic query string or null if no parameters. 
	 */
	private String createThesaurusQueryString(String termSnsId) {
		termSnsId = MdekUtils.processStringParameter(termSnsId);

		if (termSnsId == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join obj.searchtermObjs termObjs " +
			"inner join termObjs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			"termSns.snsId = '" + termSnsId + "'";
		
		return qString;
	}

	public long queryObjectsFullTextTotalNum(String searchTerm) {

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<ObjectNode> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return retList;
		}

		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
	}
	
	public List<ObjectNode> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		
		List<ObjectNode> retList = new ArrayList<ObjectNode>();
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);
		
		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";
		
		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
		
	}
	
	public long queryObjectsExtendedTotalNum(IngridDocument searchParams) {
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	/**
	 * Create basic query string for querying addresses concerning full text.
	 * @param searchTerm term to search for
	 * @return basic query string or null if no parameters. 
	 */
	private String createFullTextQueryString(String searchTerm) {
		searchTerm = MdekUtils.processStringParameter(searchTerm);

		if (searchTerm == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join oNode.fullIndexObjs fidx " +
			"where " +
			"fidx.idxName = '" + IDX_NAME_FULLTEXT + "' " +
			"and fidx.idxValue like '%" + searchTerm + "%'";

		return qString;
	}

	public List<T01Object> getAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		List<T01Object> retList = new ArrayList<T01Object>();

		Session session = getSession();

		retList = session.createQuery("select distinct o " +
			"from T01Object o " +
			"where o.responsibleUuid = ?")
			.setString(0, responsibleUserUuid)
			.list();

		return retList;
	}
}
