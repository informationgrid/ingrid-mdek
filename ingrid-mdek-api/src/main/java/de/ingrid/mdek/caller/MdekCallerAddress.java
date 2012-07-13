package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.ige.MdekUtils.IdcEntityOrderBy;
import de.ingrid.utils.ige.MdekUtils.IdcEntityVersion;
import de.ingrid.utils.ige.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.utils.ige.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.utils.ige.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.utils.ige.MdekUtils.WorkState;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 * 
 * @author Martin
 */
public class MdekCallerAddress extends MdekCaller implements IMdekCallerAddress {

	private final static Logger log = Logger.getLogger(MdekCallerAddress.class);

	private static MdekCallerAddress myInstance;

	// Jobs
	private static String MDEK_IDC_ADDRESS_JOB_ID = "de.ingrid.mdek.job.MdekIdcAddressJob";

    private MdekCallerAddress(IMdekClientCaller mdekClientCaller) {
    	super(mdekClientCaller);
    }

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 */
	public static synchronized void initialize(IMdekClientCaller mdekClientCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerAddress(mdekClientCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
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

	public IngridDocument fetchAddress(String plugId, String addrUuid, FetchQuantity howMuch,
			IdcEntityVersion whichEntityVersion,
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addrUuid);
		jobParams.put(MdekKeys.REQUESTINFO_FETCH_QUANTITY, howMuch);
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getAddrDetails", jobParams);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchAddressObjectReferences(String plugId, String addrUuid, 
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, addrUuid);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		jobParams.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getAddressObjectReferences", jobParams);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument storeAddress(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeAddress", addrDoc);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument assignAddressToQA(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("assignAddressToQA", addrDoc);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument reassignAddressToAuthor(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("reassignAddressToAuthor", addrDoc);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument updateAddressPart(String plugId, IngridDocument addrPartDoc,
			IdcEntityVersion whichEntityVersion, String userId) {
		addrPartDoc.put(MdekKeys.USER_ID, userId);
		addrPartDoc.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		List jobMethods = setUpJobMethod("updateAddressPart", addrPartDoc);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument publishAddress(String plugId, IngridDocument addrDoc,
			boolean refetchAfterStore, int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, objRefsStartIndex);
		addrDoc.put(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM, objRefsMaxNum);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("publishAddress", addrDoc);
		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddressWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("deleteAddressWorkingCopy", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddress(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("deleteAddress", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopAddresses(String plugId, String userId, boolean onlyFreeAddresses) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES, onlyFreeAddresses);
		List jobMethods = setUpJobMethod("getTopAddresses", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubAddresses(String plugId, String adrUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, adrUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSubAddresses", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressPath(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getAddressPath", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);		
	}

	public IngridDocument checkAddressSubTree(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("checkAddressSubTree", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
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
		List jobMethods = setUpJobMethod("copyAddress", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument moveAddress(String plugId, String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS, moveToFreeAddress);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("moveAddress", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialAddress(String plugId, IngridDocument newBasicAddress,
			String userId) {
		IngridDocument jobParams = newBasicAddress;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getInitialAddress", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument searchAddresses(String plugId, IngridDocument searchParams,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.TOTAL_NUM, new Long(numHits));
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = setUpJobMethod("searchAddresses", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getWorkAddresses(String plugId,
			IdcWorkEntitiesSelectionType selectionType, 
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY, orderBy);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC, orderAsc);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getWorkAddresses", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getQAAddresses(String plugId,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType, 
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_WORK_STATE, whichWorkState);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY, orderBy);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC, orderAsc);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getQAAddresses", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressStatistics(String plugId,
			String parentUuid, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, parentUuid);
		jobParams.put(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES, onlyFreeAddresses);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getAddressStatistics", jobParams);

		return callJob(plugId, MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}
}
