package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekExportService;
import de.ingrid.mdek.services.catalog.MdekImportService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
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

	private MdekPermissionHandler permissionHandler;

	private IObjectNodeDao daoObjectNode;

	public MdekIdcCatalogJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcCatalogJob.class), daoFactory);
		
		catalogService = MdekCatalogService.getInstance(daoFactory);
		exportService = MdekExportService.getInstance(daoFactory, permissionService);
		importService = MdekImportService.getInstance(daoFactory, permissionService);

		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
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

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = catalogService.getSysLists(lstIds, language);
			
			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
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
			docToBeanMapper.mapSysGuis(sysGuis, sysGuiList, true);

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
			HashMap exportInfo = exportService.getExportJobInfoDB(userId, false);

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
			HashMap exportInfo = exportService.getExportJobInfoDB(userId, false);

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
				exportInfo = exportService.getExportJobInfoDB(userId, false);
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
			HashMap exportInfo;
			HashMap runningJobInfo = jobHandler.getRunningJobInfo(JobType.EXPORT, userId);
			if (runningJobInfo.isEmpty()) {
				// no EXPORT job running, we extract import info from database
				exportInfo = exportService.getExportJobInfoDB(userId, includeData);
			} else {
				// job running, we extract export info from running job info (in memory)
				exportInfo =
					jobHandler.getJobInfoDetailsFromRunningJobInfo(runningJobInfo, false);
			}

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
			HashMap importInfo = importService.getImportJobInfoDB(userId);

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

	public IngridDocument getImportInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			// extract import info
			HashMap importInfo;
			HashMap runningJobInfo = jobHandler.getRunningJobInfo(JobType.IMPORT, userId);
			if (runningJobInfo.isEmpty()) {
				// no IMPORT job running, we extract import info from database
				importInfo = importService.getImportJobInfoDB(userId);
			} else {
				// job running, we extract import info from running job info (in memory)
				importInfo = 
					jobHandler.getJobInfoDetailsFromRunningJobInfo(runningJobInfo, false);
			}

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(importInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getURLInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			// extract url info
			HashMap urlInfo;
			HashMap runningJobInfo = jobHandler.getRunningJobInfo(JobType.URL, userId);
			// always extract URL job info from the database
			urlInfo = importService.getURLJobInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(urlInfo);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument setURLInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();

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
}
