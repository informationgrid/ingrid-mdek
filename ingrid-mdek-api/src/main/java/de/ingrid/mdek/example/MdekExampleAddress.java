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

		// TOP OBJECT
		String topUuid = "A7D04CEB-77EF-11D3-AF93-0060084A4596";
		// DETAIL OBJECT
		String adrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		IngridDocument aMap;

		// ===================================
		System.out.println("\n----- top addresses -----");
		fetchTopAddresses(true);
		fetchTopAddresses(false);

		// -----------------------------------
		System.out.println("\n----- sub objects -----");
		fetchSubAddresses(topUuid);

		// -----------------------------------
		System.out.println("\n----- address path -----");
		getAddressPath(adrUuid);

		// -----------------------------------
		System.out.println("\n----- address details -----");
		aMap = fetchAddress(adrUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing address");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing address -> working copy ! -----");
		storeAddressWithManipulation(aMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteAddressWorkingCopy(adrUuid);
		
		System.out.println("\n----- and reload -----");
		aMap = fetchAddress(adrUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new address");
		System.out.println("=========================");

		System.out.println("\n----- first load initial data (from parent " + adrUuid + ") -----");
		IngridDocument newAdrDoc = new IngridDocument();
		// supply parent uuid !
		newAdrDoc.put(MdekKeys.PARENT_UUID, adrUuid);
		newAdrDoc = getInitialAddress(newAdrDoc);

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

		System.out.println("\n###### INVOKE fetchTopAddresses ######");
		System.out.println("fetchTopAddresses -> " +
				"onlyFreeAddresses: " + onlyFreeAddresses);
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

		System.out.println("\n###### INVOKE storeAddress ######");
		
		// manipulate former loaded address !
		System.out.println("MANIPULATE ADDRESS");

		System.out.println("- change test ORGANISATION, GIVEN_NAME");
		aDocIn.put(MdekKeys.ORGANISATION, "TEST " + aDocIn.get(MdekKeys.ORGANISATION));
		aDocIn.put(MdekKeys.GIVEN_NAME, "TEST " + aDocIn.get(MdekKeys.GIVEN_NAME));

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
			System.out.println("storeAddress WITH refetching object: ");
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

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + a.get(MdekKeys.CLASS)
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
		List<IngridDocument> docList = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
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
