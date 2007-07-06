package de.ingrid.mdek.job.repository;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.mdek.job.DateJobService;
import de.ingrid.mdek.job.DummyJob;
import de.ingrid.mdek.job.persist.AbstractResourceTest;
import de.ingrid.mdek.job.persist.ResourceDeleter;
import de.ingrid.mdek.job.persist.ResourceLoader;
import de.ingrid.mdek.job.persist.ResourceStorer;
import de.ingrid.mdek.job.register.IRegistrationService;
import de.ingrid.mdek.job.register.RegistrationService;
import de.ingrid.utils.IngridDocument;

public class JobRepositoryTest extends AbstractResourceTest {

	public void testRegisterJob() throws Exception {
		Mockery mockery = new Mockery();
		final IRegistrationService service = mockery
				.mock(IRegistrationService.class);
		JobRepository repository = new JobRepository(service);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		document.put(IJobRepository.JOB_PERSIST, true);
		document.put(IJobRepository.JOB_DESCRIPTION, jobXml);

		mockery.checking(new Expectations() {
			{
				one(service).register(DummyJob.class.getName(), jobXml, true);
				one(service).getRegisteredJob(DummyJob.class.getName());
				will(returnValue(new DummyJob()));
			}
		});
		IngridDocument response = repository.register(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);
		assertEquals(1, response.size());
		assertTrue(response.getBoolean(IJobRepository.JOB_SUCCESS));
	}

	public void testRegisterFailed() throws Exception {
		Mockery mockery = new Mockery();
		final IRegistrationService service = mockery
				.mock(IRegistrationService.class);
		JobRepository repository = new JobRepository(service);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		document.put(IJobRepository.JOB_PERSIST, true);
		document.put(IJobRepository.JOB_DESCRIPTION, jobXml);

		mockery.checking(new Expectations() {
			{
				one(service).register(DummyJob.class.getName(), jobXml, true);
				will(throwException(new IOException("abc")));

			}
		});
		IngridDocument response = repository.register(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals("abc", response.get(IJobRepository.JOB_ERROR_MESSAGE));
		assertFalse(response.getBoolean(IJobRepository.JOB_SUCCESS));
	}

	public void testDeregisterJob() throws Exception {
		Mockery mockery = new Mockery();
		final IRegistrationService service = mockery
				.mock(IRegistrationService.class);
		JobRepository repository = new JobRepository(service);

		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());

		mockery.checking(new Expectations() {
			{
				one(service).deRegister(DummyJob.class.getName());
			}
		});
		IngridDocument response = repository.deRegister(document);
		mockery.assertIsSatisfied();
		assertEquals(1, response.size());
		assertTrue(response.getBoolean(IJobRepository.JOB_SUCCESS));
	}

	public void testInvokeMethod() throws Exception {

		Mockery mockery = new Mockery();
		final IRegistrationService service = mockery
				.mock(IRegistrationService.class);

		JobRepository repository = new JobRepository(service);

		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		document.put("x", 1);
		document.put("y", 2);
		document.put("sum", null);
		document.put(IJobRepository.JOB_METHODS,
				new String[] { "x", "y", "sum" });

		final DummyJob job = new DummyJob();
		mockery.checking(new Expectations() {
			{
				one(service).getRegisteredJob(DummyJob.class.getName());
				will(returnValue(job));

			}
		});
		assertEquals(0, job.getX());
		assertEquals(0, job.getY());
		IngridDocument response = repository.invoke(document);
		mockery.assertIsSatisfied();

		assertNotNull(response);
		assertEquals(2, response.size());
		assertTrue(response.getBoolean(IJobRepository.JOB_SUCCESS));

		assertEquals(1, job.getX());
		assertEquals(2, job.getY());
		assertEquals(3, job.sum());

	}

	public void testRealJob() throws Exception {
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"app-config.xml"));
		ResourceStorer storer = new ResourceStorer(_testFolder);
		ResourceLoader loader = new ResourceLoader(_testFolder);
		ResourceDeleter deleter = new ResourceDeleter(_testFolder);
		RegistrationService service = new RegistrationService(factory, storer,
				loader, deleter);
		JobRepository repository = new JobRepository(service);

		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, DateJobService.class
				.getName());
		registerDocument.put(IJobRepository.JOB_PERSIST, true);
		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DateJobService\" class=\"de.ingrid.mdek.job.DateJobService\" >"
				+ "<constructor-arg ref=\"de.ingrid.mdek.job.DateJob\"/></bean></beans>";
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		repository.register(registerDocument);

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DateJobService.class
				.getName());
		invokeDocument.put(IJobRepository.JOB_METHODS,
				new String[] { "getResults" });
		IngridDocument response = repository.invoke(invokeDocument);
		assertTrue(response.getBoolean(IJobRepository.JOB_SUCCESS));
		assertNotNull(response.get(IJobRepository.JOB_RESULT));
		assertEquals(2, response.size());

		ResourceStorer storer2 = new ResourceStorer(_testFolder);
		ResourceLoader loader2 = new ResourceLoader(_testFolder);
		ResourceDeleter deleter2 = new ResourceDeleter(_testFolder);
		RegistrationService newRegistrationService = new RegistrationService(
				factory, storer2, loader2, deleter2);
		JobRepository repository2 = new JobRepository(newRegistrationService);
		IngridDocument document = repository2.invoke(invokeDocument);
		assertFalse(document.getBoolean(IJobRepository.JOB_SUCCESS));
		newRegistrationService.registerPersistedJobs();
		document = repository2.invoke(invokeDocument);
		assertTrue(document.getBoolean(IJobRepository.JOB_SUCCESS));

		newRegistrationService.deRegister(DateJobService.class.getName());
		document = repository2.invoke(invokeDocument);
		assertFalse(document.getBoolean(IJobRepository.JOB_SUCCESS));

	}

}
