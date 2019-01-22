/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import de.ingrid.mdek.job.IJob;

public class LogServiceTest {

    //@Test
	public void testGetLogger() throws Exception {
		String property = System.getProperty("java.io.tmpdir");
		File file = new File(property, LogServiceTest.class.getName() + "-"
				+ System.currentTimeMillis());
		file.mkdir();
		LogService service = new LogService();
		Logger logger = service.getLogger(IJob.class);

		logger.info("test");

		File[] files = file.listFiles();
		Assert.assertEquals(1, files.length);
		FileReader reader = new FileReader(files[0]);
		BufferedReader reader2 = new BufferedReader(reader);
		String line = reader2.readLine();
		Assert.assertTrue(line.endsWith("test"));
		Assert.assertNull(reader2.readLine());
		reader2.close();
	}
}
