package de.ingrid.mdek.job.register;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.job.persist.IResourceDeleter;
import de.ingrid.mdek.job.persist.IResourceLoader;
import de.ingrid.mdek.job.persist.IResourceStorer;

public class RegistrationService implements IRegistrationService {

	private static final Logger LOG = Logger
			.getLogger(RegistrationService.class);

	private Map<String, XmlBeanFactory> _beanFactoryCache = new HashMap<String, XmlBeanFactory>();

	private final IResourceStorer _resourceStorer;

	private final IResourceLoader _resourceLoader;

	private final IResourceDeleter _resourceDeleter;

	private final BeanFactory _parentFactory;

	public RegistrationService(BeanFactory parentFactory,
			IResourceStorer resourceStorer, IResourceLoader resourceLoader,
			IResourceDeleter resourceDeleter) {
		_parentFactory = parentFactory;
		_resourceStorer = resourceStorer;
		_resourceLoader = resourceLoader;
		_resourceDeleter = resourceDeleter;
	}

	public void register(String jobId, String xml, boolean persist)
			throws IOException {
		ByteArrayResource resource = new ByteArrayResource(xml.getBytes());
		XmlBeanFactory beanFactory = new XmlBeanFactory(resource);
		if (LOG.isInfoEnabled()) {
			LOG.info("register job xml with id [" + jobId + "]");
		}

		_beanFactoryCache.put(jobId, beanFactory);
		beanFactory.setParentBeanFactory(_parentFactory);

		if (persist) {
			_resourceStorer.storeResource(jobId, resource);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(_beanFactoryCache);
		}
	}

	public void deRegister(String jobId) {
		XmlBeanFactory factory = _beanFactoryCache.remove(jobId);
		if (factory != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("deregister all beans of job [" + jobId + "]");
			}
			String[] beanDefinitionNames = factory.getBeanDefinitionNames();
			for (String beanDefinition : beanDefinitionNames) {
				Object bean = factory.getBean(beanDefinition);
				if (LOG.isInfoEnabled()) {
					LOG.info("destroy bean [" + beanDefinition + "]");
				}
				factory.destroyBean(beanDefinition, bean);
			}
			factory = null;
		}
		_resourceDeleter.deleteResource(jobId + ".xml");
	}

	public IJob getRegisteredJob(String jobId) {
		XmlBeanFactory factory = _beanFactoryCache.get(jobId);
		Object object = factory != null ? factory.getBean(jobId) : null;
		return (IJob) object;
	}

	/**
	 * configure this method as init-method in spring config.xml. this method
	 * loads all persited jobs
	 * 
	 * @throws IOException
	 */
	public void registerPersistedJobs() throws IOException {
		while (_resourceLoader.hasNext()) {
			FileSystemResource resource = _resourceLoader.next();
			InputStream inputStream = resource.getInputStream();
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			int read = -1;
			while ((read = inputStream.read(buffer, 0, 1024)) != -1) {
				stream.write(buffer, 0, read);
				stream.flush();
			}
			byte[] bs = stream.toByteArray();
			stream.close();
			inputStream.close();
			String filename = resource.getFilename();
			filename = filename.substring(0, filename.lastIndexOf("."));
			register(filename, new String(bs), false);
		}
	}

}
