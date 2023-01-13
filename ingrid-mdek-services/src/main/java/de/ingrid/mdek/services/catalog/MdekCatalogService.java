/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.catalog;

import java.beans.PropertyDescriptor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.Versioning;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekIgcProfileHandler;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.services.utils.MdekKeyValueHandler;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.udk.UtilsLanguageCodelist;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Encapsulates access to catalog data (syslists etc.).
 */
public class MdekCatalogService {

	private static final Logger LOG = LogManager.getLogger(MdekCatalogService.class);

	private static MdekCatalogService myInstance;

	public static String CACHE_CONFIG_FILE = "/ehcache-services.xml";
	private static String CACHE_SYS_LIST_MAP = "services-SysListMap";
	private static String CACHE_CATALOG = "services-Catalog";

	private DaoFactory daoFactory;

	private IGenericDao<IEntity> daoT03Catalogue;
	private ISysListDao daoSysList;
	private ISysGenericKeyDao daoSysGenericKey;
	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;
	private ISearchtermValueDao daoSearchtermValue;
	private ISpatialRefValueDao daoSpatialRefValue;
	private IGenericDao<IEntity> dao;

	private MdekJobHandler jobHandler;
	private MdekIgcProfileHandler profileHandler;

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
		daoSysGenericKey = daoFactory.getSysGenericKeyDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoSearchtermValue = daoFactory.getSearchtermValueDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
		dao = daoFactory.getDao(IEntity.class);

		jobHandler = MdekJobHandler.getInstance(daoFactory);
		profileHandler = MdekIgcProfileHandler.getInstance(daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);

		URL url = getClass().getResource(CACHE_CONFIG_FILE);
		cacheManager = CacheManager.create(url);
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

	/** Get language SHORTCUT (e.g. "de", "en") of catalog. USES CACHE !!!
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
			language = UtilsLanguageCodelist.getShortcutFromCode(getCatalog().getLanguageKey());
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

	/** Get all available syslist infos. */
	public List<IngridDocument> getSysListInfos() {
		List<Object[]> sysLists = daoSysList.getSysListInfos();
		
		List<IngridDocument> result = new ArrayList<IngridDocument>();
        for (Object[] row : sysLists) {
            IngridDocument retMap = new IngridDocument();
            retMap.put(MdekKeys.LST_ID, (Integer) row[0]);
            retMap.put(MdekKeys.LST_MAINTAINABLE, (Integer) row[1]);
            result.add(retMap);
        }
		
		return result;
	}

	/** Get Doc representation of syslists of given ids and language. */
	public IngridDocument getSysLists(Integer[] listIds, String language) {
		IngridDocument result = new IngridDocument();

		// use catalog language if null
		if (language == null || language.trim().length() == 0) {
			language = getCatalogLanguage();
		}

		for (int listId : listIds) {
			List<SysList> list = daoSysList.getSysList(listId, language);
			
			IngridDocument listDoc = new IngridDocument();
			beanToDocMapper.mapSysList(list, listId, listDoc);
			
			result.put(MdekKeys.SYS_LIST_KEY_PREFIX + listId,  listDoc);
		}

		return result;
	}

	/**
	 * Return the names of the syslists.
	 * Note: Not yet implemented since database has to be modified!
	 */
	/*
	public IngridDocument getSysListNames(String language) {
	    IngridDocument result = new IngridDocument();
        
//            List<SysList> list = daoSysList.getSysList(listId, language);
//            
//            IngridDocument listDoc = new IngridDocument();
//            beanToDocMapper.mapSysList(list, listId, listDoc);
//            
//            result.put(MdekKeys.SYS_LIST_KEY_PREFIX + listId,  listDoc);

        return result;
	}*/
	
	/** Get syslist entries of syslist with given id and language IN MAP. USES CACHE !<br>
	 * entry_key is Key to Map and delivers entry_name. */
	public Map<Integer, String> getSysListKeyNameMap(int listId, String language) {
		// ALWAYS USE CACHE !
		boolean useCache = true;
		String cacheKey = listId + ":" + language;

		// get map from cache if requested !
		Map<Integer, String> map = null;
		Element elem = null;
		if (useCache) {
			elem = syslistMapCache.get(cacheKey);
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
			syslistMapCache.put(new Element(cacheKey, map));
		}

		return map;
	}

	/** Get selection list of given field from profile as MAP.
	 * @param FieldKey unique key of field !
	 * @param language language 
	 * @return Map with list items in requested language or empty map !
	 */
	public Map<String, String> getProfileFieldListKeyNameMap(String fieldKey, String language) {
		return profileHandler.getFieldSelectionListMap(fieldKey, language);
	}

	public String getSysListEntryName(int listId, int entryId) {
		String language = getCatalogLanguage();
		return getSysListEntryName( listId, entryId, language );
	}
	
	public String getSysListEntryName(int listId, int entryId, String language) {
	    String entryName = null;
	    
	    Map<Integer, String> keyNameMap = getSysListKeyNameMap(listId, language);
	    if (keyNameMap != null) {
	        entryName = keyNameMap.get(entryId);
	    }
	    
	    return entryName;
	}
	
	public Integer getSysListEntryKey(int listId, String value, String language) {
	    Integer entryId = null;
	    
	    Map<Integer, String> keyNameMap = getSysListKeyNameMap(listId, language);
	    if (keyNameMap != null) {
	        for (Integer key : keyNameMap.keySet()) {
                if (value.equals( keyNameMap.get( key ) )) {
                    entryId = key;
                    break;
                }
            }
	    }
	    
	    return entryId;
	}
	
	public Integer getSysListEntryKey(int listId, String value, String language, Boolean doRobustComparison) {
	    Integer entryId = null;
	    
	    // simplify syslist value
	    if (doRobustComparison) {
	        value = value.trim().replace("—", "").replace("-", "").replace(" ", "");
	    }

	    Collection<Map<Integer, String>> localisedEntryValues = new ArrayList<Map<Integer, String>>();
	    if (language != null && language.length() > 0) {
            localisedEntryValues.add(getSysListKeyNameMap(listId, language));
        } else {
            localisedEntryValues.add( getSysListKeyNameMap(listId, "de") );
            localisedEntryValues.add( getSysListKeyNameMap(listId, "en") );
            localisedEntryValues.add( getSysListKeyNameMap(listId, "iso") );
        }
	    
	    for (Map<Integer, String> keyNameMap : localisedEntryValues) {
            
            if (keyNameMap != null) {
                for (Integer key : keyNameMap.keySet()) {
                    String compareValue = keyNameMap.get( key );
                    if (doRobustComparison) {
                        compareValue = compareValue.trim().replace("—", "").replace("-", "").replace(" ", ""); 
                    }
                    if (value.equals( compareValue )) {
                        entryId = key;
                        break;
                    }
                }
            }
	    }
        
        return entryId;
    }
	
	public Integer getInitialKeyFromListId(Integer listId) {
	    Integer entryKey = null;
        
        String language = getCatalogLanguage();
        List<SysList> list = daoSysList.getSysList(listId, language);
        
        // special behavior if default language is read and not set ! Then return catalog language !
        if (listId == 99999999) {
            entryKey = getCatalog().getLanguageKey();
        } else {
            for (SysList entry : list) {
                if ("Y".equals( entry.getIsDefault() )) {
                    entryKey = entry.getEntryId();
                    break;
                }
            }
        }
        
        
        return entryKey;
	}
	
	public String getInitialValueFromListId(Integer listId) {
	    String entryName = null;
        
        String language = getCatalogLanguage();
        List<SysList> list = daoSysList.getSysList(listId, language);
        
        // special behavior if default language is read and not set ! Then return catalog language !
        if (listId == 99999999) {
            Integer initialKey = getInitialKeyFromListId(listId);
            entryName = getSysListEntryName( listId, initialKey );
        } else {
            for (SysList entry : list) {
                if ("Y".equals( entry.getIsDefault() )) {
                    entryName = entry.getName();
                    break;
                }
            }
            
        }
        
        return entryName;
    }

	/** Get generic keys of given names AS LIST OF BEANS. PASS null if all generic keys ! */
	public List<SysGenericKey> getSysGenericKeys(String[] keyNames) {
		List<SysGenericKey> list = daoSysGenericKey.getSysGenericKeys(keyNames);
		
		return list;
	}

	/**
	 * Returns version of IGC in database (schema).
	 */
	public String getIGCVersion() throws MdekException {
		List<SysGenericKey> list =
			daoSysGenericKey.getSysGenericKeys(new String[]{Versioning.BACKEND_IGC_VERSION_KEY});
		
		String databaseIgcVersion = "";
		if (list != null && list.size() > 0) {
			databaseIgcVersion = list.get(0).getValueString();
		}
		
		return databaseIgcVersion;
	}

	/**
	 * Throws Exception if IGC schema in database conflicts with IGE iPlug.
	 */
	public void checkIGCVersion() throws MdekException {
		String databaseIgcVersion = getIGCVersion();

		if (LOG.isInfoEnabled()) {
			LOG.info("IGC schema in database version=" + databaseIgcVersion);			
			LOG.info("IGE iPlug needed version=" + Versioning.NEEDED_IGC_VERSION);
		}

        // introduce new comparison mechanism !
        // versions do not have to be equal, instead version in database (catalog) has to start with version set in IGE iPlug !
        // So IGE iPlug Version 3.6.2 and catalog version 3.6.2.2 match and IGE iPlug does not have to be updated !
        // This way we can change catalog version, e.g. when profile is updated, which does not affect IGE iPlug !
        if (!databaseIgcVersion.startsWith(Versioning.NEEDED_IGC_VERSION)) {
			String errorMsg = "IGC catalogue (schema) in database has wrong version for IGE iPlug:\n" +
				"Needed IGC version=" + Versioning.NEEDED_IGC_VERSION + " <-> current IGC Version in database=" + databaseIgcVersion;
			MdekException exc = new MdekException(errorMsg);
			LOG.error("Conflicting IGC schema in database (version=" + databaseIgcVersion +
				") with IGE iPlug (needed version=" + Versioning.NEEDED_IGC_VERSION + ") !", exc);
			throw exc;
		}
	}

	/**
	 * Update address in all objects (also published ones !).
	 * Further mod-date and -uuid in objects is updated.<br>
	 * NOTICE: already persists addresses !
	 * @param oldAddressUuid Address to be replaced
	 * @param newAddressUuid with this new Address
	 * @param userUuid calling user
	 * @return num objects updated
	 */
	public int updateAddressInObjects(String oldAddressUuid, String newAddressUuid,
			String userUuid) {
		int numAdressesChanged = 0;
		if (MdekUtils.isEqual(oldAddressUuid, newAddressUuid)) {
			return numAdressesChanged;
		}

		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// get all objects connected to the old address...
		List<T01Object> objs = 
			daoT02Address.getObjectReferencesByTypeId(oldAddressUuid, null, null);

		// process all objects
		for (T01Object obj : objs) {
			@SuppressWarnings("unchecked")
            Set<T012ObjAdr> objAdrs = obj.getT012ObjAdrs();

			// then process associations
			List<T012ObjAdr> objAdrsToRemove = new ArrayList<T012ObjAdr>();
			boolean objChanged = false;
			for (T012ObjAdr objAdr : objAdrs) {
			  if (oldAddressUuid.equals(objAdr.getAdrUuid())) {
	        // make sure no duplicates will be introduced by the replacement
			  	if (daoT01Object.hasAddressRelation(obj.getObjUuid(), newAddressUuid, objAdr.getType())) {
            // new relation would be a duplicate, mark for removal of the dataset
			  	  objAdrsToRemove.add(objAdr);
		            objChanged = true;
          } else {
            // update old address with new address
            objAdr.setAdrUuid(newAddressUuid);
            numAdressesChanged++;
            objChanged = true;
          }
				}
			}

			// persist object if changed !
			if (objChanged) {
				// first remove all associations not needed anymore !
				for (T012ObjAdr objAdrToRemove : objAdrsToRemove) {
					objAdrs.remove(objAdrToRemove);
					// delete-orphan doesn't work !!!?????
					dao.makeTransient(objAdrToRemove);
				}

				// update mod_time and mod_uuid and save (cascades save to objAdrs)
				obj.setModTime(currentTime);
				obj.setModUuid(userUuid);
				daoT01Object.makePersistent(obj);
			}
		}
		
		return numAdressesChanged;
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

		// set new data via reflection of setter methods defined by syslist
		String[] listMetadata = sysLst.getMetadata();
		if (listMetadata.length == 3) {
			String beanClassName = listMetadata[0];
			String keyProp = listMetadata[1];
			String valueProp = listMetadata[2];			

			// dynamically extract setter methods and set new values ! 
			for (IEntity entity : entities) {
				if (!entity.getClass().toString().contains(beanClassName)) {
					LOG.warn("Found entity has wrong class ! entity:'" + entity.getClass() +
							"', expected class:'" + beanClassName + "'");
					continue;
				}
				PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(entity.getClass());			
				for (PropertyDescriptor prop : props) {
					Object[] valueToSet = null;
					if (prop.getName().equals(keyProp)) {
						valueToSet = new Object[]{ new Integer(sysLstEntryId) };
					} else if (prop.getName().equals(valueProp)) {
						valueToSet = new Object[]{ sysLstEntryName };
					}
					if (valueToSet != null) {
						try {
							prop.getWriteMethod().invoke(entity, valueToSet);
						} catch (Exception ex) {
							LOG.error("Problems setting IEntity value via reflection ! entity:'" +	entity +
								"', method:'" + prop.getWriteMethod() + "', params:'" + valueToSet + "'", ex);
						}
					}
				}

				dao.makePersistent(entity);
				numReplaced++;
			}
		} else {
			LOG.error("Metadata of syslist " + sysLst.getDbValue() + " '" + sysLst + "' not set ! We cannot process free entries !");
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
	/** Update special info of UPDATE_SEARCHTERMS job IN MEMORY (Info displayed in IGE) ! */
	public void updateJobInfoNewUpdatedTerm(String termName, String alternateName, String termType,
			String msg, int numObj, int numAddr, String userUuid) {
		IngridDocument jobInfo = jobHandler.getRunningJobInfo(JobType.UPDATE_SEARCHTERMS, userUuid);
		
		List<Map> termList = (List<Map>) jobInfo.get(MdekKeys.JOBINFO_TERMS_UPDATED);
		if (termList == null) {
			termList = new ArrayList<Map>();
			jobInfo.put(MdekKeys.JOBINFO_TERMS_UPDATED, termList);
		}

		Map term = new HashMap();
		term.put(MdekKeys.TERM_NAME, termName);
		term.put(MdekKeys.TERM_ALTERNATE_NAME, alternateName);
		term.put(MdekKeys.TERM_TYPE, termType);
		term.put(MdekKeys.JOBINFO_MESSAGES, msg);
		term.put(MdekKeys.JOBINFO_NUM_OBJECTS, numObj);
		term.put(MdekKeys.JOBINFO_NUM_ADDRESSES, numAddr);

		termList.add(term);
		
		// CALL back to guarantee info "is managed" !!! also checks job canceled !
		jobHandler.updateRunningJob(userUuid, jobInfo);
	}
	/** Update special info of UPDATE_SPATIAL_REFERENCES job IN MEMORY (Info displayed in IGE) !
	 * @param oldSpatRefName name of spatial reference before update
	 * @param oldSpatRefCode code of spatial reference before update
	 * @param msg the message to display
	 * @param numObj how many objects referencing the spatial ref
	 * @param referencingObjs the referencing objects, PASS NULL if no additional data here
	 */
	public void updateJobInfoNewUpdatedSpatialRef(String oldSpatRefName, String oldSpatRefCode,
			String msg, int numObj,  List<T01Object> referencingObjs, String userUuid) {
		IngridDocument jobInfo = jobHandler.getRunningJobInfo(JobType.UPDATE_SPATIAL_REFERENCES, userUuid);
		
		List<Map> spRefList = (List<Map>) jobInfo.get(MdekKeys.JOBINFO_LOCATIONS_UPDATED);
		if (spRefList == null) {
			spRefList = new ArrayList<Map>();
			jobInfo.put(MdekKeys.JOBINFO_LOCATIONS_UPDATED, spRefList);
		}

		Map spRef = new HashMap();
		spRef.put(MdekKeys.LOCATION_NAME, oldSpatRefName);
		spRef.put(MdekKeys.LOCATION_CODE, oldSpatRefCode);
		spRef.put(MdekKeys.JOBINFO_MESSAGES, msg);
		spRef.put(MdekKeys.JOBINFO_NUM_OBJECTS, numObj);

		// add referencing objects info
		if (referencingObjs != null) {
			BeanToDocMapper beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
			List<IngridDocument> objDocs = new ArrayList<IngridDocument>();
			for (T01Object obj : referencingObjs) {
				IngridDocument objDoc =
					beanToDocMapper.mapT01Object(obj, new IngridDocument(), MappingQuantity.BASIC_ENTITY);
				beanToDocMapper.mapResponsibleUser(obj.getResponsibleUuid(), objDoc, MappingQuantity.INITIAL_ENTITY);
				objDocs.add(objDoc);
			}
			spRef.put(MdekKeys.OBJ_ENTITIES, objDocs);			
		}

		spRefList.add(spRef);
		
		// CALL back to guarantee info "is managed" !!! also checks job canceled !
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
		} else if (jobType == JobType.UPDATE_SPATIAL_REFERENCES) {
			jobDetails.put(MdekKeys.JOBINFO_LOCATIONS_UPDATED, runningJobInfo.get(MdekKeys.JOBINFO_LOCATIONS_UPDATED));
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
				if (entityClass.equals(T011ObjServ.class)) {
					// special handling if T011ObjServ !
					// fetch according T01Object, determines syslist !
					T011ObjServ objServ = (T011ObjServ) entity;
					T01Object obj = (T01Object) daoT01Object.loadById(objServ.getObjId());
					keyValueHandler.processKeyValueT011ObjServ(objServ, obj);
				} else if (entityClass.equals(T011ObjServOperation.class)) {
					// special handling if T011ObjServOperation !
					// fetch according T011ObjServ, determines syslist !
					T011ObjServOperation objServOp = (T011ObjServOperation) entity;
					T011ObjServ objServ = (T011ObjServ) daoT011ObjServ.loadById(objServOp.getObjServId());
					keyValueHandler.processKeyValueT011ObjServOperation(objServOp, objServ);
                } else if (entityClass.equals(T011ObjServVersion.class)) {
                    T011ObjServVersion objServVersion = (T011ObjServVersion) entity;
                    T011ObjServ objServ = (T011ObjServ) daoT011ObjServ.loadById(objServVersion.getObjServId());
                    keyValueHandler.processKeyValueT011ObjServVersion(objServVersion, objServ);
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
			// skip folders which are excluded from address search
			if (aNode != null) {
			    fullIndexHandler.updateAddressIndex(aNode);
			}
			updateJobInfo(JobType.REBUILD_SYSLISTS, "Address Index", ++numProcessed, totalNum, userUuid);
		}
	}

	/** Flushes after every processed term. */
	public void updateSearchTerms(List<IngridDocument> termsOld, List<IngridDocument> termsNew,
			String userUuid) {

		int totalNum = termsOld.size();
		int numUpdated = 0;
		for (int i=0; i < totalNum; i++) {
			IngridDocument oldDoc = termsOld.get(i);
			IngridDocument newDoc = termsNew.get(i);

			// map old doc to bean for better handling
			DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
			SearchtermValue tmpOldTerm =
				docToBeanMapper.mapHelperSearchtermValue(oldDoc, new SearchtermValue());
			SearchtermType oldType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpOldTerm.getType());

			if (SearchtermType.isThesaurusType(oldType)) {
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
		if (!SearchtermType.isThesaurusType(oldType)) {
			LOG.warn("SNS Update: Old type of searchterm is NOT \"Thesaurus\", we skip this one ! " +
				"type/term: " + oldType + "/" + tmpOldTerm.getTerm());
			return;
		}

		if (newTermDoc == null) {
			// Add FREE term records with according references to Objects/Addresses ! delete thesaurus record !
			updateThesaurusTermToFree(tmpOldTerm, true, userUuid);

		} else {
			// map new doc to bean for better handling
			DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
			SearchtermValue tmpNewTerm = 
				docToBeanMapper.mapHelperSearchtermValue(newTermDoc, new SearchtermValue());

			// check type of new term
			SearchtermType newType = null;
			if (tmpNewTerm != null) {
				newType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpNewTerm.getType());
			}
			if (!SearchtermType.isThesaurusType(newType)) {
				// ??? should not happen !!! all new terms are thesaurus terms !!!
				LOG.warn("SNS Update: New type of searchterm is NOT \"Thesaurus\", we skip this one ! doc:" + newTermDoc);
				return;
			}

			// get sns ids ("uba_thes...")
			String oldSnsId = tmpOldTerm.getSearchtermSns().getSnsId();
			String newSnsId = tmpNewTerm.getSearchtermSns().getSnsId();
			
			if (oldSnsId.equals(newSnsId)) {
				// Keep/update term record, keep/update SNS record
				updateThesaurusTermSameSnsId(tmpOldTerm, newTermDoc, userUuid);
				
			} else {
				String oldName = tmpOldTerm.getTerm();
				String newName = tmpNewTerm.getTerm();
				if (oldName.equals(newName)) {
					// Keep/update term record, replace SNS record (delete old SNS record)
					updateThesaurusTermNewSnsId(tmpOldTerm, newTermDoc, userUuid);
				} else {
					// Add FREE term records with according references to Objects/Addresses ! do NOT delete thesaurus record !
					updateThesaurusTermToFree(tmpOldTerm, false, userUuid);
					// Keep/update term record, replace SNS record (delete old SNS record)
					updateThesaurusTermNewSnsId(tmpOldTerm, newTermDoc, userUuid);
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
			int numObj = 0;
			for (SearchtermObj oldTermObj : oldTermObjs) {
				// first check whether connected free term already exists !
				SearchtermValue existingFreeTerm = daoSearchtermValue.loadSearchterm(SearchtermType.FREI.getDbValue(),
						oldTermValue.getTerm(), null, null, oldTermObj.getObjId(), IdcEntityType.OBJECT);

				if (existingFreeTerm != null) {
					// connected FREE term already exists !
					// delete all references to thesaurus term if term should be deleted 
					if (deleteThesaurusTerm) {
						dao.makeTransient(oldTermObj);
						numObj++;
					}
				} else {
					// FREE term does not exist, create it !
					// !!! create NEW FREE term per object (not connected to entity !)
					SearchtermValue freeTerm = daoSearchtermValue.loadOrCreate(
							SearchtermType.FREI.getDbValue(),
							oldTermValue.getTerm(), null, null, null, null, null);

					// connect to Object
					SearchtermObj newTermObj = oldTermObj;
					if (!deleteThesaurusTerm) {
						// we create new termObjs, the ones to thesaurus term still needed, because term will not be deleted !
						newTermObj = new SearchtermObj();
						newTermObj.setLine(oldTermObj.getLine());
						newTermObj.setObjId(oldTermObj.getObjId());
					}
					newTermObj.setSearchtermId(freeTerm.getId());
					newTermObj.setSearchtermValue(freeTerm);
					dao.makePersistent(newTermObj);
					numObj++;
				}
			}

			// get all references to addresses and update / create new ones
			List<SearchtermAdr> oldTermAdrs = daoSearchtermValue.getSearchtermAdrs(oldTermValue.getId());
			int numAddr = 0;
			for (SearchtermAdr oldTermAdr : oldTermAdrs) {
				// first check whether connected free term already exists !
				SearchtermValue existingFreeTerm = daoSearchtermValue.loadSearchterm(SearchtermType.FREI.getDbValue(),
						oldTermValue.getTerm(), null, null, oldTermAdr.getAdrId(), IdcEntityType.ADDRESS);

				if (existingFreeTerm != null) {
					// connected FREE term already exists !
					// delete all references to thesaurus term if term should be deleted 
					if (deleteThesaurusTerm) {
						dao.makeTransient(oldTermAdr);
						numAddr++;
					}
				} else {
					// FREE term does not exist, create it !
					// !!! create NEW FREE term per address (not connected to entity !)
					SearchtermValue freeTerm = daoSearchtermValue.loadOrCreate(
							SearchtermType.FREI.getDbValue(),
							oldTermValue.getTerm(), null, null, null, null, null);

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
					numAddr++;
				}
			}
			
			// DELETE Thesaurus TERM if requested !!! NO EXPIRED (not needed for display in IGE) !
			if (deleteThesaurusTerm) {
				dao.makeTransient(oldTermValue.getSearchtermSns());
				dao.makeTransient(oldTermValue);
			}

			// update job info only if obj/addr processed !
			if (numObj > 0 || numAddr > 0) {
				updateJobInfoNewUpdatedTerm(oldTermValue.getTerm(), oldTermValue.getAlternateTerm(),
					oldTermValue.getType(),
					"Deskriptor in freien Suchbegriff überführt",
					numObj, numAddr, userUuid);
			}
		}
	}
	/** Update term record, update SNS record.
	 * NOTICE: term may change name or type (e.g. UMTHES to GEMET) */
	private void updateThesaurusTermSameSnsId(SearchtermValue inTermOld, IngridDocument inTermNewDoc,
			String userUuid) {
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);

		// get all database searchterm beans !
		List<SearchtermValue> termValues = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		for (SearchtermValue termValue : termValues) {
			// remember old values
			String oldType = termValue.getType();
			String oldTerm = termValue.getTerm();
			String oldAlternateTerm = termValue.getAlternateTerm();

			// set new data and PERSIST
			// NOTICE: may become GEMET from UMTHES !
			SearchtermSns termSns = 
				docToBeanMapper.mapSearchtermSns(inTermNewDoc, termValue.getSearchtermSns());
			docToBeanMapper.mapSearchtermValue(termSns, inTermNewDoc, termValue);

			// persist both: default cascading is "none" !
			dao.makePersistent(termSns);
			dao.makePersistent(termValue);

			long numObj = daoSearchtermValue.countObjectsOfSearchterm(termValue.getId());
			long numAddr = daoSearchtermValue.countAddressesOfSearchterm(termValue.getId());

			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			String msg;
			if (newType == SearchtermType.GEMET) {
				msg = "Deskriptor aktualisiert, ist jetzt \"" + termValue.getTerm() + "\" (" + newType + "), " +
					"\"" + termValue.getAlternateTerm() + "\" (alternativ)";			
			} else {
				msg = "Deskriptor aktualisiert, ist jetzt \"" + termValue.getTerm() + "\" (" + newType + ")";			
			}
			updateJobInfoNewUpdatedTerm(oldTerm, oldAlternateTerm, oldType, msg,
				(int)numObj, (int)numAddr, userUuid);
		}
	}
	/** Load/create new term, delete old term. Update/delete obj/addr references
	 * dependent from whether new term already connected ! */
	private void updateThesaurusTermNewSnsId(SearchtermValue inTermOld, IngridDocument inTermNewDoc,
			String userUuid) {

		// load/create NEW spRefValue !
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SearchtermValue newSnsTermValue =
			docToBeanMapper.loadOrCreateSearchtermValueViaDoc(inTermNewDoc, null, null, true);

		// fetch all object ids where new term is already connected to
		Set<Long> objIdsOfNewTerm =
			new HashSet<Long>(daoSearchtermValue.getSearchtermObj_objIds(newSnsTermValue.getId()));
		// fetch all address ids where new term is connected to
		Set<Long> addrIdsOfNewTerm =
			new HashSet<Long>(daoSearchtermValue.getSearchtermAdr_adrIds(newSnsTermValue.getId()));

		// get all database searchterm beans !
		List<SearchtermValue> oldTermValues = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());
		
		// and process
		for (SearchtermValue oldTermValue : oldTermValues) {
			// get all object refs and check whether objects already have NEW sns term ! 
			List<SearchtermObj> oldTermObjs = daoSearchtermValue.getSearchtermObjs(oldTermValue.getId());
			int numProcessedObj = 0;
			for (SearchtermObj oldTermObj : oldTermObjs) {
				// first check whether new thesaurus term already connected to object !
				if (objIdsOfNewTerm.contains(oldTermObj.getObjId())) {
					// new thesaurus term already connected
					// delete all old references 
					dao.makeTransient(oldTermObj);
				} else {
					// new thesaurus term NOT connected
					// connect thesaurus term to Object, use old term connection
					oldTermObj.setSearchtermId(newSnsTermValue.getId());
					oldTermObj.setSearchtermValue(newSnsTermValue);
					dao.makePersistent(oldTermObj);
					objIdsOfNewTerm.add(oldTermObj.getObjId());
				}
				numProcessedObj++;
			}

			// get all address refs and check whether addresses already have NEW sns term ! 
			List<SearchtermAdr> oldTermAdrs = daoSearchtermValue.getSearchtermAdrs(oldTermValue.getId());
			int numProcessedAddr = 0;
			for (SearchtermAdr oldTermAdr : oldTermAdrs) {
				// first check whether new thesaurus term already connected to address !
				if (addrIdsOfNewTerm.contains(oldTermAdr.getAdrId())) {
					// new thesaurus term already connected
					// delete all old references 
					dao.makeTransient(oldTermAdr);
				} else {
					// new thesaurus term NOT connected
					// connect thesaurus term to Address, use old term connection
					oldTermAdr.setSearchtermId(newSnsTermValue.getId());
					oldTermAdr.setSearchtermValue(newSnsTermValue);
					dao.makePersistent(oldTermAdr);
					addrIdsOfNewTerm.add(oldTermAdr.getAdrId());
				}
				numProcessedAddr++;
			}

			// DELETE OLD STUFF !!! NO EXPIRED (not needed for display in IGE) !
			// NOTICE: SearchtermSns may be null if already deleted (when multiple SearchtermValues, e.g. "Messdaten", "Meßdaten" !)
			dao.makeTransient(oldTermValue.getSearchtermSns());
			dao.makeTransient(oldTermValue);
			
			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, newSnsTermValue.getType());
			String msg;
			if (newType == SearchtermType.GEMET) {
				msg = "Deskriptor ERSETZT durch Deskriptor \"" + newSnsTermValue.getTerm() + "\" (" + newType + "), " +
					"\"" + newSnsTermValue.getAlternateTerm() + "\" (alternativ)";			
			} else {
				msg = "Deskriptor ERSETZT durch Deskriptor \"" + newSnsTermValue.getTerm() + "\" (" + newType + ")";			
			}
			updateJobInfoNewUpdatedTerm(oldTermValue.getTerm(), oldTermValue.getAlternateTerm(),
				oldTermValue.getType(), msg, numProcessedObj, numProcessedAddr, userUuid);
		}
	}

	private void updateFreeTerm(SearchtermValue tmpOldTerm, IngridDocument newTermDoc, String userUuid) {
		// check type of old term
		SearchtermType oldType =
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpOldTerm.getType());
		if (oldType != SearchtermType.FREI) {
			LOG.warn("SNS Update: Old type of searchterm is NOT \"FREI\", we skip this one ! " +
				"type/term: " + oldType + "/" + tmpOldTerm.getTerm());
			return;
		}

		// map new doc to bean for better handling
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SearchtermValue tmpNewTerm = 
			docToBeanMapper.mapHelperSearchtermValue(newTermDoc, new SearchtermValue());
		SearchtermType newType = null;
		if (tmpNewTerm != null) {
			newType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, tmpNewTerm.getType());
		}
		if (!SearchtermType.isThesaurusType(newType)) {
			// ??? should not happen !!! all new terms are thesaurus terms !!!
			LOG.warn("SNS Update: New type of searchterm is NOT \"Thesaurus\", we skip this one ! doc:" + newTermDoc);
			return;
		}

		String oldName = tmpOldTerm.getTerm();
		String newName = tmpNewTerm.getTerm();
		if (oldName.equals(newName)) {
			// replace free with thesaurus term
			updateFreeTermToThesaurus(tmpOldTerm, newTermDoc, true, userUuid);
		} else {
			// keep free term, add new thesaurus term
			updateFreeTermToThesaurus(tmpOldTerm, newTermDoc, false, userUuid);
		}
	}

	/** Add THESAURUS record with according references to Objects/Addresses (based on free term references) !
	 * Replace free term if requested or add thesaurus term additionally ! */
	private void updateFreeTermToThesaurus(SearchtermValue inTermOld, IngridDocument inTermNewDoc,
			boolean replaceFreeTerm,
			String userUuid) {

		// load/create NEW spRefValue !
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SearchtermValue newSnsTermValue =
			docToBeanMapper.loadOrCreateSearchtermValueViaDoc(inTermNewDoc, null, null, true);

		// fetch all object ids where new term is already connected to
		Set<Long> objIdsOfNewTerm =
			new HashSet<Long>(daoSearchtermValue.getSearchtermObj_objIds(newSnsTermValue.getId()));
		// fetch all address ids where new term is connected to
		Set<Long> addrIdsOfNewTerm =
			new HashSet<Long>(daoSearchtermValue.getSearchtermAdr_adrIds(newSnsTermValue.getId()));

		// UPDATE / CREATE NEW REFERENCES TO OBJECTS/ADDRESSES

		// get all FREE terms !
		List<SearchtermValue> oldFreeTerms = daoSearchtermValue.getSearchtermValues(
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, inTermOld.getType()),
				inTermOld.getTerm(),
				inTermOld.getSearchtermSns().getSnsId());

		// and process
		int numProcessedObj = 0;
		int numProcessedAddr = 0;		
		for (SearchtermValue oldFreeTerm : oldFreeTerms) {
			
			// get all references to objects and update / create new ones
			List<SearchtermObj> oldTermObjs = daoSearchtermValue.getSearchtermObjs(oldFreeTerm.getId());
			for (SearchtermObj oldTermObj : oldTermObjs) {
				// first check whether connected thesaurus term already exists !
				if (objIdsOfNewTerm.contains(oldTermObj.getObjId())) {
					// connected thesaurus term already exists !
					// delete all references and free term if term should be replaced 
					if (replaceFreeTerm) {
						dao.makeTransient(oldTermObj);
						numProcessedObj++;
					}
				} else {
					// Thesaurus term does not exist !
					// connect thesaurus term to Object, default: use free term connections
					SearchtermObj newTermObj = oldTermObj;
					if (!replaceFreeTerm) {
						// we create new termObjs, the ones to free term still needed, because free term is kept !
						newTermObj = new SearchtermObj();
						newTermObj.setLine(oldTermObj.getLine());
						newTermObj.setObjId(oldTermObj.getObjId());
					}
					newTermObj.setSearchtermId(newSnsTermValue.getId());
					newTermObj.setSearchtermValue(newSnsTermValue);
					dao.makePersistent(newTermObj);
					objIdsOfNewTerm.add(newTermObj.getObjId());
					numProcessedObj++;
				}
			}

			// get all references to addresses and update  / create new ones
			List<SearchtermAdr> oldTermAdrs = daoSearchtermValue.getSearchtermAdrs(oldFreeTerm.getId());
			for (SearchtermAdr oldTermAdr : oldTermAdrs) {
				// first check whether connected thesaurus term already exists !
				if (addrIdsOfNewTerm.contains(oldTermAdr.getAdrId())) {
					// connected thesaurus term already exists !
					// delete all references and free term if term should be replaced 
					if (replaceFreeTerm) {
						dao.makeTransient(oldTermAdr);
						numProcessedAddr++;
					}
				} else {
					// Thesaurus term does not exist !
					// connect thesaurus term to Address, default: use free term connections
					SearchtermAdr newTermAdr = oldTermAdr;
					if (!replaceFreeTerm) {
						// we create new termAdrs, the ones to free term still needed, because free term is kept !
						newTermAdr = new SearchtermAdr();
						newTermAdr.setLine(oldTermAdr.getLine());
						newTermAdr.setAdrId(oldTermAdr.getAdrId());
					}
					newTermAdr.setSearchtermId(newSnsTermValue.getId());
					newTermAdr.setSearchtermValue(newSnsTermValue);
					dao.makePersistent(newTermAdr);
					addrIdsOfNewTerm.add(newTermAdr.getAdrId());
					numProcessedAddr++;
				}
			}
			
			// DELETE FREE Term if requested
			if (replaceFreeTerm) {
				dao.makeTransient(oldFreeTerm);
			}
		}

		// update job info only if obj/addr processed !
		if (numProcessedObj > 0 || numProcessedAddr > 0) {
			SearchtermType newType =
				EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, newSnsTermValue.getType());
			String newTermTag;
			if (newType == SearchtermType.GEMET) {
				newTermTag = "\"" + newSnsTermValue.getTerm() + "\" (" + newType + "), " +
					"\"" + newSnsTermValue.getAlternateTerm() + "\" (alternativ)";			
			} else {
				newTermTag = "\"" + newSnsTermValue.getTerm() + "\" (" + newType + ")";			
			}
			String msg;
			if (replaceFreeTerm) {
				msg = "Freien Suchbegriff ersetzt durch Deskriptor " + newTermTag;
			} else {
				msg = "Freien Suchbegriff ergänzt mit Deskriptor " + newTermTag;
			}
			updateJobInfoNewUpdatedTerm(inTermOld.getTerm(), inTermOld.getAlternateTerm(),
				inTermOld.getType(), msg,
				numProcessedObj, numProcessedAddr, userUuid);
		}
	}

	/** Flushes after every processed location. */
	public void updateSpatialReferences(List<IngridDocument> spRefsOld, List<IngridDocument> spRefsNew,
			String userUuid) {

		// temporary beans containing searchterm data of passed docs for better handling

		int totalNum = spRefsOld.size();
		int numUpdated = 0;
		
		for (int i=0; i < totalNum; i++) {
			IngridDocument oldDoc = spRefsOld.get(i);
			IngridDocument newDoc = spRefsNew.get(i);

			// map old doc to bean for better handling
			DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
			SpatialRefValue tmpOldSpRef = 
				docToBeanMapper.mapHelperSpatialRefValue(oldDoc, new SpatialRefValue());

			// check type of old term
			SpatialReferenceType oldType =
				EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, tmpOldSpRef.getType());
			if (!SpatialReferenceType.isThesaurusType(oldType)) {
				LOG.warn("SNS Update: Old type of spatial reference is NOT \"Thesaurus\", we skip this one ! " +
					"type/term: " + oldType + "/" + tmpOldSpRef.getNameValue());
			} else {
				
				// process

				// Is the location expired ?
				boolean isExpired = newDoc == null || newDoc.get(MdekKeys.LOCATION_EXPIRED_AT) != null;
				// Do we have successors ?
				boolean hasSuccessors = newDoc != null && newDoc.get(MdekKeys.SUCCESSORS) != null;
				// should the old location and all references to it should be deleted ?
				boolean deleteOldLocation = false;

				// first process new location document, not the successors !

				// is location expired
				if (isExpired) {
					// EXPIRED

					// Only set expired if NO successors.
					if (!hasSuccessors) {
						updateSpatialRefToExpired(tmpOldSpRef, userUuid);						
					} else {
						// if successors then old location should be deleted (see below) !
						deleteOldLocation = true;						
					}

				} else {
					// NOT expired !

					// map new doc to bean for better handling
					SpatialRefValue tmpNewSpRef = 
						docToBeanMapper.mapHelperSpatialRefValue(newDoc, new SpatialRefValue());

					// check type of new location
					SpatialReferenceType newType =
						EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, tmpNewSpRef.getType());
					if (!SpatialReferenceType.isThesaurusType(newType)) {
						LOG.warn("SNS Update: New type of spatial reference is NOT \"Geo-Thesaurus\", we skip this one ! " +
							"type/term: " + newType + "/" + tmpNewSpRef.getNameValue());
					}

					String oldSnsId = tmpOldSpRef.getSpatialRefSns().getSnsId();
					String newSnsId = tmpNewSpRef.getSpatialRefSns().getSnsId();
					if (oldSnsId.equals(newSnsId)) {
						updateSpatialRefValueSameSnsId(tmpOldSpRef, newDoc, userUuid);

					} else {
						// add new location and delete the old one !
						updateSpatialRefValueNewSnsId(tmpOldSpRef, newDoc, true, userUuid);
					}
				}

				// then process successors ! deleteOldLocation determines whether old location should be deleted !
				if (hasSuccessors) {
					List<IngridDocument> successors = (List<IngridDocument>) newDoc.get(MdekKeys.SUCCESSORS);

					// we have successors of a location, process them !
					for (IngridDocument successorDoc : successors) {
						updateSpatialRefValueNewSnsId(tmpOldSpRef, successorDoc, deleteOldLocation, userUuid);
					}
				}
			}

			updateJobInfo(JobType.UPDATE_SPATIAL_REFERENCES, "SpatialRefValue", ++numUpdated, totalNum, userUuid);
			// flush per spatial reference
			dao.flush();
		}
	}
	/** Set given location to expired !
	 * @param inRefOld location to be expired
	 * @param userUuid user needed for update of job info
	 */
	private void updateSpatialRefToExpired(SpatialRefValue inRefOld, String userUuid) {
		// get all database beans ! should be only one !
		List<SpatialRefValue> spRefValues = daoSpatialRefValue.getSpatialRefValues(
				EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, inRefOld.getType()),
				inRefOld.getNameValue(),
				inRefOld.getSpatialRefSns().getSnsId());

		// and process
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		for (SpatialRefValue spRefValue : spRefValues) {
			// set expired date !
			spRefValue.getSpatialRefSns().setExpiredAt(currentTime);
			dao.makePersistent(spRefValue.getSpatialRefSns());
			
			// get all objects referencing expired spatial ref
			List<T01Object> objs = daoSpatialRefValue.getObjectsOfSpatialRefValue(spRefValue.getId());

			updateJobInfoNewUpdatedSpatialRef(spRefValue.getNameValue(), spRefValue.getNativekey(), 
				"entfällt (expired)", objs.size(), objs, userUuid);
		}
	}
	/** Update old location with new data ! Database structures are kept and data updated !
	 * @param inRefOld old location
	 * @param inRefNewDoc new location as doc
	 * @param userUuid user needed for update of job info
	 */
	private void updateSpatialRefValueSameSnsId(SpatialRefValue inRefOld, IngridDocument inRefNewDoc,
			String userUuid) {
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SpatialRefValue inRefNew = 
			docToBeanMapper.mapHelperSpatialRefValue(inRefNewDoc, new SpatialRefValue());

		// get all database beans !
		List<SpatialRefValue> spRefValues = daoSpatialRefValue.getSpatialRefValues(
				EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, inRefOld.getType()),
				inRefOld.getNameValue(),
				inRefOld.getSpatialRefSns().getSnsId());

		// and process
		for (SpatialRefValue spRefValue : spRefValues) {
			// set up message
			String msg = getSpatialRefChangesMsg(spRefValue, inRefNew);
			
			// remember old data
			String oldName = spRefValue.getNameValue();
			String oldCode = spRefValue.getNativekey();

			// set new data and PERSIST
			docToBeanMapper.mapSpatialRefValue(spRefValue.getSpatialRefSns(), inRefNewDoc, spRefValue);
			dao.makePersistent(spRefValue);

			long numObj = daoSpatialRefValue.countObjectsOfSpatialRefValue(spRefValue.getId());

			updateJobInfoNewUpdatedSpatialRef(oldName, oldCode, msg, (int)numObj, null, userUuid);
		}
	}
	/** Creates a new location (new database structures !) and adds it to all objects containing the old location.
	 * @param inRefOld old location
	 * @param inRefNewDoc new location as doc
	 * @param deleteOldLocation true=Old location and all references are deleted from objects,
	 * 		false=old location and references are maintained !
	 * @param userUuid user needed for update of job info
	 */
	private void updateSpatialRefValueNewSnsId(SpatialRefValue inRefOld, IngridDocument inRefNewDoc,
			boolean deleteOldLocation,
			String userUuid) {

		// load/create NEW sns spRefValue, may already exist ! else create it (including spRefSns) !
		DocToBeanMapper docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
		SpatialRefValue newSpRefValue =
			docToBeanMapper.loadOrCreateSpatialRefValueViaDoc(inRefNewDoc, null, true);

		// fetch all object ids where new spRefValue is already connected to
		Set<Long> objIdsOfNewSpRef =
			new HashSet<Long>(daoSpatialRefValue.getObjectIdsOfSpatialRefValue(newSpRefValue.getId()));

		// get all old database spRefValue beans !
		List<SpatialRefValue> oldSpRefValues = daoSpatialRefValue.getSpatialRefValues( 
				EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, inRefOld.getType()),
				inRefOld.getNameValue(),
				inRefOld.getSpatialRefSns().getSnsId());
		
		T03Catalogue catalog = getCatalog();
		
		// and process
		for (SpatialRefValue oldSpRefValue : oldSpRefValues) {
			// get all object refs and check whether objects already have NEW sns spRef ! 
			List<SpatialReference> oldSpRefs = daoSpatialRefValue.getSpatialReferences(oldSpRefValue.getId());
			int numProcessedObj = 0;
			for (SpatialReference oldSpRef : oldSpRefs) {
				Long objId = oldSpRef.getObjId();
				T01Object obj = (T01Object) daoT01Object.loadById(objId);

				// delete reference to old location
				if (deleteOldLocation) {
					// remove from set in object before making transient to avoid exception !
					obj.getSpatialReferences().remove(oldSpRef);
					dao.makeTransient(oldSpRef);
				}

				// add new location if not present yet !
				if (! objIdsOfNewSpRef.contains(objId)) {
					// add location, already persisted !
					docToBeanMapper.addSpatialReference(obj, newSpRefValue);
					objIdsOfNewSpRef.add(objId);
				}

				numProcessedObj++;
			}

			// set up message
			String msg = getSpatialRefChangesMsg(oldSpRefValue, newSpRefValue);
			
			// remember old data
			String oldName = oldSpRefValue.getNameValue();
			String oldCode = oldSpRefValue.getNativekey();

			// DELETE OLD STUFF !!! NO EXPIRED, we replaced spatial ref
			// NOTICE: SpatialRefSns may be null if already deleted (when multiple SpatialRefValues)
			if (deleteOldLocation) {
				dao.makeTransient(oldSpRefValue.getSpatialRefSns());
				dao.makeTransient(oldSpRefValue);				
			}

			updateJobInfoNewUpdatedSpatialRef(oldName, oldCode, msg, numProcessedObj, null, userUuid);
			
			// Update id for spatial reference in catalog!!!
            if (catalog.getSpatialRefId().equals( oldSpRefValue.getId() )) {
                catalog.setSpatialRefId( newSpRefValue.getId() );
                catalog.setSpatialRefValue(newSpRefValue);
                daoT03Catalogue.makePersistent( catalog );
            }
			
		}
	}
	private String getSpatialRefChangesMsg(SpatialRefValue spRefValueOld, SpatialRefValue spRefValueNew) {
		// set up message
		String msg = null;
		if (!MdekUtils.isEqual(spRefValueOld.getNameValue(), spRefValueNew.getNameValue())) {
			msg = "neue Bezeichnung \"" + spRefValueNew.getNameValue() + "\"";
		}
		if (!MdekUtils.isEqual(spRefValueOld.getX1(), spRefValueNew.getX1()) ||
			!MdekUtils.isEqual(spRefValueOld.getX2(), spRefValueNew.getX2()) ||
			!MdekUtils.isEqual(spRefValueOld.getY1(), spRefValueNew.getY1()) ||
			!MdekUtils.isEqual(spRefValueOld.getY2(), spRefValueNew.getY2())) {
			msg = (msg == null) ? "" : (msg + ", ");
			msg += "neuer Grenzverlauf";
		}
		if (!MdekUtils.isEqual(spRefValueOld.getTopicType(), spRefValueNew.getTopicType())) {
			msg = (msg == null) ? "" : (msg + ", ");
			msg += "neuer Topic-Type \"" + spRefValueNew.getTopicType() + "\"";
		}
		if (!MdekUtils.isEqual(spRefValueOld.getSpatialRefSns().getSnsId(), spRefValueNew.getSpatialRefSns().getSnsId())) {
			msg = (msg == null) ? "" : (msg + ", ");
			msg += "neue Topic-Id \"" + spRefValueNew.getSpatialRefSns().getSnsId() + "\"";
		}
		if (msg == null) {
			msg = "Raumeinheit geändert";
		}

		return msg;
	}
}
