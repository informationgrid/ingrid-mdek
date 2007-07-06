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
