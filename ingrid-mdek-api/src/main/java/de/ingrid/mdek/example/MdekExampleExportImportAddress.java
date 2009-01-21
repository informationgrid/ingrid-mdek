package de.ingrid.mdek.example;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.utils.IngridDocument;

public class MdekExampleExportImportAddress {

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
		MdekExampleExportImportAddressThread[] threads = new MdekExampleExportImportAddressThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleExportImportAddressThread(i+1);
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

class MdekExampleExportImportAddressThread extends Thread {

	private int threadNumber;
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleExportImportAddressThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("mdek-iplug-idctest", "EXAMPLE_USER_" + threadNumber);
}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		boolean alwaysTrue = true;

		IngridDocument doc;
		IngridDocument result;
		int startIndex;
		int endIndex;
		
		// NI catalog

		// OBJECTS
		// 3866463B-B449-11D2-9A86-080000507261
		//  38664688-B449-11D2-9A86-080000507261
		//   15C69C20-FE15-11D2-AF34-0060084A4596
		//    2C997C68-2247-11D3-AF51-0060084A4596
		//     C1AA9CA6-772D-11D3-AF92-0060084A4596 // leaf
		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";
		String objParentUuid = "15C69C20-FE15-11D2-AF34-0060084A4596";
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		String objLeafUuid = "C1AA9CA6-772D-11D3-AF92-0060084A4596";
		// all further top nodes (5 top nodes at all)
//		String topObjUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
//		String topObjUuid3 = "38665183-B449-11D2-9A86-080000507261";
//		String topObjUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
		// NO SUB OBJECTS !
		String topObjUuid5 = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";

		String objWithAdditionalFieldsUuid = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";
		
		// ADDRESSES
		
		// NORMAL ONES
		// 3761E246-69E7-11D3-BB32-1C7607C10000
		//  C5FEA801-6AB2-11D3-BB32-1C7607C10000
		//   012CBA17-87F6-11D4-89C7-C1AAE1E96727 // PERSON
		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		String personAddrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		// further non free top addresses (110 top nodes at all)
//		String topAddrUuid2 = "386644BF-B449-11D2-9A86-080000507261";
//		String topAddrUuid3 = "4E9DD4F5-BC14-11D2-A63A-444553540000";

		// FREE ONES
		// 0C3E030A-5A66-4655-87F7-80E24998AA4C
		// 3F783516-40D7-11D3-AF70-0060084A4596
		// C40DE0BA-4342-11D3-AF71-0060084A4596
		// 4E9AC596-6F52-11D3-AF8A-0060084A4596
		String freeAddrUuid = "0C3E030A-5A66-4655-87F7-80E24998AA4C";

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
		// Test ...
		// -----------------------

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ===================================

		System.out.println("\n\n=========================");
		System.out.println("EXPORT ADDRESSES");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- fetch address EXPORT_ENTITY quantity -----");
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EXPORT_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		supertool.setFullOutput(false);

		System.out.println("\n----- get LAST Export Info -----");
		supertool.getExportInfo(false);

		System.out.println("\n----- export addresses TOP ADDRESS -----");
		supertool.exportAddressBranch(topAddrUuid, true, null);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportTopAddrUnzipped = "";
		try {
			exportTopAddrUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}
		
		System.out.println("\n----- export addresses FREE ADDRESS -----");
		supertool.exportAddressBranch(freeAddrUuid, true, null);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportFreeAddrUnzipped = "";
		try {
			exportFreeAddrUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export addresses ONLY PARENT NODE -----");
		supertool.exportAddressBranch(parentAddrUuid, true, null);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses FULL BRANCH UNDER PARENT -----");
		supertool.exportAddressBranch(parentAddrUuid, false, null);
		result = supertool.getExportInfo(true);
		String exportAddrBranchUnzipped = "";
		try {
			exportAddrBranchUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export addresses ALL TOP NON FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_NON_FREE_ADDRESSES);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses ALL FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_FREE_ADDRESSES);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses ALL TOP NON FREE ADDRESSES and FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_ADDRESSES);
		supertool.getExportInfo(true);
/*
		System.out.println("\n----- export addresses ALL NON FREE ADDRESSES (including subnodes) -----");
		supertool.exportAddressBranch(null, false, AddressArea.ALL_NON_FREE_ADDRESSES);
		supertool.getExportInfo(true);
*/
/*
		System.out.println("\n----- export addresses ALL ADDRESSES -----");
		supertool.exportAddressBranch(null, false, AddressArea.ALL_ADDRESSES);
		supertool.getExportInfo(true);
*/

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("IMPORT ADDRESSES");
		System.out.println("=========================");

		System.out.println("\n----- create new Import Top Node for Objects (NEVER PUBLISHED) -----");
		IngridDocument objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, false);
		String objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);
		// doc to be used afterwards for new creation of node !
		objImpNodeDoc.put(MdekKeys.UUID, objImpNodeUuid);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		IngridDocument addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, false);
		String addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);
		// doc to be used afterwards for new creation of node !
		addrImpNodeDoc.put(MdekKeys.UUID, addrImpNodeUuid);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("CLEAN UP");
		System.out.println("=========================");

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

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
