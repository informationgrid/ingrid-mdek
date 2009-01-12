package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for importer.
 */
public class MdekImportService implements IImporterCallback {

	private static final Logger LOG = Logger.getLogger(MdekImportService.class);

	private static MdekImportService myInstance;

	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekJobHandler jobHandler;

	private BeanToDocMapper beanToDocMapper;

	// static keys for accessing data stored in running job info !
	/** Value: ObjectNode */
	private final static String KEY_OBJ_IMPORT_NODE = "IMPORTSERVICE_OBJ_IMPORT_NODE";
	/** Value: AddressNode */
	private final static String KEY_ADDR_IMPORT_NODE = "IMPORTSERVICE_ADDR_IMPORT_NODE";
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

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
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
		ObjectNode objImportNode = (ObjectNode) runningJobInfo.get(KEY_OBJ_IMPORT_NODE);
		int numImported = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
		int totalNum = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);

/*
 * - Unter Importknoten werden die Objekte NIEMALS gepublished !
 * - Alle neuen Entities wandern unter Importknoten, ES SEI DENN IHR PARENT EXISITIERT
 *   - Alle neuen freien Adressen wandern unter Importknoten als "PERSON"
 *   - Alle top Entities wandern unter den Importknoten
 */

		// process import doc, remove/fix wrong data from exporting catalog
		if (objDoc.get(MdekKeys.UUID) != null && objDoc.getString(MdekKeys.UUID).trim().length() == 0) {
			objDoc.remove(MdekKeys.UUID);			
		}
		if (objDoc.get(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER) != null && objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER).trim().length() == 0) {
			objDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);			
		}
		if (objDoc.get(MdekKeys.PARENT_UUID) != null && objDoc.getString(MdekKeys.PARENT_UUID).trim().length() == 0) {
			objDoc.remove(MdekKeys.PARENT_UUID);			
		}
		objDoc.remove(MdekKeys.CATALOGUE_IDENTIFIER);
		objDoc.remove(MdekKeys.DATE_OF_LAST_MODIFICATION);
		objDoc.remove(MdekKeys.DATE_OF_CREATION);
		// TODO: check and repair further import data ?

		// the according catalog node to update, null if new node !
		ObjectNode existingNode = null;

		if (doSeparateImport) {
			// TODO: check if UUID exists, then create and set new UUID. Store UUID mapping in Map and use for all import objects (so relations stay the same) !
			// TODO: check if ORIG_ID exists, then delete ORG_ID ?
			
		} else {
			existingNode = determineObjectNode(objDoc, userUuid);

			// set mod_uuid and responsible_uuid. default is calling user.
			String modUuid = userUuid;
			String responsibleUuid = userUuid;
			if (existingNode != null) {
				// node found. take over mod_uuid and responsible_uuid FROM WORKING VERSION.
				T01Object existingObj = existingNode.getT01ObjectWork();
				modUuid = existingObj.getModUuid();
				responsibleUuid = existingObj.getResponsibleUuid();
			}
			beanToDocMapper.mapModUser(modUuid, objDoc, MappingQuantity.INITIAL_ENTITY);
			beanToDocMapper.mapResponsibleUser(responsibleUuid, objDoc, MappingQuantity.INITIAL_ENTITY);
		}


		// where to add import object
		ObjectNode parentNode = determineObjectParentNode(objDoc, existingNode, objImportNode, userUuid);

		// TODO: check if parent changed, then move object. Also move Object to Top ???

		// TODO: check additional fields -> store working version if problems
		// TODO: check relations and remove if not present
		
		// PUBLISH IMPORT OBJECT
		// ---------------------

		// create object tag for messages !
		String objTag = createObjectTag(objDoc.getString(MdekKeys.UUID),
				objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER),
				objDoc.getString(MdekKeys.PARENT_UUID));

		// message whether node exists or not ! 
		String newObjMsg = "EXISTING object ";
		if (existingNode == null) {
			newObjMsg = "NEW object ";
		}

		boolean storeWorkingVersion = true;		
		if (publishImmediately) {
			storeWorkingVersion = false;

			// TODO: check mandatory data -> store working version if problems

			// first check whether publishing is possible with according parent
			// NOTICE: determined parent can be null -> update of existing top object 
			if (parentNode != null && !objectService.hasPublishedVersion(parentNode)) {
				// parent not published -> store working version !
				updateImportJobInfoMessages("! " + objTag + "Parent not published, we store working version", userUuid);
				storeWorkingVersion = true;
				
			} else {
				// ok, we publish. On error store working version !
				String errorMsg = "! " + objTag + "Problems publishing, we store working version : ";
				try {
					// we DON'T force publication condition ! if error, we store working version !
					objectService.publishObject(objDoc, false, userUuid, false, true);
					updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
					updateImportJobInfoMessages(objTag + newObjMsg + "PUBLISHED !", userUuid);

				} catch (MdekException ex) {
					updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
					storeWorkingVersion = true;
					LOG.error(errorMsg, ex);
				} catch (Exception ex) {
					updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
					storeWorkingVersion = true;				
					LOG.error(errorMsg, ex);
				}
			}
		}

		// STORE WORKING COPY IMPORT OBJECT
		// --------------------------------

		if (storeWorkingVersion) {
			String errorMsg = "! " + objTag + "Problems storing working version : ";
			try {
				objectService.storeWorkingCopy(objDoc, userUuid, false, true);
				updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
				updateImportJobInfoMessages(objTag + newObjMsg + "stored as WORKING version", userUuid);

			} catch (MdekException ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMdekError(), userUuid);
				storeWorkingVersion = true;
				LOG.error(errorMsg, ex);
			} catch (Exception ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
				storeWorkingVersion = true;				
				LOG.error(errorMsg, ex);
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
	/** Logs given Exception in info of Import job IN DATABASE. */
	public void updateImportJobInfoException(Exception exceptionToLog, String userUuid) {
		// no log in memory, this one should be called when job has to be exited ...

		// log in job info in database
		jobHandler.updateJobInfoDBException(JobType.IMPORT, exceptionToLog, userUuid);
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

		updateImportJobInfoMessages("OBJECT Import Node = " + objImportNode.getObjUuid(), userUuid);
		updateImportJobInfoMessages("ADDRESS Import Node = " + addrImportNode.getAddrUuid(), userUuid);

		// all ok, we store data in running job info for later access !
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		runningJobInfo.put(KEY_OBJ_IMPORT_NODE, objImportNode);
		runningJobInfo.put(KEY_ADDR_IMPORT_NODE, addrImportNode);
		runningJobInfo.put(KEY_PUBLISH_IMMEDIATELY, publishImmediately);
		runningJobInfo.put(KEY_DO_SEPARATE_IMPORT, doSeparateImport);
	}
	
	private MdekException createImportException(String message) {
		return new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, message));
	}

	/** Creates a tag to be displayed as object "identifier". */
	private String createObjectTag(String objUuid, String origId, String parentUuid) {
		String tag = "Object";
		tag += " UUID:" + objUuid;
		tag += " ORIG_ID:" + origId;
		tag += " PARENT_UUID:" + parentUuid;
		tag += " >> ";

		return tag;
	}

	/**
	 * Determine the according catalog node to the import entity. NOTICE: manipulates importObjDoc !
	 * @param importObjDoc the object to import represented by its doc.
	 * 		Necessary changes (e.g. keep catalog uuid instead of import uuid) will be adapted in this doc.
	 * @param userUuid calling user
	 * @return the detected catalog node or null if new import entity
	 */
	private ObjectNode determineObjectNode(IngridDocument importObjDoc, String userUuid) {
		ObjectNode existingNode = null;

		String inObjUuid = importObjDoc.getString(MdekKeys.UUID);
		String inOrigId = importObjDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);
		String objTag = createObjectTag(inObjUuid, inOrigId, importObjDoc.getString(MdekKeys.PARENT_UUID));

		// UUID has highest priority, load via UUID
		if (inObjUuid != null) {
			existingNode = objectService.loadByUuid(inObjUuid, IdcEntityVersion.WORKING_VERSION);
			// we found UUID -> ignore ORIG_ID in doc (may differ) ! UUID has highest priority.
			if (existingNode != null) {
				importObjDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, existingNode.getT01ObjectWork().getOrgObjId());
			}
		}
		// if UUID not found load via ORIG_ID and keep existing UUID if found !
		if (existingNode == null &&
				inOrigId != null) {
			existingNode = objectService.loadByOrigId(inOrigId, IdcEntityVersion.WORKING_VERSION);
			if (existingNode != null) {
				// uuid of import doesn't match with uuid in catalog, WE KEEP UUID IN CATALOG !
				String existingUuid = existingNode.getObjUuid();
				importObjDoc.put(MdekKeys.UUID, existingUuid);
				updateImportJobInfoMessages("! " + objTag +
					"UUID not found, found ORIG_ID, we update existing object UUID:" + existingUuid, userUuid);
			}
		}
		// if no node found, check whether UUID not set, then create UUID
		// -> new "ArcGis Object" or object without any ID or ...
		if (existingNode == null &&
				inObjUuid == null) {
			// set new uuid in doc to be used afterwards !
			importObjDoc.put(MdekKeys.UUID, UuidGenerator.getInstance().generateUuid());
		}
		
		return existingNode;
	}

	/**
	 * Determine the "new" parent of the object to import.  NOTICE: manipulates importObjDoc !
	 * @param objDoc the object to import represented by its doc.
	 * 		The new parent uuid will also be set in this doc.
	 * @param existingNode the according catalog node to the import entity. Null IF NEW ENTITY !
	 * 		This one is determined separately and passed here, so we don't have to load twice !
	 * @param objectImportNode the import node for objects
	 * @param userUuid calling user
	 * @return the "new" parent node. NOTICE: can be NULL if existing object is top node !.
	 * 		This one is also set in importObjDoc. 
	 */
	private ObjectNode determineObjectParentNode(IngridDocument objDoc, ObjectNode existingNode, 
			ObjectNode objectImportNode,
			String userUuid) {
		// default "new" parent is import node
		ObjectNode newParentNode = objectImportNode;

		// create object tag for messages !
		String objTag = createObjectTag(objDoc.getString(MdekKeys.UUID),
				objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER),
				objDoc.getString(MdekKeys.PARENT_UUID));

		// fetch parent from import
		String importParentUuid = objDoc.getString(MdekKeys.PARENT_UUID);
		ObjectNode importParentNode = objectService.loadByUuid(importParentUuid, null);
		boolean importParentExists = (importParentNode != null);

		// check and set "new" parent dependent from existing object and import parent.
		if (existingNode != null) {
			// object exists.
			if (importParentUuid != null) {
				// import parent set.
				if (importParentExists) {
					// import parent exists. we set "new" parent from import.
					newParentNode = importParentNode;
				} else {
					// import parent does NOT exist. we keep "old" parent.
					updateImportJobInfoMessages("! " + objTag + "Parent not found, we keep former parent", userUuid);
					newParentNode = objectService.loadByUuid(existingNode.getFkObjUuid(), null);
				}
			} else {
				// import parent NOT set.  we keep "old" parent. NOTICE: if old parent is null we keep null (top node)
				newParentNode = objectService.loadByUuid(existingNode.getFkObjUuid(), null);
			}
		} else {
			// object does NOT exist.
			if (importParentUuid != null) {
				// import parent set.
				if (importParentExists) {
					// import parent exists. we set "new" parent from import.
					newParentNode = importParentNode;
				} else {
					// import parent does NOT exist. store under import node.
					updateImportJobInfoMessages("! " + objTag + "Parent not found, store underneath import node", userUuid);
					newParentNode = objectImportNode;
				}
			} else {
				// import parent NOT set (NEW TOP NODE). store under import node.
				updateImportJobInfoMessages("! " + objTag + "New top node, store underneath import node", userUuid);
				newParentNode = objectImportNode;
			}
		}

		String newParentUuid = null;
		// NOTICE: if existing node is top node, the parent node is NULL !
		if (newParentNode != null) {
			newParentUuid = newParentNode.getObjUuid();
		}
		objDoc.put(MdekKeys.PARENT_UUID, newParentUuid);

		return newParentNode;
	}
}
