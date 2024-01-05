/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.register;

import de.ingrid.mdek.job.DummyJob;
import junit.framework.TestCase;

public class RegistrationServiceTest extends TestCase {

	public void testRegistration() throws Exception {

		DummyJob dummyJob = new DummyJob();
		RegistrationService service = new RegistrationService(dummyJob);
		Object bean = service.getRegisteredJob(DummyJob.class.getName());
		assertNotNull(bean);

	}

//	public void testDeregister() throws Exception {
//
//		Mockery mockery = new Mockery();
//
//		String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//				+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
//				+ "<beans><bean id=\"de.ingrid.mdek.job.DummyJob\" class=\"de.ingrid.mdek.job.DummyJob\" >"
//				+ "</bean></beans>";
//		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
//				"app-config.xml"));
//		RegistrationService service = new RegistrationService(factory, storer,
//				loader, deleter);
//
//		mockery.checking(new Expectations() {
//			{
//				one(storer).storeResource(
//						with(equal(DummyJob.class.getName())),
//						with(any(ByteArrayResource.class)));
//			}
//		});
//
//		service.register(DummyJob.class.getName(), jobXml, true);
//		mockery.assertIsSatisfied();
//		Object bean = service.getRegisteredJob(DummyJob.class.getName());
//		assertNotNull(bean);
//		assertNotNull(factory.getBean("de.ingrid.mdek.job.DateJob"));
//
//		mockery.checking(new Expectations() {
//			{
//				one(deleter).deleteResource(DummyJob.class.getName() + ".xml");
//			}
//		});
//		service.deRegister(DummyJob.class.getName());
//		mockery.assertIsSatisfied();
//		bean = service.getRegisteredJob(DummyJob.class.getName());
//		assertNull(bean);
//		assertNotNull(factory.getBean("de.ingrid.mdek.job.DateJob"));
//	}

//	public void testRegisterPersitedJobs() throws Exception {
//		InputStream resourceAsStream = RegistrationServiceTest.class
//				.getResourceAsStream("/test.xml");
//		FileOutputStream stream = new FileOutputStream(new File(_testFolder,
//				DummyJob.class.getName() + ".xml"));
//
//		int read = -1;
//		byte[] buffer = new byte[1024];
//		while ((read = resourceAsStream.read(buffer, 0, 1024)) != -1) {
//			stream.write(buffer, 0, read);
//			stream.flush();
//		}
//
//		resourceAsStream.close();
//		stream.close();
//
//		Mockery mockery = new Mockery();
//		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
//				"app-config.xml"));
//		RegistrationService service = new RegistrationService(factory, storer,
//				new ResourceLoader(_testFolder), deleter);
//
//		Object registeredBean = service.getRegisteredJob(DummyJob.class
//				.getName());
//		assertNull(registeredBean);
//		service.registerPersistedJobs();
//		mockery.assertIsSatisfied();
//
//		registeredBean = service.getRegisteredJob(DummyJob.class.getName());
//		assertNotNull(registeredBean);
//	}
}
