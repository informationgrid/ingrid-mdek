/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
import org.springframework.core.io.FileSystemResource;

public class ResourceLoaderTest extends AbstractResourceTest {

	public void testLoadResources() throws Exception {
		ResourceStorer storer = new ResourceStorer(_testFolder);

		for (int i = 0; i < 10; i++) {
			ByteArrayResource xml = new ByteArrayResource(
					"<beans><bean class=\"foo.bar.Dummy\" /></beans>"
							.getBytes());
			storer.storeResource("foo.bar.Dummy" + i, xml);
		}

		ResourceLoader loader = new ResourceLoader(_testFolder);
		int i = -1;
		while (loader.hasNext()) {
			i++;
			FileSystemResource resource = loader.next();
			assertEquals("foo.bar.Dummy" + i + ".xml", resource.getFilename());
		}
		assertEquals(9, i);
	}
}
