package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.MdekUtils.IdcEntitySelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
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

	public ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		if (uuid == null) {
			return null;
		}

		Session session = getSession();

		String qString = "from ObjectNode oNode ";
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectWork ";			
		} else if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectPublished ";			
		}
		qString += "where oNode.objUuid = ?";

		ObjectNode oN = (ObjectNode) session.createQuery(qString)
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

	public List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		return getSubObjects(parentUuid, whichEntityVersion, fetchSubNodesChildren, false);
	}

	private List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren,
			boolean fetchMetadata) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			q += "left join fetch oNode.t01ObjectWork o ";			
		} else if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			q += "left join fetch oNode.t01ObjectPublished o ";			
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		if (fetchMetadata) {
			q += "left join fetch o.objectMetadata ";
		}
		q += "where oNode.fkObjUuid = ? ";
		if (whichEntityVersion != null && whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
			q += "order by o.objName"; 
		}
		
		List<ObjectNode> oNodes = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		return oNodes;
	}

	/** Get sub uuids of parent with given uuid (only next level) */
	private List<String> getSubObjectUuids(String parentUuid) {
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
		ObjectNode oN = loadByUuid(uuid, null);
		if (oN != null && oN.getFkObjUuid() != null) {
			parentNode = loadByUuid(oN.getFkObjUuid(), null);
		}
		
		return parentNode;
	}

	public ObjectNode getObjDetails(String uuid) {
		return getObjDetails(uuid, IdcEntityVersion.WORKING_VERSION);
	}

	public ObjectNode getObjDetails(String uuid, IdcEntityVersion whichEntityVersion) {
		Session session = getSession();

		String q = "from ObjectNode oNode ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			q += "left join fetch oNode.t01ObjectPublished o ";			
		} else {
			q += "left join fetch oNode.t01ObjectWork o ";			
		}
		q += 
		// referenced objects (to) 
			"left join fetch o.objectReferences oRef " +
			"left join fetch oRef.objectNode oRefNode " +
			"left join fetch oRefNode.t01ObjectWork oRefObj " +

// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !

		// referenced addresses
//			"left join fetch o.t012ObjAdrs objAdr " +
//			"left join fetch objAdr.addressNode aNode " +
//			"left join fetch aNode.t02AddressWork aWork " +
//			"left join fetch aWork.t021Communications aComm " +
		// spatial references 
//			"left join fetch o.spatialReferences spatRef " +
//			"left join fetch spatRef.spatialRefValue spatialRefVal " +
//			"left join fetch spatialRefVal.spatialRefSns " +
		// url refs 
//			"left join fetch o.t017UrlRefs urlRef " +
			"where oNode.objUuid = ?";

		// enable address filter ?
//		session.enableFilter("t012ObjAdrFilter").setParameter("type", 1);
		
		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery(q)
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
			ObjectNode oN = loadByUuid(uuid, null);
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

	public List<ObjectNode> getQAObjects(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcEntitySelectionType selectionType, Integer maxNum) {
		if (isCatAdmin) {
			return getObjects(whichWorkState, selectionType, maxNum);
		}
		
		// check whether QA user
		if (!permHandler.hasQAPermission(userUuid)) {
			return new ArrayList<ObjectNode>(0);
		}

		// determine best way to find QA objects
		// "traverse tree branches of group" <-> "select objects matching criteria and test on write permission"
		boolean getQAObjectsViaGroup = true;
		boolean doSelection = whichWorkState != null || selectionType != null;
		if (doSelection) {
			long numObjsMatchingSelection = getNumObjects(whichWorkState, selectionType);
			if (numObjsMatchingSelection <= maxNum) {
				getQAObjectsViaGroup = false;
				
				maxNum = new Long(numObjsMatchingSelection).intValue();
			}
		}

		if (getQAObjectsViaGroup) {
			return getQAObjectsViaGroup(userUuid, whichWorkState, selectionType, maxNum);			
		} else {
			return getQAObjectsViaPreSelection(userUuid, permHandler, 
					whichWorkState, selectionType, maxNum);
		}
	}

	/**
	 * Traverse all objects in QA-group of user (includes all tree branches) and
	 * return objects matching selection criteria.
	 * @param userUuid
	 * @param whichWorkState object is in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @param maxNum maximum number of objects to query, pass null if all objects !
	 * @return list of objects
	 */
	private List<ObjectNode> getQAObjectsViaGroup(String userUuid,
			WorkState whichWorkState, IdcEntitySelectionType selectionType, Integer maxNum) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		Session session = getSession();

		// select all objects in group (write permission) !
		// NOTICE: this doesn't include sub objects of "write-tree" objects !
		// Always fetch object and metadata, e.g. needed when mapping user operation (deleted) 
		String qString = "select distinct oNode, p2.action as perm " +
		"from " +
			"ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork o " +
			"left join fetch o.objectMetadata, " +
			"IdcUser usr, " +
			"IdcGroup grp, " +
			"IdcUserPermission pUsr, " +
			"Permission p1, " +
			"PermissionObj pObj, " +
			"Permission p2 " +
		"where " +
			// user -> grp -> QA
			"usr.addrUuid = '" + userUuid + "'" +
			" and usr.idcGroupId = grp.id" +
			" and grp.id = pUsr.idcGroupId " +
			" and pUsr.permissionId = p1.id " +
			" and p1.action = '" + IdcPermission.QUALITY_ASSURANCE.getDbValue() + "'" +
			// grp -> object-> write permission
			" and grp.id = pObj.idcGroupId " +
			" and pObj.permissionId = p2.id " +
			" and (p2.action = '" + IdcPermission.WRITE_SINGLE.getDbValue() + "' or " +
			"  p2.action = '" + IdcPermission.WRITE_TREE.getDbValue() + "') " +
			// object
			" and pObj.uuid = oNode.objUuid";

		Query q = session.createQuery(qString);

		// parse group objects and separate write single and write tree
		List<Object[]> groupObjPerms = q.list();
		List<ObjectNode> groupObjsWriteTree = new ArrayList<ObjectNode>();
		for (Object[] groupObjPerm : groupObjPerms) {
			ObjectNode oNode = (ObjectNode) groupObjPerm[0];
			T01Object o = oNode.getT01ObjectWork();
			IdcPermission p = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, groupObjPerm[1]);

			// check "write single objects" and include if matching selection
			if (p == IdcPermission.WRITE_SINGLE) {
				if (checkObject(o, whichWorkState, selectionType)) {
					retList.add(oNode);					
				}
			} else if (p == IdcPermission.WRITE_TREE) {
				groupObjsWriteTree.add(oNode);
			}
		}

		// process tree branches of "write-tree objects"
		Integer numNodesMissing = null;
		for (ObjectNode oN : groupObjsWriteTree) {
			if (maxNum != null) {
				numNodesMissing = maxNum - retList.size();
			}
			if (numNodesMissing == null || numNodesMissing > 0) {
				T01Object o = oN.getT01ObjectWork();
				boolean includeCurrentObj = checkObject(o, whichWorkState, selectionType);
				retList.addAll(getTreeObjects(oN, whichWorkState, selectionType, includeCurrentObj, numNodesMissing));					
			}

			if (maxNum != null) {
				if (retList.size() >= maxNum) {
					retList = retList.subList(0, maxNum);
					break;
				}
			}
		}

		return retList;
	}

	/**
	 * First select all objects matching criteria. Then return objects where user has write permission.
	 * @param userUuid
	 * @param permHandler permission handler needed for checking write permissions
	 * @param whichWorkState object is in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @param maxNum maximum number of objects to preselect, pass null if all objects !
	 * @return list of objects
	 */
	private List<ObjectNode> getQAObjectsViaPreSelection(String userUuid, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcEntitySelectionType selectionType, Integer maxNum) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		List<ObjectNode> oNs = getObjects(whichWorkState, selectionType, maxNum);
		for (ObjectNode oN : oNs) {
			if (permHandler.hasWritePermissionForObject(oN.getObjUuid(), userUuid, false)) {
				retList.add(oN);
			}
		}

		return retList;
	}

	/**
	 * Find number of objects matching the selection criteria !
	 * @param whichWorkState only return objects in this work state, pass null if workstate should be ignored
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @return number of objects found
	 */
	private long getNumObjects(WorkState whichWorkState, IdcEntitySelectionType selectionType) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>(); 

		Session session = getSession();

		// always fetch object and metadata, e.g. needed when mapping user operation (mark deleted ?) 
		String qString = "select count(oNode) " +
			"from ObjectNode oNode ";
		
		if (whichWorkState != null || selectionType != null) {
			qString += " where ";

			boolean addAnd = false;
			if (whichWorkState != null) {
				qString += "oNode.t01ObjectWork.workState = '" + whichWorkState.getDbValue() + "'";
				addAnd = true;
			}
			if (selectionType != null) {
				if (addAnd) {
					qString += " and ";
				}
				if (selectionType == IdcEntitySelectionType.EXPIRY_STATE_EXPIRED) {
					qString += "oNode.t01ObjectWork.objectMetadata.expiryState = " + ExpiryState.EXPIRED.getDbValue();
				} else if (selectionType == IdcEntitySelectionType.SPATIAL_RELATIONS_UPDATED) {
					// TODO: Add when implementing catalog management sns update !
					return 0;
				} else {
					// QASelectionType not handled ? return nothing !
					return 0;
				}
			}
		}

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	/**
	 * Get ALL Objects where WORKING VERSION is in given work state. We return nodes, so we can evaluate
	 * whether published version exists !
	 * @param whichWorkState only return objects in this work state, pass null if workstate should be ignored
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @param maxNum maximum number of objects to query, pass null if all objects !
	 * @return list of objects
	 */
	private List<ObjectNode> getObjects(WorkState whichWorkState, IdcEntitySelectionType selectionType, Integer maxNum) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>(); 

		Session session = getSession();

		// always fetch object and metadata, e.g. needed when mapping user operation (mark deleted ?) 
		String qString = "from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork o " +
			"left join fetch o.objectMetadata oMeta ";
		
		if (whichWorkState != null || selectionType != null) {
			qString += " where ";

			boolean addAnd = false;
			if (whichWorkState != null) {
				qString += "o.workState = '" + whichWorkState.getDbValue() + "'";
				addAnd = true;
			}
			if (selectionType != null) {
				if (addAnd) {
					qString += " and ";
				}
				if (selectionType == IdcEntitySelectionType.EXPIRY_STATE_EXPIRED) {
					qString += "oMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();
				} else if (selectionType == IdcEntitySelectionType.SPATIAL_RELATIONS_UPDATED) {
					// TODO: Add when implementing catalog management sns update !
					return retList;
				} else {
					// QASelectionType not handled ? return nothing !
					return retList;
				}
			}
		}

		Query q = session.createQuery(qString);
		if (maxNum != null) {
			q.setMaxResults(maxNum);				
		}

		return q.list();
	}

	/**
	 * Check whether passed object matches passed "selection criteria".
	 * @param o object to test
	 * @param whichWorkState object is in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @return true=object matches, include it<br>
	 * 		false=object doesn't match, exclude it
	 */
	private boolean checkObject(T01Object o, WorkState whichWorkState, IdcEntitySelectionType selectionType) {
		// first check work state
		if (whichWorkState != null && !whichWorkState.getDbValue().equals(o.getWorkState())) {
			return false;
		}

		// then additional selection criteria
		if (selectionType != null) {
			if (selectionType == IdcEntitySelectionType.EXPIRY_STATE_EXPIRED) {
				if (!MdekUtils.ExpiryState.EXPIRED.getDbValue().equals(o.getObjectMetadata().getExpiryState())) {
					return false;
				}

			} else if (selectionType == IdcEntitySelectionType.SPATIAL_RELATIONS_UPDATED) {
				// TODO: Add when implementing catalog management sns update !
				return false;

			} else {
				// QASelectionType not handled ? return false, object doesn't match is default !
				return false;
			}
		}

		// object matches selection criteria
		return true;
	}

	/** Fetch whole subtree (ALL levels) of given object.
	 * @param rootNode top node of tree
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if no criteria
	 * @param includeRootNode true=include the passed root node (state not checked)<br>
	 * 		false=do not include root node (state not checked)<br>
	 * @param maxNum maximum number of nodes to fetch, pass null if whole tree branch
	 * @return list of all subnodes in tree
	 */
	private List<ObjectNode> getTreeObjects(ObjectNode rootNode,
			WorkState whichWorkState, IdcEntitySelectionType selectionType,
			boolean includeRootNode, Integer maxNum) {
		List<ObjectNode> treeNodes = new ArrayList<ObjectNode>();

		boolean doSelection = whichWorkState != null || selectionType != null;

		if (includeRootNode) {
			treeNodes.add(rootNode);
		}

//		long startTime = System.currentTimeMillis();
//		long numNodes = 0;

		// traverse iteratively via stack
		Stack<ObjectNode> stack = new Stack<ObjectNode>();
		stack.push(rootNode);
		while (!stack.isEmpty()) {
			ObjectNode treeNode = stack.pop();

			// add next level of subnodes to stack (ALL NON LEAFS, independent from state, so we won't lose tree branch ...)
			List<ObjectNode> subNodes = getSubObjects(treeNode.getObjUuid(), IdcEntityVersion.WORKING_VERSION, true, true);
			for (ObjectNode sN : subNodes) {
				if (sN.getObjectNodeChildren().size() > 0) {
					stack.push(sN);					
				}
//				numNodes++;
			}

//			System.out.println("getTreeObjects NUM NODES processed: " + numNodes);

			// add subnodes matching selection
			if (doSelection) {
				for (ObjectNode oN : subNodes) {
					if (checkObject(oN.getT01ObjectWork(), whichWorkState, selectionType)) {
						treeNodes.add(oN);
					}
				}
			} else {
				treeNodes.addAll(subNodes);
			}

			if (maxNum != null) {
				if (treeNodes.size() >= maxNum) {
					treeNodes = treeNodes.subList(0, maxNum);
					break;
				}
			}
		}
/*
		long endTime = System.currentTimeMillis();
		long neededTime = endTime - startTime;
		System.out.println("\n----------");
		System.out.println("getTreeObjects NUM NODES requested: " + maxNum);
		System.out.println("getTreeObjects NUM NODES processed: " + numNodes);
		System.out.println("getTreeObjects NUM NODES delivered: " + treeNodes.size());
		System.out.println("getTreeObjects EXECUTION TIME: " + neededTime + " ms");
*/
		return treeNodes;
	}
}
