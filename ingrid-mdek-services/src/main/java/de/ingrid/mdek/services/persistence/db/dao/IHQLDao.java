/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
	 * @param allowQueryDirectInstances false=only nodes allowed in FROM clause (FROM OBJECTNODE, FROM ADDRESSNODE)<br>
	 * 		true=also direct instances allowed in FROM clause (FROM T01Object, FROM T02Address)
	 * @return IngridDocument containing ZIPPED csv records and NUM records
	 */
	IngridDocument queryHQLToCsv(String hqlQuery, boolean allowQueryDirectInstances);

	/**
	 * Query entities with the passed hql query and return entity data IN A MAP.
	 * @param maxNumHits maximum number of hits to query, pass null if all hits !
	 * @return IngridDocument containing results as List of IngridDocuments. In these docs
	 * 		the select attributes are keys to the values (all Strings).
	 */
	IngridDocument queryHQLToMap(String hqlQuery, Integer maxNumHits);
}
