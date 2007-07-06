package de.ingrid.mdek.job.persist;

import java.util.Iterator;

import org.springframework.core.io.FileSystemResource;

public interface IResourceLoader extends Iterator<FileSystemResource> {

	public boolean hasNext();

	public FileSystemResource next();

}
