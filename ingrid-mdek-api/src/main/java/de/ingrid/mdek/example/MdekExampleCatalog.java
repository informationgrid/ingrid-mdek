package de.ingrid.mdek.example;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import de.ingrid.mdek.job.MdekException;
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
		IngridDocument result;
		int startIndex;
		int endIndex;
		
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
		String topObjUuid5 = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";

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

		System.out.println("\n----- SysList Values NO language -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, null);

		System.out.println("\n----- SysList Values language: " + catLang + " -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, catLang);

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
		System.out.println("EXPORT OBJECTS");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- fetch object EXPORT_ENTITY quantity -----");
		supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- get LAST Export Info -----");
		supertool.setFullOutput(false);
		supertool.getExportInfo(false);

		System.out.println("\n----- export objects ONLY TOP NODE -----");
		supertool.exportObjectBranch(topObjUuid, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportExistingTopObjUnzipped = "";
		try {
			exportExistingTopObjUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export objects FULL BRANCH UNDER PARENT -----");
		String exportExistingObjBranchUnzipped = "";
		try {
			// causes timeout
//			supertool.exportObjectBranch(topObjUuid, false);
//			result = supertool.getExportInfo(false);
//			supertool.getExportInfo(true);

			supertool.exportObjectBranch(objUuid, false);
			// extract XML result !
			supertool.setFullOutput(true);
			result = supertool.getExportInfo(true);
			supertool.setFullOutput(false);
			exportExistingObjBranchUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));

		} catch(MdekException ex) {
			// if timeout, track running job info (still exporting) !
			for (int i=1; i<=4; i++) {
				// extracted from running job info if still running
				supertool.getExportInfo(false);				
				// also outputs running job info
				if (!supertool.hasRunningJob()) {
					break;
				}
				supertool.sleep(2000);
			}
			// if still running, cancel it !
			if (supertool.hasRunningJob()) {
				supertool.cancelRunningJob();
				// sleep, so backend notices canceled job when updating running job info 
				supertool.sleep(2000);
			}
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export \"tagged\" objects -----");
		supertool.exportObjects("CDS");
		supertool.getExportInfo(true);

		supertool.exportObjects("CdS");
		supertool.getExportInfo(true);

		supertool.exportObjects("test");
		supertool.getExportInfo(true);

		supertool.exportObjects("TEST");
		supertool.getExportInfo(true);

		System.out.println("\n----- export objects ALL TOP NODES -----");
		supertool.exportObjectBranch(null, true);
		supertool.getExportInfo(true);
/*
		System.out.println("\n----- export objects ALL NODES -----");
		try {
			// causes timeout
			supertool.exportObjectBranch(null, false);
		} catch(Exception ex) {
			// if timeout, track running job info (still exporting) !
			// also outputs running job info
			while(supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getExportInfo(false);				
				supertool.sleep(1000);
			}
		}
		supertool.getExportInfo(true);
*/

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("EXPORT ADDRESSES");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- fetch address EXPORT_ENTITY quantity -----");
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EXPORT_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		supertool.setFullOutput(false);

		System.out.println("\n----- get LAST Export Info -----");
		supertool.getExportInfo(false);

		System.out.println("\n----- export addresses ONLY PARENT NODE -----");
		supertool.exportAddressBranch(parentAddrUuid, true, null);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses FULL BRANCH UNDER PARENT -----");
		supertool.exportAddressBranch(parentAddrUuid, false, null);
		supertool.getExportInfo(true);

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
		System.out.println("IMPORT");
		System.out.println("=========================");

		System.out.println("\n----- get LAST Import Info -----");
		supertool.getImportInfo();
		supertool.getRunningJobInfo();

		System.out.println("\n----- create new Import Top Node for Objects (NEVER PUBLISHED) -----");
		IngridDocument objImpTopDoc = supertool.newObjectDoc(null);
		objImpTopDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpTopDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpTopDoc = supertool.storeObject(objImpTopDoc, false);
		String objImpNodeUuid = (String) objImpTopDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		IngridDocument addrImpTopDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpTopDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpTopDoc = supertool.storeAddress(addrImpTopDoc, false);
		String addrImpNodeUuid = (String) addrImpTopDoc.get(MdekKeys.UUID);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: UPDATE EXISTING OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		// first change data to import

		// set some stuff to simulate different catalog etc. will be replaced with correct data !
		// different catalog
		exportExistingTopObjUnzipped = exportExistingTopObjUnzipped.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		exportExistingObjBranchUnzipped = exportExistingObjBranchUnzipped.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		// different mod user
		exportExistingTopObjUnzipped = exportExistingTopObjUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportExistingObjBranchUnzipped = exportExistingObjBranchUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		// different responsible user
		exportExistingTopObjUnzipped = exportExistingTopObjUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportExistingObjBranchUnzipped = exportExistingObjBranchUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		// TODO: what else ?

		// import data: single existing top node
		String importUnzipped = exportExistingTopObjUnzipped.replace("<title>", "<title>MMImport: ");
		byte[] importExistingTopObj = new byte[0];
		try {
			importExistingTopObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing sub nodes (branch)
		importUnzipped = exportExistingObjBranchUnzipped.replace("<title>", "<title>MMImport: ");
		byte[] importExistingObjBranch = new byte[0];
		try {
			importExistingObjBranch = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// invalid XML file to test logging of exception in job info
		// causes NumberFormatException
		importUnzipped = exportExistingTopObjUnzipped.replace("<object-class id=\"", "<object-class id=\"MM");
		byte[] importInvalidXML = new byte[0];
		try {
			importInvalidXML = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		
		System.out.println("\n----- do SEPARATE IMPORT underneath import nodes AND publish -> ERROR -----");
		supertool.importEntities(importExistingObjBranch, objImpNodeUuid, addrImpNodeUuid, true, true);


		System.out.println("\n\n----- import INVALID XML -> Exception logged in jobinfo ! -----");
		supertool.importEntities(importInvalidXML, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getImportInfo();



		System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! -----");
		supertool.setFullOutput(false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing TOP NODE as WORKING VERSION -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopObj, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getImportInfo();
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing TOP NODE as PUBLISHED -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopObj, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);


		
		System.out.println("\n\n----- EXISTING BRANCH ROOT BEFORE IMPORT !!! -----");
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n\n----- import existing branch as WORKING VERSION -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingObjBranch, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing branch as PUBLISHED -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingObjBranch, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: NEW OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		String newUuidTop = "UUID012345678901234567890123456789-3";
		String newUuid1 = "UUID012345678901234567890123456789-1";
		String newUuid2 = "UUID012345678901234567890123456789-2";

		// import data: single NEW top node
		importUnzipped = exportExistingTopObjUnzipped.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(topObjUuid, newUuidTop);
		byte[] importNewTopObj = new byte[0];
		try {
			importNewTopObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW object branch with non existing parent !
		// new uuids
		importUnzipped = exportExistingObjBranchUnzipped.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(objUuid, newUuid1);
		importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
		// non existing parent
		importUnzipped = importUnzipped.replace("15C69C20-FE15-11D2-AF34-0060084A4596", "MMMMMC20-FE15-11D2-AF34-0060084A4596");
/*
		// no parent in branch root
		startIndex = importUnzipped.indexOf("<parent-data-source>");
		endIndex = importUnzipped.indexOf("</parent-data-source>") + 21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
*/
//		System.out.println(importUnzipped);
		byte[] importNewObjBranchNonExistentParent = new byte[0];
		try {
			importNewObjBranchNonExistentParent = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: SAME NEW object branch with existing parent !
		// new uuids
		importUnzipped = exportExistingObjBranchUnzipped.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(objUuid, newUuid1);
		importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
		// existing parent
		String existentParentUuid = objLeafUuid;
		importUnzipped = importUnzipped.replace("15C69C20-FE15-11D2-AF34-0060084A4596", existentParentUuid);
		byte[] importNewObjBranchExistentParent = new byte[0];
		try {
			importNewObjBranchExistentParent = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		supertool.setFullOutput(false);

		System.out.println("\n----- import NEW TOP NODE as WORKING VERSION -> underneath import node -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.deleteObject(newUuidTop, true);

		System.out.println("\n----- import NEW TOP NODE as PUBLISHED -> underneath import node as working version -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuidTop, true);


		System.out.println("\n\n\n----- import NEW object branch with EXISTING parent as WORKING VERSION -> underneath EXISTING PARENT -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n\n----- import NEW object branch with EXISTING parent as PUBLISHED -> underneath EXISTING PARENT ! we keep this new branch ! -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
//		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n\n----- import SAME NEW object branch with NON EXISTING parent as WORKING VERSION -> KEEP OLD PARENT -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n----- import NEW object branch with NON EXISTING parent as PUBLISHED -> underneath import node as working version -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuid1, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		String origId1 = "ORIG_ID1";
		String origId2 = "ORIG_ID2";
		String newOrigId = "ORIG_ID_NEW";

		// import data: existing NEW top node with ORIG_ID1
		importUnzipped = exportExistingTopObjUnzipped.replace("<title>", "<title>1.MMImport ORIG_ID1: ");
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingTopObjOrigId1 = new byte[0];
		try {
			importExistingTopObjOrigId1 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing branch with ORIG_ID1 and ORIG_ID2
		importUnzipped = exportExistingObjBranchUnzipped.replace("<title>", "<title>2.MMImport ORIG_ID1/2: ");
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add ORIG_ID2
		startIndex = importUnzipped.indexOf("</object-identifier>", importUnzipped.indexOf("</data-source>"))+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId2 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
//		System.out.println(importUnzipped);
		byte[] importExistingObjBranchOrigIds1_2 = new byte[0];
		try {
			importExistingObjBranchOrigIds1_2 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW object branch (non existing UUIDs) with ORIG_ID1 and ORIG_ID2
		// new uuids
		importUnzipped = exportExistingObjBranchUnzipped.replace("<title>", "<title>3.MMImport ORIG_ID1/2: ");
		importUnzipped = importUnzipped.replace(objUuid, newUuid1);
		importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add ORIG_ID2
		startIndex = importUnzipped.indexOf("</object-identifier>", importUnzipped.indexOf("</data-source>"))+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId2 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importNewObjBranchOrigIds1_2 = new byte[0];
		try {
			importNewObjBranchOrigIds1_2 = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW arcgis object (no UUID) with EXISTING and NON EXISTING ORIG_ID
		importUnzipped = exportExistingTopObjUnzipped.replace("<title>", "<title>4.MMImport ORIG_ID1: ");
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// remove UUID
		startIndex = importUnzipped.indexOf("<object-identifier>");
		endIndex = importUnzipped.indexOf("</object-identifier>") + 20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// import data: NEW arcgis object with EXISTING ORIG_ID
		byte[] importArcGisExistingOrigId = new byte[0];
		try {
			importArcGisExistingOrigId = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}
		// import data: NEW arcgis object with EXISTING ORIG_ID
		importUnzipped = importUnzipped.replace(origId1, newOrigId);
		byte[] importArcGisNewOrigId = new byte[0];
		try {
			importArcGisNewOrigId = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import ORIG_ID1 into EXISTING top node WORKING VERSION -> NO update of ORIG_ID, keep null (check in DB) -----");
		supertool.importEntities(importExistingTopObjOrigId1, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, false);

		
		System.out.println("\n\n----- import ORIG_ID1 + ORIG_ID2 into EXISTING branch WORKING VERSION ->  NO update of ORIG_ID, keep null (check in DB) -----");
		supertool.importEntities(importExistingObjBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, false);
		doc = supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId2);
		supertool.storeObject(doc, false);


		System.out.println("\n\n----- import NEW branch with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> update FIRST found ORIG_ID1 and found ORIG_ID2 -----");
		System.out.println("----- !!! LOG WARNING: ORIG_ID1 not unique !!! -----");
		supertool.importEntities(importNewObjBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);


		System.out.println("\n\n----- import ARCGIS object (no uuid) with EXISTING ORIG_ID as WORKING VERSION -> update existing node -----");
		supertool.importEntities(importArcGisExistingOrigId, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import ARCGIS object (no uuid) with NEW ORIG_ID as PUBLISHED -> new node underneath import node -----");
		supertool.importEntities(importArcGisNewOrigId, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(topObjUuid, true);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MOVE OBJECT -----");
		System.out.println("-------------------------------------");

		// import data: move branch under top object !
		importUnzipped = exportExistingObjBranchUnzipped.replace("<object-identifier>15C69C20-FE15-11D2-AF34-0060084A4596</object-identifier>",
				"<object-identifier>" + topObjUuid5 + "</object-identifier>");
		byte[] importMoveObjBranch = new byte[0];
		try {
			importMoveObjBranch = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- old state before import and MOVE -----");
		System.out.println("\n----- FROM -----");
		supertool.fetchSubObjects(objParentUuid);
		System.out.println("\n----- TO -----");
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n\n----- import existing branch with DIFFERENT parent as WORKING VERSION -> move branch to new parent ! -----");
		supertool.importEntities(importMoveObjBranch, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: move back to original position etc.-----");
		supertool.moveObject(objUuid, objParentUuid, false);
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- import existing branch with DIFFERENT parent as PUBLISHED -> move branch to new parent ! -----");
		supertool.importEntities(importMoveObjBranch, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: move back to original position etc.-----");
		supertool.moveObject(objUuid, objParentUuid, false);


		System.out.println("\n\n----- Enforce Error when Moving, set PubCondition of new parent to INTRANET  -----");
		doc = supertool.fetchObject(topObjUuid5, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		doc = supertool.publishObject(doc, true, false);

		System.out.println("\n\n----- Import (PUBLISHED) causes Move causes Error (Intranet) -> branch keeps position, branch root stored as working version, subnodes PUBLISHED ! -----");
		supertool.importEntities(importMoveObjBranch, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: back to Internet etc.-----");
		doc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		doc = supertool.publishObject(doc, true, false);
		supertool.deleteObjectWorkingCopy(objUuid, true);

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
