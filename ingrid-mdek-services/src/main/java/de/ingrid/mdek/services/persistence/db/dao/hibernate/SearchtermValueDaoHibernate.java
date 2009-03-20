package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

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
    
	/** Load SearchtermValue according to given values. Returns null if not found. */
	private SearchtermValue loadSearchterm(String type, String term, Integer entryId,
		Long searchtermSnsId,
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

		} else if (SearchtermType.UMTHES == termType || SearchtermType.GEMET == termType) {
			termValue = loadThesaurusSearchterm(termType, searchtermSnsId);
			
		} else if (SearchtermType.INSPIRE == termType) {
			termValue = loadInspireSearchterm(entryId);
			
		} else {
			LOG.warn("Unknown Type of SearchtermValue, type: " + type);
		}

		return termValue;
	}

	/** Load Freien SearchtermValue according to given values. Returns null if not found. */
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

	/** Load Freien SearchtermValue according to given values. Returns null if not found. */
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
	 * @param termType type of term (UMTHES or GEMET). NOT NULL !
	 * @param searchtermSnsId id of record in SearchtermSns, NEVER NULL, has to exist !
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadThesaurusSearchterm(SearchtermType termType, Long searchtermSnsId) {
		Session session = getSession();

		String qString = "from SearchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +
			"where termVal.type = '" + termType.getDbValue() + "' " +
			"and termVal.searchtermSnsId = " + searchtermSnsId + "";

		// we query list(), NOT uniqueResult() ! e.g. ST catalog has multiple imported
		// values ("Messdaten", "Meﬂdaten") refering to same searchtermSns. Comparison 
		// of these names equals true, due to configuration of MySQL !
		SearchtermValue searchtermVal = null;
		List<SearchtermValue> searchtermVals = session.createQuery(qString).list();
		if (searchtermVals.size() > 0) {
			searchtermVal = searchtermVals.get(0);
		}

		return searchtermVal;
	}

	/** Load INSPIRE SearchtermValue according to given values. Returns null if not found.
	 * @param entryId id of entry in syslist, NEVER NULL !
	 * @return SearchtermValue or null
	 */
	private SearchtermValue loadInspireSearchterm(Integer entryId) {
		Session session = getSession();

		String qString = "from SearchtermValue termVal " +
			"where termVal.type = '" + SearchtermType.INSPIRE.getDbValue() + "' " +
			"and termVal.entryId = " + entryId;
	
		return (SearchtermValue) session.createQuery(qString).uniqueResult();
	}

	public SearchtermValue loadOrCreate(String type, String term, Integer entryId,
			SearchtermSns termSns,
			Long entityId, IdcEntityType entityType)
	{
		Long termSnsId = (termSns != null) ? termSns.getId() : null; 
		SearchtermValue termValue = loadSearchterm(type, term, entryId, termSnsId,
			entityId, entityType);
		
		if (termValue == null) {
			termValue = new SearchtermValue();
			termValue.setType(type);
			termValue.setTerm(term);
			termValue.setEntryId(entryId);
			termValue.setSearchtermSns(termSns);
			termValue.setSearchtermSnsId(termSnsId);
			makePersistent(termValue);
		}
		
		return termValue;
	}

	public List<SearchtermValue> getSearchtermValues(SearchtermType[] types) {
		if (types == null) {
			types = new SearchtermType[0];
		}

		Session session = getSession();

		String q = "from SearchtermValue termVal " +
			"left join fetch termVal.searchtermSns ";

		String hqlToken = "where ";
		for (SearchtermType type : types) {
			q += hqlToken + "termVal.type = '" + type.getDbValue() + "' ";
			hqlToken = "OR ";
		}
		
		return  session.createQuery(q).list();
	}
}
