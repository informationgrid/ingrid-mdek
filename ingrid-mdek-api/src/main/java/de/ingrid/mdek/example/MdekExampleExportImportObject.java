package de.ingrid.mdek.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.utils.IngridDocument;

public class MdekExampleExportImportObject {

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
		MdekExampleExportImportObjectThread[] threads = new MdekExampleExportImportObjectThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleExportImportObjectThread(i+1);
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

class MdekExampleExportImportObjectThread extends Thread {

	private int threadNumber;
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleExportImportObjectThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
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
		// Test ...
		// -----------------------

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ===================================

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
		byte[] exportTopObjZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
		String exportTopObjUnzipped = "";
		try {
			exportTopObjUnzipped = MdekUtils.decompressZippedByteArray(exportTopObjZipped);
		} catch(IOException ex) {
			System.out.println(ex);
		}
		
		System.out.println("\n----- export object with additional field for testing import -----");
		supertool.exportObjectBranch(objWithAdditionalFieldsUuid, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		byte[] exportObjWithAdditionalFieldsZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
		String exportObjWithAdditionalFieldsUnzipped = "";
		try {
			exportObjWithAdditionalFieldsUnzipped = MdekUtils.decompressZippedByteArray(exportObjWithAdditionalFieldsZipped);
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export objects FULL BRANCH UNDER PARENT -----");
		byte[] exportObjBranchZipped = null;
		String exportObjBranchUnzipped = "";
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
			exportObjBranchZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
			exportObjBranchUnzipped = MdekUtils.decompressZippedByteArray(exportObjBranchZipped);

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

// ===================================

		System.out.println("\n\n=========================");
		System.out.println("IMPORT OBJECTS");
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
		System.out.println("----- Import: INVALID XML -----");
		System.out.println("-------------------------------------");

		// invalid XML file to test logging of exception in job info
		// causes NumberFormatException
		String importUnzipped = exportTopObjUnzipped.replace("<object-class id=\"", "<object-class id=\"MM");
		byte[] importInvalidXML = new byte[0];
		try {
			importInvalidXML = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- import INVALID XML -> Exception logged in jobinfo ! -----");
		supertool.importEntities(importInvalidXML, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: UPDATE EXISTING OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		// first change data to import

		// set some stuff to simulate different catalog etc. will be replaced with correct data !
		// different catalog
		exportTopObjUnzipped = exportTopObjUnzipped.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		exportObjBranchUnzipped = exportObjBranchUnzipped.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		// different mod user
		exportTopObjUnzipped = exportTopObjUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportObjBranchUnzipped = exportObjBranchUnzipped.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		// different responsible user
		exportTopObjUnzipped = exportTopObjUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportObjBranchUnzipped = exportObjBranchUnzipped.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		// TODO: what else ?

		// import data: single existing top node
		importUnzipped = exportTopObjUnzipped.replace("<title>", "<title>MMImport: ");
		// different languages (german to english)
		importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
		importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
		System.out.println("importExistingTopObj:\n" + importUnzipped);
		byte[] importExistingTopObj = new byte[0];
		try {
			importExistingTopObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing sub nodes (branch)
		importUnzipped = exportObjBranchUnzipped.replace("<title>", "<title>MMImport: ");
		// different languages (german to english)
		importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
		importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
		System.out.println("importExistingObjBranch:\n" + importUnzipped);
		byte[] importExistingObjBranch = new byte[0];
		try {
			importExistingObjBranch = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! -----");
		supertool.setFullOutput(false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing TOP NODE as WORKING VERSION -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopObj, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
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

		
		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: UPDATE EXISTING OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- do SEPARATE IMPORT AND PUBLISH -> ERROR -----");
		supertool.importEntities(importExistingObjBranch, objImpNodeUuid, addrImpNodeUuid, true, true);

		System.out.println("\n----- separate import existing TOP NODE -> underneath import node, NEW uuid -----");
		supertool.importEntities(importExistingTopObj, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- separate import existing branch -> underneath import node, NEW Uuid, KEEP STRUCTURE -----");
		supertool.importEntities(importExistingObjBranch, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: NEW OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		String newUuidTop = "UUID012345678901234567890123456789-3";
		String newUuid1 = "UUID012345678901234567890123456789-1";
		String newUuid2 = "UUID012345678901234567890123456789-2";

		// import data: single NEW top node
		importUnzipped = exportTopObjUnzipped.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(topObjUuid, newUuidTop);
		byte[] importNewTopObj = new byte[0];
		try {
			importNewTopObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW object branch with non existing parent !
		// new uuids
		importUnzipped = exportObjBranchUnzipped.replace("<title>", "<title>MMImport NEW: ");
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
		importUnzipped = exportObjBranchUnzipped.replace("<title>", "<title>MMImport NEW: ");
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
		System.out.println("\n----- NO PUBLISHED_VERSION -----");
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
		System.out.println("\n----- NO PUBLISHED_VERSION -----");
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		System.out.println("\n----- NO PUBLISHED_VERSION -----");
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: NEW OBJECTS (UUID) -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import NEW TOP NODE -> underneath import node, KEEP UUID -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.deleteObject(newUuidTop, true);

		System.out.println("\n----- separate import NEW object branch with EXISTING parent -> underneath import node, KEEP UUIDs -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchSubObjects(newUuid1);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n----- separate import NEW object branch with NON EXISTING parent -> underneath import node, KEEP UUIDs -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchSubObjects(newUuid1);
		supertool.deleteObject(newUuid1, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		String origId1 = "ORIG_ID1";
		String origId2 = "ORIG_ID2";
		String newOrigId = "ORIG_ID_NEW";

		// import data: existing top node with ORIG_ID1
		importUnzipped = exportTopObjUnzipped.replace("<title>", "<title>1.MMImport ORIG_ID1: ");
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
		importUnzipped = exportObjBranchUnzipped.replace("<title>", "<title>2.MMImport ORIG_ID1/2: ");
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
		importUnzipped = exportObjBranchUnzipped.replace("<title>", "<title>3.MMImport ORIG_ID1/2: ");
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
		importUnzipped = exportTopObjUnzipped.replace("<title>", "<title>4.MMImport ORIG_ID1: ");
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
		// import data: NEW arcgis object with NEW ORIG_ID
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


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import EXISTING top node with NEW ORIG_ID1 -> underneath import node, NEW UUID, KEEP ORIG_ID -----");
		supertool.importEntities(importExistingTopObjOrigId1, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


		System.out.println("\n\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, false);

		System.out.println("\n----- separate import EXISTING branch with ORIG_ID1 + ORIG_ID2 ->  underneath import node, NEW UUID, REMOVED ORIG_ID1, KEEP ORIG_ID2 -----");
		supertool.importEntities(importExistingObjBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


		System.out.println("\n\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, false);
		doc = supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId2);
		supertool.storeObject(doc, false);

		System.out.println("\n----- separate import NEW branch with ORIG_ID1 (multiple in catalog) and ORIG_ID2 (unique in catalog) -> underneath import node, NEW UUID, REMOVED ORIG_ID1, REMOVED ORIG_ID2 -----");
		System.out.println("----- !!! LOG WARNING: ORIG_ID1 not unique !!! -----");
		supertool.importEntities(importNewObjBranchOrigIds1_2, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


		System.out.println("\n\n----- separate import ARCGIS object (no uuid) with EXISTING ORIG_ID -> underneath import node, NEW UUID, REMOVED ORIG_ID -----");
		supertool.importEntities(importArcGisExistingOrigId, objImpNodeUuid, addrImpNodeUuid, false, true);

		System.out.println("\n----- import ARCGIS object (no uuid) with NEW ORIG_ID -> underneath import node, NEW UUID, KEEP ORIG_ID -----");
		supertool.importEntities(importArcGisNewOrigId, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(topObjUuid, true);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MOVE OBJECT -----");
		System.out.println("-------------------------------------");

		// import data: move branch under top object !
		importUnzipped = exportObjBranchUnzipped.replace("<object-identifier>15C69C20-FE15-11D2-AF34-0060084A4596</object-identifier>",
				"<object-identifier>" + topObjUuid5 + "</object-identifier>");
		byte[] importBranchMoveObj = new byte[0];
		try {
			importBranchMoveObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- state before import and MOVE -----");
		System.out.println("\n----- FROM -----");
		supertool.fetchSubObjects(objParentUuid);
		System.out.println("\n----- TO -----");
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n\n----- import existing branch with DIFFERENT parent as WORKING VERSION -> move branch to new parent ! -----");
		supertool.importEntities(importBranchMoveObj, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: move back to original position etc.-----");
		supertool.moveObject(objUuid, objParentUuid, false);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

		System.out.println("\n----- import existing branch with DIFFERENT parent as PUBLISHED -> move branch to new parent ! -----");
		supertool.importEntities(importBranchMoveObj, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: move back to original position etc.-----");
		supertool.moveObject(objUuid, objParentUuid, false);


		System.out.println("\n\n----- Enforce Error when Moving, set PubCondition of new parent to INTRANET  -----");
		doc = supertool.fetchObject(topObjUuid5, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		doc = supertool.publishObject(doc, true, false);

		System.out.println("\n\n----- Import branch as PUBLISHED causes Move causes Error (Intranet) -> branch keeps position, branch root stored as WORKING version, subnodes PUBLISHED ! -----");
		supertool.importEntities(importBranchMoveObj, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: back to Internet etc.-----");
		doc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		doc = supertool.publishObject(doc, true, false);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: REMOVE RELATIONS -----");
		System.out.println("-------------------------------------");

		// import data: non existing relations !
		importUnzipped = exportObjBranchUnzipped;
		// add wrong address relation
		startIndex = importUnzipped.indexOf("</related-address>") + 18;
		importUnzipped = importUnzipped.substring(0, startIndex) +
        	"\n<related-address>\n" +
        	"<type-of-relation entry-id=\"-1\">MM Relation</type-of-relation>\n" +
        	"<address-identifier>MMMMMMMMMMMMMMM</address-identifier>\n" +
        	"</related-address>\n" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add wrong object relation
		startIndex = importUnzipped.indexOf("</link-data-source>") + 19;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<link-data-source>\n" +
			"<object-link-type id=\"-1\">Detailinformation</object-link-type>" +
			"<object-identifier>MMMMMMMMMMMMMMMMMMMMM</object-identifier>\n" +
			"</link-data-source>\n" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistBranchWrongRelations = new byte[0];
		try {
			importExistBranchWrongRelations = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);
		}

		// import data: NEW object branch with Relation Parent(INTRANET) > Child(INTERNET) causing problems when publishing
		// (FROM is published, TO is not !
		// new uuids
		importUnzipped = exportObjBranchUnzipped;
		importUnzipped = importUnzipped.replace(objUuid, newUuid1);
		importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
		// existing parent
		existentParentUuid = objLeafUuid;
		importUnzipped = importUnzipped.replace("15C69C20-FE15-11D2-AF34-0060084A4596", existentParentUuid);
		// add object relation
		startIndex = importUnzipped.indexOf("</link-data-source>") + 19;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<link-data-source>\n" +
			"<object-link-type id=\"-1\">Detailinformation</object-link-type>" +
			"<object-identifier>" + newUuid2 + "</object-identifier>\n" +
			"</link-data-source>\n" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add wrong publication conditions ! Parent Intranet, child INTERNET !
		importUnzipped = importUnzipped.replace("<publication-condition>1", "<publication-condition>2");
		startIndex = importUnzipped.indexOf("</publication-condition>") + 24;
		String tmpStr = importUnzipped.substring(startIndex, importUnzipped.length());
		tmpStr = tmpStr.replace("<publication-condition>2", "<publication-condition>1");
		importUnzipped = importUnzipped.substring(0, startIndex) + tmpStr;
		byte[] importNewObjBranchRelationTargetIntranet = new byte[0];
		try {
			importNewObjBranchRelationTargetIntranet = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}


		System.out.println("\n----- state BEFORE import -----");
		supertool.setFullOutput(true);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);

		System.out.println("\n----- import EXISTING branch with WRONG RELATIONS as WORKING VERSION -> remove wrong relations -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n----- state AFTER import -----");
		supertool.setFullOutput(true);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);


		System.out.println("\n\n----- import EXISTING branch with WRONG RELATIONS as PUBLISHED -> remove wrong relations -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, true, false);

		System.out.println("\n----- state AFTER import -----");
		supertool.setFullOutput(true);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);


		System.out.println("\n\n----- import NEW branch with RELATION Parent(INTRANET) > Child(INTERNET) as WORKING VERSION -> relation OK, references working versions ! -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n----- import NEW branch with RELATION Parent(INTRANET) > Child(INTERNET) as PUBLISHED (causes error, Child is stored as WORKING VERSION)! -----");
		System.out.println("-----  -> relation REMOVED ! source published, target not published ! -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: REMOVE RELATIONS -----");
		System.out.println("-------------------------------------");

		// import data: EXISTING object branch with Relation Parent(INTRANET) > Child(INTERNET) 
		importUnzipped = exportObjBranchUnzipped;
		// add object relation
		startIndex = importUnzipped.indexOf("</link-data-source>") + 19;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<link-data-source>\n" +
			"<object-link-type id=\"-1\">Detailinformation</object-link-type>" +
			"<object-identifier>" + objLeafUuid + "</object-identifier>\n" +
			"</link-data-source>\n" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// add wrong publication conditions ! Parent Intranet, child INTERNET !
		importUnzipped = importUnzipped.replace("<publication-condition>1", "<publication-condition>2");
		startIndex = importUnzipped.indexOf("</publication-condition>") + 24;
		tmpStr = importUnzipped.substring(startIndex, importUnzipped.length());
		tmpStr = tmpStr.replace("<publication-condition>2", "<publication-condition>1");
		importUnzipped = importUnzipped.substring(0, startIndex) + tmpStr;
		byte[] importExistObjBranchRelationParentChild = new byte[0];
		try {
			importExistObjBranchRelationParentChild = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- separate import EXISTING branch with WRONG RELATIONS -> remove wrong relations -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


		System.out.println("\n\n----- separate import NEW branch with RELATION Parent(INTRANET) > Child(INTERNET)! -----");
		System.out.println("-----  -> KEEP relations \"to outside\", KEEP Parent > Child (always valid because WORKING VERSIONs) -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n----- separate import EXISTING branch with RELATION Parent > Child ! -----");
		System.out.println("-----  -> KEEP relations \"to outside\", MAPPED Parent > Child \"to inside\" (check in database) -----");
		supertool.importEntities(importExistObjBranchRelationParentChild, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

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
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import branch with WRONG RELATIONS as WORKING VERSION -> WORKING VERSION -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import branch with WRONG RELATIONS as PUBLISHED -> ASSIGNED TO QA -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- DISABLE WORKFLOW in catalog -----");
		doc = supertool.getCatalog();
		doc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		doc = supertool.storeCatalog(doc, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ADDITIONAL FIELDS -----");
		System.out.println("-------------------------------------");

		// import data: Object with VALID additional fields data 
		importUnzipped = exportObjWithAdditionalFieldsUnzipped;
		byte[] importExistObjWithAdditionalFieldsValid = new byte[0];
		try {
			importExistObjWithAdditionalFieldsValid = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: Object with INVALID additional fields, Wrong ID, Wrong NAME
		importUnzipped = exportObjWithAdditionalFieldsUnzipped;
		// change ID of TEXT FIELD
		importUnzipped = importUnzipped.replace("<general-additional-value id=\"167242\">", "<general-additional-value id=\"99999\">");
		// change NAME of LIST FIELD
		importUnzipped = importUnzipped.replace("<field-name>Test 2</field-name>", "<field-name>Test MMMM</field-name>");
		System.out.println(importUnzipped);
		byte[] importExistObjWithAdditionalFieldsInvalid_ID_Name = new byte[0];
		try {
			importExistObjWithAdditionalFieldsInvalid_ID_Name = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: Object with INVALID additional fields, Wrong ListEntry !
		importUnzipped = exportObjWithAdditionalFieldsUnzipped;
		// change Entry of LIST FIELD
		importUnzipped = importUnzipped.replace("<field-value>Eintrag 1</field-value>", "<field-value>Eintrag MMMMM</field-value>");
		System.out.println(importUnzipped);
		byte[] importExistObjWithAdditionalFieldsInvalid_ListEntry = new byte[0];
		try {
			importExistObjWithAdditionalFieldsInvalid_ListEntry = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import existing object with VALID additional fields as WORKING VERSION -> all ok -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsValid, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n\n----- import existing object with VALID additional fields as PUBLISHED -> all ok -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsValid, objImpNodeUuid, addrImpNodeUuid, true, false);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ID, NAME) as PUBLISHED -> removed and stored as WORKING VERSION -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_ID_Name, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.deleteObjectWorkingCopy(objWithAdditionalFieldsUuid, true);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ListEntry) as PUBLISHED -> removed and stored as WORKING VERSION -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_ListEntry, objImpNodeUuid, addrImpNodeUuid, true, false);
		supertool.deleteObjectWorkingCopy(objWithAdditionalFieldsUuid, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ADDITIONAL FIELDS -----");
		System.out.println("-------------------------------------");

		System.out.println("\n\n----- separate import existing object with VALID additional fields -> underneath import node, all ok -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsValid, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ID, NAME) -> underneath import node, removed 2 additional fields -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_ID_Name, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ListEntry) -> underneath import node, removed 1 additional field -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_ListEntry, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MANDATORY DATA -----");
		System.out.println("-------------------------------------");

		// import data: branch with root object missing all MANDATORY FIELDS ! 
		importUnzipped = exportObjBranchUnzipped;		
		// remove CLASS
		startIndex = importUnzipped.indexOf("<object-class");
		endIndex = importUnzipped.indexOf("/>", startIndex) + 2;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// remove TITLE
		startIndex = importUnzipped.indexOf("<title>");
		endIndex = importUnzipped.indexOf("</title>") + 8;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// add empty ABSTRACT
		startIndex = importUnzipped.indexOf("<abstract>") + 10;
		endIndex = importUnzipped.indexOf("</abstract>");
		importUnzipped = importUnzipped.substring(0, startIndex) + "     	\n" + 
			importUnzipped.substring(endIndex, importUnzipped.length());
		// remove RESPONSIBLE_USER -> will be added again !
		startIndex = importUnzipped.indexOf("<responsible-identifier>");
		endIndex = importUnzipped.indexOf("</responsible-identifier>") + 25;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// add WRONG PUBLICATION_CONDITION
		startIndex = importUnzipped.indexOf("<publication-condition>") + 23;
		endIndex = importUnzipped.indexOf("</publication-condition>");
		importUnzipped = importUnzipped.substring(0, startIndex) + "12" + 
			importUnzipped.substring(endIndex, importUnzipped.length());
		// invalidate 1. AUSKUNFT ADDRESS
		startIndex = importUnzipped.indexOf("<type-of-relation list-id=\"505\"") + 27;
		endIndex = startIndex + 3;
		importUnzipped = importUnzipped.substring(0, startIndex) + "2010" +
			importUnzipped.substring(endIndex, importUnzipped.length());
		System.out.println(importUnzipped);
		byte[] importExistObjBranchMissingMandatoryFields = new byte[0];
		try {
			importExistObjBranchMissingMandatoryFields = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import branch with MISSING MANDATORY DATA as WORKING VERSION -> no error, WORKING VERSION -----");
		supertool.importEntities(importExistObjBranchMissingMandatoryFields, objImpNodeUuid, addrImpNodeUuid, false, false);

		System.out.println("\n\n----- import branch with MISSING MANDATORY DATA as PUBLISHED -> root misses data, is stored as WORKING VERSION -----");
		supertool.importEntities(importExistObjBranchMissingMandatoryFields, objImpNodeUuid, addrImpNodeUuid, true, false);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("IMPORT MULTIPLE FILES !!!!");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- export addresses FULL BRANCH UNDER PARENT -----");
		supertool.exportAddressBranch(parentAddrUuid, false, null);
		result = supertool.getExportInfo(true);
		byte[] exportAddressBranchZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n----- import multiple files with frontend protocol -----");
		List<byte[]> importList = new ArrayList<byte[]>();
		importList.add(exportObjWithAdditionalFieldsZipped);
		importList.add(exportAddressBranchZipped);
		importList.add(exportObjBranchZipped);
		importList.add(exportTopObjZipped);
		String frontendProtocol = "exportObjWithAdditionalFieldsZipped\n\n" +
			"exportAddressBranchZipped\n\n" +
			"exportObjBranchZipped\n\n" +
			"exportTopObjZipped\n\n";
		try {
			supertool.importEntities(importList, objImpNodeUuid, addrImpNodeUuid, false, false, frontendProtocol);			
		} catch(Exception ex) {
			// if timeout, track running job info (still importing) !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.IMPORT);				
				supertool.sleep(2000);
			}
		}

		supertool.getJobInfo(JobType.IMPORT);

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
