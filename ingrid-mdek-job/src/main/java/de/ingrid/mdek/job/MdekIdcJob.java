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

	private BeanToDocMapper beanToDocMapper;
	private DocToBeanMapper docToBeanMapper;

	public MdekIdcJob(ILogService logService,
			DaoFactory daoFactory) {
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekIdcJob.class); 

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();

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
	public IngridDocument getTopObjects() {
		IngridDocument result = new IngridDocument();

		daoObjectNode.beginTransaction();

		// fetch top Objects
		List<ObjectNode> oNs = daoObjectNode.getTopObjects();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(oNs.size());
		for (ObjectNode oN : oNs) {
			IngridDocument objDoc = new IngridDocument();
			beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.BASIC_ENTITY);
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
			beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.BASIC_ENTITY);
			beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.BASIC_ENTITY);
			resultList.add(objDoc);
		}

		daoObjectNode.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
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
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// set common data in document
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());

		// TODO: copy object

		daoT01Object.beginTransaction();

		T01Object o = null;
		ObjectNode oNode = null;

		if (uuid == null) {
			// New Object  !!!
			// create and save basic object to get id !
			uuid = UuidGenerator.getInstance().generateUuid();
			oDocIn.put(MdekKeys.UUID, uuid);
			oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			o = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
			daoT01Object.makePersistent(o);
			Long oId = o.getId();
			
			// and create ObjectNode
			oNode = new ObjectNode();
			oNode.setObjUuid(uuid);
			String parentUuuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
			oNode.setFkObjUuid(parentUuuid);
			oNode.setObjId(oId);
			// TODO: ObjIdPublished should be Null on New Object !!!! but NOT NULL in database !!!
			oNode.setObjIdPublished(oId);
			oNode.setT01ObjectWork(o);
			daoObjectNode.makePersistent(oNode);

		} else {
			oNode = daoObjectNode.getObjDetails(uuid);
			o = oNode.getT01ObjectWork();
		}

		docToBeanMapper.mapT01Object(oDocIn, o, MappingQuantity.DETAIL_ENTITY);
		daoT01Object.makePersistent(o);

		daoT01Object.commitTransaction();
		
		return getObjDetails(uuid);
	}

	public IngridDocument deleteObject(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		IngridDocument result = null;

		daoT01Object.beginTransaction();

		// TODO: how to delete ? how handle working copy ? mark as deleted ?

		// NOTICE: this one is also Parent Association !
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		T01Object o = oNode.getT01ObjectWork();

		if (oNode != null) {
			// delete parent association
			daoObjectNode.makeTransient(oNode);
			// delete object
			daoT01Object.makeTransient(o);

			// TODO: delete whole sub tree of objects !!!
			
			// TODO: wie delete success in Result transportieren ? jetzt null / not null
			result = new IngridDocument();
		}

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
