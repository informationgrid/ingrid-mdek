package de.ingrid.mdek.caller;

import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning QUERY functionality (inquiry of entities).
 */
public interface IMdekCallerQuery {

	/**
	 * Search addresses via full text search.
	 * @param plugId which mdek server (iplug)
	 * @param searchTerm term to search for (arbitrary string)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryAddressesFullText(String plugId, String searchTerm,
			int startHit, int numHits,
			String userId);

	/**
	 * Search addresses via thesaurus term.
	 * @param plugId which mdek server (iplug)
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryAddressesThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId);

	/**
	 * Search objects via full text search.
	 * @param plugId which mdek server (iplug)
	 * @param searchTerm term to search for (arbitrary string)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryObjectsFullText(String plugId, String searchTerm,
			int startHit, int numHits,
			String userId);

	/**
	 * Search objects via thesaurus term.
	 * @param plugId which mdek server (iplug)
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return response containing result: map containing hits and additional
	 * info (total number of hits)
	 */
	IngridDocument queryObjectsThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId);

	/**
	 * Execute HQL Query fetching objects/addresses.
	 * @param plugId which mdek server (iplug)
	 * @param hqlQuery hql query ! NO UPDATE !
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return
	 */
	IngridDocument queryHQL(String plugId, String hqlQuery,
			int startHit, int numHits,
			String userId);

	/**
	 * Execute HQL Query fetching objects/addresses and "export" data to csv.
	 * @param plugId which mdek server (iplug)
	 * @param hqlQuery hql query ! NO UPDATE !
	 * @param userId
	 * @return
	 */
	IngridDocument queryHQLToCsv(String plugId, String hqlQuery,
			String userId);

	/**
	 * Search objects according to the searchParams supplied (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * 
	 * @param plugId which mdek server (iplug)
	 * @param searchParams The search params (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return
	 */
	IngridDocument queryObjectsExtended(String plugId, IngridDocument searchParams, int startHit, int numHits, String userId);

	/**
	 * Search addresses according to the searchParams supplied (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * 
	 * @param plugId which mdek server (iplug)
	 * @param searchParams The search params (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @param userId
	 * @return
	 */
	IngridDocument queryAddressesExtended(String plugId, IngridDocument searchParams, int startHit, int numHits, String userId);

}
