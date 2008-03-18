package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.ITransactionService;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public interface IHQLDao
	extends ITransactionService {

	/** Get total number of entities queried by the passed hql query.
	 * @param hqlQuery arbitrary hql query ! ONLY READS !
	 * @return number of found entities
	 */
	long queryHQLTotalNum(String hqlQuery);

	/**
	 * Query entities with the passed hql query.
	 * @param hqlQuery arbitrary hql query ! ONLY READS !
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return IngridDocument containing results and additional data
	 * 		(queried entity type etc.)
	 */
	IngridDocument queryHQL(String hqlQuery,
			int startHit, int numHits);
}
