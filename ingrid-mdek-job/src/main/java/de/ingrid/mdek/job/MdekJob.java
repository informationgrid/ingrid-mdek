package de.ingrid.mdek.job;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekErrorHandler;
import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of mdek jobs encapsulating common stuff and default behaviour
 * 
 * @author Martin
 */
public abstract class MdekJob implements IJob {

	private static final Logger LOG = Logger.getLogger(MdekJob.class);

	protected MdekErrorHandler errorHandler;	
	protected MdekJobHandler jobHandler;

	public MdekJob() {
		errorHandler = MdekErrorHandler.getInstance();
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
			throw new MdekException(MdekError.USER_ID_NOT_SET);
		}
		
		return userId;
	}
}
