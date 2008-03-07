package de.ingrid.mdek.services.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import de.ingrid.mdek.job.IJob;

public class LogServiceTest extends TestCase {

	public void testGetLogger() throws Exception {
		String property = System.getProperty("java.io.tmpdir");
		File file = new File(property, LogServiceTest.class.getName() + "-"
				+ System.currentTimeMillis());
		file.mkdir();
		LogService service = new LogService(file, false);
		Logger logger = service.getLogger(IJob.class);

		logger.info("test");

		File[] files = file.listFiles();
		assertEquals(1, files.length);
		FileReader reader = new FileReader(files[0]);
		BufferedReader reader2 = new BufferedReader(reader);
		String line = reader2.readLine();
		assertTrue(line.endsWith("test"));
		assertNull(reader2.readLine());
		reader2.close();
	}
}
