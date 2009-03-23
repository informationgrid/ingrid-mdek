package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.services.catalog.MdekAddressService;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekDBConsistencyService;
import de.ingrid.mdek.services.catalog.MdekExportService;
import de.ingrid.mdek.services.catalog.MdekImportService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.xml.exporter.XMLExporter;
import de.ingrid.mdek.xml.importer.XMLImporter;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Catalog functionality concerning access, syslists etc. 
 */
public class MdekIdcCatalogJob extends MdekIdcJob {

	private static final Logger LOG = Logger.getLogger(MdekIdcCatalogJob.class);

	private MdekCatalogService catalogService;
	private MdekExportService exportService;
	private MdekImportService importService;
	private MdekDBConsistencyService dbConsistencyService;
	private MdekAddressService addressService;

	private MdekPermissionHandler permissionHandler;

	private IObjectNodeDao daoObjectNode;
	private IAddressNodeDao daoAddressNode;
	private IIdcUserDao daoIdcUser;
	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;
	private ISysListDao daoSysList;
	private IHQLDao daoHQL;
	private IT08AttrTypeDao daoT08AttrType;
	private ISearchtermValueDao daoSearchtermValue;
	private ISpatialRefValueDao daoSpatialRefValue;

	public MdekIdcCatalogJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcCatalogJob.class), daoFactory);
		
		catalogService = MdekCatalogService.getInstance(daoFactory);
		exportService = MdekExportService.getInstance(daoFactory, permissionService);
		importService = MdekImportService.getInstance(daoFactory, permissionService);
		dbConsistencyService = MdekDBConsistencyService.getInstance(daoFactory);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);
		
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoAddressNode = daoFactory.getAddressNodeDao();
		daoIdcUser = daoFactory.getIdcUserDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoSysList = daoFactory.getSysListDao();
		daoHQL = daoFactory.getHQLDao();
		daoT08AttrType = daoFactory.getT08AttrTypeDao();
		daoSearchtermValue = daoFactory.getSearchtermValueDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
	}

	public IngridDocument getCatalog(IngridDocument params) {
		try {
			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = fetchCatalog();

			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	private IngridDocument fetchCatalog() {
		// fetch catalog via handler
		T03Catalogue catalog = catalogService.getCatalog();

		IngridDocument result = new IngridDocument();
		beanToDocMapper.mapT03Catalog(catalog, result);
		
		return result;
	}

	public IngridDocument storeCatalog(IngridDocument cDocIn) {
		String userId = getCurrentUserUuid(cDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			genericDao.beginTransaction();

			String currentTime = MdekUtils.dateToTimestamp(new Date());
			Boolean refetchAfterStore = (Boolean) cDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			cDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, cDocIn, MappingQuantity.INITIAL_ENTITY);

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// exception if catalog not existing
			T03Catalogue catalog = catalogService.getCatalog();
			
			// transfer new data AND MAKE PERSISTENT, so oncoming checks have newest data !
			docToBeanMapper.mapT03Catalog(cDocIn, catalog);
			genericDao.makePersistent(catalog);

			// clear all caches !
			catalogService.clearCaches();

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			genericDao.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.UUID, catalog.getCatUuid());

			if (refetchAfterStore) {
				genericDao.beginTransaction();
				result = fetchCatalog();
				genericDao.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}
	
	public IngridDocument getSysLists(IngridDocument params) {
		try {
			Integer[] lstIds = (Integer[]) params.get(MdekKeys.SYS_LIST_IDS);
			String language = params.getString(MdekKeys.LANGUAGE);

			IngridDocument result;

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			if (lstIds != null) {
				result = catalogService.getSysLists(lstIds, language);
				
			} else {
				result = new IngridDocument();
				result.put(MdekKeys.SYS_LIST_IDS, catalogService.getSysListIds());
			}
			
			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeSysList(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Integer lstId = (Integer) docIn.get(MdekKeys.LST_ID);
	
			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// get according syslist and update
			List<SysList> sysListEntries = daoSysList.getSysList(lstId, null);
			docToBeanMapper.updateSysList(docIn, sysListEntries);

			// clear all caches !
			catalogService.clearCaches();

			genericDao.commitTransaction();

			// return something (not null !)
			IngridDocument result = new IngridDocument();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getSysGuis(IngridDocument params) {
		try {
			String[] guiIds = (String[]) params.get(MdekKeys.SYS_GUI_IDS);

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = getSysGuis(guiIds);

			genericDao.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}	

	private IngridDocument getSysGuis(String[] guiIds) {
		IngridDocument result = new IngridDocument();
		
		List<SysGui> sysGuiList = catalogService.getSysGuis(guiIds);
		beanToDocMapper.mapSysGuis(sysGuiList, result);
		
		return result;
	}

	public IngridDocument storeSysGuis(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) docIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			List<IngridDocument> sysGuis = (List<IngridDocument>) docIn.get(MdekKeys.SYS_GUI_LIST);
			
			String[] guiIds = new String[sysGuis.size()];
			for (int i=0; i < sysGuis.size(); i++) {
				guiIds[i] = sysGuis.get(i).getString(MdekKeys.SYS_GUI_ID);
			}

			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// get according sysGuis
			// transfer new data AND MAKE PERSISTENT !
			List<SysGui> sysGuiList = catalogService.getSysGuis(guiIds);
			docToBeanMapper.updateSysGuis(sysGuis, sysGuiList);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			genericDao.commitTransaction();

			// return something (not null !)
			IngridDocument result = new IngridDocument();

			if (refetchAfterStore) {
				genericDao.beginTransaction();
				result = getSysGuis(guiIds);
				genericDao.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getSysGenericKeys(IngridDocument params) {
		try {
			String[] keyNames = (String[]) params.get(MdekKeys.SYS_GENERIC_KEY_NAMES);

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = getSysGenericKeys(keyNames);
			
			genericDao.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}	

	private IngridDocument getSysGenericKeys(String[] keyNames) {
		IngridDocument result = new IngridDocument();
		
		List<SysGenericKey> sysGenericKeyList = catalogService.getSysGenericKeys(keyNames);
		beanToDocMapper.mapSysGenericKeys(sysGenericKeyList, result);
		
		return result;
	}

	public IngridDocument storeSysGenericKeys(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			String[] keyNames = (String[]) docIn.get(MdekKeys.SYS_GENERIC_KEY_NAMES);

			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// get according generic keys
			// transfer new data AND MAKE PERSISTENT !
			List<SysGenericKey> sysGenericKeys = catalogService.getSysGenericKeys(keyNames);
			docToBeanMapper.updateSysGenericKeys(docIn, sysGenericKeys);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			genericDao.commitTransaction();

			// Refetch to return updated values !
			genericDao.beginTransaction();
			IngridDocument result = getSysGenericKeys(keyNames);
			genericDao.commitTransaction();
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getSysAdditionalFields(IngridDocument params) {
		try {
			Long[] fieldIds = (Long[]) params.get(MdekKeys.SYS_ADDITIONAL_FIELD_IDS);
			String language = params.getString(MdekKeys.LANGUAGE);

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = catalogService.getSysAdditionalFields(fieldIds, language);

			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeAllSysAdditionalFields(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			List<IngridDocument> fieldDocs = (List<IngridDocument>) docIn.get(MdekKeys.SYS_ADDITIONAL_FIELD_LIST);
			
			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// get all current additional fields
			List<T08AttrType> additFields = daoT08AttrType.getT08AttrTypes(null, null);
			// add/update/delete additional fields according to docs !
			List<Long> ids = docToBeanMapper.updateT08AttrTypes(fieldDocs, additFields);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			genericDao.commitTransaction();

			// return ids of stored additional fields
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.SYS_ADDITIONAL_FIELD_IDS, ids.toArray(new Long[ids.size()]));
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument exportObjectBranch(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.EXPORT, 0, 0, false));

			String rootUuid = (String) docIn.get(MdekKeys.UUID);
			Boolean exportOnlyRoot = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			List<String> uuidsToExport = new ArrayList<String>();
			if (rootUuid != null) {
				uuidsToExport.add(rootUuid);
			} else {
				uuidsToExport = exportService.getTopObjectUuids();
			}

			// initialize export job info
			// NOTICE: totalNumToExport should be called and set in exporter !
//			int totalNumToExport = exportService.getTotalNumObjectsToExport(uuidsToExport, !exportOnlyRoot, userId);				
			exportService.startExportJobInfo(IdcEntityType.OBJECT, 0, userId);

			// export
			byte[] expData = new XMLExporter(exportService).exportObjects(uuidsToExport, !exportOnlyRoot, userId);

			// finish export job info and fetch it
			exportService.endExportJobInfo(expData, IdcEntityType.OBJECT, userId);
			HashMap exportInfo = getJobInfo(JobType.EXPORT, userId, false, false);

			// just to be sure ExportJobInfo is up to date !
			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
			
			// LOG relevant EXCEPTION IN DATABASE Job Info !
			if (errorHandler.shouldLog(handledExc)) {
				logExportException(handledExc, IdcEntityType.OBJECT, 0, userId);
			}

		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument exportAddressBranch(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.EXPORT, 0, 0, false));

			String rootUuid = (String) docIn.get(MdekKeys.UUID);
			Boolean exportOnlyRoot = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT);
			AddressArea addressArea = (AddressArea) docIn.get(MdekKeys.REQUESTINFO_EXPORT_ADDRESS_AREA);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			List<String> uuidsToExport = new ArrayList<String>();
			if (rootUuid != null) {
				uuidsToExport.add(rootUuid);
			} else {
				uuidsToExport = exportService.getTopAddressUuids(addressArea);
			}

			// initialize export job info
			// NOTICE: totalNumToExport should be called and set in exporter !
//			int totalNumToExport = exportService.getTotalNumAddressesToExport(uuidsToExport, !exportOnlyRoot, userId);
			exportService.startExportJobInfo(IdcEntityType.ADDRESS, 0, userId);

			// export
			byte[] expData = new XMLExporter(exportService).exportAddresses(uuidsToExport, !exportOnlyRoot, userId);

			// finish export job info and fetch it
			exportService.endExportJobInfo(expData, IdcEntityType.ADDRESS, userId);
			HashMap exportInfo = getJobInfo(JobType.EXPORT, userId, false, false);

			// just to be sure ExportJobInfo is up to date !
			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);

			// LOG relevant EXCEPTION IN DATABASE Job Info !
			if (errorHandler.shouldLog(handledExc)) {
				logExportException(handledExc, IdcEntityType.ADDRESS, 0, userId);
			}

		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument exportObjects(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		int numToExport = 0;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.EXPORT, 0, 0, false));

			String exportCriterion = (String) docIn.get(MdekKeys.EXPORT_CRITERION_VALUE);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// find objects to export
			List<String> expUuids = daoObjectNode.getObjectUuidsForExport(exportCriterion);
			numToExport = expUuids.size();
			
			HashMap exportInfo = new HashMap();
			byte[] expData = new byte[0];;
			if (numToExport > 0) {
				// initialize export info in database
				exportService.startExportJobInfo(IdcEntityType.OBJECT, numToExport, userId);

				// export
				expData = new XMLExporter(exportService).exportObjects(expUuids, false, userId);

				// finish export job info and fetch it
				exportService.endExportJobInfo(expData, IdcEntityType.OBJECT, userId);
				exportInfo = getJobInfo(JobType.EXPORT, userId, false, false);
			}

			// just to be sure ExportJobInfo is up to date !
			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);

			// LOG relevant EXCEPTION IN DATABASE Job Info !
			if (errorHandler.shouldLog(handledExc)) {
				logExportException(handledExc, IdcEntityType.OBJECT, numToExport, userId);
			}

		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Logs given Exception (opens transaction !) in database for access via JobInfo !
	 * Pass additional describing info. */
	private void logExportException(RuntimeException e, 
			IdcEntityType whichEntityType, int numToExport,
			String userId) {
		try {
			genericDao.beginTransaction();
			exportService.startExportJobInfo(IdcEntityType.OBJECT, numToExport, userId);
			exportService.updateExportJobInfoException(e, userId);
			genericDao.commitTransaction();				
		} catch (RuntimeException e2) {
			genericDao.rollbackTransaction();
			LOG.warn("Problems logging Export Job Exception in database (JobInfo) !", e2);
		}
	}

	public IngridDocument getExportInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			Boolean includeData = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_INFO_INCLUDE_DATA);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// extract export info
			HashMap exportInfo = getJobInfo(JobType.EXPORT, userId, true, includeData);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument importEntities(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.IMPORT, 0, 0, false));

			byte[] importData = (byte[]) docIn.get(MdekKeys.REQUESTINFO_IMPORT_DATA);
			String defaultObjectParentUuid = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID);
			String defaultAddrParentUuid = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID);
			Boolean publishImmediately = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY);
			Boolean doSeparateImport = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT);

			genericDao.beginTransaction();

			// CHECKS BEFORE START OF IMPORT

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);
			importService.checkDefaultParents(defaultObjectParentUuid, defaultAddrParentUuid,
					publishImmediately, doSeparateImport, userId);

			// initialize import info in database
			importService.startImportJobInfo(userId);

			// import
			new XMLImporter(importService).importEntities(importData, userId);
			
			// post process object relations (Querverweise) after importing of all entities
			importService.postProcessRelationsOfImport(userId);

			// finish and fetch import info in database
			importService.endImportJobInfo(userId);
			HashMap importInfo = getJobInfo(JobType.IMPORT, userId, false, false);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(importInfo);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);

			// LOG relevant EXCEPTION IN DATABASE Job Info !
			if (errorHandler.shouldLog(handledExc)) {
				logImportException(handledExc, userId);
			}

		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Logs given Exception (opens transaction !) in database for access via JobInfo ! */
	private void logImportException(RuntimeException e, String userId) {
		try {
			genericDao.beginTransaction();
			importService.startImportJobInfo(userId);
			importService.updateImportJobInfoException(e, userId);
			genericDao.commitTransaction();				
		} catch (RuntimeException e2) {
			genericDao.rollbackTransaction();
			LOG.warn("Problems logging Import Job Exception in database (JobInfo) !", e2);
		}
	}

	public IngridDocument getJobInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			JobType jobType = (JobType) docIn.get(MdekKeys.REQUESTINFO_JOB_TYPE);
			boolean checkRunningJob = true;
			if (jobType == JobType.URL) {
				checkRunningJob = false;
			}

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			// extract job info. Never include additional data when calling this standard job info method !
			HashMap jobInfo = getJobInfo(jobType, userId, checkRunningJob, false);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(jobInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	/** Extract info of job of given type. 
	 * @param jobType type of job to fetch info about
	 * @param userUuid calling user, job infos are stored per user !
	 * @param checkRunningJob true=fetch job info from running job if there is one (of same type)
	 * @param includeAdditionalData true=include additional data in job info, e.g. zipped export file !
	 * @return Map containing job info
	 */
	private HashMap getJobInfo(JobType jobType, String userUuid,
			boolean checkRunningJob, boolean includeAdditionalData) {
		HashMap jobInfo = null;

		// get job info from running job if requested
		if (checkRunningJob) {
			HashMap runningJobInfo = jobHandler.getRunningJobInfo(jobType, userUuid);
			if (!runningJobInfo.isEmpty()) {
				// job running, we extract job info from running job (in memory).
				// we never extract messages from running job !
				jobInfo = jobHandler.getJobInfoDetailsFromRunningJobInfo(runningJobInfo, false);
			}
		}

		// extract info from database if no info yet
		if (jobInfo == null) {
			if (jobType == JobType.EXPORT) {
				// specials when EXPORT info. We may include exported file !
				jobInfo = exportService.getExportJobInfoDB(userUuid, includeAdditionalData);				
			} else {
				// default mapping
				SysJobInfo jobInfoDB = jobHandler.getJobInfoDB(jobType, userUuid);
				jobInfo = jobHandler.mapJobInfoDB(jobInfoDB);				
			}
		}
		
		return jobInfo;
	}

	public IngridDocument setURLInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			List<Map<String, Object>> urlList = docIn.getArrayList(MdekKeys.URL_RESULT);
			String jobStartTime = docIn.getString(MdekKeys.JOBINFO_START_TIME);
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put(MdekKeys.URL_RESULT, urlList);
			jobHandler.startJobInfoDB(
					JobType.URL,
					jobStartTime,
					data, userId);

			jobHandler.endJobInfoDB(JobType.URL, userId);
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument updateURLInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		// The IngridDocument objects in sourceUrls consist of a UUID and LINKAGE_URL
		List<IngridDocument> sourceUrls = docIn.getArrayList(MdekKeys.REQUESTINFO_URL_LIST);
		String targetUrl = docIn.getString(MdekKeys.REQUESTINFO_URL_TARGET);
		try {
			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// Retrieve the job info from the db
			SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.URL, userId);
			HashMap<String, Object> jobDetails = jobHandler.mapJobInfoDB(jobInfo);
			List<Map<String, Object>> urlList = (List<Map<String,Object>>) jobDetails.get(MdekKeys.URL_RESULT);

			// Iterate over all sourceUrl objects and locate them in the job detail
			for (IngridDocument sourceUrl : sourceUrls) {
				String objUuid = sourceUrl.getString(MdekKeys.URL_RESULT_OBJECT_UUID);
				String url = sourceUrl.getString(MdekKeys.URL_RESULT_URL);
				String state = sourceUrl.getString(MdekKeys.URL_RESULT_STATE);

				// If a reference with UUID objUuid and URL url was found, replace the url with the targetUrl
				// Also replace the URL_STATE
				for (Map<String, Object> urlMap : urlList) {
					if (urlMap.get(MdekKeys.URL_RESULT_OBJECT_UUID).equals(objUuid)
							&& urlMap.get(MdekKeys.URL_RESULT_URL).equals(url)) {
						urlMap.put(MdekKeys.URL_RESULT_URL, targetUrl);
						urlMap.put(MdekKeys.URL_RESULT_STATE, state);
					}
				}
			}

			// Store the changes in the db
			jobHandler.updateJobInfoDB(JobType.URL, jobDetails, userId);
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument replaceURLs(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		List<IngridDocument> urlList = docIn.getArrayList(MdekKeys.REQUESTINFO_URL_LIST);
		String targetUrl = docIn.getString(MdekKeys.REQUESTINFO_URL_TARGET);

		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			daoObjectNode.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			IdcEntityVersion whichEntityVersion = IdcEntityVersion.PUBLISHED_VERSION;

			for (IngridDocument urlRefDoc : urlList) {
				String uuid = (String) urlRefDoc.get(MdekKeys.URL_RESULT_OBJECT_UUID);
				String srcUrl = (String) urlRefDoc.get(MdekKeys.URL_RESULT_URL);

				// load node
				ObjectNode oNode = daoObjectNode.loadByUuid(uuid, whichEntityVersion);
				if (oNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}

				T01Object obj = oNode.getT01ObjectPublished();
				Set<T017UrlRef> urlRefs = (Set<T017UrlRef>) obj.getT017UrlRefs();
				for (T017UrlRef urlRef : urlRefs) {
					if (urlRef.getUrlLink().equals(srcUrl)) {
						urlRef.setUrlLink(targetUrl);
					}
				}
			}

			daoObjectNode.commitTransaction();

			return new IngridDocument();

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;

		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * Analyze several tables from the database for inconsistency checks
	 * @param docIn, which must contain the userUuid at least
	 * @return a map with the results of the checks
	 */
	public IngridDocument analyzeDBConsistency(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.ANALYZE, 0, 0, false));
			
			genericDao.beginTransaction();
			
			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);
			
			// initialize job info in database
			dbConsistencyService.startDBConsistencyJobInfo(userId);
			
			// analyze
			dbConsistencyService.analyze(userId);
			
			// finish and fetch job info in database
			dbConsistencyService.endDBConsistencyJobInfo(userId);

			Map dbConsistencyInfo = dbConsistencyService.getDBConsistencyJobInfoDB(userId);
			
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(dbConsistencyInfo);
			return result;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);

		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);			
			}
		}
	}

	public IngridDocument replaceAddress(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.REPLACE, 0, 0, false));

			String oldAddrUuid = (String) params.get(MdekKeys.FROM_UUID);
			String newAddrUuid = (String) params.get(MdekKeys.TO_UUID);
			if (MdekUtils.isEqual(oldAddrUuid, newAddrUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_EQUALS_TO_UUID));				
			}

			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userUuid);
			String catAdminUuid = userUuid;

			// check old address:
			// - has to exist
			// - user address not allowed
			// - children not allowed
			if (daoAddressNode.loadByUuid(oldAddrUuid, IdcEntityVersion.WORKING_VERSION) == null) {
				throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
			}
			if (daoIdcUser.getIdcUserByAddrUuid(oldAddrUuid) != null) {
				throw new MdekException(new MdekError(MdekErrorType.ADDRESS_IS_IDCUSER_ADDRESS));
			}
			if (daoAddressNode.getSubAddresses(oldAddrUuid, null, false).size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.NODE_HAS_SUBNODES));
			}

			// check new address:
			// - has to be published (will be set in published objects as auskunft)
			if (!addressService.hasPublishedVersion(
					daoAddressNode.loadByUuid(newAddrUuid, IdcEntityVersion.PUBLISHED_VERSION))) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_PUBLISHED));
			}

			// REPLACE ALL AUSKUNFTS ADDRESSES !
			int numAuskunftChanged =
				catalogService.updateAuskunftInObjects(oldAddrUuid, newAddrUuid, userUuid);

			// REPLACE ALL RESPONSIBLE USERS !
			int numResponsibleUsersChangedObjs =
				catalogService.updateResponsibleUserInObjects(oldAddrUuid, catAdminUuid);
			int numResponsibleUsersChangedAddrs =
				catalogService.updateResponsibleUserInAddresses(oldAddrUuid, catAdminUuid);

			// DELETE
			// NOTICE: current user IS CATADMIN ! so no QA workflow !
			addressService.deleteAddressFull(oldAddrUuid, true, catAdminUuid);

			genericDao.commitTransaction();

			IngridDocument resultDoc = new IngridDocument();
			// just for debugging
			resultDoc.putInt("numAuskunftChanged", numAuskunftChanged);
			resultDoc.putInt("numResponsibleUsersChangedObjs", numResponsibleUsersChangedObjs);
			resultDoc.putInt("numResponsibleUsersChangedAddrs", numResponsibleUsersChangedAddrs);
			return resultDoc;		

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	public IngridDocument getObjectsOfAuskunftAddress(IngridDocument params) {
		try {
			String uuid = (String) params.get(MdekKeys.UUID);
			Integer maxNum = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			List<T01Object> objs = 
				daoT02Address.getObjectReferencesByTypeId(uuid, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID, maxNum);

			List<IngridDocument> objDocs = 
				beanToDocMapper.mapT01Objects(objs, MappingQuantity.BASIC_ENTITY);

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, objDocs);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectsOfResponsibleUser(IngridDocument params) {
		try {
			String uuid = (String) params.get(MdekKeys.UUID);
			Integer maxNum = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			List<T01Object> objs =
				daoT01Object.getObjectsOfResponsibleUser(uuid, maxNum);

			List<IngridDocument> objDocs = 
				beanToDocMapper.mapT01Objects(objs, MappingQuantity.BASIC_ENTITY);

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, objDocs);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getAddressesOfResponsibleUser(IngridDocument params) {
		try {
			String uuid = (String) params.get(MdekKeys.UUID);
			Integer maxNum = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			List<T02Address> addrs =
				daoT02Address.getAddressesOfResponsibleUser(uuid, maxNum);

			List<IngridDocument> addrDocs = 
				beanToDocMapper.mapT02Addresses(addrs, MappingQuantity.BASIC_ENTITY);

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.ADR_ENTITIES, addrDocs);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getCsvData(IngridDocument params) {
		try {
			CsvRequestType csvType = (CsvRequestType) params.get(MdekKeys.REQUESTINFO_CSV_REQUEST_TYPE);
			String uuid = (String) params.get(MdekKeys.UUID);

			IngridDocument result = new IngridDocument();
			String hqlQuery = null;
			
			if (csvType == CsvRequestType.OBJECTS_OF_AUSKUNFT_ADDRESS) {
				hqlQuery = daoT02Address.getCsvHQLObjectReferencesByTypeId(uuid, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID);
				
			} else if (csvType == CsvRequestType.OBJECTS_OF_RESPONSIBLE_USER) {
				hqlQuery = daoT01Object.getCsvHQLAllObjectsOfResponsibleUser(uuid);
				
			} else if (csvType == CsvRequestType.ADDRESSES_OF_RESPONSIBLE_USER) {
				hqlQuery = daoT02Address.getCsvHQLAllAddressesOfResponsibleUser(uuid);
			}

			daoHQL.beginTransaction();
			genericDao.disableAutoFlush();

			if (hqlQuery != null) {
				result = daoHQL.queryHQLToCsv(hqlQuery, true);				
			}

			daoHQL.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getFreeListEntries(IngridDocument params) {
		try {
			Integer lstId = (Integer) params.get(MdekKeys.LST_ID);
			MdekSysList sysLst = EnumUtil.mapDatabaseToEnumConst(MdekSysList.class, lstId);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			List<String> freeEntries = daoSysList.getFreeListEntries(sysLst);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.LST_FREE_ENTRY_NAMES, freeEntries.toArray(new String[freeEntries.size()]));
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument replaceFreeEntryWithSyslistEntry(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			String freeEntryName = ((String[]) docIn.get(MdekKeys.LST_FREE_ENTRY_NAMES))[0];
			Integer lstId = (Integer) docIn.get(MdekKeys.LST_ID);
			MdekSysList sysLst = EnumUtil.mapDatabaseToEnumConst(MdekSysList.class, lstId);
			Integer syslstEntryId = ((Integer[]) docIn.get(MdekKeys.LST_ENTRY_IDS))[0];
			String syslstEntryName = ((String[]) docIn.get(MdekKeys.LST_ENTRY_NAMES_DE))[0];
	
			genericDao.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			int numReplaced = catalogService.replaceFreeEntryWithSyslistEntry(freeEntryName,
					sysLst, syslstEntryId, syslstEntryName);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numReplaced);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument rebuildSyslistData(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.REBUILD_SYSLISTS, 0, 0, false));

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// initialize job info in database
			catalogService.startRebuildSyslistJobInfo(userId);

			// clear all caches, read NEWEST DATA !
			catalogService.clearCaches();

			// first update content in entities
			catalogService.rebuildEntitiesSyslistData(userId);

			// COMMIT IN BETWEEN TO HAVE NEW SESSION WHEN UPDATING INDEX !?
			genericDao.flush();
			genericDao.commitTransaction();

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// then rebuild index
			catalogService.rebuildEntitiesIndex(userId);
			
			// finish and fetch job info in database
			catalogService.endRebuildSyslistJobInfo(userId);
			HashMap rebuildInfo = getJobInfo(JobType.REBUILD_SYSLISTS, userId, false, false);

			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(rebuildInfo);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getSearchTerms(IngridDocument params) {
		try {
			SearchtermType[] termTypes = (SearchtermType[]) params.get(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// get all searchterms of passed type(s)
			List<SearchtermValue> terms = daoSearchtermValue.getSearchtermValues(termTypes);

			// set up result, filter duplicates
			Set<String> filterSet = new HashSet<String>();
			List<SearchtermValue> resultTerms = new ArrayList<SearchtermValue>();

			for (SearchtermValue term : terms) {
				String termKey = (term.getType() +
					term.getSearchtermSnsId() +
					term.getTerm() +
					term.getEntryId()).toLowerCase();
				if (!filterSet.contains(termKey)) {
					filterSet.add(termKey);
					// add new Term
					resultTerms.add(term);
				}
			}
			IngridDocument result =
				beanToDocMapper.mapSearchtermValues(resultTerms, new IngridDocument());
			
			genericDao.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSpatialReferences(IngridDocument params) {
		try {
			SpatialReferenceType[] spatialRefTypes =
				(SpatialReferenceType[]) params.get(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// get all locations of passed type(s)
			List<SpatialRefValue> spatialRefValues = daoSpatialRefValue.getSpatialReferences(spatialRefTypes);

			// set up result, filter duplicates
			Set<String> filterSet = new HashSet<String>();
			List<SpatialRefValue> resultRefValues = new ArrayList<SpatialRefValue>();

			for (SpatialRefValue refValue : spatialRefValues) {
				String refKey = (refValue.getType() +
					refValue.getSpatialRefSnsId() +
					refValue.getNameKey() +
					refValue.getNameValue()).toLowerCase();
				if (!filterSet.contains(refKey)) {
					filterSet.add(refKey);
					// add new location
					resultRefValues.add(refValue);
				}
			}
			IngridDocument result =
				beanToDocMapper.mapSpatialRefValues(resultRefValues, new IngridDocument());
			
			genericDao.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}
}
