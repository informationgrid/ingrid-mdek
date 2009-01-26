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
//		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";
//		String objParentUuid = "15C69C20-FE15-11D2-AF34-0060084A4596";
//		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
//		String objLeafUuid = "C1AA9CA6-772D-11D3-AF92-0060084A4596";
		// all further top nodes (5 top nodes at all)
//		String topObjUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
//		String topObjUuid3 = "38665183-B449-11D2-9A86-080000507261";
//		String topObjUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
		// NO SUB OBJECTS !
//		String topObjUuid5 = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";

//		String objWithAdditionalFieldsUuid = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";
		
		// ADDRESSES
		
		// NORMAL ONES
		// 3761E246-69E7-11D3-BB32-1C7607C10000
		//   C5FEA801-6AB2-11D3-BB32-1C7607C10000
		//     012CBA17-87F6-11D4-89C7-C1AAE1E96727 // PERSON
		//     C5FEA804-6AB2-11D3-BB32-1C7607C10000
		//     C5FEA805-6AB2-11D3-BB32-1C7607C10000
		//     C5FEA807-6AB2-11D3-BB32-1C7607C10000
		//     C5FEA803-6AB2-11D3-BB32-1C7607C10000
		//     67172C62-6F38-11D3-BB32-1C7607C10000
		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		String child1PersonAddrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		String child2AddrUuid = "C5FEA804-6AB2-11D3-BB32-1C7607C10000";
		String child3AddrUuid = "C5FEA805-6AB2-11D3-BB32-1C7607C10000";
		String child4AddrUuid = "C5FEA807-6AB2-11D3-BB32-1C7607C10000";
		String child5AddrUuid = "C5FEA803-6AB2-11D3-BB32-1C7607C10000";
		String child6AddrUuid = "67172C62-6F38-11D3-BB32-1C7607C10000";
		// further non free top addresses (110 top nodes at all)
		String topAddrUuid2 = "386644BF-B449-11D2-9A86-080000507261";
//		String topAddrUuid3 = "4E9DD4F5-BC14-11D2-A63A-444553540000";

		// FREE ONES
		// 0C3E030A-5A66-4655-87F7-80E24998AA4C
		// 3F783516-40D7-11D3-AF70-0060084A4596
		// C40DE0BA-4342-11D3-AF71-0060084A4596
		// 4E9AC596-6F52-11D3-AF8A-0060084A4596
		String freeAddrUuid = "3F783516-40D7-11D3-AF70-0060084A4596";

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
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportBranchUnzipped = "";
		try {
			exportBranchUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
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
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		String objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		IngridDocument addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		String addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: UPDATE EXISTING ADDRESSES (UUID) -----");
		System.out.println("-------------------------------------");

		// first change data to import

		// set some stuff to simulate different catalog etc. will be replaced with correct data !
		// different mod user
		exportTopAddrUnzipped = exportTopAddrUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportFreeAddrUnzipped = exportFreeAddrUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportBranchUnzipped = exportBranchUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		// different responsible user
		exportTopAddrUnzipped = exportTopAddrUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportFreeAddrUnzipped = exportFreeAddrUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportBranchUnzipped = exportBranchUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		// TODO: what else ?

		// import data: single existing top node
		String importUnzipped = exportTopAddrUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		byte[] importExistingTopAddr = new byte[0];
		try {
			importExistingTopAddr = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: single existing free node
		importUnzipped = exportFreeAddrUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		byte[] importExistingFreeAddr = new byte[0];
		try {
			importExistingFreeAddr = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing sub nodes (branch)
		importUnzipped = exportBranchUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		byte[] importExistingAddrBranch = new byte[0];
		try {
			importExistingAddrBranch = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! -----");
		supertool.setFullOutput(false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing TOP NODE as WORKING VERSION -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopAddr, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing TOP NODE as PUBLISHED -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopAddr, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);


		System.out.println("\n\n----- EXISTING FREE NODE BEFORE IMPORT !!! -----");
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing FREE NODE as WORKING VERSION -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingFreeAddr, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing FREE NODE as PUBLISHED -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingFreeAddr, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		
		System.out.println("\n\n----- EXISTING BRANCH ROOT + SUB ENTITY BEFORE IMPORT !!! -----");
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n\n\n----- import existing branch as WORKING VERSION -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingAddrBranch, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing branch as PUBLISHED -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingAddrBranch, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		
		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: UPDATE EXISTING ADDRESSES (UUID) -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import existing TOP NODE -> underneath import node, NEW uuid -----");
		supertool.importEntities(importExistingTopAddr, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- separate import existing FREE NODE -> underneath import node, NEW uuid, CLASS Person instead of FREE ! -----");
		supertool.importEntities(importExistingFreeAddr, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- separate import existing branch -> underneath import node, NEW Uuid, KEEP STRUCTURE -----");
		supertool.importEntities(importExistingAddrBranch, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: NEW ADDRESSES (UUID) -----");
		System.out.println("-------------------------------------");

		String newUuidTop = "UUID01234567890123456789012345678TOP";
		String newUuidFree = "UUID0123456789012345678901234567FREE";
		String newUuidParent = "UUID01234567890123456789012345PARENT";
		String newUuidChild1 = "UUID01234567890123456789012345CHILD1";
		String newUuidChild2 = "UUID01234567890123456789012345CHILD2";
		String newUuidChild3 = "UUID01234567890123456789012345CHILD3";
		String newUuidChild4 = "UUID01234567890123456789012345CHILD4";
		String newUuidChild5 = "UUID01234567890123456789012345CHILD5";
		String newUuidChild6 = "UUID01234567890123456789012345CHILD6";

		// import data: single NEW top node
		importUnzipped = exportTopAddrUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace(topAddrUuid, newUuidTop);
		byte[] importNewTopAddr = new byte[0];
		try {
			importNewTopAddr = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: single NEW free node
		importUnzipped = exportFreeAddrUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace(freeAddrUuid, newUuidFree);
		byte[] importNewFreeAddr = new byte[0];
		try {
			importNewFreeAddr = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW branch with non existing parent !
		// new uuids
		importUnzipped = exportBranchUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace("<name>", "<name>MMImport: ");
		importUnzipped = importUnzipped.replace(parentAddrUuid, newUuidParent);
		importUnzipped = importUnzipped.replace(child1PersonAddrUuid, newUuidChild1);
		importUnzipped = importUnzipped.replace(child2AddrUuid, newUuidChild2);
		importUnzipped = importUnzipped.replace(child3AddrUuid, newUuidChild3);
		importUnzipped = importUnzipped.replace(child4AddrUuid, newUuidChild4);
		importUnzipped = importUnzipped.replace(child5AddrUuid, newUuidChild5);
		importUnzipped = importUnzipped.replace(child6AddrUuid, newUuidChild6);
		// non existing parent
		importUnzipped = importUnzipped.replace(topAddrUuid, "MMMMMC20-FE15-11D2-AF34-0060084A4596");
/*
		// no parent in branch root
		startIndex = importUnzipped.indexOf("<parent-data-source>");
		endIndex = importUnzipped.indexOf("</parent-data-source>") + 21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
*/
//		System.out.println(importUnzipped);
		byte[] importNewBranchNonExistentParent = new byte[0];
		try {
			importNewBranchNonExistentParent = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: SAME NEW branch with existing parent !
		// new uuids
		importUnzipped = exportBranchUnzipped.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace("<name>", "<name>MMImport: ");
		importUnzipped = importUnzipped.replace(parentAddrUuid, newUuidParent);
		importUnzipped = importUnzipped.replace(child1PersonAddrUuid, newUuidChild1);
		importUnzipped = importUnzipped.replace(child2AddrUuid, newUuidChild2);
		importUnzipped = importUnzipped.replace(child3AddrUuid, newUuidChild3);
		importUnzipped = importUnzipped.replace(child4AddrUuid, newUuidChild4);
		importUnzipped = importUnzipped.replace(child5AddrUuid, newUuidChild5);
		importUnzipped = importUnzipped.replace(child6AddrUuid, newUuidChild6);
		// existing parent
		String existentParentUuid = topAddrUuid2;
		importUnzipped = importUnzipped.replace(topAddrUuid, existentParentUuid);
		byte[] importNewBranchExistentParent = new byte[0];
		try {
			importNewBranchExistentParent = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		supertool.setFullOutput(false);

		System.out.println("\n----- import NEW TOP NODE as WORKING VERSION -> underneath import node -----");
		supertool.importEntities(importNewTopAddr, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidTop, true);

		System.out.println("\n----- import NEW TOP NODE as PUBLISHED -> underneath import node as working version -----");
		supertool.importEntities(importNewTopAddr, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteAddress(newUuidTop, true);


		System.out.println("\n\n----- import NEW FREE NODE as WORKING VERSION -> underneath import node, CLASS Person instead of FREE ! -----");
		supertool.importEntities(importNewFreeAddr, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidFree, true);

		System.out.println("\n----- import NEW FREE NODE as PUBLISHED -> underneath import node as working version, CLASS Person instead of FREE ! -----");
		supertool.importEntities(importNewFreeAddr, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchAddress(newUuidFree, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteAddress(newUuidFree, true);


		System.out.println("\n\n----- import NEW branch with EXISTING parent as WORKING VERSION -> underneath EXISTING PARENT -----");
		supertool.importEntities(importNewBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubAddresses(existentParentUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n\n----- import NEW branch with EXISTING parent as PUBLISHED -> underneath EXISTING PARENT ! we keep this new branch ! -----");
		supertool.importEntities(importNewBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(existentParentUuid);
		supertool.fetchSubAddresses(newUuidParent);
//		supertool.deleteAddress(newUuidParent, true);


		System.out.println("\n\n----- import SAME NEW branch with NON EXISTING parent as WORKING VERSION -> KEEP OLD PARENT -----");
		supertool.importEntities(importNewBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubAddresses(existentParentUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n----- import NEW branch with NON EXISTING parent as PUBLISHED -> underneath import node as working version -----");
		supertool.importEntities(importNewBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: NEW ADDRESSES (UUID) -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import NEW TOP NODE -> underneath import node, KEEP UUID -----");
		supertool.importEntities(importNewTopAddr, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidTop, true);

		System.out.println("\n----- separate import NEW FREE NODE -> underneath import node, KEEP UUID -----");
		supertool.importEntities(importNewFreeAddr, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidFree, true);

		System.out.println("\n----- separate import NEW branch with EXISTING parent -> underneath import node, KEEP UUIDs -----");
		supertool.importEntities(importNewBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n----- separate import NEW branch with NON EXISTING parent -> underneath import node, KEEP UUIDs -----");
		supertool.importEntities(importNewBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		String origId1 = "ORIG_ID1";
		String origId2 = "ORIG_ID2";
		String newOrigId = "ORIG_ID_NEW";

		// import data: existing TOP node with ORIG_ID1
		importUnzipped = exportTopAddrUnzipped;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingTopAddrOrigId1 = new byte[0];
		try {
			importExistingTopAddrOrigId1 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing FREE node with ORIG_ID1
		importUnzipped = exportFreeAddrUnzipped;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingFreeAddrOrigId1 = new byte[0];
		try {
			importExistingFreeAddrOrigId1 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing branch with ORIG_ID1 and ORIG_ID2
		importUnzipped = exportBranchUnzipped;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add ORIG_ID2
		startIndex = importUnzipped.indexOf("</address-identifier>", importUnzipped.indexOf("</address>"))+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId2 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
//		System.out.println(importUnzipped);
		byte[] importExistingBranchOrigIds1_2 = new byte[0];
		try {
			importExistingBranchOrigIds1_2 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW branch (non existing UUIDs) with ORIG_ID1 and ORIG_ID2
		// new uuids
		importUnzipped = exportBranchUnzipped;
		importUnzipped = importUnzipped.replace(parentAddrUuid, newUuidParent);
		importUnzipped = importUnzipped.replace(child1PersonAddrUuid, newUuidChild1);
		importUnzipped = importUnzipped.replace(child2AddrUuid, newUuidChild2);
		importUnzipped = importUnzipped.replace(child3AddrUuid, newUuidChild3);
		importUnzipped = importUnzipped.replace(child4AddrUuid, newUuidChild4);
		importUnzipped = importUnzipped.replace(child5AddrUuid, newUuidChild5);
		importUnzipped = importUnzipped.replace(child6AddrUuid, newUuidChild6);
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add ORIG_ID2
		startIndex = importUnzipped.indexOf("</address-identifier>", importUnzipped.indexOf("</address>"))+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId2 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importNewBranchOrigIds1_2 = new byte[0];
		try {
			importNewBranchOrigIds1_2 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW arcgis address (no UUID) with EXISTING and NON EXISTING ORIG_ID
		importUnzipped = exportTopAddrUnzipped;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// remove UUID
		startIndex = importUnzipped.indexOf("<address-identifier>");
		endIndex = importUnzipped.indexOf("</address-identifier>") + 21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// import data: NEW arcgis address with EXISTING ORIG_ID
		byte[] importArcGisExistingOrigId = new byte[0];
		try {
			importArcGisExistingOrigId = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}
		// import data: NEW arcgis address with NEW ORIG_ID
		importUnzipped = importUnzipped.replace(origId1, newOrigId);
		byte[] importArcGisNewOrigId = new byte[0];
		try {
			importArcGisNewOrigId = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import ORIG_ID1 into EXISTING top node WORKING VERSION -> NO update of ORIG_ID, keep null (check in DB) -----");
		supertool.importEntities(importExistingTopAddrOrigId1, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);

		
		System.out.println("\n\n----- import ORIG_ID1 into EXISTING FREE node WORKING VERSION -> NO update of ORIG_ID, keep null (check in DB) -----");
		supertool.importEntities(importExistingFreeAddrOrigId1, objImpNodeUuid, addrImpNodeUuid, false, false);

		
		System.out.println("\n\n----- import ORIG_ID1 + ORIG_ID2 into EXISTING branch WORKING VERSION ->  NO update of ORIG_ID, keep null (check in DB) -----");
		supertool.importEntities(importExistingBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);
		doc = supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId2);
		supertool.storeAddress(doc, false);


		System.out.println("\n\n----- import NEW branch with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> update FIRST found ORIG_ID1 (top address) and found ORIG_ID2 ->  -----");
		System.out.println("----- -> remaining children can't find their parent and are stored underneath import node -----");
		System.out.println("----- !!! LOG WARNING: ORIG_ID1 not unique !!! -----");
		supertool.importEntities(importNewBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import ARCGIS address (no uuid) with EXISTING ORIG_ID as WORKING VERSION -> update existing node -----");
		supertool.importEntities(importArcGisExistingOrigId, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import ARCGIS address (no uuid) with NEW ORIG_ID as PUBLISHED -> new node underneath import node -----");
		supertool.importEntities(importArcGisNewOrigId, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, true);
		supertool.deleteAddressWorkingCopy(freeAddrUuid, true);
		supertool.deleteAddressWorkingCopy(parentAddrUuid, true);
		supertool.deleteAddressWorkingCopy(child1PersonAddrUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import EXISTING top node with NEW ORIG_ID1 -> underneath import node, NEW UUID, KEEP ORIG_ID -----");
		supertool.importEntities(importExistingTopAddrOrigId1, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n----- separate import EXISTING FREE node with NEW ORIG_ID1 -> underneath import node, NEW UUID, KEEP ORIG_ID -----");
		supertool.importEntities(importExistingFreeAddrOrigId1, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- separate import EXISTING branch with ORIG_ID1 + ORIG_ID2 ->  underneath import node, NEW UUID, REMOVED ORIG_ID1, KEEP ORIG_ID2 -----");
		supertool.importEntities(importExistingBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);
		doc = supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId2);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- separate import NEW branch with ORIG_ID1 (multiple in catalog) and ORIG_ID2 (unique in catalog) -> underneath import node, NEW UUID, REMOVED ORIG_ID1, REMOVED ORIG_ID2 -----");
		supertool.importEntities(importNewBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n----- separate import ARCGIS address (no uuid) with EXISTING ORIG_ID -> underneath import node, NEW UUID, REMOVED ORIG_ID -----");
		supertool.importEntities(importArcGisExistingOrigId, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- import ARCGIS address (no uuid) with NEW ORIG_ID -> underneath import node, NEW UUID, KEEP ORIG_ID -----");
		supertool.importEntities(importArcGisNewOrigId, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, true);
		supertool.deleteAddressWorkingCopy(freeAddrUuid, true);
		supertool.deleteAddressWorkingCopy(parentAddrUuid, true);
		supertool.deleteAddressWorkingCopy(child1PersonAddrUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MOVE ADDRESS -----");
		System.out.println("-------------------------------------");

		// import data: move branch under top address !
		importUnzipped = exportBranchUnzipped.replace("<address-identifier>3761E246-69E7-11D3-BB32-1C7607C10000</address-identifier>",
				"<address-identifier>" + topAddrUuid2 + "</address-identifier>");
		byte[] importBranchMoveBranchToTop = new byte[0];
		try {
			importBranchMoveBranchToTop = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: move branch under FREE address -> ERROR !
		importUnzipped = exportBranchUnzipped.replace("<address-identifier>3761E246-69E7-11D3-BB32-1C7607C10000</address-identifier>",
				"<address-identifier>" + freeAddrUuid + "</address-identifier>");
		byte[] importBranchMoveBranchToFree = new byte[0];
		try {
			importBranchMoveBranchToFree = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- state before import and MOVE -----");
		System.out.println("\n----- FROM -----");
		supertool.fetchSubAddresses(topAddrUuid);
		System.out.println("\n----- TO -----");
		supertool.fetchSubAddresses(topAddrUuid2);

		System.out.println("\n\n----- import existing branch with DIFFERENT parent as WORKING VERSION -> move branch to new parent ! -----");
		supertool.importEntities(importBranchMoveBranchToTop, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubAddresses(topAddrUuid);
		supertool.fetchSubAddresses(topAddrUuid2);

		System.out.println("\n----- Clean Up: move back to original position -----");
		supertool.moveAddress(parentAddrUuid, topAddrUuid, false);

		System.out.println("\n----- import existing branch with DIFFERENT parent as PUBLISHED -> move branch to new parent ! -----");
		supertool.importEntities(importBranchMoveBranchToTop, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(topAddrUuid);
		supertool.fetchSubAddresses(topAddrUuid2);

		System.out.println("\n----- Clean Up: move back to original position -----");
		supertool.moveAddress(parentAddrUuid, topAddrUuid, false);


		System.out.println("\n\n----- Import branch as PUBLISHED causes Move causes Error (to FREE ADDRESS) ->  branch keeps position, root stored as WORKING version, subnodes PUBLISHED !-----");
		supertool.importEntities(importBranchMoveBranchToFree, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubAddresses(topAddrUuid);
		supertool.fetchSubAddresses(freeAddrUuid);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ASSIGN TO QA -----");
		System.out.println("-------------------------------------");

		System.out.println("\n---------------------------------------------");
		System.out.println("----- ENABLE WORKFLOW in catalog -----");
		doc = supertool.getCatalog();
		doc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		doc = supertool.storeCatalog(doc, true);

		System.out.println("\n----- state BEFORE import -----");
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import branch as WORKING VERSION -> WORKING VERSION -----");
		supertool.importEntities(importExistingAddrBranch, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import branch as PUBLISHED -> ASSIGNED TO QA -----");
		supertool.importEntities(importExistingAddrBranch, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteAddressWorkingCopy(parentAddrUuid, true);
		supertool.deleteAddressWorkingCopy(child1PersonAddrUuid, true);
		supertool.deleteAddressWorkingCopy(child2AddrUuid, true);
		supertool.deleteAddressWorkingCopy(child3AddrUuid, true);
		supertool.deleteAddressWorkingCopy(child4AddrUuid, true);
		supertool.deleteAddressWorkingCopy(child5AddrUuid, true);
		supertool.deleteAddressWorkingCopy(child6AddrUuid, true);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- DISABLE WORKFLOW in catalog -----");
		doc = supertool.getCatalog();
		doc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		doc = supertool.storeCatalog(doc, true);

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
