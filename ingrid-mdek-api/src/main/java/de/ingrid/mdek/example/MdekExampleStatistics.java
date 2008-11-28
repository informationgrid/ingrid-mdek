package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

public class MdekExampleStatistics {

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
		MdekExampleStatisticsThread[] threads = new MdekExampleStatisticsThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleStatisticsThread(i+1);
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

class MdekExampleStatisticsThread extends Thread {

	private int threadNumber;
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleStatisticsThread(int threadNumber)
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
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		String objLeafUuid = "C1AA9CA6-772D-11D3-AF92-0060084A4596";
		// all further top nodes (5 top nodes at all)
		String topObjUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
//		String topObjUuid3 = "38665183-B449-11D2-9A86-080000507261";
//		String topObjUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
//		String topObjUuid5 = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";

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

// ====================
// test single stuff
// -----------------------------------
/*
		// TEST TREE PATH on New Node, Move and Copy (database check via phpMyAdmin)
		// -----------------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("OBJECTS");
		
		System.out.println("\n----- new top object publish directly -----");
		doc = supertool.newObjectDoc(null);
		doc = supertool.publishObject(doc, true, true);
		String newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteObject(newUuid, true);
		System.out.println("\n----- new top object store working version -----");
		doc = supertool.newObjectDoc(null);
		doc = supertool.storeObject(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteObject(newUuid, true);

		System.out.println("\n----- new branch object publish directly -----");
		doc = supertool.newObjectDoc(objUuid);
		doc = supertool.publishObject(doc, true, true);
		newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteObject(newUuid, true);
		System.out.println("\n----- new branch object store working version -----");
		doc = supertool.newObjectDoc(objUuid);
		doc = supertool.storeObject(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		
		System.out.println("\n----- copy parent of new object to top (sub to top) -----");
		doc = supertool.copyObject(objUuid, null, true);
		String copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchSubObjects(copiedUuid);
		System.out.println("\n----- copy copied top to branch (top to sub) -----");
		doc = supertool.copyObject(copiedUuid, topObjUuid, true);
		String copiedUuid2 = doc.getString(MdekKeys.UUID);
		supertool.fetchSubObjects(copiedUuid2);
		System.out.println("\n----- clean up -----");
		supertool.deleteObject(copiedUuid, true);
		supertool.deleteObject(copiedUuid2, true);
		System.out.println("\n----- copy parent of new object to branch (sub to sub) -----");
		doc = supertool.copyObject(objUuid, topObjUuid, true);
		copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchSubObjects(copiedUuid);

		System.out.println("\n----- move copied object to top (sub to top) -----");
		doc = supertool.moveObject(copiedUuid, null, true);
		System.out.println("\n----- move back to branch (top to sub) -----");
		doc = supertool.moveObject(copiedUuid, topObjUuid, true);
		System.out.println("\n----- move to leaf (sub to sub) -----");
		doc = supertool.moveObject(copiedUuid, objLeafUuid, true);

		System.out.println("\n----- clean up -----");
		supertool.deleteObject(newUuid, true);
		supertool.deleteObject(copiedUuid, true);

		System.out.println("\n\n=========================");
		System.out.println("ADDRESSES");
		
		System.out.println("\n----- new top address publish directly -----");
		doc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		doc = supertool.publishAddress(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteAddress(newUuid, true);
		System.out.println("\n----- new top address store working version -----");
		doc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		doc = supertool.storeAddress(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteAddress(newUuid, true);

		System.out.println("\n----- new branch address publish directly -----");
		doc = supertool.newAddressDoc(topAddrUuid, AddressType.INSTITUTION);
		doc = supertool.publishAddress(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		supertool.deleteAddress(newUuid, true);
		System.out.println("\n----- new branch address store working version -----");
		doc = supertool.newAddressDoc(topAddrUuid, AddressType.INSTITUTION);
		doc = supertool.storeAddress(doc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("\n----- new sub branch address store working version -----");
		doc = supertool.newAddressDoc(newUuid, AddressType.INSTITUTION);
		supertool.storeAddress(doc, true);
		
		System.out.println("\n----- copy parent of new address to top (sub to top) -----");
		doc = supertool.copyAddress(newUuid, null, true, false);
		copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchSubAddresses(copiedUuid);
		System.out.println("\n----- copy copied top to branch (top to sub) -----");
		doc = supertool.copyAddress(copiedUuid, topAddrUuid, true, false);
		copiedUuid2 = doc.getString(MdekKeys.UUID);
		supertool.fetchSubAddresses(copiedUuid2);
		System.out.println("\n----- clean up -----");
		supertool.deleteAddress(copiedUuid, true);
		supertool.deleteAddress(copiedUuid2, true);
		System.out.println("\n----- copy parent of new address to branch (sub to sub) -----");
		doc = supertool.copyAddress(newUuid, topAddrUuid2, true, false);
		copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchSubAddresses(copiedUuid);

		System.out.println("\n----- move copied address to top (sub to top) -----");
		doc = supertool.moveAddress(copiedUuid, null, false);
		System.out.println("\n----- move back to branch (top to sub) -----");
		doc = supertool.moveAddress(copiedUuid, topAddrUuid2, false);
		System.out.println("\n----- move to other branch (sub to sub) -----");
		doc = supertool.moveAddress(copiedUuid, topAddrUuid, false);

		System.out.println("\n----- clean up -----");
		supertool.deleteAddress(newUuid, true);
		supertool.deleteAddress(copiedUuid, true);

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
		System.out.println("OBJECT STATISTICS");
		System.out.println("=========================");

//		supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
//		supertool.fetchSubObjects(objUuid);
		
		System.out.println("\n-------------------------------------");
		System.out.println("----- CLASSES AND WORK STATES -----");

		System.out.println("\n----- whole catalog -----");
		doc = supertool.getObjectStatistics(null, 
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getObjectStatistics(topObjUuid, 
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);
		System.out.println("\n----- leaf -----");
		doc = supertool.getObjectStatistics(objLeafUuid, 
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEARCHTERMS FREE -----");

		// plain sql for evaluating results !
		// SELECT * FROM t01_object o, searchterm_obj so, searchterm_value sv WHERE o.id=so.obj_id and so.searchterm_id=sv.id

		System.out.println("\n----- whole catalog -----");
		doc = supertool.getObjectStatistics(null, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);
		doc = supertool.getObjectStatistics(null, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				10, 10);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getObjectStatistics(topObjUuid, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);
		System.out.println("\n----- leaf -----");
		doc = supertool.getObjectStatistics(objLeafUuid, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEARCHTERMS THESAURUS -----");

		System.out.println("\n----- whole catalog -----");
		doc = supertool.getObjectStatistics(null, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);
		doc = supertool.getObjectStatistics(null, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				10, 10);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getObjectStatistics(topObjUuid, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);
		System.out.println("\n----- leaf -----");
		doc = supertool.getObjectStatistics(objLeafUuid, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);


		System.out.println("\n\n=========================");
		System.out.println("ADDRESS STATISTICS");
		System.out.println("=========================");

		System.out.println("\n----- CLASSES AND WORK STATES -----");
		System.out.println("\n----- whole catalog -----");
		doc = supertool.getAddressStatistics(null, false,
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);
		System.out.println("\n----- all free addresses -----");
		doc = supertool.getAddressStatistics(null, true,
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getAddressStatistics(topAddrUuid, false, 
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);
		System.out.println("\n----- leaf -----");
		doc = supertool.getAddressStatistics(personAddrUuid, false, 
				IdcStatisticsSelectionType.CLASSES_AND_STATES,
				-1, -1);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEARCHTERMS FREE -----");

		// plain sql for evaluating results !
		// SELECT * FROM t02_address a, searchterm_adr sa, searchterm_value sv WHERE a.id=sa.adr_id and sa.searchterm_id=sv.id

		System.out.println("\n----- whole catalog -----");
		doc = supertool.getAddressStatistics(null, false,
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);
		doc = supertool.getAddressStatistics(null, false,
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				10, 10);
		System.out.println("\n----- all free addresses -----");
		doc = supertool.getAddressStatistics(null, true,
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getAddressStatistics(topAddrUuid, false, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);
		System.out.println("\n----- leaf -----");
		doc = supertool.getAddressStatistics(personAddrUuid, false, 
				IdcStatisticsSelectionType.SEARCHTERMS_FREE,
				0, 10);

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEARCHTERMS THESAURUS -----");

		System.out.println("\n----- whole catalog -----");
		doc = supertool.getAddressStatistics(null, false,
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);
		doc = supertool.getAddressStatistics(null, false,
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				10, 10);
		System.out.println("\n----- all free addresses -----");
		doc = supertool.getAddressStatistics(null, true,
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);
		System.out.println("\n----- tree branch -----");
		doc = supertool.getAddressStatistics(topAddrUuid, false, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);
		System.out.println("\n----- leaf -----");
		doc = supertool.getAddressStatistics(personAddrUuid, false, 
				IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS,
				0, 10);

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
