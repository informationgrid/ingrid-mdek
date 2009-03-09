package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to catalog data (syslists etc.).
 */
public class MdekCatalogService {

	private static MdekCatalogService myInstance;

	private IGenericDao<IEntity> daoT03Catalogue;
	private ISysListDao daoSysList;
	private ISysGuiDao daoSysGui;
	private ISysGenericKeyDao daoSysGenericKey;
	private IT08AttrTypeDao daoT08AttrType;
	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;

	private BeanToDocMapper beanToDocMapper;

	/** Get The Singleton */
	public static synchronized MdekCatalogService getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekCatalogService(daoFactory);
	      }
		return myInstance;
	}

	private MdekCatalogService(DaoFactory daoFactory) {
		daoT03Catalogue = daoFactory.getDao(T03Catalogue.class);
		daoSysList = daoFactory.getSysListDao();
		daoSysGui = daoFactory.getSysGuiDao();
		daoSysGenericKey = daoFactory.getSysGenericKeyDao();
		daoT08AttrType = daoFactory.getT08AttrTypeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
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

	/** Get language of catalog. NOTICE: transaction must be active when called the first time ! */
	public String getCatalogLanguage() {
		return getCatalog().getLanguageCode();
	}

	/** Is workflow control (QA) activated ? */
	public boolean isWorkflowEnabled() {
		return MdekUtils.YES.equals(getCatalog().getWorkflowControl());
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

	/** Get syslist entries of syslist with given id and language IN MAP.<br>
	 * entry_key is Key to Map and delivers entry_name. */
	public Map<Integer, String> getSysListKeyNameMap(int listId, String language) {
		Map map = new HashMap<Integer, String>();
		List<SysList> entries = daoSysList.getSysList(listId, language);
		for (SysList entry : entries) {
			map.put(entry.getEntryId(), entry.getName());
		}
		
		return map;
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
}
