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
