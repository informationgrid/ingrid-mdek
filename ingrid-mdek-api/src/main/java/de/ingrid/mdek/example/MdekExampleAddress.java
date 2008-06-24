package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.MdekCaller;
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
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleAddressThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("mdek-iplug-idctest", "EXAMPLE_USER_" + threadNumber);
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

		IngridDocument aMap;

		boolean alwaysTrue = true;

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		IngridDocument doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

// ====================
// test single stuff
// -----------------------------------
/*
		// check address manipulation -> "create user" stored in comment

		supertool.setFullOutput(true);
		
		System.out.println("\n----- address details -----");
		aMap = supertool.fetchAddress(personUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change and store existing address -> working copy ! -----");
		storeAddressWithManipulation(aMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(personUuid, true);
		
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
		IngridDocument oMap = supertool.fetchObject(objFrom, Quantity.DETAIL_ENTITY);
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
		supertool.storeObject(oMap, true);

		System.out.println("\n----- load referenced address -> has reference from published object only ! -----");
		supertool.fetchAddress(adrTo, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objFrom, true);

		System.out.println("\n----- verify from object, no working version and references to address again ! -----");
		supertool.fetchObject(objFrom, Quantity.DETAIL_ENTITY);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// ====================
*/
		// ===================================
		System.out.println("\n----- top addresses -----");
		supertool.fetchTopAddresses(true);
		supertool.fetchTopAddresses(false);

		// -----------------------------------
		System.out.println("\n----- sub addresses -----");
//		fetchSubAddresses(topUuid);
		supertool.fetchSubAddresses("386644BF-B449-11D2-9A86-080000507261");

		// -----------------------------------
		System.out.println("\n----- address path -----");
		supertool.getAddressPath(personUuid);

		// -----------------------------------
		System.out.println("\n----- address details -----");
		aMap = supertool.fetchAddress(personUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing address");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing address -> working copy ! -----");
		storeAddressWithManipulation(aMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(personUuid, true);
		
		System.out.println("\n----- and reload -----");
		aMap = supertool.fetchAddress(personUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new address");
		System.out.println("=========================");

		System.out.println("\n----- check load initial data for TOP ADDRESS -----");
		// set no parent
		IngridDocument newAdrDoc = new IngridDocument();
		supertool.getInitialAddress(newAdrDoc);

		System.out.println("\n----- check load initial data (from " + personUuid + ") -----");
		// initial data from person address (to test take over of SUBJECT_TERMS)
		newAdrDoc = new IngridDocument();
		// supply parent uuid !
		newAdrDoc.put(MdekKeys.PARENT_UUID, personUuid);
		newAdrDoc = supertool.getInitialAddress(newAdrDoc);

		System.out.println("\n----- extend initial address and store -----");

		// extend initial address with own data !
		System.out.println("- add NAME, GIVEN_NAME, CLASS");
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());

		// new parent
		System.out.println("- store under parent: " + parentUuid);
		newAdrDoc.put(MdekKeys.PARENT_UUID, parentUuid);
		IngridDocument aMapNew = storeAddressWithManipulation(newAdrDoc);
		// uuid created !
		String newAddrUuid = (String) aMapNew.get(MdekKeys.UUID);

		System.out.println("\n----- verify new subaddress -> load parent subaddresses -----");
		supertool.fetchSubAddresses(parentUuid);

		System.out.println("\n----- do \"forbidden\" store -> \"free address\" WITH parent -----");
		Integer origType = (Integer) aMapNew.get(MdekKeys.CLASS);
		aMapNew.put(MdekKeys.CLASS, MdekUtils.AddressType.FREI.getDbValue());
		supertool.storeAddress(aMapNew, false);
		aMapNew.put(MdekKeys.CLASS, origType);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITH sub tree) -> ERROR -----");
		String addressFrom = personUuid;
		String addressTo = null;
		aMap = supertool.copyAddress(addressFrom, addressTo, true, true);
		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITHOUT sub tree) -----");
		aMap = supertool.copyAddress(addressFrom, addressTo, false, true);
		String copy1Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchAddress(copy1Uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify copy, load top FREE ADDRESSES -> new FREE ADDRESS -----");
		supertool.fetchTopAddresses(true);

		System.out.println("\n\n----- copy FREE Address under parent of new address -----");
		addressFrom = freeUuid;
		addressTo = parentUuid;
		aMap = supertool.copyAddress(addressFrom, addressTo, true, false);
		String copy2Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchAddress(copy2Uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify children -> new child -----");
		supertool.fetchSubAddresses(addressTo);

		System.out.println("\n\n----- copy FREE Address to FREE address -----");
		addressFrom = freeUuid;
		addressTo = null;
		aMap = supertool.copyAddress(addressFrom, addressTo, false, true);
		String copy3Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchAddress(addressFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchAddress(copy3Uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- delete copies (WORKING COPY) -> FULL DELETE -----");
		supertool.deleteAddressWorkingCopy(copy1Uuid, true);
		supertool.deleteAddressWorkingCopy(copy2Uuid, true);
		supertool.deleteAddressWorkingCopy(copy3Uuid, true);

		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new address below new address (WITH sub tree) -----");
		IngridDocument subtreeCopyDoc = supertool.copyAddress(parentUuid, newAddrUuid, true, false);
		String subtreeCopyUuid = subtreeCopyDoc.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy -> load children of new address -----");
		supertool.fetchSubAddresses(newAddrUuid);
		System.out.println("\n\n----- verify copied sub addresses -> load children of copy -----");
		supertool.fetchSubAddresses(subtreeCopyUuid);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n----- create NEW TOP ADDRESS -----");
		IngridDocument newTopAddrDoc = new IngridDocument();
		newTopAddrDoc = supertool.getInitialAddress(newTopAddrDoc);
		newTopAddrDoc.put(MdekKeys.ORGANISATION, "TEST TOP ADDRESS");
		newTopAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		newTopAddrDoc = supertool.storeAddress(newTopAddrDoc, true);
		// uuid created !
		String newTopAddrUuid = (String) newTopAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n\n----- move new address to NEW TOP ADDRESS -> ERROR (new parent not published) -----");
		String oldParentUuid = parentUuid;
		String newParentUuid = newTopAddrUuid;
		supertool.moveAddress(newAddrUuid, newParentUuid, false);
		System.out.println("\n----- publish NEW TOP ADDRESS -----");
		supertool.publishAddress(newTopAddrDoc, true);
		System.out.println("\n\n----- move new address again -> SUCCESS (new parent published) -----");
		supertool.moveAddress(newAddrUuid, newParentUuid, false);
		System.out.println("\n----- check new address subtree -> has working copies ! (move worked !) -----");
		supertool.checkAddressSubTree(newAddrUuid);
		System.out.println("\n----- verify old parent subaddresses (cut) -----");
		supertool.fetchSubAddresses(oldParentUuid);
		System.out.println("\n----- verify new parent subaddresses (added) -----");
		supertool.fetchSubAddresses(newParentUuid);

		System.out.println("\n----- move new Address to FREE Address ! -> ERROR: has subtree -----");
		supertool.moveAddress(newAddrUuid, null, true);
		System.out.println("\n\n----- delete subtree of new Address -----");
		supertool.deleteAddress(subtreeCopyUuid, true);
		System.out.println("\n----- move new Address to FREE Address ! -> ERROR (type conflicts -> EINHEIT) -----");
		supertool.moveAddress(newAddrUuid, null, true);
		System.out.println("\n----- publish new Address as PERSON -----");
		aMapNew.put(MdekKeys.CLASS, MdekUtils.AddressType.PERSON.getDbValue());
		supertool.publishAddress(aMapNew, true);
		System.out.println("\n----- store changed working copy of new Address -----");
		aMapNew.put(MdekKeys.GIVEN_NAME, "changed!!!!!!!!");
		supertool.storeAddress(aMapNew, true);
		System.out.println("\n----- move new Address to FREE Address ! -> SUCCESS -----");
		supertool.moveAddress(newAddrUuid, null, true);
		supertool.setFullOutput(false);
		supertool.fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		System.out.println("\n----- move new FREE Address to NOT FREE Address ! -> SUCCESS -----");
		newParentUuid = topUuid;
		supertool.moveAddress(newAddrUuid, newParentUuid, false);
		supertool.setFullOutput(false);
		supertool.fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);

		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		supertool.moveAddress(topUuid, parentUuid, false);
		System.out.println("\n----- do \"forbidden\" move (institution to free address) -----");
		supertool.moveAddress(topUuid, null, true);

		System.out.println("\n----- delete NEW TOP ADDRESS -----");
		supertool.deleteAddress(newTopAddrUuid, true);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("DELETE TEST");
		System.out.println("=========================");

		System.out.println("\n----- delete new address (WORKING COPY) -> NO full delete -----");
		supertool.deleteAddressWorkingCopy(newAddrUuid, true);
		System.out.println("\n----- delete new address (FULL) -> full delete -----");
		supertool.deleteAddress(newAddrUuid, true);
		System.out.println("\n----- verify deletion of new address -----");
		supertool.fetchAddress(newAddrUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subaddresses -----");
		supertool.fetchSubAddresses(newParentUuid);

		// -----------------------------------
		System.out.println("\n----- test deletion of references / WARNINGS -----");

		System.out.println("\n----- create new TOP ADDRESS -----");
		IngridDocument toAddrDoc = new IngridDocument();
		toAddrDoc = supertool.getInitialAddress(toAddrDoc);
		toAddrDoc.put(MdekKeys.ORGANISATION, "TEST TOP ADDRESS");
		toAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		toAddrDoc = supertool.storeAddress(toAddrDoc, true);
		// uuid created !
		String topAddrUuid = (String) toAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new SUB ADDRESS to be REFERENCED -----");
		// initial data from parent
		toAddrDoc = new IngridDocument();
		toAddrDoc.put(MdekKeys.PARENT_UUID, topAddrUuid);
		toAddrDoc = supertool.getInitialAddress(toAddrDoc);
		toAddrDoc.put(MdekKeys.ORGANISATION, "TEST SUB ADDRESS -> wird referenziert");
		toAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		toAddrDoc = supertool.storeAddress(toAddrDoc, true);
		// uuid created !
		String toAddrUuid = (String) toAddrDoc.get(MdekKeys.UUID);
		
		System.out.println("\n----- create new OBJECT REFERENCING ADDRESS as AUSKUNFT-----");
		IngridDocument fromObjDoc = new IngridDocument();
		fromObjDoc = supertool.getInitialObject(fromObjDoc);
		fromObjDoc.put(MdekKeys.TITLE, "TEST OBJECT -> referenziert");
		ArrayList<IngridDocument> adrRefsList = new ArrayList<IngridDocument>(1);
		toAddrDoc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID); // AUSKUNFT
		adrRefsList.add(toAddrDoc);
		fromObjDoc.put(MdekKeys.ADR_REFERENCES_TO, adrRefsList);
		fromObjDoc = supertool.storeObject(fromObjDoc, true);
		// uuid created !
		String fromObjUuid = (String) fromObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete TOP ADDRESS (WORKING COPY) WITHOUT refs -> Error ADDRESS_IS_AUSKUNFT -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, false);

		System.out.println("\n----- change OBJECT REFERENCE to NOT AUSKUNFT -----");
		adrRefsList = (ArrayList<IngridDocument>) fromObjDoc.get(MdekKeys.ADR_REFERENCES_TO);
		adrRefsList.get(0).put(MdekKeys.RELATION_TYPE_ID, -1); // free reference
		fromObjDoc = supertool.storeObject(fromObjDoc, true);
		
		System.out.println("\n----- delete TOP ADDRESS (WORKING COPY) WITHOUT refs -> Error ENTITY_REFERENCED_BY_OBJ -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, false);
		System.out.println("\n----- delete TOP ADDRESS (FULL) WITHOUT refs -> Error ENTITY_REFERENCED_BY_OBJ -----");
		supertool.deleteAddress(topAddrUuid, false);
		
		System.out.println("\n----- delete ADDRESS (WORKING COPY) WITH refs -> OK (full delete) -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, true);
		System.out.println("\n----- delete OBJECT (WORKING COPY) without refs -> OK (full delete) -----");
		supertool.deleteObject(fromObjUuid, false);

		// -----------------------------------
		System.out.println("\n----- test delete of IDC AUSKUNFT address -----");

		System.out.println("\n----- delete ADDRESS referenced as AUSKUNFT -> Error ADDRESS_IS_AUSKUNFT (for 486 objects !) -----");
		supertool.deleteAddress("BF1156BA-F74D-11D4-8868-0060084A6015", false);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("PUBLISH TEST");
		System.out.println("=========================");

		System.out.println("\n----- publish NEW TOP ADDRESS immediately -----");
		IngridDocument newTopDoc = new IngridDocument();
		newTopDoc.put(MdekKeys.ORGANISATION, "TEST NEW TOP ADDRESS DIRECT PUBLISH");
		newTopDoc.put(MdekKeys.CLASS, AddressType.INSTITUTION.getDbValue());
		aMap = supertool.publishAddress(newTopDoc, true);
		// uuid created !
		String newTopUuid = (String)aMap.get(MdekKeys.UUID);

		System.out.println("\n----- delete NEW TOP ADDRESS (FULL) -----");
		supertool.deleteAddress(newTopUuid, true);

		System.out.println("\n----- copy address (without subnodes) -> returns only \"TREE Data\" of copied address -----");
		addressFrom = newParentUuid;
		addressTo = topUuid;
		aMap = supertool.copyAddress(addressFrom, addressTo, false, false);
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
		supertool.publishAddress(newPubDoc, true);

		System.out.println("\n----- refetch FULL PARENT, UNPUBLISHED !  -----");
		aMap = supertool.fetchAddress(pub1Uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change organization and publish PARENT -> create pub version/\"delete\" work version -----");
		aMap.put(MdekKeys.ORGANISATION, "COPIED, Orga CHANGED and PUBLISHED: " + aMap.get(MdekKeys.ORGANISATION));	
		supertool.publishAddress(aMap, true);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		aMap = supertool.publishAddress(newPubDoc, true);
		// uuid created !
		String pub2Uuid = aMap.getString(MdekKeys.UUID);

		System.out.println("\n----- verify -> load sub addresses of parent of copy -----");
		supertool.fetchSubAddresses(addressTo);
		System.out.println("\n----- delete 1. published copy = sub-address (WORKING COPY) -> NO DELETE -----");
		supertool.deleteAddressWorkingCopy(pub1Uuid, true);
		System.out.println("\n----- delete 2. published copy = sub-sub-address (FULL) -----");
		supertool.deleteAddress(pub2Uuid, true);
		System.out.println("\n----- delete 1. published copy = sub-address (FULL) -----");
		supertool.deleteAddress(pub1Uuid, true);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("SEARCH TEST");
		System.out.println("=========================");

		System.out.println("\n----- search address by orga / name,given-name -----");
		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.ORGANISATION, "Bezirksregierung");
		supertool.searchAddress(searchParams, 0, 5);
		supertool.searchAddress(searchParams, 5, 5);
		supertool.searchAddress(searchParams, 10, 5);

		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.NAME, "Dahlmann");
		searchParams.put(MdekKeys.GIVEN_NAME, "Irene");
		supertool.searchAddress(searchParams, 0, 5);

		// ===================================
		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	private IngridDocument storeAddressWithManipulation(IngridDocument aDocIn) {
		if (aDocIn == null) {
			return null;
		}

		IngridDocument result;

		System.out.println("\n###### CALLED storeAddress (with manipulation) !!!");
		
		// manipulate former loaded address !
		System.out.println("MANIPULATE ADDRESS");

		System.out.println("- change test ORGANISATION, GIVEN_NAME,NAME_FORM_KEY, TITLE_OR_FUNCTION_KEY");
		String origORGANISATION = aDocIn.getString(MdekKeys.ORGANISATION);
		String origGIVEN_NAME = aDocIn.getString(MdekKeys.GIVEN_NAME);
		aDocIn.put(MdekKeys.ORGANISATION, "TEST/" + origORGANISATION);
		aDocIn.put(MdekKeys.GIVEN_NAME, "TEST/" + origGIVEN_NAME);
		// check storing of value to key !
		aDocIn.put(MdekKeys.NAME_FORM_KEY, 2);
		aDocIn.put(MdekKeys.TITLE_OR_FUNCTION_KEY, 3);

		// add entry to COMMUNICATION
		System.out.println("- add test COMMUNICATION");
		List<IngridDocument> docList = (List<IngridDocument>) aDocIn.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		// check COMMUNICATION_MEDIUM_KEY -> COMMUNICATION_MEDIUM is stored via syslist
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, 2);
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
		IngridDocument createUserDoc = new IngridDocument();
		createUserDoc.put(MdekKeys.UUID, supertool.getCallingUserUuid());
		testDoc.put(MdekKeys.CREATE_USER, createUserDoc);
		docList.add(testDoc);
		aDocIn.put(MdekKeys.COMMENT_LIST, docList);

		// store
		System.out.println("STORE");
		result = supertool.storeAddress(aDocIn, false);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredAddress = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredAddress);
			System.out.println("refetch Address");
			IngridDocument aRefetchedDoc = supertool.fetchAddress(uuidStoredAddress, Quantity.DETAIL_ENTITY);
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
			result = supertool.storeAddress(aRefetchedDoc, true);
		}

		return result;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
