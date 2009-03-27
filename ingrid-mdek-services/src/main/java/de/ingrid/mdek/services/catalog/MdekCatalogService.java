package de.ingrid.mdek.services.catalog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.services.utils.MdekKeyValueHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to catalog data (syslists etc.).
 */
public class MdekCatalogService {

	private static final Logger LOG = Logger.getLogger(MdekCatalogService.class);

	private static MdekCatalogService myInstance;

	private static String CACHE_CONFIG_FILE = "/ehcache-services.xml";
	private static String CACHE_SYS_LIST_MAP = "services-SysListMap";
	private static String CACHE_CATALOG = "services-Catalog";

	private DaoFactory daoFactory;

	private IGenericDao<IEntity> daoT03Catalogue;
	private ISysListDao daoSysList;
	private ISysGuiDao daoSysGui;
	private ISysGenericKeyDao daoSysGenericKey;
	private IT08AttrTypeDao daoT08AttrType;
	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;
	private ISearchtermValueDao daoSearchtermValue;
	private ISearchtermSnsDao daoSearchtermSns;
	private IGenericDao<IEntity> dao;

	private MdekJobHandler jobHandler;

	private BeanToDocMapper beanToDocMapper;

	private CacheManager cacheManager;
	private Cache syslistMapCache;
	private Cache catalogCache;

	/** Get The Singleton */
	public static synchronized MdekCatalogService getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekCatalogService(daoFactory);
		}
		return myInstance;
	}

	private MdekCatalogService(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;

		daoT03Catalogue = daoFactory.getDao(T03Catalogue.class);
		daoSysList = daoFactory.getSysListDao();
		daoSysGui = daoFactory.getSysGuiDao();
		daoSysGenericKey = daoFactory.getSysGenericKeyDao();
		daoT08AttrType = daoFactory.getT08AttrTypeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoSearchtermValue = daoFactory.getSearchtermValueDao();
		daoSearchtermSns = daoFactory.getSearchtermSnsDao();
		dao = daoFactory.getDao(IEntity.class);

		jobHandler = MdekJobHandler.getInstance(daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);

		URL url = getClass().getResource(CACHE_CONFIG_FILE);
		CacheManager.create(url);
		cacheManager = CacheManager.getInstance();
		syslistMapCache = cacheManager.getCache(CACHE_SYS_LIST_MAP);
		catalogCache = cacheManager.getCache(CACHE_CATALOG);
	}

	public void clearCaches() {
		syslistMapCache.removeAll();
		catalogCache.removeAll();
	}

	/** Get catalog. NOTICE: transaction must be active when called the first time ! */
	public T03Catalogue getCatalog() {
		// NEVER CACHE !!!!!! can be changed (name etc.) !!!
		T03Catalogue catalog = (T03Catalogue) daoT03Catalogue.findFirst();
		if (catalog == null) {
			throw new MdekException(new MdekError(MdekErrorType.CATALOG_NOT_FOUND));
		}			

		return catalog;
	}

	/** Get ID of catalog (id not uuid!). NOTICE: transaction must be active when called the first time ! */
	public Long getCatalogId() {
		return getCatalog().getId();
	}

	/** Get language of catalog. USES CACHE !!!
	 * NOTICE: transaction must be active when called the first time ! */
	public String getCatalogLanguage() {
		boolean useCache = true;
		String cacheLanguageKey = "catalog-language";

		// get language from cache if requested !
		String language = null;
		Element elem = null;
		if (useCache) {
			elem = catalogCache.get(cacheLanguageKey);
			if (elem != null) {
				language = (String) elem.getObjectValue();
			}
		}
		
		// read language if not cached !
		if (language == null) {
			language = getCatalog().getLanguageCode();
		}

		// add to cache if cache used !
		if (useCache && elem == null) {
			catalogCache.put(new Element(cacheLanguageKey, language));
		}

		return language;
	}

	/** Is workflow control (QA) activated ? */
	public boolean isWorkflowEnabled() {
		return MdekUtils.YES.equals(getCatalog().getWorkflowControl());
	}

	/** Get all available syslist ids. */
	public Integer[] getSysListIds() {
		List<Integer> idList = daoSysList.getSysListIds();

		return idList.toArray(new Integer[idList.size()]);
	}

	/** Get Doc representation of syslists of given ids and language. */
	public IngridDocument getSysLists(Integer[] listIds, String language) {
		IngridDocument result = new IngridDocument();
			
		for (int listId : listIds) {
			List<SysList> list = daoSysList.getSysList(listId, language);
			
			IngridDocument listDoc = new IngridDocument();
			beanToDocMapper.mapSysList(list, listId, listDoc);
			
			result.put(MdekKeys.SYS_LIST_KEY_PREFIX + listId,  listDoc);
		}

		return result;
	}

	
	/** Get syslist entries of syslist with given id and language IN MAP. USES CACHE !<br>
	 * entry_key is Key to Map and delivers entry_name. */
	public Map<Integer, String> getSysListKeyNameMap(int listId, String language) {
		// ALWAYS USE CACHE !
		boolean useCache = true;

		// get map from cache if requested !
		Map<Integer, String> map = null;
		Element elem = null;
		if (useCache) {
			elem = syslistMapCache.get(listId);
			if (elem != null) {
				map = (Map<Integer, String>) elem.getObjectValue();
			}
		}
		
		// create map if not cached !
		if (map == null) {
			map = new HashMap<Integer, String>();
			List<SysList> entries = daoSysList.getSysList(listId, language);
			for (SysList entry : entries) {
				map.put(entry.getEntryId(), entry.getName());
			}			
		}

		// add to cache if cache used !
		if (useCache && elem == null) {
			syslistMapCache.put(new Element(listId, map));
		}

		return map;
	}

	public String getSysListEntryName(int listId, int entryId) {
		String entryName = null;
		
		String language = getCatalogLanguage();
		Map<Integer, String> keyNameMap = getSysListKeyNameMap(listId, language);
		if (keyNameMap != null) {
			entryName = keyNameMap.get(entryId);
		}
		
		return entryName;
	}

	/** Get sysgui elements with given ids AS LIST OF BEANS. */
	public List<SysGui> getSysGuis(String[] guiIds) {
		List<SysGui> list = daoSysGui.getSysGuis(guiIds);
		
		return list;
	}

	/** Get generic keys of given names AS LIST OF BEANS. PASS null if all generic keys ! */
	public List<SysGenericKey> getSysGenericKeys(String[] keyNames) {
		List<SysGenericKey> list = daoSysGenericKey.getSysGenericKeys(keyNames);
		
		return list;
	}

	/** Get Doc representation of DEFINITIONS of additional fields of given ids and language (for items in selection list if present). */
	public IngridDocument getSysAdditionalFields(Long[] fieldIds, String language) {
		List<T08AttrType> fields = daoT08AttrType.getT08AttrTypes(fieldIds, language);

		IngridDocument result = new IngridDocument();
		beanToDocMapper.mapT08AttrTypes(fields, result);

		return result;
	}

	/**
	 * Update auskunft in all objects (also published ones !).
	 * Further mod-date and -uuid in objects is updated.<br>
	 * NOTICE: already persists addresses !
	 * @param oldAuskunftUuid auskunft to be replaced
	 * @param newAuskunftUuid with this new auskunft
	 * @param userUuid calling user
	 * @return num objects updated
	 */
	public int updateAuskunftInObjects(String oldAuskunftUuid, String newAuskunftUuid, String userUuid) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		int numAuskunftChanged = 0;

		List<T01Object> objs = 
			daoT02Address.getObjectReferencesByTypeId(oldAuskunftUuid, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID, null);
		for (T01Object obj : objs) {
			Set<T012ObjAdr> objAdrs = obj.getT012ObjAdrs();
			
			// replace auskunft
			boolean objChanged = false;
			for (T012ObjAdr objAdr : objAdrs) {
				if (MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID.equals(objAdr.getType())) {
					objAdr.setAdrUuid(newAuskunftUuid);
					objChanged = true;
					numAuskunftChanged++;
				}
			}

			if (objChanged) {
				// update mod_time and mod_uuid and save (cascades save to objAdrs)
				obj.setModTime(currentTime);
				obj.setModUuid(userUuid);
				daoT01Object.makePersistent(obj);					
			}
		}
		
		return numAuskunftChanged;
	}

	/**
	 * Update all entities (also published ones !), where passed old uuid is responsible user with passed new uuid.<br>
	 * NOTICE: already persists entities !
	 * @param oldResponsibleUuid
	 * @param newResponsibleUuid
	 * @return num entities updated (objects and addresses)
	 */
	public int updateResponsibleUserInEntities(String oldResponsibleUuid, String newResponsibleUuid) {
		int numObjs = updateResponsibleUserInObjects(oldResponsibleUuid, newResponsibleUuid);
		int numAddrs = updateResponsibleUserInAddresses(oldResponsibleUuid, newResponsibleUuid);
		
		return numObjs + numAddrs;
	}

	/**
	 * Update all objects (also published ones !), where passed old uuid is responsible user with passed new uuid.<br>
	 * NOTICE: already persists objects !
	 * @param oldResponsibleUuid
	 * @param newResponsibleUuid
	 * @return num objects updated
	 */
	public int updateResponsibleUserInObjects(String oldResponsibleUuid, String newResponsibleUuid) {
		List<T01Object> os = 
			daoT01Object.getObjectsOfResponsibleUser(oldResponsibleUuid, null);
		int numObjs = 0;
		for (T01Object o : os) {
			o.setResponsibleUuid(newResponsibleUuid);
			daoT01Object.makePersistent(o);
			numObjs++;
		}
		
		return numObjs;
	}

	/**
	 * Update all addresses (also published ones !), where passed old uuid is responsible user with passed new uuid.<br>
	 * NOTICE: already persists addresses !
	 * @param oldResponsibleUuid
	 * @param newResponsibleUuid
	 * @return num addresses updated
	 */
	public int updateResponsibleUserInAddresses(String oldResponsibleUuid, String newResponsibleUuid) {
		List<T02Address> as = 
			daoT02Address.getAddressesOfResponsibleUser(oldResponsibleUuid, null);
		int numAddrs = 0;
		for (T02Address a : as) {
			a.setResponsibleUuid(newResponsibleUuid);
			daoT02Address.makePersistent(a);
			numAddrs++;
		}

		return numAddrs;
	}

	/** Replace the given free entry with the given syslist entry.
	 * @param freeEntry entry name of free entry
	 * @param sysLst specifies syslist and according entities
	 * @param sysLstEntryId syslist entry id
	 * @param sysLstEntryName entry name of syslist entry
	 * @return number of free entries (entities) replaced
	 */
	public int replaceFreeEntryWithSyslistEntry(String freeEntry,
			MdekSysList sysLst, int sysLstEntryId, String sysLstEntryName) {

		List<IEntity> entities = daoSysList.getEntitiesOfFreeListEntry(sysLst, freeEntry);
		
		int numReplaced = 0;
		if (sysLst == MdekSysList.LEGIST) {
			for (IEntity entity : entities) {
				T015Legist legist = (T015Legist) entity;
				legist.setLegistKey(sysLstEntryId);
				legist.setLegistValue(sysLstEntryName);
				dao.makePersistent(legist);
				numReplaced++;
			}
		}
		
		return numReplaced;
	}

	/** "logs" Start-Info in job information of given type IN MEMORY and IN DATABASE */
	public void startJobInfo(JobType jobType, int numProcessed, int totalNum, HashMap jobDetails, String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(jobType, numProcessed, totalNum, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
		jobHandler.startJobInfoDB(jobType, startTime, jobDetails, userUuid);
	}
	/** Update general info of job IN MEMORY. */
	public void updateJobInfo(JobType jobType, 
			String entityType, int numUpdated, int totalNum, String userUuid) {
		// update in memory job state.
		IngridDocument jobDoc = jobHandler.createRunningJobDescription(
				jobType, entityType, numUpdated, totalNum, false);
		jobHandler.updateRunningJob(userUuid, jobDoc);
	}
	/** Update special info of UPDATE_SEARCHTERMS job IN MEMORY (Info displayed on 2. tab) ! */
	public void updateJobInfoNewUpdatedTerm(String termName, String termType,
			String msg, int numObj, int numAddr, String userUuid) {
		IngridDocument jobInfo = jobHandler.getRunningJobInfo(JobType.UPDATE_SEARCHTERMS, userUuid);
		
		List<Map> termList = (List<Map>) jobInfo.get(MdekKeys.JOBINFO_TERMS_UPDATED);
		if (termList == null) {
			termList = new ArrayList<Map>();
			jobInfo.put(MdekKeys.JOBINFO_TERMS_UPDATED, termList);
		}

		Map term = new HashMap();
		term.put(MdekKeys.TERM_NAME, termName);
		term.put(MdekKeys.TERM_TYPE, termType);
		term.put(MdekKeys.JOBINFO_MESSAGES, msg);
		term.put(MdekKeys.JOBINFO_NUM_OBJECTS, numObj);
		term.put(MdekKeys.JOBINFO_NUM_ADDRESSES, numAddr);

		termList.add(term);
		
		// CALL back !!! to check job canceled !
		jobHandler.updateRunningJob(userUuid, jobInfo);
	}
	/**
	 * "logs" End-Info in job information of given type IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param userUuid calling user
	 */
	public void endJobInfo(JobType jobType, String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails = jobHandler.getJobInfoDetailsFromRunningJobInfo(
				runningJobInfo, false);
		if (jobType == JobType.UPDATE_SEARCHTERMS) {
			jobDetails.put(MdekKeys.JOBINFO_TERMS_UPDATED, runningJobInfo.get(MdekKeys.JOBINFO_TERMS_UPDATED));
		}

		// then update job info in database
		jobHandler.updateJobInfoDB(jobType, jobDetails, userUuid);
		// add end info
		jobHandler.endJobInfoDB(jobType, userUuid);
	}

	/** Update all syslist values in entities according to current syslists. updates WORKING AND PUBLISHED VERSION !!! */
	public void rebuildEntitiesSyslistData(String userUuid) {
		// update all entities containing key/value pairs

		// GET SINGLETON HERE AND NOT IN CONSTRUCTOR TO AVOID ENDLESS LOOPS (dependent from each other)
		MdekKeyValueHandler keyValueHandler = MdekKeyValueHandler.getInstance(daoFactory);

		// special dao needed for special entities
		IGenericDao<IEntity> daoT011ObjServ = daoFactory.getDao(T011ObjServ.class);

		// get entity classes and process
		Class[] entityClasses = keyValueHandler.getEntityClassesContainingKeyValue();
		for (Class entityClass : entityClasses) {
			String className = entityClass.getName();
			if (className.lastIndexOf('.') > 0) {
				className = className.substring(className.lastIndexOf('.')+1);
		    }

			// get according entities (hibernate beans)
			IGenericDao<IEntity> dao = daoFactory.getDao(entityClass);
			List<IEntity> entities = dao.findAll();
			
			// process all entities
			int totalNum = entities.size();
			int numProcessed = 0;
			for (IEntity entity : entities) {
				// special handling if T011ObjServOperation !
				// fetch according T011ObjServ, determines syslist !
				if (entityClass.equals(T011ObjServOperation.class)) {
					T011ObjServOperation objServOp = (T011ObjServOperation) entity;
					T011ObjServ objServ = (T011ObjServ) daoT011ObjServ.loadById(objServOp.getObjServId());
					keyValueHandler.processKeyValueT011ObjServOperation(objServOp, objServ);
				} else {
					keyValueHandler.processKeyValue(entity);					
				}
				dao.makePersistent(entity);

				updateJobInfo(JobType.REBUILD_SYSLISTS, className, ++numProcessed, totalNum, userUuid);
			}
		}
	}

	/** Rebuild all indices of objects/addresses */
	public void rebuildEntitiesIndex(String userUuid) {
		// GET SINGLETON HERE AND NOT IN CONSTRUCTOR TO AVOID ENDLESS LOOPS (dependent from each other)
		MdekFullIndexHandler fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);

		List<String> uuids = null;
		int totalNum = 0;
		int numProcessed = 0;
		
		// object index

		IObjectNodeDao daoObjectNode = daoFactory.getObjectNodeDao();
		uuids = daoObjectNode.getAllObjectUuids();
		
		totalNum = uuids.size();
		numProcessed = 0;
		updateJobInfo(JobType.REBUILD_SYSLISTS, "Object Index", numProcessed, totalNum, userUuid);

		for (String uuid : uuids) {
			ObjectNode oNode = daoObjectNode.getObjectForIndex(uuid);
			fullIndexHandler.updateObjectIndex(oNode);
			updateJobInfo(JobType.REBUILD_SYSLISTS, "Object Index", ++numProcessed, totalNum, userUuid);
		}

		// address index

		IAddressNodeDao daoAddressNode = daoFactory.getAddressNodeDao();
		uuids = daoAddressNode.getAllAddressUuids();

		totalNum = uuids.size();
		numProcessed = 0;
		updateJobInfo(JobType.REBUILD_SYSLISTS, "Address Index", numProcessed, totalNum, userUuid);

		for (String uuid : uuids) {
			AddressNode aNode = daoAddressNode.getAddressForIndex(uuid);
			fullIndexHandler.updateAddressIndex(aNode);
			updateJobInfo(JobType.REBUILD_SYSLISTS, "Address Index", ++numProcessed, totalNum, userUuid);
		}
	}

	/** Flushes after every processed term. */
	public void updateSearchTerms(List<IngridDocument> termsOld, List<IngridDocument> termsNew,
			String userUuid) {

		// temporary beans containing searchterm data of passed docs for better handling
		SearchtermValue tmpOldTerm = new SearchtermValue();

		int totalNum = termsOld.size();
		int numUpdated = 0;
		for (int i=0; i < totalNum; i++) {
			IngridDocument oldDoc = termsOld.get(i);
			IngridDocument newDoc = termsNew.get(i);

			// map old doc to bean for better handling
			DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
			docToBeanMapper.mapSearchtermValueForSNSUpdate(oldDoc, tmpOldTerm);
			SearchtermType oldType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpOldTerm.getType());

			if (SearchtermType.isThesaurusTerm(oldType)) {
				updateThesaurusTerm(tmpOldTerm, newDoc, userUuid);
				
			} else if (oldType == SearchtermType.FREI) {
				updateFreeTerm(tmpOldTerm, newDoc, userUuid);
			}
			
			updateJobInfo(JobType.UPDATE_SEARCHTERMS, "Searchterm", ++numUpdated, totalNum, userUuid);
			// flush per term
			dao.flush();
		}
	}

	private void updateThesaurusTerm(SearchtermValue tmpOldTerm, IngridDocument newTermDoc, String userUuid) {
		// check type of old term
		SearchtermType oldType =
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpOldTerm.getType());
		if (!SearchtermType.isThesaurusTerm(oldType)) {
			LOG.warn("SNS Update: Old type of searchterm is NOT \"Thesaurus\", we skip this one ! " +
				"type/term:" + oldType + "/" + tmpOldTerm.getTerm());
			return;
		}

		if (newTermDoc == null) {
			// Add FREE term records with according references to Objects/Addresses ! delete thesaurus record !
			updateThesaurusTermToFree(tmpOldTerm, true, userUuid);

		} else {
			// map new doc to bean for better handling
			DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
			SearchtermValue tmpNewTerm = 
				docToBeanMapper.mapSearchtermValueForSNSUpdate(newTermDoc, new SearchtermValue());

			// check type of new term
			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpNewTerm.getType());
			if (!SearchtermType.isThesaurusTerm(newType)) {
				// ??? should not happen !!! all new terms are thesaurus terms !!!
				LOG.warn("SNS Update: New type of searchterm is NOT \"Thesaurus\", we skip this one ! doc:" + newTermDoc);
				return;
			}

			// get sns ids ("uba_thes...")
			String oldSnsId = tmpOldTerm.getSearchtermSns().getSnsId();
			String newSnsId = tmpNewTerm.getSearchtermSns().getSnsId();
			
			if (oldSnsId.equals(newSnsId)) {
				// Keep/update term record, keep/update SNS record
				updateThesaurusTermSameSnsId(tmpOldTerm, tmpNewTerm, userUuid);
				
			} else {
				String oldName = tmpOldTerm.getTerm();
				String newName = tmpNewTerm.getTerm();
				if (oldName.equals(newName)) {
					// Keep/update term record, replace SNS record (delete old SNS record)
					updateThesaurusTermNewSnsId(tmpOldTerm, tmpNewTerm, userUuid);
				} else {
					// Add FREE term records with according references to Objects/Addresses ! do NOT delete thesaurus record !
					updateThesaurusTermToFree(tmpOldTerm, false, userUuid);
					// Keep/update term record, replace SNS record (delete old SNS record)
					updateThesaurusTermNewSnsId(tmpOldTerm, tmpNewTerm, userUuid);
				}
			}
		}
	}

	/** Add term FREE records with according references to Objects/Addresses (based on thesaurus term references) !
	 * Delete thesaurus term if requested ! */
	private void updateThesaurusTermToFree(SearchtermValue inTermOld, boolean deleteThesaurusTerm, String userUuid) {
		// get all database searchterm beans !
		List<SearchtermValue> oldTermValues = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		for (SearchtermValue oldTermValue : oldTermValues) {
			
			// get all references to objects and update / create new ones
			List<SearchtermObj> oldTermObjs = daoSearchtermValue.getSearchtermObjs(oldTermValue.getId());
			int numObj = oldTermObjs.size();
			for (SearchtermObj oldTermObj : oldTermObjs) {
				// !!! create NEW FREE term per object (not connected to entity !)
				SearchtermValue freeTerm = daoSearchtermValue.loadOrCreate(
						SearchtermType.FREI.getDbValue(), oldTermValue.getTerm(), null, null, null, null);

				// connect to Object
				SearchtermObj newTermObj = oldTermObj;
				if (!deleteThesaurusTerm) {
					// we create new termObjs, the ones to thesaurus term still needed, because will not be deleted !
					newTermObj = new SearchtermObj();
					newTermObj.setLine(oldTermObj.getLine());
					newTermObj.setObjId(oldTermObj.getObjId());
				}
				newTermObj.setSearchtermId(freeTerm.getId());
				newTermObj.setSearchtermValue(freeTerm);
				dao.makePersistent(newTermObj);
			}

			// get all references to addresses and update / create new ones
			List<SearchtermAdr> oldTermAdrs = daoSearchtermValue.getSearchtermAdrs(oldTermValue.getId());
			int numAddr = oldTermAdrs.size();
			for (SearchtermAdr oldTermAdr : oldTermAdrs) {
				// create NEW FREE term per address (not connected to entity !)
				SearchtermValue freeTerm = daoSearchtermValue.loadOrCreate(
						SearchtermType.FREI.getDbValue(), oldTermValue.getTerm(), null, null, null, null);

				// connect to Address
				SearchtermAdr newTermAdr = oldTermAdr;
				if (!deleteThesaurusTerm) {
					// we create new termAdrs, the ones to thesaurus term still needed, because will not be deleted !
					newTermAdr = new SearchtermAdr();
					newTermAdr.setLine(oldTermAdr.getLine());
					newTermAdr.setAdrId(oldTermAdr.getAdrId());
				}
				newTermAdr.setSearchtermId(freeTerm.getId());
				newTermAdr.setSearchtermValue(freeTerm);
				dao.makePersistent(newTermAdr);
			}
			
			// DELETE Thesaurus TERM if requested !!! NO EXPIRED (not needed for display in IGE) !
			if (deleteThesaurusTerm) {
				dao.makeTransient(oldTermValue.getSearchtermSns());
				dao.makeTransient(oldTermValue);
			}
			
			updateJobInfoNewUpdatedTerm(oldTermValue.getTerm(), oldTermValue.getType(),
				"Deskriptor in freien Suchbegriff überführt",
				numObj, numAddr, userUuid);
		}
	}
	/** Update term record, update SNS record.
	 * NOTICE: term may change name or type (e.g. UMTHES to GEMET) */
	private void updateThesaurusTermSameSnsId(SearchtermValue inTermOld, SearchtermValue inTermNew,
			String userUuid) {
		// get all database searchterm beans ! throws exception if not SINGLE one !
		List<SearchtermValue> termValues = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		for (SearchtermValue termValue : termValues) {
			// NOTICE: may become GEMET from UMTHES !
			String oldType = termValue.getType();
			termValue.setType(inTermNew.getType());
			String oldTerm = termValue.getTerm();
			termValue.setTerm(inTermNew.getTerm());
			termValue.setEntryId(null);
			
			// NOTICE: may become GEMET from UMTHES !
			SearchtermSns termSns = termValue.getSearchtermSns();
			termSns.setSnsId(inTermNew.getSearchtermSns().getSnsId());
			termSns.setGemetId(inTermNew.getSearchtermSns().getGemetId());

			// persist both: default cascading is "none" !
			dao.makePersistent(termSns);
			dao.makePersistent(termValue);

			long numObj = daoSearchtermValue.countObjectsOfSearchterm(termValue.getId());
			long numAddr = daoSearchtermValue.countAddressesOfSearchterm(termValue.getId());

			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			updateJobInfoNewUpdatedTerm(oldTerm, oldType,
				"Deskriptor aktualisiert, ist jetzt \"" + termValue.getTerm() + "\" (" + newType + ")",
				(int)numObj, (int)numAddr, userUuid);
		}
	}
	/** Update term record, replace SNS record (delete old SNS record) */
	private void updateThesaurusTermNewSnsId(SearchtermValue inTermOld, SearchtermValue inTermNew, String userUuid) {
		// get all database searchterm beans ! throws exception if not SINGLE one !
		List<SearchtermValue> termValues = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		for (SearchtermValue termValue : termValues) {

			// load NEW sns term, may already exist ! else create it !
			SearchtermSns newTermSns = daoSearchtermSns.loadOrCreate(
					inTermNew.getSearchtermSns().getSnsId(),
					inTermNew.getSearchtermSns().getGemetId());

			// remember old sns term, will be deleted !
			// NOTICE: may be null if already deleted (when multiple SearchtermValues, e.g. "Messdaten", "Meßdaten" !) 
			SearchtermSns oldTermSns = termValue.getSearchtermSns();

			// set new data and PERSIST
			String oldType = termValue.getType();
			termValue.setType(inTermNew.getType());
			String oldName = termValue.getTerm();
			termValue.setTerm(inTermNew.getTerm());
			termValue.setEntryId(null);
			// also sns data
			termValue.setSearchtermSnsId(newTermSns.getId());
			termValue.setSearchtermSns(newTermSns);
			dao.makePersistent(termValue);

			// DELETE SNS TERM !!! NO EXPIRED (not needed for display in IGE) !
			dao.makeTransient(oldTermSns);
			
			long numObj = daoSearchtermValue.countObjectsOfSearchterm(termValue.getId());
			long numAddr = daoSearchtermValue.countAddressesOfSearchterm(termValue.getId());

			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			updateJobInfoNewUpdatedTerm(oldName, oldType,
				"Deskriptor ERSETZT durch Deskriptor \"" + termValue.getTerm() + "\" (" + newType + ")",
				(int)numObj, (int)numAddr, userUuid);
		}
	}

	private void updateFreeTerm(SearchtermValue tmpOldTerm, IngridDocument newTermDoc, String userUuid) {
		// check type of old term
		SearchtermType oldType =
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpOldTerm.getType());
		if (oldType != SearchtermType.FREI) {
			LOG.warn("SNS Update: Old type of searchterm is NOT \"FREI\", we skip this one ! " +
				"type/term:" + oldType + "/" + tmpOldTerm.getTerm());
			return;
		}

		// map new doc to bean for better handling
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SearchtermValue tmpNewTerm = 
			docToBeanMapper.mapSearchtermValueForSNSUpdate(newTermDoc, new SearchtermValue());
		SearchtermType newType = null;
		if (tmpNewTerm != null) {
			newType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpNewTerm.getType());
		}
		if (!SearchtermType.isThesaurusTerm(newType)) {
			// ??? should not happen !!! all new terms are thesaurus terms !!!
			LOG.warn("SNS Update: New type of searchterm is NOT \"Thesaurus\", we skip this one ! doc:" + newTermDoc);
			return;
		}

		String oldName = tmpOldTerm.getTerm();
		String newName = tmpNewTerm.getTerm();
		if (oldName.equals(newName)) {
			// replace free with thesaurus term
			updateFreeTermToThesaurus(tmpOldTerm, tmpNewTerm, true, userUuid);
		} else {
			// keep free term, add new thesaurus term
			updateFreeTermToThesaurus(tmpOldTerm, tmpNewTerm, false, userUuid);
		}
	}

	/** Add THESAURUS record with according references to Objects/Addresses (based on free term references) !
	 * Replace free term if requested or add thesaurus term additionally ! */
	private void updateFreeTermToThesaurus(SearchtermValue inTermOld, SearchtermValue inTermNew,
			boolean replaceFreeTerm,
			String userUuid) {

		// CREATE SNS TERM !

		// load NEW sns data, may already exist ! else create it !
		SearchtermSns newSnsData = daoSearchtermSns.loadOrCreate(
				inTermNew.getSearchtermSns().getSnsId(),
				inTermNew.getSearchtermSns().getGemetId());
		// load NEW sns term, may already exist ! else create it !
		SearchtermValue newSnsTerm = daoSearchtermValue.loadOrCreate(inTermNew.getType(),
				inTermNew.getTerm(), null, newSnsData, null, null);


		// UPDATE / CREATE NEW REFERENCES TO OBJECTS/ADDRESSES

		// get all FREE terms !
		List<SearchtermValue> oldFreeTerms = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		int numObj = 0;
		int numAddr = 0;		
		for (SearchtermValue oldFreeTerm : oldFreeTerms) {
			
			// get all references to objects and update / create new ones
			List<SearchtermObj> oldTermObjs = daoSearchtermValue.getSearchtermObjs(oldFreeTerm.getId());
			numObj += oldTermObjs.size();
			for (SearchtermObj oldTermObj : oldTermObjs) {
				// connect to Object
				SearchtermObj newTermObj = oldTermObj;
				if (!replaceFreeTerm) {
					// we create new termObjs, the ones to free term still needed, because free term is kept !
					newTermObj = new SearchtermObj();
					newTermObj.setLine(oldTermObj.getLine());
					newTermObj.setObjId(oldTermObj.getObjId());
				}
				newTermObj.setSearchtermId(newSnsTerm.getId());
				newTermObj.setSearchtermValue(newSnsTerm);
				dao.makePersistent(newTermObj);
			}

			// get all references to addresses and update  / create new ones
			List<SearchtermAdr> oldTermAdrs = daoSearchtermValue.getSearchtermAdrs(oldFreeTerm.getId());
			numAddr += oldTermAdrs.size();
			for (SearchtermAdr oldTermAdr : oldTermAdrs) {
				// connect to Address
				SearchtermAdr newTermAdr = oldTermAdr;
				if (!replaceFreeTerm) {
					// we create new termAdrs, the ones to free term still needed, because free term is kept !
					newTermAdr = new SearchtermAdr();
					newTermAdr.setLine(oldTermAdr.getLine());
					newTermAdr.setAdrId(oldTermAdr.getAdrId());
				}
				newTermAdr.setSearchtermId(newSnsTerm.getId());
				newTermAdr.setSearchtermValue(newSnsTerm);
				dao.makePersistent(newTermAdr);
			}
			
			// DELETE FREE Term if requested
			if (replaceFreeTerm) {
				dao.makeTransient(oldFreeTerm);
			}
		}
		
		SearchtermType newType =
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, newSnsTerm.getType());
		String msg;
		if (replaceFreeTerm) {
			msg = "Freien Suchbegriff ersetzt durch Deskriptor \"" + newSnsTerm.getTerm() + "\" (" + newType + ")";
		} else {
			msg = "Freien Suchbegriff ergänzt mit Deskriptor \"" + newSnsTerm.getTerm() + "\" (" + newType + ")";			
		}
		updateJobInfoNewUpdatedTerm(inTermOld.getTerm(), inTermOld.getType(), msg,
			numObj, numAddr, userUuid);
	}
}
