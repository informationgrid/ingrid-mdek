package de.ingrid.mdek.caller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing basic central methods to communicate with the Mdek backend (jobs).
 * SINGLETON BECAUSE MdekClient IS SINGLETON AND THIS IS FACADE TO MdekClient.
 * 
 * @author Martin
 */
public class MdekClientCaller implements IMdekClientCaller {

	private final static Logger log = Logger.getLogger(MdekClientCaller.class);

	private static MdekClientCaller myInstance;
	
	/** Interface to backend */
	private static MdekClient client;
	
	private static String CACHE_CONFIG_FILE = "/ehcache-ige-api.xml";
	private static CacheManager cacheManager;
	private static Cache jobRepoCache;

	// Jobs
	// TODO: better create separate job for handling job synchronization (at the moment we use object job !)
	private static String MDEK_IDC_SYNC_JOB_ID = "de.ingrid.mdek.job.MdekIdcObjectJob";
	private static String MDEK_IDC_VERSION_JOB_ID = "de.ingrid.mdek.job.MdekIdcObjectJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !
	 * To instantiate new instance shutdown() has to be called !
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(File communicationProperties) {
		if (myInstance == null) {
			myInstance = new MdekClientCaller(communicationProperties);
		}
	}

    private MdekClientCaller() {}

    private MdekClientCaller(File communicationProperties) {
        try {
    		// First set up cache for use !
    		URL url = getClass().getResource(CACHE_CONFIG_FILE);
    		cacheManager = new CacheManager(url);
    		jobRepoCache = cacheManager.getCache("mdekJobRepoCache");
 
    		// instantiate client (ibus)
    		client = MdekClient.getInstance(communicationProperties);
    		Thread.sleep(2000);

        	// explicit registration of jobs if not persistent !
//    		registerJob(MDEK_JOB_ID, MDEK_JOB_XML);

        } catch (Throwable t) {
        	log.fatal("Error initiating the Mdek interface.", t);
        }
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekClientCaller getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE MdekCaller Instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public static synchronized void shutdown() {
		if (myInstance == null) {
			return;
		}

        try {
        	// explicit deregistration of jobs if not persistent !
//        	myInstance.deregisterJob(MDEK_JOB_ID);

    		// shutdown client
    		client.shutdown();

    		// shutdown cache at last !
        	cacheManager.shutdown();

        } catch (Throwable t) {
        	log.error("Problems SHUTTING DOWN Mdek interface.", t);
        } finally {
        	myInstance = null;
        	client = null;
        }
	}

	public IngridDocument getVersion(String plugId) {
		// fetch version of mdek server !
		IngridDocument jobParams = new IngridDocument();
		List jobMethods = setUpJobMethod("getVersion", jobParams);
		IngridDocument response = callJob(plugId, MDEK_IDC_VERSION_JOB_ID, jobMethods);

		IngridDocument resultDoc = getResultFromResponse(response);

		// then version of api (in client)
		// NOTICE: has to have different property names !
		ResourceBundle resourceBundle = ResourceBundle.getBundle("mdek-api-version");   
		Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			resultDoc.put(key, resourceBundle.getObject(key));
		}

		return response;
	}

	public List<String> getRegisteredIPlugs() {
		return client.getRegisteredMdekServers();
	}

/*
	public IngridDocument testMdekEntity(int threadNumber) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.TITLE, "TEST obj_name");
		jobParams.put(MdekKeys.ABSTRACT, "TEST obj_descr");
		jobParams.put("THREAD_NUMBER", new Integer(threadNumber));
		List jobMethods = setUpJobMethod("testMdekEntity", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}
*/

	public List setUpJobMethod(String methodName, IngridDocument methodParams) {
		debugDocument("JobMethod: " + methodName + ", PARAMETERS:", methodParams);

		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair(methodName, methodParams));
		return methodList;
	}

	public IngridDocument callJob(String plugId, String jobId, List jobMethods) {
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, jobId);
		invokeDocument.put(IJobRepository.JOB_METHODS, jobMethods);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IJobRepositoryFacade jobRepo = getJobRepo(plugId);

		IngridDocument response = jobRepo.execute(invokeDocument);
		debugDocument("Job: " + jobId + ", RESPONSE:", response);
		
		return response;
	}

	public IngridDocument getRunningJobInfo(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getRunningJobInfo", jobParams);
		return callJob(plugId, MDEK_IDC_SYNC_JOB_ID, jobMethods);
	}

	public IngridDocument cancelRunningJob(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("cancelRunningJob", jobParams);
		return callJob(plugId, MDEK_IDC_SYNC_JOB_ID, jobMethods);
	}

	public IngridDocument getResultFromResponse(IngridDocument mdekResponse) {
		IngridDocument result = null;

		boolean success = mdekResponse.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS);
		if (success) {
			List pairList = (List) mdekResponse.get(IJobRepository.JOB_INVOKE_RESULTS);
			Pair pair = (Pair) pairList.get(0);
			result = (IngridDocument) pair.getValue();
			
			// NOTICE: we return an empty document if the called method has no return data
			// (but finished succesfully). So we avoid returning null WHICH INDICATES AN ERROR !
			if (result == null) {
				result = new IngridDocument();
			}
		}

		return result;
	}

	public List<MdekError> getErrorsFromResponse(IngridDocument mdekResponse) {
		return (List<MdekError>) mdekResponse.get(IJobRepository.JOB_INVOKE_ERROR_MDEK);
	}

	public String getErrorMsgFromResponse(IngridDocument mdekResponse) {
		int numErrorTypes = 4;
		String[] errMsgs = new String[numErrorTypes];

		errMsgs[0] = (String) mdekResponse.get(IJobRepository.JOB_REGISTER_ERROR_MESSAGE);
		errMsgs[1] = (String) mdekResponse.get(IJobRepository.JOB_INVOKE_ERROR_MESSAGE);
		errMsgs[2] = (String) mdekResponse.get(IJobRepository.JOB_COMMON_ERROR_MESSAGE);
		errMsgs[3] = (String) mdekResponse.get(IJobRepository.JOB_DEREGISTER_ERROR_MESSAGE);

		String retMsg = null;
		for (String errMsg : errMsgs) {
			if (errMsg != null) {
				if (retMsg == null) {
					retMsg = errMsg;
				} else {
					retMsg += "\n!!! Further Error !!!:\n" + errMsg;
				}
			}
		}
		
		return retMsg;
	}

	/**
	 * Get JobRepo for passed MdekServer. Uses cache !
	 */
	private IJobRepositoryFacade getJobRepo(String plugId) {
		IJobRepositoryFacade jobRepo;

		// get repo from cache
		Element elem = jobRepoCache.get(plugId);

		if (elem != null) {
			// repo in cache
			jobRepo = (IJobRepositoryFacade) elem.getObjectValue();

		} else {
			// no repo in cache
			// get it from client and put it to cache
			jobRepo = client.getJobRepositoryFacade(plugId);
			jobRepoCache.put(new Element(plugId, jobRepo));

			if (log.isDebugEnabled()) {
	        	log.debug("Fetching JobRepository from MdekServer: " + plugId);
			}
		}
		
		return jobRepo;
	}

	private IngridDocument registerJob(String plugId, String jobId, String jobXml) {
		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, jobId);
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		// ALWAYS PERSIST, will be loaded from persistent XML at startup, otherwise job not registered !
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IJobRepositoryFacade jobRepo = getJobRepo(plugId);

		IngridDocument response = jobRepo.execute(registerDocument);
		debugDocument("registerJob " + jobId + ": ", response);

		return response;
	}

	private IngridDocument deregisterJob(String plugId, String jobId) {
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument.put(IJobRepository.JOB_ID, jobId);
		deregisterDocument.put(IJobRepository.JOB_PERSIST, false);

		IJobRepositoryFacade jobRepo = getJobRepo(plugId);

		IngridDocument response = jobRepo.execute(deregisterDocument);
		debugDocument("deregisterJob " + jobId + ": ", response);

		return response;
	}

	private void debugDocument(String title, IngridDocument doc) {
		if (!log.isInfoEnabled()) {
			return;
		}

		if (title != null) {
			log.info(title);
		}
		if (doc != null) {
			int docLength = doc.toString().length();
			log.info("IngridDocument length: " + docLength);
		}

		if (!log.isDebugEnabled()) {
			return;
		}

		log.debug("IngridDocument: " + doc);			
	}
}
