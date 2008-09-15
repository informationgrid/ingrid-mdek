package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcEntitySelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning OBJECT Manipulation.
 * 
 * @author Martin
 */
public class MdekCallerObject extends MdekCallerAbstract implements IMdekCallerObject {

	private final static Logger log = Logger.getLogger(MdekCallerObject.class);

	private static MdekCallerObject myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	private static String MDEK_IDC_OBJECT_JOB_ID = "de.ingrid.mdek.job.MdekIdcObjectJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerObject(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerObject() {}

    private MdekCallerObject(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerObject getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerObject.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument fetchObject(String plugId, String uuid, Quantity howMuch,
			IdcEntityVersion whichEntityVersion, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		jobParams.put(MdekKeys.USER_ID, userId);
		if (howMuch == Quantity.DETAIL_ENTITY) {
			List jobMethods = mdekCaller.setUpJobMethod("getObjDetails", jobParams);
			return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
		}
		
		return new IngridDocument();
	}

	public IngridDocument storeObject(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeObject", objDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument assignObjectToQA(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("assignObjectToQA", objDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument updateObjectPart(String plugId, IngridDocument objPartDoc,
			IdcEntityVersion whichEntityVersion, String userId) {
		objPartDoc.put(MdekKeys.USER_ID, userId);
		objPartDoc.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		List jobMethods = mdekCaller.setUpJobMethod("updateObjectPart", objPartDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument publishObject(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			boolean forcePublicationCondition,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("publishObject", objDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObjectWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteObjectWorkingCopy", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObject(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteObject", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopObjects(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getTopObjects", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubObjects(String plugId, String objUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, objUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSubObjects", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectPath(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getObjectPath", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);		
	}

	public IngridDocument checkObjectSubTree(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("checkObjectSubTree", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument copyObject(String plugId, String fromUuid, String toUuid, boolean copySubtree,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_COPY_SUBTREE, copySubtree);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("copyObject", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument moveObject(String plugId, String fromUuid, String toUuid,
			boolean forcePublicationCondition,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("moveObject", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialObject(String plugId, IngridDocument newBasicObject,
			String userId) {
		IngridDocument jobParams = newBasicObject;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getInitialObject", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getQAObjects(String plugId, WorkState whichWorkState, IdcEntitySelectionType selectionType, 
			Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_WORK_STATE, whichWorkState);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getQAObjects", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}
}
