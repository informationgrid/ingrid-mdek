/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekException;
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
		return createRunningJobDescription(jobType, null, numProcessed, numTotal, canceledByUser);
	}

	/**
	 * Create a document describing a job.
	 * NOTICE: Adds only basic data (passed stuff) which then can be extended !
	 * @param JobType what type of Job/Operation
	 * @param whichType what kind of entities are processed (objects or addresses)
	 * @param numProcessed number of already processed entities
	 * @param numTotal total number of entities to be processed 
	 * @param canceledByUser was this job canceled by user ? 
	 * @return document describing current state of job
	 */
	public IngridDocument createRunningJobDescription(JobType jobType,
			String entityType, 
			Integer numProcessed,
			Integer numTotal,
			boolean canceledByUser) {
		IngridDocument runningJob = new IngridDocument();
		runningJob.put(MdekKeys.RUNNINGJOB_TYPE, jobType.getDbValue());

		runningJob.put(MdekKeys.RUNNINGJOB_ENTITY_TYPE, entityType);
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES, numProcessed);
		runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES, numTotal);

		// also differentiate objects and addresses
		if (entityType == IdcEntityType.OBJECT.getDbValue()) {
			runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS, numProcessed);
			runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS, numTotal);
		} else if(entityType == IdcEntityType.ADDRESS.getDbValue()){
			runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES, numProcessed);
			runningJob.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES, numTotal);
		}

		runningJob.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, canceledByUser);
		
		return runningJob;
	}

	/**
	 * Extracts the basic running job info from the Full Info Map (which may contain all kind of data,
	 * e.g. import nodes when import job is running).
	 * @param fullJobInfo full running job info from getRunningJobInfo()
	 * @return reduced running job info ready for transport to IGE !
	 */
	public IngridDocument extractRunningJobDescription(IngridDocument fullJobInfo) {
		IngridDocument baseJobInfo = new IngridDocument();
		for (String key : MdekKeys.RUNNINGJOB_BASIC_KEYS) {
			if (fullJobInfo.containsKey(key)) {
				baseJobInfo.put(key, fullJobInfo.get(key));
			}
		}
		
		return baseJobInfo;
	}

	/** THROWS EXCEPTION IF USER ALREADY HAS A RUNNING JOB */
	public void addRunningJob(String userId, IngridDocument jobDescr) {
		// first check whether there is already a running job
		IngridDocument runningJob = getRunningJobInfo(userId);
		if (!runningJob.isEmpty()) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_RUNNING_JOBS));			
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId + ", jobDescr: " + jobDescr);
		}

		runningJobsMap.put(userId, jobDescr);
	}

	/** return ANY running job information, no matter which job. 
	 * NOTICE: returns EMPTY Document if no running job ! */
	public IngridDocument getRunningJobInfo(String userId) {
		return getRunningJobInfo(null, userId);
	}
	/** return SPECIFIC running job information, only jobs of passed type ! 
	 * @param jobType pass null if type doen't matter, any running job should be fetched 
	 * @param userId user who started job
	 * @return running job info.
	 * 		NOTICE: returns NEW UNMANAGED EMPTY Document if no running job of passed type.
	 * 		This document has to be stored with updateRunningJob(...) to be "managed" !!!
	 */
	public IngridDocument getRunningJobInfo(JobType jobType, String userId) {
		IngridDocument runningJob = runningJobsMap.get(userId);
		if (runningJob != null) {
			// only return specific job type ?
			if (jobType != null) {
				if (!jobType.getDbValue().equals(runningJob.get(MdekKeys.RUNNINGJOB_TYPE))) {
					runningJob = null;
				}
			}
		}
		
		if (runningJob == null) {
			runningJob = new IngridDocument();
		}
		
		return runningJob;
	}

	/** Add keys in passed map to current job information.<br> 
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	public void updateRunningJob(String userId, Map additionalJobInfo) {
/*
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateRunningJob: userId:" + userId + ", jobDescr: " + additionalJobInfo);
		}
*/
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);
		
		IngridDocument jobInfo = getRunningJobInfo(userId);
		// NOTICE: we do NOT sync number of ADDRESSES/OBJECTS with number of ENTITIES !
		// So entities does NOT contain sum of addresses and objects !
		jobInfo.putAll(additionalJobInfo);

		runningJobsMap.put(userId, jobInfo);
	}
	/** Add new protocol message to current job information.<br>
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	public void updateRunningJobMessages(String userId, String newMessage) {
		updateRunningJobMessages(userId, MdekKeys.RUNNINGJOB_MESSAGES, newMessage);
	}
	/** Add new frontend message to current job information.<br>
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	public void updateRunningJobFrontendMessages(String userId, String newMessage) {
		updateRunningJobMessages(userId, MdekKeys.RUNNINGJOB_FRONTEND_MESSAGES, newMessage);
	}
	/** Add new message to current job information.<br>
	 * NOTICE: NO checks whether jobs are already running !
	 * BUT CHECKS WHETHER JOB WAS CANCELED ! and throws exception if canceled ! */
	private void updateRunningJobMessages(String userId, String messageTypeKey, String newMessage) {
		// throws exception if canceled !
		checkRunningJobCanceledByUser(userId);

		IngridDocument jobInfo = getRunningJobInfo(userId);

		String currentMessages = jobInfo.getString(messageTypeKey);
		if (currentMessages == null) {
			currentMessages = "";
		} else if (!currentMessages.endsWith("\n")) {
			currentMessages += "\n";
		}
		currentMessages += newMessage;

		jobInfo.put(messageTypeKey, currentMessages);

		runningJobsMap.put(userId, jobInfo);
	}

	/**
	 * Cancel the current running job of the passed user.
	 * @param userId running job of this user is canceled
	 * @return the FULL information of the current running job which was canceled
	 * 		or empty document if no running job ! 
	 */
	public IngridDocument cancelRunningJob(String userId) {
		IngridDocument fullInfo = getRunningJobInfo(userId);
		if (!fullInfo.isEmpty()) {
			// only add if there is a running job 
			fullInfo.put(MdekKeys.RUNNINGJOB_CANCELED_BY_USER, true);			
		}

		return fullInfo;
	}

	/**
	 * Remove all Running Job data of the passed user.
	 * NOTICE: Strictly removes ALL DATA, NO check whether canceled etc.
	 * THIS ONE SHOULD BE CALLED FROM JOB WHEN JOB IS FINISHED !!!
	 * @param userId
	 */
	public void removeRunningJob(String userId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userId:" + userId);
		}
		runningJobsMap.remove(userId);
	}

	/** THROWS EXCEPTION if job canceled */
	private void checkRunningJobCanceledByUser(String userId) {
		IngridDocument runningJob = getRunningJobInfo(userId);
		Boolean wasCanceled = (Boolean) runningJob.get(MdekKeys.RUNNINGJOB_CANCELED_BY_USER);
		wasCanceled = (wasCanceled == null) ? false : wasCanceled;

		if (wasCanceled) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Job " + runningJob.get(MdekKeys.RUNNINGJOB_TYPE) + " was canceled by user !");
			}
			throw new MdekException(new MdekError(MdekErrorType.USER_CANCELED_JOB));
		}
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
/*
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
*/
	/** Logs the given Exception (and replaces an existing one) in the job information IN DATABASE !
	 * NOTICE: all other infos in DB stay unchanged ! */
	public void updateJobInfoDBException(JobType whichJob, Exception exceptionToLog, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);

		HashMap jobDetails = deformatJobDetailsFromDB(jobInfo.getJobDetails());

		jobDetails.put(MdekKeys.JOBINFO_EXCEPTION, exceptionToLog);

		jobInfo.setJobDetails(formatJobDetailsForDB(jobDetails));
		persistJobInfoDB(jobInfo, userUuid);
	}
	/** "logs" End-Info in job information IN DATABASE */
	public void endJobInfoDB(JobType whichJob, String userUuid) {
		SysJobInfo jobInfo = getJobInfoDB(whichJob, userUuid);
		jobInfo.setEndTime(MdekUtils.dateToTimestamp(new Date()));
		
		persistJobInfoDB(jobInfo, userUuid);
	}

	/** Persists given JobInfo */
	private void persistJobInfoDB(SysJobInfo jobInfo, String userUuid) {
		daoSysJobInfo.makePersistent(jobInfo);
	}

	/**
	 * Maps Info from passed RunningJobInfo to map to be stored as job details.
	 * @param runningJobInfo info from running job
	 * @param includeMessages also map messages ? (or only basic data)
	 * @return map containing job details information
	 */
	public HashMap getJobInfoDetailsFromRunningJobInfo(HashMap runningJobInfo,
			boolean includeMessages) {

		// set up BASIC job info details just like it would be stored in DB
        HashMap jobDetails = setUpJobInfoDetailsDB(
        		(String) runningJobInfo.get(MdekKeys.RUNNINGJOB_ENTITY_TYPE),
        		(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES),
        		(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES));

        // add explicit object/address counter from running job !
        jobDetails.put(MdekKeys.JOBINFO_TOTAL_NUM_OBJECTS,
        	(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS));
        jobDetails.put(MdekKeys.JOBINFO_NUM_OBJECTS,
        	(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS));
        jobDetails.put(MdekKeys.JOBINFO_TOTAL_NUM_ADDRESSES,
        	(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES));
        jobDetails.put(MdekKeys.JOBINFO_NUM_ADDRESSES,
        	(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES));

        // also add start time from running job if present
        if (runningJobInfo.containsKey(MdekKeys.JOBINFO_START_TIME)) {
            jobDetails.put(MdekKeys.JOBINFO_START_TIME, runningJobInfo.get(MdekKeys.JOBINFO_START_TIME));        	
        }
        
        if (includeMessages) {
            jobDetails.put(MdekKeys.JOBINFO_MESSAGES, runningJobInfo.get(MdekKeys.RUNNINGJOB_MESSAGES));        	
            jobDetails.put(MdekKeys.JOBINFO_FRONTEND_MESSAGES, runningJobInfo.get(MdekKeys.RUNNINGJOB_FRONTEND_MESSAGES));        	
        }
		
		return jobDetails;
	}

	/** Set up BASIC generic details to be stored in database.
	 * @param entityType type of entity, pass IdcEntityType.getDbValue() or arbitrary string if other entity !
	 */
	public HashMap setUpJobInfoDetailsDB(String entityType, int num, int totalNum) {
        HashMap details = new HashMap();
    	details.put(MdekKeys.JOBINFO_ENTITY_TYPE, entityType);

    	// always set generic entities
        details.put(MdekKeys.JOBINFO_TOTAL_NUM_ENTITIES, totalNum);
        details.put(MdekKeys.JOBINFO_NUM_ENTITIES, num);

		// also differentiate objects and addresses
        IdcEntityType whichType = EnumUtil.mapDatabaseToEnumConst(IdcEntityType.class, entityType);
        if (whichType == IdcEntityType.OBJECT) {
            details.put(MdekKeys.JOBINFO_TOTAL_NUM_OBJECTS, totalNum);        	
            details.put(MdekKeys.JOBINFO_NUM_OBJECTS, num);
        } else if (whichType == IdcEntityType.ADDRESS) {
            details.put(MdekKeys.JOBINFO_TOTAL_NUM_ADDRESSES, totalNum);        	
            details.put(MdekKeys.JOBINFO_NUM_ADDRESSES, num);
        }
		
        return details;
	}

	/** NOTICE: if passed details are NULL returns NULL ! */
	private String formatJobDetailsForDB(HashMap jobDetailsForDB) {
		if (jobDetailsForDB == null) {
			return null;			
		}
		return xstream.toXML(jobDetailsForDB);
	}
	/** NOTICE: returns empty HashMap if passed details from DB are null ! */
	private HashMap deformatJobDetailsFromDB(String jobDetailsFromDB) {
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
	public HashMap getJobDetailsAsHashMap(JobType whichJob, String userUuid) {
	    SysJobInfo myJobInfo = getJobInfoDB(whichJob, userUuid);
	    return deformatJobDetailsFromDB( myJobInfo.getJobDetails() );
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
