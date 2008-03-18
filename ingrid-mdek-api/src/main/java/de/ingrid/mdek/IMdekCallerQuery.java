package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning QUERY functionality (inquiry of entities).
 */
public interface IMdekCallerQuery {

	/**
	 * Search addresses via thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits,
			String userId);

	/**
	 * Search objects via thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits,
			String userId);

	/**
	 * Execute HQL Query fetching objects/addresses.
	 * @param hqlQuery hql query ! NO UPDATE !
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return
	 */
	IngridDocument queryHQL(String hqlQuery,
			int startHit, int numHits,
			String userId);
}
