package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntitySelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 * 
 * @author Martin
 */
public class MdekCallerAddress extends MdekCallerAbstract implements IMdekCallerAddress {

	private final static Logger log = Logger.getLogger(MdekCallerAddress.class);

	private static MdekCallerAddress myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	private static String MDEK_IDC_ADDRESS_JOB_ID = "de.ingrid.mdek.job.MdekIdcAddressJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerAddress(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerAddress() {}

    private MdekCallerAddress(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerAddress getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerAddress.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument fetchAddress(String plugId, String addrUuid, Quantity howMuch,
			IdcEntityVersion whichEntityVersion,
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addrUuid);
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		if (howMuch == Quantity.DETAIL_ENTITY) {
			List jobMethods = mdekCaller.setUpJobMethod("getAddrDetails", jobParams);
			return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
		}

		return new IngridDocument();
	}

	public IngridDocument fetchAddressObjectReferences(String plugId, String addrUuid, 
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addrUuid);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getAddressObjectReferences", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument storeAddress(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeAddress", addrDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument assignAddressToQA(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("assignAddressToQA", addrDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument reassignAddressToAuthor(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("reassignAddressToAuthor", addrDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument updateAddressPart(String plugId, IngridDocument addrPartDoc,
			IdcEntityVersion whichEntityVersion, String userId) {
		addrPartDoc.put(MdekKeys.USER_ID, userId);
		addrPartDoc.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		List jobMethods = mdekCaller.setUpJobMethod("updateAddressPart", addrPartDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument publishAddress(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("publishAddress", addrDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddressWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteAddressWorkingCopy", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddress(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteAddress", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopAddresses(String plugId, String userId, boolean nurFreieAdressen) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES, nurFreieAdressen);
		List jobMethods = mdekCaller.setUpJobMethod("getTopAddresses", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubAddresses(String plugId, String adrUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, adrUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSubAddresses", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressPath(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getAddressPath", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);		
	}

	public IngridDocument checkAddressSubTree(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("checkAddressSubTree", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument copyAddress(String plugId, String fromUuid, String toUuid,
			boolean copySubtree,
			boolean copyToFreeAddress,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_COPY_SUBTREE, copySubtree);
		jobParams.put(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS, copyToFreeAddress);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("copyAddress", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument moveAddress(String plugId, String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS, moveToFreeAddress);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("moveAddress", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialAddress(String plugId, IngridDocument newBasicAddress,
			String userId) {
		IngridDocument jobParams = newBasicAddress;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getInitialAddress", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument searchAddresses(String plugId, IngridDocument searchParams,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("searchAddresses", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getQAAddresses(String plugId, WorkState whichWorkState, IdcEntitySelectionType selectionType, 
			Integer maxNum, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_WORK_STATE, whichWorkState);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, maxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getQAAddresses", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressStatistics(String plugId, String parentUuid,
			IdcEntitySelectionType selectionType, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, parentUuid);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getAddressStatistics", jobParams);

		return mdekCaller.callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}
}
