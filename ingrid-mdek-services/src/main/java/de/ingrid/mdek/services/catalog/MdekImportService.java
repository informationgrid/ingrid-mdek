package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
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

	private MdekCatalogService catalogService;
	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekJobHandler jobHandler;

	private BeanToDocMapper beanToDocMapper;
	private DocToBeanMapper docToBeanMapper;

	/** Generic dao for class unspecific operations !!! */
	private IGenericDao<IEntity> dao;

	// static keys for accessing data stored in running job info !
	/** Value: ObjectNode */
	private final static String KEY_OBJ_IMPORT_NODE = "IMPORTSERVICE_OBJ_IMPORT_NODE";
	/** Value: AddressNode */
	private final static String KEY_ADDR_IMPORT_NODE = "IMPORTSERVICE_ADDR_IMPORT_NODE";
	/** Value: Boolean */
	private final static String KEY_PUBLISH_IMMEDIATELY = "IMPORTSERVICE_PUBLISH_IMMEDIATELY";
	/** Value: Boolean */
	private final static String KEY_DO_SEPARATE_IMPORT = "IMPORTSERVICE_DO_SEPARATE_IMPORT";
	/** Holds map with mapping of old to new UUID for existing entities when "separate import" 
	 * Value: HashMap */
	private final static String KEY_SEPARATE_IMPORT_UUID_MAP = "IMPORTSERVICE_SEPARATE_IMPORT_UUID_MAP";
	/** Holds map with list of object references for every processed object<br>
	 * Value: HashMap */
	private final static String KEY_OBJECT_REFERENCES_MAP = "IMPORTSERVICE_OBJECT_REFERENCES_MAP";


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
		catalogService = MdekCatalogService.getInstance(daoFactory);
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		jobHandler = MdekJobHandler.getInstance(daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);

		dao = daoFactory.getDao(IEntity.class);
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

		// process import doc, remove/fix wrong data from exporting catalog
		preprocessObjectDoc(objDoc, userUuid);

		boolean storeWorkingVersion = false;
		String objTag;
		String errorMsg;
		String newObjMsg = "NEW object ";

		if (doSeparateImport) {
			// process UUIDs, ORIG_IDs, PARENT_UUIDs so ALL objects will be created under import node ! 
			processObjectUuidsOnSeparateImport(objDoc, objImportNode.getObjUuid(), userUuid);

			// process all relations (to objects and addresses) !
			processRelationsOfObject(objDoc, userUuid);

		// TODO: check additional fields -> store working version if problems ?
		
			storeWorkingVersion = true;
			
		} else {
			// determine the according node in the catalog
			ObjectNode existingNode = determineObjectNode(objDoc, userUuid);

			// determine the parent (may differ from current parent)
			ObjectNode parentNode = determineObjectParentNode(objDoc, existingNode, objImportNode, userUuid);

			// process all relations (to objects and addresses) !
			processRelationsOfObject(objDoc, userUuid);

		// TODO: check additional fields -> store working version if problems
		

			// MOVE EXISTING OBJECT ?
			// ----------------------

			// create object tag for messages !
			objTag = createObjectTag(objDoc);

			// move existing object if valid (import structure has higher priority than existing structure).
			errorMsg = "! " + objTag + "Problems moving, we store working version : ";
			try {
				processObjectMove(objDoc, existingNode, parentNode, userUuid);

			} catch (Exception ex) {
				updateImportJobInfoMessages(errorMsg + ex, userUuid);
				storeWorkingVersion = true;
				LOG.error(errorMsg, ex);
			}

			// PUBLISH IMPORT OBJECT ?
			// -----------------------

			// create object tag for messages !
			objTag = createObjectTag(objDoc);

			// message whether node exists or not ! 
			if (existingNode != null) {
				newObjMsg = "EXISTING object ";
			}

			if (publishImmediately && !storeWorkingVersion) {

				// TODO: check mandatory data -> store working version if problems

				// if workflow enabled then ASSIGN TO QA else PUBLISH !
				if (catalogService.isWorkflowEnabled()) {
					// Workflow enabled -> ASSIGN TO QA ! On error store working version !

					errorMsg = "! " + objTag + "Problems assigning to QA, we store working version : ";
					try {
						objectService.assignObjectToQA(objDoc, userUuid, false, true);
						updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
						updateImportJobInfoMessages(objTag + newObjMsg + "ASSIGNED TO QA", userUuid);

					} catch (Exception ex) {
						updateImportJobInfoMessages(errorMsg + ex, userUuid);
						storeWorkingVersion = true;
						LOG.error(errorMsg, ex);
					}

				} else {
					// Workflow disabled -> PUBLISH !  On error store working version !

					// first check whether publishing is possible with according parent
					// NOTICE: determined parent can be null -> update of existing top object 
					if (parentNode != null && !objectService.hasPublishedVersion(parentNode)) {
						// parent not published -> store working version !
						updateImportJobInfoMessages("! " + objTag + "Parent not published, we store working version", userUuid);
						storeWorkingVersion = true;
						
					} else {
						// ok, we publish. On error store working version !
						errorMsg = "! " + objTag + "Problems publishing, we store working version : ";
						try {
							// we DON'T force publication condition ! if error, we store working version !
							objectService.publishObject(objDoc, false, userUuid, false, true);
							updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
							updateImportJobInfoMessages(objTag + newObjMsg + "PUBLISHED", userUuid);

						} catch (Exception ex) {
							updateImportJobInfoMessages(errorMsg + ex, userUuid);
							storeWorkingVersion = true;
							LOG.error(errorMsg, ex);
						}
					}
				}

			} else {
				storeWorkingVersion = true;			
			}
		}


		// STORE WORKING COPY IMPORT OBJECT ?
		// ----------------------------------

		// create object tag for messages !
		objTag = createObjectTag(objDoc);
		String objUuid = objDoc.getString(MdekKeys.UUID);

		if (storeWorkingVersion) {
			errorMsg = "! " + objTag + "Problems storing working version : ";
			try {
				objectService.storeWorkingCopy(objDoc, userUuid, false, true);
				updateImportJobInfo(IdcEntityType.OBJECT, numImported+1, totalNum, userUuid);
				updateImportJobInfoMessages(objTag + newObjMsg + "stored as WORKING version", userUuid);

			} catch (Exception ex) {
				updateImportJobInfoMessages(errorMsg + ex.getMessage(), userUuid);
				// object was NOT persisted, we also remove remembered obj references of this object.
				evictObjReferences(objUuid, userUuid);
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

		/*
		 * - Unter Importknoten werden die Objekte NIEMALS gepublished !
		 * - Alle neuen Entities wandern unter Importknoten, ES SEI DENN IHR PARENT EXISITIERT
		 *   - Alle neuen freien Adressen wandern unter Importknoten als "PERSON"
		 *   - Alle top Entities wandern unter den Importknoten
		 */


		// TODO: implemnt writeAddress
		
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

	/**
	 * AFTER Import of all Entities process the imported Object-Object relations ("Querverweise"),
	 * to guarantee no missing objects. Remove all relations to non existing entities or to entities
	 * not in same state (e.g. published objects can only reference published objects !)
	 * NOTICE: has to be called AFTER IMPORT of entities. Uses the relations stored in running job info ! 
	 * @param userUuid calling user
	 */
	public void postProcessRelationsOfImport(String userUuid) {
//		updateImportJobInfoMessages("\nSTART postprocessing of Object references", userUuid);

		// extract context
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		boolean doSeparateImport = (Boolean) runningJobInfo.get(KEY_DO_SEPARATE_IMPORT);

		// extract map containing ALL object refs !
		HashMap<String, List<IngridDocument>> allRefsMap = getObjectReferencesMap(userUuid);
		// extract map containing mapping of old to new UUIDs (when separate import) !
		HashMap<String, String> uuidMappingMap = getUuidMappingMap(userUuid);

		// process all contained objects ("source" objects)
		for (Iterator<String> i = allRefsMap.keySet().iterator(); i.hasNext();) {
			String objUuid = i.next();
			// create object tag for messages !
			String objTag = createObjectTag(objUuid);
			
			// load object instance and add all references to existing objects.
			// remove reference to non existent objects or to objects not in same state !
			ObjectNode objNode = objectService.loadByUuid(objUuid, IdcEntityVersion.WORKING_VERSION);
			if (objNode != null) {
				boolean objIsPublished = !objectService.hasWorkingCopy(objNode);

				// extract object refs from map and remove "corrupt" ones
				List<IngridDocument> objRefs = allRefsMap.get(objUuid);
				for (Iterator<IngridDocument> j = objRefs.iterator(); j.hasNext();) {
					IngridDocument objRef = j.next();
					String objRefUuid = objRef.getString(MdekKeys.UUID);
					String refType = objRef.getString(MdekKeys.RELATION_TYPE_NAME);

					// if separate import executed check whether UUID was mapped and replace !
					if (doSeparateImport) {
						if (uuidMappingMap.containsKey(objRefUuid)) {
							objRefUuid = uuidMappingMap.get(objRefUuid);
							// also document !!!!!! is base for update !!!
							objRef.put(MdekKeys.UUID, objRefUuid);
						}
					}

					// check "target" object and relation.
					ObjectNode objRefNode = objectService.loadByUuid(objRefUuid, null);
					if (objRefNode == null) {
						// remove if not found !
						updateImportJobInfoMessages("! " + objTag +
								"REMOVED reference of type \"" + refType + "\" to non existing object " + objRefUuid, userUuid);
						j.remove();
					} else {
						// remove if not same state !
						boolean objRefHasPublishedVersion = objectService.hasPublishedVersion(objRefNode);
						if (objIsPublished && !objRefHasPublishedVersion) {
							updateImportJobInfoMessages("! " + objTag +
									"REMOVED reference of type \"" + refType + "\" to NON PUBLISHED object " + objRefUuid, userUuid);
							j.remove();
						}
					}
				}

				// add remaining ones to object and store
				T01Object obj = objNode.getT01ObjectWork();
				docToBeanMapper.updateObjectReferences(objRefs, obj);
				dao.makePersistent(obj);
				
			} else {
				LOG.warn("source object " + objUuid + " not found for postprocessing of object references !");
			}
		}
	}

	/** Preprocess import doc, remove/fix wrong data from exporting catalog.
	 * @param importObjDoc the object to import represented by its doc. Will be manipulated !
	 * @param userUuid calling user
	 * @return the preprocessed importObjDoc (same instance as passed one !) 
	 */
	private IngridDocument preprocessObjectDoc(IngridDocument importObjDoc, String userUuid) {
		if (importObjDoc.get(MdekKeys.UUID) != null && importObjDoc.getString(MdekKeys.UUID).trim().length() == 0) {
			importObjDoc.remove(MdekKeys.UUID);			
		}
		if (importObjDoc.get(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER) != null && importObjDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER).trim().length() == 0) {
			importObjDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);			
		}
		if (importObjDoc.get(MdekKeys.PARENT_UUID) != null && importObjDoc.getString(MdekKeys.PARENT_UUID).trim().length() == 0) {
			importObjDoc.remove(MdekKeys.PARENT_UUID);			
		}
		// remove WRONG data (from different catalog ?)
		importObjDoc.remove(MdekKeys.CATALOGUE_IDENTIFIER);
		importObjDoc.remove(MdekKeys.DATE_OF_LAST_MODIFICATION);
		importObjDoc.remove(MdekKeys.DATE_OF_CREATION);

		// default: calling user is mod user and responsible !
		beanToDocMapper.mapModUser(userUuid, importObjDoc, MappingQuantity.INITIAL_ENTITY);
		beanToDocMapper.mapResponsibleUser(userUuid, importObjDoc, MappingQuantity.INITIAL_ENTITY);

		// TODO: check and repair further import data ?

		return importObjDoc;
	}

	/**
	 * Processes the passed doc, so that the node will be created under import node under according parent !
	 * If UUID exists then a new one will be set to create a new object ! Then the ORIG_ID is removed if not unique.
	 * The new parent UUID is determined from already mapped objects.
	 * @param objDoc the object to import represented by its doc. Will be manipulated !
	 * @param objImportUuid the UUID of the import node for objects
	 * @param userUuid calling user
	 * @return the processed objDoc (same instance as passed one !) 
	 */
	private IngridDocument processObjectUuidsOnSeparateImport(IngridDocument objDoc, String objImportUuid,
			String userUuid) {
		// first extract map of mapped uuids !
		HashMap<String, String> uuidMappingMap = getUuidMappingMap(userUuid);

		String inUuid = objDoc.getString(MdekKeys.UUID);
		String inOrigId = objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);
		String inParentUuid = objDoc.getString(MdekKeys.PARENT_UUID);

		// process UUID

		// check whether object exists. if so create new UUID to store new object and remember mapping !
		String newUuid = inUuid;
		if (inUuid != null) {
			// first check map (maybe object already mapped ? then included multiple times in import ?)
			if (uuidMappingMap.containsKey(inUuid)) {
				newUuid = uuidMappingMap.get(inUuid);
			} else if (objectService.loadByUuid(inUuid, null) != null) {
				// existing object, new UUID will be created !
				newUuid = null;
			}
		}
		// create new uuid to create new object !
		if (newUuid == null) {
			newUuid = UuidGenerator.getInstance().generateUuid();
		}
		objDoc.put(MdekKeys.UUID, newUuid);
		uuidMappingMap.put(inUuid, newUuid);
		
		// process ORIG_ID

		// check orig id and remove if not unique !
		String newOrigId = inOrigId;
		if (inOrigId != null) {
			ObjectNode oNodeWithSameOrigId = objectService.loadByOrigId(inOrigId,  null);
			if (oNodeWithSameOrigId != null) {
				// just to be sure: could be the same object (when included multiple times !? )
				if (!newUuid.equals(oNodeWithSameOrigId.getObjUuid())) {
					// same orig id set in other object, we remove this one
					newOrigId = null;
					objDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);				
				}				
			}
		}
		
		// process PARENT UUID

		String newParentUuid = inParentUuid;
		if (inParentUuid != null) {
			// get mapped UUID returns null if not mapped yet
			newParentUuid = uuidMappingMap.get(inParentUuid);
		}
		// if not mapped yet store under import node
		if (newParentUuid == null) {
			newParentUuid = objImportUuid;
		}
		objDoc.put(MdekKeys.PARENT_UUID, newParentUuid);

		// log mapping if changed
		if (!MdekUtils.isEqual(inUuid, newUuid) ||
				!MdekUtils.isEqual(inOrigId, newOrigId) ||
				!MdekUtils.isEqual(inParentUuid, newParentUuid)) {
			String objTag = createObjectTag(inUuid, inOrigId, inParentUuid);
			updateImportJobInfoMessages(objTag +
					"MAPPED to UUID:" + newUuid + " ORIG_ID:" + newOrigId + " PARENT_UUID:" + newParentUuid, userUuid);
		}

		return objDoc;		
	}

	/**
	 * Determine the according catalog node to the import entity.
	 * Also takes over mod-user and responsible-user if an existing node was found !
	 * NOTICE: manipulates importObjDoc !
	 * @param importObjDoc the object to import represented by its doc.
	 * 		Necessary changes (e.g. keep existing uuid instead of import uuid) will be adapted in this doc.
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

		// set mod_uuid and responsible_uuid from existing object.
		if (existingNode != null) {
			// take over FROM WORKING VERSION.
			T01Object existingObj = existingNode.getT01ObjectWork();
			String modUuid = existingObj.getModUuid();
			String respUuid = existingObj.getResponsibleUuid();
			// just to be sure: we take over only if set ??? check, at the moment we keep current object state !
//			modUuid = (modUuid == null) ? userUuid : modUuid;
//			respUuid = (respUuid == null) ? userUuid : respUuid;
			beanToDocMapper.mapModUser(modUuid, importObjDoc, MappingQuantity.INITIAL_ENTITY);
			beanToDocMapper.mapResponsibleUser(respUuid, importObjDoc, MappingQuantity.INITIAL_ENTITY);
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
		String objTag = createObjectTag(objDoc);

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

	/**
	 * Process all relations of the given objects meaning:
	 * - object-object relations are removed and stored in running job info to be processed after import of all entities !
	 * - relation to address is checked immediately and removed if address not present !
	 * @param objDoc the object to import represented by its doc (containing all relations).
	 * 		NOTICE: will be manipulated (relations to objs removed !)
	 * @param userUuid calling user
	 */
	private void processRelationsOfObject(IngridDocument objDoc, String userUuid) {
		// create object tag for messages !
		String objTag = createObjectTag(objDoc);
		String objUuid = objDoc.getString(MdekKeys.UUID);

		// ADDRESS REFERENCES

		// We process list instance in doc and remove non existing refs !
		List<IngridDocument> addrRefs = (List) objDoc.get(MdekKeys.ADR_REFERENCES_TO);
		for (Iterator i = addrRefs.iterator(); i.hasNext();) {
			IngridDocument addrRef = (IngridDocument) i.next();
			String addrRefUuid = addrRef.getString(MdekKeys.UUID);
			if (addressService.loadByUuid(addrRefUuid, null) == null) {
				String refType = addrRef.getString(MdekKeys.RELATION_TYPE_NAME);
				updateImportJobInfoMessages("! " + objTag +
					"REMOVED reference of type \"" + refType + "\" to non existing address " + addrRefUuid, userUuid);
				i.remove();
			}
		}

		// OBJECT REFERENCES

		// remove all relations, but remember them in running job info to be
		// processed after import of entities ! We process list instance in doc !
		List<IngridDocument> objRefs = (List) objDoc.get(MdekKeys.OBJ_REFERENCES_TO);
		rememberObjReferences(objUuid, objRefs, userUuid);
		objDoc.remove(MdekKeys.OBJ_REFERENCES_TO);
	}
	
	/** Store the given object-object relations in running job info for later access !
	 * @param fromUuid source object
	 * @param toObjRefs list of target objects and relation details as stored in import doc !
	 * @param userUuid calling user
	 */
	private void rememberObjReferences(String fromUuid, List<IngridDocument> toObjRefs,
			String userUuid) {
		if (toObjRefs == null) {
			return;
		}

		// extract map containing ALL object refs !
		HashMap<String, List<IngridDocument>> allRefsMap = getObjectReferencesMap(userUuid);
		
		// extract refs of our object and add ref
		List<IngridDocument> objRefs = allRefsMap.get(fromUuid);
		if (objRefs == null) {
			objRefs = new ArrayList<IngridDocument>();
			allRefsMap.put(fromUuid, objRefs);
		}
		objRefs.addAll(toObjRefs);
	}

	/** Removes all remembered object references of the given object from running job info
	 * to avoid post processing. */
	private void evictObjReferences(String fromUuid, String userUuid) {
		// extract map containing ALL object refs !
		HashMap<String, List<IngridDocument>> allRefsMap = getObjectReferencesMap(userUuid);

		// remove references of given obj
		allRefsMap.remove(fromUuid);
	}
	
	/** Extract map from running job info containing object references of all processed objects.
	 * @param userUuid calling user
	 * @return the map. NEVER null.
	 */
	private HashMap<String, List<IngridDocument>> getObjectReferencesMap(String userUuid) {
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		HashMap<String, List<IngridDocument>> allRefsMap =
			(HashMap<String, List<IngridDocument>>) runningJobInfo.get(KEY_OBJECT_REFERENCES_MAP);
		if (allRefsMap == null) {
			allRefsMap = new HashMap<String, List<IngridDocument>>();
			runningJobInfo.put(KEY_OBJECT_REFERENCES_MAP, allRefsMap);
		}
		
		return allRefsMap;
	}

	/** Extract map from running job info containing mapping of old to new UUIDs (when executing separate import !).
	 * @param userUuid calling user
	 * @return the map. NEVER null.
	 */
	private HashMap<String, String> getUuidMappingMap(String userUuid) {
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		HashMap<String, String> uuidMap = (HashMap<String, String>) runningJobInfo.get(KEY_SEPARATE_IMPORT_UUID_MAP);
		if (uuidMap == null) {
			uuidMap = new HashMap<String, String>();
			runningJobInfo.put(KEY_SEPARATE_IMPORT_UUID_MAP, uuidMap);
		}

		return uuidMap;
	}

	/**
	 * Move existing node to "new" parent node if parent in IMPORT differs and exists !
	 * @param objDoc the object to import represented by its doc.
	 * 		NOTICE: already processed ! data MUST fit to passed nodes ! 
	 * @param existingNode existing node like determined before. IF NULL non existent !
	 * @param parentNode new parent node like determined before. IF NULL then has to be existing top node.
	 * @param userUuid calling user
	 */
	private void processObjectMove(IngridDocument objDoc,
			ObjectNode existingNode, ObjectNode parentNode, String userUuid)
	throws Exception {

		if (existingNode == null) {
			return;
		}

		String objTag = createObjectTag(objDoc);
		String currentParentUuid = existingNode.getFkObjUuid();
		String newParentUuid = null;
		if (parentNode != null) {
			newParentUuid = parentNode.getObjUuid();
		}

		if (newParentUuid == null) {
			// "new" position is top node -> only possible if existing node is top node !
			if (currentParentUuid != null) {
				// ??? we can't move to top due to import ! should not happen !!! Log !!!
				throw createImportException("! " + objTag + "Can't move to TOP because of Import, we keep position");			
			}
		} else if (!newParentUuid.equals(currentParentUuid)) {
			// wemove :) !
			try {
				objectService.moveObject(existingNode.getObjUuid(), newParentUuid, false, userUuid, false);				
				updateImportJobInfoMessages(objTag + "Moved to new parent, former parent: " + currentParentUuid, userUuid);
			} catch (Exception ex) {
				// problems ! we set parent in doc to current parent to guarantee correct parent !
				objDoc.put(MdekKeys.PARENT_UUID, existingNode.getFkObjUuid());
				throw ex;
			}
		}
	}

	private MdekException createImportException(String message) {
		return new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, message));
	}

	/** Creates object tag from object doc ! To be displayed as object "identifier". */
	private String createObjectTag(IngridDocument objDoc) {
		return createObjectTag(objDoc.getString(MdekKeys.UUID),
				objDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER),
				objDoc.getString(MdekKeys.PARENT_UUID));
	}

	/** Creates object tag to be displayed as object "identifier". */
	private String createObjectTag(String objUuid, String origId, String parentUuid) {
		String tag = "Object";
		tag += " UUID:" + objUuid;
		tag += " ORIG_ID:" + origId;
		tag += " PARENT_UUID:" + parentUuid;
		tag += " >> ";

		return tag;
	}
	/** Creates object tag simply from UUID ! To be displayed as object "identifier". */
	private String createObjectTag(String objUuid) {
		String tag = "Object";
		tag += " UUID:" + objUuid;
		tag += " >> ";

		return tag;
	}
}
