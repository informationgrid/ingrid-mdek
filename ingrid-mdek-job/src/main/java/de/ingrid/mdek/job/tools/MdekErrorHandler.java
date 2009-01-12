package de.ingrid.mdek.job.tools;

import org.apache.log4j.Logger;
import org.hibernate.StaleStateException;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;


/**
 * Handles Mdek Exceptions.
 */
public class MdekErrorHandler {

	private static final Logger LOG = Logger.getLogger(MdekErrorHandler.class);

	private static MdekErrorHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekErrorHandler getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekErrorHandler();
	      }
		return myInstance;
	}

	private MdekErrorHandler() {}
	
	/**
	 * Transform Exception to a Mdek Exception if error is known.
	 * @return MdekException or exception as it was passed 
	 */
	public RuntimeException handleException(RuntimeException excIn) {
		RuntimeException retExc = excIn;

		if (excIn instanceof MdekException) {
			// do nothing, we already have an identified error !
			
		} else  {
//			if (LOG.isDebugEnabled()) {
				LOG.warn("EXCEPTION: " + excIn);
//			}

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
		boolean removeRunningJob = true;

		if (excIn instanceof MdekException) {
			MdekException mdekExc = (MdekException) excIn;
			if (mdekExc.containsError(MdekErrorType.USER_HAS_RUNNING_JOBS)) {
				removeRunningJob = false;
			}
		}
		return removeRunningJob;
	}
}
