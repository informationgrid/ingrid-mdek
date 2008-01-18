package de.ingrid.mdek.job.repository;

import de.ingrid.utils.IngridDocument;

public interface IJobRepository {

	public static final String JOB_PERSIST = "job_persist";

	public static final String JOB_ID = "job_id";

	public static final String JOB_DESCRIPTION = "job_description";

	public static final String JOB_REGISTER_SUCCESS = "job_register_success";

	public static final String JOB_REGISTER_ERROR_MESSAGE = "job_register_error_message";

	public static final String JOB_DEREGISTER_SUCCESS = "job_deregister_success";

	public static final String JOB_DEREGISTER_ERROR_MESSAGE = "job_deregister_error_message";

	public static final String JOB_INVOKE_SUCCESS = "job_invoke_success";

	public static final String JOB_INVOKE_ERROR_MESSAGE = "job_invoke_error_message";

	public static final String JOB_INVOKE_ERROR_MDEK = "job_invoke_error_mdek";

	public static final String JOB_RESULT = "job_result";

	public static final String JOB_METHODS = "job_methods";

	public static final String JOB_COMMON_ERROR_MESSAGE = "job_common_error_message";

	public static final String JOB_INVOKE_RESULTS = "job_invoke_results";

	IngridDocument register(IngridDocument document);

	IngridDocument deRegister(IngridDocument document);

	IngridDocument invoke(IngridDocument document);

}
