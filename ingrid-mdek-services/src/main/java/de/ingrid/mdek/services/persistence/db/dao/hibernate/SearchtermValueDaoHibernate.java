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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.hibernate.query.Query;
import org.hibernate.transform.ToListResultTransformer;

/**
 * Hibernate-specific implementation of the <tt>SearchtermValue</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 *
 * @author Martin
 */
public class SearchtermValueDaoHibernate
	extends GenericHibernateDao<SearchtermValue>
	implements  ISearchtermValueDao {

	private static final Logger LOG = LogManager.getLogger(SearchtermValueDaoHibernate.class);

    public SearchtermValueDaoHibernate(SessionFactory factory) {
        super(factory, SearchtermValue.class);
    }

	public SearchtermValue loadSearchterm(String type, String term, Integer entryId,
		Long searchtermSnsId,
		Long entityId, IdcEntityType entityType)
	{

//		if (LOG.isDebugEnabled()) {
//			LOG.debug("type: " + type + ", term: " + term + ", SearchtermSns_ID: " + searchtermSnsId);
//		}

		SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, type);

		SearchtermValue termValue = null;
		if (SearchtermType.FREI == termType) {
			if (entityType != null && entityId != null) {
				if (entityType == IdcEntityType.OBJECT) {
					termValue = loadFreiSearchtermObject(term, entityId);
				} else {
					termValue = loadFreiSearchtermAddress(term, entityId);
				}
			}

		} else if (SearchtermType.isThesaurusType(termType)) {
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
				"where termVal.type = ?1 " +
				// we have to use LIKE to work on Oracle ! can't compare CLOB (text) with =
				// NOTICE: term changed to VARCHAR(4000) on ORACLE ! but we keep CLOB Version, also works !
				"and termVal.term LIKE ?2 " +
				"and termObj.objId = ?3";

		Query q = session.createQuery(qString);
		q.setParameter(1, SearchtermType.FREI.getDbValue());
		q.setParameter(2, term);
		q.setParameter(3, objId, Long.class);

		SearchtermValue termValue = null;
		// we query list(), NOT uniqueResult() because mySQL doesn't differ between ss <-> ß, lower <-> uppercase ...
		// then we check all results in Java whether equal !
		List<SearchtermObj> termObjs = q.list();
		for (SearchtermObj termObj : termObjs) {
			if (term.equals(termObj.getSearchtermValue().getTerm())) {
				termValue = termObj.getSearchtermValue();
				break;
			}
		}

		return termValue;
	}

	/** Load Freien SearchtermValue according to given values. Returns null if not found. */
	private SearchtermValue loadFreiSearchtermAddress(String term, Long adrId) {
		Session session = getSession();

		String qString = "from SearchtermAdr termAdr " +
			"left join fetch termAdr.searchtermValue termVal " +
			"where termVal.type = ?1 " +
			// we have to use LIKE to work on Oracle ! can't compare CLOB (text) with =
			// NOTICE: term changed to VARCHAR(4000) on ORACLE ! but we keep CLOB Version, also works !
			"and termVal.term LIKE ?2 " +
			"and termAdr.adrId = ?3";

		Query q = session.createQuery(qString);
		q.setParameter(1, SearchtermType.FREI.getDbValue());
		q.setParameter(2, term);
		q.setParameter(3, adrId, Long.class);

		SearchtermValue termValue = null;
		// we query list(), NOT uniqueResult() because mySQL doesn't differ between ss <-> ß, lower <-> uppercase ...
		// then we check all results in Java whether equal !
		List<SearchtermAdr> termAdrs = q.list();
		for (SearchtermAdr termAdr : termAdrs) {
			if (term.equals(termAdr.getSearchtermValue().getTerm())) {
				termValue = termAdr.getSearchtermValue();
				break;
			}
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
			"and termVal.searchtermSnsId = " + searchtermSnsId;

		// we query list(), NOT uniqueResult() ! e.g. ST catalog has multiple imported
		// values ("Messdaten", "Meßdaten") refering to same searchtermSns. Comparison
		// of these names in MySQL equals true, due to configuration of MySQL !
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

	public SearchtermValue loadOrCreate(String type,
			String term, String alternateTerm,
			Integer entryId,
			SearchtermSns termSns,
			Long entityId, IdcEntityType entityType)
	{
		Long termSnsId = (termSns != null) ? termSns.getId() : null;
		SearchtermValue termValue = loadSearchterm(type, term, entryId, termSnsId,
			entityId, entityType);

		if (termValue == null) {
			termValue = new SearchtermValue();
		}

		// update with newest values
		termValue.setType(type);
		termValue.setTerm(term);
		termValue.setAlternateTerm(alternateTerm);
		termValue.setEntryId(entryId);
		termValue.setSearchtermSns(termSns);
		termValue.setSearchtermSnsId(termSnsId);
		makePersistent(termValue);

		return termValue;
	}

	public List<SearchtermValue> getSearchtermValues(SearchtermType type, String term,
			String snsId) {
		Session session = getSession();
		List<SearchtermValue> retList = null;

		String q = "from SearchtermValue termVal ";
		if (SearchtermType.isThesaurusType(type)) {
			q += "left join fetch termVal.searchtermSns termSns ";
		}
		q += "where termVal.type = '" + type.getDbValue() + "' ";

		if (type == SearchtermType.FREI) {
			// we have to use LIKE to work on Oracle ! can't compare CLOB (text) with =
			// NOTICE: term changed to VARCHAR(4000) on ORACLE ! but we keep CLOB Version, also works !
			q += "and termVal.term LIKE '" + term + "'";
			// NOTICE: we query MULTIPLE values !
			retList = session.createQuery(q).list();

		} else if (SearchtermType.isThesaurusType(type)) {
			q += "and termSns.snsId = '" + snsId + "'";
/*
			// NOTICE: we query SINGLE value ! Has to be unique !
			retList = new ArrayList<SearchtermValue>();
			SearchtermValue termValue = (SearchtermValue) session.createQuery(q).uniqueResult();
			if (termValue != null) {
				retList.add(termValue);
			}
*/
			// we query list(), NOT uniqueResult() ! e.g. ST catalog has multiple imported
			// values ("Messdaten", "Meßdaten") refering to same searchtermSns. Comparison
			// of these names in MySQL equals true, due to configuration of MySQL !
			retList = session.createQuery(q).list();
		}

		return retList;
	}

	public List<SearchtermValue> getSearchtermValues(SearchtermType[] types) {
		if (types == null) {
			types = new SearchtermType[0];
		}

		Session session = getSession();

		// create where clause
		String whereClause = "";
		String hqlToken = "where ";
		for (SearchtermType type : types) {
			whereClause += hqlToken + "termVal.type = '" + type.getDbValue() + "' ";
			hqlToken = "OR ";
		}

		// fetch all terms referenced by Objects !
		String q = "select termVal " +
			"from SearchtermObj termObj " +
			"inner join termObj.searchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +
			whereClause;
		List<SearchtermValue> terms = session.createQuery(q)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();

		// fetch all terms referenced by Addresses and add to list
		q = "select termVal " +
			"from SearchtermAdr termAdr " +
			"inner join termAdr.searchtermValue termVal " +
			"left join fetch termVal.searchtermSns " +
			whereClause;
		List<SearchtermValue> addrTerms = session.createQuery(q)
			.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();
		terms.addAll(addrTerms);

		// set up result list, remove duplicate SearchtermValues
		List<SearchtermValue> resultList = new ArrayList<SearchtermValue>();
		Set<Long> addedIds = new HashSet<Long>();
		for (SearchtermValue term : terms) {
			if (!addedIds.contains(term.getId())) {
				addedIds.add(term.getId());
				resultList.add(term);
			}
		}

		return resultList;
	}

	public long countObjectsOfSearchterm(long termId) {
		String q = "select count(distinct termObj) " +
			"from SearchtermObj termObj " +
			"where termObj.searchtermId = " + termId;

		return (Long) getSession().createQuery(q).uniqueResult();
	}
	public long countAddressesOfSearchterm(long termId) {
		String q = "select count(distinct termAdr) " +
			"from SearchtermAdr termAdr " +
			"where termAdr.searchtermId = " + termId;

		return (Long) getSession().createQuery(q).uniqueResult();
	}

	public List<SearchtermObj> getSearchtermObjs(long searchtermValueId) {
		String q = "from SearchtermObj termObj " +
			"where termObj.searchtermId = " + searchtermValueId;

		return  getSession().createQuery(q).list();
	}
	public List<Long> getSearchtermObj_objIds(long searchtermValueId) {
		String q = "select distinct termObj.objId " +
			"from SearchtermObj termObj " +
			"where termObj.searchtermId = " + searchtermValueId;

		return  getSession().createQuery(q).list();
	}

	public List<SearchtermAdr> getSearchtermAdrs(long searchtermValueId) {
		String q = "from SearchtermAdr termAdr " +
			"where termAdr.searchtermId = " + searchtermValueId;

		return  getSession().createQuery(q).list();
	}
	public List<Long> getSearchtermAdr_adrIds(long searchtermValueId) {
		String q = "select distinct termAdr.adrId " +
			"from SearchtermAdr termAdr " +
			"where termAdr.searchtermId = " + searchtermValueId;

		return  getSession().createQuery(q).list();
	}
}
