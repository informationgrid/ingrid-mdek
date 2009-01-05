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

		ObjectNode parentNode = determineObjectParentNode(objDoc, defaultParentNode, userUuid);
		boolean hasDefaultParent = (parentNode == defaultParentNode);

		if (doSeparateImport) {
			// TODO: check if UUID exists, then create and set new UUID. Store UUID mapping in Map and use for all import objects (so relations stay the same) !
			// TODO: check if ORIG_ID exists, then delete ORG_ID ?
			
		} else {
			// TODO: check if ORIG_ID exists, then take over UUID of existing object
			// TODO: take over mod_uuid and responsible_uuid of existing catalog object or set uuid of calling user if new !
			// TODO: check if parent changed, then move object. Also move Object to Top ???
		}

		// TODO: check additional fields -> store working version if problems
		// TODO: check relations and remove if not present


		// PUBLISH IMPORT OBJECTS
		// ----------------------

		boolean storeWorkingVersion = true;		
		if (publishImmediately) {
			storeWorkingVersion = false;

			// TODO: check mandatory data -> store working version if problems

			// first check whether publishing is possible with according parent
			if (hasDefaultParent) {
				// parent not found or new top node -> store working version ! (message already written)
				storeWorkingVersion = true;
				
			} else if (!objectService.hasPublishedVersion(parentNode)) {
				// parent found but not published -> store working version !
				updateImportJobInfoMessages("! Object " + objUuid + ": Parent not published", userUuid);
				storeWorkingVersion = true;
				
			} else {
				// ok, we publish. On error store working version !
				String errorMsg = "! Object " +  objUuid + ": Problems publishing: ";
				try {
					// we DON'T force publication condition ! if error, we store working version !
					objectService.publishObject(objDoc, false, userUuid, false);
					updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
					updateImportJobInfoMessages("Object " + objUuid + ": Stored PUBLISHED version", userUuid);

				} catch (MdekException ex) {
					updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
					storeWorkingVersion = true;
				} catch (Exception ex) {
					updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
					storeWorkingVersion = true;				
				}
			}
		}

		// STORE WORKING COPY IMPORT OBJECTS
		// ---------------------------------

		if (storeWorkingVersion) {
			if (publishImmediately) {
				updateImportJobInfoMessages("! Object " +  objUuid + ": Publishing not possible, we store working version", userUuid);
			}

			String errorMsg = "! Object " +  objUuid + ": Problems storing working version: ";
			try {
				objectService.storeWorkingCopy(objDoc, userUuid, false);
				updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
				updateImportJobInfoMessages("Object " + objUuid + ": Stored WORKING version", userUuid);

			} catch (MdekException ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
				storeWorkingVersion = true;
			} catch (Exception ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
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
		// ONLY WORKING VERSION ! Under these nodes there will never be a published version (IMPORT NODES never published !)

		ObjectNode objImportNode = objectService.loadByUuid(defaultObjectParentUuid, IdcEntityVersion.WORKING_VERSION);
		AddressNode addrImportNode = addressService.loadByUuid(defaultAddrParentUuid, IdcEntityVersion.WORKING_VERSION);
		if (objImportNode == null) {
			throw createImportException("Node for Import of Objects not found.");
		}			
		if (addrImportNode == null) {
			throw createImportException("Node for Import of Addresses not found.");
		}			

		// fetch and check entities

		// check top address for import
		T02Address addrImport = addrImportNode.getT02AddressWork();
		if (!MdekUtils.AddressType.INSTITUTION.getDbValue().equals(addrImport.getAdrType())) {
			throw createImportException("Node for Import of Addresses is NO Institution.");
		}

		// if all should be imported underneath import node, then publishing not possible !
		if (doSeparateImport && publishImmediately) {
			throw createImportException("Publishing underneath Import Nodes not possible.");			
		}

		// all ok, we store data in running job info for later access !
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		runningJobInfo.put(KEY_OBJ_TOP_NODE, objImportNode);
		runningJobInfo.put(KEY_ADDR_TOP_NODE, addrImportNode);
		runningJobInfo.put(KEY_PUBLISH_IMMEDIATELY, publishImmediately);
		runningJobInfo.put(KEY_DO_SEPARATE_IMPORT, doSeparateImport);
	}
	
	private MdekException createImportException(String message) {
		return new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, message));
	}

	/** Check whether parent in doc exists else set default parent. Returns the detected parent. */
	private ObjectNode determineObjectParentNode(IngridDocument objDoc, ObjectNode defaultParentNode,
			String userUuid) {
		String objUuid = objDoc.getString(MdekKeys.UUID);
		ObjectNode retParentNode = null;

		String docParentUuid = objDoc.getString(MdekKeys.PARENT_UUID);
		boolean setDefaultParent = false;
		if (docParentUuid != null) {
			// node to import has parent. check whether parent exists and fetch it.

			retParentNode = objectService.loadByUuid(docParentUuid, null);
			if (retParentNode == null) {
				updateImportJobInfoMessages("! Object " + objUuid + ": Parent not found, store underneath import node", userUuid);
				setDefaultParent = true;
			}
		} else {
			// node to import is top node. check whether new top node !

			ObjectNode existingNode = objectService.loadByUuid(objUuid, null);
			if (existingNode == null) {
				// new top node, import underneath import node
				updateImportJobInfoMessages("! Object " + objUuid + ": New top node, store underneath import node", userUuid);				
				setDefaultParent = true;
			} else {
				// existing node. if parent is import node DON'T MOVE !
				// TODO: können bestehende Knoten unter Import Knoten verschoben werden aufgrund von neuem Import ? z.B. direktes Kind unter Import-Knoten zu Top Knoten ? 
				if (defaultParentNode.getObjUuid().equals(existingNode.getFkObjUuid())) {
					// TODO: Verschiebung nicht möglich
					updateImportJobInfoMessages("! Object " + objUuid + ": Keep position underneath import node", userUuid);				
					setDefaultParent = true;
				}
			}
		}

		if (setDefaultParent) {
			retParentNode = defaultParentNode;
			objDoc.put(MdekKeys.PARENT_UUID, retParentNode.getObjUuid());			
		}

		return retParentNode;
	}
}
