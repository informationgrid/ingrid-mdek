package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.utils.IngridDocument;



/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning SECURITY / USER MANAGEMENT.
 */
public class MdekCallerSecurity extends MdekCallerAbstract implements IMdekCallerSecurity {

	private final static Logger log = Logger.getLogger(MdekCallerSecurity.class);

	private static MdekCallerSecurity myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	private static String MDEK_IDC_SECURITY_JOB_ID = "de.ingrid.mdek.job.MdekIdcSecurityJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerSecurity(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerSecurity() {}

    private MdekCallerSecurity(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerSecurity getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerSecurity.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument getGroups(String plugId,
			String userId,
			boolean includeCatAdminGroup) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.REQUESTINFO_INCLUDE_CATADMIN_GROUP, includeCatAdminGroup);
		List jobMethods = mdekCaller.setUpJobMethod("getGroups", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getGroupDetails(String plugId,
			String groupName,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.NAME, groupName);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getGroupDetails", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getUsersOfGroup(String plugId,
			String groupName,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.NAME, groupName);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getUsersOfGroup", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument createGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId) {
		groupDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		groupDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("createGroup", groupDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument storeGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId) {
		groupDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		groupDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeGroup", groupDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument deleteGroup(String plugId, Long idcGroupId,
			boolean forceDeleteGroupWhenUsers,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_GROUP_ID, idcGroupId);
		jobParams.put(MdekKeysSecurity.REQUESTINFO_FORCE_DELETE_GROUP_WHEN_USERS, forceDeleteGroupWhenUsers);
		List jobMethods = mdekCaller.setUpJobMethod("deleteGroup", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressPermissions(String plugId, String addrUuid, String userAddrUuid,
			boolean checkWorkflow) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userAddrUuid);
		jobParams.put(MdekKeys.UUID, addrUuid);
		jobParams.put(MdekKeys.REQUESTINFO_CHECK_WORKFLOW, checkWorkflow);
		List jobMethods = mdekCaller.setUpJobMethod("getAddressPermissions", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);		
	}

	public IngridDocument getObjectPermissions(String plugId, String objUuid, String userAddrUuid,
			boolean checkWorkflow) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userAddrUuid);
		jobParams.put(MdekKeys.UUID, objUuid);
		jobParams.put(MdekKeys.REQUESTINFO_CHECK_WORKFLOW, checkWorkflow);
		List jobMethods = mdekCaller.setUpJobMethod("getObjectPermissions", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);		
	}

	public IngridDocument getUserPermissions(String plugId, String userAddrUuid) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userAddrUuid);
		List jobMethods = mdekCaller.setUpJobMethod("getUserPermissions", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);		
	}

	public IngridDocument getUserDetails(String plugId, String addrUuid, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuid);
		List jobMethods = mdekCaller.setUpJobMethod("getUserDetails", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getSubUsers(String plugId, Long parentIdcUserId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_USER_ID, parentIdcUserId);
		List jobMethods = mdekCaller.setUpJobMethod("getSubUsers", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument createUser(String plugId, IngridDocument userDoc, boolean refetchAfterStore, String userId) {
		userDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		userDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("createUser", userDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument storeUser(String plugId, IngridDocument userDoc, boolean refetchAfterStore, String userId) {
		userDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		userDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeUser", userDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument deleteUser(String plugId, Long idcUserId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_USER_ID, idcUserId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteUser", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getCatalogAdmin(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getCatalogAdmin", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getUsersWithWritePermissionForObject(String plugId, String objectUuid, String userId,
			boolean checkWorkflow, boolean getDetailedPermissions) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, objectUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.REQUESTINFO_CHECK_WORKFLOW, checkWorkflow);
		jobParams.put(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS, getDetailedPermissions);
		List jobMethods = mdekCaller.setUpJobMethod("getUsersWithWritePermissionForObject", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getUsersWithWritePermissionForAddress(String plugId, String addressUuid, String userId,
			boolean checkWorkflow, boolean getDetailedPermissions) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addressUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.REQUESTINFO_CHECK_WORKFLOW, checkWorkflow);
		jobParams.put(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS, getDetailedPermissions);
		List jobMethods = mdekCaller.setUpJobMethod("getUsersWithWritePermissionForAddress", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}
}
