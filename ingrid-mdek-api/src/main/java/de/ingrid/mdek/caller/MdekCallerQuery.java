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
public class MdekCallerQuery extends MdekCallerAbstract implements IMdekCallerQuery {

	private final static Logger log = Logger.getLogger(MdekCallerQuery.class);

	private static IMdekCallerQuery myInstance;
	IMdekCaller mdekCaller;

	// Jobs
	static String MDEK_IDC_QUERY_JOB_ID = "de.ingrid.mdek.job.MdekIdcQueryJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerQuery(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerQuery() {}

    private MdekCallerQuery(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
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
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.SEARCH_TERM, searchTerm);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("queryAddressesFullText", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryAddressesThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.TERM_SNS_ID, termSnsId);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("queryAddressesThesaurusTerm", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsFullText(String plugId, String searchTerm,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.SEARCH_TERM, searchTerm);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("queryObjectsFullText", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsThesaurusTerm(String plugId, String termSnsId,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);

		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.TERM_SNS_ID, termSnsId);
		
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("queryObjectsThesaurusTerm", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryHQL(String plugId, String hqlQuery,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);
		jobParams.put(MdekKeys.HQL_QUERY, hqlQuery);
		
		List jobMethods = mdekCaller.setUpJobMethod("queryHQL", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryHQLToCsv(String plugId, String hqlQuery,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.HQL_QUERY, hqlQuery);
		
		List jobMethods = mdekCaller.setUpJobMethod("queryHQLToCsv", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryObjectsExtended(String plugId, IngridDocument searchParams,
			int startHit, int numHits, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);

		List jobMethods = mdekCaller.setUpJobMethod("queryObjectsExtended", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}

	public IngridDocument queryAddressesExtended(String plugId, IngridDocument searchParams,
			int startHit, int numHits, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		
		List jobMethods = mdekCaller.setUpJobMethod("queryAddressesExtended", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_QUERY_JOB_ID, jobMethods);
	}
	
}
