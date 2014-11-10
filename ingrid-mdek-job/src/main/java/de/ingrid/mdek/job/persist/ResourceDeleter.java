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

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ResourceDeleter implements IResourceDeleter {

	private static final Logger LOG = Logger.getLogger(ResourceDeleter.class);

	private final File _persistenceFolder;

	public ResourceDeleter(File persistenceFolder) {
		_persistenceFolder = persistenceFolder;
	}

	public void deleteResource(String name) {
		File resource = new File(_persistenceFolder, name);
		if (LOG.isInfoEnabled()) {
			LOG.info("delete resource [" + resource.getAbsolutePath() + "]");
		}
		boolean success = resource.delete();
		if (!success) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("can not delete resource ["
						+ resource.getAbsolutePath() + "]");
			}
		}
	}
}
