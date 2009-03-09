package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

public class MdekExampleCatalog {

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
		MdekExampleCatalogThread[] threads = new MdekExampleCatalogThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleCatalogThread(i+1);
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
/*
		System.out.println("END OF EXAMPLE (end of main())");

		System.out.println(Thread.activeCount());
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		for (StackTraceElement[] st : allStackTraces.values()) {
			for (StackTraceElement stackTraceElement : st) {
		        System.out.println(stackTraceElement);
            }
            System.out.println("===============");
		}

//		System.exit(0);
//		return;
*/
	}
}

class MdekExampleCatalogThread extends Thread {

	private int threadNumber;
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleCatalogThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("mdek-iplug-idctest", "EXAMPLE_USER_" + threadNumber);
}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		boolean alwaysTrue = true;

		IngridDocument doc;
		
		// NI catalog

		// OBJECTS
		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";
		// underneath upper top node
		// 3866463B-B449-11D2-9A86-080000507261
		//  38664688-B449-11D2-9A86-080000507261
		//   15C69C20-FE15-11D2-AF34-0060084A4596
		//    2C997C68-2247-11D3-AF51-0060084A4596
		//     C1AA9CA6-772D-11D3-AF92-0060084A4596 // leaf
		String objParentUuid = "15C69C20-FE15-11D2-AF34-0060084A4596";
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		String objLeafUuid = "C1AA9CA6-772D-11D3-AF92-0060084A4596";
		// all further top nodes (5 top nodes at all)
//		String topObjUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
//		String topObjUuid3 = "38665183-B449-11D2-9A86-080000507261";
//		String topObjUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
		// NO SUB OBJECTS !

		String objWithAdditionalFieldsUuid = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";
		
		// ADDRESSES
		// TOP ADDRESS
		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		// PARENT ADDRESS (sub address of topUuid)
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		// PERSON ADDRESS (sub address of parentUuid)
		String personAddrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		// further non free top addresses (110 top nodes at all)
		String topAddrUuid2 = "386644BF-B449-11D2-9A86-080000507261";
//		String topAddrUuid3 = "4E9DD4F5-BC14-11D2-A63A-444553540000";

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n\n----- DISABLE WORKFLOW in catalog -----");
		doc = supertool.getCatalog();
		doc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		doc = supertool.storeCatalog(doc, true);

// ====================
// test single stuff
// -----------------------------------
/*
		// Test EH Cache on catalog, user ...
		// -----------------------

		supertool.setFullOutput(false);

		for (int i = 1; i <= 5; i++) {
			supertool.getCatalog();
		}
		for (int i = 1; i <= 5; i++) {
			supertool.getCatalogAdmin();
		}

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ===================================

		System.out.println("\n----- backend version -----");
		supertool.getVersion();

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("CATALOG");
		System.out.println("=========================");

		System.out.println("\n----- CATALOG data -----");
		IngridDocument catDoc = supertool.getCatalog();
		String catLang = catDoc.getString(MdekKeys.LANGUAGE);
		System.out.println("catalog language=" + catLang);

		System.out.println("\n----- change CATALOG data -----");
		System.out.println("- change Partner, Provider");
		String origPartner = catDoc.getString(MdekKeys.PARTNER_NAME);
		String origProvider = catDoc.getString(MdekKeys.PROVIDER_NAME);
		catDoc.put(MdekKeys.PARTNER_NAME, "testPARTNER");
		catDoc.put(MdekKeys.PROVIDER_NAME, "testPROVIDER");
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n----- back to orig data of CATALOG -----");
		System.out.println("- change Partner, Provider");
		catDoc.put(MdekKeys.PARTNER_NAME, origPartner);
		catDoc.put(MdekKeys.PROVIDER_NAME, origProvider);
		catDoc = supertool.storeCatalog(catDoc, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SYSLISTS");
		System.out.println("=========================");

		System.out.println("\n----- ALL SysList ids -----");
		supertool.getSysLists(null, null);

		System.out.println("\n----- SysList Values NO language -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, null);

		System.out.println("\n----- SysList Values language: " + catLang + " -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, catLang);

		System.out.println("\n\n----- new Syslist id=0815 and load -----");
		supertool.storeSysList(815, true, null,
			new Integer[]{null, null, null},
			new String[]{"name1_de", "name2_de", "name3_de"},
			new String[]{null, null, null});
		supertool.getSysLists(new Integer[] { 815 }, null);

		System.out.println("\n----- change Syslist id=0815 and load -----");
		supertool.storeSysList(815, false, 1,
			new Integer[]{1, 2, 3, null},
			new String[]{"NAME1_de", "NAME2_de", "NAME3_de", "NAME4_de"},
			new String[]{"name1_en", "name2_en", null, "name4_en"});
		supertool.getSysLists(new Integer[] { 815 }, null);

		System.out.println("\n----- change Syslist id=0815 (remove all) and load -----");
		supertool.storeSysList(815, false, null,
			new Integer[]{},
			new String[]{},
			new String[]{});
		supertool.getSysLists(new Integer[] { 815 }, null);


// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SYSGUIS");
		System.out.println("=========================");

		System.out.println("\n----- get ALL SYSGUI Elements -----");
		supertool.getSysGuis(null);

		System.out.println("\n----- store specific SYSGUI Element and refetch -----");
		IngridDocument[] sysGuis = new IngridDocument[2];
		sysGuis[0] = new IngridDocument();
		sysGuis[0].put(MdekKeys.SYS_GUI_ID, "TEST GUI_ID 1");
		sysGuis[0].put(MdekKeys.SYS_GUI_BEHAVIOUR, MdekUtils.SysGuiBehaviour.MANDATORY.getDbValue());
		sysGuis[1] = new IngridDocument();
		sysGuis[1].put(MdekKeys.SYS_GUI_ID,  "TEST GUI_ID 2");
		sysGuis[1].put(MdekKeys.SYS_GUI_BEHAVIOUR, MdekUtils.SysGuiBehaviour.REMOVED.getDbValue());
		supertool.storeSysGuis(Arrays.asList(sysGuis), true);

		System.out.println("\n----- get SPECIFIC SYSGUI Element -----");
		supertool.getSysGuis(new String[] { "TEST GUI_ID 2" });

		System.out.println("\n----- get ALL SYSGUI Elements -----");
		supertool.getSysGuis(null);

// -----------------------------------

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SYS GENERIC KEYS");
		System.out.println("=========================");

		System.out.println("\n----- get ALL SYS GENERIC KEYS -----");
		supertool.getSysGenericKeys(null);

		System.out.println("\n----- store specific SYS GENERIC KEYS and refetch -----");
		String[] keyNames = new String[]{"TEST KEY 1", "TEST KEY 2"};
		String[] keyValues = new String[]{"TEST VALUE 1", null};
		supertool.storeSysGenericKeys(keyNames, keyValues);

		System.out.println("\n----- get SPECIFIC SYS GENERIC KEY -----");
		supertool.getSysGenericKeys(new String[] { "TEST KEY 1" });

		System.out.println("\n----- get ALL SYS GENERIC KEYS -----");
		supertool.getSysGenericKeys(null);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SYS ADDITIONAL FIELDS (Definitions)");
		System.out.println("=========================");

		System.out.println("\n----- Specific SysAdditionalFields with language -----");
		supertool.getSysAdditionalFields(new Long[] { 167242L, 167243L }, catLang);

		System.out.println("\n----- Specific SysAdditionalFields NO language -----");
		supertool.getSysAdditionalFields(new Long[] { 167242L, 167243L }, null);

		System.out.println("\n----- ALL SysAdditionalFields Values NO language -----");
		supertool.getSysAdditionalFields(null, null);
		
// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("ANALYZE DB (Consistency Check)");
		System.out.println("=========================");

		supertool.analyze();

// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("DELETE ADDRESS (Gesamtkatalogmanagement) ");
		System.out.println("=========================");

		System.out.println("\n----- STORE new 'Address to replace' (will be AUSKUNFT, RESPONSIBLE USER) -----");
		doc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		doc.put(MdekKeys.ORGANISATION, "testADDRESS_TO_REPLACE");
		doc = supertool.storeAddress(doc, true);
		String uuidToReplace = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE new SUB-ADDRESS of 'Address to replace' -----");
		doc = supertool.newAddressDoc(uuidToReplace, AddressType.INSTITUTION);
		doc.put(MdekKeys.ORGANISATION, "testSUBADDRESS");
		doc = supertool.storeAddress(doc, true);
		String uuidSubaddress = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE 'ADDRESS to replace with'. NOTICE: 'Address to replace' is RESPONSIBLE USER -----");
		IngridDocument newDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		newDoc.put(MdekKeys.ORGANISATION, "testADDRESS_TO_REPLACE_WITH");
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.publishAddress(newDoc, true);
		String uuidToReplaceWith = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE 1. new Object where 'Address to replace' is AUSKUNFT, ANBIETER and RESPONSIBLE USER -----");
		newDoc = supertool.newObjectDoc(null);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT STORED (NOT Published)");
		List<IngridDocument> objAdrDocs = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.ADR_REFERENCES_TO, objAdrDocs);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, 1);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.storeObject(newDoc, true);
		String newObjStoredUuid = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- PUBLISH 2. new Object where 'Address to replace' is AUSKUNFT, ANBIETER and RESPONSIBLE USER -----");
		newDoc = supertool.newObjectDoc(null);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT PUBLISHED");
		objAdrDocs = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.ADR_REFERENCES_TO, objAdrDocs);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, 1);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.publishObject(newDoc, true, false);
		String newObjPublishedUuid = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- getObjectsOfAuskunftAddress -> 2 Objects -----");
		supertool.getObjectsOfAuskunftAddress(uuidToReplace, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.OBJECTS_OF_AUSKUNFT_ADDRESS, uuidToReplace);
		
		System.out.println("\n----- getObjectsOfResponsibleUser -> 2 Objects -----");
		supertool.getObjectsOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.OBJECTS_OF_RESPONSIBLE_USER, uuidToReplace);
		
		System.out.println("\n----- getAddressesOfResponsibleUser -> 1 Address -----");
		supertool.getAddressesOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.ADDRESSES_OF_RESPONSIBLE_USER, uuidToReplace);
		
		System.out.println("\n----- DELETE Address to replace -> Error: ADDRESS_IS_AUSKUNFT -----");
		supertool.deleteAddress(uuidToReplace, false);


		System.out.println("\n\n----- REPLACE CATADMIN address -> Error: ADDRESS_IS_IDCUSER_ADDRESS -----");
		supertool.replaceAddress(catalogAdminUuid, uuidToReplaceWith);
		System.out.println("\n----- REPLACE with equal Addresses -> Error: FROM_UUID_EQUALS_TO_UUID -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplace);
		System.out.println("\n----- REPLACE with UNPUBLISHED new Address -> Error: ENTITY_NOT_PUBLISHED -----");
		supertool.replaceAddress(uuidToReplaceWith, uuidToReplace);
		System.out.println("\n----- REPLACE address has subnodes -> Error: NODE_HAS_SUBNODES -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplaceWith);

		System.out.println("\n----- DELETE Subaddress of 'Address to replace', so replace works ! -----");
		supertool.deleteAddress(uuidSubaddress, false);


		System.out.println("\n\n----- REPLACE 'Address to replace' with 'Address to replace with' \n" +
				"          -> all Auskunfts are replaced, all References deleted, all responsible users set to catadmin -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplaceWith);

		System.out.println("\n----- getObjectsOfAuskunftAddress with OLD Auskunft-> 0 Objects -----");
		supertool.getObjectsOfAuskunftAddress(uuidToReplace, null);
		System.out.println("\n----- getObjectsOfAuskunftAddress with NEW Auskunft-> 2 Objects -----");
		supertool.getObjectsOfAuskunftAddress(uuidToReplaceWith, null);
		
		System.out.println("\n----- getObjectsOfResponsibleUser with OLD responsible User -> 0 Objects -----");
		supertool.getObjectsOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- getObjectsOfResponsibleUser with NEW responsible User (=catadmin) -> LOTs of objects -----");
		supertool.getObjectsOfResponsibleUser(catalogAdminUuid, 50);
		
		System.out.println("\n----- getAddressesOfResponsibleUser with OLD responsible User -> 0 Addresses -----");
		supertool.getAddressesOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- getAddressesOfResponsibleUser with NEW responsible User (=catadmin) -> LOTs of addresses -----");
		supertool.getAddressesOfResponsibleUser(catalogAdminUuid, 50);


		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteObject(newObjStoredUuid, true);
		supertool.deleteObject(newObjPublishedUuid, true);
		supertool.deleteAddress(uuidToReplaceWith, true);
		System.out.println("\n----- Auskunft may be already deleted -----");
		supertool.deleteAddress(uuidToReplace, true);


// ===================================

		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
