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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;

public class ResourceStorer implements IResourceStorer {

	private static final Logger LOG = Logger.getLogger(ResourceStorer.class);

	private final File _persistenceFolder;

	public ResourceStorer(File persistenceFolder) {
		_persistenceFolder = persistenceFolder;
	}

	public void storeResource(String name, ByteArrayResource xml)
			throws IOException {
		byte[] byteArray = xml.getByteArray();
		FileOutputStream stream = null;

		File file = new File(_persistenceFolder, "" + name + ".xml");
		if (file.exists()) {
			throw new IOException("resource alreadsy exists");
		}
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("store bean to [" + file.getAbsolutePath() + "]");
			}
			stream = new FileOutputStream(file);
			stream.write(byteArray);
		} catch (IOException e) {
			throw new IOException("can not persist bean.");
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isDebugEnabled()) {
						LOG.error("can not close stream for persisting beans.");
					}
				}
			}
		}

	}

}
