package de.ingrid.mdek;

import java.util.List;

import org.apache.log4j.Logger;

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

    private MdekCallerAddress() {};

    private MdekCallerAddress(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    };

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

	public IngridDocument fetchAddress(String uuid, Quantity howMuch,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		if (howMuch == Quantity.DETAIL_ENTITY) {
			List jobMethods = mdekCaller.setUpJobMethod("getAddrDetails", jobParams);
			return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
		}

		return new IngridDocument();
	}

	public IngridDocument storeAddress(IngridDocument adrDoc,
			boolean refetchAfterStore,
			String userId) {
		adrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		adrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeAddress", adrDoc);
		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument publishAddress(IngridDocument addrDoc,
			boolean refetchAfterStore,
			String userId) {
		addrDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		addrDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("publishAddress", addrDoc);
		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddressWorkingCopy(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteAddressWorkingCopy", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument deleteAddress(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteAddress", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopAddresses(String userId, boolean nurFreieAdressen) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES, nurFreieAdressen);
		List jobMethods = mdekCaller.setUpJobMethod("getTopAddresses", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubAddresses(String adrUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, adrUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getSubAddresses", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getAddressPath(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getAddressPath", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);		
	}

	public IngridDocument checkAddressSubTree(String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("checkAddressSubTree", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument copyAddress(String fromUuid, String toUuid,
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

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean performSubtreeCheck,
			boolean moveToFreeAddress,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_PERFORM_CHECK, performSubtreeCheck);
		jobParams.put(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS, moveToFreeAddress);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("moveAddress", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialAddress(IngridDocument newBasicAddress,
			String userId) {
		IngridDocument jobParams = newBasicAddress;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getInitialAddress", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}

	public IngridDocument searchAddresses(IngridDocument searchParams,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeys.SEARCH_START_HIT, startHit);
		jobParams.put(MdekKeys.SEARCH_NUM_HITS, numHits);
		jobParams.put(MdekKeys.SEARCH_PARAMS, searchParams);
		List jobMethods = mdekCaller.setUpJobMethod("searchAddresses", jobParams);

		return mdekCaller.callJob(MDEK_IDC_ADDRESS_JOB_ID, jobMethods);
	}
}
