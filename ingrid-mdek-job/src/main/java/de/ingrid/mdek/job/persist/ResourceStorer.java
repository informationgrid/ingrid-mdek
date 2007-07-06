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
