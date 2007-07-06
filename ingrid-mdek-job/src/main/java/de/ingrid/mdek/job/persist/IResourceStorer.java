package de.ingrid.mdek.job.persist;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;

public interface IResourceStorer {

	void storeResource(String name, ByteArrayResource xml) throws IOException;
}
