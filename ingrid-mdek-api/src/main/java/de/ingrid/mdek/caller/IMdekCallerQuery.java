/*
 * **************************************************-
 * ingrid-mdek-api
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
package de.ingrid.mdek.caller;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

import java.util.List;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning QUERY functionality (inquiry of entities).
 */
public interface IMdekCallerQuery extends IMdekCaller {

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
	 * @return response containing result: map containing csv data
	 */
	IngridDocument queryHQLToCsv(String plugId, String hqlQuery,
			String userId);

	/**
	 * Execute HQL Query fetching object/address data and return data IN A MAP !
	 * @param plugId which mdek server (iplug)
	 * @param hqlQuery hql query ! NO UPDATE !
	 * @param maxNumHits maximum number of hits to query, pass null if all hits !
	 * @param userId
	 * @return IngridDocument containing results as List of IngridDocuments. In these docs
	 * 		the select attributes are keys to the values.
	 */
	IngridDocument queryHQLToMap(String plugId, String hqlQuery, Integer maxNumHits,
			String userId);

	/**
	 * Execute any HQL which can modify the database.
	 * @param plugId which mdek server (iplug)
	 * @param hqlQueries is a combination of modifier (HQL_SELECT, HQL_UPDATE, HQL_DELETE) and hql-query
	 * @return
	 */
	IngridDocument updateByHQL(String plugId, List<Pair> hqlQueries);

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
