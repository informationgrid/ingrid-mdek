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
