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
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
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
	}

	// ----------------------------------- IExporterCallback START ------------------------------------

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getTotalNumObjectsToExport(java.util.List, boolean, java.lang.String)
	 */
	public int getTotalNumObjectsToExport(List<String> objUuids,
			boolean includeSubnodes,
			String userUuid) {
		int totalNum = objUuids.size();
		
		if (includeSubnodes) {
			for (String objUuid : objUuids) {
				// count only published ones !
				totalNum += daoObjectNode.countAllSubObjects(objUuid, IdcEntityVersion.PUBLISHED_VERSION);
			}
		}

		return totalNum;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getObjectDetails(java.lang.String, java.lang.String)
	 */
	public IngridDocument getObjectDetails(String objUuid, String userUuid) {
		return objectService.getObjectDetails(objUuid,
				IdcEntityVersion.PUBLISHED_VERSION, FetchQuantity.EXPORT_ENTITY,
				userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getAddressDetails(java.lang.String, java.lang.String)
	 */
	public IngridDocument getAddressDetails(String addrUuid, String userUuid) {
		return addressService.getAddressDetails(addrUuid,
				IdcEntityVersion.PUBLISHED_VERSION, FetchQuantity.EXPORT_ENTITY,
				0, 0,
				userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getTotalNumAddressesToExport(java.util.List, boolean, java.lang.String)
	 */
	public int getTotalNumAddressesToExport(List<String> addrUuids,
			boolean includeSubnodes,
			String userUuid) {		
		int totalNum = addrUuids.size();
		
		if (includeSubnodes) {
			for (String addrUuid : addrUuids) {
				// count only published ones !
				totalNum += daoAddressNode.countAllSubAddresses(addrUuid, IdcEntityVersion.PUBLISHED_VERSION);
			}
		}

		return totalNum;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubObjects(java.lang.String, java.lang.String)
	 */
	public List<String> getSubObjects(String parentUuid, String userUuid) {
		List<String> uuids = new ArrayList<String>();

		List<ObjectNode> nodes = daoObjectNode.getSubObjects(parentUuid, null, false);
		// only published ones !
		for (ObjectNode node : nodes) {
			Long idPublished = node.getObjIdPublished();
			if (idPublished != null) {
				uuids.add(node.getObjUuid());
			}
		}

		return uuids;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubAddresses(java.lang.String, java.lang.String)
	 */
	public List<String> getSubAddresses(String parentUuid, String userUuid) {
		List<String> uuids = new ArrayList<String>();

		List<AddressNode> nodes = daoAddressNode.getSubAddresses(parentUuid, null, false);
		// only published ones !
		for (AddressNode node : nodes) {
			Long idPublished = node.getAddrIdPublished();
			if (idPublished != null) {
				uuids.add(node.getAddrUuid());
			}
		}

		return uuids;
	}


	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSysAdditionalFields(java.lang.Long[])
	 */
	public IngridDocument getSysAdditionalFields(Long[] fieldIds) {
		return catalogService.getSysAdditionalFields(fieldIds, null);
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
	 * Maps Info from passed RunningJobInfo to ExportInfo map.
	 * @param runningJobInfo info from running job
	 * @param whichEntityType which entity type processes the current job (not stored in job info !)
	 * @param includeMessages also map messages ? (or only basic data)
	 * @return map containing export information
	 */
	public HashMap getExportInfoFromRunningJobInfo(HashMap runningJobInfo,
			IdcEntityType whichEntityType,
			boolean includeMessages) {
		// set up job info details just like it wouild be stored in DB
        HashMap expInfo = setUpExportJobInfoDetailsDB(
        		// we don't know which entity type from RunningJobInfo ! default is objects !
        		whichEntityType,
        		(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES),
        		(Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES));
        // also add start time from running job (was explicitly added, see startExportInfoDB)
        expInfo.put(MdekKeys.JOBINFO_START_TIME, runningJobInfo.get(MdekKeys.JOBINFO_START_TIME));
        
        if (includeMessages) {
            expInfo.put(MdekKeys.JOBINFO_MESSAGES, runningJobInfo.get(MdekKeys.RUNNINGJOB_MESSAGES));        	
        }
		
		return expInfo;
	}

	/**
	 * Returns "logged" Export job information IN DATABASE.
	 * NOTICE: returns EMPTY HashMap if no job info !
	 * @param userUuid calling user
	 * @param includeData true=export result data is included in info<br>
	 * 		false=not included
	 * @return
	 */
	public HashMap getExportInfoDB(String userUuid, boolean includeData) {
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
			jobHandler.createRunningJobDescription(JobType.EXPORT, 0, totalNum, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
        HashMap details = setUpExportJobInfoDetailsDB(whichType, 0, totalNum);
		jobHandler.startJobInfoDB(JobType.EXPORT, startTime, details, userUuid);
	}
	/** Update general info of Export job IN MEMORY and IN DATABASE */
	public void updateExportJobInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid, 
				jobHandler.createRunningJobDescription(JobType.EXPORT, numExported, totalNum, false));

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//        HashMap details = setUpExportJobInfoDetailsDB(whichType, numExported, totalNum);
//		jobHandler.updateJobInfoDB(JobType.EXPORT, details, userUuid);
	}
	/** Add new Message to info of Export job IN MEMORY and IN DATABASE. */
	public void updateExportJobInfoMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobMessages(userUuid, newMessage);

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//		jobHandler.updateJobInfoDBMessages(JobType.EXPORT, newMessage, userUuid);
	}

	/** "logs" End-Info in Export information IN DATABASE */
	/**
	 * Add export result data to export job info and "logs" End-Info 
	 * in Export information IN DATABASE <br>
	 * NOTICE: when job is running we store all info in memory (running job info).
	 * This method persists that info !
	 * @param data result data from export (xml)
	 * @param whichEntityType entity type the export processed (not stored in running job info !)
	 * @param userUuid calling user
	 */
	public void endExportJobInfo(byte[] data,
			IdcEntityType whichEntityType, String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails = getExportInfoFromRunningJobInfo(runningJobInfo, whichEntityType, true);
		jobDetails.put(MdekKeys.EXPORT_RESULT, data);

		// persistent store
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.EXPORT, userUuid);
		jobInfo.setJobDetails(jobHandler.formatJobDetailsForDB(jobDetails));
		jobHandler.persistJobInfoDB(jobInfo, userUuid);

		// add end info
		jobHandler.endJobInfoDB(JobType.EXPORT, userUuid);
	}

	/** Set up basic details (no messages !) of export to be stored in database. */
	public static HashMap setUpExportJobInfoDetailsDB(IdcEntityType whichType, int num, int totalNum) {
        HashMap details = new HashMap();
        if (whichType == IdcEntityType.OBJECT) {
            details.put(MdekKeys.JOBINFO_TOTAL_NUM_OBJECTS, totalNum);        	
            details.put(MdekKeys.JOBINFO_NUM_OBJECTS, num);
        } else if (whichType == IdcEntityType.ADDRESS) {
            details.put(MdekKeys.JOBINFO_TOTAL_NUM_ADDRESSES, totalNum);        	
            details.put(MdekKeys.JOBINFO_NUM_ADDRESSES, num);
        }
		
        return details;
	}
}
