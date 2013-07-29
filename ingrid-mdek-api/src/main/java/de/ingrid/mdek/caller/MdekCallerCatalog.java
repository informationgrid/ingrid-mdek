package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.job.IJob.JobType;
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
	public static String MDEK_IDC_CATALOG_JOB_ID = "de.ingrid.mdek.job.MdekIdcCatalogJob";

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

	public IngridDocument getSysLists(String plugId, Integer[] listIds, String languageShortcut,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SYS_LIST_IDS, listIds);
		jobParams.put(MdekKeys.LANGUAGE_SHORTCUT, languageShortcut);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSysLists", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	/**
	 * Return the names of the syslist(s).
	 * Note: Not yet implemented since changes in database has to be made!
	 */
	/*public IngridDocument getSysListNames(String plugId, String language, String userId) {
	    
        jobParams.put(MdekKeys.LANGUAGE_SHORTCUT, language);
        jobParams.put(MdekKeys.USER_ID, userId);
        List jobMethods = setUpJobMethod("getSysListNames", jobParams);
        return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
        
	    return null;
	}*/
	
	public IngridDocument storeSysList(String plugId,
			int listId, boolean maintainable, Integer defaultEntryIndex,
			Integer[] entryIds, String[] entryNames_de, String[] entryNames_en, 
			String[] data, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.LST_ID, listId);
		jobParams.put(MdekKeys.LST_MAINTAINABLE, maintainable);
		jobParams.put(MdekKeys.LST_DEFAULT_ENTRY_INDEX, defaultEntryIndex);
		jobParams.put(MdekKeys.LST_ENTRY_IDS, entryIds);
		jobParams.put(MdekKeys.LST_ENTRY_NAMES_DE, entryNames_de);
		jobParams.put(MdekKeys.LST_ENTRY_NAMES_EN, entryNames_en);
		jobParams.put(MdekKeys.LST_ENTRY_DATA, data);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeSysList", jobParams);
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

	public IngridDocument exportObjectBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			boolean includeWorkingCopies,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES, includeWorkingCopies);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("exportObjectBranch", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportObjects(String plugId,
			String exportCriterion,
			boolean includeWorkingCopies,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.EXPORT_CRITERION_VALUE, exportCriterion);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES, includeWorkingCopies);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("exportObjects", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument exportAddressBranch(String plugId, String rootUuid,
			boolean exportOnlyRoot,
			AddressArea addressArea,
			boolean includeWorkingCopies,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, rootUuid);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT, exportOnlyRoot);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_ADDRESS_AREA, addressArea);
		jobParams.put(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES, includeWorkingCopies);
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

	public IngridDocument importEntities(String plugId, List<byte[]> importData,
			String targetObjectUuid, String targetAddressUuid,
			boolean publishImmediately,
			boolean doSeparateImport,
			boolean copyNodeIfPresent,
			String frontendProtocol,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_DATA, importData);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, targetObjectUuid);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, targetAddressUuid);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, publishImmediately);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, doSeparateImport);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, copyNodeIfPresent);
		jobParams.put(MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, frontendProtocol);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("importEntities", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
		
	}

	public IngridDocument getJobInfo(String plugId, JobType jobType, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_JOB_TYPE, jobType);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getJobInfo", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument setURLInfo(String plugId, IngridDocument urlInfo, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.URL_RESULT, urlInfo.get(MdekKeys.URL_RESULT));
		jobParams.put(MdekKeys.CAP_RESULT, urlInfo.get(MdekKeys.CAP_RESULT));
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

	public IngridDocument replaceURLs(String plugId, List<IngridDocument> urlList, String targetUrl, String type, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_URL_LIST, urlList);
		jobParams.put(MdekKeys.REQUESTINFO_URL_TARGET, targetUrl);
		jobParams.put(MdekKeys.LINKAGE_URL_TYPE, type);
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

	public IngridDocument getObjectsOfAddressByType(String plugId,
			String addressUuid, Integer referenceTypeId, Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addressUuid);
    jobParams.put(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY, referenceTypeId);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjectsOfAddressByType", jobParams);
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

	public IngridDocument getFreeListEntries(String plugId, MdekSysList sysLst,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.LST_ID, sysLst.getDbValue());
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getFreeListEntries", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument replaceFreeEntryWithSyslistEntry(String plugId, String freeEntry,
			MdekSysList sysLst, int sysLstEntryId, String sysLstEntryName,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.LST_FREE_ENTRY_NAMES, new String[]{freeEntry});
		jobParams.put(MdekKeys.LST_ID, sysLst.getDbValue());
		jobParams.put(MdekKeys.LST_ENTRY_IDS, new Integer[]{sysLstEntryId});
		jobParams.put(MdekKeys.LST_ENTRY_NAMES, new String[]{sysLstEntryName});
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("replaceFreeEntryWithSyslistEntry", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument rebuildSyslistData(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("rebuildSyslistData", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSearchTerms(String plugId, SearchtermType[] termTypes, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY, termTypes);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSearchTerms", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument updateSearchTerms(String plugId,
			List<IngridDocument> oldTerms, List<IngridDocument> newTerms,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.SUBJECT_TERMS_OLD, oldTerms);
		jobParams.put(MdekKeys.SUBJECT_TERMS_NEW, newTerms);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("updateSearchTerms", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument getSpatialReferences(String plugId, SpatialReferenceType[] spatialRefTypes, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY, spatialRefTypes);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSpatialReferences", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

	public IngridDocument updateSpatialReferences(String plugId,
			List<IngridDocument> oldSpatialRefs, List<IngridDocument> newSpatialRefs,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.LOCATIONS_OLD, oldSpatialRefs);
		jobParams.put(MdekKeys.LOCATIONS_NEW, newSpatialRefs);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("updateSpatialReferences", jobParams);
		return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
	}

    
	@Override
    public IngridDocument storeSysLists(String plugId, List<IngridDocument> syslistDoc, Long timestamp, String userId) {
	    IngridDocument jobParams = new IngridDocument();
        jobParams.put(MdekKeys.LST_SYSLISTS, syslistDoc);
        jobParams.put(MdekKeys.LST_LAST_MODIFIED, String.valueOf(timestamp));
        jobParams.put(MdekKeys.USER_ID, userId);
        List jobMethods = setUpJobMethod("storeSysLists", jobParams);
        return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);    
    }

    
	@Override
    public IngridDocument getLastModifiedTimestampOfSyslists(String plugId, String userId) {
	    IngridDocument jobParams = new IngridDocument();
        jobParams.put(MdekKeys.USER_ID, userId);
        List jobMethods = setUpJobMethod("getLastModifiedTimestampOfSyslists", jobParams);
        return callJob(plugId, MDEK_IDC_CATALOG_JOB_ID, jobMethods);
    }

}
