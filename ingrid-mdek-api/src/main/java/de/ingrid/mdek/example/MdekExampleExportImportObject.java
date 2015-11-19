/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekException;
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

		IngridDocument objImpNodeDoc;
		String objImpNodeUuid;
		IngridDocument addrImpNodeDoc;
		String addrImpNodeUuid;

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
        // Test EXPORT / IMPORT new Exchange Format 3.6.1
        // --------------------------
		// "Version des Dienstes" wird zu Syslist s. dev.informationgrid.eu/redmine/issues/47
		// useConstraints / useLimitation trennen ... s. dev.informationgrid.eu/redmine/issues/13

        supertool.setFullOutput(true);

        System.out.println("\n----- object details -----");
        IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

        System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

        // change TECHNICAL DOMAIN SERVICE
        // add version with key/value (now syslist), see https://dev.informationgrid.eu/redmine/issues/47
        IngridDocument technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
        technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
        oDoc.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, technicalDomain);
        // needed for publishing !
        technicalDomain.put(MdekKeys.SERVICE_TYPE_KEY, 2);
        technicalDomain.put(MdekKeys.COUPLING_TYPE, "tight");
        // add TECHNICAL DOMAIN SERVICE - versions
        List<IngridDocument> docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_VERSION_LIST);
        docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
        technicalDomain.put(MdekKeys.SERVICE_VERSION_LIST, docList);
        // check SERVICE_VERSION_KEY -> SERVICE_VERSION_VALUE is stored via syslist
        // NOTICE: "interacts" with SERVICE_TYPE_KEY
        IngridDocument testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.SERVICE_VERSION_KEY, 1);
        testDoc.put(MdekKeys.SERVICE_VERSION_VALUE, "TEST VERSION 1 IS OVERWRITTEN");
        testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.SERVICE_VERSION_KEY, 2);
        testDoc.put(MdekKeys.SERVICE_VERSION_VALUE, "TEST VERSION 2 IS OVERWRITTEN");

        // see https://dev.informationgrid.eu/redmine/issues/13 2.)

        // change useLimitation
        docList = (List) oDoc.get(MdekKeys.USE_LIST);
        docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
        oDoc.put(MdekKeys.USE_LIST, docList);
        // useLimitation now ALWAYS free value (-1) but does not matter, we set to 1 for testing
        testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.USE_TERMS_OF_USE_KEY, -1);
        testDoc.put(MdekKeys.USE_TERMS_OF_USE_VALUE, "TEST USE_TERMS_OF_USE_VALUE always free value now with KEY -1 !");
        testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.USE_TERMS_OF_USE_KEY, 1);
        testDoc.put(MdekKeys.USE_TERMS_OF_USE_VALUE, "TEST Syslist USE_TERMS_OF_USE_VALUE with KEY 1 !");

        // change useConstraints
        docList = (List) oDoc.get(MdekKeys.USE_CONSTRAINTS);
        docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
        oDoc.put(MdekKeys.USE_CONSTRAINTS, docList);
        // new useConstraints handled via Syslists !
        testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.USE_LICENSE_KEY, -1);
        testDoc.put(MdekKeys.USE_LICENSE_VALUE, "Free License Key -1");
        testDoc = new IngridDocument();
        docList.add(testDoc);
        testDoc.put(MdekKeys.USE_LICENSE_KEY, 1);
        testDoc.put(MdekKeys.USE_LICENSE_VALUE, "TEST License IS OVERWRITTEN with Syslist value");

        oDoc = supertool.publishObject(oDoc, true, false);
        
        System.out.println("\n----- export object -----");
        supertool.exportObjectBranch(objUuid, true, false);
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

        System.out.println("\n----- import object OVERWRITE ! -----");
        supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false, true);
        supertool.getJobInfo(JobType.IMPORT);
        supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

        System.out.println("\n----- discard changes -> remove former change -----");
        oDoc.remove(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
        // OBJECT USE wieder wie vorher !
        docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_LIST);
        docList.remove(docList.size()-1);
        docList.remove(docList.size()-1);
        // USE_CONSTRAINTS USE wieder wie vorher !
        docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_CONSTRAINTS);
        docList.remove(docList.size()-1);
        docList.remove(docList.size()-1);

        result = supertool.publishObject(oDoc, true, false);

        System.out.println("----- DELETE Import Top Nodes -----");
        supertool.deleteObject(objImpNodeUuid, true);
        supertool.deleteAddress(addrImpNodeUuid, true);

        if (alwaysTrue) {
            isRunning = false;
            return;
        }
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 3.3.2
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

		// change TECHNICAL DOMAIN SERVICE
		// set HAS_ATOM_DOWNLOAD, see REDMINE-230
		IngridDocument technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		oDoc.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, technicalDomain);
		technicalDomain.put(MdekKeys.HAS_ATOM_DOWNLOAD, "Y");
		// needed for publishing !
		technicalDomain.put(MdekKeys.SERVICE_TYPE_KEY, 2);
		technicalDomain.put(MdekKeys.COUPLING_TYPE, "tight");
		// add TECHNICAL DOMAIN SERVICE - operations
		List<IngridDocument> docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_OPERATION_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		technicalDomain.put(MdekKeys.SERVICE_OPERATION_LIST, docList);
		// check SERVICE_OPERATION_NAME_KEY -> SERVICE_OPERATION_NAME is stored via syslist
		// NOTICE: "interacts" with SERVICE_TYPE_KEY
		IngridDocument testDoc = new IngridDocument();
		docList.add(testDoc);
		testDoc.put(MdekKeys.SERVICE_OPERATION_NAME_KEY, 1);
		testDoc.put(MdekKeys.SERVICE_OPERATION_DESCRIPTION, "TEST SERVICE_OPERATION_DESCRIPTION");
		testDoc.put(MdekKeys.INVOCATION_NAME, "TEST INVOCATION_NAME");
		// add TECHNICAL DOMAIN SERVICE - connectPoints
		List<String> strList = new ArrayList<String>();
		testDoc.put(MdekKeys.CONNECT_POINT_LIST, strList);
		strList.add("TEST CONNECT_POINT1");
		strList.add("TEST CONNECT_POINT2");
		// add TECHNICAL DOMAIN SERVICE - operation platforms
		docList = new ArrayList<IngridDocument>();
		testDoc.put(MdekKeys.PLATFORM_LIST, docList);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PLATFORM_KEY, 1);
		testDoc.put(MdekKeys.PLATFORM_VALUE, "TEST PLATFORM1");
		docList.add(testDoc);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PLATFORM_KEY, 2);
		testDoc.put(MdekKeys.PLATFORM_VALUE, "TEST PLATFORM2");
		docList.add(testDoc);

		oDoc = supertool.publishObject(oDoc, true, false);
		
		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true, false);
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

		System.out.println("\n----- import object OVERWRITE ! -----");
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false, true);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change -----");
		oDoc.remove(MdekKeys.TECHNICAL_DOMAIN_SERVICE);

		result = supertool.publishObject(oDoc, true, false);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 3.3.1
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

		// set IS_OPEN_DATA, see , see REDMINE-128
		String origIsOpenData = oDoc.getString(MdekKeys.IS_OPEN_DATA);
		oDoc.put(MdekKeys.IS_OPEN_DATA, "Y");

		// add entry to OBJECT OPEN_DATA_CATEGORY_LIST, see , see REDMINE-128
		List<IngridDocument> docList = (List<IngridDocument>) oDoc.get(MdekKeys.OPEN_DATA_CATEGORY_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		// check OPEN_DATA_CATEGORY_KEY -> OPEN_DATA_CATEGORY_VALUE is stored via syslist
		testDoc.put(MdekKeys.OPEN_DATA_CATEGORY_KEY, 2);
		docList.add(testDoc);
		oDoc.put(MdekKeys.OPEN_DATA_CATEGORY_LIST, docList);

		// add entry to LINKAGES, added LINKAGE_DATATYPE, see REDMINE-118
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LINKAGES);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check LINKAGE_REFERENCE_ID -> LINKAGE_REFERENCE is stored via syslist
		// check LINKAGE_DATATYPE_KEY -> LINKAGE_DATATYPE is stored via syslist
		testDoc.put(MdekKeys.LINKAGE_URL, "http://LINKAGE_URL");
		testDoc.put(MdekKeys.LINKAGE_REFERENCE_ID, 3100);
//		testDoc.put(MdekKeys.LINKAGE_REFERENCE, "Methode / Datengrundlage");
		testDoc.put(MdekKeys.LINKAGE_DESCRIPTION, "LINKAGE_DESCRIPTION");
		testDoc.put(MdekKeys.LINKAGE_NAME, "LINKAGE_NAME");
		testDoc.put(MdekKeys.LINKAGE_URL_TYPE, 1);
		testDoc.put(MdekKeys.LINKAGE_DATATYPE_KEY, 1);
//		testDoc.put(MdekKeys.LINKAGE_DATATYPE, "WinWord");
		docList.add(testDoc);
		oDoc.put(MdekKeys.LINKAGES, docList);

		oDoc = supertool.publishObject(oDoc, true, false);
		
		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true, false);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change -----");
		oDoc.put(MdekKeys.IS_OPEN_DATA, origIsOpenData);
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.OPEN_DATA_CATEGORY_LIST);
		docList.remove(docList.size()-1);
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LINKAGES);
		docList.remove(docList.size()-1);

		result = supertool.publishObject(oDoc, true, false);

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

		System.out.println("\n----- object details -----");
		IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);
		
		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

		// add entry to OBJECT USE, now key/value from new syslist
		List<IngridDocument> docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		// check USE_TERMS_OF_USE_KEY -> USE_TERMS_OF_USE_VALUE is stored via syslist
		testDoc.put(MdekKeys.USE_TERMS_OF_USE_KEY, 1);
		docList.add(testDoc);
		oDoc.put(MdekKeys.USE_LIST, docList);

		// add entry to OBJECT CONFORMITY, specification now key/value from new syslist
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.CONFORMITY_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check CONFORMITY_SPECIFICATION_KEY -> CONFORMITY_SPECIFICATION_VALUE is stored via syslist
		testDoc.put(MdekKeys.CONFORMITY_SPECIFICATION_KEY, 12);
		// check CONFORMITY_DEGREE_KEY -> CONFORMITY_DEGREE_VALUE is stored via syslist
		testDoc.put(MdekKeys.CONFORMITY_DEGREE_KEY, 1);
		docList.add(testDoc);
		oDoc.put(MdekKeys.CONFORMITY_LIST, docList);

		// Schlüsselkatalog/Objektartenkatalog now in technical domains MAP and(!) DATASET !

		// add TECHNICAL DOMAIN DATASET
		IngridDocument technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		oDoc.put(MdekKeys.TECHNICAL_DOMAIN_DATASET, technicalDomain);
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST DESCRIPTION_OF_TECH_DOMAIN");
		// add TECHNICAL DOMAIN DATASET - key catalog
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.KEY_CATALOG_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check SUBJECT_CAT_KEY -> SUBJECT_CAT is stored via syslist
		testDoc.put(MdekKeys.SUBJECT_CAT_KEY, 1);
		testDoc.put(MdekKeys.KEY_DATE, "TEST " + MdekKeys.KEY_DATE);
		testDoc.put(MdekKeys.EDITION, "TEST " + MdekKeys.EDITION);
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.KEY_CATALOG_LIST, docList);
		// add TECHNICAL DOMAIN DATASET - dataset-parameter
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.PARAMETERS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PARAMETER, "TEST PARAMETER");
		testDoc.put(MdekKeys.SUPPLEMENTARY_INFORMATION, "TEST SUPPLEMENTARY_INFORMATION");
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.PARAMETERS, docList);

		// change TECHNICAL DOMAIN MAP
		technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		// add TECHNICAL DOMAIN MAP - key catalog
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.KEY_CATALOG_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check SUBJECT_CAT_KEY -> SUBJECT_CAT is stored via syslist
		testDoc.put(MdekKeys.SUBJECT_CAT_KEY, 1);
		testDoc.put(MdekKeys.KEY_DATE, "TEST " + MdekKeys.KEY_DATE);
		testDoc.put(MdekKeys.EDITION, "TEST " + MdekKeys.EDITION);
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.KEY_CATALOG_LIST, docList);
		// add TECHNICAL DOMAIN MAP - feature types
		List<String> strList = (List<String>) technicalDomain.get(MdekKeys.FEATURE_TYPE_LIST);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST feature type");
		technicalDomain.put(MdekKeys.FEATURE_TYPE_LIST, strList);

		// add entry to LINKAGES, removed stuff from LINKAGE, see INGRID32-27
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LINKAGES);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check USE_TERMS_OF_USE_KEY -> USE_TERMS_OF_USE_VALUE is stored via syslist
		testDoc.put(MdekKeys.LINKAGE_URL, "http://LINKAGE_URL");
		testDoc.put(MdekKeys.LINKAGE_REFERENCE_ID, 3100);
		testDoc.put(MdekKeys.LINKAGE_REFERENCE, "Methode / Datengrundlage");
		testDoc.put(MdekKeys.LINKAGE_DESCRIPTION, "LINKAGE_DESCRIPTION");
		testDoc.put(MdekKeys.LINKAGE_NAME, "LINKAGE_NAME");
		testDoc.put(MdekKeys.LINKAGE_URL_TYPE, 1);
		testDoc.put("linkage-datatype-key", "REMOVED !!!!!!!");
		testDoc.put("linkage-datatype", "REMOVED !!!!!!!");
		testDoc.put("linkage-volume", "REMOVED !!!!!!!");
		testDoc.put("linkage-icon-text", "REMOVED !!!!!!!");
		testDoc.put("linkage-icon-url", "REMOVED !!!!!!!");
		docList.add(testDoc);
		oDoc.put(MdekKeys.LINKAGES, docList);

		// change TECHNICAL DOMAIN SERVICE
		// add operation with platform key/value (now syslist), see INGRID32-26
		technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		oDoc.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, technicalDomain);
		technicalDomain.put(MdekKeys.SERVICE_TYPE_KEY, 2);
		technicalDomain.put(MdekKeys.COUPLING_TYPE, "tight");
		// add TECHNICAL DOMAIN SERVICE - operations
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_OPERATION_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		technicalDomain.put(MdekKeys.SERVICE_OPERATION_LIST, docList);
		// check SERVICE_OPERATION_NAME_KEY -> SERVICE_OPERATION_NAME is stored via syslist
		// NOTICE: "interacts" with SERVICE_TYPE_KEY
		testDoc = new IngridDocument();
		docList.add(testDoc);
		testDoc.put(MdekKeys.SERVICE_OPERATION_NAME_KEY, 1);
		testDoc.put(MdekKeys.SERVICE_OPERATION_DESCRIPTION, "TEST SERVICE_OPERATION_DESCRIPTION");
		testDoc.put(MdekKeys.INVOCATION_NAME, "TEST INVOCATION_NAME");
		// add TECHNICAL DOMAIN SERVICE - connectPoints
		strList = new ArrayList<String>();
		testDoc.put(MdekKeys.CONNECT_POINT_LIST, strList);
		strList.add("TEST CONNECT_POINT1");
		strList.add("TEST CONNECT_POINT2");
		// add TECHNICAL DOMAIN SERVICE - operation platforms
		docList = new ArrayList<IngridDocument>();
		testDoc.put(MdekKeys.PLATFORM_LIST, docList);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PLATFORM_KEY, 1);
		testDoc.put(MdekKeys.PLATFORM_VALUE, "TEST PLATFORM1");
		docList.add(testDoc);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PLATFORM_KEY, 2);
		testDoc.put(MdekKeys.PLATFORM_VALUE, "TEST PLATFORM2");
		docList.add(testDoc);

		supertool.publishObject(oDoc, true, false);

		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		// OVERWRITE !!!
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

//		System.out.println("\n----- discard changes (working copy) -----");
//		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- discard changes -> remove former change and publish -----");
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_LIST);
		docList.remove(docList.size()-1);
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.CONFORMITY_LIST);
		docList.remove(docList.size()-1);
		oDoc.remove(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		technicalDomain = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.KEY_CATALOG_LIST);
		docList.remove(docList.size()-1);
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.FEATURE_TYPE_LIST);
		docList.remove(docList.size()-1);
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LINKAGES);
		docList.remove(docList.size()-1);
		oDoc.remove(MdekKeys.TECHNICAL_DOMAIN_SERVICE);

		result = supertool.publishObject(oDoc, true, false);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 3.0.1
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		// already published and has "Raumbezugssystem" ("coordinate-system" now moved to "spatial-domain" !)
		IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);
		
		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");
		// further additional change in ImportMapper 3011, now LIST of SpatialSystems (1:n !!!)
		// we add new one and publish !
		List<IngridDocument> docList = (List<IngridDocument>) oDoc.get(MdekKeys.SPATIAL_SYSTEM_LIST);
		// add second spatial system from Syslist ! Name should be overwritten with name from syslist ! 
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.REFERENCESYSTEM_ID, 3068);
		testDoc.put(MdekKeys.COORDINATE_SYSTEM, "TEST COORDINATE_SYSTEM");
		docList.add(testDoc);
		oDoc = supertool.publishObject(oDoc, true, false);

		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		// OVERWRITE !!!
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

//		System.out.println("\n----- discard changes (working copy) -----");
//		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- discard changes -> remove former change -----");
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.SPATIAL_SYSTEM_LIST);
		docList.remove(docList.size()-1);
		result = supertool.publishObject(oDoc, true, false);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 3.0.0
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		IngridDocument oDoc = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

		// set IS_INSPIRE_RELEVANT
		String origIsInspireRelevant = oDoc.getString(MdekKeys.IS_INSPIRE_RELEVANT);
		oDoc.put(MdekKeys.IS_INSPIRE_RELEVANT, "Y");

		// set VERTICAL_EXTENT_VDATUM_KEY
		Integer origVerticalExtentVdatumKey = (Integer) oDoc.get(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY);
		String origVerticalExtentVdatumValue = oDoc.getString(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE);
		oDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY, new Integer(5129));

		// add entries to OBJECT ADDITIONAL_FIELDS
		List<IngridDocument> docList = (List<IngridDocument>) oDoc.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList == null) {
			docList = new ArrayList<IngridDocument>();
			oDoc.put(MdekKeys.ADDITIONAL_FIELDS, docList);
		}
		// add single field
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY SINGLE");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, "TEST ADDITIONAL_FIELD_DATA");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID, "ADDITIONAL_FIELD_LIST_ITEM_ID");
		docList.add(testDoc);
		// add table
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY TABLE");
		List<List<IngridDocument>> rowsList = new ArrayList<List<IngridDocument>>();
		List<IngridDocument> row1List = new ArrayList<IngridDocument>();
		rowsList.add(row1List);
		List<IngridDocument> row2List = new ArrayList<IngridDocument>();
		rowsList.add(row2List);
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_ROWS, rowsList);
		docList.add(testDoc);
		// add columns to rows
		// row 1
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY ROW1 COL1");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, "TEST ADDITIONAL_FIELD_DATA ROW1 COL1");
		row1List.add(testDoc);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY ROW1 COL2");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, "TEST ADDITIONAL_FIELD_DATA ROW1 COL2");
		row1List.add(testDoc);
		// row 2
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY ROW2 COL1");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, "TEST ADDITIONAL_FIELD_DATA ROW2 COL1");
		row2List.add(testDoc);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, "TEST ADDITIONAL_FIELD_KEY ROW2 COL2");
		testDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, "TEST ADDITIONAL_FIELD_DATA ROW2 COL2");
		row2List.add(testDoc);

		oDoc = supertool.publishObject(oDoc, true, false);

		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, true);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change -----");
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList != null && docList.size() > 0) {
			docList.remove(docList.size()-1);
			docList.remove(docList.size()-1);
		}
		oDoc.put(MdekKeys.IS_INSPIRE_RELEVANT, origIsInspireRelevant);
		oDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY, origVerticalExtentVdatumKey);
		oDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE, origVerticalExtentVdatumValue);

		result = supertool.publishObject(oDoc, true, false);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 2.0.3
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		IngridDocument oMap = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");
		// add entries to OBJECT DQ
		MdekSysList[] dqSyslists = new MdekSysList[] {
				MdekSysList.DQ_109_CompletenessComission,
				MdekSysList.DQ_110_CompletenessOmission,
				MdekSysList.DQ_112_ConceptualConsistency,
				MdekSysList.DQ_113_DomainConsistency,
				MdekSysList.DQ_114_FormatConsistency,
				MdekSysList.DQ_115_TopologicalConsistency,
				MdekSysList.DQ_117_AbsoluteExternalPositionalAccuracy,
				MdekSysList.DQ_120_TemporalConsistency,
				MdekSysList.DQ_125_ThematicClassificationCorrectness,
				MdekSysList.DQ_126_NonQuantitativeAttributeAccuracy,
				MdekSysList.DQ_127_QuantitativeAttributeAccuracy };
		List<IngridDocument> docList = (List<IngridDocument>) oMap.get(MdekKeys.DATA_QUALITY_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		oMap.put(MdekKeys.DATA_QUALITY_LIST, docList);
		for (MdekSysList dqSyslist : dqSyslists) {
			// also add free entry for every list (key = -1)
			int key = 1;
			while (key >= -1) {
				IngridDocument testDoc = new IngridDocument();
				testDoc.put(MdekKeys.DQ_ELEMENT_ID, dqSyslist.getDqElementId());
				testDoc.put(MdekKeys.NAME_OF_MEASURE_KEY, key);
				testDoc.put(MdekKeys.NAME_OF_MEASURE_VALUE, "Free NAME_OF_MEASURE_VALUE !!!?");
				testDoc.put(MdekKeys.RESULT_VALUE, "Test RESULT_VALUE " + dqSyslist.getDqElementId());
				testDoc.put(MdekKeys.MEASURE_DESCRIPTION, "Test MEASURE_DESCRIPTION " + dqSyslist.getDqElementId());
				docList.add(testDoc);
				key = key - 2;
			}
		}
		// add entries to OBJECT FORMAT_INSPIRE
		docList = (List<IngridDocument>) oMap.get(MdekKeys.FORMAT_INSPIRE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		oMap.put(MdekKeys.FORMAT_INSPIRE_LIST, docList);
		Integer[] formatInspireKeys = new Integer[] {
				1, 2, 3, 4, 5, 6, -1 };
		for (Integer formatInspireKey : formatInspireKeys) {
			IngridDocument testDoc = new IngridDocument();
			testDoc.put(MdekKeys.FORMAT_KEY, formatInspireKey);
			testDoc.put(MdekKeys.FORMAT_VALUE, "Free FORMAT_VALUE !!!?");
			docList.add(testDoc);
		}
		oMap = supertool.publishObject(oMap, true, false);

		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change -----");
		// remove all added test dq elements
		docList = (List<IngridDocument>) oMap.get(MdekKeys.DATA_QUALITY_LIST);
		for (int i=0; i < dqSyslists.length; i++) {
			docList.remove(docList.size()-1);
			docList.remove(docList.size()-1);
		}
		// remove all added test FORMAT_INSPIRE elements
		docList = (List<IngridDocument>) oMap.get(MdekKeys.FORMAT_INSPIRE_LIST);
		for (int i=0; i < formatInspireKeys.length; i++) {
			docList.remove(docList.size()-1);
		}
		result = supertool.publishObject(oMap, true, false);

		System.out.println("----- DELETE Import Top Nodes -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
/*
		// Test EXPORT / IMPORT new Exchange Format 1.0.9
		// --------------------------
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		IngridDocument oMap = supertool.fetchObject(objUuid, FetchQuantity.EXPORT_ENTITY);

		System.out.println("\n----- change and publish existing object (only published are exported) ! -----");

		// add entry to OBJECT USE
		List<IngridDocument> docList = (List<IngridDocument>) oMap.get(MdekKeys.USE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.USE_TERMS_OF_USE, "TEST USE_TERMS_OF_USE");
		docList.add(testDoc);
		oMap.put(MdekKeys.USE_LIST, docList);

		// test new SERVICE stuff
		IngridDocument serviceDoc = (IngridDocument) oMap.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		// should be null for tests (object is of other type !)
		if (serviceDoc != null)
			throw new RuntimeException("Fachbezug \"Service\" vorhanden, Test nicht möglich !!!");
		serviceDoc = new IngridDocument();
		oMap.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, serviceDoc);
		serviceDoc.put(MdekKeys.HAS_ACCESS_CONSTRAINT, "N");
		serviceDoc.put(MdekKeys.SERVICE_TYPE_KEY, new Integer(6));
		serviceDoc.put(MdekKeys.SERVICE_TYPE, "Sonstige Dienste");
		docList = new ArrayList<IngridDocument>();
		serviceDoc.put(MdekKeys.SERVICE_TYPE2_LIST, docList);
		doc = new IngridDocument();
		doc.put(MdekKeys.SERVICE_TYPE2_KEY, new Integer(108));
		doc.put(MdekKeys.SERVICE_TYPE2_VALUE, "Editor für geografische Symbole");
		docList.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.SERVICE_TYPE2_KEY, new Integer(109));
		doc.put(MdekKeys.SERVICE_TYPE2_VALUE, "Editor für die Objektgeneralisierung");
		docList.add(doc);
		docList = new ArrayList<IngridDocument>();
		serviceDoc.put(MdekKeys.URL_LIST, docList);
		doc = new IngridDocument();
		doc.put(MdekKeys.NAME, "www.url11111111 NAME");
		doc.put(MdekKeys.URL, "www.url11111111");
		doc.put(MdekKeys.DESCRIPTION, "www.url11111111 Descriptionnnnnnnn");
		docList.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.NAME, "www.url222222 NAME");
		doc.put(MdekKeys.URL, "www.url222222");
		doc.put(MdekKeys.DESCRIPTION, "www.url2222 Descriptionnnnnnnn");
		docList.add(doc);

		oMap = supertool.publishObject(oMap, true, false);

		System.out.println("\n----- export object -----");
		supertool.exportObjectBranch(objUuid, true);
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

		System.out.println("\n----- import object as WORKING VERSION -----");
		supertool.importEntities(exportZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- discard changes -> remove former change -----");
		docList = (List<IngridDocument>) oMap.get(MdekKeys.USE_LIST);
		if (docList != null && docList.size() > 0) {
			docList.remove(docList.size()-1);
		}
		oMap.remove(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		result = supertool.publishObject(oMap, true, false);

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
		System.out.println("EXPORT OBJECTS");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- fetch object EXPORT_ENTITY quantity -----");
		IngridDocument topObjUuidPublishedDoc = supertool.fetchObject(topObjUuid, FetchQuantity.EXPORT_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- get LAST Export Info -----");
		supertool.setFullOutput(false);
		supertool.getExportInfo(false);


		System.out.println("\n\n----- export objects ONLY TOP NODE -----\n");

		System.out.println("\n----- first fetch object and store a working version to test export of different instances ! -----");
		doc = supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.TITLE, "WORKING VERSION: " + doc.get(MdekKeys.TITLE));
		supertool.storeObject(doc, false);
		
		System.out.println("\n----- export ONLY TOP NODE, NO export of working version -----\n");
		supertool.exportObjectBranch(topObjUuid, true, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		byte[] exportTopObjZipped_onlyPublished = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
		String exportTopObjUnzipped_onlyPublished = "";
		try {
			exportTopObjUnzipped_onlyPublished = MdekUtils.decompressZippedByteArray(exportTopObjZipped_onlyPublished);
		} catch(IOException ex) {
			System.out.println(ex);
		}
		
		System.out.println("\n----- export ONLY TOP NODE, WITH export of working version -----\n");
		supertool.exportObjectBranch(topObjUuid, true, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);
		byte[] exportTopObjZipped_workAndPublished = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		String exportTopObjUnzipped_workAndPublished = "";
		try {
			exportTopObjUnzipped_workAndPublished = MdekUtils.decompressZippedByteArray(exportTopObjZipped_workAndPublished);
		} catch(IOException ex) {
			System.out.println(ex);
		}

		System.out.println("\n----- Clean Up ! delete TOP OBJECT (WORKING COPY) -----");
		supertool.deleteObjectWorkingCopy(topObjUuid, false);

		System.out.println("\n\n-----------------------------------------");
		System.out.println("----- CHECK EXPORT TOP NODE / BRANCH WITH MISSING VERSIONS ! -----");

		System.out.println("\n\n----- create NEW TOP NODE only working version ! -----");
		IngridDocument newObjDoc = new IngridDocument();
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.addPointOfContactAddress(newObjDoc, personAddrUuid);
		newObjDoc = supertool.storeObject(newObjDoc, true);
		// uuid created !
		String newTopObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- export NEW TOP NODE, PUBLISHED version -> Error ENTITY_NOT_FOUND -----");
		System.out.println("----- EXCEPTION ONLY THROWN IF STARTING(!) OBJECT VERSION NOT FOUND, NOT FOR SUBOBJECTS (subobjects are just skipped) -----\n");
		supertool.exportObjectBranch(newTopObjUuid, true, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- export NEW TOP NODE, WITH export of working version, OK, export has just working version -----\n");
		supertool.exportObjectBranch(newTopObjUuid, true, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- publish NEW TOP OBJECT to test export of published parent, unpublished 1. child, published 2. child -----");
		supertool.publishObject(newObjDoc, true, true);

		System.out.println("\n----- create NEW 1. SUB NODE only working version ! -----");
		newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, newTopObjUuid);
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST 1. NEUES SUB OBJEKT");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.addPointOfContactAddress(newObjDoc, personAddrUuid);
		newObjDoc = supertool.storeObject(newObjDoc, true);
		// uuid created !
		//String newSubUuid1 = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create NEW 2. SUB NODE published AND working version ! -----");
		newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, newTopObjUuid);
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST 2. NEUES SUB OBJEKT");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.addPointOfContactAddress(newObjDoc, personAddrUuid);
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		// uuid created !
		//String newSubUuid2 = (String) newObjDoc.get(MdekKeys.UUID);
		System.out.println("\n----- store 2. child again to have working version ! -----\n");
		supertool.storeObject(newObjDoc, true);

		System.out.println("\n----- export NEW TOP NODE branch, only PUBLISHED version -> OK, 2 nodes, first child skipped, working version of 2. child skipped ! -----");
		System.out.println("----- NO EXCEPTION IF CHILD HAS NOT REQUESTED VERSION, CHILD JUST SKIPPED -----\n");
		supertool.exportObjectBranch(newTopObjUuid, false, false);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- export NEW TOP NODE branch, include WORKING version -> OK, 3 nodes, 2. child working version also included ! -----\n");
		supertool.exportObjectBranch(newTopObjUuid, false, true);
		supertool.setFullOutput(true);
		result = supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n===== Clean Up ! back to old state of DB ! =====");

		System.out.println("\n----- delete NEW TOP OBJECT (FULL) -----");
		supertool.deleteObject(newTopObjUuid, false);


		System.out.println("\n\n-----------------------------------------");
		System.out.println("----- FURTHER EXPORTS -----");

		System.out.println("\n\n-----------------------------------------");
		System.out.println("----- export object with additional field for testing import -----");
		System.out.println("\n----- was unpublished by udk updater (1.0.9), we publish before export to export published version ! -----");
		doc = supertool.fetchObject(objWithAdditionalFieldsUuid, FetchQuantity.EDITOR_ENTITY);
		IngridDocument objWithAdditionalFieldsPublishedDoc = supertool.publishObject(doc, true,false);
		supertool.exportObjectBranch(objWithAdditionalFieldsUuid, true, false);
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

		System.out.println("\n\n----- export objects FULL BRANCH UNDER PARENT -----");

		System.out.println("\n----- first fetch objects and store a working versions to test export of different instances ! -----");
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.TITLE, "WORKING VERSION: " + doc.get(MdekKeys.TITLE));
		supertool.storeObject(doc, true);
		doc = supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.TITLE, "WORKING VERSION: " + doc.get(MdekKeys.TITLE));
		supertool.storeObject(doc, true);
				
		System.out.println("\n----- export ONLY PUBLISHED INSTANCES, NO export of working version -----\n");
		byte[] exportObjBranchZipped_onlyPublished = null;
		String exportObjBranchUnzipped_onlyPublished = "";
		try {
			// causes timeout
//			supertool.exportObjectBranch(topObjUuid, false);
//			result = supertool.getExportInfo(false);
//			supertool.getExportInfo(true);

			supertool.exportObjectBranch(objUuid, false, false);
			// extract XML result !
			supertool.setFullOutput(true);
			result = supertool.getExportInfo(true);
			supertool.setFullOutput(false);
			exportObjBranchZipped_onlyPublished = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
			exportObjBranchUnzipped_onlyPublished = MdekUtils.decompressZippedByteArray(exportObjBranchZipped_onlyPublished);

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

		System.out.println("\n----- export WITH export of WORKING INSTANCES !!! -----\n");
		byte[] exportObjBranchZipped_workAndPublished = null;
		String exportObjBranchUnzipped_workAndPublished = "";
		try {
			supertool.exportObjectBranch(objUuid, false, true);
			// extract XML result !
			supertool.setFullOutput(true);
			result = supertool.getExportInfo(true);
			supertool.setFullOutput(false);
			exportObjBranchZipped_workAndPublished = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
			exportObjBranchUnzipped_workAndPublished = MdekUtils.decompressZippedByteArray(exportObjBranchZipped_workAndPublished);

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


		System.out.println("\n\n----- export \"tagged\" objects -----");
		supertool.exportObjects("CDS", false);
		supertool.setFullOutput(true);
		supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		supertool.exportObjects("CdS", true);
		supertool.setFullOutput(true);
		supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		supertool.exportObjects("test", false);
		supertool.setFullOutput(true);
		supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		supertool.exportObjects("TEST", true);
		supertool.setFullOutput(true);
		supertool.getExportInfo(true);
		supertool.setFullOutput(false);

		System.out.println("\n----- export objects ALL TOP NODES -----");
		supertool.exportObjectBranch(null, true, false);
		supertool.setFullOutput(true);
		supertool.getExportInfo(true);
		supertool.setFullOutput(false);
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

		System.out.println("\n----- create new Import Top Node for Objects (NOT PUBLISHED) -----");
		objImpNodeDoc = supertool.newObjectDoc(null);
		objImpNodeDoc.put(MdekKeys.TITLE, "IMPORT OBJECTS");
		objImpNodeDoc.put(MdekKeys.CLASS, MdekUtils.ObjectType.DATENSAMMLUNG.getDbValue());
		objImpNodeDoc = supertool.storeObject(objImpNodeDoc, true);
		objImpNodeUuid = (String) objImpNodeDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new Import Top Node for Addresses (NOT PUBLISHED) -----");
		addrImpNodeDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		addrImpNodeDoc.put(MdekKeys.ORGANISATION, "IMPORT ADDRESSES");
		addrImpNodeDoc = supertool.storeAddress(addrImpNodeDoc, true);
		addrImpNodeUuid = (String) addrImpNodeDoc.get(MdekKeys.UUID);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: INVALID XML -----");
		System.out.println("-------------------------------------");

		// invalid XML file to test logging of exception in job info
		// causes NumberFormatException
		String importUnzipped = exportTopObjUnzipped_onlyPublished.replace("<object-class id=\"", "<object-class id=\"MM");
		byte[] importInvalidXML = new byte[0];
		try {
			importInvalidXML = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n----- import INVALID XML -> Exception logged in jobinfo ! -----");
		supertool.importEntities(importInvalidXML, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.getJobInfo(JobType.IMPORT);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: UPDATE EXISTING OBJECTS (UUID) -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
		System.out.println("-------------------------------------");

		// first change data to import

		// set some stuff to simulate different catalog etc. will be replaced with correct data !
		// different catalog
		exportTopObjUnzipped_onlyPublished = exportTopObjUnzipped_onlyPublished.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		exportObjBranchUnzipped_onlyPublished = exportObjBranchUnzipped_onlyPublished.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
		// different mod user !
		// Existing USERS from import are written ! NON existing replaced by user doing import.
		// Set existing user ! Should be kept
		exportTopObjUnzipped_onlyPublished = exportTopObjUnzipped_onlyPublished.replace(
				"<modificator-identifier>3866459C-B449-11D2-9A86-080000507261</modificator-identifier>", 
				"<modificator-identifier>2BA71BA3-8E35-4963-89B0-988EB04A0BAA</modificator-identifier>");
		// Set NON existing user ! Should be replaced by user doing import (A84E2BD7-648E-4205-9D70-397D05E9AD65)
		exportObjBranchUnzipped_onlyPublished = exportObjBranchUnzipped_onlyPublished.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
		// different responsible user
		// Existing USERS from import are written ! If not existent, then former responsible user is kept !
		// Set existing user ! Should be kept
		exportTopObjUnzipped_onlyPublished = exportTopObjUnzipped_onlyPublished.replace(
				"<responsible-identifier>A84E2BD7-648E-4205-9D70-397D05E9AD65</responsible-identifier>", 
				"<responsible-identifier>2BA71BA3-8E35-4963-89B0-988EB04A0BAA</responsible-identifier>");
		// Set NON existing user ! Former responsible user should be kept (A84E2BD7-648E-4205-9D70-397D05E9AD65)
		exportObjBranchUnzipped_onlyPublished = exportObjBranchUnzipped_onlyPublished.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
		// TODO: what else ?

		// import data: single existing top node
		importUnzipped = exportTopObjUnzipped_onlyPublished.replace("<title>", "<title>MMImport: ");
		// different languages (german to english)
		importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
		importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
		System.out.println("importExistingTopObj:\n" + importUnzipped);
		byte[] importExistingTopObj_onlyPublished = new byte[0];
		try {
			importExistingTopObj_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing sub nodes (branch)
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<title>", "<title>MMImport: ");
		// different languages (german to english)
		importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
		importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
		System.out.println("importExistingObjBranch:\n" + importUnzipped);
		byte[] importExistingObjBranch_onlyPublished = new byte[0];
		try {
			importExistingObjBranch_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! (Published Version because only published instance in impport file !) -----");
		supertool.setFullOutput(false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- import existing TOP NODE (only published instance !), publishImmediately=FALSE -> IS PUBLISHED ! check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopObj_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.getJobInfo(JobType.IMPORT);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing TOP NODE (only published instance !), publishImmediately=TRUE -> IS PUBLISHED ! check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingTopObj_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);


		
		System.out.println("\n\n----- EXISTING ROOT OF BRANCH BEFORE IMPORT !!! (Published Version because only published instances in import file !) -----");
		IngridDocument objUuidPublishedDoc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n\n\n----- import existing branch (only published instances !), publishImmediately=FALSE -> ALL PUBLISHED ! check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingObjBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import existing branch (only published instances !), publishImmediately=TRUE -> ALL PUBLISHED ! check correct catalog id, moduser, responsibleuser -----");
		supertool.importEntities(importExistingObjBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		
		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: EXISTING OBJECTS (UUID) -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
		System.out.println("-------------------------------------");

		supertool.setFullOutput(true);
		
		System.out.println("\n----- separate import existing TOP NODE, COPY NOT ALLOWED -> ERROR: IMPORT_OBJECTS_ALREADY_EXIST -----");
		supertool.importEntities(importExistingTopObj_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- separate import existing branch, COPY NOT ALLOWED -> ERROR: IMPORT_OBJECTS_ALREADY_EXIST -----");
		supertool.importEntities(importExistingObjBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);

		supertool.setFullOutput(false);

		System.out.println("\n----- separate import existing TOP NODE, COPY ALLOWED -> underneath import node, NEW uuid -----");
		supertool.importEntities(importExistingTopObj_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- separate import + publishImmediately = true -> NOW ALLOWED !!! No more \"Forced publishing underneath import nodes not possible\" -----");
		supertool.importEntities(importExistingTopObj_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- separate import existing branch, COPY ALLOWED -> underneath import node, NEW Uuid, KEEP STRUCTURE -----");
		supertool.importEntities(importExistingObjBranch_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

				System.out.println("\n\n-------------------------------------");
				System.out.println("----- Import: UPDATE EXISTING OBJECTS (UUID) -----");
				System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
				System.out.println("-------------------------------------");

				// first change data to import

				// set some stuff to simulate different catalog etc. will be replaced with correct data !
				// different catalog
				exportTopObjUnzipped_workAndPublished = exportTopObjUnzipped_workAndPublished.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
				exportObjBranchUnzipped_workAndPublished = exportObjBranchUnzipped_workAndPublished.replace("<catalogue-identifier>", "<catalogue-identifier>99999");
				// different mod user !
				// Existing USERS from import are written ! NON existing replaced by user doing import.
				// Set existing user ! Should be kept
				exportTopObjUnzipped_workAndPublished = exportTopObjUnzipped_workAndPublished.replace(
						"<modificator-identifier>3866459C-B449-11D2-9A86-080000507261</modificator-identifier>", 
						"<modificator-identifier>2BA71BA3-8E35-4963-89B0-988EB04A0BAA</modificator-identifier>");
				// Set NON existing user ! Should be replaced by user doing import (A84E2BD7-648E-4205-9D70-397D05E9AD65)
				exportObjBranchUnzipped_workAndPublished = exportObjBranchUnzipped_workAndPublished.replace("<modificator-identifier>", "<modificator-identifier>MMMMM");
				// different responsible user
				// Existing USERS from import are written ! If not existent, then former responsible user is kept !
				// Set existing user ! Should be kept
				exportTopObjUnzipped_workAndPublished = exportTopObjUnzipped_workAndPublished.replace(
						"<responsible-identifier>A84E2BD7-648E-4205-9D70-397D05E9AD65</responsible-identifier>", 
						"<responsible-identifier>2BA71BA3-8E35-4963-89B0-988EB04A0BAA</responsible-identifier>");
				// Set NON existing user ! Former responsible user should be kept (A84E2BD7-648E-4205-9D70-397D05E9AD65)
				exportObjBranchUnzipped_workAndPublished = exportObjBranchUnzipped_workAndPublished.replace("<responsible-identifier>", "<responsible-identifier>MMMMM");
				// TODO: what else ?

				// import data: single existing top node
				importUnzipped = exportTopObjUnzipped_workAndPublished.replace("<title>", "<title>MMImport: ");
				// different languages (german to english)
				importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
				importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
				System.out.println("importExistingTopObj:\n" + importUnzipped);
				byte[] importExistingTopObj_workAndPublished = new byte[0];
				try {
					importExistingTopObj_workAndPublished = MdekUtils.compressString(importUnzipped);						
				} catch (Exception ex) {
					System.out.println(ex);			
				}

				// import data: existing sub nodes (branch)
				importUnzipped = exportObjBranchUnzipped_workAndPublished.replace("<title>", "<title>MMImport: ");
				// different languages (german to english)
				importUnzipped = importUnzipped.replace("<data-language id=\"150\">", "<data-language id=\"123\">");
				importUnzipped = importUnzipped.replace("<metadata-language id=\"150\">", "<metadata-language id=\"123\">");
				System.out.println("importExistingObjBranch:\n" + importUnzipped);
				byte[] importExistingObjBranch_workAndPublished = new byte[0];
				try {
					importExistingObjBranch_workAndPublished = MdekUtils.compressString(importUnzipped);						
				} catch (Exception ex) {
					System.out.println(ex);			
				}

				System.out.println("\n\n----- EXISTING TOP NODE BEFORE IMPORT !!! (WORKING_VERSION) -----");
				supertool.setFullOutput(false);
				supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

				System.out.println("\n----- import existing TOP NODE (published + WORKING instance !) -> saved PUBLISH + WORKING ! check correct catalog id, moduser, responsibleuser -----");
				supertool.importEntities(importExistingTopObj_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
				supertool.getJobInfo(JobType.IMPORT);
				supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
				supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

				
				System.out.println("\n\n----- EXISTING ROOT OF BRANCH BEFORE IMPORT !!! (WORKING_VERSION) -----");
				supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

				System.out.println("\n\n\n----- import existing branch (published + WORKING instances !), -> saved PUBLISH + WORKING ! check correct catalog id, moduser, responsibleuser -----");
				supertool.importEntities(importExistingObjBranch_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
				supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
				supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

				
				System.out.println("\n\n-------------------------------------");
				System.out.println("----- SEPARATE Import: EXISTING OBJECTS (UUID) -----");
				System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
				System.out.println("-------------------------------------");

				supertool.setFullOutput(true);
				
				System.out.println("\n----- separate import existing TOP NODE, COPY NOT ALLOWED -> ERROR: IMPORT_OBJECTS_ALREADY_EXIST -----");
				supertool.importEntities(importExistingTopObj_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
				supertool.fetchSubObjects(objImpNodeUuid);

				System.out.println("\n----- separate import existing branch, COPY NOT ALLOWED -> ERROR: IMPORT_OBJECTS_ALREADY_EXIST -----");
				supertool.importEntities(importExistingObjBranch_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
				supertool.fetchSubObjects(objImpNodeUuid);

				supertool.setFullOutput(false);
				
				System.out.println("\n----- separate import existing TOP NODE, COPY ALLOWED -> underneath import node, NEW uuid, PUBLISHED AND WORKING STORED AS WORKING VERSION ! -----");
				supertool.importEntities(importExistingTopObj_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
				supertool.fetchSubObjects(objImpNodeUuid);

				System.out.println("\n----- separate import existing branch, COPY ALLOWED -> underneath import node, NEW Uuid, KEEP STRUCTURE -----");
				supertool.importEntities(importExistingObjBranch_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
				supertool.fetchSubObjects(objImpNodeUuid);

				System.out.println("\n----- Clean Up ImportNode -----");
				supertool.deleteObject(objImpNodeUuid, true);
				supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: NEW OBJECTS (UUID) into catalog -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
		System.out.println("-------------------------------------");

		String newUuidTop = "UUID012345678901234567890123456789-3";
		String newUuid1 = "UUID012345678901234567890123456789-1";
		String newUuid2 = "UUID012345678901234567890123456789-2";

		// import data: single NEW top node
		importUnzipped = exportTopObjUnzipped_onlyPublished.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(topObjUuid, newUuidTop);
		byte[] importNewTopObj = new byte[0];
		try {
			importNewTopObj = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW object branch with non existing parent !
		// new uuids
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<title>", "<title>MMImport NEW: ");
		importUnzipped = importUnzipped.replace(objUuid, newUuid1);
		importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
		// set non existing parent in root of branch
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
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<title>", "<title>MMImport NEW: ");
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

		System.out.println("\n----- import NEW TOP NODE into catalog, publishImmediately=FALSE -> NEW top node published due to published instance ! -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuidTop, true);

		System.out.println("\n----- import NEW TOP NODE into catalog, publishImmediately=TRUE -> NEW top node published due to published instance -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuidTop, true);


		System.out.println("\n\n\n----- import NEW object branch with EXISTING parent into catalog, publishImmediately=FALSE -> underneath EXISTING PARENT, all published due to published instances -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		System.out.println("\n----- we delete the new sub branch ! -----");
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n\n----- import NEW object branch with EXISTING parent into catalog, publishImmediately=TRUE -> underneath EXISTING PARENT, all published due to published instances -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		System.out.println("\n----- we keep this new sub branch ! -----");
//		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n\n----- import SAME NEW object branch with NON EXISTING parent into catalog, publishImmediately=FALSE -> KEEP \"OLD\" PARENT of existing branch, all published -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubObjects(existentParentUuid);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		System.out.println("\n----- we delete the new sub branch ! -----");
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n----- import NEW object branch with NON EXISTING parent into catalog, publishImmediately=TRUE -> UNDERNEATH IMPORT NODE, try to publish, but stored as working version -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		System.out.println("\n----- NO PUBLISHED_VERSION -> ERROR: ENTITY_NOT_FOUND -----");
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.fetchSubObjects(newUuid1);
		System.out.println("\n----- NO PUBLISHED_VERSION -> ERROR: ENTITY_NOT_FOUND -----");
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: NEW OBJECTS (UUID) -----");
		System.out.println("----- !!! ONLY PUBLISHED IMPORT INSTANCE(s) !!! -----");
		System.out.println("-------------------------------------");

		System.out.println("\n----- separate import NEW TOP NODE -> underneath import node, KEEP UUID, try to publish, but stored as working version -----");
		supertool.importEntities(importNewTopObj, objImpNodeUuid, addrImpNodeUuid, false, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.deleteObject(newUuidTop, true);

		System.out.println("\n----- separate import NEW object branch with EXISTING parent -> underneath import node, KEEP UUIDs, try to publish, but stored as working version -----");
		supertool.importEntities(importNewObjBranchExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchSubObjects(newUuid1);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n----- separate import NEW object branch with NON EXISTING parent -> underneath import node, KEEP UUIDs, try to publish, but stored as working version -----");
		supertool.importEntities(importNewObjBranchNonExistentParent, objImpNodeUuid, addrImpNodeUuid, false, true, false);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.fetchSubObjects(newUuid1);
		supertool.deleteObject(newUuid1, true);

// -----------------------------------

			System.out.println("\n\n-------------------------------------");
			System.out.println("----- Import: NEW OBJECTS (UUID) into catalog -----");
			System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
			System.out.println("-------------------------------------");
	
			// import data: single NEW top node
			importUnzipped = exportTopObjUnzipped_workAndPublished.replace("<title>", "<title>MMImport NEW: ");
			importUnzipped = importUnzipped.replace(topObjUuid, newUuidTop);
			byte[] importNewTopObj_workAndPublished = new byte[0];
			try {
				importNewTopObj_workAndPublished = MdekUtils.compressString(importUnzipped);						
			} catch (Exception ex) {
				System.out.println(ex);			
			}
	
			// import data: NEW object branch with non existing parent !
			// new uuids
			importUnzipped = exportObjBranchUnzipped_workAndPublished.replace("<title>", "<title>MMImport NEW: ");
			importUnzipped = importUnzipped.replace(objUuid, newUuid1);
			importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
			// set non existing parent in root of branch
			importUnzipped = importUnzipped.replace("15C69C20-FE15-11D2-AF34-0060084A4596", "MMMMMC20-FE15-11D2-AF34-0060084A4596");
			byte[] importNewObjBranchNonExistentParent_workAndPublished = new byte[0];
			try {
				importNewObjBranchNonExistentParent_workAndPublished = MdekUtils.compressString(importUnzipped);						
			} catch (Exception ex) {
				System.out.println(ex);			
			}
	
			// import data: SAME NEW object branch with existing parent !
			// new uuids
			importUnzipped = exportObjBranchUnzipped_workAndPublished.replace("<title>", "<title>MMImport NEW: ");
			importUnzipped = importUnzipped.replace(objUuid, newUuid1);
			importUnzipped = importUnzipped.replace(objLeafUuid, newUuid2);
			// existing parent
			importUnzipped = importUnzipped.replace("15C69C20-FE15-11D2-AF34-0060084A4596", existentParentUuid);
			byte[] importNewObjBranchExistentParent_workAndPublished = new byte[0];
			try {
				importNewObjBranchExistentParent_workAndPublished = MdekUtils.compressString(importUnzipped);						
			} catch (Exception ex) {
				System.out.println(ex);			
			}
	
			supertool.setFullOutput(false);
	
			System.out.println("\n----- import NEW TOP NODE into catalog (published + WORKING instances !) -> NEW top node, saved PUBLISH + WORKING ! -----");
			supertool.importEntities(importNewTopObj_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
			supertool.fetchSubObjects(objImpNodeUuid);
			supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			supertool.deleteObject(newUuidTop, true);
	
	
			System.out.println("\n\n\n----- import NEW object branch with EXISTING parent into catalog (published + WORKING instances !) -> underneath EXISTING PARENT, saved PUBLISH + WORKING ! -----");
			supertool.importEntities(importNewObjBranchExistentParent_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
			supertool.fetchSubObjects(existentParentUuid);
			supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			supertool.fetchSubObjects(newUuid1);
			supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			System.out.println("\n----- we keep this new sub branch ! -----");
//			supertool.deleteObject(newUuid1, true);
		
	
			System.out.println("\n\n\n----- import SAME NEW object branch with NON EXISTING parent into catalog (published + WORKING instances !) -> KEEP \"OLD\" PARENT of existing branch, saved PUBLISH + WORKING ! -----");
			supertool.importEntities(importNewObjBranchNonExistentParent_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
			supertool.fetchSubObjects(existentParentUuid);
			supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			supertool.fetchSubObjects(newUuid1);
			supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			System.out.println("\n----- we delete the new sub branch ! -----");
			supertool.deleteObject(newUuid1, true);
	
			System.out.println("\n----- import NEW object branch with NON EXISTING parent into catalog (published + WORKING instances !) -> UNDERNEATH IMPORT NODE, try to publish, but stored as working version -----");
			supertool.importEntities(importNewObjBranchNonExistentParent_workAndPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
			supertool.fetchSubObjects(objImpNodeUuid);
			System.out.println("\n----- NO PUBLISHED_VERSION -> ERROR: ENTITY_NOT_FOUND -----");
			supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			supertool.fetchSubObjects(newUuid1);
			System.out.println("\n----- NO PUBLISHED_VERSION -> ERROR: ENTITY_NOT_FOUND -----");
			supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
			supertool.deleteObject(newUuid1, true);
	
	
			System.out.println("\n\n-------------------------------------");
			System.out.println("----- SEPARATE Import: NEW OBJECTS (UUID) -----");
			System.out.println("----- !!! BOTH: PUBLISHED AND WORKING INSTANCE(s) !!! -----");
			System.out.println("-------------------------------------");
	
			System.out.println("\n----- separate import NEW TOP NODE (published + WORKING instances !) -> underneath import node, KEEP UUID, try to publish, but stored as working version -----");
			supertool.importEntities(importNewTopObj_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
			supertool.fetchSubObjects(objImpNodeUuid);
			supertool.fetchObject(newUuidTop, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
			supertool.deleteObject(newUuidTop, true);
	
			System.out.println("\n----- separate import NEW object branch with EXISTING parent (published + WORKING instances !) -> underneath import node, KEEP UUIDs, try to publish, but stored as working version -----");
			supertool.importEntities(importNewObjBranchExistentParent_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
			supertool.fetchSubObjects(objImpNodeUuid);
			supertool.fetchSubObjects(newUuid1);
			supertool.deleteObject(newUuid1, true);
	
			System.out.println("\n----- separate import NEW object branch with NON EXISTING parent (published + WORKING instances !) -> underneath import node, KEEP UUIDs, try to publish, but stored as working version -----");
			supertool.importEntities(importNewObjBranchNonExistentParent_workAndPublished, objImpNodeUuid, addrImpNodeUuid, false, true, false);
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
		importUnzipped = exportTopObjUnzipped_onlyPublished.replace("<title>", "<title>1.MMImport ORIG_ID1: ");
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		byte[] importExistingTopObjOrigId1_onlyPublished = new byte[0];
		try {
			importExistingTopObjOrigId1_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: existing branch with ORIG_ID1 and ORIG_ID2
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<title>", "<title>2.MMImport ORIG_ID1/2: ");
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
		byte[] importExistingObjBranchOrigIds1_2_onlyPublished = new byte[0];
		try {
			importExistingObjBranchOrigIds1_2_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW object branch (non existing UUIDs) with ORIG_ID1 and ORIG_ID2
		// new uuids
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<title>", "<title>3.MMImport ORIG_ID1/2: ");
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
		byte[] importNewObjBranchOrigIds1_2_onlyPublished = new byte[0];
		try {
			importNewObjBranchOrigIds1_2_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: NEW arcgis object (no work-state) with EXISTING and NON EXISTING ORIG_ID
		importUnzipped = exportTopObjUnzipped_onlyPublished.replace("<title>", "<title>4.MMImport ORIG_ID1, NO work-state: ");
		// add ORIG_ID1
		startIndex = importUnzipped.indexOf("</object-identifier>")+20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			"\n<original-control-identifier>" + origId1 + "</original-control-identifier>" +
			importUnzipped.substring(startIndex, importUnzipped.length());
		// remove work-state
		importUnzipped = importUnzipped.replace(" work-state=\"V\"", "");
		// import data: NEW arcgis object with EXISTING UUID and EXISTING ORIG_ID
		byte[] importArcGisExistingUuidAndOrigId_onlyPublished = new byte[0];
		try {
			importArcGisExistingUuidAndOrigId_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// remove UUID
		startIndex = importUnzipped.indexOf("<object-identifier>");
		endIndex = importUnzipped.indexOf("</object-identifier>") + 20;
		importUnzipped = importUnzipped.substring(0, startIndex) +
			importUnzipped.substring(endIndex, importUnzipped.length());
		// import data: NEW arcgis object with EXISTING ORIG_ID
		byte[] importArcGisNoUuidExistingOrigId_onlyPublished = new byte[0];
		try {
			importArcGisNoUuidExistingOrigId_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}
		// import data: NEW arcgis object with NEW ORIG_ID
		importUnzipped = importUnzipped.replace(origId1, newOrigId);
		byte[] importArcGisNoUuidNewOrigId_onlyPublished = new byte[0];
		try {
			importArcGisNoUuidNewOrigId_onlyPublished = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import ORIG_ID1 into EXISTING top node PUBLISHED VERSION -> NO update of ORIG_ID due to UUID found, keep null -----");
		supertool.importEntities(importExistingTopObjOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		System.out.println("\n----- CHECK: NO update of ORIG_ID, keep null (or check in DB) -----");
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- store ORIG_ID1 in top node WORKING VERSION  -----");
		doc = supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, true);

		
		System.out.println("\n\n----- import ORIG_ID1 + ORIG_ID2 into EXISTING branch PUBLISHED VERSION ->  NO update of ORIG_ID due to UUIDs found, keep null -----");
		supertool.importEntities(importExistingObjBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		System.out.println("\n----- CHECK: NO update of ORIG_ID, keep null (or check in DB) -----");
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		System.out.println("\n----- CHECK: NO update of ORIG_ID, keep null (or check in DB) -----");
		IngridDocument objLeafUuidPublishedDoc = supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- store ORIG_ID1 / ORIG_ID2 in branch WORKING VERSION  -----");
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId1);
		supertool.storeObject(doc, true);
		doc = supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, origId2);
		supertool.storeObject(doc, true);


		System.out.println("\n\n----- import NEW branch (only PUBLISHED instances) with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> update FIRST found ORIG_ID1 and found ORIG_ID2 -----");
		System.out.println("----- NOTICE: Found ORIG_IDs are in WORKING VERSIONs (always working version queried) -----");
		System.out.println("----- !!! IGE iPlug LOG WARNING: MULTIPLE NODES WITH SAME ORIG_ID: ORIG_ID1 !!! -----");
		supertool.importEntities(importNewObjBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);


		System.out.println("\n\n----- import ARCGIS object (no work-state) with existing UUID AND with EXISTING ORIG_ID, publishImmediately=FALSE -> update existing node WORKING VERSION -----");
		System.out.println("----- NOTICE: Found via UUID -----");
		supertool.importEntities(importArcGisExistingUuidAndOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import ARCGIS object (no work-state) with existing UUID AND with EXISTING ORIG_ID, publishImmediately=TRUE -> PUBLISH found object -----");
		System.out.println("----- NOTICE: Found via UUID -----");
		supertool.importEntities(importArcGisExistingUuidAndOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(topObjUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import ARCGIS object (NO uuid, no work-state) with EXISTING ORIG_ID, publishImmediately=FALSE -> update existing node WORKING VERSION -----");
		System.out.println("----- NOTICE: Found via ORIG_ID in WORKING VERSION (always working version queried) -----");
		supertool.importEntities(importArcGisNoUuidExistingOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import ARCGIS object (NO uuid, no work-state) with EXISTING ORIG_ID, publishImmediately=TRUE -> PUBLISH found object -----");
		System.out.println("----- NOTICE: Found via ORIG_ID in WORKING VERSION (always working version queried) -----");
		supertool.importEntities(importArcGisNoUuidExistingOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- import ARCGIS object NO PARENT (no uuid, no work-state) with NEW ORIG_ID, publishImmediately=TRUE -> NEW TOP NODE, Published ! -----");
		supertool.importEntities(importArcGisNoUuidNewOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n\n----- NO Clean Up: WE KEEP ORIG UUIDS ! (already published) -----");
/*
		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(topObjUuid, true);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);
*/


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ORIG_IDS -----");
		System.out.println("-------------------------------------");

		System.out.println("\n\n----- SEPARATE import, copy allowed, EXISTING top node PUBLISHED instance, existing ORIG_ID1 -> underneath import node, ORIG_ID removed, new UUID, working version ! -----");
		System.out.println("----- NOTICE: Found ORIG_IDs are in WORKING VERSIONs (always working version queried) -----");
		System.out.println("----- !!! IGE iPlug LOG WARNING: MULTIPLE NODES WITH SAME ORIG_ID: ORIG_ID1 !!! -----");
		supertool.importEntities(importExistingTopObjOrigId1_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- SEPARATE import, copy allowed, EXISTING branch PUBLISHED instance, existing ORIG_ID1 + ORIG_ID2 -> underneath import node, ORIG_IDs removed, new UUIDs, working version ! -----");
		System.out.println("----- NOTICE: Found ORIG_IDs are in WORKING VERSIONs (always working version queried) -----");
		System.out.println("----- !!! IGE iPlug LOG WARNING: MULTIPLE NODES WITH SAME ORIG_ID: ORIG_ID1 !!! -----");
		supertool.importEntities(importExistingObjBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);
		
		System.out.println("\n\n----- SEPARATE import NEW branch (only PUBLISHED instances) with ORIG_ID1 (multiple !) and ORIG_ID2 (unique) -> underneath import node, ORIG_IDs removed, UUIDs kept, working version ! -----");
		System.out.println("----- !!! IGE iPlug LOG WARNING: MULTIPLE NODES WITH SAME ORIG_ID: ORIG_ID1 !!! -----");
		supertool.importEntities(importNewObjBranchOrigIds1_2_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- SEPARATE import ARCGIS object (NO uuid, no work-state) with EXISTING ORIG_ID -> underneath import node, ORIG_ID removed, new UUID, working version ok -----");
		supertool.importEntities(importArcGisNoUuidExistingOrigId_onlyPublished, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- Clean Up: Publish ORIG ONES ! -----");
		supertool.publishObject(topObjUuidPublishedDoc, true, false);
		supertool.publishObject(objUuidPublishedDoc, true, false);
		supertool.publishObject(objLeafUuidPublishedDoc, true, false);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MOVE OBJECT -----");
		System.out.println("-------------------------------------");

		// import data: move branch under top object !
		importUnzipped = exportObjBranchUnzipped_onlyPublished.replace("<object-identifier>15C69C20-FE15-11D2-AF34-0060084A4596</object-identifier>",
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

		System.out.println("\n\n----- import existing branch with DIFFERENT parent, only PUBLISHED instances -> move branch to new parent, PUBLISH ! -----");
		supertool.importEntities(importBranchMoveObj, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.fetchSubObjects(objParentUuid);
		supertool.fetchSubObjects(topObjUuid5);

		System.out.println("\n----- Clean Up: move back to original position etc.-----");
		supertool.moveObject(objUuid, objParentUuid, false);
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

		System.out.println("\n\n----- Enforce Error when Moving, set PubCondition of new parent to INTRANET  -----");
		doc = supertool.fetchObject(topObjUuid5, FetchQuantity.EDITOR_ENTITY);
		doc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		doc = supertool.publishObject(doc, true, false);

		System.out.println("\n\n----- import existing branch with DIFFERENT parent, only PUBLISHED instances, causes Move causes Error (SUBTREE_HAS_LARGER_PUBLICATION_CONDITION) -> branch keeps position, branch root stored as WORKING version, subnodes PUBLISHED ! -----");
		supertool.importEntities(importBranchMoveObj, objImpNodeUuid, addrImpNodeUuid, true, false, false);
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
		importUnzipped = exportObjBranchUnzipped_onlyPublished;
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
		importUnzipped = exportObjBranchUnzipped_onlyPublished;
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

		// only working instances !
		importUnzipped = importUnzipped.replace("work-state=\"V\"", "work-state=\"B\"");
		byte[] importNewObjBranchRelationTargetIntranet_onlyWorking = new byte[0];
		try {
			importNewObjBranchRelationTargetIntranet_onlyWorking = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}


		System.out.println("\n----- state BEFORE import -----");
		supertool.setFullOutput(true);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);

		System.out.println("\n----- import EXISTING branch with WRONG RELATIONS, only PUBLISHED instances -> remove wrong relations, (but PUBLISHED) -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n----- state AFTER import -----");
		supertool.setFullOutput(true);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);


		System.out.println("\n----- Clean Up: Publish ORIG ONES ! -----");
		supertool.publishObject(objUuidPublishedDoc, true, false);
		supertool.publishObject(objLeafUuidPublishedDoc, true, false);


		System.out.println("\n\n----- import NEW branch, only WORKING instances, with RELATION Parent(INTRANET) > Child(INTERNET) -> relation OK, references working versions ! -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet_onlyWorking, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n----- import NEW branch, only PUBLISHED instances, with RELATION Parent(INTRANET) > Child(INTERNET) -> causes error PARENT_HAS_SMALLER_PUBLICATION_CONDITION, Child is stored as WORKING VERSION! -----");
		System.out.println("-----  -> relation REMOVED ! source published, target not published ! -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: REMOVE RELATIONS -----");
		System.out.println("-------------------------------------");

		// import data: EXISTING object branch with Relation Parent(INTRANET) > Child(INTERNET) 
		importUnzipped = exportObjBranchUnzipped_onlyPublished;
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
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);

		System.out.println("\n----- Clean Up ImportNode -----");
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);


		System.out.println("\n\n----- separate import NEW branch, only published instances, with RELATION Parent(INTRANET) > Child(INTERNET)! -> keep UUIDs, stored as WORKING VERSIONs ! -----");
		System.out.println("-----  -> KEEP relations \"to outside\", KEEP Parent > Child (always valid because WORKING VERSIONs) -----");
		supertool.importEntities(importNewObjBranchRelationTargetIntranet, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.fetchSubObjects(objImpNodeUuid);
		supertool.setFullOutput(true);
		supertool.fetchObject(newUuid1, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(newUuid2, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.setFullOutput(false);
		supertool.deleteObject(newUuid1, true);

		System.out.println("\n\n----- separate import EXISTING branch, only published instances, with RELATION Parent > Child ! -> new UUIDs, stored as WORKING VERSIONs ! -----");
		System.out.println("-----  -> KEEP relations \"to outside\", MAPPED Parent > Child \"to inside\" (check in database) -----");
		supertool.importEntities(importExistObjBranchRelationParentChild, objImpNodeUuid, addrImpNodeUuid, false, true, true);
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

		System.out.println("\n---------------------------------------------");
		System.out.println("----- IMPORT as CATADMIN (always !) -> QA Permissions -> PUBLISHED ! -----");
		
		System.out.println("\n----- state BEFORE import -----");
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- import branch with WRONG RELATIONS, only published instances -> PUBLISHED cause QA Permission -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- Clean Up: Publish ORIG ONES ! -----");
		supertool.publishObject(objUuidPublishedDoc, true, false);
		supertool.publishObject(objLeafUuidPublishedDoc, true, false);

/*
// IMPORT ONLY POSSIBLE AS CATADMIN ! So we can't switch User !

		System.out.println("\n---------------------------------------------");
		System.out.println("----- IMPORT as USER with NO QA Permissions -> PUBLISH leads to ASSIGNED TO QA ! -----");

		System.out.println("\n\n=============================================");
		System.out.println("----- create new GROUP_NO_QA -----");
		IngridDocument grpNoQADoc = new IngridDocument();
		grpNoQADoc.put(MdekKeys.NAME, "TEST Gruppe2 NO QA");
		grpNoQADoc = supertool.createGroup(grpNoQADoc, true);
		Long grpNoQAId = (Long) grpNoQADoc.get(MdekKeysSecurity.ID);

		System.out.println("\n--- Add Permissions to GROUP_NO_QA -----");
		supertool.addObjPermissionToGroupDoc(grpNoQADoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		grpNoQADoc = supertool.storeGroup(grpNoQADoc, true, false);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR' in 'GROUP_NO_QA' -----");
		IngridDocument usrGrpNoQADoc = new IngridDocument();
		usrGrpNoQADoc.put(MdekKeysSecurity.NAME, "MD_ADMINISTRATOR GROUP_NO_QA name");
		usrGrpNoQADoc.put(MdekKeysSecurity.GIVEN_NAME, "MD_ADMINISTRATOR GROUP_NO_QA given_name");
		usrGrpNoQADoc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{ grpNoQAId });
		usrGrpNoQADoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		usrGrpNoQADoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		usrGrpNoQADoc = supertool.createUser(usrGrpNoQADoc, true);
		Long usrGrpNoQAId = (Long) usrGrpNoQADoc.get(MdekKeysSecurity.IDC_USER_ID);
		String usrGrpNoQAUuid = usrGrpNoQADoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		System.out.println("\n\n----- import branch with WRONG RELATIONS, only published instances -> ASSIGNED TO QA -----");
		supertool.importEntities(importExistBranchWrongRelations, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		supertool.fetchObject(objLeafUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n\n----- Clean Up -----");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- delete group and users -----");
		supertool.deleteGroup(grpNoQAId, true);
		supertool.deleteUser(usrGrpNoQAId);

		System.out.println("\n----- delete object working copies -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);
*/
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
		importUnzipped = importUnzipped.replace("<field-key>additionalField167242", "<field-key>additionalFieldMMMMMMMText");
		// change ID of LIST FIELD
		importUnzipped = importUnzipped.replace("<field-key>additionalField167243", "<field-key>additionalFieldMMMMMMMList");
		System.out.println(importUnzipped);
		byte[] importExistObjWithAdditionalFieldsInvalid_IDs = new byte[0];
		try {
			importExistObjWithAdditionalFieldsInvalid_IDs = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// import data: Object with INVALID additional fields, Wrong ListEntry !
		importUnzipped = exportObjWithAdditionalFieldsUnzipped;
		// change Entry of LIST FIELD
		importUnzipped = importUnzipped.replace("<field-data id=\"1\">Eintrag 1", "<field-data id=\"999\">Eintrag MMMMMMMMM");
		System.out.println(importUnzipped);
		byte[] importExistObjWithAdditionalFieldsInvalid_ListEntry = new byte[0];
		try {
			importExistObjWithAdditionalFieldsInvalid_ListEntry = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import existing object with VALID additional fields, only published instance -> PUBLISHED ! -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsValid, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ID, NAME), only published instance -> PUBLISHED ! NO Check of ADDITIONAL FIELDS in profile, all imported -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_IDs, objImpNodeUuid, addrImpNodeUuid, false, false, false);
		supertool.deleteObjectWorkingCopy(objWithAdditionalFieldsUuid, true);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ListEntry), only published instance -> PUBLISHED ! NO Check of ADDITIONAL FIELDS in profile, all imported -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_ListEntry, objImpNodeUuid, addrImpNodeUuid, true, false, false);
		supertool.deleteObjectWorkingCopy(objWithAdditionalFieldsUuid, true);

		System.out.println("\n----- Clean Up: Publish original one ! -----");
		supertool.publishObject(objWithAdditionalFieldsPublishedDoc, true,false);


		System.out.println("\n\n-------------------------------------");
		System.out.println("----- SEPARATE Import: ADDITIONAL FIELDS -----");
		System.out.println("-------------------------------------");

		System.out.println("\n\n----- separate import existing object with VALID additional fields, only published instance -> underneath import node, WORKING VERSION !, NO Check of ADDITIONAL FIELDS in profile, all imported -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsValid, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

		System.out.println("\n\n----- import existing object with INVALID additional fields (ID, NAME), only published instance -> underneath import node, WORKING VERSION !, NO Check of ADDITIONAL FIELDS in profile, all imported -----");
		supertool.importEntities(importExistObjWithAdditionalFieldsInvalid_IDs, objImpNodeUuid, addrImpNodeUuid, false, true, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.storeObject(objImpNodeDoc, false);

// -----------------------------------

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- Import: MANDATORY DATA -----");
		System.out.println("-------------------------------------");

		// import data: branch with root object missing all MANDATORY FIELDS ! 
		importUnzipped = exportObjBranchUnzipped_onlyPublished;		
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
		// remove all referenced addresses
		boolean doRemove = true;
		while (doRemove) {
			startIndex = importUnzipped.indexOf("<related-address>");
			if (startIndex > 0) {
				endIndex = importUnzipped.indexOf("</related-address>") + 18;
				importUnzipped = importUnzipped.substring(0, startIndex) +
						importUnzipped.substring(endIndex, importUnzipped.length());				
			} else {
				doRemove = false;
			}
		}
		System.out.println(importUnzipped);
		byte[] importExistObjBranchMissingMandatoryFields = new byte[0];
		try {
			importExistObjBranchMissingMandatoryFields = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		// only working instances !
		importUnzipped = importUnzipped.replace("work-state=\"V\"", "work-state=\"B\"");
		byte[] importExistObjBranchMissingMandatoryFields_onlyWorking = new byte[0];
		try {
			importExistObjBranchMissingMandatoryFields_onlyWorking = MdekUtils.compressString(importUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}

		System.out.println("\n\n----- import branch with MISSING MANDATORY DATA, only WORKING instances -> no error, WORKING VERSION -----");
		supertool.importEntities(importExistObjBranchMissingMandatoryFields_onlyWorking, objImpNodeUuid, addrImpNodeUuid, false, false, false);

		System.out.println("\n\n----- import branch with MISSING MANDATORY DATA, only PUBLISHED instances -> root misses data, is stored as WORKING VERSION -----");
		supertool.importEntities(importExistObjBranchMissingMandatoryFields, objImpNodeUuid, addrImpNodeUuid, true, false, false);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteObjectWorkingCopy(objLeafUuid, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("IMPORT MULTIPLE FILES !!!!");
		System.out.println("=========================");

		supertool.setFullOutput(true);

		System.out.println("\n----- export addresses FULL BRANCH UNDER PARENT -----");
		supertool.exportAddressBranch(parentAddrUuid, false, null, false);
		result = supertool.getExportInfo(true);
		byte[] exportAddressBranchZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n----- import multiple files with frontend protocol -----");
		List<byte[]> importList = new ArrayList<byte[]>();
		importList.add(exportObjWithAdditionalFieldsZipped);
		importList.add(exportAddressBranchZipped);
		importList.add(exportObjBranchZipped_onlyPublished);
		importList.add(exportTopObjZipped_onlyPublished);
		String frontendProtocol = "exportObjWithAdditionalFieldsZipped\n\n" +
			"exportAddressBranchZipped\n\n" +
			"exportObjBranchZipped\n\n" +
			"exportTopObjZipped\n\n";
		try {
			supertool.importEntities(importList, objImpNodeUuid, addrImpNodeUuid, false, false, false, frontendProtocol);			
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
