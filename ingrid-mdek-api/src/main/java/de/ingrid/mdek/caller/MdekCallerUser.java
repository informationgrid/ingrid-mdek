package de.ingrid.mdek.caller;

import org.apache.log4j.Logger;



/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning USER MANAGEMENT (permissions etc.).
 */
public class MdekCallerUser extends MdekCallerAbstract implements IMdekCallerUser {

	private final static Logger log = Logger.getLogger(MdekCallerUser.class);

	private static MdekCallerUser myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	private static String MDEK_IDC_USER_JOB_ID = "de.ingrid.mdek.job.MdekIdcUserJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerUser(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerUser() {}

    private MdekCallerUser(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerUser getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerUser.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}
}
