package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekError.MdekErrorType;
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
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.EntityHelper;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.ige.MdekUtils;
import de.ingrid.utils.ige.MdekUtils.AddressType;
import de.ingrid.utils.ige.MdekUtils.IdcEntityType;
import de.ingrid.utils.ige.MdekUtils.IdcEntityVersion;
import de.ingrid.utils.ige.MdekUtils.ObjectType;
import de.ingrid.utils.ige.MdekUtils.PublishType;

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

	// static temporary keys for transferring arbitrary stuff between methods
	/** Store working Copy ?<br>
	 * Value: Boolean */
	private final static String TMP_STORE_WORKING_VERSION = "TMP_STORE_WORKING_VERSION";
	/** Found node in catalog (e.g. to update from Import or "new" parent node)<br>
	 * Value: IEntity (Object-/AddressNode) */
	private final static String TMP_FOUND_NODE = "TMP_FOUND_NODE";
	
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
		writeEntity(IdcEntityType.OBJECT, objDoc, userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeAddress(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeAddress(IngridDocument addrDoc, String userUuid) {
		writeEntity(IdcEntityType.ADDRESS, addrDoc, userUuid);
	}

	public void writeEntity(IdcEntityType whichType, IngridDocument inDoc, String userUuid) {
		// extract context
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		boolean publishImmediately = (Boolean) runningJobInfo.get(KEY_PUBLISH_IMMEDIATELY);
		boolean doSeparateImport = (Boolean) runningJobInfo.get(KEY_DO_SEPARATE_IMPORT);
		int numImportedObjects = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS);
		int numImportedAddresses = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES);
		int totalNumObjects = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS);
		int totalNumAddresses = (Integer) runningJobInfo.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES);
		IEntity importNode = null;
		if (whichType == IdcEntityType.OBJECT) {
			importNode = (IEntity) runningJobInfo.get(KEY_OBJ_IMPORT_NODE);			
		} else if (whichType == IdcEntityType.ADDRESS) {
			importNode = (IEntity) runningJobInfo.get(KEY_ADDR_IMPORT_NODE);
		}

		// process import doc, remove/fix wrong data from exporting catalog
		preprocessDoc(whichType, inDoc, userUuid);

		boolean storeWorkingVersion = false;
		// update existing node or create new node ! this one holds the found node !
		IEntity existingNode = null;

		if (doSeparateImport) {
			// process UUIDs, ORIG_IDs, PARENT_UUIDs so ALL entities will be created under import node ! 
			processUuidsOnSeparateImport(whichType,
				inDoc, EntityHelper.getUuidFromNode(whichType, importNode), userUuid);

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
		
			storeWorkingVersion = true;
			
		} else {
			// determine the according node in the catalog
			HashMap retMap = determineNode(whichType, inDoc, userUuid);
			existingNode = (IEntity) retMap.get(TMP_FOUND_NODE);
			if ((Boolean) retMap.get(TMP_STORE_WORKING_VERSION)) {
				storeWorkingVersion = true;
			}

			// entity type specific stuff
			if (whichType == IdcEntityType.ADDRESS) {
				// if node not found and is a FREE address then transform it to a PERSON.
				// NEW FREE addresses are never added as FREE address. Will be added under import node as PERSON !
				if (existingNode == null) {
					if (processFreeAddressToPerson(inDoc, EntityHelper.getUuidFromNode(whichType, importNode), userUuid)) {
						storeWorkingVersion = true;
					}
				}
			}

			// determine the parent (may differ from current parent)
			retMap = determineParentNode(whichType, inDoc, existingNode, importNode, userUuid);
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

			// PUBLISH ENTITY ?
			// ----------------

			if (publishImmediately &&
					!storeWorkingVersion &&
					checkMandatoryData(whichType, inDoc, userUuid)) {

				// if workflow enabled then ASSIGN TO QA else PUBLISH !
				if (catalogService.isWorkflowEnabled()) {
					// Workflow enabled -> ASSIGN TO QA ! On error store working version !
					try {
						if(whichType == IdcEntityType.OBJECT)
							processAssignToQA(whichType, inDoc, existingNode, numImportedObjects, totalNumObjects, userUuid);
						else
							processAssignToQA(whichType, inDoc, existingNode, numImportedAddresses, totalNumAddresses, userUuid);
					
					} catch (Exception ex) {
						storeWorkingVersion = true;
					}

				} else {
					// Workflow disabled -> PUBLISH !  On error store working version !
					try {
						if(whichType == IdcEntityType.OBJECT)
							processPublish(whichType, inDoc, existingNode, parentNode, numImportedObjects, totalNumObjects, userUuid);
						else
							processPublish(whichType, inDoc, existingNode, parentNode, numImportedAddresses, totalNumAddresses, userUuid);
					
					} catch (Exception ex) {
						storeWorkingVersion = true;
					}
				}

			} else {
				storeWorkingVersion = true;			
			}
		}

		// STORE WORKING COPY ?
		// --------------------

		if (storeWorkingVersion) {
			if(whichType == IdcEntityType.OBJECT)
				processStoreWorkingCopy(whichType, inDoc, existingNode, numImportedObjects, totalNumObjects, publishImmediately, userUuid);
			else
				processStoreWorkingCopy(whichType, inDoc, existingNode, numImportedAddresses, totalNumAddresses, publishImmediately, userUuid);
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
		if (!AddressType.INSTITUTION.getDbValue().equals(addrImport.getAdrType())) {
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
						j.remove();
					} else {
						// remove if not same state !
						boolean objRefHasPublishedVersion = objectService.hasPublishedVersion(objRefNode);
						if (objIsPublished && !objRefHasPublishedVersion) {
							updateImportJobInfoMessages(MSG_WARN + objTag +
									"REMOVED object reference of type \"" + refType + "\" to NON PUBLISHED object " + objRefUuid, userUuid);
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
	 * @param userUuid calling user
	 * @return the preprocessed inDoc (same instance as passed one !) 
	 */
	private IngridDocument preprocessDoc(IdcEntityType whichType,
			IngridDocument inDoc, String userUuid) {
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
		inDoc.remove(MdekKeys.DATE_OF_LAST_MODIFICATION);
		inDoc.remove(MdekKeys.DATE_OF_CREATION);

		// default: calling user is mod user and responsible !
		beanToDocMapper.mapModUser(userUuid, inDoc, MappingQuantity.INITIAL_ENTITY);
		beanToDocMapper.mapResponsibleUser(userUuid, inDoc, MappingQuantity.INITIAL_ENTITY);

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
			return false;
		}

		return true;
	}

	/**
	 * Processes the passed doc, so that the node will be created under import node under according parent !
	 * If UUID exists then a new UUID will be set to create a new entity ! Then the ORIG_ID is removed if not unique.
	 * The new parent UUID is determined from already mapped entities.<br>
	 * NOTICE: FREE Addresses will be transformed to PERSON Addresses.
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc. Will be manipulated !
	 * @param importNodeUuid the UUID of the import node for entities of given type
	 * @param userUuid calling user
	 * @return the processed inDoc (same instance as passed one !) 
	 */
	private IngridDocument processUuidsOnSeparateImport(IdcEntityType whichType,
			IngridDocument inDoc, String importNodeUuid,
			String userUuid) {
		// first extract map of mapped uuids !
		HashMap<String, String> uuidMappingMap = getUuidMappingMap(userUuid);

		String inUuid = inDoc.getString(MdekKeys.UUID);
		String inOrigId = EntityHelper.getOrigIdFromDoc(whichType, inDoc);
		String inParentUuid = inDoc.getString(MdekKeys.PARENT_UUID);

		// process UUID

		// check whether entity exists. if so create new UUID to store new entity and remember mapping !
		String newUuid = inUuid;
		if (inUuid != null) {
			// first check map (maybe entity already mapped ? then included multiple times in import ?)
			if (uuidMappingMap.containsKey(inUuid)) {
				newUuid = uuidMappingMap.get(inUuid);
			} else {
				IEntity existingNode = null;
				if (whichType == IdcEntityType.OBJECT) {
					existingNode = objectService.loadByUuid(inUuid, null);
				} else if (whichType == IdcEntityType.ADDRESS) {
					existingNode = addressService.loadByUuid(inUuid, null);
				}
				if (existingNode != null) {
					// existing entity, new UUID will be created !
					newUuid = null;
				}
			}
		}
		// create new uuid to create new entity !
		if (newUuid == null) {
			newUuid = EntityHelper.getInstance().generateUuid();
		}
		inDoc.put(MdekKeys.UUID, newUuid);
		uuidMappingMap.put(inUuid, newUuid);
		
		// process ORIG_ID

		// check orig id and remove if not unique !
		String newOrigId = inOrigId;
		if (inOrigId != null) {
			IEntity existingNode = null;
			if (whichType == IdcEntityType.OBJECT) {
				existingNode = objectService.loadByOrigId(inOrigId,  null);
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingNode = addressService.loadByOrigId(inOrigId,  null);
			}
			if (existingNode != null) {
				String existingUuid = EntityHelper.getUuidFromNode(whichType, existingNode);
				// just to be sure: could be the same entity (when included multiple times !? )
				if (!newUuid.equals(existingUuid)) {
					// same orig id set in other entity, we remove this one
					newOrigId = null;
					inDoc.remove(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);
					inDoc.remove(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER);
					String tag = createEntityTag(whichType, inUuid, inOrigId, inParentUuid);
					updateImportJobInfoMessages(MSG_WARN + tag +
						"Remove ORIG_ID:" + inOrigId + ", already set in " + whichType + " UUID:" + existingUuid, userUuid);
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
			newParentUuid = importNodeUuid;
		}
		inDoc.put(MdekKeys.PARENT_UUID, newParentUuid);

		// log mapping if changed
		if (!MdekUtils.isEqual(inUuid, newUuid) ||
				!MdekUtils.isEqual(inOrigId, newOrigId) ||
				!MdekUtils.isEqual(inParentUuid, newParentUuid)) {
			String tag = createEntityTag(whichType, inUuid, inOrigId, inParentUuid);
			updateImportJobInfoMessages(tag +
					"MAPPED to UUID:" + newUuid + " ORIG_ID:" + newOrigId + " PARENT_UUID:" + newParentUuid, userUuid);
		}

		return inDoc;		
	}
	
	/** Checks whether given doc is FREE address and transforms it to PERSON under import node.
	 * Returns TRUE if this happened. */
	private boolean processFreeAddressToPerson(IngridDocument inDoc, String importNodeUuid, String userUuid) {
		Integer addrClass = (Integer) inDoc.get(MdekKeys.CLASS);
		if (AddressType.FREI.getDbValue().equals(addrClass)) {
			String tag = createEntityTag(IdcEntityType.ADDRESS, inDoc);
			updateImportJobInfoMessages(MSG_WARN + tag + "Changed from FREE to PERSON Address, store underneath import node", userUuid);
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
	 */
	private HashMap determineNode(IdcEntityType whichType,
			IngridDocument inDoc, String userUuid) {
		IEntity existingNode = null;
		Boolean storeWorkingVersion = false;

		String inUuid = inDoc.getString(MdekKeys.UUID);
		String inOrigId = EntityHelper.getOrigIdFromDoc(whichType, inDoc);
		String tag = createEntityTag(whichType, inUuid, inOrigId, inDoc.getString(MdekKeys.PARENT_UUID));

		// UUID has highest priority, load via UUID. if entity found then ignore ORIG_ID in doc (which may differ) !
		if (inUuid != null) {
			if (whichType == IdcEntityType.OBJECT) {
				existingNode = objectService.loadByUuid(inUuid, IdcEntityVersion.WORKING_VERSION);
				if (existingNode != null) {
					inDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, ((ObjectNode)existingNode).getT01ObjectWork().getOrgObjId());
				}
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingNode = addressService.loadByUuid(inUuid, IdcEntityVersion.WORKING_VERSION);
				if (existingNode != null) {
					inDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, ((AddressNode)existingNode).getT02AddressWork().getOrgAdrId());
				}
			}
		}
		// if UUID not found load via ORIG_ID and if found keep existing UUID AND existing PARENT !
		if (existingNode == null &&	inOrigId != null) {
			if (whichType == IdcEntityType.OBJECT) {
				existingNode = objectService.loadByOrigId(inOrigId, IdcEntityVersion.WORKING_VERSION);
			} else if (whichType == IdcEntityType.ADDRESS) {
				existingNode = addressService.loadByOrigId(inOrigId, IdcEntityVersion.WORKING_VERSION);
			}
			if (existingNode != null) {
				// uuid of import doesn't match with uuid in catalog, WE KEEP UUID IN CATALOG AND ALSO KEEP PARENT IN CATALOG !
				String existingUuid = EntityHelper.getUuidFromNode(whichType, existingNode);
				inDoc.put(MdekKeys.UUID, existingUuid);
				inDoc.put(MdekKeys.PARENT_UUID, EntityHelper.getParentUuidFromNode(whichType, existingNode));
				storeWorkingVersion = true;
				updateImportJobInfoMessages(MSG_WARN + tag +
					"UUID not found, but found ORIG_ID in existing " + whichType + " UUID:" + existingUuid, userUuid);
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
		}

		// set mod_uuid and responsible_uuid from existing object.
		if (existingNode != null) {
			String modUuid = null;
			String respUuid = null;

			// take over FROM WORKING VERSION.
			if (whichType == IdcEntityType.OBJECT) {
				T01Object existingObj = ((ObjectNode) existingNode).getT01ObjectWork();
				modUuid = existingObj.getModUuid();
				respUuid = existingObj.getResponsibleUuid();
			} else if (whichType == IdcEntityType.ADDRESS) {
				T02Address existingAddr = ((AddressNode) existingNode).getT02AddressWork();
				modUuid = existingAddr.getModUuid();
				respUuid = existingAddr.getResponsibleUuid();
			}
			// just to be sure: we take over only if set ???
//			modUuid = (modUuid == null) ? userUuid : modUuid;
//			respUuid = (respUuid == null) ? userUuid : respUuid;
			beanToDocMapper.mapModUser(modUuid, inDoc, MappingQuantity.INITIAL_ENTITY);
			beanToDocMapper.mapResponsibleUser(respUuid, inDoc, MappingQuantity.INITIAL_ENTITY);
		}

		HashMap retMap = new HashMap();
		retMap.put(TMP_FOUND_NODE, existingNode);
		retMap.put(TMP_STORE_WORKING_VERSION, storeWorkingVersion);

		return retMap;
	}

	/**
	 * Determine the "new" parent of the entity to import.  NOTICE: manipulates inDoc !
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
	private HashMap determineParentNode(IdcEntityType whichType,
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
					if (whichType == IdcEntityType.OBJECT) {
						newParentNode = objectService.loadByUuid(((ObjectNode)existingNode).getFkObjUuid(), null);
					} else if (whichType == IdcEntityType.ADDRESS) {
						newParentNode = addressService.loadByUuid(((AddressNode)existingNode).getFkAddrUuid(), null);
					}
				}
			} else {
				// import parent NOT set.  we keep "old" parent. NOTICE: if old parent is null we keep null (top node)
				if (whichType == IdcEntityType.OBJECT) {
					newParentNode = objectService.loadByUuid(((ObjectNode)existingNode).getFkObjUuid(), null);
				} else if (whichType == IdcEntityType.ADDRESS) {
					newParentNode = addressService.loadByUuid(((AddressNode)existingNode).getFkAddrUuid(), null);
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
					newParentNode = importNode;
					storeWorkingVersion = true;
				}
			} else {
				// import parent NOT set (NEW TOP NODE). store under import node.
				updateImportJobInfoMessages(MSG_WARN + tag + "New top node, store underneath import node", userUuid);
				newParentNode = importNode;
				storeWorkingVersion = true;
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
		HashMap<String, String> uuidMap = (HashMap<String, String>) runningJobInfo.get(KEY_SEPARATE_IMPORT_UUID_MAP);
		if (uuidMap == null) {
			uuidMap = new HashMap<String, String>();
			runningJobInfo.put(KEY_SEPARATE_IMPORT_UUID_MAP, uuidMap);
		}

		return uuidMap;
	}

	/**
	 * Move existing node to "new" parent node if parent in IMPORT differs and exists !
	 * @param whichType which type is this entity to import
	 * @param inDoc the entity to import represented by its doc.
	 * 		NOTICE: already processed ! data MUST fit to passed nodes ! 
	 * @param existingNode existing node like determined before. IF NULL non existent !
	 * @param parentNode new parent node like determined before. IF NULL then has to be existing top node.
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

		// create entity tag with "old" data (doc contains already new parent !)
		String tag = createEntityTag(whichType, inDoc.getString(MdekKeys.UUID),
			EntityHelper.getOrigIdFromDoc(whichType, inDoc),
			currentParentUuid);

		if (newParentUuid == null) {
			// "new" position is top node -> only possible if existing node is top node !
			if (currentParentUuid != null) {
				// ??? we can't move to top due to import ! should not happen !!! Log !!!
				throw createImportException(MSG_WARN + tag + "Can't move to TOP because of Import, we keep position");			
			}
		} else if (!newParentUuid.equals(currentParentUuid)) {
			// wemove :) !
			try {
				String uuidToMove = EntityHelper.getUuidFromNode(whichType, existingNode);

				if (whichType == IdcEntityType.OBJECT) {
					objectService.moveObject(uuidToMove, newParentUuid, false, userUuid, true);				
				} else if (whichType == IdcEntityType.ADDRESS) {
					addressService.moveAddress(uuidToMove, newParentUuid, false, userUuid, true);				
				}

				updateImportJobInfoMessages(tag + "Moved to new parent " + newParentUuid, userUuid);
			} catch (Exception ex) {
				// problems ! we set parent in doc to current parent to guarantee correct parent !
				inDoc.put(MdekKeys.PARENT_UUID, currentParentUuid);

				// and log
				String errorMsg = MSG_WARN + tag + "Problems moving to new parent " + newParentUuid + " : ";
				LOG.error(errorMsg, ex);
				updateImportJobInfoMessages(errorMsg + ex, userUuid);

				throw ex;
			}
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

			throw ex;
		}
	}

	/** PUBLISH entity. */
	private void processPublish(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode, IEntity parentNode,
			int numImported, int totalNum, String userUuid)
	throws Exception {
		// create tag and text for messages !
		String tag = createEntityTag(whichType, inDoc);
		String newEntityMsg = createNewEntityMsg(whichType, (existingNode == null));

		// first check whether publishing is possible with according parent
		// NOTICE: determined parent can be null -> update of existing top object 
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
				throw createImportException(msg);
			}
		}

		// ok, we publish !
		try {
			if (whichType == IdcEntityType.OBJECT) {
				// we DON'T force publication condition ! if error, we store working version !
				objectService.publishObject(inDoc, false, userUuid, true);
			} else if (whichType == IdcEntityType.ADDRESS) {
				addressService.publishAddress(inDoc, userUuid, true);
			}

			updateImportJobInfo(whichType, numImported+1, totalNum, userUuid);
			updateImportJobInfoMessages(tag + newEntityMsg + "PUBLISHED", userUuid);

		} catch (Exception ex) {
			String errorMsg = MSG_WARN + tag + "Problems publishing : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);

			throw ex;
		}
	}

	/** Store WORKING VERSION of entity. */
	private void processStoreWorkingCopy(IdcEntityType whichType,
			IngridDocument inDoc, IEntity existingNode,
			int numImported, int totalNum, boolean wasPublishImmediately,
			String userUuid)
	throws MdekException {
		// create tag and text for messages !
		String tag = createEntityTag(whichType, inDoc);
		// add warning if entity originally should be published !
		if (wasPublishImmediately) {
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

		} catch (Exception ex) {
			String errorMsg = MSG_WARN + tag + "Problems storing working version : ";
			LOG.error(errorMsg, ex);
			updateImportJobInfoMessages(errorMsg + ex, userUuid);

			// entity type specific stuff
			if (whichType == IdcEntityType.OBJECT) {
				// object was NOT persisted, we also remove remembered obj references of this object.
				String objUuid = inDoc.getString(MdekKeys.UUID);
				evictObjReferences(objUuid, userUuid);
			}

			throw createImportException(errorMsg + ex);			
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
				entityDoc.getString(MdekKeys.PARENT_UUID));
	}

	/** Creates entity tag to be displayed as entity "identifier". */
	private String createEntityTag(IdcEntityType whichType,
			String uuid, String origId, String parentUuid) {
		StringBuilder tag = new StringBuilder();
		if (whichType == IdcEntityType.OBJECT) {
			tag.append("Object");
		} else if (whichType == IdcEntityType.ADDRESS) {
			tag.append("Address");			
		}
		tag.append(" UUID:" + uuid);
		tag.append(" ORIG_ID:" + origId);
		tag.append(" PARENT_UUID:" + parentUuid);
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
}
