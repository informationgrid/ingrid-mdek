package de.ingrid.mdek;

import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;

import de.ingrid.mdek.MdekErrors.MdekError;


public class MdekErrorHandler {

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
			
		} else if (excIn instanceof StaleStateException) {
			// database version is different, someone else changed entity
			retExc = new MdekException(MdekError.ENTITY_CHANGED_IN_BETWEEN);
			
		} else if (excIn instanceof ConstraintViolationException) {
			// Unique constraint violated, someone else added/updated entity
			retExc = new MdekException(MdekError.ENTITY_CHANGED_IN_BETWEEN);
		}

		// TODO: handle other types of exceptions e.g. hibernate exceptions ...
		
		return retExc;
	}
}
