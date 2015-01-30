/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.persist;

import org.springframework.core.io.ByteArrayResource;

public class ResourceStorerTest extends AbstractResourceTest {

	public void testStore() throws Exception {
		ResourceStorer storer = new ResourceStorer(_testFolder);
		ByteArrayResource xml = new ByteArrayResource(
				"<beans><bean class=\"foo.bar.Dummy\" /></beans>".getBytes());
		assertEquals(0, _testFolder.listFiles().length);
		storer.storeResource("foo.bar.Dummy", xml);
		assertEquals(1, _testFolder.listFiles().length);
		assertEquals("foo.bar.Dummy.xml", _testFolder.listFiles()[0].getName());
	}
}
