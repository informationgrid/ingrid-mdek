package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekCallerCommon.Quantity;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;

public class MdekExampleAddress {

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties> [--threads 1]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() < 1) {
			printUsage();
		}

		// read passed Parameters
		System.out.println("\n###### PARAMS ######");
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}
		System.out.println("THREADS: " + numThreads);

		// INITIALIZE MDEK INTERFACE ONCE !
		MdekCaller.initialize(new File((String) map.get("--descriptor")));

		// start threads calling job
		System.out.println("\n###### OUTPUT THREADS ######\n");
		MdekExampleAddressThread[] threads = new MdekExampleAddressThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleAddressThread(i+1);
		}
		// fire
		for (int i=0; i<numThreads; i++) {
			threads[i].start();
		}

		// wait till all threads are finished
		boolean threadsFinished = false;
		while (!threadsFinished) {
			threadsFinished = true;
			for (int i=0; i<numThreads; i++) {
				if (threads[i].isRunning()) {
					threadsFinished = false;
					Thread.sleep(500);
					break;
				}
			}
		}

		// shutdown mdek
		MdekCaller.shutdown();
	}
}

class MdekExampleAddressThread extends Thread {

	private int threadNumber;
	String myUserId;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	public MdekExampleAddressThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserId = "EXAMPLE_USER_" + threadNumber;
	}

	public void run() {
		isRunning = true;
//		this.doFullOutput = false;

		long exampleStartTime = System.currentTimeMillis();

		// TOP ADDRESS
		String topUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		// PARENT ADDRESS (sub address of topUuid)
		String parentUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		// PERSON ADDRESS (sub address of parentUuid)
		String personUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";

		// FREE ADDRESS
		String freeUuid = "9B1A4FF6-8643-11D5-987F-00D0B70EFC19";
		
		// ===================================
		System.out.println("\n----- top addresses -----");
		fetchTopAddresses(true);
		fetchTopAddresses(false);

		// -----------------------------------
		System.out.println("\n----- sub addresses -----");
		fetchSubAddresses(topUuid);

		// -----------------------------------
		System.out.println("\n----- address path -----");
		getAddressPath(personUuid);

		// -----------------------------------
		System.out.println("\n----- address details -----");
		IngridDocument aMap = fetchAddress(personUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing address");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing address -> working copy ! -----");
		storeAddressWithManipulation(aMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteAddressWorkingCopy(personUuid);
		
		System.out.println("\n----- and reload -----");
		aMap = fetchAddress(personUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new address");
		System.out.println("=========================");

		System.out.println("\n----- first load initial data (from " + personUuid + ") -----");
		// initial data from person address (to test take over of SUBJECT_TERMS)
		IngridDocument newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, personUuid);
		newAdrDoc = getInitialAddress(newAdrDoc);

		System.out.println("\n----- extend initial address and store -----");

		// extend initial address with own data !
		System.out.println("- add NAME, GIVEN_NAME, TITLE_OR_FUNCTION, CLASS");
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());

		// new parent
		System.out.println("- store under parent: " + parentUuid);
		newAdrDoc.put(MdekKeys.PARENT_UUID, parentUuid);
		IngridDocument aMapNew = storeAddressWithManipulation(newAdrDoc);
		// uuid created !
		String newAdrUuid = (String) aMapNew.get(MdekKeys.UUID);

		System.out.println("\n----- verify new subaddress -> load parent subaddresses -----");
		fetchSubAddresses(parentUuid);

		System.out.println("\n----- do \"forbidden\" store -> \"free address\" WITH parent -----");
		Integer origType = (Integer) aMapNew.get(MdekKeys.CLASS);
		aMapNew.put(MdekKeys.CLASS, MdekUtils.AddressType.FREI.getDbValue());
		storeAddressWithoutManipulation(aMapNew, false);
		aMapNew.put(MdekKeys.CLASS, origType);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITH sub tree) -> ERROR -----");
		String addressFrom = personUuid;
		String addressTo = null;
		aMap = copyAddress(addressFrom, addressTo, true, true);
		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITHOUT sub tree) -----");
		aMap = copyAddress(addressFrom, addressTo, false, true);
		String copy1Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		fetchAddress(copy1Uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify copy, load top FREE ADDRESSES -> new FREE ADDRESS -----");
		fetchTopAddresses(true);

		System.out.println("\n\n----- copy FREE Address under parent of new address -----");
		addressFrom = freeUuid;
		addressTo = parentUuid;
		aMap = copyAddress(addressFrom, addressTo, true, false);
		String copy2Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		fetchAddress(copy2Uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify children -> new child -----");
		fetchSubAddresses(addressTo);

		System.out.println("\n\n----- copy FREE Address to FREE address -----");
		addressFrom = freeUuid;
		addressTo = null;
		aMap = copyAddress(addressFrom, addressTo, false, true);
		String copy3Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		fetchAddress(copy3Uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- delete copies (WORKING COPY) -> FULL DELETE -----");
		deleteAddressWorkingCopy(copy1Uuid);
		deleteAddressWorkingCopy(copy2Uuid);
		deleteAddressWorkingCopy(copy3Uuid);

		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new address below new address (WITH sub tree) -----");
		IngridDocument subtreeCopyDoc = copyAddress(parentUuid, newAdrUuid, true, false);
		String subtreeCopyUuid = subtreeCopyDoc.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy -> load children of new address -----");
		fetchSubAddresses(newAdrUuid);
		System.out.println("\n\n----- verify copied sub addresses -> load children of copy -----");
		fetchSubAddresses(subtreeCopyUuid);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- move new address WITHOUT CHECK WORKING COPIES -> ERROR (not published yet) -----");
		String oldParentUuid = parentUuid;
		String newParentUuid = topUuid;
		moveAddress(newAdrUuid, newParentUuid, false, false);
		System.out.println("\n----- publish new address -> create pub version/delete work version -----");
		publishAddress(aMapNew, true);
/*
		System.out.println("\n\n----- move new address again WITH CHECK WORKING COPIES -> ERROR (subtree has working copies) -----");
		moveAddress(newAdrUuid, newParentUuid, true, false);
		System.out.println("\n----- check new address subtree -----");
		checkAddressSubTree(newAdrUuid);
		System.out.println("\n\n----- delete subtree -----");
		deleteAddress(subtreeCopyUuid);
		System.out.println("\n\n----- move new address again WITH CHECK WORKING COPIES -> SUCCESS (published AND no working copies ) -----");
		moveAddress(newAdrUuid, newParentUuid, true, false);
		System.out.println("\n----- verify old parent subaddresses (cut) -----");
		fetchSubAddresses(oldParentUuid);
		System.out.println("\n----- verify new parent subaddresses (added) -----");
		fetchSubAddresses(newParentUuid);
		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		moveAddress(topUuid, parentUuid, false);
*/
		// -----------------------------------

		// ===================================
		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	private IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String onlyFreeAddressesInfo = (onlyFreeAddresses) ? "ONLY FREE ADDRESSES" : "ONLY NO FREE ADDRESSES";
		System.out.println("\n###### INVOKE fetchTopAddresses " + onlyFreeAddressesInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchTopAddresses(myUserId, onlyFreeAddresses);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchSubAddresses(String uuid) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchSubAddresses(uuid, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getAddressPath(String uuidIn) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.getAddressPath(uuidIn, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<String> uuidList = (List<String>) result.get(MdekKeys.PATH);
			System.out.println("SUCCESS: " + uuidList.size() + " levels");
			String indent = " ";
			for (String uuid : uuidList) {
				System.out.println(indent + uuid);
				indent += " ";
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchAddress(String uuid, Quantity howMuch) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchAddress(uuid, howMuch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeAddressWithoutManipulation(IngridDocument aDocIn,
			boolean refetchAddress) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchAddressInfo = (refetchAddress) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeAddress (no manipulation) " + refetchAddressInfo + " ######");

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		response = mdekCaller.storeAddress(aDocIn, refetchAddress, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument storeAddressWithManipulation(IngridDocument aDocIn) {
		if (aDocIn == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeAddress (with manipulation) ######");
		
		// manipulate former loaded address !
		System.out.println("MANIPULATE ADDRESS");

		System.out.println("- change test ORGANISATION, GIVEN_NAME");
		String origORGANISATION = aDocIn.getString(MdekKeys.ORGANISATION);
		String origGIVEN_NAME = aDocIn.getString(MdekKeys.GIVEN_NAME);
		aDocIn.put(MdekKeys.ORGANISATION, "TEST/" + origORGANISATION);
		aDocIn.put(MdekKeys.GIVEN_NAME, "TEST/" + origGIVEN_NAME);

		// add entry to COMMUNICATION
		System.out.println("- add test COMMUNICATION");
		List<IngridDocument> docList = (List<IngridDocument>) aDocIn.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM, "TEST COMMUNIC_MEDIUM");
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "TEST COMMUNICATION_VALUE");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		aDocIn.put(MdekKeys.COMMUNICATION, docList);

		// add entry to SUBJECT_TERMS
		System.out.println("- add test SUBJECT_TERM");
		docList = (List<IngridDocument>) aDocIn.get(MdekKeys.SUBJECT_TERMS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.TERM_NAME, "TEST TERM_NAME");
		testDoc.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
		testDoc.put(MdekKeys.TERM_SNS_ID, "TEST TERM_SNS_ID");
		docList.add(testDoc);
		aDocIn.put(MdekKeys.SUBJECT_TERMS, docList);

		// add entry to COMMENT_LIST
		System.out.println("- add test COMMENT");
		docList = (List<IngridDocument>) aDocIn.get(MdekKeys.COMMENT_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMENT, "TEST COMMENT");
		testDoc.put(MdekKeys.CREATE_TIME, MdekUtils.dateToTimestamp(new Date()));
		testDoc.put(MdekKeys.CREATE_UUID, "TEST CREATE_UUID");
		docList.add(testDoc);
		aDocIn.put(MdekKeys.COMMENT_LIST, docList);

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		System.out.println("storeAddress WITHOUT refetching address: ");
		response = mdekCaller.storeAddress(aDocIn, false, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredAddress = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredAddress);
			System.out.println("refetch Address");
			IngridDocument aRefetchedDoc = fetchAddress(uuidStoredAddress, Quantity.DETAIL_ENTITY);
			System.out.println("");

			System.out.println("MANIPULATE ADDRESS: back to origin");

			System.out.println("- set original ORGANISATION, GIVEN_NAME");
			aRefetchedDoc.put(MdekKeys.ORGANISATION, origORGANISATION);
			aRefetchedDoc.put(MdekKeys.GIVEN_NAME, origGIVEN_NAME);

			// COMMUNICATION wieder wie vorher !
			System.out.println("- remove test COMMUNICATION");
			docList = (List<IngridDocument>) aRefetchedDoc.get(MdekKeys.COMMUNICATION);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				aRefetchedDoc.put(MdekKeys.COMMUNICATION, docList);				
			}

			// SUBJECT_TERMS wieder wie vorher !
			System.out.println("- remove test SUBJECT_TERM");
			docList = (List<IngridDocument>) aRefetchedDoc.get(MdekKeys.SUBJECT_TERMS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				aRefetchedDoc.put(MdekKeys.SUBJECT_TERMS, docList);				
			}

			// COMMENT wieder wie vorher !
			System.out.println("- remove test COMMENT");
			docList = (List<IngridDocument>) aRefetchedDoc.get(MdekKeys.COMMENT_LIST);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				aRefetchedDoc.put(MdekKeys.COMMENT_LIST, docList);				
			}

			// store
			System.out.println("STORE");
			startTime = System.currentTimeMillis();
			System.out.println("storeAddress WITH refetching address: ");
			response = mdekCaller.storeAddress(aRefetchedDoc, true, myUserId);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);

			if (result != null) {
				System.out.println("SUCCESS: ");
				debugAddressDoc(result);
			} else {
				handleError(response);
			}					
			
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument getInitialAddress(IngridDocument newBasicAddress) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialAddress ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.getInitialAddress(newBasicAddress, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument deleteAddressWorkingCopy(String uuid) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteAddressWorkingCopy ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.deleteAddressWorkingCopy(uuid, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			Boolean fullyDeleted = (Boolean) result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED);
			System.out.println("was fully deleted: " + fullyDeleted);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument copyAddress(String fromUuid, String toUuid,
		boolean copySubtree, boolean copyToFreeAddress)
	{
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		String copyToFreeAddressInfo = (copyToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE copyAddress " + copySubtreeInfo + copyToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.copyAddress(fromUuid, toUuid, copySubtree, copyToFreeAddress, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
			System.out.println("Copy Node (rudimentary): ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean performSubtreeCheck, boolean moveToFreeAddress)
	{
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String performCheckInfo = (performSubtreeCheck) ? "WITH CHECK SUBTREE (working copies)" 
			: "WITHOUT CHECK SUBTREE (working copies)";
		String moveToFreeAddressInfo = (moveToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE moveAddress " + performCheckInfo + moveToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.moveAddress(fromUuid, toUuid, performSubtreeCheck, moveToFreeAddress, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch) {
		if (aDocIn == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String withRefetchInfo = (withRefetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE publishAddress  " + withRefetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.publishAddress(aDocIn, withRefetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuid = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuid);
			if (withRefetch) {
				debugAddressDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
		);
		System.out.println("        "
			+ "status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
		);
		System.out.println("  " + a);

		if (!doFullOutput) {
			return;
		}

		IngridDocument myDoc;
		List<IngridDocument> docList;
		List<String> strList;

		docList = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Communication: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		myDoc = (IngridDocument) a.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
		strList = (List<String>) a.get(MdekKeys.PATH);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Path: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
	}

	private void handleError(IngridDocument response) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			
		
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
