package de.ingrid.mdek.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.services.catalog.MdekAddressService;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekExportService;
import de.ingrid.mdek.services.catalog.MdekObjectService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.xml.exporter.XMLExporter;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Catalog functionality concerning access, syslists etc. 
 */
public class MdekIdcCatalogJob extends MdekIdcJob {

	private MdekCatalogService catalogService;
	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekPermissionHandler permissionHandler;

	private IObjectNodeDao daoObjectNode;

	public MdekIdcCatalogJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcCatalogJob.class), daoFactory);
		
		catalogService = MdekCatalogService.getInstance(daoFactory);
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
	}

	public IngridDocument getCatalog(IngridDocument params) {
		try {
			genericDao.beginTransaction();

			IngridDocument result = fetchCatalog();

			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
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
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
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

			IngridDocument result = catalogService.getSysLists(lstIds, language);
			
			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSysGuis(IngridDocument params) {
		try {
			String[] guiIds = (String[]) params.get(MdekKeys.SYS_GUI_IDS);

			genericDao.beginTransaction();
			IngridDocument result = getSysGuis(guiIds);
			genericDao.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
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
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
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

			IngridDocument result = catalogService.getSysAdditionalFields(fieldIds, language);

			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
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

			// initialize export info in database
			catalogService.startExportInfoDB(IdcEntityType.OBJECT, 0, userId);

			MdekExportService exportCallbacks = MdekExportService.getInstance(
					catalogService, objectService, addressService);
			XMLExporter exporter = new XMLExporter(exportCallbacks);
			
			byte[] expData = exporter.exportObjectBranch(rootUuid, exportOnlyRoot, userId);

			// finish export info and fetch it
			catalogService.endExportInfoDB(userId);
			HashMap exportInfo = catalogService.getExportInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);
			result.put(MdekKeys.EXPORT_RESULT, expData);

			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
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
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.EXPORT, 0, 0, false));

			String exportCriterion = (String) docIn.get(MdekKeys.EXPORT_CRITERION_VALUE);

			genericDao.beginTransaction();

			// find objects to export
			List<String> expUuids = daoObjectNode.getExportObjectsUuids(exportCriterion);
			int numToExport = expUuids.size();
			
			HashMap exportInfo = new HashMap(); 
			if (numToExport > 0) {
				// initialize export info in database
				catalogService.startExportInfoDB(IdcEntityType.OBJECT, numToExport, userId);

				// TODO implement exportObjects

				// finish and fetch export info in database
				catalogService.endExportInfoDB(userId);
				exportInfo = catalogService.getExportInfoDB(userId);
			}

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
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

			genericDao.beginTransaction();

			// initialize export info in database
			catalogService.startExportInfoDB(IdcEntityType.ADDRESS, 0, userId);

// TEST
			// test logging of current state
			catalogService.updateExportInfoDB(IdcEntityType.ADDRESS, 1, 1, userId);
// TEST END

			// TODO implement exportObjectBranch

			// finish and fetch export info in database
			catalogService.endExportInfoDB(userId);
			HashMap exportInfo = catalogService.getExportInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getExportInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();

			HashMap exportInfo = catalogService.getExportInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(exportInfo);

			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument importEntities(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.IMPORT, 0, 0, false));

			Byte[] importData = (Byte[]) docIn.get(MdekKeys.REQUESTINFO_IMPORT_DATA);
			String objParent = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID);
			String addrParent = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID);
			Boolean publishImmediately = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY);

			genericDao.beginTransaction();

			// initialize import info in database
			catalogService.startImportInfoDB(userId);

// TEST
			// test logging of current state
			catalogService.updateImportInfoDB(IdcEntityType.ADDRESS, 1, 10, userId);
			catalogService.updateImportInfoDBMessages("Address 1 out of 10 written !", userId);

			// test cancel of job (called by client)
//			cancelRunningJob(docIn);
			// THROWS EXCEPTION IF CANCELED !
			catalogService.updateImportInfoDB(IdcEntityType.OBJECT, 2, 10, userId);
			catalogService.updateImportInfoDBMessages("Object 2 out of 10 written !", userId);
// TEST END

			// TODO implement importEntities

			// finish and fetch import info in database
			catalogService.endImportInfoDB(userId);
			HashMap importInfo = catalogService.getImportInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(importInfo);
			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument getImportInfo(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		try {
			genericDao.beginTransaction();

			HashMap importInfo = catalogService.getImportInfoDB(userId);

			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(importInfo);

			return result;

		} catch (RuntimeException e) {
			genericDao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}	
}
