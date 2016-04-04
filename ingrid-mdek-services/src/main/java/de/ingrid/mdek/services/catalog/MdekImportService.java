/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.catalog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.EntityHelper;
import de.ingrid.mdek.services.utils.MdekIdcUserHandler;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
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
	private MdekIdcUserHandler userHandler;
	private MdekPermissionHandler permissionHandler;

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
	/** Should the import be separated under one node ! 
	 * Value: Boolean */
	private final static String KEY_DO_SEPARATE_IMPORT = "IMPORTSERVICE_DO_SEPARATE_IMPORT";
	/** On separate import, should an existing node (uuid) be copied to a new node (uuid) or not ! 
	 * Value: Boolean */
	private final static String KEY_COPY_NODE_IF_PRESENT = "IMPORTSERVICE_COPY_NODE_IF_PRESENT";
	/** Holds all UUIDs which should be reported to frontend, because node already exists and should NOT be copied to separate import branch ! 
	 * Value: Set */
	private final static String KEY_EXISTING_UUIDS_TO_REPORT = "IMPORTSERVICE_EXISTING_UUIDS_TO_REPORT";
	/** Holds map with mapping of old to new UUID for existing entities when "separate import" 
	 * Value: HashMap */
	private final static String KEY_UUID_MAPPING_MAP = "IMPORTSERVICE_UUID_MAPPING_MAP";
	/** Holds map with list of object references for every processed object<br>
	 * Value: HashMap */
	private final static String KEY_OBJECT_REFERENCES_MAP = "IMPORTSERVICE_OBJECT_REFERENCES_MAP";

	// static temporary keys for transferring arbitrary stuff between methods
	/** Store working Copy ?<br>
	 * Value: Boolean */
	private final static String TMP_STORE_WORKING_VERSION = "TMP_STORE_WORKING_VERSION";
	/** Found node in catalog (e.g. to update from Import or "new" parent node)<br>
	 * Value: IEntity (Object-/AddressNode) */
	private final static String TMP_FOUND_NODE = "TMP_FOUND_NODE";
	/** Uuid or name of entity (if uuid is null)<br>
	 * Value: String */
	private final static String TMP_ENTITY_IDENTIFIER = "TMP_ENTITY_IDENTIFIER";
	/** true=responsible user of import file replaced with calling user, false=responsible user of import file found and kept !<br>
	 * Value: Boolean */
	private final static String TMP_RESPONSIBLE_USER_NOT_FOUND = "TMP_RESPONSIBLE_USER_NOT_FOUND";
	/** true=this instance is the first instance of an entity to process, false=this instance is another instance of an already processed entity<br>
	 * Value: Boolean */
	private final static String TMP_FIRST_INSTANCE = "TMP_FIRST_INSTANCE";
	
	// static strings for import messages !
	private final static String MSG_WARN = "! ";
	
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
		userHandler = MdekIdcUserHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);

		dao = daoFactory.getDao(IEntity.class);
	}

	// ----------------------------------- IImporterCallback START ------------------------------------

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeObject(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeObject(List<IngridDocument> objDocs, String userUuid) {
		// array may have multiple instances, process from end to begin, from published to working instance.
		// If published instance causes problems then it is saved as working version which is overwritten by working instance then !
		int numDocs = objDocs.size();
		for (int i=numDocs-1; i >= 0; i--) {
			IngridDocument objDoc = objDocs.get(i);
			objDoc.put(TMP_FIRST_INSTANCE, true);
			if (i < numDocs -1) {
				// multiple instances and this is NOT the first one processed. We indicate this !
				objDoc.put(TMP_FIRST_INSTANCE, false);
			}

			try {
				writeEntity(IdcEntityType.OBJECT, objDoc, userUuid);
			} catch (MdekException mdekExc) {
				if (mdekExc.containsError(MdekErrorType.ENTITY_ALREADY_EXISTS)) {
					// report !
					String uuid = mdekExc.getMdekError().getErrorMessage();
					getExistingUuidsToReport(userUuid).add(uuid);
					break;

				} else {
					throw mdekExc;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeAddress(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeAddress(List<IngridDocument> addrDocs, String userUuid) {
		// array may have multiple instances, process from end to begin, from published to working instance.
		// If published instance causes problems then it is saved as working version which is overwritten by working instance then !
		int numDocs = addrDocs.size();
		for (int i=numDocs-1; i >= 0; i--) {
			IngridDocument addrDoc = addrDocs.get(i);
			addrDoc.put(TMP_FIRST_INSTANCE, true);
			if (i < numDocs -1) {
				// multiple instances and this is NOT the first one processed. We indicate this !
				addrDoc.put(TMP_FIRST_INSTANCE, false);
			}

			try {
				writeEntity(IdcEntityType.ADDRESS, addrDoc, userUuid);
			} catch (MdekException mdekExc) {
				if (mdekExc.containsError(MdekErrorType.ENTITY_ALREADY_EXISTS)) {
					// just skip, we do NOT process
					// "Existierende Adressen werden NICHT überschrieben (sie werden beim Import ignoriert)."
					// see https://dev2.wemove.com/confluence/display/INGRID33/Export-Import+3.3+-+Implementierung
					updateImportJobInfoFrontendMessages(
						"Import-Addresse \"" + addrDoc.get(TMP_ENTITY_IDENTIFIER) + "\" schon vorhanden, wird ignoriert.", userUuid);
					break;

				} else {
					throw mdekExc;
				}
			}
		}
	}

	public void writeEntity(IdcEntityType whichType, IngridDocument inDoc, String userUuid) {
		// extract context
		HashMap<Object, Object> runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		boolean publishImmediately = (Boolean) runningJobInfo.get(KEY_PUBLISH_IMMEDIATELY);
		boolean doSeparateImport = (Boolean) runningJobInfo.get(KEY_DO_SEPARATE_IMPORT);
		boolean copyNodeIfPresent = (Boolean) runningJobInfo.get(KEY_COPY_NODE_IF_PRESENT);
		int numImportedObjects = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS);
		int numImportedAddresses = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES);
		int totalNumObjects = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS);
		int totalNumAddresses = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES);
		boolean errorOnExistingUuid = runningJobInfo.containsKey( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID ) ? (boolean)runningJobInfo.get( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID) : false;
		
		IEntity importNode = null;
		if (whichType == IdcEntityType.OBJECT) {
			importNode = (IEntity) runningJobInfo.get(KEY_OBJ_IMPORT_NODE);
			if (errorOnExistingUuid) {
			    ObjectNode objImportNode = objectService.loadByOrigId( inDoc.getString( MdekKeys.ORIGINAL_CONTROL_IDENTIFIER ), IdcEntityVersion.WORKING_VERSION);
			    if (objImportNode != null) {
			        throw createImportException("Object already exists with Orig-UUID: " + inDoc.getString( MdekKeys.ORIGINAL_CONTROL_IDENTIFIER ));
			    }
			}
		} else if (whichType == IdcEntityType.ADDRESS) {
			importNode = (IEntity) runningJobInfo.get(KEY_ADDR_IMPORT_NODE);
		}

		// process import doc, remove/fix wrong data from exporting catalog
		preprocessDoc(whichType, inDoc, userUuid);

		boolean storeWorkingVersion = false;
		// update existing node or create new node ! this one holds the found node !
		IEntity existingNode = null;

		if (doSeparateImport) {
			// process stuff, so ALL entities will be created under import node, check validity ...
			HashMap retMap = processUuidsOnSeparateImport(whichType, copyNodeIfPresent,
				inDoc, EntityHelper.getUuidFromNode(whichType, importNode), userUuid);
			existingNode = (IEntity) retMap.get(TMP_FOUND_NODE);

			// entity type specific stuff
			if (whichType == IdcEntityType.OBJECT) {
				// process all relations (to objects and addresses) !
				processRelationsOfObject(inDoc, userUuid);

				// verify whether additional fields definitions match
//				processAdditionalFields(inDoc, userUuid);

			} else if (whichType == IdcEntityType.ADDRESS) {
				// transform FREE address to PERSON UNDER IMPORT NODE !
				processFreeAddressToPerson(inDoc, EntityHelper.getUuidFromNode(whichType, importNode), userUuid);
			}
			
		} else {
			// determine the according node in the catalog
			HashMap retMap = determineNodeInCatalog(whichType, inDoc, userUuid);
			existingNode = (IEntity) retMap.get(TMP_FOUND_NODE);
			if ((Boolean) retMap.get(TMP_STORE_WORKING_VERSION)) {
				storeWorkingVersion = true;
			}

			// determine the parent (may differ from current parent)
			retMap = determineParentNodeInCatalog(whichType, inDoc, existingNode, importNode, userUuid);
			IEntity parentNode = (IEntity) retMap.get(TMP_FOUND_NODE);
			if ((Boolean) retMap.get(TMP_STORE_WORKING_VERSION)) {
				storeWorkingVersion = true;
			}

			// entity type specific stuff
			if (whichType == IdcEntityType.OBJECT) {
				// process all relations (to objects and addresses) !
				processRelationsOfObject(inDoc, userUuid);

				// verify whether additional fields definitions match
//				if (!processAdditionalFields(inDoc, userUuid)) {
//					storeWorkingVersion = true;				
//				}
			}

			// MOVE EXISTING ENTITY ?
			// ----------------------

			// move existing entity if valid (import structure has higher priority than existing structure).
			try {
				processMove(whichType, inDoc, existingNode, parentNode, userUuid);

			} catch (Exception ex) {
				storeWorkingVersion = true;
			}
		}
		
		// SAVE ENTITY
		// -----------
		
		// first check whether published or working version set in instance !
		WorkState instanceVersion = EnumUtil.mapDatabaseToEnumConst(WorkState.class, inDoc.get(MdekKeys.WORK_STATE));
		if (instanceVersion == null) {
			// Import ArcGis OR CSW: no version set !
			// we set version according to chosen settings
			if (publishImmediately) {
				instanceVersion = WorkState.VEROEFFENTLICHT;
			} else {
				instanceVersion = WorkState.IN_BEARBEITUNG;
			}
		}

		// PUBLISH ?
		// ----------------
		boolean isPublishedInstance = (instanceVersion == WorkState.VEROEFFENTLICHT);

		boolean doPublish =
				isPublishedInstance &&
				!storeWorkingVersion &&
				checkMandatoryData(whichType, inDoc, userUuid);

		if (doPublish) {

			// NOTICE: hasQAPermission always returns true if workflow disabled !
			if (permissionHandler.hasQAPermission(userUuid)) {
				// Workflow disabled OR has QA Permission -> PUBLISH !  On error store working version !
				try {
					if(whichType == IdcEntityType.OBJECT)
						processPublish(whichType, inDoc, existingNode, numImportedObjects, totalNumObjects, userUuid);
					else
						processPublish(whichType, inDoc, existingNode, numImportedAddresses, totalNumAddresses, userUuid);
				
				} catch (Exception ex) {
					storeWorkingVersion = true;
				}
				
			} else {
				// Workflow enabled && NO QA PERMISSION -> ASSIGN TO QA ! On error store working version !
				try {
					if(whichType == IdcEntityType.OBJECT)
						processAssignToQA(whichType, inDoc, existingNode, numImportedObjects, totalNumObjects, userUuid);
					else
						processAssignToQA(whichType, inDoc, existingNode, numImportedAddresses, totalNumAddresses, userUuid);
				
				} catch (Exception ex) {
					storeWorkingVersion = true;
				}
			}

		} else {
			storeWorkingVersion = true;
		}

		// STORE WORKING COPY ?
		// --------------------
		if (storeWorkingVersion) {
			if(whichType == IdcEntityType.OBJECT)
				processStoreWorkingCopy(whichType, inDoc, existingNode, numImportedObjects, totalNumObjects, isPublishedInstance, userUuid);
			else
				processStoreWorkingCopy(whichType, inDoc, existingNode, numImportedAddresses, totalNumAddresses, isPublishedInstance, userUuid);
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeImportInfo(de.ingrid.mdek.MdekUtils.IdcEntityType, int, int, java.lang.String)
	 */
	public void writeImportInfo(IdcEntityType whichType, int numImported, int totalNum,
			String userUuid) {
		updateImportJobInfo(whichType, numImported, totalNum, userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeImportInfoMessage(java.lang.String, java.lang.String)
	 */
	public void writeImportInfoMessage(String message, String userUuid) {
		updateImportJobInfoMessages(message, userUuid);
	}

	// ----------------------------------- IImporterCallback END ------------------------------------

	/** "logs" Start-Info of Import job IN MEMORY and IN DATABASE */
	public void startImportJobInfo(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.IMPORT, 0, 0, false);
		// add default values !
		runningJobInfo.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS, 0);
		runningJobInfo.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS, 0);
		runningJobInfo.put(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES, 0);
		runningJobInfo.put(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES, 0);
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
				jobHandler.createRunningJobDescription(JobType.IMPORT, whichType.getDbValue(), numImported, totalNum, false));

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
	/** Add new Frontend Message to info of Import job IN MEMORY. */
	public void updateImportJobInfoFrontendMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobFrontendMessages(userUuid, newMessage);
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
			boolean publishImmediately,
			boolean doSeparateImport,
			boolean copyNodeIfPresent,
			String userUuid)
	throws MdekException {
		if (defaultObjectParentUuid == null) {
			throw createImportException("Top Node for Import of Objects not set.");
		}
		if (defaultAddrParentUuid == null) {
			throw createImportException("Top Node for Import of Addresses not set.");
		}

		// fetch and check nodes, ONLY WORKING VERSION !

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
		if (!AddressType.INSTITUTION.getDbValue().equals(addrImport.getAdrType())) {
			throw createImportException("Node for Import of Addresses is NO Institution.");
		}

		// if all should be imported underneath import node, then FORCED publishing not possible !
		// NO, NOT ANYMORE ! Allow it now :)
/*
		if (doSeparateImport && publishImmediately) {
			throw createImportException("Forced publishing underneath import nodes not possible.");
		}
*/

		updateImportJobInfoMessages("OBJECT Import Node = " + objImportNode.getObjUuid(), userUuid);
		updateImportJobInfoMessages("ADDRESS Import Node = " + addrImportNode.getAddrUuid(), userUuid);

		// all ok, we store data in running job info for later access !
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		runningJobInfo.put(KEY_OBJ_IMPORT_NODE, objImportNode);
		runningJobInfo.put(KEY_ADDR_IMPORT_NODE, addrImportNode);
		runningJobInfo.put(KEY_PUBLISH_IMMEDIATELY, publishImmediately);
		runningJobInfo.put(KEY_DO_SEPARATE_IMPORT, doSeparateImport);
		runningJobInfo.put(KEY_COPY_NODE_IF_PRESENT, copyNodeIfPresent);
	}

	/** Check state after import of entities. May throw controlled exception if "controlled problem" occured !
	 * @param userUuid the calling user for accessing runningJobInfo !
	 * @throws MdekException IMPORT_OBJECTS_ALREADY_EXIST
	 */
	public void checkImportEntities(String userUuid)
			throws MdekException {
		// check whether existing UUIDs have to be reported !
		Set<String> existingUuids = getExistingUuidsToReport(userUuid);
		if (existingUuids.size() > 0) {
			IngridDocument errInfoDoc = new IngridDocument();
			for (String existingUuid : existingUuids) {
				objectService.setupErrorInfoObj(errInfoDoc, existingUuid);
			}
			throw new MdekException(new MdekError(MdekErrorType.IMPORT_OBJECTS_ALREADY_EXIST, errInfoDoc));				
		}
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
			String objTag = createEntityTag(IdcEntityType.OBJECT, objUuid);
			
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
						updateImportJobInfoMessages(MSG_WARN + objTag +
							"REMOVED object reference of type \"" + refType + "\" to non existing object " + objRefUuid, userUuid);
						updateImportJobInfoFrontendMessages(
							"Objekt Referenz vom Typ \"" + refType + "\" zu nicht existierendem Objekt \"" + objRefUuid +
							"\" entfernt von Import-Objekt \"" + objUuid + "\".", userUuid);
						j.remove();
					} else {
						// remove if not same state !
						boolean objRefHasPublishedVersion = objectService.hasPublishedVersion(objRefNode);
						if (objIsPublished && !objRefHasPublishedVersion) {
							updateImportJobInfoMessages(MSG_WARN + objTag +
									"REMOVED object reference of type \"" + refType + "\" to NON PUBLISHED object " + objRefUuid, userUuid);
							updateImportJobInfoFrontendMessages(
									"Objekt Referenz vom Typ \"" + refType + "\" zu nicht veröffentlichtem Objekt \"" + objRefUuid +
									"\" entfernt von Import-Objekt \"" + objUuid + "\".", userUuid);
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
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc. Will be manipulated !
	 * @param callingUserUuid calling user
	 * @return the preprocessed inDoc (same instance as passed one !) 
	 */
	private IngridDocument preprocessDoc(IdcEntityType whichType,
			IngridDocument inDoc, String callingUserUuid) {
		if (inDoc.containsKey(MdekKeys.UUID) && !MdekUtils.hasContent(inDoc.getString(MdekKeys.UUID))) {
			inDoc.remove(MdekKeys.UUID);
		}
		if (inDoc.containsKey(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER) && !MdekUtils.hasContent(inDoc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER))) {
			inDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);			
		}
		if (inDoc.containsKey(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER) && !MdekUtils.hasContent(inDoc.getString(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER))) {
			inDoc.remove(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER);			
		}
		if (inDoc.containsKey(MdekKeys.PARENT_UUID) && !MdekUtils.hasContent(inDoc.getString(MdekKeys.PARENT_UUID))) {
			inDoc.remove(MdekKeys.PARENT_UUID);			
		}
		// remove WRONG data (may be from different catalog)
		inDoc.remove(MdekKeys.CATALOGUE_IDENTIFIER);

		// Keep Dates from Import File -> "Der "Zeitpunkt der letzten Bearbeitung" wird aus dem Import übernommen"
		// see https://dev2.wemove.com/confluence/display/INGRID33/Export-Import+3.3+-+Implementierung
//		inDoc.remove(MdekKeys.DATE_OF_LAST_MODIFICATION);
//		inDoc.remove(MdekKeys.DATE_OF_CREATION);

		// Keep Users from Import File if present, if not set calling User.
		// see https://dev2.wemove.com/confluence/display/INGRID33/Export-Import+3.3+-+Implementierung

		// last modifying user
		String userUuid = docToBeanMapper.extractModUserUuid(inDoc);
		if (!userHandler.userExists(userUuid)) {
			beanToDocMapper.mapModUser(callingUserUuid, inDoc, MappingQuantity.INITIAL_ENTITY);			
		}
		
		// responsible user. Also set flag indicating change for later processing !
		userUuid = docToBeanMapper.extractResponsibleUserUuid(inDoc);
		if (!userHandler.userExists(userUuid)) {
			inDoc.put(TMP_RESPONSIBLE_USER_NOT_FOUND, true);
			beanToDocMapper.mapResponsibleUser(callingUserUuid, inDoc, MappingQuantity.INITIAL_ENTITY);
		} else {
			inDoc.put(TMP_RESPONSIBLE_USER_NOT_FOUND, false);
		}

		inDoc.put(TMP_ENTITY_IDENTIFIER, EntityHelper.getEntityIdentifierFromDoc(whichType, inDoc));

		return inDoc;
	}

	/**
	 * Check whether all mandatory data is set for publishing.
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc.
	 * @param userUuid calling user
	 * @return true=all data set, can be published<br>
	 * 		false=data is missing, publishing not possible
	 */
	private boolean checkMandatoryData(IdcEntityType whichType,
			IngridDocument inDoc, String userUuid) {
		StringBuilder missingFields = new StringBuilder();
		String separator = ", ";

		// create tag for messages !
		String tag = createEntityTag(whichType, inDoc);

		if (EnumUtil.mapDatabaseToEnumConst(ObjectType.class, inDoc.get(MdekKeys.CLASS)) == null) {
			MdekUtils.appendWithSeparator(missingFields, separator, MdekKeys.CLASS);
		}
		if (docToBeanMapper.extractResponsibleUserUuid(inDoc) == null) {
			MdekUtils.appendWithSeparator(missingFields, separator, MdekKeys.RESPONSIBLE_USER);
		}

		if (whichType == IdcEntityType.OBJECT) {
			if (!MdekUtils.hasContent(inDoc.getString(MdekKeys.TITLE))) {
				MdekUtils.appendWithSeparator(missingFields, separator, MdekKeys.TITLE);
			}
			if (!MdekUtils.hasContent(inDoc.getString(MdekKeys.ABSTRACT))) {
				MdekUtils.appendWithSeparator(missingFields, separator, MdekKeys.ABSTRACT);
			}
			if (EnumUtil.mapDatabaseToEnumConst(PublishType.class, inDoc.get(MdekKeys.PUBLICATION_CONDITION)) == null) {
				MdekUtils.appendWithSeparator(missingFields, separator, MdekKeys.PUBLICATION_CONDITION);			
			}
			if (!objectService.hasAddressReference(inDoc)) {
				MdekUtils.appendWithSeparator(missingFields, separator, "referenced address");
			}

		} else if (whichType == IdcEntityType.ADDRESS) {

		}

		// TODO: check ALL mandatory data for publishing ?

		if (missingFields.length() > 0) {
			updateImportJobInfoMessages(MSG_WARN + tag + "Mandatory data missing [" + missingFields + "]", userUuid);
			updateImportJobInfoFrontendMessages(
				"Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) +
				"\" fehlen Daten für Veröffentlichung [" + missingFields + "].", userUuid);
			return false;
		}

		return true;
	}

	/**
	 * Process the passed doc, so that the node will be created under import node under according parent !<br>
	 * The new parent UUID is determined from already mapped entities.<br>
	 * NOTICE: Node with existing UUID is handled dependent from flag <b>copyNodeIfPresent</b>.
	 * 		<b>MAY THROW MDEK EXCEPTION for reporting existing UUID !</b><br>
	 * NOTICE: Existing Address is NOT handled only non existing one !
	 * 		Not existing FREE Address will be transformed to PERSON Address.
	 * @param whichType which type is this entity to import
	 * @param copyNodeIfPresent true=If UUID exists then a new UUID will be set to create a new entity !
	 * 		Then the ORIG_ID is removed if not unique.<br>
	 * 		false=If UUID exists exception is thrown, so UUID is reported and can be collected !
	 * @param inDoc the entity to import represented by its doc. Will be manipulated !
	 * @param importNodeUuid the UUID of the import node for entities of given type
	 * @param userUuid calling user
	 * @return Map containing the detected node in catalog, e.g. if further instance of already processed entity
	 * 		(or null if new import entity)
	 * @throws MdekException MdekErrorType.ENTITY_ALREADY_EXISTS = thrown if copy of node not allowed, e.g. separateImport && no copy of node allowed !
	 * 		encapsulates UUID !
	 */
	private HashMap processUuidsOnSeparateImport(IdcEntityType whichType,
			boolean copyNodeIfPresent,
			IngridDocument inDoc, String importNodeUuid,
			String userUuid)
		throws MdekException {
		// first extract map of mapped uuids !
		HashMap<String, String> uuidMappingMap = getUuidMappingMap(userUuid);

		String inUuid = inDoc.getString(MdekKeys.UUID);
		String inOrigId = EntityHelper.getOrigIdFromDoc(whichType, inDoc);
		String inParentUuid = inDoc.getString(MdekKeys.PARENT_UUID);
		String inWorkState = inDoc.getString(MdekKeys.WORK_STATE);

		IEntity retNode = null;

		// process UUID

		// check whether entity exists. if so react according to copyNodeIfPresent.
		// If copy allowed create new UUID to store new entity and remember mapping !
		String newUuid = inUuid;
		if (inUuid != null) {
			// first check map !
			if (uuidMappingMap.containsKey(inUuid)) {
				// ALREADY MAPPED -> multiple instances of entity (or included multiple times as entity in import) ?
				// Get UUID mapped to !
				newUuid = uuidMappingMap.get(inUuid);

				// WE ALSO LOAD existing node, to be passed to outside for checks !
				if (whichType == IdcEntityType.OBJECT) {
					retNode = objectService.loadByUuid(newUuid, null);
				} else if (whichType == IdcEntityType.ADDRESS) {
					retNode = addressService.loadByUuid(newUuid, null);
				}

			} else {
				// NOT MAPPED YET -> first instance of entity !
				IEntity existingNode = null;
				if (whichType == IdcEntityType.OBJECT) {
					existingNode = objectService.loadByUuid(inUuid, null);
				} else if (whichType == IdcEntityType.ADDRESS) {
					existingNode = addressService.loadByUuid(inUuid, null);
				}

				if (existingNode != null) {
					// existing entity !!!
					if (whichType == IdcEntityType.OBJECT && copyNodeIfPresent) {
						// create new node, new UUID will be created !
						newUuid = null;
					} else {
						// report existing node, will be collected and reported (Object) or ignored (Address) !
						// No changes in catalogue !
						throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS, newUuid));
					}
				}
			}
		}
		// create new uuid to create new entity !
		boolean newUuidGenerated = false;
		if (newUuid == null) {
			newUuid = EntityHelper.getInstance().generateUuid();
			newUuidGenerated = true;
		}

		// process ORIG_ID

		// check orig id and remove if not unique !
		String newOrigId = inOrigId;
		if (inOrigId != null) {
			IEntity existingOrigIdNode = null;
			if (whichType == IdcEntityType.OBJECT) {
				existingOrigIdNode = objectService.loadByOrigId(inOrigId,  null);
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingOrigIdNode = addressService.loadByOrigId(inOrigId,  null);
			}

			if (existingOrigIdNode != null) {
				// existing ORIG_ID entity !!!
				String existingUuid = EntityHelper.getUuidFromNode(whichType, existingOrigIdNode);

				String tag = createEntityTag(whichType, inUuid, inOrigId, inParentUuid, inWorkState);
				if (whichType == IdcEntityType.OBJECT && copyNodeIfPresent) {
					// just to be sure: could be the same entity (when multiple instances !), then
					// keep ORIG_ID, entity will be overwritten
					if (!newUuid.equals(existingUuid)) {
						// different entity ! we remove ORIG_ID
						newOrigId = null;
						inDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);
						inDoc.remove(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER);
						updateImportJobInfoMessages(MSG_WARN + tag +
							"Remove ORIG_ID:" + inOrigId + ", already set in " + whichType + " UUID:" + existingUuid, userUuid);
						updateImportJobInfoFrontendMessages(
							"Orig-ID \""+ inOrigId +"\" entfernt von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) +
							"\". Existiert schon in Objekt \"" + existingUuid + "\".", userUuid);
					}

				} else {
					updateImportJobInfoMessages(MSG_WARN + tag +
						"ORIG_ID:" + inOrigId + ", already set in " + whichType + " UUID:" + existingUuid + " ! WE SKIP !", userUuid);
					updateImportJobInfoFrontendMessages(
						"Orig-ID \""+ inOrigId +"\" von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) +
						"\" existiert schon in " + whichType.toGerman() + " \"" + existingUuid + "\".", userUuid);
					// report existing node, will be collected and reported (Object) or ignored (Address) !
					// No changes in catalogue !
					throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS, existingUuid));					
				}
			}
		}

		// finally map to UUID under import node !
		inDoc.put(MdekKeys.UUID, newUuid);
		uuidMappingMap.put(inUuid, newUuid);
		
		// process PARENT UUID

		String newParentUuid = inParentUuid;
		if (inParentUuid != null) {
			// get mapped UUID returns null if not mapped yet
			newParentUuid = uuidMappingMap.get(inParentUuid);
		}
		// if not mapped yet store under import node
		if (newParentUuid == null) {
			newParentUuid = importNodeUuid;
		}
		inDoc.put(MdekKeys.PARENT_UUID, newParentUuid);

		// log mapping if changed

		// only UUID change logged in frontend message !
		if (!MdekUtils.isEqual(inUuid, newUuid)) {
			// Only report if really generated, meaning if we have the FIRST instance of an entity to write !
			if (newUuidGenerated) {
				updateImportJobInfoFrontendMessages(
					"Neue UUID \"" + newUuid + "\" generiert für Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\".",
					userUuid);
			}
			// Update Identifier shown in frontend messages !
			inDoc.put(TMP_ENTITY_IDENTIFIER, "" + newUuid + " (" + inDoc.get(TMP_ENTITY_IDENTIFIER) + ")");			
		}

		// full logging in job info !
		if (!MdekUtils.isEqual(inUuid, newUuid) ||
				!MdekUtils.isEqual(inOrigId, newOrigId) ||
				!MdekUtils.isEqual(inParentUuid, newParentUuid)) {
			String tag = createEntityTag(whichType, inUuid, inOrigId, inParentUuid, inWorkState);
			updateImportJobInfoMessages(tag +
					"MAPPED to UUID:" + newUuid + " ORIG_ID:" + newOrigId + " PARENT_UUID:" + newParentUuid, userUuid);
		}

		HashMap retMap = new HashMap();
		retMap.put(TMP_FOUND_NODE, retNode);

		return retMap;
	}
	
	/** Checks whether given doc is FREE address and transforms it to PERSON under import node.
	 * Returns TRUE if this happened. */
	private boolean processFreeAddressToPerson(IngridDocument inDoc, String importNodeUuid, String userUuid) {
		Integer addrClass = (Integer) inDoc.get(MdekKeys.CLASS);
		if (AddressType.FREI.getDbValue().equals(addrClass)) {
			String tag = createEntityTag(IdcEntityType.ADDRESS, inDoc);
			updateImportJobInfoMessages(MSG_WARN + tag + "Changed from FREE to PERSON Address, store underneath import node", userUuid);
			updateImportJobInfoFrontendMessages("Import-Adresse \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" von Typ FREI zu PERSON geändert, unter Import Knoten gespeichert.", userUuid);
			inDoc.put(MdekKeys.CLASS, AddressType.PERSON.getDbValue());
			inDoc.put(MdekKeys.PARENT_UUID, importNodeUuid);
			
			return true;
		}
		
		return false;
	}

	/**
	 * Determine the according node in catalog of the import entity.
	 * Also takes over mod-user and responsible-user if an existing node was found !
	 * NOTICE: manipulates inDoc !
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc.
	 * 		Necessary changes (e.g. keep existing uuid instead of import uuid) will be adapted in this doc.
	 * @param userUuid calling user
	 * @return Map containing the detected node in catalog (or null if new import entity) AND whether
	 * 		working copy should be stored
	 * @throws MdekException MdekErrorType.ENTITY_ALREADY_EXISTS = thrown if address node already exists, then will be ignored, encapsulates UUID !
	 */
	private HashMap determineNodeInCatalog(IdcEntityType whichType,
			IngridDocument inDoc, String userUuid) {
		IEntity existingNode = null;
		Boolean storeWorkingVersion = false;

		String inUuid = inDoc.getString(MdekKeys.UUID);
		String inOrigId = EntityHelper.getOrigIdFromDoc(whichType, inDoc);
		String tag = createEntityTag(whichType, inUuid, inOrigId,
				inDoc.getString(MdekKeys.PARENT_UUID),
				inDoc.getString(MdekKeys.WORK_STATE));

		// UUID has highest priority, load via UUID. if entity found then replace ORIG_ID in doc (which may differ) !
		// Existing Addresses are skipped !
		if (inUuid != null) {
			if (whichType == IdcEntityType.OBJECT) {
				existingNode = objectService.loadByUuid(inUuid, IdcEntityVersion.WORKING_VERSION);
				if (existingNode != null) {
					inDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, ((ObjectNode)existingNode).getT01ObjectWork().getOrgObjId());
				}
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingNode = addressService.loadByUuid(inUuid, IdcEntityVersion.WORKING_VERSION);
				// Only report existing node if we have the FIRST instance of an entity to write !
				// Former instances should also be written !
				if (existingNode != null && inDoc.getBoolean(TMP_FIRST_INSTANCE)) {
//					inDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, ((AddressNode)existingNode).getT02AddressWork().getOrgAdrId());

					// report existing address, will be skipped !
					// No changes in catalogue !
					throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS, inUuid));
				}
			}
		}

		// if UUID not found load via ORIG_ID !
		if (existingNode == null &&	inOrigId != null) {
			if (whichType == IdcEntityType.OBJECT) {
				existingNode = objectService.loadByOrigId(inOrigId, IdcEntityVersion.WORKING_VERSION);
				
				// OBJECTS: if found keep existing UUID AND existing PARENT !
				if (existingNode != null) {
					// loaded via ORIG_ID: WE KEEP UUID IN CATALOG AND ALSO KEEP PARENT IN CATALOG !
					String existingUuid = EntityHelper.getUuidFromNode(whichType, existingNode);
					inDoc.put(MdekKeys.UUID, existingUuid);
					inDoc.put(MdekKeys.PARENT_UUID, EntityHelper.getParentUuidFromNode(whichType, existingNode));
					// NO WORKING VERSION but normal processing:
					// "work-state" OR "publishImmediately" setting determines how object is stored (published or working version)
//					storeWorkingVersion = true;
					updateImportJobInfoMessages(MSG_WARN + tag +
						"UUID not found, but found ORIG_ID in existing " + whichType + " UUID:" + existingUuid, userUuid);
					updateImportJobInfoFrontendMessages(
						"Import-Objekt \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" nur per Orig-ID \"" + inOrigId +
						"\" in Katalog gefunden. Katalog-Objekt \"" + existingUuid + "\" wird überschrieben, keine Verschiebung im Baum !", userUuid);
					// Update Identifier shown in frontend messages !
					inDoc.put (TMP_ENTITY_IDENTIFIER, "" + existingUuid + " (" + inDoc.get(TMP_ENTITY_IDENTIFIER) + ")");
				}
				
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingNode = addressService.loadByOrigId(inOrigId, IdcEntityVersion.WORKING_VERSION);
				
				// ADDRESS: if found skip address ! No changes in catalogue !
				// But only if we have the FIRST instance of an entity to write !
				// Former instances should also be written !
				if (existingNode != null && inDoc.getBoolean(TMP_FIRST_INSTANCE)) {
					String existingUuid = EntityHelper.getUuidFromNode(whichType, existingNode);
					// report existing address, will be skipped !
					updateImportJobInfoFrontendMessages(
						"Import-Adresse \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" nur per Orig-ID \"" + inOrigId +
						"\" in Katalog gefunden. Katalog-Adresse \"" + existingUuid + "\" bleibt unverändert. Import-Adresse wird ignoriert !", userUuid);
					// Update Identifier shown in frontend messages !
					inDoc.put (TMP_ENTITY_IDENTIFIER, "" + existingUuid + " (" + inDoc.get(TMP_ENTITY_IDENTIFIER) + ")");
					throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS, existingUuid));
				}
			}
		}

		// if no node found, check whether UUID not set, then create UUID
		// -> new "ArcGis Entity" or entity without any ID or ...
		if (existingNode == null && inUuid == null) {
			// set new uuid in doc to be used afterwards !
			String newUuid = EntityHelper.getInstance().generateUuid();
			inDoc.put(MdekKeys.UUID, newUuid);
			updateImportJobInfoMessages(MSG_WARN + tag +
				"UUID not found, ORIG_ID not found, create new " + whichType + " UUID:" + newUuid, userUuid);
			updateImportJobInfoFrontendMessages(
				"Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" im Katalog nicht gefunden (keine UUID, ORIG-ID)." +
				" Neue UUID \"" + newUuid + "\" generiert !", userUuid);
			// Update Identifier shown in frontend messages !
			inDoc.put (TMP_ENTITY_IDENTIFIER, "" + newUuid + " (" + inDoc.get(TMP_ENTITY_IDENTIFIER) + ")");
		}

		// modifying and responsible user already processed in preprocessDoc !
		// but keep responsible user of existing node if not found there !
		if (existingNode != null &&
				(Boolean)inDoc.get(TMP_RESPONSIBLE_USER_NOT_FOUND)) {
			// set responsible_uuid from existing entity.
			String respUuid = null;

			// take over FROM WORKING VERSION.
			if (whichType == IdcEntityType.OBJECT) {
				T01Object existingObj = ((ObjectNode) existingNode).getT01ObjectWork();
				respUuid = existingObj.getResponsibleUuid();
			} else if (whichType == IdcEntityType.ADDRESS) {
				T02Address existingAddr = ((AddressNode) existingNode).getT02AddressWork();
				respUuid = existingAddr.getResponsibleUuid();
			}
			// just to be sure: we take over only if set ???
//			modUuid = (modUuid == null) ? userUuid : modUuid;
//			respUuid = (respUuid == null) ? userUuid : respUuid;
			beanToDocMapper.mapResponsibleUser(respUuid, inDoc, MappingQuantity.INITIAL_ENTITY);
		}

		HashMap retMap = new HashMap();
		retMap.put(TMP_FOUND_NODE, existingNode);
		retMap.put(TMP_STORE_WORKING_VERSION, storeWorkingVersion);

		return retMap;
	}

	/**
	 * Determine the "new" parent of the entity in the catalog ! NO separate import !
	 * NOTICE: manipulates inDoc !
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc.
	 * 		The new parent uuid will be set in this doc.
	 * @param existingNode the according node in catalog of the import entity. Null IF NEW ENTITY !
	 * 		This one is determined separately and passed here, so we don't have to load twice !
	 * @param importNode the import node for entities of given type
	 * @param userUuid calling user
	 * @return Map containing the detected "new" parent node. NOTICE: can be NULL if existing entity
	 * 		is top node !. This one is also set in inDoc. Further returns whether working copy
	 * 		should be stored
	 */
	private HashMap determineParentNodeInCatalog(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode, 
			IEntity importNode,
			String userUuid) {
		// default "new" parent is import node
		IEntity newParentNode = importNode;
		Boolean storeWorkingVersion = false;

		// create entity tag for messages !
		String tag = createEntityTag(whichType, inDoc);

		// fetch parent from import
		String inParentUuid = inDoc.getString(MdekKeys.PARENT_UUID);
		IEntity inParentNode = null;
		if (whichType == IdcEntityType.OBJECT) {
			inParentNode = objectService.loadByUuid(inParentUuid, null);
		} else if (whichType == IdcEntityType.ADDRESS) {
			inParentNode = addressService.loadByUuid(inParentUuid, null);
		}
		boolean inParentExists = (inParentNode != null);

		// check and set "new" parent dependent from existing entity and import parent.
		if (existingNode != null) {
			// entity exists.
			if (inParentUuid != null) {
				// import parent set.
				if (inParentExists) {
					// import parent exists. we set "new" parent from import.
					newParentNode = inParentNode;
				} else {
					// import parent does NOT exist. we keep "old" parent.
					updateImportJobInfoMessages(MSG_WARN + tag + "Parent not found, we keep former parent", userUuid);
					// Only report if we have the FIRST instance of an entity to write ! all instances should have SAME PARENT !
					if (inDoc.getBoolean(TMP_FIRST_INSTANCE)) {
						updateImportJobInfoFrontendMessages(
							"Import-Elternknoten \"" + inParentUuid +	"\" in Katalog NICHT gefunden. Vorhandene(s) Import-" +
							whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" behält Position im Katalog !",
							userUuid);
					}
					if (whichType == IdcEntityType.OBJECT) {
						newParentNode = objectService.loadByUuid(((ObjectNode)existingNode).getFkObjUuid(), null);
					} else if (whichType == IdcEntityType.ADDRESS) {
						newParentNode = addressService.loadByUuid(((AddressNode)existingNode).getFkAddrUuid(), null);
					}
				}
			} else {
				// import parent NOT set.
				// If imported document does NOT contain work state it was imported via CSW or ArcGis.
				// Then we keep "old" parent. NOTICE: if old parent is null we keep null (top node)
				// If work state set, then it was imported via IGE Export/Import, then we set parent null
				// and assume it's a TOP NODE !
				if (inDoc.get(MdekKeys.WORK_STATE) != null) {
					// Export/Import from IGE: no parent -> top node
					newParentNode = null;
				} else {
					// Import ArcGis OR CSW: keep catalog parent
					if (whichType == IdcEntityType.OBJECT) {
						newParentNode = objectService.loadByUuid(((ObjectNode)existingNode).getFkObjUuid(), null);
					} else if (whichType == IdcEntityType.ADDRESS) {
						newParentNode = addressService.loadByUuid(((AddressNode)existingNode).getFkAddrUuid(), null);
					}
				}
			}
		} else {
			// object does NOT exist.
			if (inParentUuid != null) {
				// import parent set.
				if (inParentExists) {
					// import parent exists. we set "new" parent from import.
					newParentNode = inParentNode;
				} else {
					// import parent does NOT exist. store under import node.
					updateImportJobInfoMessages(MSG_WARN + tag + "Parent not found, store underneath import node", userUuid);
					updateImportJobInfoFrontendMessages(
						"Import-Elternknoten in Katalog NICHT gefunden. Neue(s) Import-" + whichType.toGerman() +
						" \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" wird unter Import Knoten gespeichert !", userUuid);
					newParentNode = importNode;
					// NO WORKING VERSION but normal processing:
					// "work-state" OR "publishImmediately" setting determines how object is stored (published or working version)
//					storeWorkingVersion = true;
				}
			} else {
				// import parent NOT set -> NEW TOP NODE
				updateImportJobInfoMessages(MSG_WARN + tag + "Parent not set, we create new top node", userUuid);
				updateImportJobInfoFrontendMessages(
					"Import-Elternknoten nicht gesetzt. Neue(s) Import-" + whichType.toGerman() +
					" \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\" wird zum Top Knoten !", userUuid);
				newParentNode = null;
				// NO WORKING VERSION but normal processing:
				// "work-state" OR "publishImmediately" setting determines how object is stored (published or working version)
//				storeWorkingVersion = true;
			}
		}

		String newParentUuid = null;
		// NOTICE: if existing node is top node, the parent node is NULL !
		if (newParentNode != null) {
			newParentUuid = EntityHelper.getUuidFromNode(whichType, newParentNode);
		}
		inDoc.put(MdekKeys.PARENT_UUID, newParentUuid);

		HashMap retMap = new HashMap();
		retMap.put(TMP_FOUND_NODE, newParentNode);
		retMap.put(TMP_STORE_WORKING_VERSION, storeWorkingVersion);

		return retMap;
	}

	/**
	 * Process all relations of the given objects meaning:
	 * - object-object relations are removed and stored in running job info to be processed after import of all entities !
	 * - relation to address is checked immediately and removed if address not present !
	 * @param objDoc the object to import represented by its doc (containing all relations).
	 * 		NOTICE: may be manipulated (relations to objs removed !)
	 * @param userUuid calling user
	 */
	private void processRelationsOfObject(IngridDocument objDoc, String userUuid) {
		// create object tag for messages !
		String objTag = createEntityTag(IdcEntityType.OBJECT, objDoc);
		String objUuid = objDoc.getString(MdekKeys.UUID);

		// ADDRESS REFERENCES

		// We process list instance in doc and remove non existing refs !
		List<IngridDocument> addrRefs = (List) objDoc.get(MdekKeys.ADR_REFERENCES_TO);
		for (Iterator i = addrRefs.iterator(); i.hasNext();) {
			IngridDocument addrRef = (IngridDocument) i.next();
			String addrRefUuid = addrRef.getString(MdekKeys.UUID);
			if (addressService.loadByUuid(addrRefUuid, null) == null) {
				String refType = addrRef.getString(MdekKeys.RELATION_TYPE_NAME);
				updateImportJobInfoMessages(MSG_WARN + objTag +
					"REMOVED address reference of type \"" + refType + "\" to non existing address " + addrRefUuid, userUuid);
				updateImportJobInfoFrontendMessages("Adress-Referenz vom Typ \"" + refType + "\" zu nicht existierender Adresse \"" +
						addrRefUuid + "\" entfernt von Import-Objekt \"" + objDoc.get(TMP_ENTITY_IDENTIFIER) + "\".", userUuid);
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
	
	/**
	 * Process all additional field values and remove a value if the according additional field definition
	 * is not found or differs from the one in catalog. Returns whether problems occured.
	 * @param objDoc the object to import represented by its doc (containing all relations).
	 * 		NOTICE: may be manipulated (additional field value removed if field not found !)
	 * @param userUuid calling user
	 * @return true=all fields ok, NO data changed<br>
	 * 		false=problems with additional fields, data was removed
	 */
/*
	private boolean processAdditionalFields(IngridDocument objDoc, String userUuid) {
		boolean allFieldsOk = true;

		// create object tag for messages !
		String objTag = createEntityTag(IdcEntityType.OBJECT, objDoc);

		// get additional field data of object (NOT DEFINITION)
		List<IngridDocument> inFieldDocs = (List) objDoc.get(MdekKeys.ADDITIONAL_FIELDS);
		if (inFieldDocs == null) {
			inFieldDocs = new ArrayList<IngridDocument>(0);
		}

		for (Iterator<IngridDocument> i = inFieldDocs.iterator(); i.hasNext();) {
			IngridDocument inFieldDoc = i.next();
			Long inFieldId = (Long) inFieldDoc.get(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER);
			String inFieldName = inFieldDoc.getString(MdekKeys.SYS_ADDITIONAL_FIELD_NAME);
			String inFieldValue = inFieldDoc.getString(MdekKeys.ADDITIONAL_FIELD_VALUE);
			
			// type of additional field NOT part of object data !
			// but all field definitions are also part of object doc, get type via definition !
			String inFieldType = null;
			IngridDocument inFieldDef = (IngridDocument) objDoc.get(MdekKeys.SYS_ADDITIONAL_FIELD_KEY_PREFIX + inFieldId);
			if (inFieldDef != null) {
				inFieldType = inFieldDef.getString(MdekKeys.SYS_ADDITIONAL_FIELD_TYPE);
			}

			// check whether additional field / value exists
			boolean fieldOk = false;

			// fetch field(s) in catalog and compare
			// NOTICE: We fetch field by NAME cause ids might differ across IGC catalogs.
			IngridDocument sysFieldDocs = catalogService.getSysAdditionalFieldsByName(new String[]{inFieldName}, null);
			Iterator itKeys = sysFieldDocs.keySet().iterator();
			while (itKeys.hasNext()) {
				String key = (String) itKeys.next();
				Long sysFieldId = null;
				if (key != null && key.startsWith(MdekKeys.SYS_ADDITIONAL_FIELD_KEY_PREFIX)) {
					IngridDocument sysFieldDoc = (IngridDocument) sysFieldDocs.get(key);
					if (sysFieldDoc != null) {
						sysFieldId = (Long) sysFieldDoc.get(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER);
						String sysFieldName = sysFieldDoc.getString(MdekKeys.SYS_ADDITIONAL_FIELD_NAME);
						String sysFieldType = sysFieldDoc.getString(MdekKeys.SYS_ADDITIONAL_FIELD_TYPE);
						
						if (MdekUtils.isEqual(inFieldName, sysFieldName) &&
								MdekUtils.isEqual(inFieldType, sysFieldType))
						{
							// WE DO NOT COMPARE VALUE OF SELECTION LIST BECAUSE OF COMBOBOX in GUI (FREE TEXT ENTRY POSSIBLE !!!) !!!
							// so we take over all values if name and type of field is ok !
							fieldOk = true;
							
							// field equals field in catalog, check whether field has selection list and compare value !
//							if (MdekUtils.AdditionalFieldType.LIST.getDbValue().equals(sysFieldType)) {
								// Is selection list, so restricted entry values ! we fetch all selection lists and compare value
//								for (Iterator<String> j = sysFieldDoc.keySet().iterator(); j.hasNext();) {
//									String sysFieldKey = j.next();
//									if (sysFieldKey.startsWith(MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX)) {
//										String[] entries = (String[]) sysFieldDoc.get(sysFieldKey);
//										if (Arrays.asList(entries).contains(inFieldValue)) {
//											fieldOk = true;
//											break;
//										}
//									}
//								}
//								if (!fieldOk) {
//									updateImportJobInfoMessages(MSG_WARN + objTag +
//										"Additional field DATA \"" + inFieldValue + "\" NOT FOUND in defined SELECTION_LIST " +
//										"(Field-Name:" + inFieldName + ", Field-Type:" + inFieldType + ")", userUuid);
//								}
//							} else {
								// No selection list, so any text value is ok !
//								fieldOk = true;
//							}

						} else {
							updateImportJobInfoMessages(MSG_WARN + objTag +
								"Additional field DEFINITION found, but differs ! DATA \"" + inFieldValue +
								"\" (Import Field-Name:" + inFieldName + ", Field-Type:" + inFieldType +
								") != (Existing Field-Name:" + sysFieldName + ", Field-Type:" + sysFieldType + ")", userUuid);
						}
					}
				}
				
				// break if field found and is ok ! But take over field id of this catalog (may differ across catalogs !)
				if (fieldOk) {
					inFieldDoc.put(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER, sysFieldId);
					break;
				}
			}
			
			// remove field from import if field not ok
			if (!fieldOk) {
				updateImportJobInfoMessages(MSG_WARN + objTag +
					"Additional Field not found in catalogue, REMOVE from Object ! DATA \"" + inFieldValue +
					"\" (Field-Name:" + inFieldName + ", Field-Type:" + inFieldType + ")", userUuid);
				allFieldsOk = false;
				i.remove();
			}
		}

		return allFieldsOk;
	}
*/
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
		HashMap<String, String> uuidMap = (HashMap<String, String>) runningJobInfo.get(KEY_UUID_MAPPING_MAP);
		if (uuidMap == null) {
			uuidMap = new HashMap<String, String>();
			runningJobInfo.put(KEY_UUID_MAPPING_MAP, uuidMap);
		}

		return uuidMap;
	}

	/** Extract set from running job info containing existing object UUIDs which should be reported to fronted
	 * cause copy not allowed (when executing separate import !).
	 * @param userUuid calling user
	 * @return the set of uuids. NEVER null.
	 */
	private Set<String> getExistingUuidsToReport(String userUuid) {
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		Set<String> uuids = (Set<String>) runningJobInfo.get(KEY_EXISTING_UUIDS_TO_REPORT);
		if (uuids == null) {
			uuids = new HashSet<String>();
			runningJobInfo.put(KEY_EXISTING_UUIDS_TO_REPORT, uuids);
		}

		return uuids;
	}

	/**
	 * Move existing node to "new" parent node if parent in IMPORT differs and exists !
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc.
	 * 		NOTICE: already processed ! data MUST fit to passed nodes ! 
	 * @param existingNode existing node like determined before. IF NULL non existent !
	 * @param parentNode new parent node like determined before. IF NULL then new top node !
	 * @param userUuid calling user
	 */
	private void processMove(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode, IEntity parentNode, String userUuid)
	throws Exception {

		if (existingNode == null) {
			return;
		}

		String currentParentUuid = EntityHelper.getParentUuidFromNode(whichType, existingNode);
		String newParentUuid = null;
		if (parentNode != null) {
			newParentUuid = EntityHelper.getUuidFromNode(whichType, parentNode);
		}

		if (MdekUtils.isEqual(newParentUuid, currentParentUuid)) {
			return;
		}

		// create entity tag with "old" data (doc contains already new parent !)
		String tag = createEntityTag(whichType, inDoc.getString(MdekKeys.UUID),
			EntityHelper.getOrigIdFromDoc(whichType, inDoc),
			currentParentUuid,
			inDoc.getString(MdekKeys.WORK_STATE));

		// NOTICE: we move to top if newParentUuid == null
		// wemove :) !
		try {
			String uuidToMove = EntityHelper.getUuidFromNode(whichType, existingNode);

			if (whichType == IdcEntityType.OBJECT) {
				objectService.moveObject(uuidToMove, newParentUuid, false, userUuid, true);
			} else if (whichType == IdcEntityType.ADDRESS) {
				// should NOT happen ! Existing addresses are skipped !
				String msg = MSG_WARN + tag + "Do NOT move existing address ! Should have been skipped before !?";
				updateImportJobInfoMessages(msg, userUuid);
				throw createImportException(msg);
			}

			updateImportJobInfoMessages(tag + "Moved to new parent " + newParentUuid, userUuid);
			updateImportJobInfoFrontendMessages(
				"VERSCHOBEN im Katalog: Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) +
				"\" unter neuen Elternknoten \"" + newParentUuid + "\".",
				userUuid);
		} catch (Exception ex) {
			// problems ! we set parent in doc to current parent to guarantee correct parent !
			inDoc.put(MdekKeys.PARENT_UUID, currentParentUuid);

			// and log
			String errorMsg = MSG_WARN + tag + "Problems moving to new parent " + newParentUuid + " : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);
			updateImportJobInfoFrontendMessages(
				"Probleme beim Verschieben von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) +
				"\" unter neuen Elternknoten \"" + newParentUuid + "\". " + whichType.toGerman() + " wird nicht verschoben.",
				userUuid);

			throw ex;
		}
	}

	/** Assign entity to QA. */
	private void processAssignToQA(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode,
			int numImported, int totalNum, String userUuid)
	throws Exception {
		// create tag and text for messages !
		String tag = createEntityTag(whichType, inDoc);
		String newEntityMsg = createNewEntityMsg(whichType, (existingNode == null));

		try {
			if (whichType == IdcEntityType.OBJECT) {
				objectService.assignObjectToQA(inDoc, userUuid, true);
			} else if (whichType == IdcEntityType.ADDRESS) {
				addressService.assignAddressToQA(inDoc, userUuid, true);
			}
			updateImportJobInfo(whichType, numImported+1, totalNum, userUuid);
			updateImportJobInfoMessages(tag + newEntityMsg + "ASSIGNED TO QA", userUuid);

		} catch (Exception ex) {
			String errorMsg = MSG_WARN + tag + "Problems assigning to QA : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);
			updateImportJobInfoFrontendMessages(
				"Probleme beim Zuweisen an QS von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\".",
				userUuid);

			throw ex;
		}
	}

	/** PUBLISH entity. */
	private void processPublish(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode,
			int numImported, int totalNum, String userUuid)
	throws Exception {
		// create tag and text for messages !
		String tag = createEntityTag(whichType, inDoc);
		String newEntityMsg = createNewEntityMsg(whichType, (existingNode == null));

		// first check whether publishing is possible with according parent
		// NOTICE: determined parent can be null -> update of existing top object
		String inParentUuid = inDoc.getString(MdekKeys.PARENT_UUID);
		IEntity parentNode = null;
		if (whichType == IdcEntityType.OBJECT) {
			parentNode = objectService.loadByUuid(inParentUuid, null);
		} else if (whichType == IdcEntityType.ADDRESS) {
			parentNode = addressService.loadByUuid(inParentUuid, null);
		}

		if (parentNode != null) {
			boolean hasPublishedVersion = false;
			if (whichType == IdcEntityType.OBJECT) {
				hasPublishedVersion = objectService.hasPublishedVersion((ObjectNode) parentNode);
			} else if (whichType == IdcEntityType.ADDRESS) {
				hasPublishedVersion = addressService.hasPublishedVersion((AddressNode) parentNode);
			}

			if (!hasPublishedVersion) {
				// parent not published -> store working version !
				String msg = MSG_WARN + tag + "Parent not published";
				updateImportJobInfoMessages(msg, userUuid);
				updateImportJobInfoFrontendMessages(
					"Probleme beim Veröffentlichen von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\"." +
					" Eltern-"+ whichType.toGerman() + " nicht veröffentlicht.",
					userUuid);
				throw createImportException(msg);
			}
		}

		// ok, we publish !
		try {
			if (whichType == IdcEntityType.OBJECT) {
				// we DON'T force publication condition ! if error, we store working version !
				objectService.publishObject(inDoc, false, userUuid, true);
			} else if (whichType == IdcEntityType.ADDRESS) {
				addressService.publishAddress(inDoc, false, userUuid, true);
			}

			updateImportJobInfo(whichType, numImported+1, totalNum, userUuid);
			updateImportJobInfoMessages(tag + newEntityMsg + "PUBLISHED", userUuid);

		} catch (Exception ex) {
			String errorMsg = MSG_WARN + tag + "Problems publishing : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);
			updateImportJobInfoFrontendMessages(
				"Probleme beim Veröffentlichen von Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\".",
				userUuid);

			throw ex;
		}
	}

	/** Store WORKING VERSION of entity. */
	private void processStoreWorkingCopy(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode,
			int numImported, int totalNum, boolean wasPublish,
			String userUuid)
	throws MdekException {
		// create tag and text for messages !
		String tag = createEntityTag(whichType, inDoc);
		// add warning if entity originally should be published !
		if (wasPublish) {
			tag = MSG_WARN + tag;
		}
		String newEntityMsg = createNewEntityMsg(whichType, (existingNode == null));

		try {
			if (whichType == IdcEntityType.OBJECT) {
				objectService.storeWorkingCopy(inDoc, userUuid, true);
			} else if (whichType == IdcEntityType.ADDRESS) {
				addressService.storeWorkingCopy(inDoc, userUuid, true);
			}

			updateImportJobInfo(whichType, numImported+1, totalNum, userUuid);
			updateImportJobInfoMessages(tag + newEntityMsg + "stored as WORKING version", userUuid);
			if (wasPublish) {
				updateImportJobInfoFrontendMessages(
					"Importiert als Arbeitskopie, nicht veröffentlicht ! Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\".",
					userUuid);				
			}

		} catch (Exception ex) {
			String errorMsg = MSG_WARN + tag + "Problems storing working version : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);
			updateImportJobInfoFrontendMessages(
				"Probleme beim Speichern als Arbeitskopie ! Import-" + whichType.toGerman() + " \"" + inDoc.get(TMP_ENTITY_IDENTIFIER) + "\".",
				userUuid);				

			// entity type specific stuff
			if (whichType == IdcEntityType.OBJECT) {
				// object was NOT persisted, we also remove remembered obj references of this object.
				String objUuid = inDoc.getString(MdekKeys.UUID);
				evictObjReferences(objUuid, userUuid);
			}

			// we throw MdekException (= RuntimeException) to avoid catch clause outside !
			// NOTICE: MdekErrorHandler.handleException reacts to type of exception but should not matter,
			// we already encapsulate exception info (full stack trace) ! 
			throw createImportException(errorMsg + getStackTrace(ex));			
		}
	}

	private MdekException createImportException(String message) {
		return new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, message));
	}

	/** Creates entity tag from entity doc ! To be displayed as entity "identifier". */
	private String createEntityTag(IdcEntityType whichType, IngridDocument entityDoc) {
		String origId = EntityHelper.getOrigIdFromDoc(whichType, entityDoc);
		return createEntityTag(whichType,
				entityDoc.getString(MdekKeys.UUID),
				origId,
				entityDoc.getString(MdekKeys.PARENT_UUID),
				entityDoc.getString(MdekKeys.WORK_STATE));
	}

	/** Creates entity tag to be displayed as entity "identifier". */
	private String createEntityTag(IdcEntityType whichType,
			String uuid, String origId, String parentUuid, String workState) {
		StringBuilder tag = new StringBuilder();
		if (whichType == IdcEntityType.OBJECT) {
			tag.append("Object");
		} else if (whichType == IdcEntityType.ADDRESS) {
			tag.append("Address");			
		}
		tag.append(" UUID:" + uuid);
		tag.append(" ORIG_ID:" + origId);
		tag.append(" PARENT_UUID:" + parentUuid);
		tag.append(" work-state:" + workState);
		tag.append(" >> ");

		return tag.toString();
	}
	/** Creates object tag simply from UUID ! To be displayed as object "identifier". */
	private String createEntityTag(IdcEntityType whichType, String uuid) {
		StringBuilder tag = new StringBuilder();
		if (whichType == IdcEntityType.OBJECT) {
			tag.append("Object");
		} else if (whichType == IdcEntityType.ADDRESS) {
			tag.append("Address");			
		}
		tag.append(" UUID:" + uuid);
		tag.append(" >> ");

		return tag.toString();
	}

	/** Creates a message-part whether entity is NEW or EXISTING */
	private String createNewEntityMsg(IdcEntityType whichType, boolean isNewEntity) {
		StringBuilder tag = new StringBuilder();
		if (isNewEntity) {
			tag.append("NEW ");
		} else {
			tag.append("EXISTING ");
		}
		if (whichType == IdcEntityType.OBJECT) {
			tag.append("Object ");
		} else if (whichType == IdcEntityType.ADDRESS) {
			tag.append("Address ");
		}

		return tag.toString();
	}

	private String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();		
	}
}
