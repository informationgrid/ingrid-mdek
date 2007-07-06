package de.ingrid.mdek.job.persist;

import java.io.File;

import junit.framework.TestCase;

public abstract class AbstractResourceTest extends TestCase {
	protected File _testFolder = new File(System.getProperty("java.io.tmpdir"),
			"" + System.currentTimeMillis());

	protected void setUp() throws Exception {
		assertTrue(_testFolder.mkdirs());
	}

	protected void tearDown() throws Exception {
		File[] files = _testFolder.listFiles();
		for (File file : files) {
			assertTrue(file.delete());
		}
		assertTrue(_testFolder.delete());
	}
}
