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
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
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
		MdekClientCaller.initialize(new File((String) map.get("--descriptor")));
		IMdekClientCaller mdekClientCaller = MdekClientCaller.getInstance();

		// wait till iPlug registered !
		System.out.println("\n###### waiting for mdek iPlug to register ######\n");
		boolean plugRegistered = false;
		while (!plugRegistered) {
			List<String> iPlugs = mdekClientCaller.getRegisteredIPlugs();
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
		
		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
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
		IngridDocument newAddrDoc;
		String newAddrUuid;

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
		// Test EH Cache !
		// ---------------

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- ENABLE WORKFLOW in catalog -----");
		IngridDocument catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		catDoc = supertool.storeCatalog(catDoc, true);

		supertool.setFullOutput(false);

		for (int i = 1; i <= 5; i++) {
			supertool.fetchTopAddresses(true);
		}
		for (int i = 1; i <= 5; i++) {
			supertool.fetchTopAddresses(false);
		}

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- DISABLE WORKFLOW in catalog -----");
		catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		catDoc = supertool.storeCatalog(catDoc, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

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

// -----------------------------------

		// TEST handling of ORG_ID, should be unique
		// ==============

		System.out.println("\n----- Create new address working version with ORG_ID (hack, org_id never passed from client, we simulate import !) -----");
		IngridDocument newDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		newDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, "ORIGINAL_ADDRESS_IDENTIFIER");
		newDoc = supertool.storeAddress(newDoc, true);
		String newUuid = (String) newDoc.get(MdekKeys.UUID);

		System.out.println("\n----- Publish new address -> ORG_ID in published version (check database) -----");
		newDoc = supertool.publishAddress(newDoc, true);
		supertool.deleteAddress(newUuid, false);

		System.out.println("\n\n\n----- Publish new address immediately with ORG_ID (hack, org_id never passed from client, we simulate import !) -----");
		newDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		newDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, "ORIGINAL_ADDRESS_IDENTIFIER");
		newDoc = supertool.publishAddress(newDoc, true);
		newUuid = (String) newDoc.get(MdekKeys.UUID);

		System.out.println("\n----- Store working version of new address -> ORG_ID working version (check database) -----");
		newDoc = supertool.storeAddress(newDoc, true);

		System.out.println("\n----- Copy new address -> ORG_ID NOT copied (check database) -----");
		aMap = supertool.copyAddress(newUuid, null, false, false);
		String copyUuid = (String)aMap.get(MdekKeys.UUID);

		System.out.println("\n----- Clean up -----");
		supertool.deleteAddress(newUuid, false);
		supertool.deleteAddress(copyUuid, false);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// TEST of GEMET searchterms and INSPIRE searchterms
		// ==============

		System.out.println("\n----- PUBLISH new Term-Address with FREE, UMTHES, GEMET and INSPIRE searchterms -----");
		IngridDocument newDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		// extend initial address with searchterms !
		List<IngridDocument> myTerms = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.SUBJECT_TERMS, myTerms);
		IngridDocument myTerm;
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.UMTHES.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "Geographie");
		myTerm.put(MdekKeys.TERM_SNS_ID, "uba_thes_10946");
		myTerms.add(myTerm);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "TEST Freier Searchterm !");
		myTerms.add(myTerm);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.GEMET.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "Feinstaub");
		myTerm.put(MdekKeys.TERM_SNS_ID, "uba_thes_9320");
		myTerm.put(MdekKeys.TERM_GEMET_ID, "3209");
		myTerms.add(myTerm);
		// extend initial address with searchterms INSPIRE !
		myTerms = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.SUBJECT_TERMS_INSPIRE, myTerms);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.INSPIRE.getDbValue());
		myTerm.put(MdekKeys.TERM_ENTRY_ID, 311);
		myTerms.add(myTerm);
		newDoc = supertool.publishAddress(newDoc, true);
		// uuid created !
		String newUuid = (String) newDoc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE again -> NEW working copy, so NEW FREE searchterm created in DB ! same THESAURUS/INSPIRE terms ! -----");
		newDoc = supertool.storeAddress(newDoc, true);

		System.out.println("\n----- STORE again -> NO NEW FREE searchterm, because term already connected to same address -----");
		newDoc = supertool.storeAddress(newDoc, true);

		System.out.println("\n----- Get initial data for new SUB Address inheriting thesaurus and INSPIRE terms -----");
		supertool.newAddressDoc(newUuid, AddressType.INSTITUTION);

		System.out.println("\n\n=========================");
		System.out.println("EXPORT TERM-ADDRESS");

		System.out.println("\n----- export TERM-Address -----");
		supertool.exportAddressBranch(newUuid, true, null);
		supertool.setFullOutput(true);
		IngridDocument result = supertool.getExportInfo(true);
		byte[] exportTermAddrZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n\n----- Remove all terms from TERM-Address and STORE ! -> working copy has no terms -----");
		newDoc.put(MdekKeys.SUBJECT_TERMS, new ArrayList<IngridDocument>());
		newDoc.put(MdekKeys.SUBJECT_TERMS_INSPIRE, new ArrayList<IngridDocument>());
		newDoc = supertool.storeAddress(newDoc, true);

		System.out.println("\n\n=========================");
		System.out.println("IMPORT TERM-ADDRESS");

		System.out.println("\n----- create new Import Top Node for Objects (NEVER PUBLISHED) -----");
		IngridDocument objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		String objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		IngridDocument addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		String addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- import TERM-Address as WORKING VERSION -> all terms again in working version -----");
		supertool.importEntities(exportTermAddrZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(newUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteAddress(newUuid, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------
*/
		// AOR Paging, see http://jira.101tec.com/browse/INGRID-1503
		// ------------------

		System.out.println("\n\n=========================");
		System.out.println("Test Object References to Address (AOR) paging ");
		System.out.println("=========================");

		String addrReferenced = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		String objReferencing = "C5FEA80D-6AB2-11D3-BB32-1C7607C10000";

		System.out.println("\n----- initial load of referenced address -> all obj references from working version -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- load object referencing address -----");
		IngridDocument oMap = supertool.fetchObject(objReferencing, FetchQuantity.EDITOR_ENTITY);
		List<IngridDocument> docList = (List<IngridDocument>) oMap.get(MdekKeys.ADR_REFERENCES_TO);
		// find address to remove
		IngridDocument docToRemove = null;
		for (IngridDocument myDoc : docList) {
			if (addrReferenced.equals(myDoc.get(MdekKeys.UUID))) {
				docToRemove = myDoc;
				break;
			}
		}

		System.out.println("\n----- remove reference to address / store working version -----");
		if (docToRemove != null) {
			docList.remove(docToRemove);
		}
		supertool.storeObject(oMap, true);

		System.out.println("\n----- load referenced address WITH PAGING, start at 0, fetch 1 -> only 1 reference from published included ! -----");
		aMap = supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 0, 1);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 0, 1);
		System.out.println("\n----- store address with refetch (same paging params when refetching) -----");
		supertool.storeAddress(aMap, true, 0, 1);
		System.out.println("\n----- publish address with refetch (same paging params when refetching) -----");
		supertool.publishAddress(aMap, true, false, 0, 1);
		System.out.println("\n----- load details of referenced address, fetch all objRefs -> all obj references there, 1 reference from published, all from working included -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- load referenced address WITH PAGING, start at 0, fetch 2 -> 1 reference from published, 1 from working included ! -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 0, 2);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 0, 2);

		System.out.println("\n----- load referenced address WITH PAGING, start at 0, fetch 10 -> 1 reference from published, all from working included ! -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 0, 10);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 0, 10);

		System.out.println("\n----- load referenced address WITH PAGING, start at 10, fetch 10 -> NO REFERENCES INCLUDED -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 10, 10);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 10, 10);

		System.out.println("\n----- load referenced address WITH PAGING, start at 0, fetch 0 -> NO REFERENCES INCLUDED -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 0, 0);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 0, 0);

		System.out.println("\n----- load referenced address WITH PAGING, start at 1, fetch 2 -> 0 reference from published, first 2 from working included ! -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY, 1, 2);
		System.out.println("\n----- load referencing objects separately -----");
		supertool.fetchAddressObjectReferences(addrReferenced, 1, 2);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objReferencing, true);
		supertool.deleteAddressWorkingCopy(addrReferenced, true);

		System.out.println("\n----- verify object, no working version and references to address again ! -----");
		supertool.fetchObject(objReferencing, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- verify address, only object references from \"working version\" (equals published ones) -----");
		supertool.fetchAddress(addrReferenced, FetchQuantity.EDITOR_ENTITY);
/*
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
		aMap = supertool.fetchAddress(personUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing address");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing address -> working copy ! -----");
		storeAddressWithManipulation(aMap);

		System.out.println("\n----- COMPARE ADDRESS WORKING/PUBLISHED VERSION -----");
		System.out.println("\nWORKING VERSION:");
		supertool.fetchAddress(personUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		System.out.println("\nPUBLISHED VERSION:");
		supertool.fetchAddress(personUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(personUuid, true);
		
		System.out.println("\n----- and reload -----");
		aMap = supertool.fetchAddress(personUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new address");
		System.out.println("=========================");

		System.out.println("\n----- check load initial data for TOP ADDRESS -----");
		// set no parent
		newAddrDoc = new IngridDocument();
		supertool.getInitialAddress(newAddrDoc);

		System.out.println("\n----- check load initial data (from " + personUuid + ") -----");
		// initial data from person address (to test take over of SUBJECT_TERMS)
		newAddrDoc = new IngridDocument();
		// supply parent uuid !
		newAddrDoc.put(MdekKeys.PARENT_UUID, personUuid);
		newAddrDoc = supertool.getInitialAddress(newAddrDoc);

		System.out.println("\n----- extend initial address and store -----");

		// extend initial address with own data !
		System.out.println("- add NAME, GIVEN_NAME, CLASS");
		newAddrDoc.put(MdekKeys.NAME, "testNAME");
		newAddrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		// email has to exist !
		docList = (List<IngridDocument>) newAddrDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newAddrDoc.put(MdekKeys.COMMUNICATION, docList);

		// new parent
		System.out.println("- store under parent: " + parentUuid);
		newAddrDoc.put(MdekKeys.PARENT_UUID, parentUuid);
		newAddrDoc = storeAddressWithManipulation(newAddrDoc);
		// uuid created !
		newAddrUuid = (String) newAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- verify new subaddress -> load parent subaddresses -----");
		supertool.fetchSubAddresses(parentUuid);

		System.out.println("\n----- do \"forbidden\" store -> ERROR: FREE_ADDRESS_WITH_PARENT -----");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.FREI.getDbValue());
		supertool.storeAddress(newAddrDoc, false);

		System.out.println("\n----- Clean Up: refetch new address -----");
		newAddrDoc = supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("MERGE TEST");
		System.out.println("=========================");

		// Test data: parent with 2 children !
		String mergeParentUuid = "38664489-B449-11D2-9A86-080000507261";
		String mergeChild1Uuid = "38664594-B449-11D2-9A86-080000507261";
		String mergeChild2Uuid = "38664595-B449-11D2-9A86-080000507261";
		boolean oldDoFullOutput = supertool.doFullOutput;
		supertool.doFullOutput = false;

		System.out.println("\n\n----- check details of parent, will be merged to subaddresses ! -----");
		doc = supertool.fetchAddress(mergeParentUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n----- Manipulate sub addresses where data is merged to ! -----");

		System.out.println("\n\n----- STORE 1. child as working copy with changed merge data -----");
		System.out.println("\n----- load 1. child FOR ORIGINAL DATA (used to discard changes) -----");
		IngridDocument origDocChild1 = supertool.fetchAddress(mergeChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(origDocChild1);
		System.out.println("\n----- load 1. child again, MANIPULATE AND STORE AS WORKING VERSION -----");
		doc = supertool.fetchAddress(mergeChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.manipulateAddressDocMergeData(doc, "MM1_");
		doc = supertool.storeAddress(doc, true);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n----- PUBLISH 2. child with changed merge data -----");
		System.out.println("\n----- load 2. child FOR ORIGINAL DATA (used to discard changes) -----");
		IngridDocument origDocChild2 = supertool.fetchAddress(mergeChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(origDocChild2);
		System.out.println("\n----- load 2. child again, MANIPULATE AND PUBLISH -----");
		doc = supertool.fetchAddress(mergeChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.manipulateAddressDocMergeData(doc, "MM2_");
		doc = supertool.publishAddress(doc, true, false);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n----- Perform Merge ! -----");
		supertool.mergeAddressToSubAddresses(mergeParentUuid);
		
		System.out.println("\n\n----- Check merged data in sub addresses, same as parent, work state NOT CHANGED ! -----");
		System.out.println("\n----- load 1. child, parent merge data ! WORKING VERSION ! -----");
		doc = supertool.fetchAddress(mergeChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(doc);
		System.out.println("\n----- load 2. child, parent merge data ! PUBLISHED ! -----");
		doc = supertool.fetchAddress(mergeChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n----- discard changes -> back to original versions -----");
		doc = supertool.publishAddress(origDocChild1, true, false);
		supertool.debugAddressDocMergeData(doc);
		doc = supertool.publishAddress(origDocChild2, true, false);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n=========================");
		System.out.println("MERGE TEST WITH WORKFLOW");
		System.out.println("=========================");

		System.out.println("\n\n----- First Manipulate sub addresses where data is merged to ! -----");

		System.out.println("\n\n----- STORE 1. child as working copy with changed merge data -----");
		System.out.println("\n----- load 1. child, MANIPULATE AND STORE AS WORKING VERSION -----");
		doc = supertool.fetchAddress(mergeChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.manipulateAddressDocMergeData(doc, "MM1_");
		doc = supertool.storeAddress(doc, true);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n----- PUBLISH 2. child with changed merge data -----");
		System.out.println("\n----- load 2. child, MANIPULATE AND PUBLISH -----");
		doc = supertool.fetchAddress(mergeChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.manipulateAddressDocMergeData(doc, "MM2_");
		doc = supertool.publishAddress(doc, true, false);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n\n----- create new group1 -----");
		IngridDocument newGroup1Doc = new IngridDocument();
		newGroup1Doc.put(MdekKeys.NAME, "TEST Gruppe1");
		newGroup1Doc = supertool.createGroup(newGroup1Doc, true);
		Long newGroup1Id = (Long) newGroup1Doc.get(MdekKeysSecurity.ID);

		System.out.println("\n----- create new user of group -----");
		IngridDocument newMetaAdminGroup1Doc = new IngridDocument();
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.NAME, "MD_ADMINISTRATOR GROUP 1 name");
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.GIVEN_NAME, "MD_ADMINISTRATOR GROUP 1 given_name");
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroup1Id});
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdminGroup1Doc = supertool.createUser(newMetaAdminGroup1Doc, true);
		Long newMetaAdminGroup1Id = (Long) newMetaAdminGroup1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdminGroup1Uuid = newMetaAdminGroup1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("--- Add Permissions to group -> NO QS ! -----");
		System.out.println("----- add parent address WRITE_TREE permissions to group 1 -----");
		supertool.addAddrPermissionToGroupDoc(newGroup1Doc, mergeParentUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		newGroup1Doc = supertool.storeGroup(newGroup1Doc, true, false);

		System.out.println("\n----- !!! ENABLE WORKFLOW in catalog !!! -----");
		IngridDocument catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO user NO QS -----");
		supertool.setCallingUser(newMetaAdminGroup1Uuid);

		System.out.println("\n\n----- Perform Merge ! try to publish sub address -> NON QA -> ASSIGNED TO QA ! -----");
		supertool.mergeAddressToSubAddresses(mergeParentUuid);
		
		System.out.println("\n\n----- Check merged data in sub addresses, same as parent, work state WORKING VERSION or ASSIGNED TO QA -----");
		System.out.println("\n----- load 1. child, parent merge data ! WORKING VERSION ! -----");
		doc = supertool.fetchAddress(mergeChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(doc);
		System.out.println("\n----- load 2. child, parent merge data ! ASSIGNED TO QA ! -----");
		doc = supertool.fetchAddress(mergeChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.debugAddressDocMergeData(doc);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- DISABLE WORKFLOW in catalog -----");
		catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n----- delete user  -----");
		supertool.deleteUser(newMetaAdminGroup1Id);

		System.out.println("\n----- delete group -----");
		supertool.deleteGroup(newGroup1Id, false);

		System.out.println("\n----- discard changes -> back to original versions -----");
		doc = supertool.publishAddress(origDocChild1, true, false);
		supertool.debugAddressDocMergeData(doc);
		doc = supertool.publishAddress(origDocChild2, true, false);
		supertool.debugAddressDocMergeData(doc);

		supertool.doFullOutput = oldDoFullOutput;

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITH sub tree) -> ERROR: FREE_ADDRESS_WITH_SUBTREE -----");
		String addressFrom = personUuid;
		String addressTo = null;
		aMap = supertool.copyAddress(addressFrom, addressTo, true, true);
		System.out.println("\n\n----- copy PERSON address to FREE ADDRESS (WITHOUT sub tree) -----");
		aMap = supertool.copyAddress(addressFrom, addressTo, false, true);
		String copy1Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchAddress(addressFrom, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchAddress(copy1Uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- verify copy, load top FREE ADDRESSES -> new FREE ADDRESS -----");
		supertool.fetchTopAddresses(true);

		System.out.println("\n\n----- copy FREE Address under parent of new address -----");
		addressFrom = freeUuid;
		addressTo = parentUuid;
		aMap = supertool.copyAddress(addressFrom, addressTo, true, false);
		String copy2Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -> free address with ORGANISATION and NAME -----");
		supertool.fetchAddress(addressFrom, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- then load copy -> PERSON, NO ORGANISATION but NAME -----");
		supertool.fetchAddress(copy2Uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- verify children -> new child -----");
		supertool.fetchSubAddresses(addressTo);

		System.out.println("\n\n----- copy FREE Address to FREE address -----");
		addressFrom = freeUuid;
		addressTo = null;
		aMap = supertool.copyAddress(addressFrom, addressTo, false, true);
		String copy3Uuid = aMap.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchAddress(addressFrom, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchAddress(copy3Uuid, FetchQuantity.EDITOR_ENTITY);

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

		System.out.println("\n----- create NEW TOP ADDRESS (parent to move to) = INTRANET -----");
		IngridDocument newTopAddrDoc = new IngridDocument();
		newTopAddrDoc = supertool.getInitialAddress(newTopAddrDoc);
		newTopAddrDoc.put(MdekKeys.ORGANISATION, "TEST TOP ADDRESS");
		newTopAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		newTopAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		// email has to exist !
		docList = (List<IngridDocument>) newTopAddrDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newTopAddrDoc.put(MdekKeys.COMMUNICATION, docList);

		newTopAddrDoc = supertool.storeAddress(newTopAddrDoc, true);
		// uuid created !
		String newTopAddrUuid = (String) newTopAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n\n----- move new address to NEW TOP ADDRESS -> BOTH NOT PUBLISHED, OK !!! -----");
		String oldParentUuid = parentUuid;
		String newParentUuid = newTopAddrUuid;
		supertool.moveAddress(newAddrUuid, newParentUuid, false, true);
		System.out.println("\n\n----- move back -----");
		supertool.moveAddress(newAddrUuid, oldParentUuid, false, true);

		System.out.println("\n----- publish NEW TOP ADDRESS -----");
		supertool.publishAddress(newTopAddrDoc, true, true);
		System.out.println("\n----- set new address (the one to move) to INTERNET and publish (pub=work=INTERNET) -----");
		newAddrDoc = supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		newAddrDoc = supertool.publishAddress(newAddrDoc, true, false);
		System.out.println("\n----- create work version -> change title (pub=INTERNET, work=INTERNET) -----");
		newAddrDoc.put(MdekKeys.TITLE, "TEST CHANGED!!! TITLE");
		newAddrDoc = supertool.storeAddress(newAddrDoc, true);
		System.out.println("\n\n----- move new address to TOP INTRANET -> ERROR: SUBTREE_HAS_LARGER_PUBLICATION_CONDITION (pub=INTERNET) -----");
		supertool.moveAddress(newAddrUuid, newParentUuid, false, false);
		System.out.println("\n\n----- move new address again with forcePubCondition -> SUCCESS: but only pubVersion was adapted (pub=INTRANET, work=INTERNET !) -----");
		supertool.moveAddress(newAddrUuid, newParentUuid, false, true);
		System.out.println("\n\n----- verify moved address (work=INTERNET although parent=INTRANET) -----");
		newAddrDoc = supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n\n----- publish moved address -> ERROR: PARENT_HAS_SMALLER_PUBLICATION_CONDITION (parent INTRANET) -----");
		supertool.publishAddress(newAddrDoc, true, false);
		System.out.println("\n----- change moved address to INTRANET and store -----");
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		newAddrDoc = supertool.storeAddress(newAddrDoc, true);
		System.out.println("\n\n----- publish again -> SUCCESS (pub=work=INTRANET) ALTHOUGH subNodes are INTERNET, but these are NOT published yet !!! -----");
		supertool.publishAddress(newAddrDoc, true, false);
		System.out.println("\n----- check new address subtree (was also moved) -> has working copies (were copied above) -----");
		supertool.checkAddressSubTree(newAddrUuid);
		System.out.println("\n----- fetch subaddresses of new address (was moved) -----");
		System.out.println("----- NO CHANGE OF MODIFICATION TIME AND USER IN MOVED SUBTREE, see http://jira.media-style.com/browse/INGRIDII-266 -----");
		System.out.println("----- OK, not to see here, because subaddresses were created by same user :-), but tested in mdek app ! -----");
		doc = supertool.fetchSubAddresses(newAddrUuid);
		List l = (List) doc.get(MdekKeys.ADR_ENTITIES);
		if (l.size() > 0) {
			System.out.println("\n----- fetch first subaddress details -----");
			String uuid = ((IngridDocument)l.get(0)).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		System.out.println("\n----- verify old parent subaddresses (cut) -----");
		supertool.fetchSubAddresses(oldParentUuid);
		System.out.println("\n----- verify new parent subaddresses (added) -----");
		supertool.fetchSubAddresses(newParentUuid);

		System.out.println("\n----- move new Address to FREE Address ! -> ERROR: FREE_ADDRESS_WITH_SUBTREE -----");
		supertool.moveAddress(newAddrUuid, null, true, false);
		System.out.println("\n\n----- delete subtree of new Address -----");
		supertool.deleteAddress(subtreeCopyUuid, true);
		System.out.println("\n----- move new Address to FREE Address ! -> ERROR: ADDRESS_TYPE_CONFLICT (type conflicts -> EINHEIT) -----");
		supertool.moveAddress(newAddrUuid, null, true, false);
		System.out.println("\n----- publish new Address as PERSON -----");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.PERSON.getDbValue());
		supertool.publishAddress(newAddrDoc, true, false);
		System.out.println("\n----- store changed working copy of new Address -----");
		newAddrDoc.put(MdekKeys.GIVEN_NAME, "changed!!!!!!!!");
		supertool.storeAddress(newAddrDoc, true);
		System.out.println("\n----- move new Address to FREE Address ! -> SUCCESS -----");
		supertool.moveAddress(newAddrUuid, null, true, false);
		supertool.setFullOutput(false);
		supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		System.out.println("\n----- move new FREE Address to NOT FREE Address ! -> SUCCESS -----");
		newParentUuid = topUuid;
		supertool.moveAddress(newAddrUuid, newParentUuid, false, false);
		supertool.setFullOutput(false);
		supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);

		System.out.println("\n----- do \"forbidden\" move (move to subnode) -> ERROR: TARGET_IS_SUBNODE_OF_SOURCE -----");
		supertool.moveAddress(topUuid, parentUuid, false, false);
		System.out.println("\n----- do \"forbidden\" move (institution to free address) -> ERROR: FREE_ADDRESS_WITH_SUBTREE -----");
		supertool.moveAddress(topUuid, null, true, false);

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
		System.out.println("\n----- verify deletion of new address -> ERROR: UUID_NOT_FOUND -----");
		supertool.fetchAddress(newAddrUuid, FetchQuantity.EDITOR_ENTITY);
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
		
		System.out.println("\n----- create new OBJECT REFERENCING ADDRESS as Ansprechpartner -----");
		IngridDocument fromObjDoc = new IngridDocument();
		fromObjDoc = supertool.getInitialObject(fromObjDoc);
		fromObjDoc.put(MdekKeys.TITLE, "TEST OBJECT -> referenziert");
		ArrayList<IngridDocument> adrRefsList = new ArrayList<IngridDocument>(1);
		toAddrDoc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID); // Ansprechpartner
		adrRefsList.add(toAddrDoc);
		fromObjDoc.put(MdekKeys.ADR_REFERENCES_TO, adrRefsList);
		fromObjDoc = supertool.storeObject(fromObjDoc, true);
		// uuid created !
		String fromObjUuid = (String) fromObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete TOP ADDRESS (WORKING COPY) WITHOUT refs -> Error ENTITY_REFERENCED_BY_OBJ -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, false);

		System.out.println("\n----- change OBJECT REFERENCE to NOT Ansprechpartner -----");
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
		System.out.println("\n----- test delete of referenced address -----");

		System.out.println("\n----- delete referenced ADDRESS -> Error ENTITY_REFERENCED_BY_OBJ (for 486 objects !) -----");
		supertool.deleteAddress("BF1156BA-F74D-11D4-8868-0060084A6015", false);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("PUBLISH TEST");
		System.out.println("=========================");

		System.out.println("\n----- publish NEW TOP ADDRESS immediately -> ERROR: ADDRESS_HAS_NO_EMAIL -----");
		IngridDocument newTopDoc = new IngridDocument();
		newTopDoc.put(MdekKeys.ORGANISATION, "TEST NEW TOP ADDRESS DIRECT PUBLISH");
		newTopDoc.put(MdekKeys.CLASS, AddressType.INSTITUTION.getDbValue());
		newTopDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		aMap = supertool.publishAddress(newTopDoc, true, false);

		System.out.println("\n----- add email and publish -> SUCCESS -----");
		docList = new ArrayList<IngridDocument>();
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newTopDoc.put(MdekKeys.COMMUNICATION, docList);
		aMap = supertool.publishAddress(newTopDoc, true, false);
		// uuid created !
		String newTopUuid = (String)aMap.get(MdekKeys.UUID);

		System.out.println("\n----- delete NEW TOP ADDRESS (FULL) -----");
		supertool.deleteAddress(newTopUuid, true);

		System.out.println("\n----- PUBLISH NEW ADDRESS WITH VARIOUS CHECKS ! -----");
		
		System.out.println("\n----- copy address (without subnodes) to be parent of address to publish -> returns only \"TREE Data\" of copied address -----");
		addressFrom = newParentUuid;
		addressTo = topUuid;
		aMap = supertool.copyAddress(addressFrom, addressTo, false, false);
		String pubParentUuid = aMap.getString(MdekKeys.UUID);

		System.out.println("\n----- publish NEW SUB ADDRESS immediately (INTERNET) -> ERROR: PARENT NOT PUBLISHED ! -----");
		IngridDocument newPubDoc = new IngridDocument();
		newPubDoc.put(MdekKeys.ORGANISATION, "TEST NEW SUB ADDRESS DIRECT PUBLISH");
		newPubDoc.put(MdekKeys.NAME, "testNAME");
		newPubDoc.put(MdekKeys.NAME_FORM, "Herr");
		newPubDoc.put(MdekKeys.NAME_FORM_KEY, new Integer(-1));
		newPubDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newPubDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newPubDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newPubDoc.put(MdekKeys.CLASS, AddressType.PERSON.getDbValue());
		newPubDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		// email has to exist !
		docList = new ArrayList<IngridDocument>();
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newPubDoc.put(MdekKeys.COMMUNICATION, docList);
		// sub address of unpublished parent !!!
		newPubDoc.put(MdekKeys.PARENT_UUID, pubParentUuid);
		supertool.publishAddress(newPubDoc, true, false);

		System.out.println("\n----- refetch FULL PARENT, UNPUBLISHED !  -----");
		aMap = supertool.fetchAddress(pubParentUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- change organization and publish PARENT -> create pub version/\"delete\" work version -----");
		aMap.put(MdekKeys.ORGANISATION, "COPIED, Orga CHANGED and PUBLISHED: " + aMap.get(MdekKeys.ORGANISATION));	
		supertool.publishAddress(aMap, true, false);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		newPubDoc = supertool.publishAddress(newPubDoc, true, false);
		// uuid created !
		String pubChildUuid = newPubDoc.getString(MdekKeys.UUID);

		System.out.println("\n----- publish NEW TOP OBJECT as INTERNET referencing new address -----");
		System.out.println("----- first get initial top object -----");
		IngridDocument newObjDoc = new IngridDocument();
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT INTERNET");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		System.out.println("\n----- add published new address as point of contact -----");
		supertool.addPointOfContactAddress(newObjDoc, pubChildUuid);
		newObjDoc.put(MdekKeys.PARENT_UUID, null);
		System.out.println("\n----- then publish new INTERNET top object referencing new INTERNET address (obj.INTERNET -> addr.INTERNET) -----");
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		String newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- Publish referenced address as AMTSINTERN -> ERROR: REFERENCING_OBJECTS_HAVE_LARGER_PUBLICATION_CONDITION (obj.INTERNET -> addr.AMTSINTERN) ! -----");
		newPubDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		supertool.publishAddress(newPubDoc, true, false);

		System.out.println("\n----- publish new object referencing address as AMTSINTERN -----");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		newObjDoc = supertool.publishObject(newObjDoc, true, false);

		System.out.println("\n----- Now publish referenced address as AMTSINTERN possible (obj.AMTSINTERN -> addr.AMTSINTERN) ! -----");
		newPubDoc = supertool.publishAddress(newPubDoc, true, false);

		System.out.println("\n----- delete new object (FULL) -----");
		supertool.deleteObject(newObjUuid, true);
		System.out.println("\n----- delete parent published copy (WORKING COPY) -> NO DELETE -----");
		supertool.deleteAddressWorkingCopy(pubParentUuid, true);
		System.out.println("\n----- delete child published copy (FULL) -----");
		supertool.deleteAddress(pubChildUuid, true);
		System.out.println("\n----- delete parent published copy (FULL) -----");
		supertool.deleteAddress(pubParentUuid, true);

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("PUBLICATION CONDITION TEST");
		System.out.println("=========================");

		supertool.setFullOutput(false);

		System.out.println("\n\n===== TEST simple change of publication condition in hierarchy  =====");

		// institution parent with 1 child !
		parentUuid = "3866449C-B449-11D2-9A86-080000507261";
		String childUuid = "FAEFC65F-0B53-11D6-97D2-000476105676";

		System.out.println("\n----- fetch parent -----");
		IngridDocument oMapParent = supertool.fetchAddress(parentUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- sub addresses -----");
		supertool.fetchSubAddresses(parentUuid);

		System.out.println("\n----- fetch child -----");
		IngridDocument oMapChild = supertool.fetchAddress(childUuid, FetchQuantity.EDITOR_ENTITY);
		
		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> ERROR: SUBTREE_HAS_LARGER_PUBLICATION_CONDITION -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishAddress(oMapParent, false, false);

		System.out.println("\n----- change child to INTRANET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishAddress(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishAddress(oMapParent, true, false);

		System.out.println("\n----- change child to INTERNET -> ERROR: PARENT_HAS_SMALLER_PUBLICATION_CONDITION -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapChild, false, false);

		System.out.println("\n----- change parent to INTERNET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapParent, true, true);

		System.out.println("\n----- refetch child -> STILL INTRANET -----");
		oMapChild = supertool.fetchAddress(childUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- change child to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishAddress(oMapParent, true, true);

		System.out.println("\n----- refetch child -> NOW INTRANET -----");
		oMapChild = supertool.fetchAddress(childUuid, FetchQuantity.EDITOR_ENTITY);


		System.out.println("\n\n===== TEST change of publication condition VIA MOVE ! =====");
		
		System.out.println("\n----- verify INTERNET parent and children to MOVE -----");

		// NOTICE: UUid to move is TOP ADDRESS (institution) !
		String moveUuid = "38664482-B449-11D2-9A86-080000507261";
		String moveChild1Uuid = "38664591-B449-11D2-9A86-080000507261";
		String moveChild2Uuid = "38664592-B449-11D2-9A86-080000507261";
		supertool.fetchAddress(moveUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchSubAddresses(moveUuid);
		IngridDocument child1Doc = supertool.fetchAddress(moveChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchAddress(moveChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		
		System.out.println("\n----- Store 1. child to have WORKING VERSION where pub condition is NOT manipulated when moving (Internet) -----");
		supertool.storeAddress(child1Doc, true);

		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent -> ERROR: SUBTREE_HAS_LARGER_PUBLICATION_CONDITION -----");
		supertool.moveAddress(moveUuid, parentUuid, false, false);

		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent with FORCE -> SUCCESS -----");
		supertool.moveAddress(moveUuid, parentUuid, false, true);

		System.out.println("\n----- verify -> all moved nodes INTRANET ! ONLY IN PUBLISHED_VERSIONS !!! WORKING_VERSION child1 NOT CHANGED (still Internet) !!! -----");
		IngridDocument oMapMoved1 = supertool.fetchAddress(moveUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		IngridDocument oMapMoved2 = supertool.fetchAddress(moveChild1Uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		IngridDocument oMapMoved3 = supertool.fetchAddress(moveChild2Uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		
		System.out.println("\n===== Clean Up ! back to old state of DB ! =====");
		
		System.out.println("\n----- move node back to top -----");
		supertool.moveAddress(moveUuid, null, false, true);
		oMapMoved1.put(MdekKeys.PARENT_UUID, null);

		System.out.println("\n----- and change all moved nodes back to INTERNET -> SUCCESS -----");
		oMapMoved1.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapMoved1, true, true);
		oMapMoved2.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapMoved2, true, true);
		oMapMoved3.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapMoved3, true, true);

		System.out.println("\n----- change parent back to INTERNET -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapParent, true, true);

		System.out.println("\n----- change child back to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishAddress(oMapChild, true, true);

		supertool.setFullOutput(true);

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

		System.out.println("- change test ORGANISATION, GIVEN_NAME,NAME_FORM_KEY, TITLE_OR_FUNCTION_KEY ...");
		String origORGANISATION = aDocIn.getString(MdekKeys.ORGANISATION);
		String origGIVEN_NAME = aDocIn.getString(MdekKeys.GIVEN_NAME);
		aDocIn.put(MdekKeys.ORGANISATION, "TEST/" + origORGANISATION);
		aDocIn.put(MdekKeys.GIVEN_NAME, "TEST/" + origGIVEN_NAME);
		// check storing of value to key !
		Integer origNameFormKey = (Integer) aDocIn.get(MdekKeys.NAME_FORM_KEY);
		aDocIn.put(MdekKeys.NAME_FORM_KEY, 2);
		Integer origTitleKey = (Integer) aDocIn.get(MdekKeys.TITLE_OR_FUNCTION_KEY);
		aDocIn.put(MdekKeys.TITLE_OR_FUNCTION_KEY, 3);
		Integer origCountryKey = (Integer) aDocIn.get(MdekKeys.COUNTRY_CODE);
		aDocIn.put(MdekKeys.COUNTRY_CODE, 826);
		aDocIn.put(MdekKeys.HIDE_ADDRESS, "Y");

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

		// add entries to SUBJECT_TERMS
		docList = (List<IngridDocument>) aDocIn.get(MdekKeys.SUBJECT_TERMS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		System.out.println("- add test SUBJECT_TERM GEMET");
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.TERM_NAME, "TEST TERM_NAME");
		testDoc.put(MdekKeys.TERM_ALTERNATE_NAME, "TEST TERM_ALTERNATE_NAME");
		testDoc.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.GEMET.getDbValue());
		testDoc.put(MdekKeys.TERM_SNS_ID, "TEST TERM_SNS_ID");
		testDoc.put(MdekKeys.TERM_GEMET_ID, "TEST TERM_GEMET_ID");
		testDoc.put(MdekKeys.TERM_ENTRY_ID, 1);
		docList.add(testDoc);
		System.out.println("- add test SUBJECT_TERM FREE");
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.TERM_NAME, "TEST FREE TERM_NAME");
		testDoc.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
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
			IngridDocument aRefetchedDoc = supertool.fetchAddress(uuidStoredAddress, FetchQuantity.EDITOR_ENTITY);
			System.out.println("");

			System.out.println("MANIPULATE ADDRESS: back to origin");

			System.out.println("- set original ORGANISATION, GIVEN_NAME ...");
			aRefetchedDoc.put(MdekKeys.ORGANISATION, origORGANISATION);
			aRefetchedDoc.put(MdekKeys.GIVEN_NAME, origGIVEN_NAME);
			aRefetchedDoc.put(MdekKeys.NAME_FORM_KEY, origNameFormKey);
			aRefetchedDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, origTitleKey);
			aRefetchedDoc.put(MdekKeys.COUNTRY_CODE, origCountryKey);
			aRefetchedDoc.put(MdekKeys.HIDE_ADDRESS, "N");

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
