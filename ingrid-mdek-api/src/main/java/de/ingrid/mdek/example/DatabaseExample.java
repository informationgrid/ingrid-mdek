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

public class DatabaseExample {

	// NOTICE: our own JOB_ID !!! so we don't need mdek server lib !
	public static String DATABASE_JOB_ID = "TestDatabaseJob";

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
				.getJobRepositoryFacade("/101tec-group:101tec-mdek-server");

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
			+ "<bean class=\"de.ingrid.mdek.services.persistence.db.test.TestDaoFactory\" id=\"TestDaoFactory\">"
			+ "<constructor-arg ref=\"sessionFactory\"/>"
			+ "</bean>"
			+ "<bean class=\"de.ingrid.mdek.services.persistence.db.AtomarModelPersister\" id=\"TestPersister\">"
			+ "<constructor-arg ref=\"TestDaoFactory\"/>"
			+ "</bean>"
			+ "<bean id=\"" + DATABASE_JOB_ID + "\" class=\"de.ingrid.mdek.job.test.TestDatabaseJob\" >"
			+ "<constructor-arg ref=\"de.ingrid.mdek.services.log.ILogService\"/>"
			+ "<constructor-arg ref=\"TestPersister\"/>"
			+ "<constructor-arg ref=\"TestDaoFactory\"/>"
			+ "<constructor-arg ref=\"de.ingrid.mdek.services.persistence.db.IHQLExecuter\"/>"
			+ "<constructor-arg ref=\"sessionFactory\"/>"
			+ "</bean>"
			+ "</beans>";

		// register job
		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, DATABASE_JOB_ID);
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### REGISTER ######");
		IngridDocument response = jobRepositoryFacade.execute(registerDocument);
		debugDocument("RESPONSE", response);

		System.out.println("\n\n###### OUTPUT THREADS ######\n\n");

		// threads calling job
		DatabaseThread[] threads = new DatabaseThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new DatabaseThread(i, jobRepositoryFacade);
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
		deregisterDocument.put(IJobRepository.JOB_ID, DATABASE_JOB_ID);
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
}

class DatabaseThread extends Thread {

	private int threadNumber;
	private IJobRepositoryFacade jobRepositoryFacade;
	
	private boolean isRunning = false;

	public DatabaseThread(int threadNumber,
			IJobRepositoryFacade jobFacade)
	{
		this.threadNumber = threadNumber;
		this.jobRepositoryFacade = jobFacade;
	}

	public void run() {
		isRunning = true;

		testDAO();
		testDAOFactory();
		testPersister();
		testHSQL();

/*
		System.out.println("\n###### RESULTS THREAD " + threadNumber + " ######");
		System.out.println("###### EXCEL OUTPUT START THREAD " + threadNumber + " ######");

		String DELIMITER = "\t";
		System.out.print("needed time" + DELIMITER + neededTime);
		System.out.print("\n");
		System.out.println("###### EXCEL OUTPUT ENDE THREAD " + threadNumber + " ######\n");
*/
		isRunning = false;
	}
	
	private void testDAO() {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		// test DAO
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DatabaseExample.DATABASE_JOB_ID);
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair("testDao", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE testDao ######");

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		DatabaseExample.debugDocument("RESPONSE:", invokeResponse);

		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
	}

	private void testDAOFactory() {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		// test DAO
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DatabaseExample.DATABASE_JOB_ID);
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair("testDaoFactory", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE testDaoFactory ######");

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		DatabaseExample.debugDocument("RESPONSE:", invokeResponse);

		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
	}

	private void testPersister() {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		// test DAO
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DatabaseExample.DATABASE_JOB_ID);
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair("testPersister", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE testPersister ######");

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		DatabaseExample.debugDocument("RESPONSE:", invokeResponse);

		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");		
	}

	private void testHSQL() {
		long startTime = 0;
		long endTime = 0;
		long neededTime = 0;

		// test DAO
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DatabaseExample.DATABASE_JOB_ID);
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair("testHQLExecuter", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("\n###### INVOKE testHSQL ######");

		startTime = System.currentTimeMillis();
		IngridDocument invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		endTime = System.currentTimeMillis();

		DatabaseExample.debugDocument("RESPONSE:", invokeResponse);

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
