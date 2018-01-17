/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.mdek.job;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.iplug.dsc.index.DscDocumentProducer;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.tools.MdekErrorHandler;
import de.ingrid.mdek.services.log.AuditService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of all idc jobs encapsulating common idc stuff.
 */
public abstract class MdekIdcJob extends MdekJob {

	protected MdekErrorHandler errorHandler;

	protected IGenericDao<IEntity> genericDao;

	protected BeanToDocMapper beanToDocMapper;
	protected DocToBeanMapper docToBeanMapper;

    protected DscDocumentProducer docProducerObject;
    protected DscDocumentProducer docProducerAddress;
    protected IndexManager indexManager;

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

    /** Checks passed Exception (catched from Job method) and handles current transaction accordingly.
     * A rollback is executed if NOT keepTransaction set and if NOT exception is USER_HAS_RUNNING_JOBS
     * (indicating a job is still running and needs active transaction !!!).
     * Further may "transform" exception to MdekException.
     * @param excIn an exception from a job method
     * @param keepTransaction set to true if you do NOT want to rollback stuff
     * @return exception of type MdekException if problem can be recognized or the given exception if not recognized.
     */
    protected RuntimeException handleException(RuntimeException excIn, boolean keepTransaction) {
        if (!keepTransaction) {
            // normal behaviour if we can manipulate transaction (e.g. rollback)
            return handleException( excIn );
        }

        // no manipulation of transaction, we just map exception
        return errorHandler.handleException(excIn);
    }

    /** Checks passed Exception (catched from Job method) and handles current transaction accordingly.
     * A rollback is executed if NOT exception is USER_HAS_RUNNING_JOBS
     * (indicating a job is still running and needs active transaction !!!).
     * Further may "transform" exception to MdekException.
     * @param excIn an exception from a job method
     * @return exception of type MdekException if problem can be recognized or the given exception if not recognized.
     */
	protected RuntimeException handleException(RuntimeException excIn) {
		
		// handle transaction rollback
		// rollback NOT executed if MdekErrorType.USER_HAS_RUNNING_JOBS -> job is still running
		// and needs active transaction !!!)
		if (!errorHandler.isHasRunningJobsException(excIn)) {
			genericDao.rollbackTransaction();			
		}

		return errorHandler.handleException(excIn);
	}

	/** Update ES search index with data from PUBLISHED entities and log via audit service if set.
	 * @param changedEntities List of maps containing data about changed entities.
	 * NOTICE: May also contain unpublished entities, this is checked, only published ones are processed ! 
	 */
	protected void updateSearchIndexAndAudit(List<HashMap> changedEntities) {
        for (Map entity : changedEntities) {

            // use document producer according to entity type
            DscDocumentProducer docProducer = docProducerObject;
            if (IdcEntityType.ADDRESS.getDbValue().equals( entity.get( MdekKeys.JOBINFO_ENTITY_TYPE ) )) {
                docProducer = docProducerAddress;
            }

            // PUBLISHED entities !
            if (WorkState.VEROEFFENTLICHT.getDbValue().equals( entity.get( MdekKeys.WORK_STATE ) )) {
                // update search index
                ElasticDocument doc = docProducer.getById( entity.get( MdekKeys.ID ).toString(), "id" );
                if (doc != null && !doc.isEmpty()) {
                    indexManager.addBasicFields( doc, docProducer.getIndexInfo() );
                    indexManager.update( docProducer.getIndexInfo(), doc, true );
                    indexManager.flush();
                }

                // and log if audit service set
                if (AuditService.instance != null && doc != null) {
                    String auditMsg = (String) entity.get( MdekKeys.JOBINFO_MESSAGES );
                    String message = "" + auditMsg + " with UUID: " + entity.get( MdekKeys.UUID );
                    Map<String, String> map = new HashMap<String, String>();
                    map.put( "idf", (String) doc.get( "idf" ) );
                    String payload = JSONObject.toJSONString( map );
                    AuditService.instance.log( message, payload );
                }
            }

            // DELETED entities !
            if (WorkState.DELETED.getDbValue().equals( entity.get( MdekKeys.WORK_STATE ) )) {
                String uuid = (String) entity.get( MdekKeys.UUID );
                if (log.isDebugEnabled()) log.debug( "Going to remove it from the index using uuId: " + uuid );
                indexManager.delete( docProducer.getIndexInfo(), uuid, true );
                indexManager.flush();

                if (AuditService.instance != null) {
                    String auditMsg = (String) entity.get( MdekKeys.JOBINFO_MESSAGES );
                    String message = "" + auditMsg + " with UUID: " + uuid;
                    AuditService.instance.log( message );
                }
            }
        }
	}

    /** Returns value of key in given doc or defaultValue if key not set ! */
    protected Object getOrDefault(IngridDocument doc, String key, Object defaultValue) {
        if (doc.containsKey( key )) {
            return doc.get( key );
        } else {
            return defaultValue;
        }
    }

    @Autowired
    @Qualifier("dscDocumentProducer")
    private void setDocProducerObject(DscDocumentProducer docProducer) {
        this.docProducerObject = docProducer;
    }

    @Autowired
    @Qualifier("dscDocumentProducerAddress")
    private void setDocProducerAddress(DscDocumentProducer docProducer) {
        this.docProducerAddress = docProducer;
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
