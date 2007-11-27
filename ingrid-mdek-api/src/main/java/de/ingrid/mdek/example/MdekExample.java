package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.job.MdekKeys;
import de.ingrid.mdek.job.MdekValues;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

public class MdekExample {

	// NOTICE: our own JOB_ID !!! so we don't need mdek server lib !
	public static String MDEK_JOB_ID = "de.ingrid.mdek.job.MdekTreeJob";

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties> [--threads 1]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() < 1) {
			printUsage();
		}

		MdekClient client = MdekClient.getInstance(new File((String) map
				.get("--descriptor")));

		Thread.sleep(2000);
		IJobRepositoryFacade jobRepositoryFacade = client
				.getJobRepositoryFacade();

		// Job Parameters and other data
		System.out.println("\n###### PARAMS ######");
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}
		System.out.println("THREADS: " + numThreads);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
			+ "<beans>"
			+ "<bean id=\"" + MDEK_JOB_ID + "\" class=\"de.ingrid.mdek.job.MdekTreeJob\" >"
			+ "<constructor-arg ref=\"de.ingrid.mdek.services.log.ILogService\"/>"
			+ "<constructor-arg ref=\"sessionFactory\"/>"
			+ "</bean>"
			+ "</beans>";

		// register job
		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, MDEK_JOB_ID);
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### REGISTER ######");
		IngridDocument response = jobRepositoryFacade.execute(registerDocument);
		debugDocument("RESPONSE", response);

		System.out.println("\n###### OUTPUT THREADS ######\n");

		// threads calling job
		MdekThread[] threads = new MdekThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekThread(i+1, jobRepositoryFacade);
		}
		// fire
		for (int i=0; i<numThreads; i++) {
			threads[i].start();
		}

		// wait till all threads are finished
		boolean threadsFinished = false;
		while (!threadsFinished) {
			threadsFinished = true;
			for (int i=0; i<numThreads; i++) {
				if (threads[i].isRunning()) {
					threadsFinished = false;
					Thread.sleep(500);
					break;
				}
			}
		}

		// deregister job
		System.out.println("\n###### DEREGISTER ######");
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument.put(IJobRepository.JOB_ID, MDEK_JOB_ID);
		deregisterDocument.put(IJobRepository.JOB_PERSIST, false);
		IngridDocument deregisterResponse = jobRepositoryFacade.execute(deregisterDocument);
		debugDocument("RESPONSE", deregisterResponse);

		client.shutdown();
	}
	
	public static void debugDocument(String title, IngridDocument doc) {
		if (title != null) {
			System.out.println(title);						
		}
		int docLength = doc.toString().length();
		System.out.println("IngridDocument length: " + docLength);			
//		if (docLength < 2000)  {
			System.out.println("IngridDocument: " + doc);			
//		}		
	}
}

class MdekThread extends Thread {

	private int threadNumber;
	private IJobRepositoryFacade jobRepositoryFacade;
	
	private boolean isRunning = false;

	public MdekThread(int threadNumber,
			IJobRepositoryFacade jobFacade)
	{
		this.threadNumber = threadNumber;
		this.jobRepositoryFacade = jobFacade;
	}

	public void run() {
		isRunning = true;

		//testMdekEntity(threadNumber);
		getSubTree();

		isRunning = false;
	}
	
	private void testMdekEntity(int threadNumber) {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		ArrayList<Pair> methodList = new ArrayList<Pair>();
		IngridDocument inputDocument = new IngridDocument();
		inputDocument.put(MdekKeys.ENTITY_TYPE, MdekValues.ENTITY_TYPE_OBJECT);
		inputDocument.put(MdekKeys.ENTITY_NAME, "TEST obj_name");
		inputDocument.put(MdekKeys.ENTITY_DESCRIPTION, "TEST obj_descr");
		inputDocument.put("THREAD_NUMBER", new Integer(threadNumber));
		methodList.add(new Pair("testMdekEntity", inputDocument));

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, MdekExample.MDEK_JOB_ID);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE testMdekObject ######");
		MdekExample.debugDocument("PARAMETERS:", inputDocument);

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		MdekExample.debugDocument("RESPONSE:", invokeResponse);

		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
	}

	private void getSubTree() {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		ArrayList<Pair> methodList = new ArrayList<Pair>();
		IngridDocument inputDocument = new IngridDocument();
		inputDocument.put(MdekKeys.ENTITY_UUID, "FB5D7527-8331-4870-9CE0-B8BDF9DAB619");
		inputDocument.put(MdekKeys.DEPTH, new Integer(1));
		inputDocument.put(MdekKeys.ENTITY_TYPE, MdekValues.ENTITY_TYPE_OBJECT);
		methodList.add(new Pair("getSubTree", inputDocument));

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, MdekExample.MDEK_JOB_ID);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE getSubTree ######");
		MdekExample.debugDocument("PARAMETERS:", inputDocument);

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		MdekExample.debugDocument("RESPONSE:", invokeResponse);

		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
