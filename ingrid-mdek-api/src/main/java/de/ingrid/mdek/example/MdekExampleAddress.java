package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
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

		// INITIALIZE CENTRAL MDEK CALLER !
		System.out.println("\n###### start mdek iBus ######\n");
		MdekCaller.initialize(new File((String) map.get("--descriptor")));
		IMdekCaller mdekCaller = MdekCaller.getInstance();

		// and our specific job caller !
		MdekCallerSecurity.initialize(mdekCaller);
		MdekCallerAddress.initialize(mdekCaller);
		MdekCallerObject.initialize(mdekCaller);
		
		// wait till iPlug registered !
		System.out.println("\n###### waiting for mdek iPlug to register ######\n");
		boolean plugRegistered = false;
		while (!plugRegistered) {
			List<String> iPlugs = mdekCaller.getRegisteredIPlugs();
			if (iPlugs.size() > 0) {
				plugRegistered = true;
				System.out.println("Registered iPlugs: " + iPlugs);
			} else {
				System.out.println("wait ...");
				Thread.sleep(2000);
			}
		}

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
	String myUserUuid;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	// MDEK SERVER TO CALL !
	private String plugId = "mdek-iplug-idctest";
	
	private IMdekCaller mdekCaller;
	private IMdekCallerSecurity mdekCallerSecurity;
	private IMdekCallerAddress mdekCallerAddress;
	private IMdekCallerObject mdekCallerObject;
	
	private MdekExampleUtils exUtils;

	public MdekExampleAddressThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserUuid = "EXAMPLE_USER_" + threadNumber;
		
		mdekCaller = MdekCaller.getInstance();
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
		mdekCallerObject = MdekCallerObject.getInstance();
		
		exUtils = MdekExampleUtils.getInstance();
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

		boolean alwaysTrue = true;

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		IngridDocument doc = getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		myUserUuid = catalogAdminUuid;

// ====================
// test single stuff
// -----------------------------------
/*
		// add functionality !

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// Change Request 22, see INGRIDII-127
		// deliver "from object references" from published versions which were deleted in working version
		// ------------------
		String objFrom = "012CBA09-87F6-11D4-89C7-C1AAE1E96727";
		String adrTo = "012CBA0B-87F6-11D4-89C7-C1AAE1E96727";

		System.out.println("\n----- load object from -----");
		IngridDocument oMap = fetchObject(objFrom, Quantity.DETAIL_ENTITY);
		List<IngridDocument> docList = (List<IngridDocument>) oMap.get(MdekKeys.ADR_REFERENCES_TO);
		// find address to remove
		IngridDocument docToRemove = null;
		for (IngridDocument doc : docList) {
			if (adrTo.equals(doc.get(MdekKeys.UUID))) {
				docToRemove = doc;
				break;
			}
		}

		System.out.println("\n----- remove reference to address / store working version -----");
		if (docToRemove != null) {
			docList.remove(docToRemove);
		}
		storeObjectWithoutManipulation(oMap, true);

		System.out.println("\n----- load referenced address -> has reference from published object only ! -----");
		fetchAddress(adrTo, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteObjectWorkingCopy(objFrom, true);

		System.out.println("\n----- verify from object, no working version and references to address again ! -----");
		fetchObject(objFrom, Quantity.DETAIL_ENTITY);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// ====================
*/
		// ===================================
		System.out.println("\n----- backend version -----");
		getVersion();

		// -----------------------------------
		System.out.println("\n----- top addresses -----");
		fetchTopAddresses(true);
		fetchTopAddresses(false);

		// -----------------------------------
		System.out.println("\n----- sub addresses -----");
//		fetchSubAddresses(topUuid);
		fetchSubAddresses("386644BF-B449-11D2-9A86-080000507261");

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
		deleteAddressWorkingCopy(personUuid, true);
		
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
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());

		// new parent
		System.out.println("- store under parent: " + parentUuid);
		newAdrDoc.put(MdekKeys.PARENT_UUID, parentUuid);
		IngridDocument aMapNew = storeAddressWithManipulation(newAdrDoc);
		// uuid created !
		String newAddrUuid = (String) aMapNew.get(MdekKeys.UUID);

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
		deleteAddressWorkingCopy(copy1Uuid, true);
		deleteAddressWorkingCopy(copy2Uuid, true);
		deleteAddressWorkingCopy(copy3Uuid, true);

		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new address below new address (WITH sub tree) -----");
		IngridDocument subtreeCopyDoc = copyAddress(parentUuid, newAddrUuid, true, false);
		String subtreeCopyUuid = subtreeCopyDoc.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy -> load children of new address -----");
		fetchSubAddresses(newAddrUuid);
		System.out.println("\n\n----- verify copied sub addresses -> load children of copy -----");
		fetchSubAddresses(subtreeCopyUuid);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n----- create NEW TOP ADDRESS -----");
		IngridDocument newTopAddrDoc = new IngridDocument();
		newTopAddrDoc = getInitialAddress(newTopAddrDoc);
		newTopAddrDoc.put(MdekKeys.ORGANISATION, "TEST TOP ADDRESS");
		newTopAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		newTopAddrDoc = storeAddressWithoutManipulation(newTopAddrDoc, true);
		// uuid created !
		String newTopAddrUuid = (String) newTopAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n\n----- move new address to NEW TOP ADDRESS -> ERROR (new parent not published) -----");
		String oldParentUuid = parentUuid;
		String newParentUuid = newTopAddrUuid;
		moveAddress(newAddrUuid, newParentUuid, false);
		System.out.println("\n----- publish NEW TOP ADDRESS -----");
		publishAddress(newTopAddrDoc, true);
		System.out.println("\n\n----- move new address again -> SUCCESS (new parent published) -----");
		moveAddress(newAddrUuid, newParentUuid, false);
		System.out.println("\n----- check new address subtree -> has working copies ! (move worked !) -----");
		checkAddressSubTree(newAddrUuid);
		System.out.println("\n----- verify old parent subaddresses (cut) -----");
		fetchSubAddresses(oldParentUuid);
		System.out.println("\n----- verify new parent subaddresses (added) -----");
		fetchSubAddresses(newParentUuid);

		System.out.println("\n----- move new Address to FREE Address ! -> ERROR: has subtree -----");
		moveAddress(newAddrUuid, null, true);
		System.out.println("\n\n----- delete subtree of new Address -----");
		deleteAddress(subtreeCopyUuid, true);
		System.out.println("\n----- move new Address to FREE Address ! -> ERROR (type conflicts -> EINHEIT) -----");
		moveAddress(newAddrUuid, null, true);
		System.out.println("\n----- publish new Address as PERSON -----");
		aMapNew.put(MdekKeys.CLASS, MdekUtils.AddressType.PERSON.getDbValue());
		publishAddress(aMapNew, true);
		System.out.println("\n----- store changed working copy of new Address -----");
		aMapNew.put(MdekKeys.GIVEN_NAME, "changed!!!!!!!!");
		storeAddressWithoutManipulation(aMapNew, true);
		System.out.println("\n----- move new Address to FREE Address ! -> SUCCESS -----");
		moveAddress(newAddrUuid, null, true);
		doFullOutput = false;
		fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		doFullOutput = true;
		System.out.println("\n----- move new FREE Address to NOT FREE Address ! -> SUCCESS -----");
		newParentUuid = topUuid;
		moveAddress(newAddrUuid, newParentUuid, false);
		doFullOutput = false;
		fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		doFullOutput = true;

		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		moveAddress(topUuid, parentUuid, false);
		System.out.println("\n----- do \"forbidden\" move (institution to free address) -----");
		moveAddress(topUuid, null, true);

		System.out.println("\n----- delete NEW TOP ADDRESS -----");
		deleteAddress(newTopAddrUuid, true);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("DELETE TEST");
		System.out.println("=========================");

		System.out.println("\n----- delete new address (WORKING COPY) -> NO full delete -----");
		deleteAddressWorkingCopy(newAddrUuid, true);
		System.out.println("\n----- delete new address (FULL) -> full delete -----");
		deleteAddress(newAddrUuid, true);
		System.out.println("\n----- verify deletion of new address -----");
		fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subaddresses -----");
		fetchSubAddresses(newParentUuid);

		System.out.println("\n----- test deletion of references / WARNINGS -----");

		System.out.println("\n----- create new TOP ADDRESS -----");
		IngridDocument toAddrDoc = new IngridDocument();
		toAddrDoc = getInitialAddress(toAddrDoc);
		toAddrDoc.put(MdekKeys.ORGANISATION, "TEST TOP ADDRESS");
		toAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		toAddrDoc = storeAddressWithoutManipulation(toAddrDoc, true);
		// uuid created !
		String topAddrUuid = (String) toAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new SUB ADDRESS to be REFERENCED -----");
		// initial data from parent
		toAddrDoc = new IngridDocument();
		toAddrDoc.put(MdekKeys.PARENT_UUID, topAddrUuid);
		toAddrDoc = getInitialAddress(toAddrDoc);
		toAddrDoc.put(MdekKeys.ORGANISATION, "TEST SUB ADDRESS -> wird referenziert");
		toAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		toAddrDoc = storeAddressWithoutManipulation(toAddrDoc, true);
		// uuid created !
		String toAddrUuid = (String) toAddrDoc.get(MdekKeys.UUID);
		
		System.out.println("\n----- create new OBJECT REFERENCING ADDRESS -----");
		IngridDocument fromObjDoc = new IngridDocument();
		fromObjDoc = getInitialObject(fromObjDoc);
		fromObjDoc.put(MdekKeys.TITLE, "TEST OBJECT -> referenziert");
		ArrayList<IngridDocument> adrRefsList = new ArrayList<IngridDocument>(1);
		toAddrDoc.put(MdekKeys.RELATION_TYPE_ID, -1); // needed !
		adrRefsList.add(toAddrDoc);
		fromObjDoc.put(MdekKeys.ADR_REFERENCES_TO, adrRefsList);
		fromObjDoc = storeObjectWithoutManipulation(fromObjDoc, true);
		// uuid created !
		String fromObjUuid = (String) fromObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete ADDRESS (WORKING COPY) WITHOUT refs -> Error -----");
		deleteAddressWorkingCopy(topAddrUuid, false);
		System.out.println("\n----- delete ADDRESS (FULL) WITHOUT refs -> Error -----");
		deleteAddress(topAddrUuid, false);
		System.out.println("\n----- delete ADDRESS (WORKING COPY) WITH refs -> OK -----");
		deleteAddressWorkingCopy(topAddrUuid, true);
		System.out.println("\n----- delete OBJECT (WORKING COPY) without refs -> OK -----");
		deleteObject(fromObjUuid, false);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("PUBLISH TEST");
		System.out.println("=========================");

		System.out.println("\n----- publish NEW TOP ADDRESS immediately -----");
		IngridDocument newTopDoc = new IngridDocument();
		newTopDoc.put(MdekKeys.ORGANISATION, "TEST NEW TOP ADDRESS DIRECT PUBLISH");
		newTopDoc.put(MdekKeys.CLASS, AddressType.INSTITUTION.getDbValue());
		aMap = publishAddress(newTopDoc, true);
		// uuid created !
		String newTopUuid = (String)aMap.get(MdekKeys.UUID);

		System.out.println("\n----- delete NEW TOP ADDRESS (FULL) -----");
		deleteAddress(newTopUuid, true);

		System.out.println("\n----- copy address (without subnodes) -> returns only \"TREE Data\" of copied address -----");
		addressFrom = newParentUuid;
		addressTo = topUuid;
		aMap = copyAddress(addressFrom, addressTo, false, false);
		String pub1Uuid = aMap.getString(MdekKeys.UUID);

		System.out.println("\n----- publish NEW SUB ADDRESS immediately -> ERROR, PARENT NOT PUBLISHED ! -----");
		IngridDocument newPubDoc = new IngridDocument();
		newPubDoc.put(MdekKeys.ORGANISATION, "TEST NEW SUB ADDRESS DIRECT PUBLISH");
		newPubDoc.put(MdekKeys.NAME, "testNAME");
		newPubDoc.put(MdekKeys.NAME_FORM, "Herr");
		newPubDoc.put(MdekKeys.NAME_FORM_KEY, new Integer(-1));
		newPubDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newPubDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newPubDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newPubDoc.put(MdekKeys.CLASS, AddressType.PERSON.getDbValue());
		// sub address of unpublished parent !!!
		newPubDoc.put(MdekKeys.PARENT_UUID, pub1Uuid);
		publishAddress(newPubDoc, true);

		System.out.println("\n----- refetch FULL PARENT, UNPUBLISHED !  -----");
		aMap = fetchAddress(pub1Uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change organization and publish PARENT -> create pub version/\"delete\" work version -----");
		aMap.put(MdekKeys.ORGANISATION, "COPIED, Orga CHANGED and PUBLISHED: " + aMap.get(MdekKeys.ORGANISATION));	
		publishAddress(aMap, true);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		aMap = publishAddress(newPubDoc, true);
		// uuid created !
		String pub2Uuid = aMap.getString(MdekKeys.UUID);

		System.out.println("\n----- verify -> load sub addresses of parent of copy -----");
		fetchSubAddresses(addressTo);
		System.out.println("\n----- delete 1. published copy = sub-address (WORKING COPY) -> NO DELETE -----");
		deleteAddressWorkingCopy(pub1Uuid, true);
		System.out.println("\n----- delete 2. published copy = sub-sub-address (FULL) -----");
		deleteAddress(pub2Uuid, true);
		System.out.println("\n----- delete 1. published copy = sub-address (FULL) -----");
		deleteAddress(pub1Uuid, true);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("SEARCH TEST");
		System.out.println("=========================");

		System.out.println("\n----- search address by orga / name,given-name -----");
		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.ORGANISATION, "Bezirksregierung");
		searchAddress(searchParams, 0, 5);
		searchAddress(searchParams, 5, 5);
		searchAddress(searchParams, 10, 5);

		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.NAME, "Dahlmann");
		searchParams.put(MdekKeys.GIVEN_NAME, "Irene");
		searchAddress(searchParams, 0, 5);

		// ===================================
		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	private void getVersion() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getVersion ######");
		startTime = System.currentTimeMillis();
		// ACHTUNG: ist DIREKT result ! sollte nie null sein (hoechstens leer)
		response = mdekCaller.getVersion(plugId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");

		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("All entries in Map: ");
			Set<Map.Entry> entries = result.entrySet();
			for (Map.Entry entry : entries) {
				System.out.println("  " + entry);
			}
			System.out.println("Explicit read of entries: ");
			System.out.println("  API_BUILD_NAME: " + result.get(MdekKeys.API_BUILD_NAME));
			System.out.println("  API_BUILD_VERSION: " + result.get(MdekKeys.API_BUILD_VERSION));
			System.out.println("  API_BUILD_NUMBER: " + result.get(MdekKeys.API_BUILD_NUMBER));
			System.out.println("  API_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.API_BUILD_TIMESTAMP)));
			System.out.println("  SERVER_BUILD_NAME: " + result.get(MdekKeys.SERVER_BUILD_NAME));
			System.out.println("  SERVER_BUILD_VERSION: " + result.get(MdekKeys.SERVER_BUILD_VERSION));
			System.out.println("  SERVER_BUILD_NUMBER: " + result.get(MdekKeys.SERVER_BUILD_NUMBER));
			System.out.println("  SERVER_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.SERVER_BUILD_TIMESTAMP)));

		} else {
			handleError(response);
		}
	}

	private IngridDocument getCatalogAdmin() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getCatalogAdmin ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getCatalogAdmin(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	private IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String onlyFreeAddressesInfo = (onlyFreeAddresses) ? "ONLY FREE ADDRESSES" : "ONLY NO FREE ADDRESSES";
		System.out.println("\n###### INVOKE fetchTopAddresses " + onlyFreeAddressesInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchTopAddresses(plugId, myUserUuid, onlyFreeAddresses);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchSubAddresses(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchSubAddresses(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getAddressPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressPath(plugId, uuidIn, myUserUuid);
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
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, howMuch, myUserUuid);
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

	private IngridDocument fetchObject(String uuid, Quantity howMuch) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, howMuch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeObjectWithoutManipulation(IngridDocument oDocIn,
			boolean refetchObject) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeObject ######");

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.storeObject(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObjectWorkingCopy " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObjectWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
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

	private IngridDocument storeAddressWithoutManipulation(IngridDocument aDocIn,
			boolean refetchAddress) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

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
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, refetchAddress, myUserUuid);
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
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, new Integer(-1));
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
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, false, myUserUuid);
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
			response = mdekCallerAddress.storeAddress(plugId, aRefetchedDoc, true, myUserUuid);
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
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialAddress ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getInitialAddress(plugId, newBasicAddress, myUserUuid);
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

	private IngridDocument getInitialObject(IngridDocument newBasicObject) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialObject ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getInitialObject(plugId, newBasicObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument checkAddressSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkAddressSubTree ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.checkAddressSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument deleteAddress(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddress " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddress(plugId, uuid, forceDeleteReferences, myUserUuid);
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

	private IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObject " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObject(plugId, uuid, forceDeleteReferences, myUserUuid);
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

	private IngridDocument deleteAddressWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddressWorkingCopy " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddressWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
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
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		String copyToFreeAddressInfo = (copyToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE copyAddress " + copySubtreeInfo + copyToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.copyAddress(plugId, fromUuid, toUuid, copySubtree, copyToFreeAddress, myUserUuid);
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
			boolean moveToFreeAddress)
	{
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String moveToFreeAddressInfo = (moveToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE moveAddress " + moveToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.moveAddress(plugId, fromUuid, toUuid, moveToFreeAddress, myUserUuid);
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

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String withRefetchInfo = (withRefetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE publishAddress  " + withRefetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.publishAddress(plugId, aDocIn, withRefetch, myUserUuid);
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

	private IngridDocument searchAddress(IngridDocument searchParams,
			int startHit, int numHits) {
		if (searchParams == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE searchAddress ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.searchAddresses(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + l.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument a : l) {
				debugAddressDoc(a);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return result;
	}

	private void debugUserDoc(IngridDocument g) {
		System.out.println("User: " + g.get(MdekKeysSecurity.IDC_USER_ID) 
			+ ", " + g.get(MdekKeysSecurity.IDC_USER_ADDR_UUID)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + exUtils.extractModUserData((IngridDocument)g.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + g);
	}
	
	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
//		);
//		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + exUtils.extractModUserData((IngridDocument)a.get(MdekKeys.MOD_USER))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + a);

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
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
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
		docList = (List<IngridDocument>) a.get(MdekKeys.PATH_ORGANISATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Path Organisations: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}			
		}
	}

	private void debugObjectDoc(IngridDocument o) {
		System.out.println("Object: " + o.get(MdekKeys.ID) 
			+ ", " + o.get(MdekKeys.UUID)
			+ ", " + o.get(MdekKeys.TITLE)
//		);
//		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.get(MdekKeys.PUBLICATION_CONDITION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + exUtils.extractModUserData((IngridDocument)o.get(MdekKeys.MOD_USER))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + o);

		List<IngridDocument>  docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			doFullOutput = false;
			for (IngridDocument a : docList) {
				debugAddressDoc(a);
			}			
			doFullOutput = true;
		}
	}

	private void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));

		if (!doFullOutput) {
			return;
		}

		// detailed output  
		List<MdekError> errors = mdekCaller.getErrorsFromResponse(response);
		doFullOutput = false;
		for (MdekError err : errors) {
			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				IngridDocument info = err.getErrorInfo();
				// referenced address
				debugAddressDoc(info);
				// objects referencing
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}
			}
		}
		doFullOutput = true;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
