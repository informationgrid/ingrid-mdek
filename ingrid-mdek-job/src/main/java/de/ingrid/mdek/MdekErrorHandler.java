package de.ingrid.mdek;

import org.apache.log4j.Logger;


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
		}
		// TODO: handle other types of exceptions e.g. hibernate exceptions ...
		
		return retExc;
	}
}
