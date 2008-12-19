package de.ingrid.mdek.services.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.SysList;
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
	private IT08AttrTypeDao daoT08AttrType;

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
		daoT08AttrType = daoFactory.getT08AttrTypeDao();

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

	/** Get Doc representation of DEFINITIONS of additional fields of given ids and language (for items in selection list if present). */
	public IngridDocument getSysAdditionalFields(Long[] fieldIds, String language) {
		List<T08AttrType> fields = daoT08AttrType.getT08AttrTypes(fieldIds, language);

		IngridDocument result = new IngridDocument();
		beanToDocMapper.mapT08AttrTypes(fields, result);

		return result;
	}
}
