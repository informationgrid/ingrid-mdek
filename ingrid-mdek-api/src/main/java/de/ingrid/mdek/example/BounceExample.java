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
				+ "--descriptor <communication.properties> [--wait 5] [--length 1000]");
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
		Integer dataLength = 1000;
		if (map.get("--length") != null) {
			dataLength = new Integer((String) map.get("--length"));
		}
		StringBuffer dataBuffer = new StringBuffer(dataLength);
		dataBuffer.setLength(dataLength);
		String data = dataBuffer.toString();
		
		Integer wait = 5;
		if (map.get("--wait") != null) {
			wait = new Integer((String) map.get("--wait"));
		}

		Integer loops = 1;
		if (map.get("--loops") != null) {
			loops = new Integer((String) map.get("--loops"));
		}

		debugParams(dataLength, wait, loops);

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

		// LOOPING !!!
		int numLoops = 0;
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;
		Long[] loopTimes1 = new Long[loops];
		Long[] loopTimes2 = new Long[loops];

		while (numLoops < loops) {
			
			System.out.println("\n--- LOOP" + (numLoops+1) + " ---\n");

			// set data in job and read back
			IngridDocument invokeDocument = new IngridDocument();
			invokeDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
			ArrayList<Pair> methodList = new ArrayList<Pair>();
			methodList.add(new Pair("data", data));
			methodList.add(new Pair("wait", wait));
			methodList.add(new Pair("getDataWithWait", null));
			invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
			invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
			System.out.println("###### INVOKE setData, setWait, getDataWithWait ######");

			startTime = System.currentTimeMillis();
			IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
			endTime = System.currentTimeMillis();

			debugDocument("RESPONSE", invokeResponse);

			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
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
			System.out.println("###### INVOKE bounceDocWithWait ######");
			debugDocument("INPUT:", inputDocument);

			startTime = System.currentTimeMillis();
			invokeResponse = jobRepositoryFacade.execute(invokeDocument);
			endTime = System.currentTimeMillis();

			debugDocument("RESPONSE:", invokeResponse);

			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			loopTimes2[numLoops] = neededTime;

			numLoops++;
		}

		System.out.println("\n###### DEREGISTER ######");
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument.put(IJobRepository.JOB_ID, BounceJob.class.getName());
		deregisterDocument.put(IJobRepository.JOB_PERSIST, false);
		IngridDocument deregisterResponse = jobRepositoryFacade.execute(deregisterDocument);
		debugDocument("RESPONSE", deregisterResponse);

		System.out.println("\n###### LOOP TIMES ######");
		debugParams(dataLength, wait, loops);
		for (int i=0; i<loops; i++) {
			System.out.println((i+1) + ". " + loopTimes1[i] + " ms, " + loopTimes2[i] + " ms");
		}

		client.shutdown();
	}
	
	private static void debugDocument(String title, IngridDocument doc) {
		if (title != null) {
			System.out.println(title);						
		}
		int docLength = doc.toString().length();
		System.out.println("IngridDocument length: " + docLength);			
		if (docLength < 1000)  {
			System.out.println("IngridDocument: " + doc);			
		}		
	}

	private static void debugParams(Integer length, Integer wait, Integer loops) {
		System.out.println("DATA length: " + length);
		System.out.println("WAIT sec: " + wait);
		System.out.println("LOOPS: " + loops);
	}
}
