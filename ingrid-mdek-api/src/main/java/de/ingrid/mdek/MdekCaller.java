package de.ingrid.mdek;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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

	private static String MDEK_TREE_JOB_ID = "de.ingrid.mdek.job.MdekTreeJob";

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

//    		registerJob(MDEK_TREE_JOB_ID, MDEK_TREE_JOB_XML);

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
//        	myInstance.deregisterJob(MDEK_TREE_JOB_ID);

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
*/
	public IngridDocument testMdekEntity(int threadNumber) {
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		IngridDocument inputDocument = new IngridDocument();
		inputDocument.put(MdekKeys.TITLE, "TEST obj_name");
		inputDocument.put(MdekKeys.ABSTRACT, "TEST obj_descr");
		inputDocument.put("THREAD_NUMBER", new Integer(threadNumber));
		methodList.add(new Pair("testMdekEntity", inputDocument));

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, MDEK_TREE_JOB_ID);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		debugDocument("PARAMETERS:", inputDocument);

		IngridDocument response = jobRepo.execute(invokeDocument);
		debugDocument("RESPONSE:", response);

		return response;
	}

	public IngridDocument fetchSubObjects(String objUuid) {
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		IngridDocument inputDocument = new IngridDocument();
		inputDocument.put(MdekKeys.UUID, objUuid);
		methodList.add(new Pair("getSubObjects", inputDocument));
		debugDocument("PARAMETERS:", inputDocument);

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, MDEK_TREE_JOB_ID);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IngridDocument response = jobRepo.execute(invokeDocument);
		debugDocument("RESPONSE:", response);
		
		return response;
	}

	public IngridDocument fetchObjAddresses(String objUuid) {
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		IngridDocument inputDocument = new IngridDocument();
		inputDocument.put(MdekKeys.UUID, objUuid);
		methodList.add(new Pair("getObjAddresses", inputDocument));
		debugDocument("PARAMETERS:", inputDocument);

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, MDEK_TREE_JOB_ID);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IngridDocument response = jobRepo.execute(invokeDocument);
		debugDocument("RESPONSE:", response);
		
		return response;
	}

	private static void debugDocument(String title, IngridDocument doc) {
		if (!log.isDebugEnabled()) {
			return;
		}

		if (title != null) {
			log.debug(title);			
		}
		int docLength = doc.toString().length();
		log.debug("IngridDocument length: " + docLength);			
//		if (docLength < 2000)  {
			log.debug("IngridDocument: " + doc);			
//		}		
	}

	/**
	 * Get Result Document from Mdek response
	 * @param jobResponse response from mdek call
	 * @return null if errors occured otherwise IngridDocument containing results
	 */
	public static IngridDocument getResult(IngridDocument jobResponse) {
		IngridDocument result = null;

		boolean success = jobResponse.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS);
		if (success) {
			List pairList = (List) jobResponse.get(IJobRepository.JOB_INVOKE_RESULTS);
			Pair pair = (Pair) pairList.get(0);
			result = (IngridDocument) pair.getValue();
		}

		return result;
	}

	public static String getErrorMsg(IngridDocument jobResponse) {
		String errMsg = "NO ERROR MESSAGE !";
		
		boolean success = jobResponse.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS);
		if (!success) {
			errMsg = (String) jobResponse.get(IJobRepository.JOB_INVOKE_ERROR_MESSAGE);
		}

		return errMsg;
	}

}
