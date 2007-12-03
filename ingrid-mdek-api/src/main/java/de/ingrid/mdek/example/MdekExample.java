package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

public class MdekExample {

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

		// read passed Parameters
		System.out.println("\n###### PARAMS ######");
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}
		System.out.println("THREADS: " + numThreads);

		// INITIALIZE MDEK INTERFACE ONCE !
		MdekCaller.initialize(new File((String) map.get("--descriptor")));

		// start threads calling job
		System.out.println("\n###### OUTPUT THREADS ######\n");
		MdekThread[] threads = new MdekThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekThread(i+1);
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

		// shutdown mdek
		MdekCaller.shutdown();
	}
}

class MdekThread extends Thread {

	private int threadNumber;
	
	private boolean isRunning = false;

	public MdekThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
	}

	public void run() {
		isRunning = true;

		IMdekCaller mdekCaller = MdekCaller.getInstance();

		//System.out.println("\n###### INVOKE testMdekEntity ######");
		//mdekCaller.testMdekEntity(threadNumber);

		System.out.println("\n###### INVOKE getSubObjects ######");
		long startTime = System.currentTimeMillis();
		IngridDocument response = mdekCaller.fetchSubObjects("FB5D7527-8331-4870-9CE0-B8BDF9DAB619");
		long endTime = System.currentTimeMillis();
		long neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		System.out.println("response: " + response);
		IngridDocument result = MdekCaller.getResult(response);
		if (result != null) {
			List<IngridDocument> objs = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			for (IngridDocument o : objs) {
				System.out.println(o.get(MdekKeys.UUID) + " " + o.get(MdekKeys.TITLE) + " " + o.get(MdekKeys.ABSTRACT));
			}
		} else {
			System.out.println(MdekCaller.getErrorMsg(response));			
		}


		System.out.println("\n###### INVOKE getObjAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchObjAddresses("FB5D7527-8331-4870-9CE0-B8BDF9DAB619");
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		System.out.println("response: " + response);
		result = MdekCaller.getResult(response);
		if (result != null) {
			List<IngridDocument> objs = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			for (IngridDocument o : objs) {
				System.out.println(o.get(MdekKeys.UUID) + " " + 
					o.get(MdekKeys.GIVEN_NAME) + " " + 
					o.get(MdekKeys.NAME) + " " + 
					o.get(MdekKeys.ADDRESS_DESCRIPTION));
			}
		} else {
			System.out.println(MdekCaller.getErrorMsg(response));			
		}
		
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
