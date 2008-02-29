package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;

/**
 * Hibernate-specific implementation of the <tt>SearchtermValue</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SearchtermValueDaoHibernate
	extends GenericHibernateDao<SearchtermValue>
	implements  ISearchtermValueDao {

	private static final Logger LOG = Logger.getLogger(SearchtermValueDaoHibernate.class);

    public SearchtermValueDaoHibernate(SessionFactory factory) {
        super(factory, SearchtermValue.class);
    }
    
	/** Load SearchtermValue according to given values. Returns null if not found. 
	 * @param type
	 * @param term
	 * @param searchtermSnsId id of record in SearchtermSns
	 * @param entityId connected to this entity (object or address)
	 * @param entityType type of entity (object or address)
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadSearchterm(String type, String term, Long searchtermSnsId,
		Long entityId, IdcEntityType entityType)
	{

//		if (LOG.isDebugEnabled()) {
//			LOG.debug("type: " + type + ", term: " + term + ", SearchtermSns_ID: " + searchtermSnsId);			
//		}

		SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, type);

		SearchtermValue termValue = null;
		if (SearchtermType.FREI == termType) {
			if (entityType == IdcEntityType.OBJECT) {
				termValue = loadFreiSearchtermObject(term, entityId);
			} else {
				termValue = loadFreiSearchtermAddress(term, entityId);
			}

		} else if (SearchtermType.THESAURUS == termType) {
			termValue = loadThesaurusSearchterm(term, searchtermSnsId);
			
		} else {
			LOG.warn("Unknown Type of SearchtermValue, type: " + type);
		}

		return termValue;
	}

	/** Load Freien SearchtermValue according to given values. Returns null if not found. 
	 * @param term
	 * @param objId connected to this object
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadFreiSearchtermObject(String term, Long objId) {
		Session session = getSession();

		String qString = "from SearchtermObj termObj " +
			"left join fetch termObj.searchtermValue termVal " +
			"where termVal.type = '" + SearchtermType.FREI.getDbValue() + "' " +
			"and termVal.term = ? " +
			"and termObj.objId = ?";
	
		Query q = session.createQuery(qString);
		q.setString(0, term);
		q.setLong(1, objId);

		SearchtermValue termValue = null;
		SearchtermObj termObj = (SearchtermObj) q.uniqueResult();
		if (termObj != null) {
			termValue = termObj.getSearchtermValue();
		}

		return termValue; 
	}

	/** Load Freien SearchtermValue according to given values. Returns null if not found. 
	 * @param term
	 * @param adrId connected to this address
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadFreiSearchtermAddress(String term, Long adrId) {
		Session session = getSession();

		String qString = "from SearchtermAdr termAdr " +
			"left join fetch termAdr.searchtermValue termVal " +
			"where termVal.type = '" + SearchtermType.FREI.getDbValue() + "' " +
			"and termVal.term = ? " +
			"and termAdr.adrId = ?";
	
		Query q = session.createQuery(qString);
		q.setString(0, term);
		q.setLong(1, adrId);

		SearchtermValue termValue = null;
		SearchtermAdr termAdr = (SearchtermAdr) q.uniqueResult();
		if (termAdr != null) {
			termValue = termAdr.getSearchtermValue();
		}

		return termValue; 
	}

	/** Load Thesaurus SearchtermValue according to given values. Returns null if not found.
	 * Pass both search criteria or only one of em.
	 * @param term pass null if only searchtermSnsId
	 * @param searchtermSnsId id of record in SearchtermSns, pass null if only term
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadThesaurusSearchterm(String term, Long searchtermSnsId) {
		if (term == null && searchtermSnsId == null) {
			return null;
		}

		Session session = getSession();

		String qString = "from SearchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +
			"where termVal.type = '" + SearchtermType.THESAURUS.getDbValue() + "' ";

		if (term != null) {
			qString += "and termVal.term = ? ";
		}
		if (searchtermSnsId != null) {
			qString += "and termVal.searchtermSnsId = ? ";
		}
	
		Query q = session.createQuery(qString);
		int nextPos = 0;
		if (term != null) {
			q.setString(nextPos++, term);
		}
		if (searchtermSnsId != null) {
			q.setLong(nextPos++, searchtermSnsId);			
		}

		return (SearchtermValue) q.uniqueResult();
	}

	public SearchtermValue loadOrCreate(String type, String term, SearchtermSns termSns,
			Long entityId, IdcEntityType entityType)
	{
		Long termSnsId = (termSns != null) ? termSns.getId() : null; 
		SearchtermValue termValue = loadSearchterm(type, term, termSnsId, entityId, entityType);
		
		if (termValue == null) {
			termValue = new SearchtermValue();
			termValue.setType(type);
			termValue.setTerm(term);
			termValue.setSearchtermSns(termSns);
			termValue.setSearchtermSnsId(termSnsId);
			makePersistent(termValue);
		}
		
		return termValue;
	}
}
