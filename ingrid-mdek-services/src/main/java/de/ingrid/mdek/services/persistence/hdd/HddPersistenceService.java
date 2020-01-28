/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HddPersistenceService<T extends Serializable> implements
		IHddPersistence<T> {

	private static final String SUFFIX = ".ser";

	private static final Logger LOG = LogManager.getLogger(HddPersistenceService.class);

	private final File _persistenceFolder;

	private class SerFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(SUFFIX);
		}
	}

	public HddPersistenceService(File persistenceFolder) throws FileNotFoundException {
	    if (!persistenceFolder.exists()) {
            throw new FileNotFoundException("Directory " + persistenceFolder + " does not exist.");
        }
        if (!persistenceFolder.isDirectory()) {
            throw new FileNotFoundException("Path " + persistenceFolder + " is not a directory.");
        }
        if (!persistenceFolder.canRead()) {
            throw new FileNotFoundException("No permission to access directory " + persistenceFolder + ".");
        }
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
				stream.close();
			}
		} catch (FileNotFoundException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("can load object, because file does not exists", e);
			}
		} catch (IOException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("can load object", e);
			}
		} catch (ClassNotFoundException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("can load object (class not found)", e);
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isWarnEnabled()) {
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
			if (LOG.isErrorEnabled()) {
				LOG.error("can load object (file not found)", e);
			}
			if (shouldExists) {
				throw new IOException(e.getMessage());
			}
		} catch (IOException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("can load object", e);
			}
			if (shouldExists) {
				throw new IOException(e.getMessage());
			}
		} catch (ClassNotFoundException e) {
			if (LOG.isErrorEnabled()) {
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
					if (LOG.isWarnEnabled()) {
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
			if (LOG.isInfoEnabled()) {
				LOG.warn("save object to file [" + file.getAbsolutePath()+ "]");
			}

			if (!file.exists()) {
				outputStream = new FileOutputStream(file);
				stream = new ObjectOutputStream(outputStream);
				stream.writeObject(object);
				stream.close();
			} else {
				if (LOG.isWarnEnabled()) {
					LOG.warn("can not persist object, because file already exists");
				}
				throw new IOException(
						"can not persist object, because file already exists");
			}
		} catch (FileNotFoundException e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("can not persist object", e);
			}
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("can not persist object", e);
			}
			throw new IOException(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("can not close stream for persist object", e);
					}
				}
			}
		}

	}

	public void makeTransient(String id) throws IOException {
		File file = new File(_persistenceFolder, id + SUFFIX);
		if (file.exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.warn("Delete file [" + file.getAbsolutePath() + "]");
			}
			boolean success = file.delete();
			if (!success) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("can not delete file [" + file.getAbsolutePath()
							+ "]");
				}
			}
		} else {
			if (LOG.isWarnEnabled()) {
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
				if (LOG.isWarnEnabled()) {
					LOG.warn("can not delete file [" + file.getAbsolutePath()
							+ "]");
				}
			}
		}
	}
}
