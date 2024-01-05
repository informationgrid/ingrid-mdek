/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job;

import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.utils.MdekJobHandler;
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

	public void setJobHandler(MdekJobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    public MdekJob(Logger log, DaoFactory daoFactory) {
		this.log = log;

		jobHandler = MdekJobHandler.getInstance(daoFactory);
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

	/** Called from Client (IGE) */
	public IngridDocument getRunningJobInfo(IngridDocument params) {
		String userId = getCurrentUserUuid(params);

		IngridDocument fullInfo = jobHandler.getRunningJobInfo(userId);

		// reduce running job info to stuff which can be transported to IGE !!!
		return jobHandler.extractRunningJobDescription(fullInfo);
	}

	/** Called from Client (IGE) */
	public IngridDocument cancelRunningJob(IngridDocument params) {
		String userId = getCurrentUserUuid(params);

		IngridDocument fullInfo = jobHandler.cancelRunningJob(userId);

		// reduce running job info to stuff which can be transported to IGE !!!
		return jobHandler.extractRunningJobDescription(fullInfo);
	}

	/**
	 * Create a document describing a job.
	 * @param JobType what type of Job/Operation
	 * @param numProcessed number of already processed entities
	 * @param numTotal total number of entities to be processed 
	 * @param canceledByUser was this job canceled by user ? 
	 * @return document describing current state of job
	 */
	protected IngridDocument createRunningJobDescription(JobType jobType,
			Integer numProcessed,
			Integer numTotal,
			boolean canceledByUser) {
		return jobHandler.createRunningJobDescription(jobType,
				numProcessed,
				numTotal,
				canceledByUser);
	}

	/** CALL BEFORE STARTING TRANSACTION !!! THROWS EXCEPTION IF USER ALREADY
	 * HAS A RUNNING JOB !!! */
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

	/**
	 * Return the AddressUuid of the user set in passed job doc.
	 * THROWS EXCEPTION IF USER NOT SET in passed doc.
	 * @param jobDoc the doc as passed to the job
	 * @return userUuid
	 */
	protected String getCurrentUserUuid(IngridDocument jobDoc) {
		String userId = jobDoc.getString(MdekKeys.USER_ID);
		if (userId == null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_ID_NOT_SET));
		}
		
		return userId;
	}
}
