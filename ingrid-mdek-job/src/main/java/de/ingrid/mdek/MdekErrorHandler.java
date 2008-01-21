package de.ingrid.mdek;

import java.util.ConcurrentModificationException;

import org.apache.log4j.Logger;
import org.hibernate.StaleStateException;

import de.ingrid.mdek.MdekErrors.MdekError;


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
		} else if (excIn instanceof StaleStateException) {
			retExc = new MdekException(MdekError.ENTITY_CHANGED_IN_BETWEEN);
		} else if (excIn instanceof ConcurrentModificationException) {
			retExc = new MdekException(MdekError.ENTITY_CHANGED_IN_BETWEEN);			
		}

		// TODO: handle other types of exceptions e.g. hibernate exceptions ...
		
		return retExc;
	}
}
