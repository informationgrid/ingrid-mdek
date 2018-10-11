/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.iplug.dsc.index.DscDocumentProducer;
import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.job.mapping.DataMapperFactory;
import de.ingrid.mdek.job.protocol.HashMapProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
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
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.xml.exporter.XMLExporter;
import de.ingrid.mdek.xml.importer.IImporter;
import de.ingrid.mdek.xml.importer.XMLImporter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.XMLUtils;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 * Encapsulates all Catalog functionality concerning access, syslists etc. 
 */
@Service
public class MdekIdcCatalogJob extends MdekIdcJob {

	private static final Logger LOG = LogManager.getLogger(MdekIdcCatalogJob.class);

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
	private ISearchtermValueDao daoSearchtermValue;
	private ISpatialRefValueDao daoSpatialRefValue;
	
	@Autowired
    private DataMapperFactory dataMapperFactory;

	public void setDataMapperFactory(DataMapperFactory dataMapperFactory) {
        this.dataMapperFactory = dataMapperFactory;
    }

    @Autowired
	public MdekIdcCatalogJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService,
            IndexManager indexManager) {
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
		daoSearchtermValue = daoFactory.getSearchtermValueDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
        this.indexManager = indexManager;
	}

	public IngridDocument getVersion(IngridDocument params) {
		try {
			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result = super.getVersion(params);

			// first check own conflicts ;) (between IGE and IGC schema)
			// throws Exception if conflicts occur.
			catalogService.checkIGCVersion();
			
			// add Version of IGC Schema to version info, needed in frontend for checking compatibility !
			String igcSchemaVersion = catalogService.getIGCVersion();
			result.put(MdekKeys.SYS_GENERIC_KEY_VALUES, new String[] {igcSchemaVersion});

			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getCatalog(IngridDocument params) {
		try {
			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			// before fetching catalog check whether version of IGC in database fits !
			// throws Exception if not !
			catalogService.checkIGCVersion();
			
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
			String language = params.getString(MdekKeys.LANGUAGE_SHORTCUT);

			IngridDocument result;

			genericDao.beginTransaction();
			daoObjectNode.disableAutoFlush();

			if (lstIds != null) {
				result = catalogService.getSysLists(lstIds, language);
				
			} else {
				result = new IngridDocument();
				result.put(MdekKeys.LST_SYSLISTS, catalogService.getSysListInfos());
			}
			
			genericDao.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}
	
	/**
	 * Return the names of the syslists.
	 * Note: Not yet implemented since database has to be modified!
	 */
	/*
	public IngridDocument getSysListNames(IngridDocument params) {
	    //String language = params.getString(MdekKeys.LANGUAGE_SHORTCUT);
	    //IngridDocument result;
	    //result = catalogService.getSysListNames(language);
	    //return result;
	    return null;
	}*/

	/**
	 * This function updates a syslist in the database manipulated in the IGE 
	 * frontend.
	 * @param docIn contains the syslist
	 * @return an empty IngridDocument on success
	 */
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

	/**
	 * Overwrite all syslists coming from a repository, which shall not be
	 * editor in the IGE frontend.
	 * @param docIn contains the syslists from the repository
	 * @return an empty IngridDocument on success
	 */
	public IngridDocument storeSysLists(IngridDocument docIn) {
        String userId = getCurrentUserUuid(docIn);
        boolean removeRunningJob = true;
        try {
            // first add basic running jobs info !
            addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

            genericDao.beginTransaction();

            // check permissions !
            permissionHandler.checkIsCatalogAdmin(userId);

            // get according syslist and update
            List<IngridDocument> codelists = (List<IngridDocument>) docIn.get(MdekKeys.LST_SYSLISTS);
            for (IngridDocument codelist : codelists) {
                // check if syslist already exists
                List<SysList> sysListEntries = daoSysList.getSysList(Integer.valueOf(codelist.getInt(MdekKeys.LST_ID)), null);
                
                // TODO: is syslist locked then ignore updating it!
                
                docToBeanMapper.updateSysListAllLang(codelist, sysListEntries);
            }

            // store last modified timestamp from codelists
            String[] keyNames = {"lastModifiedSyslist"};
            String[] keyValues = {(String)docIn.get(MdekKeys.LST_LAST_MODIFIED)};
            List<SysGenericKey> sysGenericKeys = catalogService.getSysGenericKeys(keyNames);
            IngridDocument docSyskeys = new IngridDocument();
            docSyskeys.put(MdekKeys.SYS_GENERIC_KEY_NAMES, keyNames);
            docSyskeys.put(MdekKeys.SYS_GENERIC_KEY_VALUES, keyValues);
            docToBeanMapper.updateSysGenericKeys(docSyskeys, sysGenericKeys);

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

	public IngridDocument exportObjectBranch(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.EXPORT, 0, 0, false));

			String rootUuid = (String) docIn.get(MdekKeys.UUID);
			Boolean exportOnlyRoot = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_ONLY_ROOT);
			Boolean includeWorkingCopies = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES);
			
			IdcEntityVersion whichObjectVersions = IdcEntityVersion.PUBLISHED_VERSION;
			if (includeWorkingCopies) {
				whichObjectVersions = IdcEntityVersion.ALL_VERSIONS;
			}

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
			byte[] expData = new XMLExporter(exportService).exportObjects(uuidsToExport, whichObjectVersions, !exportOnlyRoot, userId);

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
			Boolean includeWorkingCopies = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES);
			
			IdcEntityVersion whichAddrVersions = IdcEntityVersion.PUBLISHED_VERSION;
			if (includeWorkingCopies) {
				whichAddrVersions = IdcEntityVersion.ALL_VERSIONS;
			}

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
			byte[] expData = new XMLExporter(exportService).exportAddresses(uuidsToExport, whichAddrVersions, !exportOnlyRoot, userId);

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
			Boolean includeWorkingCopies = (Boolean) docIn.get(MdekKeys.REQUESTINFO_EXPORT_INCLUDE_WORKING_COPIES);
			
			IdcEntityVersion whichObjectVersions = IdcEntityVersion.PUBLISHED_VERSION;
			if (includeWorkingCopies) {
				whichObjectVersions = IdcEntityVersion.ALL_VERSIONS;
			}

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// initialize export info in database
			exportService.startExportJobInfo(IdcEntityType.OBJECT, numToExport, userId);

			// find objects to export
			List<String> expUuids = daoObjectNode.getObjectUuidsForExport(exportCriterion);
			numToExport = expUuids.size();
			
			HashMap exportInfo = new HashMap();
			byte[] expData = new byte[0];
			if (numToExport > 0) {
				// export
				expData = new XMLExporter(exportService).exportObjects(expUuids, whichObjectVersions, false, userId);
			}

			// finish export job info and fetch it
			exportService.endExportJobInfo(expData, IdcEntityType.OBJECT, userId);
			exportInfo = getJobInfo(JobType.EXPORT, userId, false, false);

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

	public IngridDocument analyzeImportData(IngridDocument docIn) {
	    IngridDocument result = new IngridDocument();
	    byte[] importData = (byte[])docIn.get(MdekKeys.REQUESTINFO_IMPORT_DATA);
	    String frontendProtocol = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL);
	    //Boolean importAfterAnalyze = docIn.getBoolean(MdekKeys.REQUESTINFO_IMPORT_DATA_AFTER_ANALYZE);
	    Boolean startNewAnalysis = docIn.getBoolean(MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS);
        boolean transactionInProgress = (boolean) getOrDefault(docIn, MdekKeys.REQUESTINFO_TRANSACTION_IS_HANDLED, false );
	    ProtocolHandler protocolHandler = new HashMapProtocolHandler();
	    byte[] mappedDataCompressed = importData;

	    String userUuid = getCurrentUserUuid(docIn);
	    try {
            addRunningJob(userUuid, createRunningJobDescription(JobType.IMPORT_ANALYZE, 0, 0, false));
    	    
            if (!transactionInProgress) {
                genericDao.beginTransaction();
            }
    
    	    try {
                if (!"igc".equals( frontendProtocol )) {
                    InputStream in = new GZIPInputStream(new ByteArrayInputStream(importData));

                    // Create source ISO XML Document from input stream
                    DocumentBuilderFactory nsAwareDbf = DocumentBuilderFactory.newInstance();
                    nsAwareDbf.setNamespaceAware(true);
                    Document source = nsAwareDbf.newDocumentBuilder()
                        .parse(in);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Source XML%n%s", XMLUtils.toString(source)));
                    }

                    // Create target empty document.
                    Document target = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .newDocument();

                    dataMapperFactory.getMapper(frontendProtocol).convert(source, target, protocolHandler);

                    /*
                     * Collect all errors from the protocol handler and put them
                     * in the result. If the mapping has been started by a CSW
                     * insert or update operation, then we will later retrieve
                     * them and return them as the exception text, if necessary.
                     */
                    List<String> errors = protocolHandler.getProtocol(ProtocolHandler.Type.ERROR);
                    if (!errors.isEmpty()) {
                        result.put("mapping_errors", String.join("\n - ", errors));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Converted XML%n%s", XMLUtils.toString(target)));
                    }
                    String xml = XMLUtils.toString(target);
                    InputStream mappedData = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                    mappedDataCompressed = compress(mappedData).toByteArray();
                }
    	    } catch( Exception ex ) {
    	        log.error( "There was an error during mapping." + ex.getMessage() );
    	        result.put( "error", "There was an error during mapping." + ex.getMessage() );
    	    }
    	    
    	    
            // then update job info in database
    	    SysJobInfo jobInfoDB = jobHandler.getJobInfoDB( JobType.IMPORT_ANALYZE, userUuid );
    	    List<byte[]> analyzedData = null;
    	    HashMap<String, List<byte[]>> jobDetails = null;
    	    if (jobInfoDB == null) {
    	        jobHandler.startJobInfoDB( JobType.IMPORT_ANALYZE, null, jobDetails, userUuid );
    	        jobDetails = new HashMap<String, List<byte[]>>();
    	    } else {
        	    jobDetails = jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, userUuid );
        	    if (jobDetails == null) jobDetails = new HashMap<String, List<byte[]>>();
        	    analyzedData = jobDetails.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
    	    }
            
    	    if (startNewAnalysis || analyzedData == null) analyzedData = new ArrayList<byte[]>();
    	    analyzedData.add( mappedDataCompressed );
    	    jobDetails.put( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA, analyzedData );
            jobHandler.updateJobInfoDB(JobType.IMPORT_ANALYZE, jobDetails, userUuid);
	    } catch (Exception ex) {
	        log.error( "Exception occurred during analysis", ex );
	    }
	    
	    jobHandler.removeRunningJob( userUuid );
	    
        // add end info
        jobHandler.endJobInfoDB(JobType.IMPORT_ANALYZE, userUuid);
	    
        if (!transactionInProgress) {
            genericDao.commitTransaction();
        }
	    
        result.put("protocol", protocolHandler);
        
//        if (importAfterAnalyze) {
//            docIn.put(MdekKeys.REQUESTINFO_IMPORT_DATA, mappedDataCompressed);
//            importEntities( docIn );
//        }
        
        return result;
	}
	
	/**
	 * Make sure to call analyzeImportData-function first, since the result will be
	 * stored inside the job info, which will be required here.
	 * @param docIn
	 * @return
	 */
	public IngridDocument importEntities(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean transactionInProgress = (boolean) getOrDefault( docIn, MdekKeys.REQUESTINFO_TRANSACTION_IS_HANDLED, false );
		boolean errorOnExisitingUuid = (boolean) getOrDefault( docIn, MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, false );
		boolean errorOnMissingUuid = (boolean) getOrDefault( docIn, MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_MISSING_UUID, false );
		boolean errorOnException = (boolean) getOrDefault( docIn, MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXCEPTION, false );
		boolean ignoreParentImportNodes = (boolean) getOrDefault( docIn, MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, false );
		boolean removeRunningJob = true;
		try {
		    if (!transactionInProgress) {
		        genericDao.beginTransaction();
		    }
		    
			IngridDocument jobDescr = createRunningJobDescription(JobType.IMPORT, 0, 0, false);
			jobDescr.put( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, errorOnExisitingUuid );
			jobDescr.put( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_MISSING_UUID, errorOnMissingUuid );
			jobDescr.put( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXCEPTION, errorOnException );
            jobDescr.put( MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, ignoreParentImportNodes );
            // first add basic running jobs info !
			addRunningJob(userId, jobDescr );

			HashMap<String, List<byte[]>> jobDetails = jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, userId );
			Object importData = jobDetails.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
			boolean multipleImportFiles = false;
			if (List.class.isAssignableFrom(importData.getClass())) {
				// multipleFiles !
				multipleImportFiles = true;
			}
			String defaultObjectParentUuid = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID);
			String defaultAddrParentUuid = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID);
			Boolean publishImmediately = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY);
			Boolean doSeparateImport = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT);
			Boolean copyNodeIfPresent = (Boolean) docIn.get(MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT);
			String frontendProtocol = (String) docIn.get(MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL);

			// CHECKS BEFORE START OF IMPORT

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// add frontend protocol to jobinfo (should start with that one)
			// NOTICE: check of import nodes already add top nodes to job info !
			if (frontendProtocol != null && !frontendProtocol.isEmpty()) {
				importService.updateImportJobInfoMessages("FRONTEND PROTOCOl:\n==================\n", userId);
				importService.updateImportJobInfoMessages(frontendProtocol, userId);
				importService.updateImportJobInfoMessages("\n\nBACKEND PROTOCOl:\n=================\n", userId);
			}

			// check top import nodes ! Adds messages to job info !
			if (ignoreParentImportNodes) {
			    // special handling for CSW-T import !
			    importService.handleObjectParent( defaultObjectParentUuid, userId);
			} else {
    			importService.checkDefaultParents(defaultObjectParentUuid, defaultAddrParentUuid, userId);
			}
			importService.setOptions(userId, publishImmediately, doSeparateImport, copyNodeIfPresent);

			// initialize import info in database
			importService.startImportJobInfo(userId);
			

			// import
			IImporter xmlImporter = new XMLImporter(importService);
			if (multipleImportFiles) {
				List<byte[]> importDataList = (List<byte[]>) importData;

				// FIRST COUNT OBJECTS / ADDRESSES of all files to get correct total number
				// before starting import of single files !
				xmlImporter.countEntities(importDataList, userId);

				for(int i=0; i<importDataList.size(); i++){
					// import
					xmlImporter.importEntities(importDataList.get(i), userId);
				}				
			} else {
				xmlImporter.importEntities((byte[])importData, userId);
			}
			
			// check whether "controlled problems" occured !
			importService.checkImportEntities(userId);

			// post process object relations (Querverweise) after importing of all entities
			importService.postProcessRelationsOfImport(userId);

			// finish and fetch import info in database
			importService.endImportJobInfo(userId);
			HashMap importInfo = getJobInfo(JobType.IMPORT, userId, false, false);

			if (!transactionInProgress) {
	            genericDao.commitTransaction();
	            
	            // Update search index with data of PUBLISHED entities and also log if set
	            updateSearchIndexAndAudit(jobHandler.getRunningJobChangedEntities(userId));
			}

			IngridDocument result = new IngridDocument();
			result.putAll(importInfo);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e, transactionInProgress);
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
	
	// Compress (zip) any data on InputStream and write it to a ByteArrayOutputStream
    public static ByteArrayOutputStream compress(InputStream is) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(is);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(new BufferedOutputStream(out));

        final int BUFFER = 2048;
        int count;
        byte data[] = new byte[BUFFER];
        while((count = bin.read(data, 0, BUFFER)) != -1) {
           gzout.write(data, 0, count);
        }

        gzout.close();
        return out;
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

			List<Object> urlList = docIn.getArrayList(MdekKeys.URL_RESULT);
			List<Object> capList = docIn.getArrayList(MdekKeys.CAP_RESULT);
			String jobStartTime = docIn.getString(MdekKeys.JOBINFO_START_TIME);
			boolean isUpdate = docIn.getBoolean(MdekKeys.JOBINFO_IS_UPDATE);
			boolean isFinished = docIn.getBoolean(MdekKeys.JOBINFO_IS_FINISHED);
			HashMap<String, Object> data = new HashMap<String, Object>();
			if (isUpdate) {
			    HashMap detail = jobHandler.getJobDetailsAsHashMap( JobType.URL, userId );
			    urlList.addAll( (List<Object>) detail.get( MdekKeys.URL_RESULT ) );
			    capList.addAll( (List<Object>) detail.get( MdekKeys.CAP_RESULT ) );
			    data.put(MdekKeys.URL_RESULT, urlList);
			    data.put(MdekKeys.CAP_RESULT, capList);
			    jobHandler.updateJobInfoDB(
			            JobType.URL,
			            data, userId);
			} else {
			    data.put(MdekKeys.URL_RESULT, urlList);
			    data.put(MdekKeys.CAP_RESULT, capList);
			    jobHandler.startJobInfoDB(
			            JobType.URL,
			            jobStartTime,
			            data, userId);
			}
			if (isFinished) {
			    jobHandler.endJobInfoDB(JobType.URL, userId);
			}

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
		List<Object> sourceUrls = docIn.getArrayList(MdekKeys.REQUESTINFO_URL_LIST);
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
			for (Object sourceUrlObject : sourceUrls) {
			    IngridDocument sourceUrl = (IngridDocument) sourceUrlObject;
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
		List<Object> urlList = docIn.getArrayList(MdekKeys.REQUESTINFO_URL_LIST);
		String targetUrl = docIn.getString(MdekKeys.REQUESTINFO_URL_TARGET);
		String type = docIn.getString(MdekKeys.LINKAGE_URL_TYPE);

		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			daoObjectNode.beginTransaction();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			IdcEntityVersion whichEntityVersion = IdcEntityVersion.PUBLISHED_VERSION;

			for (Object urlRefDocObject : urlList) {
			    IngridDocument urlRefDoc = (IngridDocument) urlRefDocObject;
				String uuid = (String) urlRefDoc.get(MdekKeys.URL_RESULT_OBJECT_UUID);
				String srcUrl = (String) urlRefDoc.get(MdekKeys.URL_RESULT_URL);

				// load node
				ObjectNode oNode = daoObjectNode.loadByUuid(uuid, whichEntityVersion);
				if (oNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}

				T01Object obj = oNode.getT01ObjectPublished();
				if ("capabilities".equals(type)) {
    				Set<T011ObjServ> urlRefs = obj.getT011ObjServs();
    				
    				
    				for (T011ObjServ urlRef : urlRefs) {
    				    Set<T011ObjServOperation> operations = urlRef.getT011ObjServOperations();
    				    for (T011ObjServOperation operation : operations) {
					        Set<T011ObjServOpConnpoint> connections = operation.getT011ObjServOpConnpoints();
					        for (T011ObjServOpConnpoint connection : connections) {
					            if (connection.getConnectPoint().equals(srcUrl)) {
					                connection.setConnectPoint(targetUrl);
					            }
					        }
    					}
    				}
				} else {
				    Set<T017UrlRef> urlRefs = (Set<T017UrlRef>) obj.getT017UrlRefs();
				    for (T017UrlRef urlRef : urlRefs) {
				        if (urlRef.getUrlLink().equals(srcUrl)) {
				            urlRef.setUrlLink(targetUrl);
				        }
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
			// - has to be published (will be set in published objects)
			if (!addressService.hasPublishedVersion(
					daoAddressNode.loadByUuid(newAddrUuid, IdcEntityVersion.PUBLISHED_VERSION))) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_PUBLISHED));
			}

			// REPLACE ALL ADDRESSES !
			int numObjectsChanged =
				catalogService.updateAddressInObjects(oldAddrUuid, newAddrUuid, userUuid);

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
			resultDoc.putInt("numObjectsChanged", numObjectsChanged);
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

	public IngridDocument getObjectsOfAddressByType(IngridDocument params) {
		try {
			String uuid = (String) params.get(MdekKeys.UUID);
			Integer maxNum = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);
			Integer referenceTypeId = (Integer) params.get(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			List<T01Object> objs = 
				daoT02Address.getObjectReferencesByTypeId(uuid, referenceTypeId, maxNum);

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
			
			if (csvType == CsvRequestType.OBJECTS_OF_ADDRESS) {
				hqlQuery = daoT02Address.getCsvHQLObjectReferencesByTypeId(uuid, null);
				
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
			String syslstEntryName = ((String[]) docIn.get(MdekKeys.LST_ENTRY_NAMES))[0];
	
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

			// initialize job info
			catalogService.startJobInfo(JobType.REBUILD_SYSLISTS, 0, 0, null, userId);

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
			
			// finish and fetch job info
			catalogService.endJobInfo(JobType.REBUILD_SYSLISTS, userId);
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

	public IngridDocument updateSearchTerms(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.UPDATE_SEARCHTERMS, 0, 0, false));

			List<IngridDocument> termsOld = (List<IngridDocument>) docIn.get(MdekKeys.SUBJECT_TERMS_OLD);
			List<IngridDocument> termsNew = (List<IngridDocument>) docIn.get(MdekKeys.SUBJECT_TERMS_NEW);
			if (termsOld == null || termsOld.size() == 0) {
				termsOld = new ArrayList<IngridDocument>(0);
			}

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// initialize job info
			catalogService.startJobInfo(JobType.UPDATE_SEARCHTERMS, 0, termsOld.size(), null, userId);

			// clear all caches, read NEWEST DATA !
			catalogService.clearCaches();

			// update content in entities and log protocol in job info
			catalogService.updateSearchTerms(termsOld, termsNew, userId);

			// finish and fetch job info in database
			catalogService.endJobInfo(JobType.UPDATE_SEARCHTERMS, userId);
			HashMap jobInfo = getJobInfo(JobType.UPDATE_SEARCHTERMS, userId, false, false);

			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(jobInfo);
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

	public IngridDocument getSpatialReferences(IngridDocument params) {
		try {
			SpatialReferenceType[] spatialRefTypes =
				(SpatialReferenceType[]) params.get(MdekKeys.REQUESTINFO_TYPES_OF_ENTITY);

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// get all locations of passed type(s)
			List<SpatialRefValue> spatialRefValues = daoSpatialRefValue.getSpatialRefValues(spatialRefTypes);

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


	public IngridDocument updateSpatialReferences(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.UPDATE_SPATIAL_REFERENCES, 0, 0, false));

			List<IngridDocument> spRefsOld = (List<IngridDocument>) docIn.get(MdekKeys.LOCATIONS_OLD);
			List<IngridDocument> spRefsNew = (List<IngridDocument>) docIn.get(MdekKeys.LOCATIONS_NEW);
			if (spRefsOld == null || spRefsOld.size() == 0) {
				spRefsOld = new ArrayList<IngridDocument>(0);
			}

			genericDao.beginTransaction();
			genericDao.disableAutoFlush();

			// check permissions !
			permissionHandler.checkIsCatalogAdmin(userId);

			// initialize job info
			catalogService.startJobInfo(JobType.UPDATE_SPATIAL_REFERENCES, 0, spRefsOld.size(), null, userId);

			// clear all caches, read NEWEST DATA !
			catalogService.clearCaches();

			// update content in entities and log protocol in job info
			catalogService.updateSpatialReferences(spRefsOld, spRefsNew, userId);

			// finish and fetch job info in database
			catalogService.endJobInfo(JobType.UPDATE_SPATIAL_REFERENCES, userId);
			HashMap jobInfo = getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES, userId, false, false);

			genericDao.flush();
			genericDao.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.putAll(jobInfo);
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

	public IngridDocument getLastModifiedTimestampOfSyslists(IngridDocument docIn) {
	    try {
            genericDao.beginTransaction();
            genericDao.disableAutoFlush();

            // get all locations of passed type(s)
            String[] keys = {"lastModifiedSyslist"};
            List<SysGenericKey> genericKeysValues = catalogService.getSysGenericKeys(keys);
            
            Long timestamp = -1L;
            if (genericKeysValues.size() != 0) { 
                timestamp = Long.valueOf(genericKeysValues.get(0).getValueString());
            }
            
            IngridDocument result = new IngridDocument();
            result.put(MdekKeys.LST_LAST_MODIFIED, timestamp);
            
            genericDao.commitTransaction();

            return result;

        } catch (RuntimeException e) {
            RuntimeException handledExc = handleException(e);
            throw handledExc;
        }
	}

    public String getCatalogAdminUserUuid() {
        genericDao.beginTransaction();
        String addrUuid = permissionHandler.getCatalogAdminUser().getAddrUuid();
        genericDao.commitTransaction();
        return addrUuid;
    }
    
    public void beginTransaction() {
        genericDao.beginTransaction();
    }
    
    public void commitTransaction() {
        genericDao.commitTransaction();
    }
    
    public void rollbackTransaction() {
        genericDao.rollbackTransaction();
    }
}
