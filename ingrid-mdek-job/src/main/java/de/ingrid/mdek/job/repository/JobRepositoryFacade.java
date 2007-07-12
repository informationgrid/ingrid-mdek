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

		Object jobId = get(document, IJobRepository.JOB_ID, null);
		Object jobXml = get(document, IJobRepository.JOB_DESCRIPTION, null);
		boolean jobPersist = getBoolean(document, IJobRepository.JOB_PERSIST,
				false);
		Object jobMethods = get(document, IJobRepository.JOB_METHODS, null);

		if (jobId == null) {
			ret.put(IJobRepository.JOB_COMMON_ERROR_MESSAGE,
					"Job id is not set.");
			return ret;
		}

		if (jobXml != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("register job by job repository [" + jobId + "]");
			}
			IngridDocument registerDocument = _jobRepository.register(document);
			boolean registerSuccess = registerDocument
					.getBoolean(IJobRepository.JOB_REGISTER_SUCCESS);
			ret.putAll(registerDocument);
			if (!registerSuccess) {
				return ret;
			}
		}

		if (jobMethods != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("invoke job by job repository [" + jobId + "]");
			}
			IngridDocument invokeDocument = _jobRepository.invoke(document);
			ret.putAll(invokeDocument);
		}

		if (!jobPersist) {
			if (LOG.isInfoEnabled()) {
				LOG.info("deRegister job by job repository [" + jobId + "]");
			}
			IngridDocument deRegisterDocument = _jobRepository
					.deRegister(document);
			ret.putAll(deRegisterDocument);
		}

		return ret;
	}

	private boolean getBoolean(IngridDocument document, String key,
			boolean defaultValue) {
		boolean ret = defaultValue;
		if (document.containsKey(key)) {
			ret = document.getBoolean(key);
		}
		return ret;
	}

	private Object get(IngridDocument document, String key, Object defaultValue) {
		Object object = defaultValue;
		if (document.containsKey(key)) {
			object = document.get(key);
		}
		return object;
	}
}
