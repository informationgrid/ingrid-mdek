package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

public class BounceExample {

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties> [--wait 0] [--minlength 1000] [--maxlength 1000] [--delta 500] [--loops 1] [--threads 1]");
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
				.getJobRepositoryFacade("/101tec-group:101tec-mdek-server");

		// Job Parameters and other data
		System.out.println("\n###### PARAMS ######");
		Integer minDataLength = 1000;
		if (map.get("--minlength") != null) {
			minDataLength = new Integer((String) map.get("--minlength"));
			if (minDataLength <= 0) {
				minDataLength = 1000;
			}
		}
		Integer maxDataLength = 1000;
		if (map.get("--maxlength") != null) {
			maxDataLength = new Integer((String) map.get("--maxlength"));
			if (maxDataLength < minDataLength) {
				maxDataLength = minDataLength.intValue();
			}
		}
		Integer delta = 500;
		if (map.get("--delta") != null) {
			delta = new Integer((String) map.get("--delta"));
			if (delta <= 0) {
				delta = 500;
			}
		}
		Integer wait = 0;
		if (map.get("--wait") != null) {
			wait = new Integer((String) map.get("--wait"));
			if (wait < 0) {
				wait = 0;
			}
		}
		Integer loops = 1;
		if (map.get("--loops") != null) {
			loops = new Integer((String) map.get("--loops"));
			if (loops < 1) {
				loops = 1;
			}
		}
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}

		debugParams(minDataLength, maxDataLength, delta, wait, loops);
		System.out.println("THREADS: " + numThreads);

		// inject logger service
		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.example.BounceJob\" class=\"de.ingrid.mdek.example.BounceJob\" >"
				+ "<constructor-arg ref=\"de.ingrid.mdek.services.log.ILogService\"/></bean></beans>";

		// register job
		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### REGISTER ######");
		IngridDocument response = jobRepositoryFacade.execute(registerDocument);
		debugDocument("RESPONSE", response);

		System.out.println("\n\n###### OUTPUT THREADS ######\n\n");

		// threads calling job
		BounceThread[] bounceThreads = new BounceThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			bounceThreads[i] = new BounceThread(i, jobRepositoryFacade,
					minDataLength, maxDataLength, delta, wait, loops);
		}
		// fire
		for (int i=0; i<numThreads; i++) {
			bounceThreads[i].start();
		}

		// wait till all threads are finished
		boolean threadsFinished = false;
		while (!threadsFinished) {
			threadsFinished = true;
			for (int i=0; i<numThreads; i++) {
				if (bounceThreads[i].isRunning()) {
					threadsFinished = false;
					Thread.sleep(500);
					break;
				}
			}
		}

		// deregister job
		System.out.println("\n###### DEREGISTER ######");
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
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
		if (docLength < 500)  {
			System.out.println("IngridDocument: " + doc);			
		}		
	}

	public static void debugParams(Integer minLength, 
			Integer maxLength,
			Integer delta,
			Integer wait,
			Integer loops) {
		System.out.println("DATA minLength: " + minLength);
		System.out.println("DATA maxLength: " + maxLength);
		System.out.println("DATA delta: " + delta);
		System.out.println("WAIT sec: " + wait);
		System.out.println("LOOPS: " + loops);
	}
}

class BounceThread extends Thread {

	private Integer minLength; 
	private Integer maxLength;
	private Integer delta;
	private Integer wait;
	private Integer loops;
	private int threadNumber;
	private IJobRepositoryFacade jobRepositoryFacade;
	
	private boolean isRunning = false;

	public BounceThread(int threadNumber,
			IJobRepositoryFacade jobFacade,
			Integer minLength, 
			Integer maxLength,
			Integer delta,
			Integer wait,
			Integer loops)
	{
		this.threadNumber = threadNumber;
		this.jobRepositoryFacade = jobFacade;

		this.minLength = minLength;
		this.maxLength = maxLength;
		this.delta = delta;
		this.wait = wait;
		this.loops = loops;
	}

	public void run() {
		isRunning = true;

		// final results
		ArrayList finalLengths = new ArrayList(0);
		ArrayList finalLoopTimes1 = new ArrayList(0);
		ArrayList finalLoopTimes2 = new ArrayList(0);

		Integer dataLength = minLength;
		while (dataLength <= maxLength) {
			finalLengths.add(dataLength);
//			System.out.println("\n=== DATA length: " + dataLength + " ===\n");

			StringBuffer dataBuffer = new StringBuffer(dataLength);
			dataBuffer.setLength(dataLength);
			String data = dataBuffer.toString();

			int numLoops = 0;
			Long[] loopTimes1 = new Long[loops];
			Long[] loopTimes2 = new Long[loops];
			long startTime = 0;
			long endTime = 0;
			long neededTime = 0;

			while (numLoops < loops) {
				
//				System.out.println("\n--- LOOP" + (numLoops+1) + " ---\n");

				// set data in job and read back
				IngridDocument invokeDocument = new IngridDocument();
				invokeDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
				ArrayList<Pair> methodList = new ArrayList<Pair>();
				methodList.add(new Pair("data", data));
				methodList.add(new Pair("wait", wait));
				methodList.add(new Pair("getDataWithWait", null));
				invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
				invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
//				System.out.println("###### INVOKE setData, setWait, getDataWithWait ######");

				startTime = System.currentTimeMillis();
				IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
				endTime = System.currentTimeMillis();

				BounceExample.debugDocument("RESPONSE", invokeResponse);

				neededTime = endTime - startTime;
//				System.out.println("EXECUTION TIME: " + neededTime + " ms");
				loopTimes1[numLoops] = neededTime;

				// transmit data via Ingrid Document as Parameters and read back
				methodList.clear();
				invokeDocument = new IngridDocument();
				invokeDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
				IngridDocument inputDocument = new IngridDocument();
				inputDocument.put("data", data);
				inputDocument.put("wait", wait);
				methodList.add(new Pair("bounceDocWithWait", inputDocument));
				invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
				invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
//				System.out.println("###### INVOKE bounceDocWithWait ######");
//				BounceExample.debugDocument("INPUT:", inputDocument);

				startTime = System.currentTimeMillis();
				invokeResponse = jobRepositoryFacade.execute(invokeDocument);
				endTime = System.currentTimeMillis();

				BounceExample.debugDocument("RESPONSE:", invokeResponse);

				neededTime = endTime - startTime;
//				System.out.println("EXECUTION TIME: " + neededTime + " ms");
				loopTimes2[numLoops] = neededTime;

				numLoops++;
			}

			finalLoopTimes1.add(loopTimes1);
			finalLoopTimes2.add(loopTimes2);

			dataLength = dataLength + delta;
		}

		System.out.println("\n###### RESULTS THREAD " + threadNumber + " ######");
		BounceExample.debugParams(minLength, maxLength, delta, wait, loops);
		System.out.println("###### EXCEL OUTPUT START THREAD " + threadNumber + " ######");

		String DELIMITER = "\t";
		for (int i=0; i<finalLengths.size(); i++) {
			if (i==0) {
				System.out.print("Datengrösse" + DELIMITER);
			}
			// 2 Spalten je Datengroesse -> 2 unterschiedliche Server Calls
			System.out.print(finalLengths.get(i) + DELIMITER + DELIMITER);
		}
		System.out.print("\n");
		for (int i=0; i<loops; i++) {
			System.out.print("loop " + (i+1) + DELIMITER);
			for (int j=0; j<finalLengths.size(); j++) {
				System.out.print(((Long[]) finalLoopTimes1.get(j))[i] + DELIMITER);
				System.out.print(((Long[]) finalLoopTimes2.get(j))[i] + DELIMITER);
			}
			System.out.print("\n");
		}
		System.out.println("###### EXCEL OUTPUT ENDE THREAD " + threadNumber + " ######\n");

		isRunning = false;
	}
	
	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
