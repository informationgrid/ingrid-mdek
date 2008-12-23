package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.HashMap;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for importer.
 */
public class MdekImportService implements IImporterCallback {

	private static MdekImportService myInstance;

	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekJobHandler jobHandler;

	// static keys for accessing data stored in running job info !
	/** Value: ObjectNode */
	private final static String KEY_OBJ_TOP_NODE = "IMPORTSERVICE_OBJ_TOP_NODE";
	/** Value: AddressNode */
	private final static String KEY_ADDR_TOP_NODE = "IMPORTSERVICE_ADDR_TOP_NODE";
	/** Value: Boolean */
	private final static String KEY_PUBLISH_IMMEDIATELY = "IMPORTSERVICE_PUBLISH_IMMEDIATELY";
	/** Value: Boolean */
	private final static String KEY_DO_SEPARATE_IMPORT = "IMPORTSERVICE_DO_SEPARATE_IMPORT";


	/** Get The Singleton */
	public static synchronized MdekImportService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekImportService(daoFactory, permissionService);
	      }
		return myInstance;
	}

	private MdekImportService(DaoFactory daoFactory,
			IPermissionService permissionService) {
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		jobHandler = MdekJobHandler.getInstance(daoFactory);
	}

	// ----------------------------------- IImporterCallback START ------------------------------------

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeObject(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeObject(IngridDocument objDoc, String userUuid) {
		// extract context
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		boolean publishImmediately = (Boolean) runningJobInfo.get(KEY_PUBLISH_IMMEDIATELY);
		boolean doSeparateImport = (Boolean) runningJobInfo.get(KEY_DO_SEPARATE_IMPORT);
		ObjectNode defaultParentNode = (ObjectNode) runningJobInfo.get(KEY_OBJ_TOP_NODE);
		int numImported = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
		int totalNum = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);

/*
 * - Unter Importknoten werden die Objekte NIEMALS gepublished !
 * - Alle neuen Entities wandern unter Importknoten, ES SEI DENN IHR PARENT EXISITIERT
 *   - Alle neuen freien Adressen wandern unter Importknoten als "PERSON"
 *   - Alle top Entities wandern unter den Importknoten
 */
		String objUuid = objDoc.getString(MdekKeys.UUID);
		String origId = objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);

		ObjectNode parentNode = processParent(objDoc, defaultParentNode);


		if (doSeparateImport) {
			// TODO: check if UUID exists, then create and set new UUID
			// TODO: check if ORIG_ID exists, then delete ORG_ID ?
			
		} else {
			// TODO: check if ORIG_ID exists, then take over UUID of existing object
			// TODO: take over mod_uuid and responsible_uuid of existing catalog object or set uuid of calling user if new !
			// TODO: check if parent changed, then move object			
		}

		// TODO: check additional fields -> store working version if problems

		boolean storeWorkingVersion = true;
		if (publishImmediately) {
			storeWorkingVersion = false;

			// TODO: check mandatory data -> store working version if problems
			// TODO: check whether parent published (no working version) -> store working version if problems
			
			String errorMsg = "Problems publishing imported Object " +  objUuid + ": ";
			try {
				objectService.publishObject(objDoc, false, userUuid, false);
				updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
				updateImportJobInfoMessages("Updated PUBLISHED version of Object " + objUuid, userUuid);

			} catch (MdekException ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
				storeWorkingVersion = true;
			} catch (Exception ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
				storeWorkingVersion = true;				
			}
		}
		
		String errorMsg = "Problems storing working version of imported Object " +  objUuid + ": ";
		if (storeWorkingVersion) {
			if (publishImmediately) {
				updateImportJobInfoMessages("We store working version of Object " + objUuid, userUuid);
			}

			try {
				objectService.storeWorkingCopy(objDoc, userUuid, false);
				updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
				updateImportJobInfoMessages("Updated WORKING version of Object " + objUuid, userUuid);

			} catch (MdekException ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
				storeWorkingVersion = true;
			} catch (Exception ex) {
				updateImportJobInfoMessages(ex.getMessage(), userUuid);
				storeWorkingVersion = true;				
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeAddress(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeAddress(IngridDocument addrDoc, String userUuid) {
		// extract context
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		boolean publishImmediately = (Boolean) runningJobInfo.get(KEY_PUBLISH_IMMEDIATELY);

		// TODO: check whether parent exists ! else set default !
		
		if (publishImmediately) {
			// TODO: check mandatory data

		} else {
			
		}
	}

	// ----------------------------------- IImporterCallback END ------------------------------------

	/** Returns "logged" Import job information IN DATABASE.
	 * NOTICE: returns EMPTY HashMap if no job info ! */
	public HashMap getImportJobInfoDB(String userUuid) {
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.IMPORT, userUuid);
		return jobHandler.mapJobInfoDB(jobInfo);
	}
	/** "logs" Start-Info of Import job IN MEMORY and IN DATABASE */
	public void startImportJobInfo(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.IMPORT, 0, 0, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
		jobHandler.startJobInfoDB(JobType.IMPORT, startTime, null, userUuid);
	}
	/** Update general info of Import job IN MEMORY. */
	public void updateImportJobInfo(IdcEntityType whichType, int numImported, int totalNum,
			String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid,
				jobHandler.createRunningJobDescription(JobType.IMPORT, whichType, numImported, totalNum, false));

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//        HashMap details = MdekImportService.setUpImportJobInfoDetailsDB(whichType, numImported, totalNum);
//		jobHandler.updateJobInfoDB(JobType.IMPORT, details, userUuid);
	}
	/** Add new Message to info of Import job IN MEMORY. */
	public void updateImportJobInfoMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobMessages(userUuid, newMessage);

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//		jobHandler.updateJobInfoDBMessages(JobType.IMPORT, newMessage, userUuid);
	}

	/**
	 * "logs" End-Info in import job information IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param userUuid calling user
	 */
	/** "logs" End-Info in Import information IN DATABASE */
	public void endImportJobInfo(String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails = jobHandler.getJobInfoDetailsFromRunningJobInfo(
				runningJobInfo, true);

		// then update job info in database
		jobHandler.updateJobInfoDB(JobType.IMPORT, jobDetails, userUuid);
		// add end info
		jobHandler.endJobInfoDB(JobType.IMPORT, userUuid);
	}

	/**
	 * Arbitrary checks whether passed default parents for imported entities are ok for requested import.
	 * Throws Exception encapsulating error message if not.
	 * @param defaultObjectParentUuid uuid of default parent for imported objects
	 * @param defaultAddrParentUuid  uuid of default parent for imported addresses
	 * @param publishImmediately publish imported entities immediately ?
	 * @param doSeparateImport separate all imported entities underneath the chosen "import nodes".
	 * @param userUuid calling user
	 * @throws MdekException encapsulates error message
	 */
	public void checkDefaultParents(String defaultObjectParentUuid, String defaultAddrParentUuid,
			boolean publishImmediately, boolean doSeparateImport,
			String userUuid)
	throws MdekException {
		if (defaultObjectParentUuid == null) {
			throw createImportException("Top Node for Import of Objects not set.");
		}
		if (defaultAddrParentUuid == null) {
			throw createImportException("Top Node for Import of Addresses not set.");
		}

		// fetch and check nodes
		ObjectNode topObjImportNode;
		AddressNode topAddrImportNode;
		if (publishImmediately) {
			topObjImportNode = objectService.loadByUuid(defaultObjectParentUuid, IdcEntityVersion.PUBLISHED_VERSION);
			topAddrImportNode = addressService.loadByUuid(defaultAddrParentUuid, IdcEntityVersion.PUBLISHED_VERSION);
		} else {
			topObjImportNode = objectService.loadByUuid(defaultObjectParentUuid, IdcEntityVersion.WORKING_VERSION);
			topAddrImportNode = addressService.loadByUuid(defaultAddrParentUuid, IdcEntityVersion.WORKING_VERSION);
		}
		if (topObjImportNode == null) {
			throw createImportException("Top Node for Import of Objects not found.");
		}			
		if (topAddrImportNode == null) {
			throw createImportException("Top Node for Import of Addresses not found.");
		}			

		// fetch and check entities
		T01Object topObjImport;
		T02Address topAddrImport;
		if (publishImmediately) {
			topObjImport = topObjImportNode.getT01ObjectPublished();
			if (topObjImport == null) {
				throw createImportException("Top Node for Import of Objects not published.");
			}
			topAddrImport = topAddrImportNode.getT02AddressPublished();
			if (topAddrImport == null) {
				throw createImportException("Top Node for Import of Addresses not published.");
			}			
		} else {
			topObjImport = topObjImportNode.getT01ObjectWork();
			topAddrImport = topAddrImportNode.getT02AddressWork();
		}

		// check top address import node
		if (!MdekUtils.AddressType.INSTITUTION.getDbValue().equals(topAddrImport.getAdrType())) {
			throw createImportException("Top Node for Import of Addresses is NO Institution.");
		}
		
		// all ok, we store data in running job info for later access !
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		runningJobInfo.put(KEY_OBJ_TOP_NODE, topObjImportNode);
		runningJobInfo.put(KEY_ADDR_TOP_NODE, topAddrImportNode);
		runningJobInfo.put(KEY_PUBLISH_IMMEDIATELY, publishImmediately);
		runningJobInfo.put(KEY_DO_SEPARATE_IMPORT, doSeparateImport);
	}
	
	private MdekException createImportException(String message) {
		return new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, message));
	}

	/** Check whether parent in doc exists else set default parent. Returns the detected parent. */
	private ObjectNode processParent(IngridDocument objDoc, ObjectNode defaultParentNode) {
		ObjectNode retParentNode = null;

		String docParentUuid = objDoc.getString(MdekKeys.PARENT_UUID);
		if (docParentUuid != null) {
			// check whether parent in doc exists and fetch according version
			retParentNode = objectService.loadByUuid(docParentUuid, null);
		}

		if (retParentNode == null) {
			retParentNode = defaultParentNode;
			objDoc.put(MdekKeys.PARENT_UUID, retParentNode.getObjUuid());
		}

		return retParentNode;
	}
}
