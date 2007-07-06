package de.ingrid.mdek.job.register;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.mdek.job.DummyJob;
import de.ingrid.mdek.job.persist.AbstractResourceTest;
import de.ingrid.mdek.job.persist.IResourceDeleter;
import de.ingrid.mdek.job.persist.IResourceLoader;
import de.ingrid.mdek.job.persist.IResourceStorer;
import de.ingrid.mdek.job.persist.ResourceLoader;

public class RegistrationServiceTest extends AbstractResourceTest {

	public void testRegistration() throws Exception {

		Mockery mockery = new Mockery();
		final IResourceStorer storer = mockery.mock(IResourceStorer.class);
		final IResourceLoader loader = mockery.mock(IResourceLoader.class);
		final IResourceDeleter deleter = mockery.mock(IResourceDeleter.class);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"app-config.xml"));
		RegistrationService service = new RegistrationService(factory, storer,
				loader, deleter);
		Object bean = service.getRegisteredJob(DummyJob.class.getName());
		assertNull(bean);

		mockery.checking(new Expectations() {
			{
				one(storer).storeResource(
						with(equal(DummyJob.class.getName())),
						with(any(ByteArrayResource.class)));
			}
		});

		service.register(DummyJob.class.getName(), jobXml, true);
		mockery.assertIsSatisfied();
		bean = service.getRegisteredJob(DummyJob.class.getName());
		assertNotNull(bean);
	}

	public void testDeregister() throws Exception {

		Mockery mockery = new Mockery();
		final IResourceStorer storer = mockery.mock(IResourceStorer.class);
		final IResourceLoader loader = mockery.mock(IResourceLoader.class);
		final IResourceDeleter deleter = mockery.mock(IResourceDeleter.class);

		String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"app-config.xml"));
		RegistrationService service = new RegistrationService(factory, storer,
				loader, deleter);

		mockery.checking(new Expectations() {
			{
				one(storer).storeResource(
						with(equal(DummyJob.class.getName())),
						with(any(ByteArrayResource.class)));
			}
		});

		service.register(DummyJob.class.getName(), jobXml, true);
		mockery.assertIsSatisfied();
		Object bean = service.getRegisteredJob(DummyJob.class.getName());
		assertNotNull(bean);
		assertNotNull(factory.getBean("de.ingrid.mdek.job.DateJob"));

		mockery.checking(new Expectations() {
			{
				one(deleter).deleteResource(DummyJob.class.getName() + ".xml");
			}
		});
		service.deRegister(DummyJob.class.getName());
		mockery.assertIsSatisfied();
		bean = service.getRegisteredJob(DummyJob.class.getName());
		assertNull(bean);
		assertNotNull(factory.getBean("de.ingrid.mdek.job.DateJob"));
	}

	public void testRegisterPersitedJobs() throws Exception {
		InputStream resourceAsStream = RegistrationServiceTest.class
				.getResourceAsStream("/test.xml");
		FileOutputStream stream = new FileOutputStream(new File(_testFolder,
				DummyJob.class.getName() + ".xml"));

		int read = -1;
		byte[] buffer = new byte[1024];
		while ((read = resourceAsStream.read(buffer, 0, 1024)) != -1) {
			stream.write(buffer, 0, read);
			stream.flush();
		}

		resourceAsStream.close();
		stream.close();

		Mockery mockery = new Mockery();
		final IResourceStorer storer = mockery.mock(IResourceStorer.class);
		final IResourceDeleter deleter = mockery.mock(IResourceDeleter.class);
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"app-config.xml"));
		RegistrationService service = new RegistrationService(factory, storer,
				new ResourceLoader(_testFolder), deleter);

		Object registeredBean = service.getRegisteredJob(DummyJob.class
				.getName());
		assertNull(registeredBean);
		service.registerPersistedJobs();
		mockery.assertIsSatisfied();

		registeredBean = service.getRegisteredJob(DummyJob.class.getName());
		assertNotNull(registeredBean);
	}
}
