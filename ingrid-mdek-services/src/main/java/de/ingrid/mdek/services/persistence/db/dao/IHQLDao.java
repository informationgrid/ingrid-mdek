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

	/**
	 * Query entities with the passed hql query and return data as csv
	 * @param hqlQuery arbitrary hql query ! ONLY READS !
	 * @return IngridDocument containing csv records
	 */
	IngridDocument queryHQLToCsv(String hqlQuery);

	/**
	 * Query entities with the passed hql query and return entity data IN A MAP.
	 * @param maxNumHits maximum number of hits to query, pass null if all hits !
	 * @return IngridDocument containing results as List of IngridDocuments. In these docs
	 * 		the select attributes are keys to the values (all Strings).
	 */
	IngridDocument queryHQLToMap(String hqlQuery, Integer maxNumHits);
}
