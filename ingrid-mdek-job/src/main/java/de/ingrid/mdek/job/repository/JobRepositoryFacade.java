package de.ingrid.mdek.job.repository;

import org.apache.log4j.Logger;

import de.ingrid.utils.IngridDocument;

public class JobRepositoryFacade implements IJobRepositoryFacade {

	private static final Logger LOG = Logger
			.getLogger(JobRepositoryFacade.class);

	private final IJobRepository _jobRepository;

	public JobRepositoryFacade(IJobRepository jobRepository) {
		_jobRepository = jobRepository;
	}

	@SuppressWarnings("unchecked")
	public IngridDocument execute(IngridDocument document) {

		IngridDocument ret = new IngridDocument();

		String jobId = (String) document.get(IJobRepository.JOB_ID);
		String jobXml = (String) document.get(IJobRepository.JOB_DESCRIPTION);
		boolean persist = document.getBoolean(IJobRepository.JOB_PERSIST);
		boolean registerSuccess = true;

		if (jobXml != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("register job by job repository [" + jobId + "]");
			}
			IngridDocument registerDocument = _jobRepository.register(document);
			registerSuccess = registerDocument
					.getBoolean(IJobRepository.JOB_REGISTER_SUCCESS);
			ret.putAll(registerDocument);
		}

		if (registerSuccess) {
			if (LOG.isInfoEnabled()) {
				LOG.info("invoke job by job repository [" + jobId + "]");
			}
			IngridDocument invokeDocument = _jobRepository.invoke(document);
			ret.putAll(invokeDocument);
		}

		if (!persist && registerSuccess) {
			if (LOG.isInfoEnabled()) {
				LOG.info("deRegister job by job repository [" + jobId + "]");
			}
			IngridDocument deRegisterDocument = _jobRepository
					.deRegister(document);
			ret.putAll(deRegisterDocument);
		}

		return ret;
	}
}
