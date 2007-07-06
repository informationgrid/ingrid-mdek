package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.WetagURL;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.ingrid.mdek.job.repository.IJobRepository;

public class MdekServer implements IMdekServer {

	private final File _communicationProperties;

	private final String _proxyServiceUrl;

	private final IJobRepository _jobRepository;

	public MdekServer(File communicationProperties, String proxyServiceUrl,
			IJobRepository jobRepository) {
		_communicationProperties = communicationProperties;
		_proxyServiceUrl = proxyServiceUrl;
		_jobRepository = jobRepository;

	}

	public void run() throws IOException {
		ICommunication communication = initCommunication(
				_communicationProperties, _proxyServiceUrl);

		ProxyService.createProxyServer(communication, IJobRepository.class,
				_jobRepository);

		synchronized (MdekServer.class) {
			try {
				MdekServer.class.wait();
			} catch (InterruptedException e) {
				throw new IOException(e.getMessage());
			}
		}
	}

	private ICommunication initCommunication(File properties,
			String proxyServiceUrl) throws IOException {
		FileInputStream confIS = new FileInputStream(properties);
		ICommunication communication = StartCommunication.create(confIS);
		WetagURL proxyUrl = new WetagURL(proxyServiceUrl);
		communication.setPeerName(proxyUrl.getPath());
		communication.startup();
		return communication;
	}

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
						+ MdekServer.class.getName()
						+ "--descriptor <communication.properties> --proxyServiceUrl <proxyServiceUrl>");
		System.exit(0);
	}

	public static void main(String[] args) throws IOException {
		Map map = readParameters(args);
		if (map.size() != 2) {
			printUsage();
		}

		String communicationFile = (String) map.get("--descriptor");
		String proxyUrl = (String) map.get("--proxyServiceUrl");

		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "startup.xml" });
		BeanFactory factory = (BeanFactory) context;
		IJobRepository jobRepository = (IJobRepository) factory
				.getBean(IJobRepository.class.getName());
		MdekServer server = new MdekServer(new File(communicationFile),
				proxyUrl, jobRepository);
		server.run();
	}
}
