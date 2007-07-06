package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.job.repository.IJobRepository;
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
		System.err
				.println("Usage: "
						+ MdekClient.class.getName()
						+ "--descriptor <communication.properties> --proxyServiceUrl <proxyServiceUrl>");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() != 2) {
			printUsage();
		}

		MdekClient client = MdekClient.getInstance(new File((String) map
				.get("--descriptor")), (String) map.get("--proxyServiceUrl"));

		Thread.sleep(2000);
		IJobRepository jobRepository = client.getJobRepository();

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.example.RandomJob\" class=\"de.ingrid.mdek.example.RandomJob\" >"
				+ "<constructor-arg ref=\"de.ingrid.mdek.services.date.SimpleDateJob\"/></bean></beans>";
		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, RandomJob.class.getName());
		document.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		document.putBoolean(IJobRepository.JOB_PERSIST, true);

		IngridDocument response = jobRepository.register(document);
		System.out.println(response);

	}
}
