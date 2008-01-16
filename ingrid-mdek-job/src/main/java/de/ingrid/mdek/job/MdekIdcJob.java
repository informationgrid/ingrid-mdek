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
			beanToDocMapper.mapObjectNode(oN, objDoc);
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
			beanToDocMapper.mapObjectNode(oN, objDoc);
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

		ArrayList<String> uuidList = new ArrayList<String>();
		while(uuid != null) {
			ObjectNode oN = daoObjectNode.loadByUuid(uuid);
			if (oN == null) {
				log.error("Object with uuid=" + uuid + " NOT FOUND !");
				uuidList = null;
				break;
			}
			uuidList.add(0, uuid);
			uuid = oN.getFkObjUuid();
		}

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
	private IngridDocument getObjDetails(String uuid) {
		IngridDocument resultDoc = null;

		daoObjectNode.beginTransaction();

		// first get all "internal" object data (referenced addresses ...)
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode != null) {
			resultDoc = new IngridDocument();
			beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);			
		
			// then get "external" data (objects referencing the given object ...)
			List<ObjectNode> oNs = daoObjectNode.getObjectReferencesFrom(uuid);
			beanToDocMapper.mapObjectReferencesFrom(oNs, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);
		}

		daoObjectNode.commitTransaction();

		return resultDoc;		
	}

	public IngridDocument storeObject(IngridDocument oDocIn) {
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// set common data in document
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());

		daoT01Object.beginTransaction();

		T01Object o = null;
		ObjectNode oNode = null;

		if (uuid == null) {
			// New Object  !!!
			
			// create new object (save it to generate id !)
			uuid = UuidGenerator.getInstance().generateUuid();
			oDocIn.put(MdekKeys.UUID, uuid);
			oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			o = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
			daoT01Object.makePersistent(o);
			
			// and new ObjectNode
			oNode = docToBeanMapper.mapObjectNode(oDocIn, new ObjectNode());

		} else {
			// Existing Object !!!

			oNode = daoObjectNode.getObjDetails(uuid);

			// do we have to create a working copy
			if (oNode.getObjId().equals(oNode.getObjIdPublished())) {

				// no working copy yet, create it with same uuid and save it (to generate id !)
				T01Object oPub = oNode.getT01ObjectPublished();
				o = new T01Object();
				o.setObjUuid(oPub.getObjUuid());
				daoT01Object.makePersistent(o);

				// then copy content from published one (via mappers)
				IngridDocument oDocPub =
					beanToDocMapper.mapT01Object(oPub, new IngridDocument(), MappingQuantity.COPY_ENTITY);
				docToBeanMapper.mapT01Object(oDocPub, o, MappingQuantity.COPY_ENTITY);
				
			} else {
				o = oNode.getT01ObjectWork();
			}
		}

		// transfer new data and store.
		docToBeanMapper.mapT01Object(oDocIn, o, MappingQuantity.DETAIL_ENTITY);
		daoT01Object.makePersistent(o);

		// and update ObjectNode with working copy if not set yet
		Long oId = o.getId();
		if (!oId.equals(oNode.getObjId())) {
			oNode.setObjId(oId);
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

	public IngridDocument moveObjectSubTree(IngridDocument params) {
		String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
		String toUuid = (String) params.get(MdekKeys.TO_UUID);
		IngridDocument result = new IngridDocument();

		daoT01Object.beginTransaction();

		// NOTICE: this one also contains Parent Association !
		ObjectNode fromNode = daoObjectNode.loadByUuid(fromUuid);

		// TODO: perform checks whether object move allowed ! (from/to-node exist, new parent not subobject ...)

		// set new parent
		fromNode.setFkObjUuid(toUuid);		
		daoObjectNode.makePersistent(fromNode);

		daoT01Object.commitTransaction();

		return result;		
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
