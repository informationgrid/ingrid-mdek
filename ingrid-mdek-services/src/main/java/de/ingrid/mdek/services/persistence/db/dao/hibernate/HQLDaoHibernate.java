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

	private static String KEY_ENTITY_TYPE = "ENTITY_TYPE";

    public HQLDaoHibernate(SessionFactory factory) {
        super(factory);
    }

	public long queryHQLTotalNum(String hqlQuery) {

		IngridDocument hqlDoc = preprocessHQL(hqlQuery, true);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);

		if (qString == null) {
			return 0;
		}

		qString = "select count(*) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public IngridDocument queryHQL(String hqlQuery,
			int startHit, int numHits) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery, false);
		IdcEntityType entityType = (IdcEntityType) hqlDoc.get(KEY_ENTITY_TYPE);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);

		List entityList = new ArrayList();
		if (qString != null) {
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
	private IngridDocument preprocessHQL(String hqlQuery, boolean isCountQuery) {
		IngridDocument result = new IngridDocument();
		
		hqlQuery = MdekUtils.processStringParameter(hqlQuery);
		if (hqlQuery == null) {
			return result;
		}

		IdcEntityType entityType = null; 
		if (hqlQuery.startsWith("from ObjectNode")) {
			entityType = IdcEntityType.OBJECT;
		} else if (hqlQuery.startsWith("from AddressNode")) {
			entityType = IdcEntityType.ADDRESS;			
		}

		// wrong bean queried ?
		if (entityType == null) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
		}
		// fetch not allowed ! (we handle this ourselves !
		if (hqlQuery.indexOf("join fetch") != -1) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));			
		}

		if (!isCountQuery) {
			hqlQuery = hqlQuery.replace("join", "join fetch");
		}

		result.put(KEY_ENTITY_TYPE, entityType);
		result.put(MdekKeys.HQL_QUERY, hqlQuery);

		return result;
	}
}
