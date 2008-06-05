package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
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

	private static final Logger LOG = Logger.getLogger(AddressNodeDaoHibernate.class);

    public AddressNodeDaoHibernate(SessionFactory factory) {
        super(factory, AddressNode.class);
    }

	public AddressNode loadByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}

		Session session = getSession();

		AddressNode aN = (AddressNode) session.createQuery("from AddressNode aNode " +
			"where aNode.addrUuid = ?")
			.setString(0, uuid)
			.uniqueResult();
		
		return aN;
	}

	public List<AddressNode> getTopAddresses(boolean onlyFreeAddresses) {
		Session session = getSession();
		String query = "select distinct aNode from AddressNode aNode " +
			"left join fetch aNode.t02AddressWork a " +
			"left join fetch aNode.addressNodeChildren aChildren " +
			"where aNode.fkAddrUuid is null ";
		if (onlyFreeAddresses) {
			query += "and a.adrType=" + AddressType.FREI.getDbValue();
		} else {
			query += "and a.adrType!=" + AddressType.FREI.getDbValue();			
		}
		query += "order by a.adrType desc, a.institution, a.lastname, a.firstname";
		
		List<AddressNode> aNodes = session.createQuery(query).list();

		return aNodes;
	}

	public List<AddressNode> getSubAddresses(String parentUuid, boolean fetchAddressLevel) {
		Session session = getSession();
		String q = "select distinct aNode from AddressNode aNode ";
		if (fetchAddressLevel) {
			q += "left join fetch aNode.t02AddressWork a " +
				 "left join fetch aNode.addressNodeChildren aChildren ";
		}
		q += "where aNode.fkAddrUuid = ? ";
		if (fetchAddressLevel) {
			q += "order by a.adrType desc, a.institution, a.lastname, a.firstname"; 
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		return aNodes;
	}

	public List<String> getSubAddressUuids(String parentUuid) {
		Session session = getSession();

		List<String> childUuids = session.createQuery("select aNode.addrUuid " +
				"from AddressNode aNode " +
				"where aNode.fkAddrUuid = ?")
				.setString(0, parentUuid)
				.list();
		
		return childUuids;
	}

	public int countSubAddresses(String parentUuid) {
		int totalNum = 0;

		Stack<String> uuidStack = new Stack<String>();
		uuidStack.push(parentUuid);

		while (!uuidStack.isEmpty()) {
			String uuid = uuidStack.pop();
			if (!uuid.equals(parentUuid)) {
				totalNum++;
			}
			List<String> subUuids = getSubAddressUuids(uuid);
			for (String subUuid : subUuids) {
				uuidStack.push(subUuid);
			}
		}
		
		return totalNum;
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
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		AddressNode aN = (AddressNode) session.createQuery("from AddressNode aNode " +
			"left join fetch aNode.t02AddressWork aWork " +
			"left join fetch aWork.t021Communications aComm " +

// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !

			"where aNode.addrUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

		return aN;
	}

	public List<ObjectNode>[] getAllObjectReferencesFrom(String addressUuid) {
		Session session = getSession();

		// first select all references from working copies (node ids)
		// NOTICE: working copy == published one if not "in Bearbeitung" !
		List<Long> nodeIdsWork = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectWork oWork " +
				"left join oWork.t012ObjAdrs objAdr " +
				"where objAdr.adrUuid = ?")
				.setString(0, addressUuid)
				.list();

		// then select all references from published ones
		List<Long> nodeIdsPub = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectPublished oPub " +
				"left join oPub.t012ObjAdrs objAdr " +
				"where objAdr.adrUuid = ?")
				.setString(0, addressUuid)
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
			nodesWork,
			nodesPubOnly
		};

		return retObjects;
	}

	public List<ObjectNode> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId) {
		Session session = getSession();
		
		String sql = "select distinct oNode from ObjectNode oNode " +
			"left join oNode.t01ObjectWork oWork " +
			"left join oWork.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = ?";
		
		if (referenceTypeId != null) {
			sql += " and objAdr.type = " + referenceTypeId;
		}

		List<ObjectNode> objs = session.createQuery(sql)
				.setString(0, addressUuid)
				.list();
		
		return objs;
	}

	public AddressNode getParent(String uuid) {
		AddressNode parentNode = null;
		AddressNode aN = loadByUuid(uuid);
		if (aN != null && aN.getFkAddrUuid() != null) {
			parentNode = loadByUuid(aN.getFkAddrUuid());
		}
		
		return parentNode;
	}

	public List<String> getAddressPath(String uuid) {
		ArrayList<String> uuidList = new ArrayList<String>();
		while(uuid != null) {
			AddressNode aN = loadByUuid(uuid);
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
		while(uuid != null) {
			AddressNode aN = loadByUuid(uuid);
			if (aN == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			boolean addToPath = true;
			if (uuid == inUuid && !includeEndNode) {
				addToPath = false;
			}
			if (addToPath) {
				String orga = aN.getT02AddressWork().getInstitution();
				Integer type = aN.getT02AddressWork().getAdrType();
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

		qString = "select distinct aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
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
			// dummy, so we can start with "and"
			"aNode.id IS NOT NULL ";

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

		qString = "select distinct aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
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
			"termSns.snsId = '" + termSnsId + "'";
		
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

		qString = "select distinct aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
	}

	public List<AddressNode> queryAddressesExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		
		List<AddressNode> retList = new ArrayList<AddressNode>();
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createAddressExtendedSearchQuery(searchParams);
		
		Session session = getSession();

		qString = "select distinct aNode " + qString;
		qString += " order by addr.adrType, addr.institution, addr.lastname, addr.firstname";
		
		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
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
			"fidx.idxName = '" + IDX_NAME_FULLTEXT + "' " +
			"and fidx.idxValue like '%" + searchTerm + "%'";
		
		return qString;
	}

	public List<T02Address> getAllAddressesOfResponsibleUser(String responsibleUserUuid) {
		List<T02Address> retList = new ArrayList<T02Address>();

		Session session = getSession();

		retList = session.createQuery("select distinct a " +
			"from T02Address a " +
			"where a.responsibleUuid = ?")
			.setString(0, responsibleUserUuid)
			.list();

		return retList;
	}
}
