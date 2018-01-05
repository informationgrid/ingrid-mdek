/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.List;

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
		registerDocument.put(IJobRepository.JOB_DEREGISTER_SUCCESS, true);

		mockery.checking(new Expectations() {
			{
				one(repository).register(document);
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

			}
		});
		JobRepositoryFacade facade = new JobRepositoryFacade(repository);
		IngridDocument response = facade.execute(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);
	}

	public void testExecuteOnlyMethods() throws Exception {
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
			}
		});
		JobRepositoryFacade facade = new JobRepositoryFacade(repository);
		IngridDocument response = facade.execute(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);

		mockery = new Mockery();
		final IJobRepository repository2 = mockery.mock(IJobRepository.class);
		JobRepositoryFacade facade2 = new JobRepositoryFacade(repository2);

		final IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		List<Pair> list = new ArrayList<Pair>();
		list.add(new Pair("getResults", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, list);
		invokeDocument.put(IJobRepository.JOB_PERSIST, true);
		mockery.checking(new Expectations() {
			{
				one(repository2).invoke(invokeDocument);
				will(returnValue(document));

			}
		});
		response = facade2.execute(invokeDocument);
		mockery.assertIsSatisfied();
		assertNotNull(response);

	}

	public void testExecuteMethodAndDeregister() throws Exception {
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
			}
		});
		JobRepositoryFacade facade = new JobRepositoryFacade(repository);
		IngridDocument response = facade.execute(document);
		mockery.assertIsSatisfied();
		assertNotNull(response);

		mockery = new Mockery();
		final IJobRepository repository2 = mockery.mock(IJobRepository.class);
		JobRepositoryFacade facade2 = new JobRepositoryFacade(repository2);

		final IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, DummyJob.class.getName());
		List<Pair> list = new ArrayList<Pair>();
		list.add(new Pair("getResults", null));
		invokeDocument.put(IJobRepository.JOB_METHODS, list);
		invokeDocument.put(IJobRepository.JOB_PERSIST, false);
		mockery.checking(new Expectations() {
			{
				one(repository2).invoke(invokeDocument);
				will(returnValue(document));
				one(repository2).deRegister(invokeDocument);
				will(returnValue(document));
			}
		});
		response = facade2.execute(invokeDocument);
		mockery.assertIsSatisfied();
		assertNotNull(response);

	}

}
