/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
import java.io.FileFilter;
import java.io.FileNotFoundException;
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

    public ResourceLoader(File persistenceFolder) throws FileNotFoundException {
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
        throw new UnsupportedOperationException("removing of bean resource is not supportet");
    }

    public static void main(String[] args) {
        new DefaultBeanDefinitionDocumentReader().registerBeanDefinitions(null, null);
    }
}
