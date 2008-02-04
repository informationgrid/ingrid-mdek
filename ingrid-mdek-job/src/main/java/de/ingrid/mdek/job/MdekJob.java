package de.ingrid.mdek.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekErrorHandler;
import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekErrors.MdekError;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of mdek jobs encapsulating common stuff and default behaviour
 * 
 * @author Martin
 */
public abstract class MdekJob implements IJob {

	private static final Logger LOG = Logger.getLogger(MdekJob.class);

	protected MdekErrorHandler errorHandler;	
	private Map<String, IngridDocument> runningJobsMap;

	public MdekJob() {
		errorHandler = MdekErrorHandler.getInstance();
		runningJobsMap = Collections.synchronizedMap(new HashMap<String, IngridDocument>());
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
		IngridDocument result = new IngridDocument();

		String userId = params.getString(MdekKeys.USER_ID);
		return getRunningJobInfo(userId);
	}

	/** Called from Client */
	public IngridDocument cancelRunningJob(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		String userId = params.getString(MdekKeys.USER_ID);
		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			runningJob.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, true);
			result = runningJob;
		}
		
		return result;
	}

	/** THROWS EXCEPTION IF USER NOT SET in passed doc */
	protected String getCurrentUserId(IngridDocument inDoc) {
		String userId = inDoc.getString(MdekKeys.USER_ID);
		if (userId == null) {
			throw new MdekException(MdekError.USER_ID_NOT_SET);
		}
		
		return userId;
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
		IngridDocument runningJob = new IngridDocument();
		runningJob.put(MdekKeys.RUNNINGJOB_DESCRIPTION, jobDescr);
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES, numProcessed);
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES, numTotal);
		runningJob.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, canceledByUser);
		
		return runningJob;
	}

	/** THROWS EXCEPTION IF USER ALREADY HAS A RUNNING JOB */
	protected void addRunningJob(String userId, IngridDocument jobDescr) {
		// first check whether there is already a running job
		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			// TODO: transfer info about running job !
			throw new MdekException(MdekError.USER_HAS_RUNNING_JOBS);			
		}

		runningJobsMap.put(userId, jobDescr);
	}

	/** NO checks whether jobs are already running !<br>
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	protected void updateRunningJob(String userId, IngridDocument jobDescr) {
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);

		runningJobsMap.put(userId, jobDescr);
	}

	/** NOTICE: returns empty Document if no running job ! */
	protected IngridDocument getRunningJobInfo(String userId) {
		IngridDocument result = new IngridDocument();

		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			result = runningJob;
		}
		
		return result;
	}

	protected void removeRunningJob(String userId) {
		runningJobsMap.put(userId, null);
	}

	/** THROWS EXCEPTION if job canceled */
	private void checkRunningJobCanceledByUser(String userId) {
		IngridDocument runningJob = getRunningJobInfo(userId);
		Boolean wasCanceled = (Boolean) runningJob.get(MdekKeys.RUNNINGJOB_CANCELED_BY_USER);
		wasCanceled = (wasCanceled == null) ? false : wasCanceled;

		if (wasCanceled) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Job " + runningJob.get(MdekKeys.RUNNINGJOB_DESCRIPTION) + " was canceled by user !");
			}
			throw new MdekException(MdekError.USER_CANCELED_JOB);
		}
	}
}
