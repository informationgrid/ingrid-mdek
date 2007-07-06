package de.ingrid.mdek.job.register;

import java.io.IOException;

import de.ingrid.mdek.job.IJob;

public interface IRegistrationService {

	void register(String jobId, String xml, boolean persist) throws IOException;

	void deRegister(String jobId);

	IJob getRegisteredJob(String jobId);

}
