package de.ingrid.mdek.services.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.SysGui;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;
import de.ingrid.mdek.services.utils.MdekJobHandler;
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

	protected MdekJobHandler jobHandler;

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

		jobHandler = MdekJobHandler.getInstance(daoFactory);

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

	/** "logs" Start-Info in Export information IN DATABASE */
	public void startExportInfoDB(IdcEntityType whichType, int totalNum, String userUuid) {
		// set up export details
        HashMap details = setUpExportDetailsDB(whichType, 0, totalNum);
        // and store
		jobHandler.startJobInfoDB(JobType.EXPORT, details, userUuid);
	}

	/** Set up details of export to be stored in database. */
	private HashMap setUpExportDetailsDB(IdcEntityType whichType, int num, int totalNum) {
        HashMap details = new HashMap();
        if (whichType == IdcEntityType.OBJECT) {
            details.put(MdekKeys.EXPORT_TOTAL_NUM_OBJECTS, totalNum);        	
            details.put(MdekKeys.EXPORT_NUM_OBJECTS, num);
        } else if (whichType == IdcEntityType.ADDRESS) {
            details.put(MdekKeys.EXPORT_TOTAL_NUM_ADDRESSES, totalNum);        	
            details.put(MdekKeys.EXPORT_NUM_ADDRESSES, num);
        }
		
        return details;
	}

	/** Updates info of Export job IN MEMORY and IN DATABASE */
	public void updateExportInfoDB(IdcEntityType whichType, int numExported, int totalNum, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid, 
				jobHandler.createRunningJobDescription(JobType.EXPORT, numExported, totalNum, false));

		// then update job info in database
        HashMap details = setUpExportDetailsDB(whichType, numExported, totalNum);
		jobHandler.updateJobInfoDB(JobType.EXPORT, details, userUuid);
	}

	/** "logs" End-Info in Export information IN DATABASE */
	public void endExportInfoDB(String userUuid) {
		jobHandler.endJobInfoDB(JobType.EXPORT, userUuid);
	}

	/** Returns "logged" Export job information IN DATABASE.
	 * NOTICE: returns EMPTY HashMap if no job info ! */
	public HashMap getExportInfoDB(String userUuid) {
        HashMap exportInfo = new HashMap();

		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.EXPORT, userUuid);
		if (jobInfo != null) {
			HashMap jobDetails = (HashMap) jobHandler.deformatJobDetailsFromDB(jobInfo.getJobDetails());

	        exportInfo.putAll(jobDetails);
	        exportInfo.put(MdekKeys.EXPORT_START_TIME, jobInfo.getStartTime());
	        exportInfo.put(MdekKeys.EXPORT_END_TIME, jobInfo.getEndTime());			
		}
        
        return exportInfo;
	}
}
