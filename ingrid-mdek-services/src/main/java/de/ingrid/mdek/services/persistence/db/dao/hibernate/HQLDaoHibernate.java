package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.TransactionService;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public class HQLDaoHibernate
	extends TransactionService
	implements IHQLDao {

	private static String KEY_ENTITY_TYPE = "_ENTITY_TYPE";
	private static String KEY_ENTITY_ALIAS = "_ENTITY_ALIAS";
	private static String KEY_FROM_START_INDEX = "_FROM_START_INDEX";

	public HQLDaoHibernate(SessionFactory factory) {
        super(factory);
    }

	public long queryHQLTotalNum(String hqlQuery) {

		IngridDocument hqlDoc = preprocessHQL(hqlQuery);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);

		if (qString == null) {
			return 0;
		}

		if (entityAlias == null) {
			qString = "select count(*) "
				+ qString;			
		} else {
			qString = "select count(distinct "
				+ entityAlias + ") "
				+ qString;
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

		List entityList = new ArrayList();
		if (qString != null) {
			
			if (entityAlias != null) {
				qString = "select distinct "
					+ entityAlias + " " 
					+ qString;
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
		String entityAlias = null;
		String[] tokens = hqlQuery.substring(fromStartIndex).split(" ");
		if (tokens.length > 2) {
			entityAlias = tokens[2];
			if (entityAlias.toUpperCase().equals("AS")) {
				if (tokens.length > 3) {
					entityAlias = tokens[3];					
				}
			}
		}
		
		result.put(KEY_ENTITY_TYPE, entityType);
		result.put(KEY_ENTITY_ALIAS, entityAlias);
		result.put(KEY_FROM_START_INDEX, fromStartIndex);
		result.put(MdekKeys.HQL_QUERY, hqlQuery);

		return result;
	}
}
