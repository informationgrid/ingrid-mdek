/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

public class RandomExample {

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties>");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() != 1) {
			printUsage();
		}

		MdekClient client = MdekClient.getInstance(new File((String) map
				.get("--descriptor")));

		Thread.sleep(2000);
		IJobRepositoryFacade jobRepositoryFacade = client
				.getJobRepositoryFacade("/101tec-group:101tec-mdek-server");

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.example.RandomJob\" class=\"de.ingrid.mdek.example.RandomJob\" >"
				+ "<constructor-arg ref=\"de.ingrid.mdek.services.date.SimpleDateJob\"/></bean></beans>";

		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, RandomJob.class.getName());
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		registerDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		System.out.println("###### REGISTER ######");
		IngridDocument response = jobRepositoryFacade.execute(registerDocument);
		System.out.println(response);

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, RandomJob.class.getName());
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair("sayHello", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("###### INVOKE sayHello ######");
		IngridDocument invokeResponse = jobRepositoryFacade
				.execute(invokeDocument);

		System.out.println(invokeResponse);
		methodList.clear();
		methodList.add(new Pair("name", "mb"));
		methodList.add(new Pair("sayHello", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
		System.out.println("###### INVOKE setName, sayHello  ######");
		invokeResponse = jobRepositoryFacade.execute(invokeDocument);
		System.out.println(invokeResponse);

		System.out.println("###### DEREGISTER ######");
		IngridDocument deregisterDocument = new IngridDocument();
		deregisterDocument
				.put(IJobRepository.JOB_ID, RandomJob.class.getName());
		deregisterDocument.put(IJobRepository.JOB_PERSIST, false);
		IngridDocument deregisterResponse = jobRepositoryFacade.execute(deregisterDocument);
		System.out.println(deregisterResponse);
		client.shutdown();
	}
}
