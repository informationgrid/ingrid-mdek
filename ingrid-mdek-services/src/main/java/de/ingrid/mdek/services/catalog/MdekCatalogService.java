package de.ingrid.mdek.services.catalog;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
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

	/** "logs" Start-Info of RebuildSyslist job IN MEMORY and IN DATABASE */
	public void startRebuildSyslistJobInfo(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.REBUILD_SYSLISTS, 0, 0, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
		jobHandler.startJobInfoDB(JobType.REBUILD_SYSLISTS, startTime, null, userUuid);
	}
	/** Update general info of RebuildSyslist job IN MEMORY. */
	public void updateRebuildSyslistJobInfo(String entityType, int numUpdated, int totalNum, String userUuid) {
		// update in memory job state.
		IngridDocument jobDoc = jobHandler.createRunningJobDescription(
				JobType.REBUILD_SYSLISTS, entityType, numUpdated, totalNum, false);
		jobHandler.updateRunningJob(userUuid, jobDoc);
	}
	/**
	 * "logs" End-Info in rebuild job information IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param userUuid calling user
	 */
	public void endRebuildSyslistJobInfo(String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails = jobHandler.getJobInfoDetailsFromRunningJobInfo(
				runningJobInfo, false);

		// then update job info in database
		jobHandler.updateJobInfoDB(JobType.REBUILD_SYSLISTS, jobDetails, userUuid);
		// add end info
		jobHandler.endJobInfoDB(JobType.REBUILD_SYSLISTS, userUuid);
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

				updateRebuildSyslistJobInfo(className, ++numProcessed, totalNum, userUuid);
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
		updateRebuildSyslistJobInfo("Object Index", numProcessed, totalNum, userUuid);

		for (String uuid : uuids) {
			ObjectNode oNode = daoObjectNode.getObjectForIndex(uuid);
			fullIndexHandler.updateObjectIndex(oNode);
			updateRebuildSyslistJobInfo("Object Index", ++numProcessed, totalNum, userUuid);
		}

		// address index

		IAddressNodeDao daoAddressNode = daoFactory.getAddressNodeDao();
		uuids = daoAddressNode.getAllAddressUuids();

		totalNum = uuids.size();
		numProcessed = 0;
		updateRebuildSyslistJobInfo("Address Index", numProcessed, totalNum, userUuid);

		for (String uuid : uuids) {
			AddressNode aNode = daoAddressNode.getAddressForIndex(uuid);
			fullIndexHandler.updateAddressIndex(aNode);
			updateRebuildSyslistJobInfo("Address Index", ++numProcessed, totalNum, userUuid);
		}
	}
}
