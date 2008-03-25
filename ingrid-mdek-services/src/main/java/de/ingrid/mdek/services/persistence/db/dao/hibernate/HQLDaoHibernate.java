package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.TransactionService;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
import de.ingrid.mdek.services.utils.csv.ExcelCSVPrinter;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public class HQLDaoHibernate
	extends TransactionService
	implements IHQLDao {

	private static final Logger LOG = Logger.getLogger(HQLDaoHibernate.class);

	/** Value: IdcEntityType */
	private static String KEY_ENTITY_TYPE = "_ENTITY_TYPE";
	/** Value: String */
	private static String KEY_ENTITY_ALIAS = "_ENTITY_ALIAS";
	/** Value: Integer */
	private static String KEY_FROM_START_INDEX = "_FROM_START_INDEX";

	public HQLDaoHibernate(SessionFactory factory) {
        super(factory);
    }

	public long queryHQLTotalNum(String hqlQuery) {

		IngridDocument hqlDoc = preprocessHQL(hqlQuery);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);

		if (qString == null) {
			return 0;
		}

		String qStringFrom = qString.substring(fromStartIndex);
		
		if (entityAlias == null) {
			qString = "select count(*) "
				+ qStringFrom;			
		} else {
			qString = "select count(distinct "
				+ entityAlias + ") "
				+ qStringFrom;
		}

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public IngridDocument queryHQL(String hqlQuery,
			int startHit, int numHits) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		IdcEntityType entityType = (IdcEntityType) hqlDoc.get(KEY_ENTITY_TYPE);
		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);

		List entityList = new ArrayList();
		if (qString != null) {
			
			String qStringFrom = qString.substring(fromStartIndex);

			if (entityAlias != null) {
				qString = "select distinct "
					+ entityAlias + " " 
					+ qStringFrom;
			}
			
			Session session = getSession();

			entityList = session.createQuery(qString)
				.setFirstResult(startHit)				
				.setMaxResults(numHits)				
				.list();
		}

		IngridDocument result = new IngridDocument();
		if (entityType == IdcEntityType.OBJECT) {
			result.put(MdekKeys.OBJ_ENTITIES, entityList);
		} else {
			result.put(MdekKeys.ADR_ENTITIES, entityList);
		}

		return result;
	}

	public IngridDocument queryHQLToCsv(String hqlQuery) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);
//		IdcEntityType entityType = (IdcEntityType) hqlDoc.get(KEY_ENTITY_TYPE);
//		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);

		List hits = new ArrayList();
		if (qString != null) {
			Session session = getSession();
			hits = session.createQuery(qString)
				.list();
		}

		// our csv writer !
		StringWriter sw = new StringWriter();
		ExcelCSVPrinter ecsvp = new ExcelCSVPrinter(sw);

		// titles
		String[] titles = extractCsvTitles(qString.substring(0, fromStartIndex));
		ecsvp.println(titles);

		for (Object hit : hits) {
			String[] csvValues = extractCsvValues(hit);
			ecsvp.println(csvValues);
		}

		try {
			ecsvp.close();			
		} catch (Exception ex) {
			LOG.error("Problems closing ExcelCSVPrinter !", ex);
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.CSV_RESULT, sw.toString());
		result.put(MdekKeys.SEARCH_TOTAL_NUM_HITS, new Integer(hits.size()).longValue());

		return result;
	}
	
	/**
	 * Process HQL query string for querying entities (objects or addresses).
	 * Performs checks etc.
	 */
	private IngridDocument preprocessHQL(String hqlQuery) {
		IngridDocument result = new IngridDocument();
		
		hqlQuery = MdekUtils.processStringParameter(hqlQuery);
		if (hqlQuery == null) {
			return result;
		}

		// check DML statements !
		// ----------
		String hqlUPPERCASE = hqlQuery.toUpperCase();
		String[] notAllowed_UPPERCASE = new String[] {
			"DELETE", "UPDATE", "INSERT"
		};
		for (String noGo : notAllowed_UPPERCASE) {
			if (hqlUPPERCASE.indexOf(noGo) != -1) {
				throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
			}
		}

		// check "fetch"
		// ----------
		// fetch not allowed ! Problems with select count()
		if (hqlUPPERCASE.indexOf("JOIN FETCH") != -1) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));			
		}

		// check which entity
		// ----------
		IdcEntityType entityType = null;
		Integer fromStartIndex = hqlUPPERCASE.indexOf("FROM OBJECTNODE");
		if (fromStartIndex != -1) {
			entityType = IdcEntityType.OBJECT;
		}
		Integer fromAddrStartIndex = hqlUPPERCASE.indexOf("FROM ADDRESSNODE");
		if (fromAddrStartIndex != -1) {
			if (entityType != null) {
				throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
			}
			entityType = IdcEntityType.ADDRESS;
			fromStartIndex = fromAddrStartIndex;
		}
		// wrong entity queried ?
		if (entityType == null) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
		}

		// determine entity alias!
		// ----------
		String entityAlias = extractEntityAlias(hqlQuery.substring(fromStartIndex));
		
		result.put(KEY_ENTITY_TYPE, entityType);
		result.put(KEY_ENTITY_ALIAS, entityAlias);
		result.put(KEY_FROM_START_INDEX, fromStartIndex);
		result.put(MdekKeys.HQL_QUERY, hqlQuery);

		return result;
	}

	private String extractEntityAlias(String hqlFromClause) {
		String entityAlias = null;

		String[] tokens = hqlFromClause.split(" ");
		int numTokens = 0;
		boolean aliasLongVersion = false;
		for (String token : tokens) {
			// skip empty strings (when multiple " " in a row)
			if (token.length() == 0) {
				continue;
			}
			numTokens++;
			if (numTokens == 3) {
				entityAlias = token;
				if (entityAlias.toUpperCase().equals("AS")) {
					entityAlias = null;
					aliasLongVersion = true;
					continue;
				}
			}
			// take next token after AS
			if (aliasLongVersion) {
				entityAlias = token;
			}
			if (entityAlias != null) {
				break;
			}
		}
		
		return entityAlias;
	}

	private String[] extractCsvTitles(String hqlSelectClause) {
		ArrayList<String> titles = new ArrayList<String>();

		String[] tokens = hqlSelectClause.split(",");
		for (String token : tokens) {
			token = token.trim();
			String tokenUpp = token.toUpperCase();
			if (tokenUpp.startsWith("SELECT") ||
				tokenUpp.startsWith("DISTINCT")) {
				int startIndex = token.lastIndexOf(" ");
				token = token.substring(startIndex+1);
			}
			titles.add(token);
		}

		return titles.toArray(new String[titles.size()]);
	}

	private String[] extractCsvValues(Object hit) {
		ArrayList<String> values = new ArrayList<String>();
		
		if (hit instanceof Object[]) {
			Object[] objs = (Object[]) hit;
			for (Object obj : objs) {
				appendCsvValues(obj, values);
			}

		} else {
			appendCsvValues(hit, values);
		}
		
		return values.toArray(new String[values.size()]);
	}

	private void appendCsvValues(Object resultObject, ArrayList<String> values) {
		if (resultObject instanceof IEntity) {
			// TODO: IEntity fuer CSV aufloesen !
			values.add("" + resultObject);
			
		} else {
			values.add("" + resultObject);
		}
	}
}
