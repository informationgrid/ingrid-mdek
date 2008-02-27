package de.ingrid.mdek.job;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekErrorHandler;
import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of all idc jobs encapsulating common idc stuff.
 */
public abstract class MdekIdcJob extends MdekJob {

	protected MdekErrorHandler errorHandler;	

	protected ISysListDao daoSysList;
	protected IGenericDao daoT03Catalog;

	protected BeanToDocMapper beanToDocMapper;
	protected DocToBeanMapper docToBeanMapper;

	public MdekIdcJob(Logger log, DaoFactory daoFactory) {
		super(log);

		errorHandler = MdekErrorHandler.getInstance();

		daoSysList = daoFactory.getSysListDao();
		daoT03Catalog = daoFactory.getDao(T03Catalogue.class);

		beanToDocMapper = BeanToDocMapper.getInstance();
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
	}

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
	
	public IngridDocument getCatalog(IngridDocument params) {
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
