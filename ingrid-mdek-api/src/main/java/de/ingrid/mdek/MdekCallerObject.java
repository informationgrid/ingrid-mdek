package de.ingrid.mdek;

import java.util.List;

import org.apache.log4j.Logger;

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

	public IngridDocument fetchObject(String uuid, Quantity howMuch,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		if (howMuch == Quantity.DETAIL_ENTITY) {
			List jobMethods = mdekCaller.setUpJobMethod("getObjDetails", jobParams);
			return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
		}
		
		return new IngridDocument();
	}

	public IngridDocument storeObject(IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeObject", objDoc);
		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument publishObject(IngridDocument objDoc,
			boolean refetchAfterStore,
			boolean forcePublicationCondition,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("publishObject", objDoc);
		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteObjectWorkingCopy", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteObject", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopObjects(String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getTopObjects", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubObjects(String objUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, objUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSubObjects", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectPath(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getObjectPath", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);		
	}

	public IngridDocument checkObjectSubTree(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("checkObjectSubTree", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_COPY_SUBTREE, copySubtree);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("copyObject", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument moveObject(String fromUuid, String toUuid,
			boolean performSubtreeCheck,
			boolean forcePublicationCondition,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_PERFORM_CHECK, performSubtreeCheck);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("moveObject", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialObject(IngridDocument newBasicObject,
			String userId) {
		IngridDocument jobParams = newBasicObject;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getInitialObject", jobParams);

		return mdekCaller.callJob(MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}
}
