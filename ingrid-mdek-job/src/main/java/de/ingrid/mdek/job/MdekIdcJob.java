package de.ingrid.mdek.job;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import de.ingrid.mdek.job.tools.MdekErrorHandler;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of all idc jobs encapsulating common idc stuff.
 */
public abstract class MdekIdcJob extends MdekJob {

	protected MdekErrorHandler errorHandler;

	protected IGenericDao<IEntity> genericDao;

	protected BeanToDocMapper beanToDocMapper;
	protected DocToBeanMapper docToBeanMapper;

	public MdekIdcJob(Logger log, DaoFactory daoFactory) {
		super(log, daoFactory);

		errorHandler = MdekErrorHandler.getInstance();

		genericDao = daoFactory.getDao(IEntity.class);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
	}

	/** Returns info about the version of the MdekServer (iPlug). */
	public IngridDocument getVersion(IngridDocument params) {
		try {
			IngridDocument resultDoc = new IngridDocument();

			// extract version properties from version.properties
			ResourceBundle resourceBundle = ResourceBundle.getBundle("mdek-job-version");   
			Enumeration<String> keys = resourceBundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				resultDoc.put(key, resourceBundle.getObject(key));
			}

			return resultDoc;

		} catch (RuntimeException e) {
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/** Checks passed Exception (catched from Job method) and rollbacks current transaction if necessary (e.g.
	 * NO rollback if due to USER_HAS_RUNNING_JOBS -> job is still running and needs active transaction !!!).
	 * Further may "transform" exception to MdekException. */
	protected RuntimeException handleException(RuntimeException excIn) {
		
		// handle transaction rollback
		// rollback NOT executed if MdekErrorType.USER_HAS_RUNNING_JOBS -> job is still running
		// and needs active transaction !!!)
		if (!MdekErrorHandler.isHasRunningJobsException(excIn)) {
			genericDao.rollbackTransaction();			
		}

		return errorHandler.handleException(excIn);
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
}
