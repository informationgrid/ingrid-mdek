package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning CATALOG data.
 * 
 * @author Martin
 */
public class MdekCallerCatalog extends MdekCaller implements IMdekCallerCatalog {

	private final static Logger log = Logger.getLogger(MdekCallerCatalog.class);

	private static MdekCallerCatalog myInstance;

	// Jobs
	private static String MDEK_IDC_CATALOG_JOB_ID = "de.ingrid.mdek.job.MdekIdcCatalogJob";

    private MdekCallerCatalog(IMdekClientCaller mdekClientCaller) {
    	super(mdekClientCaller);
    }

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 */
	public static synchronized void initialize(IMdekClientCaller mdekClientCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerCatalog(mdekClientCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
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
		List jobMethods = setUpJobMethod("getCatalog", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeCatalog(String plugId, IngridDocument catalogDoc,
			boolean refetchAfterStore,
			String userId) {
		catalogDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		catalogDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeCatalog", catalogDoc);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSysLists(String plugId, Integer[] listIds, String language,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_LIST_IDS, listIds);
		jobParams.put(MdekKeys.LANGUAGE, language);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSysLists", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeSysList(String plugId,
			int listId, boolean maintainable, Integer defaultEntryIndex,
			Integer[] entryIds, String[] entryNames_de, String[] entryNames_en, 
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.LST_ID, listId);
		jobParams.put(MdekKeys.LST_MAINTAINABLE, maintainable);
		jobParams.put(MdekKeys.LST_DEFAULT_ENTRY_INDEX, defaultEntryIndex);
		jobParams.put(MdekKeys.LST_ENTRY_IDS, entryIds);
		jobParams.put(MdekKeys.LST_ENTRY_NAMES_DE, entryNames_de);
		jobParams.put(MdekKeys.LST_ENTRY_NAMES_EN, entryNames_en);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeSysList", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);		
	}

	public IngridDocument getSysGuis(String plugId, String[] guiIds, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GUI_IDS, guiIds);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSysGuis", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeSysGuis(String plugId, List<IngridDocument> sysGuis,
			boolean refetchAfterStore,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GUI_LIST, sysGuis);
		jobParams.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeSysGuis", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSysGenericKeys(String plugId, String[] keyNames, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GENERIC_KEY_NAMES, keyNames);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSysGenericKeys", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument storeSysGenericKeys(String plugId,
			String[] keyNames, String[] keyValues,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_GENERIC_KEY_NAMES, keyNames );
		jobParams.put(MdekKeys.SYS_GENERIC_KEY_VALUES, keyValues );
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeSysGenericKeys", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSysAdditionalFields(String plugId, Long[] fieldIds, String language,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_ADDITIONAL_FIELD_IDS, fieldIds);
		jobParams.put(MdekKeys.LANGUAGE, language);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSysAdditionalFields", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("exportObjectBranch", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportObjects(String plugId, String exportCriterion,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.EXPORT_CRITERION_VALUE, exportCriterion);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("exportObjects", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			AddressArea addressArea,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ADDRESS_AREA, addressArea);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("exportAddressBranch", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getExportInfo(String plugId,
			boolean includeExportData,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_INFO_INCLUDE_DATA, includeExportData);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getExportInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument importEntities(String plugId, byte[] importData,
			String targetObjectUuid, String targetAddressUuid,
			boolean publishImmediately,
			boolean doSeparateImport,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_DATA, importData);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, targetObjectUuid);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, targetAddressUuid);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, publishImmediately);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, doSeparateImport);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("importEntities", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);		
	}

	public IngridDocument getImportInfo(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getImportInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getURLInfo(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getURLInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument setURLInfo(String plugId, IngridDocument urlInfo, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.URL_RESULT, urlInfo.get(MdekKeys.URL_RESULT));
		jobParams.put(MdekKeys.JOBINFO_START_TIME, urlInfo.get(MdekKeys.JOBINFO_START_TIME));
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("setURLInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument updateURLInfo(String plugId, List<IngridDocument> urlList, String targetUrl, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_URL_LIST, urlList);
		jobParams.put(MdekKeys.REQUESTINFO_URL_TARGET, targetUrl);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("updateURLInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument replaceURLs(String plugId, List<IngridDocument> urlList, String targetUrl, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_URL_LIST, urlList);
		jobParams.put(MdekKeys.REQUESTINFO_URL_TARGET, targetUrl);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("replaceURLs", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument analyze(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("analyzeDBConsistency", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument replaceAddress(String plugId, String oldUuid, String newUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, oldUuid);
		jobParams.put(MdekKeys.TO_UUID, newUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("replaceAddress", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectsOfAuskunftAddress(String plugId,
			String auskunftAddressUuid, Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, auskunftAddressUuid);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjectsOfAuskunftAddress", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectsOfResponsibleUser(String plugId,
			String responsibleUserUuid, Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, responsibleUserUuid);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjectsOfResponsibleUser", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressesOfResponsibleUser(String plugId,
			String responsibleUserUuid, Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, responsibleUserUuid);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getAddressesOfResponsibleUser", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}


	public IngridDocument getCsvData(String plugId, CsvRequestType csvType,
			String uuid, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_CSV_REQUEST_TYPE, csvType);
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getCsvData", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}
}
