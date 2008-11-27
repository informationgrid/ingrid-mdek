package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
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
	private static String MDEK_IDC_CATALOG_JOB_ID = "de.ingrid.mdek.job.MdekIdcCatalogJob";

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
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeCatalog(String plugId, IngridDocument catalogDoc,
			boolean refetchAfterStore,
			String userId) {
		catalogDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		catalogDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeCatalog", catalogDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSysLists(String plugId, Integer[] listIds, String language,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_LIST_IDS, listIds);
		jobParams.put(MdekKeys.LANGUAGE, language);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSysLists", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSysGuis(String plugId, String[] guiIds, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GUI_IDS, guiIds);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSysGuis", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeSysGuis(String plugId, List<IngridDocument> sysGuis,
			boolean refetchAfterStore,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GUI_LIST, sysGuis);
		jobParams.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeSysGuis", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("exportObjectBranch", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportObjects(String plugId, String exportCriteria,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.EXPORT_CRITERIA, exportCriteria);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("exportObjects", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("exportAddressBranch", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}
}
