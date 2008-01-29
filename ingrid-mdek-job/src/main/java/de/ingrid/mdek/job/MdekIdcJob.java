package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekErrorHandler;
import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekErrors.MdekError;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.UuidGenerator;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.persistence.db.model.IMapper.MappingQuantity;
import de.ingrid.utils.IngridDocument;

public class MdekIdcJob extends MdekJob {

    /** Logger configured via Properties. ONLY if no logger via logservice is specified
     * for same class !. If Logservice logger is specified, this one uses
     * Logservice configuration -> writes to separate logfile for this Job. */
//    private final static Log log = LogFactory.getLog(MdekTreeJob.class);

	/** logs in separate File (job specific log file) */
	protected Logger log;

	private IObjectNodeDao daoObjectNode;
	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;
	private ISpatialRefValueDao daoSpatialRefValue;
	private ISysListDao daoSysList;
	private IGenericDao daoT03Catalog;
	private BeanToDocMapper beanToDocMapper;
	private DocToBeanMapper docToBeanMapper;

	private MdekErrorHandler errorHandler;

	public MdekIdcJob(ILogService logService,
			DaoFactory daoFactory) {
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekIdcJob.class); 

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
		daoSysList = daoFactory.getSysListDao();
		daoT03Catalog = daoFactory.getDao(T03Catalogue.class);

		beanToDocMapper = BeanToDocMapper.getInstance();
		docToBeanMapper = docToBeanMapper.getInstance(daoFactory);

		errorHandler = MdekErrorHandler.getInstance();

	}
/*
	public IngridDocument testMdekEntity(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		// fetch parameters
		String name = (String) params.get(MdekKeys.TITLE);
		String descr = (String) params.get(MdekKeys.ABSTRACT);
		Integer threadNumber = (Integer) params.get("THREAD_NUMBER");

		T01Object objTemplate = new T01Object();
		objTemplate.setObjName(name);
		objTemplate.setObjDescr(descr);
		
		daoT01Object.beginTransaction();

		List<T01Object> objs = daoT01Object.findByExample(objTemplate);

		// thread 1 -> WAIT so we can test staled Object
		if (threadNumber == 1) {
			// wait time in ms
			long waitTime = 1000;
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < waitTime) {
				// do nothing
			}
		}

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		if (objs.size() > 0) {
			for (T01Object o : objs) {
				Integer oClass = o.getObjClass();
				oClass = (oClass == null ? 1 : oClass+1);
				o.setObjClass(oClass);
				
				if (threadNumber == 1) {
					// test update/deletion of staled Object !
		            log.debug("Thread 1 DELETING OBJECT:" + o.getId());
					daoT01Object.makeTransient(o);
//		            log.debug("Thread 1 UPDATE OBJECT:" + o.getId());
//					daoT01Object.makePersistent(o);
				} else {
					daoT01Object.makePersistent(o);
				}
				resultList.add(mapper.mapT01Object(o, MappingQuantity.DETAIL_ENTITY));
			}			
		} else {
			daoT01Object.makePersistent(objTemplate);
			
			T01Object o = daoT01Object.loadById(objTemplate.getId());
			resultList.add(mapper.mapT01Object(o, MappingQuantity.DETAIL_ENTITY));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);

		return result;
	}
*/
	public IngridDocument getSysLists(IngridDocument params) {
		try {
			daoSysList.beginTransaction();
			Integer[] lstIds = (Integer[]) params.get(MdekKeys.SYS_LIST_IDS);
			Integer langCode = (Integer) params.get(MdekKeys.LANGUAGE_CODE);

			IngridDocument result = new IngridDocument();
			
			for (int lstId : lstIds) {
				List<SysList> list = daoSysList.getSysList(lstId, langCode);
				
				IngridDocument listDoc = new IngridDocument();
				beanToDocMapper.mapSysList(list, lstId, listDoc);
				
				result.put(MdekKeys.SYS_LIST_KEY_PREFIX + lstId,  listDoc);
			}

			daoSysList.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoSysList.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}
	
	public IngridDocument getCatalog() {
		try {
			daoT03Catalog.beginTransaction();

			// fetch catalog
			T03Catalogue catalog = (T03Catalogue) daoT03Catalog.findFirst();
			if (catalog == null) {
				throw new MdekException(MdekError.CATALOG_NOT_FOUND);
			}

			IngridDocument result = new IngridDocument();
			beanToDocMapper.mapT03Catalog(catalog, result);

			daoT03Catalog.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoT03Catalog.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getTopObjects() {
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
		String uuid = (String) params.get(MdekKeys.UUID);
		if (log.isDebugEnabled()) {
			log.debug("Invoke getObjDetails (uuid='"+uuid+"').");
		}
		return getObjDetails(uuid);
	}

	private IngridDocument getObjDetails(String uuid) {
		try {
			daoObjectNode.beginTransaction();

			// first get all "internal" object data (referenced addresses ...)
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
			}

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);
			
			// also map ObjectNode for published info
			beanToDocMapper.mapObjectNode(oNode, resultDoc, MappingQuantity.DETAIL_ENTITY);
		
			// then get "external" data (objects referencing the given object ...)
			List<ObjectNode> oNs = daoObjectNode.getObjectReferencesFrom(uuid);
			beanToDocMapper.mapObjectReferencesFrom(oNs, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);
			
			// get parent data
			ObjectNode pNode = daoObjectNode.getParent(uuid);
			if (pNode != null) {
				beanToDocMapper.mapParentData(pNode.getT01ObjectWork(), resultDoc);
			}

			daoObjectNode.commitTransaction();
			return resultDoc;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getInitialObject(IngridDocument oDocIn) {
		try {
			daoObjectNode.beginTransaction();
			
			// take over "thesaurus" searchterms from parent
			String parentUuid = oDocIn.getString(MdekKeys.PARENT_UUID);
			if (parentUuid != null) {
				List<IngridDocument> termDocs = daoObjectNode.getObjectThesaurusTerms(parentUuid);
				oDocIn.put(MdekKeys.SUBJECT_TERMS, termDocs);
			}

			// take over spatial reference from catalog
			T03Catalogue catalog = (T03Catalogue) daoT03Catalog.findFirst();
			if (catalog == null) {
				throw new MdekException(MdekError.CATALOG_NOT_FOUND);
			}

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
		try {
			daoObjectNode.beginTransaction();

			String uuid = (String) oDocIn.get(MdekKeys.UUID);
			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			String currentTime = MdekUtils.dateToTimestamp(new Date()); 

			// set common data to transfer to working copy !
			oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());

			if (uuid == null) {
				// NEW Object !

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
			T01Object oWork = oNode.getT01ObjectWork();
			Long oWorkId = (oWork != null) ? oWork.getId() : null; 
			T01Object oPub = oNode.getT01ObjectPublished();
			Long oPubId = (oPub != null) ? oPub.getId() : null; 
			if (oWorkId == null || oWorkId.equals(oPubId)) {
				// no working copy yet, create new object with BASIC data

				// set some missing data which may not be passed from client.
				// set from published version if existent
				if (oPub != null) {
					oDocIn.put(MdekKeys.DATE_OF_CREATION, oPub.getCreateTime());				
				} else {
					oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
				}
				oWork = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
				 // save it to generate id needed for mapping
				daoT01Object.makePersistent(oWork);
			}

			// transfer new data and store.
			docToBeanMapper.mapT01Object(oDocIn, oWork, MappingQuantity.DETAIL_ENTITY);
			daoT01Object.makePersistent(oWork);

			// and update ObjectNode with working copy if not set yet
			oWorkId = oWork.getId();
			if (!oWorkId.equals(oNode.getObjId())) {
				oNode.setObjId(oWorkId);
				daoObjectNode.makePersistent(oNode);
			}
			
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			daoObjectNode.commitTransaction();

			if (refetchAfterStore) {
				result = getObjDetails(uuid);
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument publishObject(IngridDocument oDocIn) {
		try {
			daoObjectNode.beginTransaction();

			String uuid = (String) oDocIn.get(MdekKeys.UUID);
			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			String currentTime = MdekUtils.dateToTimestamp(new Date()); 

			// set common data to transfer to working copy !
			oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			oDocIn.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());

			// TODO: Perform Checks !!! (Pflichtfelder etc.)
			checkObjectPathForPublish(uuid, false);

			if (uuid == null) {
				// NEW NODE !!!!
				// create new uuid
				uuid = UuidGenerator.getInstance().generateUuid();
				oDocIn.put(MdekKeys.UUID, uuid);
				// and get parent, that's the one to check
				String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
				checkObjectPathForPublish(parentUuid, true);
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
			oNode.setObjId(oPubId);
			oNode.setObjIdPublished(oPubId);
			daoObjectNode.makePersistent(oNode);
			
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			daoObjectNode.commitTransaction();

			if (refetchAfterStore) {
				result = getObjDetails(uuid);
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}		
	}

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 */
	public IngridDocument deleteObjectWorkingCopy(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);

			// NOTICE: this one also contains Parent Association !
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
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
				}
			}

			if (performFullDelete) {
				result = deleteObject(params);
			}

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object is non existent afterwards !
	 */
	public IngridDocument deleteObject(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);

			// NOTICE: this one also contains Parent Association !
			ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
			if (oNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
			}

			// delete complete Node ! rest is deleted per cascade !
			daoObjectNode.makeTransient(oNode);

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}		
	}

	/** Move Object with its subtree to new parent. */
	public IngridDocument moveObject(IngridDocument params) {
		try {
			Boolean performAdditionalCheck = (Boolean) params.get(MdekKeys.REQUESTINFO_PERFORM_CHECK);
			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);

			// perform additional check whether subnodes are ok IN OWN TRANSACTION !!!
			if (performAdditionalCheck) {
				IngridDocument checkResult = checkObjectSubTree(fromUuid);
				if ((Boolean) checkResult.get(MdekKeys.RESULTINFO_HAS_WORKING_COPY)) {
					throw new MdekException(MdekError.SUBTREE_HAS_WORKING_COPIES);
				}
			}

			// OK, perform move in separate transaction !

			daoObjectNode.beginTransaction();
			String toUuid = (String) params.get(MdekKeys.TO_UUID);

			// perform basic checks
			ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);
			checkNodesForMove(fromNode, toUuid);
			
			// move object when checks ok
			// set new parent, may be null, then top node !
			fromNode.setFkObjUuid(toUuid);		
			daoObjectNode.makePersistent(fromNode);

			// change date and mod_uuid of all moved nodes !
			// TODO: pass correct user uuid
			IngridDocument result = processMovedNodes(fromNode, "USER UUID");

			daoObjectNode.commitTransaction();
			return result;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/**
	 * Process the moved tree, meaning set modification date and user in every node ...
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
			T01Object obj = objNode.getT01ObjectWork();
			obj.setModTime(currentTime);
			// TODO: set modUuid
//			obj.setModUuid(modUuid);

			daoT01Object.makePersistent(obj);
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
		String rootUuid = (String) params.get(MdekKeys.UUID);
		return checkObjectSubTree(rootUuid);
	}

	/** Checks whether subtree of object has working copies. */
	private IngridDocument checkObjectSubTree(String rootUuid) {
		try {
			daoObjectNode.beginTransaction();

			// load "root"
			ObjectNode rootNode = daoObjectNode.loadByUuid(rootUuid);
			if (rootNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
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

			daoObjectNode.commitTransaction();
			return result;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/** Copy Object to new parent (with or without its subtree). Returns basic data of copied root object. */
	public IngridDocument copyObject(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);

			// perform checks
			ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);
			if (fromNode == null) {
				throw new MdekException(MdekError.FROM_UUID_NOT_FOUND);
			}

			ObjectNode toNode = null;
			// NOTICE: copy to top when toUuid is null
			if (toUuid != null) {
				toNode = daoObjectNode.loadByUuid(toUuid);
				if (toNode == null) {
					throw new MdekException(MdekError.TO_UUID_NOT_FOUND);
				}
			}

			// copy fromNode
			IngridDocument copyResult = createObjectNodeCopy(fromNode, toNode, copySubtree);
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
		    throw handledExc;
		}
	}

	/** Check whether passed nodes are valid for move operation
	 * (e.g. move to subnode not allowed). Throws MdekException if not valid.
	 */
	private void checkNodesForMove(ObjectNode fromNode, String toUuid) {
		ArrayList<MdekError> errors = new ArrayList<MdekError>();

		if (fromNode == null) {
			throw new MdekException(MdekError.FROM_UUID_NOT_FOUND);
		}		

		String fromUuid = fromNode.getObjUuid();
		if (fromUuid == null) {
			throw new MdekException(MdekError.FROM_UUID_NOT_FOUND);
		}		

		if (toUuid != null) {
			if (daoObjectNode.isSubNode(toUuid, fromUuid)) {
				throw new MdekException(MdekError.TARGET_IS_SUBNODE_OF_SOURCE);				
			}
		}
	}

	/** Checks whether node has unpublished parents. Throws MdekException if so. */
	private void checkObjectPathForPublish(String inUuid, boolean includeInNode) {
		// no check when parent node is null
		if (inUuid == null) {
			return;
		}
		
		// check whether a parent is not published
		List<String> pathUuids = daoObjectNode.getObjectPath(inUuid);
		
		for (String pathUuid : pathUuids) {
			if (pathUuid.equals(inUuid) && !includeInNode) {
				continue;
			}
			ObjectNode pathNode = daoObjectNode.loadByUuid(pathUuid);
			if (pathNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
			}
			
			// check
			if (pathNode.getObjIdPublished() == null) {
				throw new MdekException(MdekError.PARENT_NOT_PUBLISHED);
			}
		}
	}

	/**
	 * Creates a copy of the given ObjectNode and adds it under the given parent.
	 * Also copies whole subtree dependent from passed flag.
	 * NOTICE: supports also copy of a tree to one of its subnodes !
	 * Copied nodes are already Persisted !!!
	 * @return doc containing additional info (copy of root node, number copied objects ...)
	 */
	private IngridDocument createObjectNodeCopy(ObjectNode sourceNode, ObjectNode newParentNode,
			boolean copySubtree)
	{
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

			// copy source work version !
			String newUuid = UuidGenerator.getInstance().generateUuid();
			T01Object targetObjWork = createT01ObjectCopy(sourceNode.getT01ObjectWork(), newUuid);
			// set in Bearbeitung !
			targetObjWork.setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());

			T01Object targetObjPub = null;
/*
			// NEVER COPY PUBLISHED VERSION !
				// check whether we also have a published version to copy !
				Long sourceObjPubId = sourceNode.getObjIdPublished();
				Long sourceObjWorkId = sourceNode.getObjId();		
				if (sourceObjPubId != null) {
					if (sourceObjPubId.equals(sourceObjWorkId)) {
						targetObjPub = targetObjWork;
					} else {
						targetObjPub = createT01ObjectCopy(sourceNode.getT01ObjectPublished(), newUuid);
					}
				}		
*/
			// create new Node and set data !
			// we also set Beans in object node, so we can access them afterwards.
			Long targetObjWorkId = targetObjWork.getId();
			Long targetObjPubId = (targetObjPub != null) ? targetObjPub.getId() : null;
			String newParentUuid = null;
			if (newParentNode != null) {
				newParentUuid = newParentNode.getObjUuid();
			}
			
			ObjectNode targetNode = new ObjectNode();
			targetNode.setObjUuid(newUuid);
			targetNode.setObjId(targetObjWorkId);
			targetNode.setT01ObjectWork(targetObjWork);
			targetNode.setObjIdPublished(targetObjPubId);
			targetNode.setT01ObjectPublished(targetObjPub);
			targetNode.setFkObjUuid(newParentUuid);
			daoObjectNode.makePersistent(targetNode);
			numberOfCopiedObj++;
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

/*
	public IngridDocument getTopAddresses() {
		IngridDocument result = new IngridDocument();

		daoT02Address.beginTransaction();

		// fetch top Addresses
		List<T02Address> adrs = daoT02Address.getTopAddresses();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		for (T02Address adr : adrs) {
			resultList.add(mapper.mapT02Address(adr, MappingType.TOP_ENTITY));
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT02Address.beginTransaction();

		Set<T02Address> adrs = daoT02Address.getSubAddresses(uuid);

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		for (T02Address adr : adrs) {
			resultList.add(mapper.mapT02Address(adr, MappingType.SUB_ENTITY));
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}
*/
}
