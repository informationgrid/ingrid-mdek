package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.UuidGenerator;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
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

	private BeanToDocMapper beanToDocMapper;
	private DocToBeanMapper docToBeanMapper;

	public MdekIdcJob(ILogService logService,
			DaoFactory daoFactory) {
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekIdcJob.class); 

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();

		beanToDocMapper = BeanToDocMapper.getInstance();
		docToBeanMapper = docToBeanMapper.getInstance(daoFactory);
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
	// TODO: encapsulate all mdekJob methods in throw/catch performing rollback (releasing session not necessary due to getCurrentSession !?)
	// TODO: check error in catch and set error in result ! Then check result-error in job framework and take over to response (set null result ?) ?  
	// TODO: How to transmit SUCCESS ? at the moment just non null result (empty IngridDoc)

	public IngridDocument getUiListValues() {
		IngridDocument result = new IngridDocument();

		daoSpatialRefValue.beginTransaction();

		// fetch top Objects
		List<String> list = daoSpatialRefValue.getFreieRefValueNames();
		result.put(MdekKeys.UI_FREE_SPATIAL_REFERENCES, list);

		daoSpatialRefValue.commitTransaction();

		return result;
	}

	public IngridDocument getTopObjects() {
		IngridDocument result = new IngridDocument();

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

		daoObjectNode.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoObjectNode.beginTransaction();

		List<ObjectNode> oNs = daoObjectNode.getSubObjects(uuid);

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(oNs.size());
		for (ObjectNode oN : oNs) {
			IngridDocument objDoc = new IngridDocument();
			beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.TREE_ENTITY);
			beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.BASIC_ENTITY);
			resultList.add(objDoc);
		}

		daoObjectNode.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getObjectPath(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);

		daoObjectNode.beginTransaction();

		List<String> uuidList = daoObjectNode.getObjectPath(uuid);

		daoObjectNode.commitTransaction();

		IngridDocument result = null;
		if (uuidList != null && uuidList.size() > 0) {
			result = new IngridDocument();
			result.put(MdekKeys.PATH, uuidList);
		}

		return result;
	}

	public IngridDocument getObjDetails(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		return getObjDetails(uuid);
	}

	public IngridDocument storeObject(IngridDocument oDocIn) {
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// set common data to transfer to working copy !
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());

		daoT01Object.beginTransaction();

		if (uuid == null) {
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
		
		daoT01Object.commitTransaction();
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.UUID, uuid);
		if (refetchAfterStore) {
			result = getObjDetails(uuid);
		}
		
		return result;
	}

	public IngridDocument publishObject(IngridDocument oDocIn) {
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// set common data to transfer to working copy !
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		oDocIn.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());

		daoT01Object.beginTransaction();
		
		// TODO: Perform Checks !!! (Pflichtfelder etc.)

		if (uuid == null) {
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
		oNode.setObjId(oPubId);
		oNode.setObjIdPublished(oPubId);
		daoObjectNode.makePersistent(oNode);
		
		daoT01Object.commitTransaction();
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.UUID, uuid);
		if (refetchAfterStore) {
			result = getObjDetails(uuid);
		}
		
		return result;
	}

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 */
	public IngridDocument deleteObjectWorkingCopy(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		IngridDocument result = new IngridDocument();

		daoT01Object.beginTransaction();

		// NOTICE: this one also contains Parent Association !
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);

		boolean performFullDelete = false;
		if (oNode != null) {
			Long idPublished = oNode.getObjIdPublished();
			Long idWorkingCopy = oNode.getObjId();

			// if we have NO published version -> delete complete node !
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
		}

		daoT01Object.commitTransaction();
		
		if (performFullDelete) {
			result = deleteObject(params);
		}

		return result;
	}

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object is non existent afterwards !
	 */
	public IngridDocument deleteObject(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		IngridDocument result = new IngridDocument();

		daoT01Object.beginTransaction();

		// NOTICE: this one also contains Parent Association !
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode != null) {
			// delete complete Node ! rest is deleted per cascade !
			daoObjectNode.makeTransient(oNode);
		}	

		daoT01Object.commitTransaction();

		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);			
		return result;		
	}

	/** Move Object with its subtree to new parent. */
	public IngridDocument moveObject(IngridDocument params) {
		String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
		String toUuid = (String) params.get(MdekKeys.TO_UUID);
		IngridDocument result = null;

		daoT01Object.beginTransaction();

		// perform checks
		ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);
		IngridDocument errDoc = checkValidTreeNodes(fromNode, toUuid);

		// move object when checks ok
		if (errDoc == null) {
			// set new parent, may be null, then top node !
			fromNode.setFkObjUuid(toUuid);		
			daoObjectNode.makePersistent(fromNode);

			// success
			result = new IngridDocument();			
		}

		daoT01Object.commitTransaction();

		return result;		
	}

	/** Copy Object to new parent (with or without its subtree). Returns basic data of copied object. */
	public IngridDocument copyObject(IngridDocument params) {
		String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
		String toUuid = (String) params.get(MdekKeys.TO_UUID);
		Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);
		IngridDocument resultDoc = null;

		daoT01Object.beginTransaction();

		// perform checks
		ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);

		ObjectNode toNode = null;
		ArrayList<String> uuidsCopiedNodes = null;
		if (toUuid != null) {
			toNode = daoObjectNode.loadByUuid(toUuid);
			// check whether we copy to subnode
			if (daoObjectNode.isSubNode(toUuid, fromUuid)) {
				// we copy to a subnode, so we have to check already copied nodes
				// to avoid endless recursion !
				uuidsCopiedNodes = new ArrayList<String>();
			}
		}
		

		// copy fromNode
		ObjectNode fromNodeCopy = createObjectNodeCopy(fromNode, toNode, copySubtree, uuidsCopiedNodes);

		// success
		resultDoc = new IngridDocument();
		beanToDocMapper.mapT01Object(fromNodeCopy.getT01ObjectWork(), resultDoc, MappingQuantity.TABLE_ENTITY);
		// also child info
		beanToDocMapper.mapObjectNode(fromNodeCopy, resultDoc, MappingQuantity.COPY_ENTITY);

		daoT01Object.commitTransaction();

		return resultDoc;		
	}

	private IngridDocument getObjDetails(String uuid) {
		IngridDocument resultDoc = null;

		daoObjectNode.beginTransaction();

		// first get all "internal" object data (referenced addresses ...)
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode != null) {
			resultDoc = new IngridDocument();
			beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);
			
			// also map ObjectNode for published info
			beanToDocMapper.mapObjectNode(oNode, resultDoc, MappingQuantity.DETAIL_ENTITY);
		
			// then get "external" data (objects referencing the given object ...)
			List<ObjectNode> oNs = daoObjectNode.getObjectReferencesFrom(uuid);
			beanToDocMapper.mapObjectReferencesFrom(oNs, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);
		}

		daoObjectNode.commitTransaction();

		return resultDoc;		
	}

	/** Check whether passed nodes are valid for performing tree operations (copy, move ...)
	 * @param fromNode source node
	 * @param toUuid target node
	 * @return null if ok, else IngridDoc containing errors
	 */
	private IngridDocument checkValidTreeNodes(ObjectNode fromNode, String toUuid) {
		boolean nodesValid = true;
		IngridDocument errDoc = new IngridDocument();

		String fromUuid = null;
		if (fromNode == null) {
			nodesValid = false;
			// TODO: transfer error !
		} else {
			fromUuid = fromNode.getObjUuid();
		}

		if (fromUuid != null && toUuid != null) {
			if (daoObjectNode.isSubNode(toUuid, fromUuid)) {
				nodesValid = false;
				// TODO: transfer error !				
			}
		}
		
		if (!nodesValid) {
			return errDoc;
		}

		return null;
	}

	/**
	 * Creates a copy of the given ObjectNode and adds it under the given parent.
	 * Also copies whole subtree dependent from passed flag.
	 * NOTICE: supports also copy of a tree to one of its subnodes !
	 * Copied nodes are already Persisted !!!
	 */
	private ObjectNode createObjectNodeCopy(ObjectNode sourceNode, ObjectNode newParentNode,
			boolean copySubtree, List<String> uuidsCopiedNodes) {

		boolean isCopyToOwnSubnode = false;
		if (uuidsCopiedNodes != null) {
			isCopyToOwnSubnode = true;
		}

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
		
		if (isCopyToOwnSubnode) {
			uuidsCopiedNodes.add(newUuid);
		}
		
		// add child bean to parent bean, so we can determine children info when mapping (without reloading)
		if (newParentNode != null) {
			newParentNode.getObjectNodeChildren().add(targetNode);
		}
		// copy subtree ? only if not already a copied node !
		if (copySubtree) {
			List<ObjectNode> sourceSubNodes = daoObjectNode.getSubObjects(sourceNode.getObjUuid());
			for (ObjectNode sourceSubNode : sourceSubNodes) {
				if (isCopyToOwnSubnode) {
					if (uuidsCopiedNodes.contains(sourceSubNode.getObjUuid())) {
						// skip this node ! is the top node of the copied tree !
						// we set list to null, cause we don't have to perform further checks
						// when copying oncoming nodes !
						uuidsCopiedNodes = null;
						continue;
					}
				}
				createObjectNodeCopy(sourceSubNode, targetNode, copySubtree, uuidsCopiedNodes);
			}
		}

		return targetNode;
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
