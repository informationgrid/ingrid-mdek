package de.ingrid.mdek.job;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of mdek jobs encapsulating common stuff and default behaviour.
 */
public abstract class MdekJob implements IJob {

    /** Logger configured via Properties. ONLY if no logger via logservice is specified
     * for same class !. If Logservice logger is specified, the following logger uses
     * Logservice configuration -> writes to separate logfile for this Job. */
//    private final static Log log = LogFactory.getLog(MdekTreeJob.class);

	/** logs in separate File (job specific log file) */
	protected Logger log;

	protected MdekJobHandler jobHandler;

	// Descriptions of running methods when setting up JobDescription to track ! 
	protected String JOB_DESCR_READ = "READ";
	protected String JOB_DESCR_STORE = "STORE";
	protected String JOB_DESCR_PUBLISH = "PUBLISH";
	protected String JOB_DESCR_COPY = "COPY";
	protected String JOB_DESCR_MOVE = "MOVE";
	protected String JOB_DESCR_DELETE = "DELETE";
	protected String JOB_DESCR_CHECK = "CHECK";

	public MdekJob(Logger log) {
		this.log = log;

		jobHandler = MdekJobHandler.getInstance();
	}

	/**
	 * Called when job is registered !
	 * Default Handling here returns empty IngridDocument 
	 * @see de.ingrid.mdek.job.IJob#getResults()
	 */
	public IngridDocument getResults() {
        IngridDocument result = new IngridDocument();
		return result;
	}

	/** Called from Client */
	public IngridDocument getRunningJobInfo(IngridDocument params) {
		return jobHandler.getRunningJobInfo(params);
	}

	/** Called from Client */
	public IngridDocument cancelRunningJob(IngridDocument params) {
		return jobHandler.cancelRunningJob(params);
	}

	/**
	 * Create a document describing a job.
	 * @param jobDescr plain text description of job, e.g. "COPY" ...
	 * @param numProcessed number of already processed entities
	 * @param numTotal total number of entities to be processed 
	 * @param canceledByUser was this job canceled by user ? 
	 * @return document describing current state of job
	 */
	protected IngridDocument createRunningJobDescription(String jobDescr,
			Integer numProcessed,
			Integer numTotal,
			boolean canceledByUser) {
		return jobHandler.createRunningJobDescription(jobDescr,
				numProcessed,
				numTotal,
				canceledByUser);
	}

	/** THROWS EXCEPTION IF USER ALREADY HAS A RUNNING JOB */
	protected void addRunningJob(String userId, IngridDocument jobDescr) {
		jobHandler.addRunningJob(userId, jobDescr);
	}

	/** NO checks whether jobs are already running !<br>
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	protected void updateRunningJob(String userId, IngridDocument jobDescr) {
		jobHandler.updateRunningJob(userId, jobDescr);
	}

	protected void removeRunningJob(String userId) {
		jobHandler.removeRunningJob(userId);
	}

	/** THROWS EXCEPTION IF USER NOT SET in passed doc */
	public static String getCurrentUserId(IngridDocument inDoc) {
		String userId = inDoc.getString(MdekKeys.USER_ID);
		if (userId == null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_ID_NOT_SET));
		}
		
		return userId;
	}
}
