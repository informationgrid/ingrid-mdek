package de.ingrid.mdek;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning CATALOG data.
 * 
 * @author Martin
 */
public class MdekCallerCatalog extends MdekCallerAbstract implements IMdekCallerCatalog {

	private final static Logger log = Logger.getLogger(MdekCallerCatalog.class);

	private static MdekCallerCatalog myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	// TODO: better create separate job for handling catalog stuff (at the moment we use object job !)
	private static String MDEK_IDC_OBJECT_JOB_ID = "de.ingrid.mdek.job.MdekIdcObjectJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerCatalog(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerCatalog() {}

    private MdekCallerCatalog(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerCatalog getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerCatalog.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument fetchCatalog(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getCatalog", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getSysLists(String plugId, Integer[] listIds, Integer langCode,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_LIST_IDS, listIds);
		jobParams.put(MdekKeys.LANGUAGE_CODE, langCode);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSysLists", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}
}
