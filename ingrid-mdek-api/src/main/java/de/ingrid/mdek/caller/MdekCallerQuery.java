/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning QUERY functionality (inquiry of entities).
 * 
 * @author Martin
 */
public class MdekCallerQuery extends MdekCaller implements IMdekCallerQuery {

	private final static Logger log = Logger.getLogger(MdekCallerQuery.class);

	private static IMdekCallerQuery myInstance;

	// Jobs
	static String MDEK_IDC_QUERY_JOB_ID = "de.ingrid.mdek.job.MdekIdcQueryJob";

    private MdekCallerQuery(IMdekClientCaller mdekClientCaller) {
    	super(mdekClientCaller);
    }

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 */
	public static synchronized void initialize(IMdekClientCaller mdekClientCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerQuery(mdekClientCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static IMdekCallerQuery getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerQuery.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument queryAddressesFullText(String plugId, String searchTerm,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.SEARCH_TERM, searchTerm);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = setUpJobMethod("queryAddressesFullText", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryAddressesThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.TERM_SNS_ID, termSnsId);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = setUpJobMethod("queryAddressesThesaurusTerm", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsFullText(String plugId, String searchTerm,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.SEARCH_TERM, searchTerm);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = setUpJobMethod("queryObjectsFullText", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.TERM_SNS_ID, termSnsId);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = setUpJobMethod("queryObjectsThesaurusTerm", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryHQL(String plugId, String hqlQuery,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));
		jobParams.put(MdekKeys.HQL_QUERY, hqlQuery);
		
		List jobMethods = setUpJobMethod("queryHQL", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryHQLToCsv(String plugId, String hqlQuery,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.HQL_QUERY, hqlQuery);
		
		List jobMethods = setUpJobMethod("queryHQLToCsv", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryHQLToMap(String plugId, String hqlQuery, Integer maxNumHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		if (maxNumHits != null) {
			jobParams.put(MdekKeys.TOTAL_NUM, new Long(maxNumHits));
		}
		jobParams.put(MdekKeys.HQL_QUERY, hqlQuery);
		
		List jobMethods = setUpJobMethod("queryHQLToMap", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsExtended(String plugId, IngridDocument searchParams,
			int startHit, int numHits, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));
		jobParams.put(MdekKeys.SEARCH_EXT_PARAMS, searchParams);

		List jobMethods = setUpJobMethod("queryObjectsExtended", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryAddressesExtended(String plugId, IngridDocument searchParams,
			int startHit, int numHits, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));
		jobParams.put(MdekKeys.SEARCH_EXT_PARAMS, searchParams);
		
		List jobMethods = setUpJobMethod("queryAddressesExtended", jobParams);

		return callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}
	
}
