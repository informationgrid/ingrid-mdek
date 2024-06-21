/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.utils.IngridDocument;
import org.hibernate.query.Query;
import org.hibernate.transform.ToListResultTransformer;

/**
 * Hibernate-specific implementation of the <tt>IObjectNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 *
 * @author Martin
 */
public class ObjectNodeDaoHibernate
	extends GenericHibernateDao<ObjectNode>
	implements  IObjectNodeDao, IFullIndexAccess {

	private static final Logger LOG = LogManager.getLogger(ObjectNodeDaoHibernate.class);

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
		}
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectPublished ";
		}
		qString += "where oNode.objUuid = ?1";

		ObjectNode oN = (ObjectNode) session.createQuery(qString)
			.setParameter(1, uuid)
			.uniqueResult();

		return oN;
	}

	public ObjectNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion) {
		if (origId == null) {
			return null;
		}

		Session session = getSession();

		// always fetch working version. Is needed for querying, so we fetch it.
		String qString = "select oNode from ObjectNode oNode " +
				"left join fetch oNode.t01ObjectWork oWork ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectPublished ";
		}
		qString += "where oWork.orgObjId = ?1 ";
		// order to guarantee always same node in front if multiple nodes with same orig id !
		qString += "order by oNode.objUuid";

		List<ObjectNode> oNodes = session.createQuery(qString)
			.setParameter(1, origId)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		ObjectNode retNode = null;
		String nodeUuids = "";
		for (ObjectNode oNode : oNodes) {
			if (retNode == null) {
				retNode = oNode;
			}
			nodeUuids += "\n     " + oNode.getObjUuid();
		}
		if (oNodes.size() > 1) {
			LOG.warn("MULTIPLE NODES WITH SAME ORIG_ID: " + origId + " ! Nodes:" + nodeUuids);
		}

		return retNode;
	}

	public List<ObjectNode> getTopObjects(IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.fkObjUuid is null ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName";
			}
		}

		List<ObjectNode> oNodes = session.createQuery(q)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return oNodes;
	}

	public List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.fkObjUuid = ?1 ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName";
			}
		}

		List<ObjectNode> oNodes = session.createQuery(q)
			.setParameter(1, parentUuid)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return oNodes;
	}

	public List<ObjectNode> getAllSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION ||
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName";
			}
		}

		List<ObjectNode> oNodes = session.createQuery(q)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return oNodes;
	}

	public List<ObjectNode> getSelectedSubObjects(String parentUuid,
			IdcChildrenSelectionType whichChildren,
			PublishType parentPubType) {
		Session session = getSession();

		String q = "select oNode from ObjectNode oNode ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "left join fetch oNode.t01ObjectPublished o ";
		}
		q += "where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "and o.publishId < " + parentPubType.getDbValue();
		}

		List<ObjectNode> oNodes = session.createQuery(q)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return oNodes;
	}

	public int countAllSubObjects(String parentUuid, IdcEntityVersion versionOfSubObjectsToCount) {
		Session session = getSession();

		String q = "select count(oNode) " +
			"from ObjectNode oNode " +
			"where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%'";

		if (versionOfSubObjectsToCount == IdcEntityVersion.WORKING_VERSION) {
			q += " and oNode.objId != oNode.objIdPublished ";

		} else if (versionOfSubObjectsToCount == IdcEntityVersion.PUBLISHED_VERSION) {
			q += " and oNode.objIdPublished is not null";
		}

		Long totalNum = (Long) session.createQuery(q).uniqueResult();

		return totalNum.intValue();
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
			"where oNode.objUuid = ?1";

		// enable address filter ?
//		session.enableFilter("t012ObjAdrFilter").setParameter("type", 1);

		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery(q)
			.setParameter(1, uuid)
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
				"where oRef.objToUuid = ?1")
				.setParameter(1, uuid)
				.list();

		// then select all references from published ones
		List<Long> nodeIdsPub = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectPublished oPub " +
				"left join oPub.objectReferences oRef " +
				"where oRef.objToUuid = ?1")
				.setParameter(1, uuid)
				.list();

		// then remove all published references also contained in working references.
		// we get the ones only in published version, meaning they were deleted in the
		// working copies !
		// So nodeIdsPubOnly contains all published objects that have a new working copy
		// where the reference to the object with this uuid has been deleted.
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
					"select oNode from ObjectNode oNode " +
							"left join fetch oNode.t01ObjectWork oWork " +
					"where oNode.id in (" +
						"select distinct oNode2.id from ObjectNode oNode2 " +
						"left join oNode2.t01ObjectWork oWork2 " +
						"left join oWork2.objectReferences oRef " +
						"where oRef.objToUuid = ?1" +
					")")
					.setParameter(1, uuid)
					.setResultTransformer(ToListResultTransformer.INSTANCE)
					.list();

		}

		// fetch all "nodes with only publish references"
		List<ObjectNode> nodesPubOnly = new ArrayList<ObjectNode>();
		if (nodeIdsPubOnly.size() > 0) {
			String query = "SELECT oNode FROM ObjectNode oNode " +
					"LEFT JOIN FETCH oNode.t01ObjectPublished oPub WHERE ";
			query += MdekUtils.createSplittedSqlQuery( "oNode.id", nodeIdsPubOnly, 500 );
			nodesPubOnly = session.createQuery(query)
					.setResultTransformer(ToListResultTransformer.INSTANCE)
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

		qString = "select oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return retList;
	}

	/**
	 * Create basic query string for querying objects associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
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

		qString = "select oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		return retList;
	}

	public List<ObjectNode> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {

		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);

		qString = "select oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
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

	public IngridDocument getWorkObjects(String userUuid,
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, 0L);
		defaultResult.put(MdekKeys.OBJ_ENTITIES, new ArrayList<ObjectNode>());

		if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED) {
			return getWorkObjectsExpired(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.MODIFIED) {
			return getWorkObjectsModified(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
			return getWorkObjectsInQAWorkflow(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.SPATIAL_REF_EXPIRED) {
			return getWorkObjectsSpatialRefExpired(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST ||
					selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS ||
					selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS_PUBLISHED) {
			return getWorkObjectsPortalQuicklist(userUuid, startHit, numHits, selectionType);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_PUBLISHED) {
			return getPublishedObjectsPortalQuicklist(userUuid, startHit, numHits);
		}

		return defaultResult;
	}

	/** NOTICE: queries PUBLISHED version because mod-date of published one is displayed ! */
	private IngridDocument getWorkObjectsExpired(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"o.responsibleUuid = '"+ userUuid +"' " +
			"and o.objClass != '1000' " + // ignore folders
			"and oMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectPublished o " +
				"inner join o.objectMetadata oMeta " + qCriteria;

		// query string for fetching results !
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectPublished o " +
				"inner join fetch o.objectMetadata oMeta " +
				"left join fetch o.addressNodeMod aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	private IngridDocument getWorkObjectsModified(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"o.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' " +
			"and o.objClass != '1000' " + // ignore folders
			"and (o.modUuid = '" + userUuid + "' or o.responsibleUuid = '" + userUuid + "') ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " + qCriteria;

		// query string for fetching results !
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"left join fetch o.addressNodeMod aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is name
		String qOrderBy = " order by o.objName ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.objName ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.objName ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	private IngridDocument getWorkObjectsInQAWorkflow(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteriaUser = "and (oMeta.assignerUuid = '" + userUuid + "' or o.responsibleUuid = '" + userUuid + "') ";
		String qCriteria = " where " +
			"(o.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' or " +
				"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " + qCriteriaUser;
		String qCriteriaAssigned = " where " +
			"o.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' " + qCriteriaUser;
		String qCriteriaReassigned = " where " +
			"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "' " + qCriteriaUser;

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " +
				"inner join o.objectMetadata oMeta ";

		// query string for fetching results !
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"inner join fetch o.objectMetadata oMeta " +
				"left join fetch oMeta.addressNodeAssigner aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.STATE) {
			qOrderBy = " order by o.workState ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects \"QA\": " + qStringCount + qCriteria);
			LOG.debug("HQL Counting WORK objects \"QA ASSIGNED\": " + qStringCount + qCriteriaAssigned);
			LOG.debug("HQL Counting WORK objects \"QA REASSIGNED\": " + qStringCount + qCriteriaReassigned);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount + qCriteria).uniqueResult();
		Long totalNumAssigned = (Long) session.createQuery(qStringCount + qCriteriaAssigned).uniqueResult();
		Long totalNumReassigned = (Long) session.createQuery(qStringCount + qCriteriaReassigned).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.TOTAL_NUM_QA_ASSIGNED, totalNumAssigned);
		result.put(MdekKeys.TOTAL_NUM_QA_REASSIGNED, totalNumReassigned);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	private IngridDocument getWorkObjectsSpatialRefExpired(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"o.responsibleUuid = '"+ userUuid +"' " +
			"and spRefVal.type = '" + SpatialReferenceType.GEO_THESAURUS.getDbValue() + "' " +
			"and spRefSns.expiredAt is not null";

		// common associations
		String qStringCommon = "inner join o.spatialReferences spRef " +
			"inner join spRef.spatialRefValue spRefVal " +
			"inner join spRefVal.spatialRefSns spRefSns ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(distinct oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " +
				qStringCommon + qCriteria;

		// query string for fetching results !
		String qStringSelect = "select oNode " +
				"from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"left join fetch o.addressNodeMod aNode " +
				"left join fetch aNode.t02AddressWork a " +
				qStringCommon + qCriteria;

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	private IngridDocument getPublishedObjectsPortalQuicklist(String userUuid,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		// user specific and published !
		String qCriteria = " where " +
//			"oNode.objIdPublished is not null and " +
			"o.modUuid = '" + userUuid + "' " +
		    "and o.objClass != '1000' "; // ignore folders

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectPublished o ";
		qStringCount = qStringCount + qCriteria;

		// query string for fetching results !
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectPublished o ";
		qStringSelect = qStringSelect + qCriteria;

		// always order by date
		String qOrderBy = " order by o.modTime desc";
		qStringSelect += qOrderBy;

		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting PUBLISHED objects: " + qStringCount);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching PUBLISHED objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	private IngridDocument getWorkObjectsPortalQuicklist(String userUuid,
			int startHit, int numHits,
			IdcWorkEntitiesSelectionType selectionType) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where o.objClass != '1000' and "; // ignore folders "
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			// user specific
			qCriteria = qCriteria +
				"(o.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' or " +
					"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " +
				"and (oMeta.assignerUuid = '" + userUuid + "' or o.modUuid = '" + userUuid + "') ";
		} else if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS) {
			// all users, also assigned to QA
			qCriteria = qCriteria +
				"(o.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' or " +
					"o.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' or " +
					"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') ";
		} else if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS_PUBLISHED) {
			// all users, only published
			qCriteria = qCriteria +
				"(o.workState = '" + WorkState.VEROEFFENTLICHT.getDbValue() + "') ";
		}

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o ";
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			qStringCount = qStringCount +
				"inner join o.objectMetadata oMeta ";
		}
		qStringCount = qStringCount + qCriteria;

		// query string for fetching results !
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o ";
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			qStringSelect = qStringSelect +
				"inner join fetch o.objectMetadata oMeta ";
		}
		qStringSelect = qStringSelect + qCriteria;

		// always order by date
		String qOrderBy = " order by o.modTime desc";
		qStringSelect += qOrderBy;

		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	public IngridDocument getQAObjects(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, 0L);
		defaultResult.put(MdekKeys.OBJ_ENTITIES, new ArrayList<ObjectNode>());

		// check whether QA user
		if (!permHandler.hasQAPermission(userUuid)) {
			return defaultResult;
		}

		if (isCatAdmin) {
			return getQAObjects(null, null, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);
		} else {
			return getQAObjectsViaGroup(userUuid, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);
		}
	}

	/**
	 * QA PAGE: Get ALL Objects where given user is QA and objects WORKING VERSION match passed selection criteria.
	 * The QA objects are determined via assigned objects in QA group of user.
	 * All sub-objects of "write-tree" objects are included !
	 * We return nodes, so we can evaluate whether published version exists !
	 * @param userUuid QA user
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAObjectsViaGroup(String userUuid,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// select all objects in QA group (write permission) !
		// NOTICE: this doesn't include sub objects of "write-tree" objects !
		String qString = "select distinct pObj.uuid, p2.action as perm " +
		"from " +
			"IdcUser usr, " +
			"IdcUserGroup uGrp, " +
			"IdcGroup grp, " +
			"IdcUserPermission pUsr, " +
			"Permission p1, " +
			"PermissionObj pObj, " +
			"Permission p2 " +
		"where " +
			// user -> grp -> QA
			"usr.addrUuid = '" + userUuid + "'" +
			" and usr.id = uGrp.idcUserId" +
			" and uGrp.idcGroupId = grp.id" +
			" and grp.id = pUsr.idcGroupId " +
			" and pUsr.permissionId = p1.id " +
			" and p1.action = '" + IdcPermission.QUALITY_ASSURANCE.getDbValue() + "'" +
			// grp -> object -> permission
			" and grp.id = pObj.idcGroupId " +
			" and pObj.permissionId = p2.id";

		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Selecting objects in QA group: " + qString);
		}
		List<Object[]> groupObjsAndPerms = session.createQuery(qString).list();

		// parse group objects and separate "write single" and "write tree"
		List<String> objUuidsWriteSingle = new ArrayList<String>();
		List<String> objUuidsWriteTree = new ArrayList<String>();
		for (Object[] groupObjAndPerm : groupObjsAndPerms) {
			String oUuid = (String) groupObjAndPerm[0];
			IdcPermission p = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, groupObjAndPerm[1]);

			if (p == IdcPermission.WRITE_SINGLE) {
				objUuidsWriteSingle.add(oUuid);
			} else if (p == IdcPermission.WRITE_TREE) {
				objUuidsWriteTree.add(oUuid);
			}
		}

		return getQAObjects(objUuidsWriteSingle,
				objUuidsWriteTree,
				whichWorkState, selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}

	/**
	 * QA PAGE: Get ALL Objects where user has write permission matching passed selection criteria
	 * We return nodes, so we can evaluate whether published version exists !
	 * @param objUuidsWriteSingle list of object uuids where user has single write permission, pass null if all objects
	 * @param objUuidsWriteTree list of object uuids where user has tree write permission, pass null if all objects
	 * @param whichWorkState only return objects in this work state, pass null if workstate should be ignored
	 * @param selectionType further selection criteria (see Enum), pass null if no further criteria
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAObjects(List<String> objUuidsWriteSingle,
			List<String> objUuidsWriteTree,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		// first check content of lists and set to null if no content (to be used as flag)
		if (objUuidsWriteSingle != null && objUuidsWriteSingle.size() == 0) {
			objUuidsWriteSingle = null;
		}
		if (objUuidsWriteTree != null && objUuidsWriteTree.size() == 0) {
			objUuidsWriteTree = null;
		}

		Session session = getSession();

		// prepare queries

		// associations for querying expired spatial refs
		String qStringSpatialRefs = "inner join o.spatialReferences spRef " +
			"inner join spRef.spatialRefValue spRefVal " +
			"inner join spRefVal.spatialRefSns spRefSns ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(distinct oNode) " +
			"from ObjectNode oNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringCount += "inner join oNode.t01ObjectPublished o ";
		} else {
			qStringCount += "inner join oNode.t01ObjectWork o ";
		}
		qStringCount += "inner join o.objectMetadata oMeta ";
		if (selectionType == IdcQAEntitiesSelectionType.SPATIAL_REF_EXPIRED) {
			qStringCount += qStringSpatialRefs;
		}

		// with fetch: always fetch object and metadata, e.g. needed when mapping user operation (mark deleted)
		String qStringSelect = "select oNode " +
			"from ObjectNode oNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringSelect += "inner join fetch oNode.t01ObjectPublished o ";
		} else {
			qStringSelect += "inner join fetch oNode.t01ObjectWork o ";
		}
		qStringSelect += "inner join fetch o.objectMetadata oMeta ";
		if (whichWorkState == WorkState.QS_UEBERWIESEN) {
			qStringSelect += "inner join fetch oMeta.addressNodeAssigner aNode ";
		} else {
			qStringSelect += "inner join fetch o.addressNodeMod aNode ";
		}
		qStringSelect += "inner join fetch aNode.t02AddressWork a ";
		if (selectionType == IdcQAEntitiesSelectionType.SPATIAL_REF_EXPIRED) {
			qStringSelect += qStringSpatialRefs;
		}

		// selection criteria
		if (whichWorkState != null || selectionType != null ||
				objUuidsWriteSingle != null || objUuidsWriteTree != null) {
			String qStringCriteria = " where ";

			boolean addAnd = false;

			if (whichWorkState != null) {
				qStringCriteria += "o.workState = '" + whichWorkState.getDbValue() + "'";
				addAnd = true;
			}

			if (selectionType != null) {
				if (addAnd) {
					qStringCriteria += " and ";
				}
				if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
					qStringCriteria += "oMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();
				} else if (selectionType == IdcQAEntitiesSelectionType.SPATIAL_REF_EXPIRED) {
					qStringCriteria += "spRefVal.type = '" + SpatialReferenceType.GEO_THESAURUS.getDbValue() + "' " +
						"and spRefSns.expiredAt is not null";
				} else {
					// QASelectionType not handled ? return nothing !
					return result;
				}
				addAnd = true;
			}

			if (objUuidsWriteSingle != null || objUuidsWriteTree != null) {
				if (addAnd) {
					qStringCriteria += " and ( ";
				}

				// WRITE SINGLE
				// add all write tree nodes to single nodes
				// -> top nodes of branch have to be selected in same way as write single objects
				if (objUuidsWriteSingle == null) {
					objUuidsWriteSingle = new ArrayList<String>();
				}
				if (objUuidsWriteTree != null) {
					objUuidsWriteSingle.addAll(objUuidsWriteTree);
				}

				qStringCriteria += MdekUtils.createSplittedSqlQuery( "oNode.objUuid", objUuidsWriteSingle, 500 );

				// WRITE TREE
				if (objUuidsWriteTree != null) {
					qStringCriteria += " or ( ";

					boolean start = true;
					for (String oUuid : objUuidsWriteTree) {
						if (!start) {
							qStringCriteria += " or ";
						}
						qStringCriteria +=
							" oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(oUuid) + "%' ";
						start = false;
					}
					qStringCriteria += " ) ";
				}

				if (addAnd) {
					qStringCriteria += " ) ";
				}
				addAnd = true;
			}

			qStringCount += qStringCriteria;
			qStringSelect += qStringCriteria;
		}

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// set query parameters
		Query qCount = session.createQuery(qStringCount);
		Query qSelect = session.createQuery(qStringSelect);

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting QA objects: " + qStringCount);
		}
		Long totalNum = (Long) qCount.uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching QA objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = qSelect
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		// and return results
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);

		return result;
	}

	public IngridDocument getObjectStatistics(String parentUuid,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		if (selectionType == IdcStatisticsSelectionType.CLASSES_AND_STATES) {
			result = getObjectStatistics_classesAndStates(parentUuid);

		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE ||
				selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			result = getObjectStatistics_searchterms(parentUuid, startHit, numHits, selectionType);
		}

		return result;
	}

	private IngridDocument getObjectStatistics_classesAndStates(String parentUuid) {
		IngridDocument result = new IngridDocument();

		Session session = getSession();

		// prepare query
		String qString = "select count(distinct oNode) " +
			"from " +
				"ObjectNode oNode " +
				"inner join oNode.t01ObjectWork obj " +
			"where ";
		if (parentUuid != null) {
			// NOTICE: tree path in node doesn't contain node itself
			qString += "(oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' " +
				"OR oNode.objUuid = '" + parentUuid + "') " +
				"AND ";
		}

		// fetch number of objects of specific class and work state
		Object[] objClasses = EnumUtil.getDbValues(ObjectType.class);
		Object[] workStates = EnumUtil.getDbValues(WorkState.class);
		Long totalNum;
		for (Object objClass : objClasses) {
			IngridDocument classMap = new IngridDocument();

			// get total number of entities of given class underneath parent
			String qStringClass = qString +	" obj.objClass = " + objClass;
			totalNum = (Long) session.createQuery(qStringClass).uniqueResult();

			classMap.put(MdekKeys.TOTAL_NUM, totalNum);

			// add number of different work states
			for (Object workState : workStates) {
				// get total number of entities of given work state
				String qStringState = qStringClass + " AND obj.workState = '" + workState + "'";
				totalNum = (Long) session.createQuery(qStringState).uniqueResult();

				classMap.put(workState, totalNum);
			}

			result.put(objClass, classMap);
		}

		return result;
	}

	private IngridDocument getObjectStatistics_searchterms(String parentUuid,
			int startHit, int numHits,
			IdcStatisticsSelectionType selectionType) {

		IngridDocument result = new IngridDocument();

		Session session = getSession();

		// basics for queries to execute

		String qStringFromWhere = "from " +
				"ObjectNode oNode " +
				"inner join oNode.t01ObjectWork obj " +
				"inner join obj.searchtermObjs searchtObj " +
				"inner join searchtObj.searchtermValue searchtVal " +
			"where ";
		if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE) {
			qStringFromWhere += " searchtVal.type = '" + SearchtermType.FREI.getDbValue() + "' ";
		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			qStringFromWhere += " (searchtVal.type = '" + SearchtermType.UMTHES.getDbValue() + "' " +
				"OR searchtVal.type = '" + SearchtermType.GEMET.getDbValue() + "') ";
		}

		if (parentUuid != null) {
			// NOTICE: tree path in node doesn't contain node itself
			qStringFromWhere += " AND (oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' " +
				"OR oNode.objUuid = '" + parentUuid + "') ";
		}

		// first count number of assigned search terms
		String qString = "select count(searchtVal.term) " +
			qStringFromWhere;
		Long totalNumSearchtermsAssigned = (Long) session.createQuery(qString).uniqueResult();

		// then count number of distinct search terms for paging
		qString = "select count(distinct searchtVal.term) " +
			qStringFromWhere;
		Long totalNumSearchtermsPaging = (Long) session.createQuery(qString).uniqueResult();

		// then count every searchterm
		qString = "select searchtVal.term, count(searchtVal.term) " +
			qStringFromWhere;
		qString += " group by searchtVal.term " +
			// NOTICE: in order clause: use of alias for count causes HQL error !
			// use of same count expression in order causes error on mySql 4 !
			// use of integer for which select attribute works !
			"order by 2 desc, searchtVal.term";

//		if (LOG.isDebugEnabled()) {
//			LOG.debug("Executing HQL: " + qString);
//		}

		List hits = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		ArrayList<IngridDocument> termDocs = new ArrayList<IngridDocument>();
		for (Object hit : hits) {
			Object[] objs = (Object[]) hit;

			IngridDocument termDoc = new IngridDocument();
			termDoc.put(MdekKeys.TERM_NAME, objs[0]);
			termDoc.put(MdekKeys.TOTAL_NUM, objs[1]);

			termDocs.add(termDoc);
		}

		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumSearchtermsPaging);
		result.put(MdekKeys.TOTAL_NUM, totalNumSearchtermsAssigned);
		result.put(MdekKeys.STATISTICS_SEARCHTERM_LIST, termDocs);

		return result;
	}

	public List<String> getObjectUuidsForExport(String exportCriterion) {
		Session session = getSession();

		String q = "select distinct oNode.objUuid " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectPublished o " +
				"inner join o.t014InfoImparts oExp " +
			"where oExp.impartValue = ?1";

		List<String> uuids = session.createQuery(q)
				.setParameter(1, exportCriterion)
				.list();

		return uuids;
	}

	public List<String> getAllObjectUuids() {
		Session session = getSession();

		String q = "select distinct objUuid " +
			"from ObjectNode";

		List<String> uuids = session.createQuery(q)
				.list();

		return uuids;
	}

	public ObjectNode getObjectForIndex(String uuid) {
		Session session = getSession();

		// NOTICE:
		// WE DO NOT FETCH ASSOCIATIONS WITH LINE ATTRIBUTES which are ordered by SQL ("order-by" in mapping) !
		// These order by in SQL is extreme time consuming when fetching huge data sets !

		String q = "from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork o " +
//			"left join fetch o.objectComments " +

//			"left join fetch o.searchtermObjs oTerm " +
//			"left join fetch oTerm.searchtermValue termVal " +
//			"left join fetch termVal.searchtermSns " +

//			"left join fetch o.spatialReferences oSpRef " +
//			"left join fetch oSpRef.spatialRefValue spRefVal " +
//			"left join fetch spRefVal.spatialRefSns " +

//			"left join fetch o.t0110AvailFormats " +
//			"left join fetch o.t0112MediaOptions " +
			"left join fetch o.t011ObjDatas " +
//			"left join fetch o.t011ObjDataParas " +

			"left join fetch o.t011ObjGeos oGeo " +
//			"left join fetch oGeo.t011ObjGeoKeycs " +
//			"left join fetch oGeo.t011ObjGeoSymcs " +

			"left join fetch o.t011ObjLiteratures " +
			"left join fetch o.t011ObjProjects " +

			"left join fetch o.t011ObjServs oServ " +
//			"left join fetch oServ.t011ObjServOperations oServOp " +
//			"left join fetch oServOp.t011ObjServOpConnpoints " +
//			"left join fetch oServOp.t011ObjServOpDependss " +
//			"left join fetch oServOp.t011ObjServOpParas " +
//			"left join fetch oServOp.t011ObjServOpPlatforms " +
//			"left join fetch oServ.t011ObjServVersions " +

//			"left join fetch o.t014InfoImparts " +
//			"left join fetch o.t015Legists " +
//			"left join fetch o.t017UrlRefs " +
//			"left join fetch o.objectConformitys " +
//			"left join fetch o.objectAccesss " +
			"where oNode.objUuid = ?1";

		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery(q)
			.setParameter(1, uuid)
			.uniqueResult();

		return oN;
	}
}
