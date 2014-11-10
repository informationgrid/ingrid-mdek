/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.IJob.JobType;
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
		
		IngridDocument objImpNodeDoc;
		String objImpNodeUuid;
		IngridDocument addrImpNodeDoc;
		String addrImpNodeUuid;

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
		// Test EXPORT / IMPORT new Exchange Format 3.3.0
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- address details -----");
		IngridDocument aDoc = supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EXPORT_ENTITY);
		
		System.out.println("\n----- publish existing address (only published are exported) ! -----");
//		System.out.println("\n----- change and publish existing address (only published are exported) ! -----");

		// Add here changes in new format to verify
//		aDoc.put(MdekKeys.HIDE_ADDRESS, "Y");

		supertool.publishAddress(aDoc, true);

		System.out.println("\n----- export address -----");
		supertool.exportAddressBranch(child1PersonAddrUuid, true, null);
		result = supertool.getExportInfo(true);
		byte[] exportZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n----- create new Import Top Node for Objects (NEVER PUBLISHED) -----");
		objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- import address as WORKING VERSION -----");
		// OVERWRITE !!!
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- publish again (NO working version) -----");
//		System.out.println("\n----- discard changes -> remove former change and publish -----");
//		aDoc.put(MdekKeys.HIDE_ADDRESS, "N");

		supertool.publishAddress(aDoc, true);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 3.2.0
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- address details -----");
		IngridDocument aDoc = supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EXPORT_ENTITY);
		
		System.out.println("\n----- change and publish existing address (only published are exported) ! -----");

		// add entry to OBJECT USE, now key/value from new syslist
		aDoc.put(MdekKeys.HIDE_ADDRESS, "Y");

		supertool.publishAddress(aDoc, true);

		System.out.println("\n----- export address -----");
		supertool.exportAddressBranch(child1PersonAddrUuid, true, null);
		result = supertool.getExportInfo(true);
		byte[] exportZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n----- create new Import Top Node for Objects (NEVER PUBLISHED) -----");
		objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- import address as WORKING VERSION -----");
		// OVERWRITE !!!
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change and publish -----");
		aDoc.put(MdekKeys.HIDE_ADDRESS, "N");

		supertool.publishAddress(aDoc, true);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

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

		System.out.println("\n----- first fetch address and store a working version to test export of different instances ! -----");
		doc = supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.storeAddress(doc, false);
		
		System.out.println("\n----- export ONLY TOP NODE, NO export of working version -----\n");
		supertool.exportAddressBranch(topAddrUuid, true, null, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportTopAddrUnzipped_onlyPublished = "";
		try {
			exportTopAddrUnzipped_onlyPublished = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export ONLY TOP NODE, WITH export of working version -----\n");
		supertool.exportAddressBranch(topAddrUuid, true, null, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportTopAddrUnzipped_workAndPublished = "";
		try {
			exportTopAddrUnzipped_workAndPublished = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- Clean Up ! delete TOP NODE (WORKING COPY) -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, false);

		System.out.println("\n\n-----------------------------------------");
		System.out.println("----- CHECK EXPORT TOP NODE / BRANCH WITH MISSING VERSIONS ! -----");

		// PARENT ADDRESS (sub address of topUuid)
		String parentUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		// PERSON ADDRESS (sub address of parentUuid)
		String personUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";

		System.out.println("\n\n----- create NEW NODE only working version ! -----");

		System.out.println("\n----- load initial data (from " + personUuid + ") -----");
		// initial data from person address (to test take over of COMMUNICATION)
		IngridDocument newAddrDoc = new IngridDocument();
		// supply parent uuid !
		newAddrDoc.put(MdekKeys.PARENT_UUID, personUuid);
		newAddrDoc = supertool.getInitialAddress(newAddrDoc);

		System.out.println("\n----- extend initial address and store -----");
		// extend initial address with own data !
		newAddrDoc.put(MdekKeys.NAME, "testNAME");
		newAddrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		// new parent
		System.out.println("- store under parent: " + parentUuid);
		newAddrDoc.put(MdekKeys.PARENT_UUID, parentUuid);
		newAddrDoc = supertool.storeAddress(newAddrDoc, true);
		// uuid created !
		String newAddrUuid = (String) newAddrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- export NEW NODE, PUBLISHED version -> Error ENTITY_NOT_FOUND -----");
		System.out.println("----- EXCEPTION ONLY THROWN IF STARTING(!) ADDRESS VERSION NOT FOUND, NOT FOR SUBADDRESSES (subaddresses are just skipped) -----\n");
		supertool.exportAddressBranch(newAddrUuid, true, null, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- export NEW NODE, WITH export of working version, OK, export has just working version -----\n");
		supertool.exportAddressBranch(newAddrUuid, true, null, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- publish NEW ADDRESS to test export of published parent, unpublished 1. child, published 2. child -----");
		supertool.publishAddress(newAddrDoc, true, false);

		System.out.println("\n----- create NEW 1. SUB NODE only working version ! -----");
		newAddrDoc = new IngridDocument();
		newAddrDoc.put(MdekKeys.ORGANISATION, "TEST 1. NEUES SUB OBJEKT");
		newAddrDoc.put(MdekKeys.NAME, "subobject1 testNAME");
		newAddrDoc.put(MdekKeys.GIVEN_NAME, "subobject1 testGIVEN_NAME");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		newAddrDoc.put(MdekKeys.PARENT_UUID, newAddrUuid);
		newAddrDoc = supertool.storeAddress(newAddrDoc, true);

		System.out.println("\n----- create NEW 2. SUB NODE published AND working version ! -----");
		newAddrDoc = new IngridDocument();
		newAddrDoc.put(MdekKeys.ORGANISATION, "TEST 2. NEUES SUB OBJEKT");
		newAddrDoc.put(MdekKeys.NAME, "subobject2 testNAME");
		newAddrDoc.put(MdekKeys.GIVEN_NAME, "subobject2 testGIVEN_NAME");
		newAddrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		newAddrDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		newAddrDoc.put(MdekKeys.PARENT_UUID, newAddrUuid);
		// email has to exist !
		List<IngridDocument> docList = (List<IngridDocument>) newAddrDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newAddrDoc.put(MdekKeys.COMMUNICATION, docList);
		newAddrDoc = supertool.publishAddress(newAddrDoc, true, false);

		System.out.println("\n----- store 2. child again to have working version ! -----\n");
		supertool.storeAddress(newAddrDoc, true);

		System.out.println("\n----- export NEW NODE branch, only PUBLISHED version -> OK, 2 nodes, first child skipped, working version of 2. child skipped ! -----");
		System.out.println("----- NO EXCEPTION IF CHILD HAS NOT REQUESTED VERSION, CHILD JUST SKIPPED -----\n");
		supertool.exportAddressBranch(newAddrUuid, false, null, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- export NEW NODE branch, include WORKING version -> OK, 3 nodes + 2. child working version also included ! -----\n");
		supertool.exportAddressBranch(newAddrUuid, false, null, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n===== Clean Up ! back to old state of DB ! =====");

		System.out.println("\n----- delete NEW ADDRESS (FULL) -----");
		supertool.deleteAddress(newAddrUuid, false);


		System.out.println("\n\n-----------------------------------------");
		System.out.println("----- FURTHER EXPORTS -----");

		System.out.println("\n----- export addresses FREE ADDRESS -----");

		System.out.println("\n----- first fetch address and store a working version to test export of different instances ! -----");
		doc = supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.storeAddress(doc, true);
		
		System.out.println("\n----- export FREE ADDRESS, NO export of working version -----\n");
		supertool.exportAddressBranch(freeAddrUuid, true, null, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportFreeAddrUnzipped_onlyPublished = "";
		try {
			exportFreeAddrUnzipped_onlyPublished = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export FREE ADDRESS, WITH export of working version -----\n");
		supertool.exportAddressBranch(freeAddrUuid, true, null, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportFreeAddrUnzipped_workAndPublished = "";
		try {
			exportFreeAddrUnzipped_workAndPublished = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}
		
		
		System.out.println("\n----- export addresses ONLY PARENT NODE -----");
		supertool.exportAddressBranch(parentAddrUuid, true, null, true);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses FULL BRANCH UNDER PARENT -----");
		supertool.exportAddressBranch(parentAddrUuid, false, null, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		String exportBranchUnzipped_onlyPublished = "";
		try {
			exportBranchUnzipped_onlyPublished = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- export addresses ALL TOP NON FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_NON_FREE_ADDRESSES, true);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses ALL FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_FREE_ADDRESSES, true);
		supertool.getExportInfo(true);

		System.out.println("\n----- export addresses ALL TOP NON FREE ADDRESSES and FREE ADDRESSES -----");
		supertool.exportAddressBranch(null, true, AddressArea.ALL_ADDRESSES, true);
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
		objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "EXAMPLE Import Objektknoten");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NEVER PUBLISHED) -----");
		addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "EXAMPLE Import Adressknoten");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: CANCEL IMPORT ! -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- export addresses ALL ADDRESSES -----");
		try {
			// can cause timeout
			supertool.exportAddressBranch(null, false, AddressArea.ALL_ADDRESSES, true);

		} catch(Throwable t) {
			// if timeout, track running job info (still exporting) !
			for (int i=0; i<2; i++) {
				// extracted from running job info IN MEMORY if still running
				supertool.getJobInfo(JobType.EXPORT);
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
		}

		result = supertool.getExportInfo(true);
		byte[] importAllAddresses = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n----- import addresses ALL ADDRESSES -> for every address frontend message \"schon vorhanden, wird ignoriert\" -----");
		try {
			// causes timeout
			supertool.importEntities(importAllAddresses, objImpNodeUuid, addrImpNodeUuid, false, true, true);

		} catch(Throwable t) {
			// if timeout, track running job info (still importing) !
			for (int i=0; i<2; i++) {
				// extracted from running job info IN MEMORY if still running
				supertool.getJobInfo(JobType.IMPORT);
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
		}

		System.out.println("\n----- last Import Info from DATABASE because no running job ! -----");
		supertool.getJobInfo(JobType.IMPORT);				

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: WRONG IMPORT FORMAT !!! -----");
		System.out.println("-------------------------------------");

		String importUnzipped = exportTopAddrUnzipped_onlyPublished.replace("exchange-format=\"", "exchange-format=\"0.");
		byte[] importWrongFormat = new byte[0];
		try {
			importWrongFormat = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- import Wrong Format -> ERROR -----");
		supertool.importEntities(importWrongFormat, objImpNodeUuid, addrImpNodeUuid, false, false, false);


// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: UPDATE EXISTING ADDRESSES (UUID) -----");
		System.out.println("----- !!! NOT POSSIBLE ANYMORE !!! -----");
		System.out.println("----- !!! for every address frontend message \"schon vorhanden, wird ignoriert\" !!! -----");
		System.out.println("-------------------------------------");

		// first change data to import

		// set some stuff to simulate different catalog etc. will be replaced with correct data !
		// different mod user
		exportTopAddrUnzipped_onlyPublished = exportTopAddrUnzipped_onlyPublished.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportFreeAddrUnzipped_onlyPublished = exportFreeAddrUnzipped_onlyPublished.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		exportBranchUnzipped_onlyPublished = exportBranchUnzipped_onlyPublished.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		// different responsible user
		exportTopAddrUnzipped_onlyPublished = exportTopAddrUnzipped_onlyPublished.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportFreeAddrUnzipped_onlyPublished = exportFreeAddrUnzipped_onlyPublished.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		exportBranchUnzipped_onlyPublished = exportBranchUnzipped_onlyPublished.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		// TODO: what else ?

		// import data: single existing top node
		importUnzipped = exportTopAddrUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
		// different country (germany to austria)
		importUnzipped = importUnzipped.replace("<country id=\"276\">", "<country id=\"40\">");
		byte[] importExistingTopAddr_onlyPublished = new byte[0];
		try {
			importExistingTopAddr_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: single existing free node
		importUnzipped = exportFreeAddrUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
		// different country (germany to austria)
		importUnzipped = importUnzipped.replace("<country id=\"276\">", "<country id=\"40\">");
		byte[] importExistingFreeAddr_onlyPublished = new byte[0];
		try {
			importExistingFreeAddr_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing sub nodes (branch)
		importUnzipped = exportBranchUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
		// different country (germany to austria)
		importUnzipped = importUnzipped.replace("<country id=\"276\">", "<country id=\"40\">");
		byte[] importExistingAddrBranch_onlyPublished = new byte[0];
		try {
			importExistingAddrBranch_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! -----");
		supertool.setFullOutput(false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing TOP NODE, publishImmediately=FALSE -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing TOP NODE, publishImmediately=TRUE -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);


		System.out.println("\n\n----- EXISTING FREE NODE BEFORE IMPORT !!! -----");
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing FREE NODE, publishImmediately=FALSE -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingFreeAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing FREE NODE, publishImmediately=TRUE -> check correct moduser, responsibleuser -----");
		supertool.importEntities(importExistingFreeAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchAddress(freeAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		
		System.out.println("\n\n----- EXISTING BRANCH ROOT + SUB ENTITY BEFORE IMPORT !!! -----");
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n\n\n----- import existing branch, publishImmediately=FALSE -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingAddrBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- import existing branch, publishImmediately=TRUE -> check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingAddrBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);

		
		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: UPDATE EXISTING ADDRESSES (UUID) -----");
		System.out.println("----- !!! NOT POSSIBLE ANYMORE !!! -----");
		System.out.println("----- !!! for every address frontend message \"schon vorhanden, wird ignoriert\" !!! -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import existing TOP NODE -> underneath import node, NEW uuid -----");
		supertool.importEntities(importExistingTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- separate import existing FREE NODE -> underneath import node, NEW uuid, CLASS Person instead of FREE ! -----");
		supertool.importEntities(importExistingFreeAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- separate import existing branch -> underneath import node, NEW Uuid, KEEP STRUCTURE -----");
		supertool.importEntities(importExistingAddrBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: NEW ADDRESSES (UUID) -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
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
		importUnzipped = exportTopAddrUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace(topAddrUuid, newUuidTop);
		byte[] importNewTopAddr_onlyPublished = new byte[0];
		try {
			importNewTopAddr_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: single NEW free node
		importUnzipped = exportFreeAddrUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
		importUnzipped = importUnzipped.replace(freeAddrUuid, newUuidFree);
		byte[] importNewFreeAddr_onlyPublished = new byte[0];
		try {
			importNewFreeAddr_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW branch with non existing parent !
		// new uuids
		importUnzipped = exportBranchUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
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
		byte[] importNewBranchNonExistentParent_onlyPublished = new byte[0];
		try {
			importNewBranchNonExistentParent_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: SAME NEW branch with existing parent !
		// new uuids
		importUnzipped = exportBranchUnzipped_onlyPublished.replace("<organisation>", "<organisation>MMImport: ");
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
		byte[] importNewBranchExistentParent_onlyPublished = new byte[0];
		try {
			importNewBranchExistentParent_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		supertool.setFullOutput(false);

		System.out.println("\n\n----- TOP NON FREE NODES BEFORE IMPORT -----");
		supertool.fetchTopAddresses(false);

		System.out.println("\n----- import NEW TOP NODE -> ok, PUBLISHED ! -----");
		supertool.importEntities(importNewTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchTopAddresses(false);
		supertool.deleteAddress(newUuidTop, true);
		supertool.fetchTopAddresses(false);


		System.out.println("\n\n----- TOP FREE NODES BEFORE IMPORT -----");
		supertool.fetchTopAddresses(true);

		System.out.println("\n\n----- import NEW FREE NODE -> ok, PUBLISHED ! -----");
		supertool.importEntities(importNewFreeAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(newUuidFree, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchTopAddresses(true);
		supertool.deleteAddress(newUuidFree, true);


		System.out.println("\n\n----- import NEW branch with EXISTING parent -> underneath EXISTING PARENT, PUBLISHED ! -----");
		supertool.importEntities(importNewBranchExistentParent_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubAddresses(existentParentUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);


		System.out.println("\n\n----- import NEW branch with NON EXISTING parent -> underneath import node, KEEP UUID, working version ! -----");
		supertool.importEntities(importNewBranchNonExistentParent_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: NEW ADDRESSES (UUID) -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import NEW TOP NODE -> underneath import node, KEEP UUID, working version ! -----");
		supertool.importEntities(importNewTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidTop, true);

		System.out.println("\n----- separate import NEW FREE NODE -> underneath import node, TRANSFORMED TO PERSON !, KEEP UUID, working version ! -----");
		supertool.importEntities(importNewFreeAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.deleteAddress(newUuidFree, true);

		System.out.println("\n----- separate import NEW branch with EXISTING parent -> underneath import node, KEEP UUIDs, working version ! -----");
		supertool.importEntities(importNewBranchExistentParent_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n----- separate import NEW branch with NON EXISTING parent -> underneath import node, KEEP UUIDs, working version ! -----");
		supertool.importEntities(importNewBranchNonExistentParent_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubAddresses(addrImpNodeUuid);
		supertool.fetchSubAddresses(newUuidParent);
		supertool.deleteAddress(newUuidParent, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


// -----------------------------------

				System.out.println("\n\n-------------------------------------");
				System.out.println("----- Import: NEW ADDRESSES (UUID) -----");
				System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
				System.out.println("-------------------------------------");

				// import data: single NEW top node
				importUnzipped = exportTopAddrUnzipped_workAndPublished.replace("<organisation>", "<organisation>MMImport: ");
				importUnzipped = importUnzipped.replace(topAddrUuid, newUuidTop);
				byte[] importNewTopAddr_workAndPublished = new byte[0];
				try {
					importNewTopAddr_workAndPublished = MdekUtils.compressString(importUnzipped);						
				} catch (Exception ex) {
					System.out.println(ex);			
				}

				// import data: single NEW free node
				importUnzipped = exportFreeAddrUnzipped_workAndPublished.replace("<organisation>", "<organisation>MMImport: ");
				importUnzipped = importUnzipped.replace(freeAddrUuid, newUuidFree);
				byte[] importNewFreeAddr_workAndPublished = new byte[0];
				try {
					importNewFreeAddr_workAndPublished = MdekUtils.compressString(importUnzipped);						
				} catch (Exception ex) {
					System.out.println(ex);			
				}

				supertool.setFullOutput(false);

				System.out.println("\n\n----- TOP NON FREE NODES BEFORE IMPORT -----");
				supertool.fetchTopAddresses(false);

				System.out.println("\n----- import NEW TOP NODE -> saved PUBLISH + WORKING ! -----");
				supertool.importEntities(importNewTopAddr_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
				supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
				supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
				supertool.fetchTopAddresses(false);
				supertool.deleteAddress(newUuidTop, true);


				System.out.println("\n\n----- TOP FREE NODES BEFORE IMPORT -----");
				supertool.fetchTopAddresses(true);

				System.out.println("\n\n----- import NEW FREE NODE -> saved PUBLISH + WORKING ! -----");
				supertool.importEntities(importNewFreeAddr_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
				supertool.fetchAddress(newUuidFree, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
				supertool.fetchAddress(newUuidFree, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
				supertool.fetchTopAddresses(true);
				supertool.deleteAddress(newUuidFree, true);


				System.out.println("\n\n-------------------------------------");
				System.out.println("----- SEPARATE Import: NEW ADDRESSES (UUID) -----");
				System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
				System.out.println("-------------------------------------");

				System.out.println("\n----- separate import NEW TOP NODE -> underneath import node, KEEP UUID, PUBLISHED AND WORKING STORED AS WORKING VERSION ! -----");
				supertool.importEntities(importNewTopAddr_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
				supertool.fetchSubAddresses(addrImpNodeUuid);
				supertool.deleteAddress(newUuidTop, true);

				System.out.println("\n----- separate import NEW FREE NODE -> underneath import node, TRANSFORMED TO PERSON !, KEEP UUID, PUBLISHED AND WORKING STORED AS WORKING VERSION ! -----");
				supertool.importEntities(importNewFreeAddr_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
				supertool.fetchSubAddresses(addrImpNodeUuid);
				supertool.deleteAddress(newUuidFree, true);

				System.out.println("\n----- Clean Up ImportNode -----");
				supertool.deleteAddress(addrImpNodeUuid, true);
				supertool.storeAddress(addrImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: ORIG_IDS -----");
		System.out.println("----- !!! for every EXISTING address frontend message \"schon vorhanden, wird ignoriert\" !!! -----");
		System.out.println("-------------------------------------");

		String origId1 = "ORIG_ID1";
		String origId2 = "ORIG_ID2";
		String newOrigId = "ORIG_ID_NEW";

		// import data: existing TOP node with ORIG_ID1
		importUnzipped = exportTopAddrUnzipped_onlyPublished;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingTopAddrOrigId1_onlyPublished = new byte[0];
		try {
			importExistingTopAddrOrigId1_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing FREE node with ORIG_ID1
		importUnzipped = exportFreeAddrUnzipped_onlyPublished;
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</address-identifier>")+21;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-address-identifier>" + origId1 + "</original-address-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingFreeAddrOrigId1_onlyPublished = new byte[0];
		try {
			importExistingFreeAddrOrigId1_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing branch with ORIG_ID1 and ORIG_ID2
		importUnzipped = exportBranchUnzipped_onlyPublished;
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
		byte[] importExistingBranchOrigIds1_2_onlyPublished = new byte[0];
		try {
			importExistingBranchOrigIds1_2_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW branch (non existing UUIDs) with ORIG_ID1 and ORIG_ID2
		// new uuids
		importUnzipped = exportBranchUnzipped_onlyPublished;
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
		byte[] importNewBranchOrigIds1_2_onlyPublished = new byte[0];
		try {
			importNewBranchOrigIds1_2_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW arcgis address (no UUID) with EXISTING and NON EXISTING ORIG_ID
		importUnzipped = exportTopAddrUnzipped_onlyPublished;
		// remove work-state
		importUnzipped = importUnzipped.replace(" work-state=\"V\"", "");
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
		byte[] importArcGisExistingOrigId_onlyPublished = new byte[0];
		try {
			importArcGisExistingOrigId_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}
		// import data: NEW arcgis address with NEW ORIG_ID
		importUnzipped = importUnzipped.replace(origId1, newOrigId);
		byte[] importArcGisNewOrigId_onlyPublished = new byte[0];
		try {
			importArcGisNewOrigId_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import ORIG_ID1 into EXISTING top node ->  -> SKIPPED -----");
		supertool.importEntities(importExistingTopAddrOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);

		
		System.out.println("\n\n----- import ORIG_ID1 into EXISTING FREE node ->  -> SKIPPED -----");
		supertool.importEntities(importExistingFreeAddrOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		
		System.out.println("\n\n----- import ORIG_ID1 + ORIG_ID2 into EXISTING branch ->   -> SKIPPED -----");
		supertool.importEntities(importExistingBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchAddress(parentAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);
		doc = supertool.fetchAddress(child1PersonAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId2);
		supertool.storeAddress(doc, false);


		System.out.println("\n\n----- import NEW branch with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> Import addresses with existing ORIG_ID1, ORIG_ID2 ARE SKIPPED ! ->  -----");
		System.out.println("----- -> remaining children can't find their parent and are stored underneath import node -----");
		System.out.println("----- !!! IGE IPLUG LOG WARNING: ORIG_ID1 not unique !!! -----");
		supertool.importEntities(importNewBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(addrImpNodeUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import ARCGIS address (no uuid) with EXISTING ORIG_ID -> SKIPPED -----");
		supertool.importEntities(importArcGisExistingOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n----- import ARCGIS address (no uuid) with NEW ORIG_ID, publishImmediately=TRUE -> NEW UUID, KEEP ORIG_ID, TOP NODE, PUBLISHED ! -----");
		IngridDocument jobInfoDoc = supertool.importEntities(importArcGisNewOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		// HACK !!!! extract generated UUID of new top address from messages
		String[] tmpStrs = jobInfoDoc.getString(MdekKeys.JOBINFO_MESSAGES).split("create new ADDRESS UUID:");
		if (tmpStrs.length > 1) {
			String myUuid = tmpStrs[1].substring(0, 36);
			supertool.deleteAddress(myUuid, true);
		}

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteAddressWorkingCopy(topAddrUuid, true);
		supertool.deleteAddressWorkingCopy(freeAddrUuid, true);
		supertool.deleteAddressWorkingCopy(parentAddrUuid, true);
		supertool.deleteAddressWorkingCopy(child1PersonAddrUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ORIG_IDS -----");
		System.out.println("----- !!! for every EXISTING address frontend message \"schon vorhanden, wird ignoriert\" !!! -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import EXISTING top node with NEW ORIG_ID1 -> SKIPPED -----");
		supertool.importEntities(importExistingTopAddrOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n----- separate import EXISTING FREE node with NEW ORIG_ID1 -> SKIPPED -----");
		supertool.importEntities(importExistingFreeAddrOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchAddress(topAddrUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, origId1);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- separate import EXISTING branch with ORIG_ID1 + ORIG_ID2 ->  SKIPPED -----");
		supertool.importEntities(importExistingBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);

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

		System.out.println("\n\n----- Separate import NEW branch with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> Import addresses with existing ORIG_ID1, ORIG_ID2 ARE SKIPPED ! ->  -----");
		System.out.println("----- -> remaining children can't find their parent and are stored underneath import node -----");
		supertool.importEntities(importNewBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteAddress(addrImpNodeUuid, true);
		supertool.storeAddress(addrImpNodeDoc, false);


		System.out.println("\n\n----- separate import ARCGIS address (no uuid) with EXISTING ORIG_ID -> SKIPPED -----");
		supertool.importEntities(importArcGisExistingOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);

		System.out.println("\n----- separate import ARCGIS address (no uuid) with NEW ORIG_ID, publishImmediately=FALSE -> underneath import node, NEW UUID, KEEP ORIG_ID, working version -----");
		supertool.importEntities(importArcGisNewOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
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
		System.out.println("----- !!! NOT POSSIBLE ANYMORE !!! -----");
		System.out.println("----- !!! for every EXISTING address frontend message \"schon vorhanden, wird ignoriert\" !!! -----");
		System.out.println("-------------------------------------");

		// import data: move branch under top address !
		importUnzipped = exportBranchUnzipped_onlyPublished.replace("<address-identifier>3761E246-69E7-11D3-BB32-1C7607C10000</address-identifier>",
				"<address-identifier>" + topAddrUuid2 + "</address-identifier>");
		byte[] importBranchMoveBranchToTop = new byte[0];
		try {
			importBranchMoveBranchToTop = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: move branch under FREE address -> ERROR !
		importUnzipped = exportBranchUnzipped_onlyPublished.replace("<address-identifier>3761E246-69E7-11D3-BB32-1C7607C10000</address-identifier>",
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

		System.out.println("\n\n----- import existing branch with DIFFERENT parent, only PUBLISHED instances -> move branch to new parent ! -----");
		supertool.importEntities(importBranchMoveBranchToTop, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubAddresses(topAddrUuid);
		supertool.fetchSubAddresses(topAddrUuid2);

		System.out.println("\n----- Clean Up: move back to original position -----");
		supertool.moveAddress(parentAddrUuid, topAddrUuid, false, false);

		System.out.println("\n\n----- Import branch as PUBLISHED causes Move causes Error ADDRESS_TYPE_CONFLICT (to FREE ADDRESS) ->  branch keeps position, root stored as WORKING version, subnodes PUBLISHED !-----");
		supertool.importEntities(importBranchMoveBranchToFree, objImpNodeUuid, addrImpNodeUuid, true, false, false);
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

		System.out.println("\n---------------------------------------------");
		System.out.println("----- IMPORT as CATADMIN (always !) -> QA Permissions -> PUBLISHED ! -----");
		
		System.out.println("\n\n----- import NEW branch, only PUBLISHED instances -> PUBLISHED cause QA Permission -----");
		supertool.importEntities(importNewTopAddr_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteAddress(newUuidTop, true);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- DISABLE WORKFLOW in catalog -----");
		doc = supertool.getCatalog();
		doc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		doc = supertool.storeCatalog(doc, true);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MANDATORY DATA -----");
		System.out.println("-------------------------------------");

		// import data: NEW top address missing all MANDATORY FIELDS !
		importUnzipped = exportTopAddrUnzipped_onlyPublished;
		importUnzipped = importUnzipped.replace(topAddrUuid, newUuidTop);
		// remove CLASS
		startIndex = importUnzipped.indexOf("<type-of-address");
		endIndex = importUnzipped.indexOf("/>", startIndex) + 2;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// remove RESPONSIBLE_USER -> will be added again !
		startIndex = importUnzipped.indexOf("<responsible-identifier>");
		endIndex = importUnzipped.indexOf("</responsible-identifier>") + 25;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		byte[] importNewTopAddrMissingMandatoryFields_onlyPublished = new byte[0];
		try {
			importNewTopAddrMissingMandatoryFields_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import branch with MISSING MANDATORY DATA, only PUBLISHED instances -> node misses data, is stored as WORKING VERSION -----");
		supertool.importEntities(importNewTopAddrMissingMandatoryFields_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchAddress(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteAddress(newUuidTop, true);

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
