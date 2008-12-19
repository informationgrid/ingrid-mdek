package de.ingrid.mdek.services.utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.ISysJobInfoDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.utils.IngridDocument;


/**
 * Handles synchronization of different job methods.
 */
public class MdekJobHandler {

	private static final Logger LOG = Logger.getLogger(MdekJobHandler.class);

	private Map<String, IngridDocument> runningJobsMap;

	private ISysJobInfoDao daoSysJobInfo;

	private static MdekJobHandler myInstance;

    private XStream xstream;

	/** Get The Singleton */
	public static synchronized MdekJobHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekJobHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekJobHandler(DaoFactory daoFactory) {
		runningJobsMap = Collections.synchronizedMap(new HashMap<String, IngridDocument>());

		daoSysJobInfo = daoFactory.getSysJobInfoDao();
		
        try {
            xstream = new XStream();
        } catch (Throwable ex) {
        	LOG.error("Initial Xstream creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
	}

	/** Called from Client */
	public IngridDocument cancelRunningJob(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		String userId = MdekJobHandler.getCurrentUserUuidFromDoc(params);

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

	/** NOTICE: returns empty Document if no running job ! */
	public IngridDocument getRunningJobInfo(IngridDocument params) {
		String userId = MdekJobHandler.getCurrentUserUuidFromDoc(params);
		return getRunningJobInfo(userId);
	}
	/** NOTICE: returns empty Document if no running job ! */
	public IngridDocument getRunningJobInfo(String userId) {
		IngridDocument result = new IngridDocument();

		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			result = runningJob;
		}
		
		return result;
	}

	/**
	 * Create a document describing a job.
	 * @param JobType what type of Job/Operation
	 * @param numProcessed number of already processed entities
	 * @param numTotal total number of entities to be processed 
	 * @param canceledByUser was this job canceled by user ? 
	 * @return document describing current state of job
	 */
	public IngridDocument createRunningJobDescription(JobType jobType,
			Integer numProcessed,
			Integer numTotal,
			boolean canceledByUser) {
		IngridDocument runningJob = new IngridDocument();
		runningJob.put(MdekKeys.RUNNINGJOB_DESCRIPTION, jobType.getDbValue());
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES, numProcessed);
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES, numTotal);
		runningJob.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, canceledByUser);
		
		return runningJob;
	}

	/** THROWS EXCEPTION IF USER ALREADY HAS A RUNNING JOB */
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

	/** Add keys in passed map to current job information.<br> 
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	public void updateRunningJob(String userId, Map additionalJobInfo) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId + ", jobDescr: " + additionalJobInfo);
		}
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);
		
		IngridDocument jobInfo = getRunningJobInfo(userId);
		jobInfo.putAll(additionalJobInfo);

		runningJobsMap.put(userId, jobInfo);
	}
	/** Add new message to current job information.<br> 
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	public void updateRunningJobMessages(String userId, String newMessage) {
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);

		IngridDocument jobInfo = getRunningJobInfo(userId);

		String currentMessages = jobInfo.getString(MdekKeys.RUNNINGJOB_MESSAGES);
		if (currentMessages == null) {
			currentMessages = "";
		} else if (!currentMessages.endsWith("\n")) {
			currentMessages += "\n";
		}
		currentMessages += newMessage;

		jobInfo.put(MdekKeys.RUNNINGJOB_MESSAGES, currentMessages);

		runningJobsMap.put(userId, jobInfo);
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

	/**
	 * Return the AddressUuid of the user set in passed doc.
	 * THROWS EXCEPTION IF USER NOT SET in passed doc.
	 * @param inDoc
	 * @return
	 */
	public static String getCurrentUserUuidFromDoc(IngridDocument inDoc) {
		String userId = inDoc.getString(MdekKeys.USER_ID);
		if (userId == null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_ID_NOT_SET));
		}
		
		return userId;
	}

	/** "logs" Start-Info in job information IN DATABASE */
	public void startJobInfoDB(JobType whichJob, String startTime,
			HashMap jobDetails, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);
		if (jobInfo == null) {
			jobInfo = new SysJobInfo();
			jobInfo.setJobType(whichJob.getDbValue());
			jobInfo.setUserUuid(userUuid);
		}
		jobInfo.setStartTime(startTime);
		jobInfo.setEndTime(null);
		jobInfo.setJobDetails(formatJobDetailsForDB(jobDetails));
		
		persistJobInfoDB(jobInfo, userUuid);
	}

	/** Updates job information IN DATABASE, meaning add all info of passed map (keys) !
	 * NOTICE: info in database not contained in map stays unchanged ! */
	public void updateJobInfoDB(JobType whichJob, HashMap additionalJobDetails, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);

		HashMap jobDetails = deformatJobDetailsFromDB(jobInfo.getJobDetails());
		jobDetails.putAll(additionalJobDetails);
		jobInfo.setJobDetails(formatJobDetailsForDB(jobDetails));

		persistJobInfoDB(jobInfo, userUuid);
	}
	/** Adds a message to the job information IN DATABASE !
	 * NOTICE: all other infos in DB stay unchanged ! */
	public void updateJobInfoDBMessages(JobType whichJob, String newMessage, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);

		HashMap jobDetails = deformatJobDetailsFromDB(jobInfo.getJobDetails());

		String currentMessages = (String) jobDetails.get(MdekKeys.JOBINFO_MESSAGES);
		if (currentMessages == null) {
			currentMessages = "";
		} else if (!currentMessages.endsWith("\n")) {
			currentMessages += "\n";
		}
		currentMessages += newMessage;

		jobDetails.put(MdekKeys.JOBINFO_MESSAGES, currentMessages);

		jobInfo.setJobDetails(formatJobDetailsForDB(jobDetails));
		persistJobInfoDB(jobInfo, userUuid);
	}
	/** Persists given JobInfo */
	public void persistJobInfoDB(SysJobInfo jobInfo, String userUuid) {
		daoSysJobInfo.makePersistent(jobInfo);
	}

	/** "logs" End-Info in job information IN DATABASE */
	public void endJobInfoDB(JobType whichJob, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);
		jobInfo.setEndTime(MdekUtils.dateToTimestamp(new Date()));
		
		persistJobInfoDB(jobInfo, userUuid);
	}

	/** NOTICE: if passed details are NULL writes NULL into DB ! */
	public String formatJobDetailsForDB(HashMap jobDetailsForDB) {
		if (jobDetailsForDB == null) {
			return null;			
		}
		return xstream.toXML(jobDetailsForDB);
	}
	/** NOTICE: returns empty HashMap if passed details from DB are null ! */
	public HashMap deformatJobDetailsFromDB(String jobDetailsFromDB) {
		if (jobDetailsFromDB == null) {
			return new HashMap();
		}
		return (HashMap) xstream.fromXML(jobDetailsFromDB);
	}
	
	/** Returns job information "logged" IN DATABASE.
	 * NOTICE: JobDetails are still in Database format !*/
	public SysJobInfo getJobInfoDB(JobType whichJob, String userUuid) {
		// auto flushing may be disabled ! we flush before query so database is up to date !
		daoSysJobInfo.flush();
		return daoSysJobInfo.getJobInfo(whichJob, userUuid);
	}
	/** Map given jobInfo to Map */
	public HashMap mapJobInfoDB(SysJobInfo jobInfo) {
        HashMap resultMap = new HashMap();
		if (jobInfo != null) {
			HashMap jobDetails = (HashMap) deformatJobDetailsFromDB(jobInfo.getJobDetails());
			resultMap.putAll(jobDetails);
			resultMap.put(MdekKeys.JOBINFO_START_TIME, jobInfo.getStartTime());
			resultMap.put(MdekKeys.JOBINFO_END_TIME, jobInfo.getEndTime());			
		}

		return resultMap;
	}
}
