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
package de.ingrid.mdek.job.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.StaleStateException;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;


/**
 * Handles Mdek Exceptions.
 */
public class MdekErrorHandler {

	private static final Logger LOG = LogManager.getLogger(MdekErrorHandler.class);

	private static MdekErrorHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekErrorHandler getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekErrorHandler();
	      }
		return myInstance;
	}

	private MdekErrorHandler() {}

	/** Checks whether the given exception is a "USER_HAS_RUNNING_JOBS" Error (TRUE) or not (FALSE) */
	public boolean isHasRunningJobsException(RuntimeException excIn) {
		if (excIn instanceof MdekException) {
			if (((MdekException) excIn).containsError(MdekErrorType.USER_HAS_RUNNING_JOBS)) {
				return true;
			}
		}		
		return false;
	}

	/** Checks whether the given exception is a "USER_CANCELED_JOB" Error (TRUE) or not (FALSE) */
	private boolean isCanceledByUserException(RuntimeException excIn) {
		if (excIn instanceof MdekException) {
			if (((MdekException) excIn).containsError(MdekErrorType.USER_CANCELED_JOB)) {
				return true;
			}
		}		
		return false;
	}

	/**
	 * Transform Exception to a Mdek Exception if error is known.
	 * @return MdekException or exception as it was passed 
	 */
	public RuntimeException handleException(RuntimeException excIn) {
		RuntimeException retExc = excIn;

		if (excIn instanceof MdekException) {
			// log with info, we already have an identified error !
			LOG.info("Identified MdekException: " + excIn);
			excIn.printStackTrace();
		} else  {
			LOG.error("EXCEPTION: " + excIn);

			if (excIn instanceof StaleStateException) {
				// database version is different, someone else changed entity
				retExc = new MdekException(new MdekError(MdekErrorType.ENTITY_CHANGED_IN_BETWEEN));
				
			}
/*
			else if (excIn instanceof ConstraintViolationException) {
				// Unique constraint violated, someone else added/updated entity
				retExc = new MdekException(new MdekError(MdekErrorType.ENTITY_CHANGED_IN_BETWEEN));
			}
*/
		}
		
		return retExc;
	}

	/** Checks whether the info about currently running job should be removed (job finished)
	 * or not (job still running), dependent from Exception which was thrown. */
	public boolean shouldRemoveRunningJob(RuntimeException excIn) {
		if (isHasRunningJobsException(excIn)) {
			return false;
		}
		return true;
	}

	/** Checks whether the passed exception should be logged !
	 * e.g. No Logging for USER_HAS_RUNNING_JOBS or USER_CANCELED_JOB */
	public boolean shouldLog(RuntimeException excIn) {
		if (isHasRunningJobsException(excIn) ||
				isCanceledByUserException(excIn)) {
			return false;
		}

		return true;
	}
}
