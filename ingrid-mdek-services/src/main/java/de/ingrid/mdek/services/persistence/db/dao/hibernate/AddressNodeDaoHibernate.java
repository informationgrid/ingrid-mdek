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
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Hibernate-specific implementation of the <tt>IAddressNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class AddressNodeDaoHibernate
	extends GenericHibernateDao<AddressNode>
	implements  IAddressNodeDao, IFullIndexAccess {

	private static final Logger LOG = LogManager.getLogger(AddressNodeDaoHibernate.class);

    public AddressNodeDaoHibernate(SessionFactory factory) {
        super(factory, AddressNode.class);
    }

	public AddressNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		if (uuid == null) {
			return null;
		}

		Session session = getSession();

		String qString = "from AddressNode aNode ";
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch aNode.t02AddressWork ";
		}
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch aNode.t02AddressPublished ";			
		}
		// DO NOT EXCLUDE HIDDEN ADDRESSES (IGE Users), these ones are also fetched (e.g. in User management)
		qString += "where aNode.addrUuid = ?1";

		AddressNode aN = (AddressNode) session.createQuery(qString)
			.setString(1, uuid)
			.uniqueResult();

		return aN;
	}

	public AddressNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion) {
		if (origId == null) {
			return null;
		}

		Session session = getSession();

		// always fetch working version. Is needed for querying, so we fetch it.
		String qString = "select aNode from AddressNode aNode " +
				"left join fetch aNode.t02AddressWork aWork ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch aNode.t02AddressPublished ";			
		}
		qString += "where aWork.orgAdrId = ?1 ";
		// order to guarantee always same node in front if multiple nodes with same orig id ! 
		qString += "order by aNode.addrUuid";

		List<AddressNode> aNodes = session.createQuery(qString)
			.setString(1, origId)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		AddressNode retNode = null;
		String nodeUuids = "";
		for (AddressNode aNode : aNodes) {
			if (retNode == null) {
				retNode = aNode;
			}
			nodeUuids += "\n     " + aNode.getAddrUuid();
		}
		if (aNodes.size() > 1) {
			LOG.warn("MULTIPLE NODES WITH SAME ORIG_ID: " + origId + " ! Nodes:" + nodeUuids);
		}

		return retNode;
	}

	public List<AddressNode> getTopAddresses(boolean onlyFreeAddresses,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		// we need a concrete instance for determining address type !
		if (whichEntityVersion == null) {
			whichEntityVersion = IdcEntityVersion.WORKING_VERSION;
		}

		String q = "select aNode from AddressNode aNode ";
		String addrAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aPub";
			q += "left join fetch aNode.t02AddressPublished " + addrAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aWork";
			q += "left join fetch aNode.t02AddressWork " + addrAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch aNode.addressNodeChildren aChildren ";
		}
		// THIS ALSO EXCLUDES HIDDEN IGE USER ADDRESSES !
		q += "where aNode.fkAddrUuid is null ";

		if (onlyFreeAddresses) {
			q += "and " + addrAlias + ".adrType = " + AddressType.FREI.getDbValue();
		} else {
			q += "and " + addrAlias + ".adrType != " + AddressType.FREI.getDbValue();			
		}
		// only order if only ONE version requested
		if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
			q += "order by " + 
				addrAlias + ".adrType desc, " +
				addrAlias + ".institution, " +
				addrAlias + ".lastname, " +
				addrAlias + ".firstname"; 
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return aNodes;
	}

	public List<AddressNode> getSubAddresses(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select aNode from AddressNode aNode ";
		String addrAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aPub";
			q += "left join fetch aNode.t02AddressPublished " + addrAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aWork";
			q += "left join fetch aNode.t02AddressWork " + addrAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch aNode.addressNodeChildren aChildren ";
		}
		q += "where aNode.fkAddrUuid = ?1 ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + 
					addrAlias + ".adrType desc, " +
					addrAlias + ".institution, " +
					addrAlias + ".lastname, " +
					addrAlias + ".firstname"; 
			}
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
			.setString(1, parentUuid)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return aNodes;
	}

	public List<AddressNode> getAllSubAddresses(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select aNode from AddressNode aNode ";
		String addrAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aPub";
			q += "left join fetch aNode.t02AddressPublished " + addrAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			addrAlias = "aWork";
			q += "left join fetch aNode.t02AddressWork " + addrAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch aNode.addressNodeChildren aChildren ";
		}
		q += "where aNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + 
					addrAlias + ".adrType desc, " +
					addrAlias + ".institution, " +
					addrAlias + ".lastname, " +
					addrAlias + ".firstname"; 
			}
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return aNodes;
	}

	public List<AddressNode> getSelectedSubAddresses(String parentUuid,
			IdcChildrenSelectionType whichChildren,
			PublishType parentPubType) {
		Session session = getSession();

		String q = "select aNode from AddressNode aNode ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "left join fetch aNode.t02AddressPublished a ";
		}
		q += "where aNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "and a.publishId < " + parentPubType.getDbValue();
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return aNodes;
	}

	public int countAllSubAddresses(String parentUuid, IdcEntityVersion versionOfSubAddressesToCount) {
		Session session = getSession();
		
		String q = "select count(aNode) " +
			"from AddressNode aNode " +
			"where aNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%'";

		if (versionOfSubAddressesToCount == IdcEntityVersion.WORKING_VERSION) {
			q += " and aNode.addrId != aNode.addrIdPublished ";

		} else if (versionOfSubAddressesToCount == IdcEntityVersion.PUBLISHED_VERSION) {
			q += " and aNode.addrIdPublished is not null";			
		}
		
		Long totalNum = (Long) session.createQuery(q).uniqueResult();
		
		return totalNum.intValue();
	}

	public boolean isSubNode(String uuidToCheck, String uuidParent) {
		boolean isSubNode = false;

		List<String> path = getAddressPath(uuidToCheck);
		
		if (path != null) {
			if (path.contains(uuidParent)) {
				isSubNode = true;
			}
		}
		
		return isSubNode;
	}
	
	public AddressNode getAddrDetails(String uuid) {
		return getAddrDetails(uuid, IdcEntityVersion.WORKING_VERSION);
	}

	public AddressNode getAddrDetails(String uuid, IdcEntityVersion whichEntityVersion) {
		Session session = getSession();

		String q = "from AddressNode aNode ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			q += "left join fetch aNode.t02AddressPublished a ";			
		} else {
			q += "left join fetch aNode.t02AddressWork a ";			
		}
		q += "left join fetch a.t021Communications aComm " +
		// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !
		// DO NOT EXCLUDE HIDDEN ADDRESSES (IGE Users), these ones are also fetched with details (e.g. in User management)
			"where aNode.addrUuid = ?1";
 
		// fetch all at once (one select with outer joins)
		AddressNode aN = (AddressNode) session.createQuery(q)
			.setString(1, uuid)
			.uniqueResult();

		return aN;
	}

	public HashMap getObjectReferencesFrom(String addressUuid, int startIndex, int maxNum) {
		Session session = getSession();

		HashMap retMap = new HashMap();

		// select ALL references from published ones, has highest "prio"
		List<Object[]> nodeIdsAndNamesPub = session.createQuery(
				"select distinct oNode.id, oPub.objName " +
				"from ObjectNode oNode " +
				"left join oNode.t01ObjectPublished oPub " +
				"left join oPub.t012ObjAdrs objAdr " +
				"where objAdr.adrUuid = ?1 " +
				"order by oPub.objName")
				.setString(1, addressUuid)
				.list();
		// extract ids as list !
		List<Long> nodeIdsPub = new ArrayList<Long>();
		for (Object[] nodeIdAndNamePub : nodeIdsAndNamesPub) {
			Long nodeIdPub = (Long) nodeIdAndNamePub[0];
			if (!nodeIdsPub.contains(nodeIdPub)) {
				nodeIdsPub.add(nodeIdPub);
			}
		}

		// select ALL references from working copies (node ids)
		// NOTICE: working copy == published one if not "in Bearbeitung" !
		List<Object[]> nodeIdsAndNamesWork = session.createQuery(
				"select distinct oNode.id, oWork.objName " +
				"from ObjectNode oNode " +
				"left join oNode.t01ObjectWork oWork " +
				"left join oWork.t012ObjAdrs objAdr " +
				"where objAdr.adrUuid = ?1 " +
				"order by oWork.objName")
				.setString(1, addressUuid)
				.list();			
		// extract ids as list !
		List<Long> nodeIdsWork = new ArrayList<Long>();
		for (Object[] nodeIdAndNameWork : nodeIdsAndNamesWork) {
			Long nodeIdWork = (Long) nodeIdAndNameWork[0];
			if (!nodeIdsWork.contains(nodeIdWork)) {
				nodeIdsWork.add(nodeIdWork);
			}
		}

		// then remove all published references also contained in working references.
		// we get the ones only in published version, meaning they were deleted in the
		// working copies !
		List<Long> nodeIdsPubOnly = new ArrayList<Long>();
		for (Long idPub : nodeIdsPub) {
			if (!nodeIdsWork.contains(idPub)) {
				nodeIdsPubOnly.add(idPub);
			}
		}
		
		// determine total num of object references
		int totalNum = nodeIdsPubOnly.size() + nodeIdsWork.size();
		retMap.put(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM, totalNum);

		// determine published only references to show
		int clearedNodesPubOnly = 0;
		if (nodeIdsPubOnly.size() < startIndex+1) {
			clearedNodesPubOnly = nodeIdsPubOnly.size();
			nodeIdsPubOnly.clear();
		} else {
			nodeIdsPubOnly = nodeIdsPubOnly.subList(startIndex, nodeIdsPubOnly.size());			
		}
		if (nodeIdsPubOnly.size() > maxNum) {
			nodeIdsPubOnly = nodeIdsPubOnly.subList(0, maxNum);
		}

		// determine working references to show
		if (nodeIdsPubOnly.size() < maxNum) {
			if (nodeIdsWork.size() > 0) {
				// determine which work nodes to fetch
				int firstResult = 0;
				int maxResults = maxNum;
				if (nodeIdsPubOnly.size() > 0) {
					maxResults = maxNum - nodeIdsPubOnly.size();
				} else {
					firstResult = startIndex - clearedNodesPubOnly;
				}
				if (nodeIdsWork.size() < firstResult+1) {
					nodeIdsWork.clear();
				} else {
					nodeIdsWork = nodeIdsWork.subList(firstResult, nodeIdsWork.size());			
				}
				if (nodeIdsWork.size() > maxResults) {
					nodeIdsWork = nodeIdsWork.subList(0, maxResults);
				}
			}
		} else {
			// enough nodes to show from nodeIdsPubOnly list
			nodeIdsWork.clear();			
		}

		// fetch all needed "nodes with only publish references"
		List<ObjectNode> nodesPubOnly = new ArrayList<ObjectNode>();
		if (nodeIdsPubOnly.size() > 0) {
			String query = "select oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectPublished oPub WHERE ";
			query += MdekUtils.createSplittedSqlQuery( "oNode.id", nodeIdsPubOnly, 500 );
			query += " order by oPub.objName";
			nodesPubOnly = session.createQuery(query)
					.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
					.list();			
		}

		// fetch all needed "nodes with work references"
		List<ObjectNode> nodesWork = new ArrayList<ObjectNode>();
		if (nodeIdsWork.size() > 0) {
			String query = "select oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectWork oWork WHERE ";
			query += MdekUtils.createSplittedSqlQuery( "oNode.id", nodeIdsWork, 500 );
			query += " order by oWork.objName";
			nodesWork = session.createQuery(query)
					.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
					.list();			
		}


		List<ObjectNode>[] retObjects = new List[] {
			nodesPubOnly,
			nodesWork
		};

		retMap.put(MdekKeys.OBJ_REFERENCES_FROM, retObjects);

		return retMap;
	}

	public List<ObjectNode> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId) {
		Session session = getSession();
		
		String sql = "select oNode from ObjectNode oNode " +
			"left join oNode.t01ObjectWork oWork " +
			"left join oWork.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = ?1";
		
		if (referenceTypeId != null) {
			sql += " and objAdr.type = " + referenceTypeId;
		}

		List<ObjectNode> objs = session.createQuery(sql)
				.setString(1, addressUuid)
				.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
				.list();
		
		return objs;
	}

	public AddressNode getParent(String uuid) {
		AddressNode parentNode = null;
		AddressNode aN = loadByUuid(uuid, null);
		if (aN != null && MdekUtils.isValidUuid(aN.getFkAddrUuid())) {
			parentNode = loadByUuid(aN.getFkAddrUuid(), null);
		}
		
		return parentNode;
	}

	public List<String> getAddressPath(String uuid) {
		ArrayList<String> uuidList = new ArrayList<String>();
		while(MdekUtils.isValidUuid(uuid)) {
			AddressNode aN = loadByUuid(uuid, null);
			if (aN == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			uuidList.add(0, uuid);
			uuid = aN.getFkAddrUuid();
		}

		return uuidList;
	}


	public List<IngridDocument> getAddressPathOrganisation(String inUuid, boolean includeEndNode) {
		ArrayList<IngridDocument> pathList = new ArrayList<IngridDocument>();
		String uuid = inUuid;
		while(MdekUtils.isValidUuid(uuid)) {
			AddressNode aN = loadByUuid(uuid, IdcEntityVersion.WORKING_VERSION);
			if (aN == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			boolean addToPath = true;
			if (uuid == inUuid && !includeEndNode) {
				addToPath = false;
			}
			if (addToPath) {
				T02Address a = aN.getT02AddressWork();
				String orga = a.getInstitution();
				Integer type = a.getAdrType();
				IngridDocument pathDoc = new IngridDocument();
				pathDoc.put(MdekKeys.ORGANISATION, orga);
				pathDoc.put(MdekKeys.CLASS, type);
				pathList.add(0, pathDoc);
			}
			uuid = aN.getFkAddrUuid();
		}

		return pathList;
	}

	public long searchTotalNumAddresses(IngridDocument searchParams) {

		String qString = createSearchQueryString(searchParams);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct aNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<AddressNode> searchAddresses(IngridDocument searchParams,
			int startHit, int numHits) {
		List<AddressNode> retList = new ArrayList<AddressNode>();

		String qString = createSearchQueryString(searchParams);
		
		if (qString == null) {
			return retList;
		}

		qString = "select aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return retList;
	}
	
	/**
	 * Create basic query string for search (no order etc.) dependent from passed search parameters.
	 * @param searchParams search parameters
	 * @return basic query string or null if no parameters. 
	 */
	private String createSearchQueryString(IngridDocument searchParams) {
		String institution = MdekUtils.processStringParameter(searchParams.getString(MdekKeys.ORGANISATION));
		String lastname = MdekUtils.processStringParameter(searchParams.getString(MdekKeys.NAME));
		String firstname = MdekUtils.processStringParameter(searchParams.getString(MdekKeys.GIVEN_NAME));

		if (institution == null && lastname == null && firstname == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from AddressNode aNode " +
			"inner join aNode.t02AddressWork addr " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr");

		if (institution != null) {
			qString += "and addr.institution LIKE '%" + institution + "%' ";
		}
		if (lastname != null) {
			qString += "and addr.lastname LIKE '%" + lastname + "%' ";
		}
		if (firstname != null) {
			qString += "and addr.firstname LIKE '%" + firstname + "%' ";
		}
		
		return qString;
	}

	public long queryAddressesThesaurusTermTotalNum(String termSnsId) {

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct aNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<AddressNode> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		List<AddressNode> retList = new ArrayList<AddressNode>();

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return retList;
		}

		qString = "select aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return retList;
	}
	
	/**
	 * Create basic query string for querying addresses associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @return basic query string or null if no parameters. 
	 */
	private String createThesaurusQueryString(String termSnsId) {
		termSnsId = MdekUtils.processStringParameter(termSnsId);

		if (termSnsId == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from AddressNode aNode " +
			"inner join aNode.t02AddressWork addr " +
			"inner join addr.searchtermAdrs termAdrs " +
			"inner join termAdrs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr") +
			" AND termSns.snsId = '" + termSnsId + "'";
		
		return qString;
	}

	public long queryAddressesFullTextTotalNum(String searchTerm) {

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct aNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<AddressNode> queryAddressesFullText(String searchTerm,
			int startHit, int numHits) {
		List<AddressNode> retList = new ArrayList<AddressNode>();

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return retList;
		}

		qString = "select aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return retList;
	}

	public List<AddressNode> queryAddressesExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		
		List<AddressNode> retList = new ArrayList<AddressNode>();
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createAddressExtendedSearchQuery(searchParams);
		
		Session session = getSession();

		qString = "select aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";
		
		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();

		return retList;
		
	}
	
	public long queryAddressesExtendedTotalNum(IngridDocument searchParams) {
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createAddressExtendedSearchQuery(searchParams);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct aNode) " + qString;

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

		// NOTICE: Errors when using "join fetch" because also used for count(*)
		String qString = "from AddressNode aNode " +
			"inner join aNode.t02AddressWork addr " +
			"inner join aNode.fullIndexAddrs fidx " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr") +
			" AND fidx.idxName = '" + IDX_NAME_FULLTEXT + "' " +
			"and fidx.idxValue like '%" + searchTerm + "%'";
		
		return qString;
	}

	public IngridDocument getWorkAddresses(String userUuid,
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, new Long(0));
		defaultResult.put(MdekKeys.ADR_ENTITIES, new ArrayList<AddressNode>());
		
		if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED) {
			return getWorkAddressesExpired(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.MODIFIED) {
			return getWorkAddressesModified(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
			return getWorkAddressesInQAWorkflow(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST ||
				selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS) {
			return getWorkAddressesPortalQuicklist(userUuid, startHit, numHits, selectionType);
		}

		return defaultResult;
	}

	/** NOTICE: queries PUBLISHED version because mod-date of published one is displayed ! */
	private IngridDocument getWorkAddressesExpired(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND a.responsibleUuid = '"+ userUuid +"' " +
			"and aMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(aNode) " +
			"from AddressNode aNode " +
				"inner join aNode.t02AddressPublished a " +
				"inner join a.addressMetadata aMeta " + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from AddressNode aNode " +
				"inner join fetch aNode.t02AddressPublished a " +
				"inner join fetch a.addressMetadata aMeta " +
				"left join fetch a.addressNodeMod aNodeMod " +
				"left join fetch aNodeMod.t02AddressWork aMod " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by a.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by a.adrType ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by aMod.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aMod.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aMod.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK addresses: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK addresses: " + qStringSelect);
		}
		List<AddressNode> aNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.ADR_ENTITIES, aNodes);
		
		return result;
	}

	private IngridDocument getWorkAddressesModified(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND a.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' " +
			"and (a.modUuid = '" + userUuid + "' or a.responsibleUuid = '" + userUuid + "') ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(aNode) " +
			"from AddressNode aNode " +
				"inner join aNode.t02AddressWork a " + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from AddressNode aNode " +
				"inner join fetch aNode.t02AddressWork a " +
				"left join fetch a.addressNodeMod aNodeUser " +
				"left join fetch aNodeUser.t02AddressWork aUser " + qCriteria;

		// order by: default is name
		String qOrderName = " a.institution ";
		qOrderName += orderAsc ? " asc " : " desc ";
		qOrderName += ", a.lastname ";
		qOrderName += orderAsc ? " asc " : " desc ";
		qOrderName += ", a.firstname ";

		String qOrderBy = " order by " + qOrderName;
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by a.adrType ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", " + qOrderName;
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by aUser.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", " + qOrderName;
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK addresses: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK addresses: " + qStringSelect);
		}
		List<AddressNode> aNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.ADR_ENTITIES, aNodes);
		
		return result;
	}

	private IngridDocument getWorkAddressesInQAWorkflow(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteriaUser = "and (aMeta.assignerUuid = '" + userUuid + "' or a.responsibleUuid = '" + userUuid + "') ";
		String qCriteria = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND (a.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' or " +
				"a.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " + qCriteriaUser;
		String qCriteriaAssigned = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND a.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' " + qCriteriaUser;
		String qCriteriaReassigned = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND a.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "' " + qCriteriaUser;

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(aNode) " +
			"from AddressNode aNode " +
				"inner join aNode.t02AddressWork a " +
				"inner join a.addressMetadata aMeta ";

		// query string for fetching results ! 
		String qStringSelect = "from AddressNode aNode " +
				"inner join fetch aNode.t02AddressWork a " +
				"inner join fetch a.addressMetadata aMeta " +
				"left join fetch aMeta.addressNodeAssigner aNodeUser " +
				"left join fetch aNodeUser.t02AddressWork aUser " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by a.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by a.adrType ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by aUser.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.STATE) {
			qOrderBy = " order by a.workState ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK addresses \"QA\": " + qStringCount + qCriteria);
			LOG.debug("HQL Counting WORK addresses \"QA ASSIGNED\": " + qStringCount + qCriteriaAssigned);
			LOG.debug("HQL Counting WORK addresses \"QA REASSIGNED\": " + qStringCount + qCriteriaReassigned);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount + qCriteria).uniqueResult();
		Long totalNumAssigned = (Long) session.createQuery(qStringCount + qCriteriaAssigned).uniqueResult();
		Long totalNumReassigned = (Long) session.createQuery(qStringCount + qCriteriaReassigned).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK addresses: " + qStringSelect);
		}
		List<AddressNode> aNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.TOTAL_NUM_QA_ASSIGNED, totalNumAssigned);
		result.put(MdekKeys.TOTAL_NUM_QA_REASSIGNED, totalNumReassigned);
		result.put(MdekKeys.ADR_ENTITIES, aNodes);
		
		return result;
	}

	private IngridDocument getWorkAddressesPortalQuicklist(String userUuid,
			int startHit, int numHits,
			IdcWorkEntitiesSelectionType selectionType) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a");
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			// user specific
			qCriteria = qCriteria +
				" AND (a.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' or " +
					"a.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " +
				"and (aMeta.assignerUuid = '" + userUuid + "' or a.modUuid = '" + userUuid + "') ";
		} else if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS) {
			// all users, also assigned to QA
			qCriteria = qCriteria +
				" AND (a.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' or " +
					"a.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' or " +
					"a.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') ";
		}

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(aNode) " +
			"from AddressNode aNode " +
				"inner join aNode.t02AddressWork a ";
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			qStringCount = qStringCount +
				"inner join a.addressMetadata aMeta ";
		}
		qStringCount = qStringCount + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from AddressNode aNode " +
				"inner join fetch aNode.t02AddressWork a ";
		if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			qStringSelect = qStringSelect +
				"inner join fetch a.addressMetadata aMeta ";
		}
		qStringSelect = qStringSelect + qCriteria;

		// always order by date
		String qOrderBy = " order by a.modTime desc";
		qStringSelect += qOrderBy;
		
		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK addresses \"QA\": " + qStringCount);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK addresses: " + qStringSelect);
		}
		List<AddressNode> aNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.ADR_ENTITIES, aNodes);
		
		return result;
	}

	public IngridDocument getQAAddresses(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, new Long(0));
		defaultResult.put(MdekKeys.ADR_ENTITIES, new ArrayList<AddressNode>());

		// check whether QA user
		if (!permHandler.hasQAPermission(userUuid)) {
			return defaultResult;
		}

		if (isCatAdmin) {
			return getQAAddresses(null, null, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);
		} else {
			return getQAAddressesViaGroup(userUuid, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);			
		}
	}

	/**
	 * QA PAGE: Get ALL Addresses where given user is QA and addresses WORKING VERSION match passed selection criteria.
	 * The QA addresses are determined via assigned addresses in QA group of user.
	 * All sub-addresses of "write-tree" addresses are included !
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param userUuid QA user
	 * @param whichWorkState only return addresses in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all addresses
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAAddressesViaGroup(String userUuid,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		Session session = getSession();

		// select all addresses in QA group (write permission) !
		// NOTICE: this doesn't include sub addresses of "write-tree" addresses !
		String qString = "select distinct pAddr.uuid, p2.action as perm " +
		"from " +
			"IdcUser usr, " +
			"IdcUserGroup uGrp, " +
			"IdcGroup grp, " +
			"IdcUserPermission pUsr, " +
			"Permission p1, " +
			"PermissionAddr pAddr, " +
			"Permission p2 " +
		"where " +
			// user -> grp -> QA
			"usr.addrUuid = '" + userUuid + "'" +
			" and usr.id = uGrp.idcUserId" +
			" and uGrp.idcGroupId = grp.id" +
			" and grp.id = pUsr.idcGroupId " +
			" and pUsr.permissionId = p1.id " +
			" and p1.action = '" + IdcPermission.QUALITY_ASSURANCE.getDbValue() + "'" +
			// grp -> address -> permission
			" and grp.id = pAddr.idcGroupId " +
			" and pAddr.permissionId = p2.id";

		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Selecting addresses in QA group: " + qString);
		}
		List<Object[]> groupAddrsAndPerms = session.createQuery(qString).list();

		// parse group addresses and separate "write single" and "write tree"
		List<String> addrUuidsWriteSingle= new ArrayList<String>();
		List<String> addrUuidsWriteTree = new ArrayList<String>();
		for (Object[] groupAddrAndPerm : groupAddrsAndPerms) {
			String aUuid = (String) groupAddrAndPerm[0];
			IdcPermission p = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, groupAddrAndPerm[1]);

			if (p == IdcPermission.WRITE_SINGLE) {
				addrUuidsWriteSingle.add(aUuid);
			} else if (p == IdcPermission.WRITE_TREE) {
				addrUuidsWriteTree.add(aUuid);
			}
		}

		return getQAAddresses(addrUuidsWriteSingle,
				addrUuidsWriteTree,
				whichWorkState, selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}

	/**
	 * QA PAGE: Get ALL Addresses where user has write permission matching passed selection criteria
	 * We return nodes, so we can evaluate whether published version exists !
	 * @param addrUuidsWriteSingle list of address uuids where user has single write permission, pass null if all addresses
	 * @param addrUuidsWriteTree list of address uuids where user has tree write permission, pass null if all addresses
	 * @param whichWorkState only return addresses in this work state, pass null if workstate should be ignored
	 * @param selectionType further selection criteria (see Enum), pass null if no further criteria
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAAddresses(List<String> addrUuidsWriteSingle,
			List<String> addrUuidsWriteTree,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		// first check content of lists and set to null if no content (to be used as flag)
		if (addrUuidsWriteSingle != null && addrUuidsWriteSingle.size() == 0) {
			addrUuidsWriteSingle = null;
		}
		if (addrUuidsWriteTree != null && addrUuidsWriteTree.size() == 0) {
			addrUuidsWriteTree = null;
		}
		
		Session session = getSession();

		// prepare queries

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(aNode) " +
			"from AddressNode aNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringCount += "inner join aNode.t02AddressPublished a ";
		} else {
			qStringCount += "inner join aNode.t02AddressWork a ";			
		}
		qStringCount += "inner join a.addressMetadata aMeta ";

		// with fetch: always fetch address and metadata, e.g. needed when mapping user operation (mark deleted) 
		String qStringSelect = "from AddressNode aNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringSelect += "inner join fetch aNode.t02AddressPublished a ";
		} else {
			qStringSelect += "inner join fetch aNode.t02AddressWork a ";			
		}
		qStringSelect += "inner join fetch a.addressMetadata aMeta ";
		if (whichWorkState == WorkState.QS_UEBERWIESEN) {
			qStringSelect += "inner join fetch aMeta.addressNodeAssigner aNodeUser ";
		} else {
			qStringSelect += "inner join fetch a.addressNodeMod aNodeUser ";
		}
		qStringSelect += "inner join fetch aNodeUser.t02AddressWork aUser ";

		// selection criteria
		String qStringCriteria = " where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a");

		if (whichWorkState != null || selectionType != null ||
				addrUuidsWriteSingle != null || addrUuidsWriteTree != null) {
			if (whichWorkState != null) {
				qStringCriteria += " AND a.workState = '" + whichWorkState.getDbValue() + "'";
			}

			if (selectionType != null) {
				if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
					qStringCriteria += " AND aMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();
				} else {
					// QASelectionType not handled ? return nothing !
					return result;
				}
			}
			
			if (addrUuidsWriteSingle != null || addrUuidsWriteTree != null) {
				// WRITE SINGLE 
				// add all write tree nodes to single nodes
				// -> top nodes of branch have to be selected in same way as write single objects
				if (addrUuidsWriteSingle == null) {
					addrUuidsWriteSingle = new ArrayList<String>();
				}
				if (addrUuidsWriteTree != null) {
					addrUuidsWriteSingle.addAll(addrUuidsWriteTree);
				}

				qStringCriteria += " AND (" + MdekUtils.createSplittedSqlQuery( "aNode.addrUuid", addrUuidsWriteSingle, 500 );

				// WRITE TREE 
				if (addrUuidsWriteTree != null) {
					qStringCriteria += " or ( ";

					boolean start = true;
					for (String aUuid : addrUuidsWriteTree) {
						if (!start) {
							qStringCriteria += " or ";							
						}
						qStringCriteria += 
							" aNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(aUuid) + "%' ";
						start = false;
					}
					qStringCriteria += " ) ";
				}
				qStringCriteria += " ) ";
			}
		}

		qStringCount += qStringCriteria;
		qStringSelect += qStringCriteria;

		// order by: default is date
		String qOrderBy = " order by a.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by a.adrType ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by aUser.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", aUser.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// set query parameters 
		Query qCount = session.createQuery(qStringCount);
		Query qSelect = session.createQuery(qStringSelect);

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting QA addresses: " + qStringCount);
		}
		Long totalNum = (Long) qCount.uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching QA addresses: " + qStringSelect);
		}
		List<AddressNode> aNodes = qSelect.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// and return results
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.ADR_ENTITIES, aNodes);
		
		return result;
	}

	public IngridDocument getAddressStatistics(String parentUuid, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		if (selectionType == IdcStatisticsSelectionType.CLASSES_AND_STATES) {
			result = getAddressStatistics_classesAndStates(parentUuid, onlyFreeAddresses);

		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE ||
				selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			result = getAddressStatistics_searchterms(parentUuid, onlyFreeAddresses, startHit, numHits, selectionType);
		}
		
		return result;
	}
	
	private IngridDocument getAddressStatistics_classesAndStates(String parentUuid,
			boolean onlyFreeAddresses) {
		IngridDocument result = new IngridDocument();
		
		Session session = getSession();

		// prepare query
		String qString = "select count(distinct aNode) " +
			"from " +
				"AddressNode aNode " +
				"inner join aNode.t02AddressWork addr " +
			"where " +
				// exclude hidden user addresses !
				AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr");

		// which classes to evaluate
		Object[] addrClasses;
		if (parentUuid != null) {
			// all classes in tree branch
			addrClasses = EnumUtil.getDbValues(AddressType.class);

			// node token in path !
			String parentUuidToken = "|" +  parentUuid + "|";
			// NOTICE: tree path in node doesn't contain node itself
     			qString += " AND (aNode.treePath like '%" + parentUuidToken + "%' " +
				"OR aNode.addrUuid = '" + parentUuid + "') ";
		} else {
			if (onlyFreeAddresses) {
				// only free addresses
				addrClasses = new Object[] { AddressType.FREI.getDbValue() };
			} else {
				// whole catalog -> all classes
				addrClasses = EnumUtil.getDbValues(AddressType.class);
			}
		}

		// fetch number of addresses of specific class and work state
		Object[] workStates = EnumUtil.getDbValues(WorkState.class);
		Long totalNum;
		for (Object addrClass : addrClasses) {
			IngridDocument classMap = new IngridDocument();

			// get total number of entities of given class underneath parent
			String qStringClass = qString +	" AND addr.adrType = " + addrClass;
			totalNum = (Long) session.createQuery(qStringClass).uniqueResult();
			
			classMap.put(MdekKeys.TOTAL_NUM, totalNum);
			
			// add number of different work states
			for (Object workState : workStates) {
				// get total number of entities of given work state
				String qStringState = qStringClass + " AND addr.workState = '" + workState + "'";
				totalNum = (Long) session.createQuery(qStringState).uniqueResult();

				classMap.put(workState, totalNum);
			}

			result.put(addrClass, classMap);
		}

		return result;
	}

	private IngridDocument getAddressStatistics_searchterms(String parentUuid,
			boolean onlyFreeAddresses, int startHit, int numHits,
			IdcStatisticsSelectionType selectionType) {

		IngridDocument result = new IngridDocument();
		
		Session session = getSession();

		// basics for queries to execute

		String qStringFromWhere = "from " +
				"AddressNode aNode " +
				"inner join aNode.t02AddressWork addr " +
				"inner join addr.searchtermAdrs searchtAddr " +
				"inner join searchtAddr.searchtermValue searchtVal " +
				"where " +
				// exclude hidden user addresses !
				AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr");

		if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE) {
			qStringFromWhere += " AND searchtVal.type = '" + SearchtermType.FREI.getDbValue() + "' ";			
		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			qStringFromWhere += " AND (searchtVal.type = '" + SearchtermType.UMTHES.getDbValue() + "' " +
				"OR searchtVal.type = '" + SearchtermType.GEMET.getDbValue() + "') ";
		}

		if (parentUuid != null) {
			// node token in path !
			String parentUuidToken = "|" +  parentUuid + "|";
			// NOTICE: tree path in node doesn't contain node itself
			qStringFromWhere += " AND (aNode.treePath like '%" + parentUuidToken + "%' " +
				"OR aNode.addrUuid = '" + parentUuid + "') ";
		} else if (onlyFreeAddresses) {
			qStringFromWhere += " AND addr.adrType = " + AddressType.FREI.getDbValue() + " ";
		}

		// first count number of assigned free search terms
		String qString = "select count(searchtVal.term) " +
			qStringFromWhere;
		Long totalNumSearchtermsAssigned = (Long) session.createQuery(qString).uniqueResult();

		// then count number of distinct free search terms for paging
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

	public List<String> getAllAddressUuids() {
		Session session = getSession();

		String q = "select distinct addrUuid " +
			"from AddressNode aNode " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", null);
		
		List<String> uuids = session.createQuery(q)
				.list();

		return uuids;
	}

	public AddressNode getAddressForIndex(String uuid) {
		Session session = getSession();

		// NOTICE:
		// WE MAY NOT FETCH ASSOCIATIONS WITH LINE ATTRIBUTES which are ordered by SQL ("order-by" in mapping) !
		// These order by in SQL is extreme time consuming when fetching huge data sets !

		String q = "from AddressNode aNode " +
			"left join fetch aNode.t02AddressWork a " +			
			"left join fetch a.addressComments " +

			"left join fetch a.searchtermAdrs aTerm " +
			"left join fetch aTerm.searchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +

			"left join fetch a.t021Communications " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaNode("aNode", "a") +
			" AND aNode.addrUuid = ?1";
		
		// fetch all at once (one select with outer joins)
		AddressNode aN = (AddressNode) session.createQuery(q)
			.setString(1, uuid)
			.uniqueResult();

		return aN;
	}
}
