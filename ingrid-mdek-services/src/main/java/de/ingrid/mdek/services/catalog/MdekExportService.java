/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.exporter.IExporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for exporter.
 */
public class MdekExportService implements IExporterCallback {

	private static MdekExportService myInstance;

	private MdekCatalogService catalogService;
	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekJobHandler jobHandler;

	private IObjectNodeDao daoObjectNode;
	private IAddressNodeDao daoAddressNode;
	private IGenericDao<IEntity> daoObjectMetadata;
	private IGenericDao<IEntity> daoAddressMetadata;

	/** Get The Singleton */
	public static synchronized MdekExportService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekExportService(daoFactory, permissionService);
	      }
		return myInstance;
	}

	private MdekExportService(DaoFactory daoFactory,
			IPermissionService permissionService) {
		catalogService = MdekCatalogService.getInstance(daoFactory);
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		jobHandler = MdekJobHandler.getInstance(daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoAddressNode = daoFactory.getAddressNodeDao();
		daoObjectMetadata = daoFactory.getDao(ObjectMetadata.class);
		daoAddressMetadata = daoFactory.getDao(AddressMetadata.class);
	}

	// ----------------------------------- IExporterCallback START ------------------------------------

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getTotalNumObjectsToExport(java.util.List, boolean, java.lang.String)
	 */
	public int getTotalNumObjectsToExport(List<String> objUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid) {
		int totalNum = 0;
		
		for (String objUuid : objUuids) {
			// we fetch every node to guarantee requested version is there !
			ObjectNode oN = daoObjectNode.loadByUuid(objUuid, null);
			if (objectService.hasVersion(oN, whichVersion)) {
				totalNum++;
			}
		}
		if (includeSubnodes) {
			for (String objUuid : objUuids) {
				// count only requested versions !
				totalNum += daoObjectNode.countAllSubObjects(objUuid, whichVersion);
			}
		}

		return totalNum;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getObjectDetails(java.lang.String, java.lang.String)
	 */
	public List<IngridDocument> getObjectDetails(String objUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> docs = objectService.getObjectInstancesDetails(objUuid,
				whichVersion, FetchQuantity.EXPORT_ENTITY,
				userUuid);

		// we write lastexport_time when fetching details for export !
		// if something fails this one isn't written due to transaction rollback ...
		// NOTICE: we don't flush before to assure most current data ! autoflush is disabled during export !
		for (IngridDocument doc : docs) {
			Long metadataId = (Long) doc.get(MdekKeys.ENTITY_METADATA_ID);

			// may be null ??? was the case in TH catalog (INGRID-2360). Metadata is generated if not present when address/object is saved !
			// see DocToBeanMapper.updateObjectMetadata()
			if (metadataId != null) {
				ObjectMetadata metadata = (ObjectMetadata) daoObjectMetadata.getById(metadataId);
				metadata.setLastexportTime(MdekUtils.dateToTimestamp(new Date()));
				daoObjectMetadata.makePersistent(metadata);				
			}
		}

		return docs;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getAddressDetails(java.lang.String, java.lang.String)
	 */
	public List<IngridDocument> getAddressDetails(String addrUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> docs = addressService.getAddressInstancesDetails(addrUuid,
				whichVersion, FetchQuantity.EXPORT_ENTITY,
				0, 0,
				userUuid);

		// we write lastexport_time when fetching details for export !
		// if something fails this one isn't written due to transaction rollback ...
		// NOTICE: we don't flush before to assure most current data ! autoflush is disabled during export !
		for (IngridDocument doc : docs) {
			Long metadataId = (Long) doc.get(MdekKeys.ENTITY_METADATA_ID);

			// may be null ??? was the case in TH catalog (INGRID-2360). Metadata is generated if not present when address/object is saved !
			// see DocToBeanMapper.updateAddressMetadata()
			if (metadataId != null) {
				AddressMetadata metadata = (AddressMetadata) daoAddressMetadata.getById(metadataId);
				metadata.setLastexportTime(MdekUtils.dateToTimestamp(new Date()));
				daoAddressMetadata.makePersistent(metadata);
			}
		}

		return docs;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getTotalNumAddressesToExport(java.util.List, boolean, java.lang.String)
	 */
	public int getTotalNumAddressesToExport(List<String> addrUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid) {
		int totalNum = 0;
		
		for (String addrUuid : addrUuids) {
			// we fetch every node to guarantee requested version is there !
			AddressNode aN = daoAddressNode.loadByUuid(addrUuid, null);
			if (addressService.hasVersion(aN, whichVersion)) {
				totalNum++;
			}
		}
		if (includeSubnodes) {
			for (String addrUuid : addrUuids) {
				// count only requested versions !
				totalNum += daoAddressNode.countAllSubAddresses(addrUuid, whichVersion);
			}
		}

		return totalNum;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubObjects(java.lang.String, java.lang.String)
	 */
	public List<String> getSubObjects(String parentUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<String> uuids = new ArrayList<String>();

		List<ObjectNode> nodes = daoObjectNode.getSubObjects(parentUuid, whichVersion, false);
		// only objects which have requested version(s) !
		for (ObjectNode node : nodes) {
			if (objectService.hasVersion(node, whichVersion)) {
				uuids.add(node.getObjUuid());
			}
		}

		return uuids;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubAddresses(java.lang.String, java.lang.String)
	 */
	public List<String> getSubAddresses(String parentUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<String> uuids = new ArrayList<String>();

		List<AddressNode> nodes = daoAddressNode.getSubAddresses(parentUuid, whichVersion, false);
		// only addresses which have requested version(s) !
		for (AddressNode node : nodes) {
			if (addressService.hasVersion(node, whichVersion)) {
				uuids.add(node.getAddrUuid());
			}
		}

		return uuids;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#writeExportInfo(de.ingrid.mdek.MdekUtils.IdcEntityType, int, int, java.lang.String)
	 */
	public void writeExportInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid) {
		updateExportJobInfo(whichType, numExported, totalNum, userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#writeExportInfoMessage(java.lang.String, java.lang.String)
	 */
	public void writeExportInfoMessage(String newMessage, String userUuid) {
		updateExportJobInfoMessages(newMessage, userUuid);
	}

	// ----------------------------------- IExporterCallback END ------------------------------------

	/**
	 * Get uuids of top objects for export (only published ones) !
	 * @return List of uuids or empty list
	 */
	public List<String> getTopObjectUuids() {
		List<String> uuids = new ArrayList<String>();

		List<ObjectNode> nodes = daoObjectNode.getTopObjects(null, false);
		// only published ones !
		for (ObjectNode node : nodes) {
			Long idPublished = node.getObjIdPublished();
			if (idPublished != null) {
				uuids.add(node.getObjUuid());
			}
		}

		return uuids;
	}

	/**
	 * Get uuids of top addresses for export (only published ones) !
	 * @return List of uuids or empty list
	 */
	public List<String> getTopAddressUuids(AddressArea addressArea) {
		List<String> uuids = new ArrayList<String>();

		if (addressArea == AddressArea.ALL_ADDRESSES ||
			addressArea == AddressArea.ALL_NON_FREE_ADDRESSES) {
			List<AddressNode> nodes = daoAddressNode.getTopAddresses(
					false, IdcEntityVersion.PUBLISHED_VERSION, false);
			// only published ones !
			for (AddressNode node : nodes) {
				// further check whether published, just to be sure !
				Long idPublished = node.getAddrIdPublished();
				if (idPublished != null) {
					uuids.add(node.getAddrUuid());
				}
			}
		}
		if (addressArea == AddressArea.ALL_ADDRESSES ||
			addressArea == AddressArea.ALL_FREE_ADDRESSES) {
			List<AddressNode> nodes = daoAddressNode.getTopAddresses(
					true, IdcEntityVersion.PUBLISHED_VERSION, false);
			// only published ones !
			for (AddressNode node : nodes) {
				// further check whether published, just to be sure !
				Long idPublished = node.getAddrIdPublished();
				if (idPublished != null) {
					uuids.add(node.getAddrUuid());
				}
			}
		}

		return uuids;
	}

	/**
	 * Returns "logged" Export job information IN DATABASE.
	 * NOTICE: returns EMPTY HashMap if no job info !
	 * @param userUuid calling user
	 * @param includeData true=export result data is included in info<br>
	 * 		false=not included
	 * @return
	 */
	public HashMap getExportJobInfoDB(String userUuid, boolean includeData) {
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.EXPORT, userUuid);
		HashMap expInfo = jobHandler.mapJobInfoDB(jobInfo);
		
		if (!includeData) {
			expInfo.remove(MdekKeys.EXPORT_RESULT);
		}
		
		return expInfo;
	}
	/** "logs" Start-Info of Export job IN MEMORY and IN DATABASE */
	public void startExportJobInfo(IdcEntityType whichType, int totalNum, String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.EXPORT, whichType.getDbValue(), 0, totalNum, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
        HashMap details = jobHandler.setUpJobInfoDetailsDB(whichType.getDbValue(), 0, totalNum);
		jobHandler.startJobInfoDB(JobType.EXPORT, startTime, details, userUuid);
	}
	/** Update general info of Export job IN MEMORY. */
	public void updateExportJobInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid, 
				jobHandler.createRunningJobDescription(JobType.EXPORT, whichType.getDbValue(), numExported, totalNum, false));

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//        HashMap details = setUpExportJobInfoDetailsDB(whichType, numExported, totalNum);
//		jobHandler.updateJobInfoDB(JobType.EXPORT, details, userUuid);
	}
	/** Add new Message to info of Export job IN MEMORY. */
	public void updateExportJobInfoMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobMessages(userUuid, newMessage);

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//		jobHandler.updateJobInfoDBMessages(JobType.EXPORT, newMessage, userUuid);
	}
	/** Logs given Exception in info of Export job IN DATABASE. */
	public void updateExportJobInfoException(Exception exceptionToLog, String userUuid) {
		// no log in memory, this one should be called when job has to be exited ...

		// log in job info in database
		jobHandler.updateJobInfoDBException(JobType.EXPORT, exceptionToLog, userUuid);
	}

	/**
	 * Add export result data to export job info and "logs" End-Info 
	 * in Export job information IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param data result data from export (xml)
	 * @param whichEntityType entity type the export processed (not stored in running job info !)
	 * @param userUuid calling user
	 */
	public void endExportJobInfo(byte[] data,
			IdcEntityType whichEntityType, String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails =
			jobHandler.getJobInfoDetailsFromRunningJobInfo(runningJobInfo, true);
		jobDetails.put(MdekKeys.EXPORT_RESULT, data);

		// then update job info in database
		jobHandler.updateJobInfoDB(JobType.EXPORT, jobDetails, userUuid);
		// add end info
		jobHandler.endJobInfoDB(JobType.EXPORT, userUuid);
	}
}
