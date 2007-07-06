package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.WetagURL;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;
import de.ingrid.mdek.job.repository.IJobRepository;

public class MdekClient {

	private static MdekClient _client = null;

	private static IJobRepository _repository;

	public MdekClient() {
		// private
	}

	public static MdekClient getInstance(File communicationProperties,
			String proxyServiceUrl) throws IOException {
		if (_client == null) {
			_client = new MdekClient();
			_repository = _client.startup(communicationProperties,
					proxyServiceUrl);
		}
		return _client;
	}

	public IJobRepository getJobRepository() {
		return _repository;
	}

	private IJobRepository startup(File communicationProperties,
			String proxyServiceUrl) throws IOException {
		ICommunication communication = initCommunication(
				communicationProperties, proxyServiceUrl);
		IJobRepository repository = (IJobRepository) ProxyService.createProxy(
				communication, IJobRepository.class, "/101tec-group:101tec-mdek-server");
		return repository;
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

}
