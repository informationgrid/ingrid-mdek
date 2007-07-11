package de.ingrid.mdek.services.persistence.hdd;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class HddPersistenceService<T extends Serializable> implements
		IHddPersistence<T> {

	private static final String SUFFIX = ".ser";

	private static final Logger LOG = Logger
			.getLogger(HddPersistenceService.class);

	private final File _persistenceFolder;

	private class SerFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(SUFFIX);
		}
	}

	public HddPersistenceService(File persistenceFolder) {
		_persistenceFolder = persistenceFolder;
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		File[] files = _persistenceFolder.listFiles(new SerFileFilter());
		List<T> list = new ArrayList<T>();
		ObjectInputStream stream = null;
		try {
			for (File file : files) {
				stream = new ObjectInputStream(new FileInputStream(file));
				Object object = stream.readObject();
				list.add((T) object);
			}
		} catch (FileNotFoundException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object, because file does not exists", e);
			}
		} catch (IOException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object", e);
			}
		} catch (ClassNotFoundException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object (class not found)", e);
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isEnabledFor(Level.WARN)) {
						LOG.error("can close stream for loading objects", e);
					}
				}
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public T findById(String id, boolean shouldExists) throws IOException {
		File file = new File(_persistenceFolder, id + SUFFIX);
		ObjectInputStream stream = null;
		T t = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(file));
			t = (T) stream.readObject();
		} catch (FileNotFoundException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object (file not found)", e);
			}
			if (shouldExists) {
				throw new IOException(e.getMessage());
			}
		} catch (IOException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object", e);
			}
			if (shouldExists) {
				throw new IOException(e.getMessage());
			}
		} catch (ClassNotFoundException e) {
			if (LOG.isEnabledFor(Level.ERROR)) {
				LOG.error("can load object (class not found)", e);
			}
			if (shouldExists) {
				throw new IOException(e.getMessage());
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isEnabledFor(Level.WARN)) {
						LOG.error("can close stream for loading object", e);
					}
				}
			}
		}
		return t;
	}

	public void makePersistent(String id, T object) throws IOException {
		FileOutputStream outputStream = null;
		ObjectOutputStream stream = null;
		try {
			File file = new File(_persistenceFolder, id + SUFFIX);
			if (LOG.isEnabledFor(Level.INFO)) {
				LOG
						.warn("save object to file [" + file.getAbsolutePath()
								+ "]");
			}

			if (!file.exists()) {
				outputStream = new FileOutputStream(file);
				stream = new ObjectOutputStream(outputStream);
				stream.writeObject(object);
				stream.close();
			} else {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG
							.warn("can not persist object, because file already exists");
				}
				throw new IOException(
						"can not persist object, because file already exists");
			}
		} catch (FileNotFoundException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("can not persist object", e);
			}
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("can not persist object", e);
			}
			throw new IOException(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isEnabledFor(Level.WARN)) {
						LOG.warn("can not close stream for persist object", e);
					}
				}
			}
		}

	}

	public void makeTransient(String id) throws IOException {
		File file = new File(_persistenceFolder, id + SUFFIX);
		if (file.exists()) {
			if (LOG.isEnabledFor(Level.INFO)) {
				LOG.warn("Delete file [" + file.getAbsolutePath() + "]");
			}
			boolean success = file.delete();
			if (!success) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn("can not delete file [" + file.getAbsolutePath()
							+ "]");
				}
			}
		} else {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("can not delete object [" + id
						+ "] because can not find the file.");
			}
			throw new IOException("can not delete object [" + id
					+ "] because can not find the file.");
		}
	}

	public void deleteAllPersistentObjects() {
		File[] files = _persistenceFolder.listFiles(new SerFileFilter());
		for (File file : files) {
			if (LOG.isInfoEnabled()) {
				LOG.info("delete file [" + file.getAbsolutePath() + "]");
			}
			boolean success = file.delete();
			if (!success) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn("can not delete file [" + file.getAbsolutePath()
							+ "]");
				}
			}
		}
	}
}
