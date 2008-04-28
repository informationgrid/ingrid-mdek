package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.tools.MdekFullIndexHandler;
import de.ingrid.mdek.job.tools.MdekIdcEntityComparer;
import de.ingrid.mdek.job.tools.MdekPermissionHandler;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.UuidGenerator;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning OBJECTS. 
 */
public class MdekIdcObjectJob extends MdekIdcJob {

	protected MdekFullIndexHandler fullIndexHandler;
	private MdekPermissionHandler permissionHandler;

	private IObjectNodeDao daoObjectNode;
	private IT01ObjectDao daoT01Object;
	private IGenericDao<IEntity> daoObjectReference;

	public MdekIdcObjectJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcObjectJob.class), daoFactory);

		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService);

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoObjectReference = daoFactory.getDao(ObjectReference.class);
	}

	public IngridDocument getTopObjects(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();

			// fetch top Objects
			List<ObjectNode> oNs = daoObjectNode.getTopObjects();

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(oNs.size());
			for (ObjectNode oN : oNs) {
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(objDoc);
			}
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, resultList);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<ObjectNode> oNs = daoObjectNode.getSubObjects(uuid, true);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(oNs.size());
			for (ObjectNode oN : oNs) {
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(objDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, resultList);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectPath(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<String> uuidList = daoObjectNode.getObjectPath(uuid);

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.PATH, uuidList);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjDetails(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			if (log.isDebugEnabled()) {
				log.debug("Invoke getObjDetails (uuid='"+uuid+"').");
			}
			IngridDocument result = getObjDetails(uuid);
			
			daoObjectNode.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	private IngridDocument getObjDetails(String uuid) {
		// first get all "internal" object data (referenced addresses ...)
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument resultDoc = new IngridDocument();
		beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);
		
		// also map ObjectNode for published info
		beanToDocMapper.mapObjectNode(oNode, resultDoc, MappingQuantity.DETAIL_ENTITY);
	
		// then get "external" data (objects referencing the given object ...)
		List<ObjectNode>[] fromLists = daoObjectNode.getObjectReferencesFrom(uuid);
		beanToDocMapper.mapObjectReferencesFrom(fromLists, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);
		
		// get parent data
		ObjectNode pNode = daoObjectNode.getParent(uuid);
		if (pNode != null) {
			beanToDocMapper.mapObjectParentData(pNode.getT01ObjectWork(), resultDoc);
		}

		return resultDoc;
	}

	public IngridDocument getInitialObject(IngridDocument oDocIn) {
		try {
			daoObjectNode.beginTransaction();
			
			// take over data from parent (if set)
			String parentUuid = oDocIn.getString(MdekKeys.PARENT_UUID);
			if (parentUuid != null) {
				ObjectNode pNode = daoObjectNode.loadByUuid(parentUuid);
				if (pNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}

				T01Object oParent = pNode.getT01ObjectWork();

				// supply separate parent info
				beanToDocMapper.mapObjectParentData(oParent, oDocIn);

				// take over initial data from parent
				beanToDocMapper.mapT01Object(oParent, oDocIn, MappingQuantity.INITIAL_ENTITY);
			}

			// take over spatial reference from catalog
			T03Catalogue catalog = catalogHandler.getCatalog();
			IngridDocument catalogDoc = new IngridDocument();
			beanToDocMapper.mapT03Catalog(catalog, catalogDoc);

			IngridDocument catDocLoc = (IngridDocument) catalogDoc.get(MdekKeys.CATALOG_LOCATION);
			if (catDocLoc != null) {
				ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(1);
				locList.add(catDocLoc);					
				oDocIn.put(MdekKeys.LOCATIONS, locList);
			}

			daoObjectNode.commitTransaction();
			return oDocIn;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeObject(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoObjectNode.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			String uuid = (String) oDocIn.get(MdekKeys.UUID);
			// NOTICE: parent may be null, then root node !
			String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
			boolean isRootNode = (parentUuid == null) ? true : false;
			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer to working copy !
			oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
			
			boolean isNewObject = false;
			if (uuid == null) {
				// NEW Object !
				isNewObject = true;

				// check User Permissions when new root node !
				if (isRootNode) {
					permissionHandler.checkCreateRootPermission(userId);					
				}

				// create new uuid
				uuid = UuidGenerator.getInstance().generateUuid();
				oDocIn.put(MdekKeys.UUID, uuid);
			}
			
			// load node
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				oNode = docToBeanMapper.mapObjectNode(oDocIn, new ObjectNode());			
			}
			
			// get/create working copy
			if (!hasWorkingCopy(oNode)) {
				// no working copy yet, create new object with BASIC data

				// set some missing data which may not be passed from client.
				// set from published version if existent
				T01Object oPub = oNode.getT01ObjectPublished();
				if (oPub != null) {
					oDocIn.put(MdekKeys.DATE_OF_CREATION, oPub.getCreateTime());				
				} else {
					oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
				}
				T01Object oWork = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
				 // save it to generate id needed for mapping
				daoT01Object.makePersistent(oWork);
				
				// update node
				oNode.setObjId(oWork.getId());
				oNode.setT01ObjectWork(oWork);
				daoObjectNode.makePersistent(oNode);
			}

			// transfer new data and store.
			T01Object oWork = oNode.getT01ObjectWork();
			docToBeanMapper.mapT01Object(oDocIn, oWork, MappingQuantity.DETAIL_ENTITY);
			daoT01Object.makePersistent(oWork);

			// UPDATE FULL INDEX !!!
			fullIndexHandler.updateObjectIndex(oNode);

			// grant write tree permission if new root node (no permission yet)
			if (isNewObject && isRootNode) {
				permissionHandler.grantWriteTreePermissionForObject(oNode.getObjUuid(), userId);
			}

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !
			daoObjectNode.commitTransaction();

			// return uuid (may be new generated uuid if new object)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid);
				daoObjectNode.commitTransaction();
				
				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument publishObject(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_PUBLISH, 0, 1, false));

			daoObjectNode.beginTransaction();

			// uuid is null when new object !
			String uuid = (String) oDocIn.get(MdekKeys.UUID);
			// parentUuid only passed if new object !
			String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);

			Integer pubTypeIn = (Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION);
			Boolean forcePubCondition = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);
			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			String currentTime = MdekUtils.dateToTimestamp(new Date()); 

			// PERFORM CHECKS
			// NOTICE: passed object may NOT exist yet (new object published immediately)

			// all parents published ?
			checkObjectPathForPublish(parentUuid, uuid);
			// publication condition of parent fits to object ?
			checkObjectPublicationConditionParent(parentUuid, uuid, pubTypeIn);
			// publication conditions of sub nodes fit to object ?
			checkObjectPublicationConditionSubTree(uuid, pubTypeIn, forcePubCondition, true,
				currentTime, userId);

			// CHECKS OK, proceed

			// set common data to transfer
			oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			oDocIn.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());

			if (uuid == null) {
				// NEW NODE !!!!
				// create new uuid
				uuid = UuidGenerator.getInstance().generateUuid();
				oDocIn.put(MdekKeys.UUID, uuid);
			}

			// load node
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				oNode = docToBeanMapper.mapObjectNode(oDocIn, new ObjectNode());			
			}
			
			// get/create published version
			T01Object oPub = oNode.getT01ObjectPublished();
			if (oPub == null) {
				// set some missing data which may not be passed from client.
				oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
				
				// create new object with BASIC data
				oPub = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
				 // save it to generate id needed for mapping
				daoT01Object.makePersistent(oPub);
			}

			// transfer new data and store.
			docToBeanMapper.mapT01Object(oDocIn, oPub, MappingQuantity.DETAIL_ENTITY);
			daoT01Object.makePersistent(oPub);
			Long oPubId = oPub.getId();

			// and update ObjectNode

			// delete former working copy if set
			T01Object oWork = oNode.getT01ObjectWork();
			if (oWork != null && !oPubId.equals(oWork.getId())) {
				// delete working version
				daoT01Object.makeTransient(oWork);
			}
			
			// set data on node, also beans, so we can access them afterwards (index)
			oNode.setObjId(oPubId);
			oNode.setT01ObjectWork(oPub);
			oNode.setObjIdPublished(oPubId);
			oNode.setT01ObjectPublished(oPub);
			daoObjectNode.makePersistent(oNode);
			
			// UPDATE FULL INDEX !!!
			fullIndexHandler.updateObjectIndex(oNode);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !
			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid);
				daoObjectNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after publish/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 */
	public IngridDocument deleteObjectWorkingCopy(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoObjectNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			// first check User Permissions
			permissionHandler.checkWritePermissionForObject(uuid, userId);

			// NOTICE: this one also contains Parent Association !
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}

			boolean performFullDelete = false;
			Long idPublished = oNode.getObjIdPublished();
			Long idWorkingCopy = oNode.getObjId();

			// if we have NO published version -> delete complete node !
			IngridDocument result = new IngridDocument();
			if (idPublished == null) {
				performFullDelete = true;
			} else {
				result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);			

				// perform delete of working copy only if really different version
				if (!idPublished.equals(idWorkingCopy)) {
					// remove already fetched working copy from node 
					T01Object oWorkingCopy = oNode.getT01ObjectWork();
//					oNode.setObjId(null);
//					oNode.setT01ObjectWork(null);
					// and delete it
					daoT01Object.makeTransient(oWorkingCopy);
					
					// and set published one as working copy
					oNode.setObjId(idPublished);
					oNode.setT01ObjectWork(oNode.getT01ObjectPublished());
					daoObjectNode.makePersistent(oNode);
					
					// UPDATE FULL INDEX !!!
					fullIndexHandler.updateObjectIndex(oNode);
					
				}
			}

			if (performFullDelete) {
				result = deleteObject(uuid, forceDeleteReferences);
			}

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object is non existent afterwards !
	 */
	public IngridDocument deleteObject(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoObjectNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			IngridDocument result = deleteObject(uuid, forceDeleteReferences);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	private IngridDocument deleteObject(String uuid, boolean forceDeleteReferences) {
		// NOTICE: this one also contains Parent Association !
		ObjectNode oNode = daoObjectNode.loadByUuid(uuid);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		checkObjectSubTreeReferences(oNode, forceDeleteReferences);

		// delete complete Node ! rest is deleted per cascade !
		daoObjectNode.makeTransient(oNode);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);

		return result;
	}

	/** Move Object with its subtree to new parent. */
	public IngridDocument moveObject(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_MOVE, 0, 1, false));

			Boolean forcePubCondition = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);
			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);

			daoObjectNode.beginTransaction();

			// PERFORM CHECKS

			ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);
			checkObjectNodesForMove(fromNode, toUuid, forcePubCondition, userId);

			// CHECKS OK, proceed

			// set new parent, may be null, then top node !
			fromNode.setFkObjUuid(toUuid);		
			daoObjectNode.makePersistent(fromNode);

			// change date and mod_uuid of all moved nodes !
			IngridDocument result = processMovedNodes(fromNode, userId);

			daoObjectNode.commitTransaction();
			return result;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * Process the moved tree, meaning set modification date and user in every node (in
	 * published and working version !).
	 * @param rootNode root node of moved tree
	 * @param modUuid user uuid to set as modification user
	 * @return doc containing additional info (number processed nodes ...)
	 */
	private IngridDocument processMovedNodes(ObjectNode rootNode, String modUuid)
	{
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// copy iteratively via stack to avoid recursive stack overflow
		Stack<ObjectNode> stack = new Stack<ObjectNode>();
		stack.push(rootNode);

		int numberOfProcessedObj = 0;
		while (!stack.isEmpty()) {
			ObjectNode objNode = stack.pop();
			
			// set modification time and user (in both versions when present)
			T01Object objWork = objNode.getT01ObjectWork();
			T01Object objPub = objNode.getT01ObjectPublished();

			// check whether we have a different published version !
			boolean hasDifferentPublishedVersion = false;
			if (objPub != null && objWork.getId() != objPub.getId()) {
				hasDifferentPublishedVersion = true;
			}

			// change mod time and uuid
			objWork.setModTime(currentTime);
			objWork.setModUuid(modUuid);
			if (hasDifferentPublishedVersion) {
				objPub.setModTime(currentTime);
				objPub.setModUuid(modUuid);				
			}

			daoT01Object.makePersistent(objWork);
			if (hasDifferentPublishedVersion) {
				daoT01Object.makePersistent(objPub);
			}
			numberOfProcessedObj++;

			List<ObjectNode> subNodes = daoObjectNode.getSubObjects(objNode.getObjUuid(), true);
			for (ObjectNode subNode : subNodes) {
				// add to stack, will be processed
				stack.push(subNode);
			}
		}
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfProcessedObj);
		if (log.isDebugEnabled()) {
			log.debug("Number of moved objects: " + numberOfProcessedObj);
		}

		return result;
	}

	/** Checks whether subtree of object has working copies. */
	public IngridDocument checkObjectSubTree(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_CHECK, 0, 1, false));

			String rootUuid = (String) params.get(MdekKeys.UUID);

			daoObjectNode.beginTransaction();

			IngridDocument checkResult = checkObjectSubTreeWorkingCopies(rootUuid);

			daoObjectNode.commitTransaction();
			return checkResult;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Checks whether subtree of object has working copies. */
	private IngridDocument checkObjectSubTreeWorkingCopies(String rootUuid) {
		// load "root"
		ObjectNode rootNode = daoObjectNode.loadByUuid(rootUuid);
		if (rootNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// traverse iteratively via stack
		Stack<ObjectNode> stack = new Stack<ObjectNode>();
		stack.push(rootNode);

		boolean hasWorkingCopy = false;
		String uuidOfWorkingCopy = null;
		int numberOfCheckedObj = 0;
		while (!hasWorkingCopy && !stack.isEmpty()) {
			ObjectNode node = stack.pop();
			
			// check
			numberOfCheckedObj++;
			if (!node.getObjId().equals(node.getObjIdPublished())) {
				hasWorkingCopy = true;
				uuidOfWorkingCopy = node.getObjUuid();
			}

			if (!hasWorkingCopy) {
				List<ObjectNode> subNodes =
					daoObjectNode.getSubObjects(node.getObjUuid(), false);
				for (ObjectNode subNode : subNodes) {
					stack.push(subNode);
				}					
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Number of checked objects: " + numberOfCheckedObj);
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_HAS_WORKING_COPY, hasWorkingCopy);
		result.put(MdekKeys.RESULTINFO_UUID_OF_FOUND_ENTITY, uuidOfWorkingCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCheckedObj);

		return result;		
	}

	/** Copy Object to new parent (with or without its subtree). Returns basic data of copied top object. */
	public IngridDocument copyObject(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_COPY, 0, 1, false));

			daoObjectNode.beginTransaction();

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);

			// perform checks
			ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);
			if (fromNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
			}

			ObjectNode toNode = null;
			// NOTICE: copy to top when toUuid is null
			if (toUuid != null) {
				toNode = daoObjectNode.loadByUuid(toUuid);
				if (toNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
				}
			}

			// copy fromNode
			IngridDocument copyResult = createObjectNodeCopy(fromNode, toNode, copySubtree, userId);
			ObjectNode fromNodeCopy = (ObjectNode) copyResult.get(MdekKeys.OBJ_ENTITIES);
			Integer numCopiedObjects = (Integer) copyResult.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES);
			if (log.isDebugEnabled()) {
				log.debug("Number of copied objects: " + numCopiedObjects);
			}

			// success
			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapT01Object(fromNodeCopy.getT01ObjectWork(), resultDoc, MappingQuantity.TABLE_ENTITY);
			// also child info
			beanToDocMapper.mapObjectNode(fromNodeCopy, resultDoc, MappingQuantity.COPY_ENTITY);
			// and additional info
			resultDoc.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numCopiedObjects);

			daoObjectNode.commitTransaction();
			return resultDoc;		
		
		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Check whether passed nodes are valid for move operation
	 * (e.g. move to subnode not allowed). Throws MdekException if not valid.
	 */
	private void checkObjectNodesForMove(ObjectNode fromNode, String toUuid,
		Boolean forcePubCondition,
		String userId)
	{
		if (fromNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
		}		
		String fromUuid = fromNode.getObjUuid();

		// NOTICE: top node when toUuid = null
		if (toUuid != null) {
			// load toNode
			ObjectNode toNode = daoObjectNode.loadByUuid(toUuid);
			if (toNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
			}		

			// new parent has to be published ! -> not possible to move published nodes under unpublished parent
			T01Object toObjPub = toNode.getT01ObjectPublished();
			if (toObjPub == null) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}

			// is target subnode ?
			if (daoObjectNode.isSubNode(toUuid, fromUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.TARGET_IS_SUBNODE_OF_SOURCE));				
			}
			
			// are pubTypes compatible ?
			// we check and adapt ONLY PUBLISHED version !!!
			Integer publicationTypeTo = toObjPub.getPublishId();
			// adapt all child nodes if requested !
			String currentTime = MdekUtils.dateToTimestamp(new Date()); 
			checkObjectPublicationConditionSubTree(fromUuid, publicationTypeTo, forcePubCondition, false,
				currentTime, userId);
		}
	}

	/** Checks whether node has unpublished parents. Throws MdekException if so. */
	private void checkObjectPathForPublish(String parentUuid, String inUuid) {
		// default
		String endOfPath = inUuid;
		boolean includeEndOfPath = false;

		// new node (uuid not generated yet) ? parent should be passed !
		if (inUuid == null) {
			endOfPath = parentUuid;
			includeEndOfPath = true;
		}
		
		// no check if top node !
		if (endOfPath == null) {
			return;
		}
		
		// check whether a parent is not published

		List<String> pathUuids = daoObjectNode.getObjectPath(endOfPath);
		for (String pathUuid : pathUuids) {
			if (pathUuid.equals(endOfPath) && !includeEndOfPath) {
				continue;
			}
			ObjectNode pathNode = daoObjectNode.loadByUuid(pathUuid);
			if (pathNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			
			// check
			if (pathNode.getObjIdPublished() == null) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}
		}
	}

	/** Check whether publication condition of parent fits to publication condition of child.<br>
	 * NOTICE: PublishedVersion of parent is checked !<br>
	 * Throws Exception if not fitting */
	private void checkObjectPublicationConditionParent(String parentUuid,
			String childUuid,
			Integer pubTypeChildDB) {

		PublishType pubTypeChild = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeChildDB);

		// Load Parent from child
		// NOTICE: childUuid can be null if uuid not generated yet (new object)
		if (parentUuid == null) {
			// if childUuid is null then we have a new top object !
			if (childUuid != null) {
				parentUuid = daoObjectNode.loadByUuid(childUuid).getFkObjUuid();				
			}
		}
		// return if top node
		if (parentUuid == null) {
			return;
		}
		T01Object parentObjPub = daoObjectNode.loadByUuid(parentUuid).getT01ObjectPublished();
		if (parentObjPub == null) {
			throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
		}
		
		// get publish type of parent
		PublishType pubTypeParent = EnumUtil.mapDatabaseToEnumConst(PublishType.class, parentObjPub.getPublishId());

		// check whether publish type of parent is smaller
		if (!pubTypeParent.includes(pubTypeChild)) {
			throw new MdekException(new MdekError(MdekErrorType.PARENT_HAS_SMALLER_PUBLICATION_CONDITION));					
		}
	}

	/** Checks whether a tree fits to a new publication condition.
	 * !!! ALSO ADAPTS Publication Conditions IF REQUESTED !!!<br>
	 * NOTICE: ONLY PUBLISHED versions of subnodes are checked and adapted !!!
	 * @param topUuid uuid of top node of tree
	 * @param pubTypeTopDB new publication type
	 * @param forcePubCondition force change of nodes (modification time, publicationType, ...)
	 * @param skipTopNode check/change top node, e.g. when moving (true) or not, e.g. when publishing (false)
	 * @param modTime modification time to store in modified nodes
	 * @param modUuid user uuid to set as modification user
	 */
	private void checkObjectPublicationConditionSubTree(String topUuid,
			Integer pubTypeTopDB,
			Boolean forcePubCondition,
			boolean skipTopNode,
			String modTime,
			String modUuid) {

		// no check if new object ! No children !
		if (topUuid == null) {
			return;
		}

		// get current pub type. Should be set !!! (mandatory when publishing)
		PublishType pubTypeNew = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeTopDB);		
		ObjectNode topNode = daoObjectNode.loadByUuid(topUuid);
		String topName = topNode.getT01ObjectWork().getObjName();

		// should we adapt all subnodes ?
		if (!Boolean.TRUE.equals(forcePubCondition)) {
			forcePubCondition = false;			
		}

		// traverse iteratively via stack
		Stack<ObjectNode> stack = new Stack<ObjectNode>();
		stack.push(topNode);
		while (!stack.isEmpty()) {
			ObjectNode treeNode = stack.pop();
			
			// skip top node ?
			boolean processNode = true;
			boolean isTopNode = false;
			if (treeNode.equals(topNode)) {
				isTopNode = true;
				if (skipTopNode) {
					processNode = false;
				}
			}
			
			if (processNode) {

				// check whether publication condition of node is "critical"
				T01Object objPub = treeNode.getT01ObjectPublished();
				if (objPub == null) {
					// not published yet ! skip this one
					continue;
				}
				PublishType objPubType = EnumUtil.mapDatabaseToEnumConst(PublishType.class, objPub.getPublishId());				
				boolean objIsCritical = !pubTypeNew.includes(objPubType);

				if (objIsCritical) {
					// throw "warning" for user when not adapting sub tree !
					if (!forcePubCondition) {
						throw new MdekException(new MdekError(MdekErrorType.SUBTREE_HAS_LARGER_PUBLICATION_CONDITION));					
					}

					// nodes should be adapted -> in PublishedVersion !
					objPub.setPublishId(pubTypeNew.getDbValue());
					// set time and user
					objPub.setModTime(modTime);
					objPub.setModUuid(modUuid);
					
					// add comment to SUB OBJECT, document the automatic change of the publish condition
					if (!isTopNode) {
						Set<ObjectComment> commentSet = objPub.getObjectComments();
						ObjectComment newComment = new ObjectComment();
						newComment.setObjId(objPub.getId());
						newComment.setComment("Hinweis: Durch Änderung des Wertes des Feldes 'Veröffentlichung' im " +
							"übergeordneten Objekt '" + topName +
							"' ist der Wert dieses Feldes für dieses Objekt auf '" + pubTypeNew.toString() +
							"' gesetzt worden.");
						newComment.setCreateTime(objPub.getModTime());
						newComment.setCreateUuid(objPub.getModUuid());
						commentSet.add(newComment);
						daoT01Object.makePersistent(objPub);						
					}
				}
			}
			
			// add next level of subnodes to stack
			List<ObjectNode> subNodes = daoObjectNode.getSubObjects(treeNode.getObjUuid(), true);
			for (ObjectNode sN : subNodes) {
				stack.push(sN);
			}					
		}
	}

	/**
	 * Checks whether object node is referenced by other objects.
	 * All references to node are taken into account, no matter whether from a working or a published version !
	 * @param oNode object to check
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkObjectNodeReferences(ObjectNode oNode, boolean forceDeleteReferences) {
		// handle references to object
		String oUuid = oNode.getObjUuid();
		ObjectReference exampleRef = new ObjectReference();
		exampleRef.setObjToUuid(oUuid);
		List<IEntity> objRefs = daoObjectReference.findByExample(exampleRef);
		
		// throw exception with detailed errors when address referenced without reference deletion !
		if (!forceDeleteReferences) {
			int numRefs = objRefs.size();
			if (numRefs > 0) {
				// existing references -> throw exception with according error info !

				// add info about referenced object
				IngridDocument errInfo =
					beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), new IngridDocument(), MappingQuantity.BASIC_ENTITY);

				// add info about objects referencing !
				ArrayList<IngridDocument> objList = new ArrayList<IngridDocument>(numRefs);
				for (IEntity ent : objRefs) {
					ObjectReference ref = (ObjectReference) ent;
					// fetch object referencing the address
					T01Object o = daoT01Object.getById(ref.getObjFromId());
					IngridDocument objInfo =
						beanToDocMapper.mapT01Object(o, new IngridDocument(), MappingQuantity.BASIC_ENTITY);
					objList.add(objInfo);
				}
				errInfo.put(MdekKeys.OBJ_ENTITIES, objList);

				// and throw exception encapsulating errors
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_REFERENCED_BY_OBJ, errInfo));
			}
		}
		
		// delete references (querverweise)
		for (IEntity objRef : objRefs) {
			daoObjectReference.makeTransient(objRef);
		}
	}

	/**
	 * Checks whether object tree contains nodes referenced by other objects.
	 * All references to a node are taken into account, no matter whether from a working or a published version !
	 * @param topNode top node of tree to check (included in check !)
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkObjectSubTreeReferences(ObjectNode topNode, boolean forceDeleteReferences) {
		// traverse iteratively via stack
		Stack<ObjectNode> stack = new Stack<ObjectNode>();
		stack.push(topNode);

		while (!stack.isEmpty()) {
			ObjectNode node = stack.pop();
			
			// check
			checkObjectNodeReferences(node, forceDeleteReferences);

			List<ObjectNode> subNodes =
				daoObjectNode.getSubObjects(node.getObjUuid(), false);
			for (ObjectNode subNode : subNodes) {
				stack.push(subNode);
			}					
		}
	}

	/**
	 * Creates a copy of the given ObjectNode and adds it under the given parent.
	 * Copies ONLY working version and IGNORES published version !
	 * Copied nodes are already Persisted !!!<br>
	 * NOTICE: Also copies whole subtree dependent from passed flag.<br>
	 * NOTICE: also supports copy of a tree to one of its subnodes !
	 * @param sourceNode copy this node
	 * @param newParentNode under this node
	 * @param copySubtree including subtree or not
	 * @param userId current user id needed to update running jobs
	 * @return doc containing additional info (copy of source node, number copied objects ...)
	 */
	private IngridDocument createObjectNodeCopy(ObjectNode sourceNode, ObjectNode newParentNode,
			boolean copySubtree, String userId)
	{
		// refine running jobs info
		int totalNumToCopy = 1;
		if (copySubtree) {
			totalNumToCopy = daoObjectNode.countSubObjects(sourceNode.getObjUuid());
			updateRunningJob(userId, createRunningJobDescription(JOB_DESCR_COPY, 0, totalNumToCopy, false));				
		}

		// check whether we copy to subnode
		// then we have to check already copied nodes to avoid endless copy !
		boolean isCopyToOwnSubnode = false;
		ArrayList<String> uuidsCopiedNodes = null;
		if (newParentNode != null) {
			if (daoObjectNode.isSubNode(newParentNode.getObjUuid(), sourceNode.getObjUuid())) {
				isCopyToOwnSubnode = true;
				uuidsCopiedNodes = new ArrayList<String>();
			}
		}

		// copy iteratively via stack to avoid recursive stack overflow
		Stack<IngridDocument> stack = new Stack<IngridDocument>();
		IngridDocument nodeDoc = new IngridDocument();
		nodeDoc.put("NODE", sourceNode);
		nodeDoc.put("PARENT_NODE", newParentNode);
		stack.push(nodeDoc);

		int numberOfCopiedObj = 0;
		ObjectNode rootNodeCopy = null;
		while (!stack.isEmpty()) {
			nodeDoc = stack.pop();
			sourceNode = (ObjectNode) nodeDoc.get("NODE");
			newParentNode = (ObjectNode) nodeDoc.get("PARENT_NODE");

			if (log.isDebugEnabled()) {
				log.debug("Copying entity " + sourceNode.getObjUuid());
			}

			// copy source work version !
			String newUuid = UuidGenerator.getInstance().generateUuid();
			T01Object targetObjWork = createT01ObjectCopy(sourceNode.getT01ObjectWork(), newUuid);
			// set in Bearbeitung !
			targetObjWork.setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());

			// create new Node and set data !
			// we also set Beans in object node, so we can access them afterwards.
			Long targetObjWorkId = targetObjWork.getId();
			String newParentUuid = null;
			if (newParentNode != null) {
				newParentUuid = newParentNode.getObjUuid();
			}
			
			ObjectNode targetNode = new ObjectNode();
			targetNode.setObjUuid(newUuid);
			targetNode.setObjId(targetObjWorkId);
			targetNode.setT01ObjectWork(targetObjWork);
			targetNode.setFkObjUuid(newParentUuid);
			daoObjectNode.makePersistent(targetNode);
			numberOfCopiedObj++;
			// update our job information ! may be polled from client !
			// NOTICE: also checks whether job was canceled !
			updateRunningJob(userId, createRunningJobDescription(
				JOB_DESCR_COPY, numberOfCopiedObj, totalNumToCopy, false));

			if (rootNodeCopy == null) {
				rootNodeCopy = targetNode;
			}

			if (isCopyToOwnSubnode) {
				uuidsCopiedNodes.add(newUuid);
			}
			
			// add child bean to parent bean, so we can determine child info when mapping (without reloading)
			if (newParentNode != null) {
				newParentNode.getObjectNodeChildren().add(targetNode);
			}

			// copy subtree ? only if not already a copied node !
			if (copySubtree) {
				List<ObjectNode> sourceSubNodes = daoObjectNode.getSubObjects(sourceNode.getObjUuid(), true);
				for (ObjectNode sourceSubNode : sourceSubNodes) {
					if (isCopyToOwnSubnode) {
						if (uuidsCopiedNodes.contains(sourceSubNode.getObjUuid())) {
							// skip this node ! is one of our copied ones !
							continue;
						}
					}
					
					// add to stack, will be copied
					nodeDoc = new IngridDocument();
					nodeDoc.put("NODE", sourceSubNode);
					nodeDoc.put("PARENT_NODE", targetNode);
					stack.push(nodeDoc);
				}
			}
		}
		
		IngridDocument result = new IngridDocument();
		// copy of rootNode returned via OBJ_ENTITIES key !
		result.put(MdekKeys.OBJ_ENTITIES, rootNodeCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCopiedObj);

		return result;
	}

	/**
	 * Creates a copy of the given T01Object with the given NEW uuid. Already Persisted !
	 */
	private T01Object createT01ObjectCopy(T01Object sourceObj, String newUuid) {
		// create new object with new uuid and save it (to generate id !)
		T01Object targetObj = new T01Object();
		targetObj.setObjUuid(newUuid);
		daoT01Object.makePersistent(targetObj);

		// then copy content via mappers
		
		// map source bean to doc
		IngridDocument sourceObjDoc =
			beanToDocMapper.mapT01Object(sourceObj, new IngridDocument(), MappingQuantity.COPY_ENTITY);
		
		// update new data in doc !
		sourceObjDoc.put(MdekKeys.UUID, newUuid);

		// and transfer data from doc to new bean
		docToBeanMapper.mapT01Object(sourceObjDoc, targetObj, MappingQuantity.COPY_ENTITY);

		daoT01Object.makePersistent(targetObj);

		return targetObj;
	}

	private boolean hasWorkingCopy(ObjectNode oNode) {
		Long oWorkId = oNode.getObjId(); 
		Long oPubId = oNode.getObjIdPublished(); 
		if (oWorkId == null || oWorkId.equals(oPubId)) {
			return false;
		}
		
		return true;
	}

	/** Create Working Copy in given node, meaning the published object is copied and set "In Bearbeitung".<br>
	 * NOTICE: published object has to exist, so don't use this method when generating a NEW Node !<br>
	 * NOTICE: ALREADY PERSISTED (Node and T01Object !) */
/*
	private T01Object createWorkingCopyFromPublished(ObjectNode oNode) {
		if (!hasWorkingCopy(oNode)) {
			T01Object objWork = createT01ObjectCopy(oNode.getT01ObjectPublished(), oNode.getObjUuid());
			// set in Bearbeitung !
			objWork.setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());
			
			oNode.setObjId(objWork.getId());
			oNode.setT01ObjectWork(objWork);
			daoObjectNode.makePersistent(oNode);			
		}
		
		return oNode.getT01ObjectWork();
	}
*/
}
