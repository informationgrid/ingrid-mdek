package de.ingrid.mdek.job.persist;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.core.io.FileSystemResource;

public class ResourceLoader implements IResourceLoader {

	private static final Logger LOG = Logger.getLogger(ResourceLoader.class);

	private final File _persistenceFolder;

	private Iterator<File> _iterator;

	private class XmlFileFilter implements FileFilter {

		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}

	}

	public ResourceLoader(File persistenceFolder) {
		_persistenceFolder = persistenceFolder;
		File[] files = _persistenceFolder.listFiles(new XmlFileFilter());
		_iterator = Arrays.asList(files).iterator();
	}

	public boolean hasNext() {
		return _iterator.hasNext();
	}

	public FileSystemResource next() {
		File file = _iterator.next();
		if (LOG.isInfoEnabled()) {
			LOG.info("load bean definition [" + file.getAbsolutePath() + "]");
		}
		return new FileSystemResource(file);
	}

	public void remove() {
		throw new UnsupportedOperationException(
				"removing of bean resource is not supportet");
	}

	public static void main(String[] args) {
		new DefaultBeanDefinitionDocumentReader().registerBeanDefinitions(null,
				null);
	}
}
