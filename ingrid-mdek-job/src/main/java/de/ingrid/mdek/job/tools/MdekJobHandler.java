package de.ingrid.mdek.job.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.MdekJob;
import de.ingrid.utils.IngridDocument;


/**
 * Handles synchronization of different job methods.
 */
public class MdekJobHandler {

	private static final Logger LOG = Logger.getLogger(MdekJobHandler.class);

	private Map<String, IngridDocument> runningJobsMap;

	private static MdekJobHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekJobHandler getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekJobHandler();
	      }
		return myInstance;
	}

	private MdekJobHandler() {
		runningJobsMap = Collections.synchronizedMap(new HashMap<String, IngridDocument>());
	}

	/** Called from Client */
	public IngridDocument cancelRunningJob(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		String userId = MdekJob.getCurrentUserUuid(params);

		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId);
		}

		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			runningJob.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, true);
			result = runningJob;
		}
		
		return result;
	}

	public IngridDocument getRunningJobInfo(IngridDocument params) {
		String userId = MdekJob.getCurrentUserUuid(params);
		return getRunningJobInfo(userId);
	}

	/** NOTICE: returns empty Document if no running job ! */
	private IngridDocument getRunningJobInfo(String userId) {
		IngridDocument result = new IngridDocument();

		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			result = runningJob;
		}
		
		return result;
	}

	public IngridDocument createRunningJobDescription(String jobDescr,
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

	public void addRunningJob(String userId, IngridDocument jobDescr) {
		// first check whether there is already a running job
		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_RUNNING_JOBS));			
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId + ", jobDescr: " + jobDescr);
		}

		runningJobsMap.put(userId, jobDescr);
	}

	public void updateRunningJob(String userId, IngridDocument jobDescr) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId + ", jobDescr: " + jobDescr);
		}
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);

		runningJobsMap.put(userId, jobDescr);
	}

	public void removeRunningJob(String userId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId);
		}
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
			throw new MdekException(new MdekError(MdekErrorType.USER_CANCELED_JOB));
		}
	}
}
