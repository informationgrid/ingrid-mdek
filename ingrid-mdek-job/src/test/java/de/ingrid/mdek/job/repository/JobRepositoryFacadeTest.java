package de.ingrid.mdek.job.repository;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

import de.ingrid.mdek.job.DummyJob;
import de.ingrid.utils.IngridDocument;

public class JobRepositoryFacadeTest extends TestCase {

	public void testExecuteWithoutPersist() throws Exception {
		Mockery mockery = new Mockery();
		final IJobRepository repository = mockery.mock(IJobRepository.class);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		final IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		document.put(IJobRepository.JOB_PERSIST, false);
		document.put(IJobRepository.JOB_DESCRIPTION, jobXml);

		final IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_REGISTER_SUCCESS, true);
		registerDocument.put(IJobRepository.JOB_INVOKE_SUCCESS, true);
		registerDocument.put(IJobRepository.JOB_DEREGISTER_SUCCESS, true);

		mockery.checking(new Expectations() {
			{
				one(repository).register(document);
				will(returnValue(registerDocument));
				one(repository).invoke(document);
				will(returnValue(registerDocument));
				one(repository).deRegister(document);
				will(returnValue(registerDocument));
			}
		});
		JobRepositoryFacade facade = new JobRepositoryFacade(repository);
		IngridDocument response = facade.execute(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);
	}
	
	public void testExecuteWithPersist() throws Exception {
		Mockery mockery = new Mockery();
		final IJobRepository repository = mockery.mock(IJobRepository.class);

		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
				+ "</bean></beans>";
		final IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		document.put(IJobRepository.JOB_PERSIST, true);
		document.put(IJobRepository.JOB_DESCRIPTION, jobXml);

		final IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_REGISTER_SUCCESS, true);
		registerDocument.put(IJobRepository.JOB_INVOKE_SUCCESS, true);

		mockery.checking(new Expectations() {
			{
				one(repository).register(document);
				will(returnValue(registerDocument));
				one(repository).invoke(document);
				will(returnValue(registerDocument));
				
			}
		});
		JobRepositoryFacade facade = new JobRepositoryFacade(repository);
		IngridDocument response = facade.execute(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);
	}
	
	
}
