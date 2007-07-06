package de.ingrid.mdek.job.repository;

import de.ingrid.utils.IngridDocument;

public interface IJobRepository {
	
	public static final String JOB_PERSIST = "job_persist";

	public static final String JOB_ID = "job_id";

	public static final String JOB_DESCRIPTION = "job_description";

	public static final String JOB_ERROR_MESSAGE = "job_error_message";

	public static final String JOB_SUCCESS = "job_success";

	public static final String JOB_RESULT = "job_result";

	public static final Object JOB_METHODS = "job_methods";

	IngridDocument register(IngridDocument document);

	IngridDocument deRegister(IngridDocument document);

	IngridDocument invoke(IngridDocument document);

}
