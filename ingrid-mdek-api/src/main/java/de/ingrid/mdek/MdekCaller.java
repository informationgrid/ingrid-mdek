package de.ingrid.mdek;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekErrors.MdekError;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend.
 * 
 * @author Martin
 */
public class MdekCaller implements IMdekCaller {

	private final static Logger log = Logger.getLogger(MdekCaller.class);

	private static MdekCaller myInstance;
	
	private static MdekClient client;
	private static IJobRepositoryFacade jobRepo;

	// Jobs
	private static String MDEK_IDC_JOB_ID = "de.ingrid.mdek.job.MdekIdcJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(File communicationProperties) {
		if (myInstance != null) {
			log.warn("MULTIPLE initialization of MdekCaller !!!");
		}

		myInstance = new MdekCaller(communicationProperties);
	}

    private MdekCaller() {};

    private MdekCaller(File communicationProperties) {
        try {
    		// instantiate client
    		client = MdekClient.getInstance(communicationProperties);
    		Thread.sleep(2000);

        	jobRepo = client.getJobRepositoryFacade();

        	// explicit registration of jobs if not persistent !
//    		registerJob(MDEK_JOB_ID, MDEK_JOB_XML);

        } catch (Throwable t) {
        	log.fatal("Error initiating the Mdek interface.", t);
        }
    };

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static IMdekCaller getInstance() {
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

        } catch (Throwable t) {
        	log.error("Problems SHUTTING DOWN Mdek interface.", t);
        } finally {
        	myInstance = null;
        	client = null;
        	jobRepo = null;
        }
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

	public IngridDocument getSysLists(Integer[] listIds, Integer langCode) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_LIST_IDS, listIds);
		jobParams.put(MdekKeys.LANGUAGE_CODE, langCode);
		List jobMethods = setUpJobMethod("getSysLists", jobParams);
		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument fetchObject(String uuid, Quantity howMuch) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		if (howMuch == Quantity.DETAIL_ENTITY) {
			List jobMethods = setUpJobMethod("getObjDetails", jobParams);
			return callJob(MDEK_IDC_JOB_ID, jobMethods);
		}
		
		// TODO implement other quantities of fetching object ?
		return new IngridDocument();
	}

	public IngridDocument storeObject(IngridDocument objDoc, boolean refetchAfterStore) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		List jobMethods = setUpJobMethod("storeObject", objDoc);
		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument publishObject(IngridDocument objDoc, boolean refetchAfterStore) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		List jobMethods = setUpJobMethod("publishObject", objDoc);
		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObjectWorkingCopy(String uuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		List jobMethods = setUpJobMethod("deleteObjectWorkingCopy", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObject(String uuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		List jobMethods = setUpJobMethod("deleteObject", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopObjects() {
		List jobMethods = setUpJobMethod("getTopObjects", null);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubObjects(String objUuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, objUuid);
		List jobMethods = setUpJobMethod("getSubObjects", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectPath(String uuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		List jobMethods = setUpJobMethod("getObjectPath", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);		
	}

	public IngridDocument checkObjectSubTree(String uuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		List jobMethods = setUpJobMethod("checkObjectSubTree", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree)
	{
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_COPY_SUBTREE, copySubtree);
		List jobMethods = setUpJobMethod("copyObject", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument moveObject(String fromUuid, String toUuid, boolean performCheck)
	{
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_PERFORM_CHECK, performCheck);
		List jobMethods = setUpJobMethod("moveObject", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialObject(IngridDocument newBasicObject) {
		IngridDocument jobParams = newBasicObject;
		List jobMethods = setUpJobMethod("getInitialObject", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}


/*
	public IngridDocument fetchTopAddresses() {
		List jobMethods = setUpJobMethod("getTopAddresses", null);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubAddresses(String adrUuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, adrUuid);
		List jobMethods = setUpJobMethod("getSubAddresses", jobParams);

		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}
*/

	public IngridDocument getResultFromResponse(IngridDocument mdekResponse) {
		IngridDocument result = null;

		boolean success = mdekResponse.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS);
		if (success) {
			List pairList = (List) mdekResponse.get(IJobRepository.JOB_INVOKE_RESULTS);
			Pair pair = (Pair) pairList.get(0);
			result = (IngridDocument) pair.getValue();
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

	private IngridDocument registerJob(String jobId, String jobXml) {
		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, jobId);
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		IngridDocument response = jobRepo.execute(registerDocument);
		debugDocument("registerJob " + jobId + ": ", response);

		return response;
	}

	private IngridDocument deregisterJob(String jobId) {
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument.put(IJobRepository.JOB_ID, jobId);
		deregisterDocument.put(IJobRepository.JOB_PERSIST, false);
		IngridDocument response = jobRepo.execute(deregisterDocument);
		debugDocument("deregisterJob " + jobId + ": ", response);

		return response;
	}

	private List setUpJobMethod(String methodName, IngridDocument methodParams) {
		debugDocument("PARAMETERS:", methodParams);

		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair(methodName, methodParams));
		return methodList;
	}

	private IngridDocument callJob(String jobId, List jobMethods) {
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, jobId);
		invokeDocument.put(IJobRepository.JOB_METHODS, jobMethods);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IngridDocument response = jobRepo.execute(invokeDocument);
		debugDocument("RESPONSE:", response);
		
		return response;
	}

	private void debugDocument(String title, IngridDocument doc) {
		if (!log.isDebugEnabled()) {
			return;
		}

		if (title != null) {
			log.debug(title);
		}
		if (doc != null) {
			int docLength = doc.toString().length();
			log.debug("IngridDocument length: " + docLength);
		}

		log.debug("IngridDocument: " + doc);			
	}

	public IngridDocument fetchCatalog() {
		List jobMethods = setUpJobMethod("getCatalog", null);
		return callJob(MDEK_IDC_JOB_ID, jobMethods);
	}
}
