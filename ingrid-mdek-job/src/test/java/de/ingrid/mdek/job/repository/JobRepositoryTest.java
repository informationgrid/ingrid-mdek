/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.repository;

import java.io.IOException;
import java.util.ArrayList;

import org.jmock.Expectations;
import org.jmock.Mockery;

import de.ingrid.mdek.job.DateJob;
import de.ingrid.mdek.job.DateJobService;
import de.ingrid.mdek.job.DummyJob;
import de.ingrid.mdek.job.register.IRegistrationService;
import de.ingrid.mdek.job.register.RegistrationService;
import de.ingrid.utils.IngridDocument;
import junit.framework.TestCase;

public class JobRepositoryTest extends TestCase {

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
		assertTrue(response.getBoolean(IJobRepository.JOB_REGISTER_SUCCESS));
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
		assertEquals("abc", response
				.get(IJobRepository.JOB_REGISTER_ERROR_MESSAGE));
		assertFalse(response.getBoolean(IJobRepository.JOB_REGISTER_SUCCESS));
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
		assertTrue(response.getBoolean(IJobRepository.JOB_DEREGISTER_SUCCESS));
	}

	public void testInvokeMethod() throws Exception {

		Mockery mockery = new Mockery();
		final IRegistrationService service = mockery
				.mock(IRegistrationService.class);

		JobRepository repository = new JobRepository(service);

		IngridDocument document = new IngridDocument();
		document.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		ArrayList<Pair> methods = new ArrayList<Pair>();
		methods.add(new Pair("x", 1));
		methods.add(new Pair("y", 2));
		methods.add(new Pair("sum", null));
		document.put(IJobRepository.JOB_METHODS, methods);

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
		assertTrue(response.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS));

		assertEquals(1, job.getX());
		assertEquals(2, job.getY());
		assertEquals(3, job.sum());

	}

	public void testRealJob() throws Exception {
	    DateJobService dateJobService = new DateJobService( new DateJob() );
		RegistrationService service = new RegistrationService(dateJobService);
		JobRepository repository = new JobRepository(service);

		IngridDocument registerDocument = new IngridDocument();
		registerDocument.put(IJobRepository.JOB_ID, DateJobService.class.getName());
		registerDocument.put(IJobRepository.JOB_PERSIST, true);
		final String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
				+ "<beans><bean id=\"de.ingrid.mdek.job.DateJobService\" class=\"de.ingrid.mdek.job.DateJobService\" >"
				+ "<constructor-arg ref=\"de.ingrid.mdek.job.DateJob\"/></bean></beans>";
		registerDocument.put(IJobRepository.JOB_DESCRIPTION, jobXml);
		repository.register(registerDocument);

		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DateJobService.class.getName());
		ArrayList<Pair> methods = new ArrayList<Pair>();
		methods.add(new Pair("getResults", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, methods);
		IngridDocument response = repository.invoke(invokeDocument);
		assertTrue(response.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS));
		assertNotNull(response.get(IJobRepository.JOB_INVOKE_RESULTS));
		assertEquals(2, response.size());

//		RegistrationService newRegistrationService = new RegistrationService(dateJobService);
//		JobRepository repository2 = new JobRepository(newRegistrationService);
//		IngridDocument document = repository2.invoke(invokeDocument);
//		assertFalse(document.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS));
//		newRegistrationService.registerPersistedJobs();
//		document = repository2.invoke(invokeDocument);
//		assertTrue(document.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS));
//
//		newRegistrationService.deRegister(DateJobService.class.getName());
//		document = repository2.invoke(invokeDocument);
//		assertFalse(document.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS));

	}

}
