/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.udk.UtilsCountryCodelist;
import de.ingrid.utils.udk.UtilsLanguageCodelist;

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
        for (int i=0; i<numThreads; i++) {
            if (threads[i].isAlive()) {
                System.out.println( "WARNING: Thread " + i + " STILL ALIVE !!!");                
            }
        }
        MdekCaller.shutdown();
        System.exit( 0 );
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
		
		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		boolean alwaysTrue = true;

		IngridDocument doc;
		List<IngridDocument> docList;

		// NI catalog

		// OBJECTS
		String objTopUuid = "3866463B-B449-11D2-9A86-080000507261";
		// underneath upper top node
		// 3866463B-B449-11D2-9A86-080000507261
		//  38664688-B449-11D2-9A86-080000507261
		//   15C69C20-FE15-11D2-AF34-0060084A4596
		//    2C997C68-2247-11D3-AF51-0060084A4596
		//     C1AA9CA6-772D-11D3-AF92-0060084A4596 // leaf
		String objTopChildUuid = "38664688-B449-11D2-9A86-080000507261";
		String objParentUuid = "15C69C20-FE15-11D2-AF34-0060084A4596";
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		String objLeafUuid = "C1AA9CA6-772D-11D3-AF92-0060084A4596";
		// all further top nodes (5 top nodes at all)
		String objTopUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
		String objTopUuid3 = "38665183-B449-11D2-9A86-080000507261";
		String objTopUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
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

		// Test Syslist stuff ...
		// -----------------------

		supertool.setFullOutput(true);

		supertool.getSysLists(null, null);

		System.out.println("\n----- SysList Values NO language -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, "en");

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
		String catLangShortcut =
			UtilsLanguageCodelist.getShortcutFromCode((Integer)catDoc.get(MdekKeys.LANGUAGE_CODE));
		System.out.println("catalog language=" + catLangShortcut);

		System.out.println("\n----- change CATALOG data -----");
		System.out.println("- change Partner, Provider ...");
		String origNamespace = catDoc.getString(MdekKeys.CATALOG_NAMESPACE);
		String origPartner = catDoc.getString(MdekKeys.PARTNER_NAME);
		String origProvider = catDoc.getString(MdekKeys.PROVIDER_NAME);
		Integer origCountryCode = (Integer) catDoc.get(MdekKeys.COUNTRY_CODE);
		Integer origLanguageCode = (Integer) catDoc.get(MdekKeys.LANGUAGE_CODE);
		catDoc.put(MdekKeys.CATALOG_NAMESPACE, "testNAMESPACE");
		catDoc.put(MdekKeys.PARTNER_NAME, "testPARTNER");
		catDoc.put(MdekKeys.PROVIDER_NAME, "testPROVIDER");
		catDoc.put(MdekKeys.COUNTRY_CODE, UtilsCountryCodelist.getCodeFromShortcut2("GB"));
		catDoc.put(MdekKeys.LANGUAGE_CODE, UtilsLanguageCodelist.getCodeFromShortcut("en"));
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n----- back to orig data of CATALOG -----");
		System.out.println("- change Partner, Provider ...");
		catDoc.put(MdekKeys.CATALOG_NAMESPACE, origNamespace);
		catDoc.put(MdekKeys.PARTNER_NAME, origPartner);
		catDoc.put(MdekKeys.PROVIDER_NAME, origProvider);
		catDoc.put(MdekKeys.COUNTRY_CODE, origCountryCode);
		catDoc.put(MdekKeys.LANGUAGE_CODE, origLanguageCode);
		catDoc = supertool.storeCatalog(catDoc, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SYSLISTS");
		System.out.println("=========================");

		System.out.println("\n----- ALL SysList ids -----");
		supertool.getSysLists(null, null);

		System.out.println("\n----- SysList Values NO language -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, null);

		System.out.println("\n----- SysList Values language: " + catLangShortcut + " -----");
		supertool.getSysLists(new Integer[] { 100, 1100, 1350, 3555}, catLangShortcut);

		System.out.println("\n\n----- new Syslist id=08150815 and load -----");
		supertool.storeSysList(8150815, true, null,
			new Integer[]{null, null, null},
			new String[]{"name1_de", "name2_de", "name3_de"},
			new String[]{null, null, null},
			new String[]{null, null, null});
		supertool.getSysLists(new Integer[] { 8150815 }, null);

		System.out.println("\n----- change Syslist id=08150815 and load -----");
		supertool.storeSysList(8150815, false, 1,
			new Integer[]{1, 2, 3, null},
			new String[]{"NAME1_de", "NAME2_de", "NAME3_de", "NAME4_de"},
			new String[]{"name1_en", "name2_en", null, "name4_en"},
			// NOTICE: Wrong number of data in array ! Is handled in Backend !
			new String[]{null, null, null});
		supertool.getSysLists(new Integer[] { 8150815 }, null);

		System.out.println("\n----- change Syslist id=08150815 (remove all) and load -----");
		supertool.storeSysList(8150815, false, null,
			new Integer[]{},
			new String[]{},
			new String[]{},
			new String[]{});
		supertool.getSysLists(new Integer[] { 8150815 }, null);


		System.out.println("\n\n=========================");
		System.out.println("SYSLISTS REBUILD FROM IGE CODELIST ADMINISTRATION !");
		System.out.println("=========================");

		System.out.println("\n----- STORE 1. new Object with syslist LEGIST(1350) entries -----");
		IngridDocument newDoc = supertool.newObjectDoc(null);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT STORED (NOT Published)");
		// add entry to LEGISLATIONS
		docList = new ArrayList<IngridDocument>();
		IngridDocument tmpDoc = new IngridDocument();
		tmpDoc.put(MdekKeys.LEGISLATION_KEY, 35);
		docList.add(tmpDoc);
		tmpDoc = new IngridDocument();
		tmpDoc.put(MdekKeys.LEGISLATION_KEY, 49);
		docList.add(tmpDoc);
		newDoc.put(MdekKeys.LEGISLATIONS, docList);
		doc = supertool.storeObject(newDoc, true);
		String newObjStoredUuid = (String) doc.get(MdekKeys.UUID);
		
		System.out.println("\n----- STORE 2. new Object with same syslist LEGIST(1350) entries -----");
		doc = supertool.storeObject(newDoc, true);
		String newObjStoredUuid2 = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- load, change and store Syslist LEGIST(1350) -----");
		int syslistId = MdekUtils.MdekSysList.LEGIST.getDbValue();
		// pass language null, then catalog language !
		doc = supertool.getSysLists(new Integer[] { syslistId }, null);
		IngridDocument syslistDoc = (IngridDocument) doc.get(MdekKeys.SYS_LIST_KEY_PREFIX + syslistId);
		Integer[] entryIds = (Integer[]) syslistDoc.get(MdekKeys.LST_ENTRY_IDS);
		String[] entryNames_de = (String[]) syslistDoc.get(MdekKeys.LST_ENTRY_NAMES);

		System.out.println("- remove entry 49");
		ArrayList<Integer> idList = new ArrayList<Integer>(Arrays.asList(entryIds));
		ArrayList<String> nameList_de = new ArrayList<String>(Arrays.asList(entryNames_de));
		int index = idList.indexOf(49);
		idList.remove(index);
		String removedName = nameList_de.get(index);
		nameList_de.remove(index);
		System.out.println("- change entry 35");
		index = idList.indexOf(35);
		String changedNameOrig = nameList_de.get(index);
		String changedName = "MM UPDATED ENTRY !";
		nameList_de.set(index, changedName);	
		supertool.storeSysList(syslistId, true, null,
				idList.toArray(new Integer[idList.size()]),
				nameList_de.toArray(new String[nameList_de.size()]),
				null,
				null);

		System.out.println("\n----- verify: changed Syslist LEGIST(1350) -----");
		doc = supertool.getSysLists(new Integer[] { syslistId }, null);

		System.out.println("\n----- verify: query Index FORMER syslist values ! -> ALL THERE -----");
		supertool.queryObjectsFullText(removedName, 0, 5);
		supertool.queryObjectsFullText(changedNameOrig, 0, 5);

		System.out.println("\n----- verify: query Index NEW syslist values ! -> NOT THERE -----");
		supertool.queryObjectsFullText(changedName, 0, 5);

		System.out.println("\n----- fetch and store 1. Object -> new syslist entries: empty and changed syslist entry ! -----");
		doc = supertool.fetchObject(newObjStoredUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.storeObject(doc, true);

		System.out.println("\n----- fetch 2. Object -> still old wrong values ! -----");
		doc = supertool.fetchObject(newObjStoredUuid2, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- start REBUILD job -----");
		System.out.println("NO TAKES TOOOO LONG !!! UNCOMMENT IF NEEDED !!!!!");

		try {
			// causes timeout
			supertool.rebuildSyslistData();

		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.REBUILD_SYSLISTS);
				supertool.sleep(3000);
			}
		}

		System.out.println("\n----- verify: query Index FORMER syslist VALUES ! -> NOT THERE OR entities with DIFFERENT syslist entry with same content (+ §42) -----");
		supertool.queryObjectsFullText(removedName, 0, 5);
		supertool.queryObjectsFullText(changedNameOrig, 0, 5);

		System.out.println("\n----- verify: query Index NEW syslist values ! -> ALL THERE -----");
		supertool.queryObjectsFullText(changedName, 0, 5);

		System.out.println("\n----- fetch 2. Object -> new syslist entries: empty and changed syslist entry ! -----");
		doc = supertool.fetchObject(newObjStoredUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObject(newObjStoredUuid, true);
		supertool.deleteObject(newObjStoredUuid2, true);
		supertool.storeSysList(syslistId, true, null,
				entryIds,
				entryNames_de,
				null,
				// NOTICE: null Data ! Is handled in Backend !
				null);

		System.out.println("\n\n=========================");
		System.out.println("SYSLISTS REBUILD FROM REPO");
		System.out.println("=========================");

		System.out.println("\n\n----- store new Syslist id=08150815 (in structure from repo) and load different languages -----");
		List<IngridDocument> newSyslists = new ArrayList<IngridDocument>();
		IngridDocument newSyslist = new IngridDocument();
		newSyslists.add(newSyslist);
		newSyslist.put(MdekKeys.LST_ID, new Integer(8150815));
		newSyslist.put(MdekKeys.LST_NAME, "syslist Test 08150815");
		newSyslist.put(MdekKeys.LST_DESCRIPTION, "syslist Test 08150815 DESCRIPTION");
		newSyslist.put(MdekKeys.LST_MAINTAINABLE, false);
		newSyslist.put(MdekKeys.LST_DEFAULT_ENTRY_ID, 1);
		IngridDocument[] entries = new IngridDocument[2];
		entries[0] = new IngridDocument();
		entries[0].put(MdekKeys.LST_ENTRY_ID, new Integer(1));
		entries[0].put(MdekKeys.LST_ENTRY_DESCRIPTION, "111 entry description");
		IngridDocument localNamesEntry1 = new IngridDocument();
		entries[0].put(MdekKeys.LST_LOCALISED_ENTRY_NAME_MAP, localNamesEntry1);
		localNamesEntry1.put("de", "111 Eintrag DE");
		localNamesEntry1.put("en", "111 entry EN");
		localNamesEntry1.put("es", "111 primero ES");
		entries[1] = new IngridDocument();
		entries[1].put(MdekKeys.LST_ENTRY_ID, new Integer(2));
		entries[1].put(MdekKeys.LST_ENTRY_DESCRIPTION, "222 entry description");
		IngridDocument localNamesEntry2 = new IngridDocument();
		entries[1].put(MdekKeys.LST_LOCALISED_ENTRY_NAME_MAP, localNamesEntry2);
		localNamesEntry2.put("de", "222 Eintrag DE");
		localNamesEntry2.put("en", "222 entry EN");
		localNamesEntry2.put("es", "222 segundo ES");
		newSyslist.put(MdekKeys.LST_ENTRIES, entries);
		supertool.storeSysLists(newSyslists);
		supertool.getSysLists(new Integer[] { 8150815 }, "de");
		supertool.getSysLists(new Integer[] { 8150815 }, "en");
		supertool.getSysLists(new Integer[] { 8150815 }, "es");
		System.out.println("\n\n----- load with non existing language -----");
		supertool.getSysLists(new Integer[] { 8150815 }, "fr");
		System.out.println("\n\n----- load with null language -> uses catalog language (\"de\") -----");
		supertool.getSysLists(new Integer[] { 8150815 }, null);

		System.out.println("\n\n----- store AGAIN with additional language \"fr\", remove language \"es\" (process existing and add new values) and load -----");
		localNamesEntry1.put("fr", "111 un FR");
		localNamesEntry1.remove("es");
		localNamesEntry2.put("fr", "222 deuxieme FR");
		localNamesEntry2.remove("es");
		supertool.storeSysLists(newSyslists);
		supertool.getSysLists(new Integer[] { 8150815 }, "de");
		supertool.getSysLists(new Integer[] { 8150815 }, "en");
		supertool.getSysLists(new Integer[] { 8150815 }, "es");
		supertool.getSysLists(new Integer[] { 8150815 }, "fr");

		System.out.println("\n----- Clean Up: Change Syslist id=08150815 (remove all) and load -----");
		localNamesEntry1.clear();
		localNamesEntry2.clear();
		supertool.storeSysLists(newSyslists);
		supertool.getSysLists(new Integer[] { 8150815 }, null);

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
		System.out.println("ANALYZE DB (Consistency Check)");
		System.out.println("=========================");

		try {
			// may cause timeout
			supertool.analyze();

		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.ANALYZE);
				supertool.sleep(3000);
			}
		}

// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("DELETE ADDRESS (Gesamtkatalogmanagement)");
		System.out.println("=========================");

		System.out.println("\n----- PUBLISH new 'Address to replace' (will be Ansprechpartner, RESPONSIBLE USER) -----");
		doc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		doc.put(MdekKeys.ORGANISATION, "testADDRESS_TO_REPLACE");
		doc = supertool.publishAddress(doc, true, false);
		String uuidToReplace = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE new SUB-ADDRESS of 'Address to replace' -----");
		doc = supertool.newAddressDoc(uuidToReplace, AddressType.INSTITUTION);
		doc.put(MdekKeys.ORGANISATION, "testSUBADDRESS");
		doc = supertool.storeAddress(doc, true);
		String uuidSubaddress = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE 'ADDRESS to replace with'. NOTICE: 'Address to replace' is RESPONSIBLE USER -----");
		newDoc = supertool.newAddressDoc(null, AddressType.INSTITUTION);
		newDoc.put(MdekKeys.ORGANISATION, "testADDRESS_TO_REPLACE_WITH");
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.publishAddress(newDoc, true, false);
		String uuidToReplaceWith = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE 1. new Object where 'Address to replace' is Ansprechpartner, Ressourcenanbieter and RESPONSIBLE USER -----");
		System.out.println("----- !!! also add 'ADDRESS to replace with' as Ansprechpartner !!! to check whether same Ansprechpartner exists only ONCE !!! -----");
		newDoc = supertool.newObjectDoc(null);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT STORED (NOT Published)");
		List<IngridDocument> objAdrDocs = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.ADR_REFERENCES_TO, objAdrDocs);
		// 'Address to replace'-Ansprechpartner
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		// 'Address to replace with'-Ansprechpartner
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplaceWith);
		doc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		// Ressourcenanbieter
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, 1);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		// responsible user
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.storeObject(newDoc, true);
		newObjStoredUuid = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- PUBLISH 2. new Object where 'Address to replace' is Ansprechpartner, Ressourcenanbieter and RESPONSIBLE USER -----");
		newDoc = supertool.newObjectDoc(null);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT PUBLISHED");
		objAdrDocs = new ArrayList<IngridDocument>();
		newDoc.put(MdekKeys.ADR_REFERENCES_TO, objAdrDocs);
		// 'Address to replace'-Ansprechpartner
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		// Ressourcenanbieter
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		doc.put(MdekKeys.RELATION_TYPE_ID, 1);
		doc.put(MdekKeys.RELATION_TYPE_REF, 505);
		objAdrDocs.add(doc);
		// responsible user
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, uuidToReplace);
		newDoc.put(MdekKeys.RESPONSIBLE_USER, doc);
		doc = supertool.publishObject(newDoc, true, false);
		String newObjPublishedUuid = (String) doc.get(MdekKeys.UUID);

		System.out.println("\n----- getObjectsOf Ansprechpartner Address -> 2 Objects -----");
		supertool.getObjectsOfAddressByType(uuidToReplace, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.OBJECTS_OF_ADDRESS, uuidToReplace);
		
		System.out.println("\n----- getObjectsOfResponsibleUser -> 2 Objects -----");
		supertool.getObjectsOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.OBJECTS_OF_RESPONSIBLE_USER, uuidToReplace);
		
		System.out.println("\n----- getAddressesOfResponsibleUser -> 1 Address -----");
		supertool.getAddressesOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- according CSV data -----");
		supertool.getCsvData(CsvRequestType.ADDRESSES_OF_RESPONSIBLE_USER, uuidToReplace);
		
		System.out.println("\n----- DELETE Address to replace -> Error: ENTITY_REFERENCED_BY_OBJ -----");
		supertool.deleteAddress(uuidToReplace, false);


		System.out.println("\n\n----- REPLACE CATADMIN address -> Error: ADDRESS_IS_IDCUSER_ADDRESS -----");
		supertool.replaceAddress(catalogAdminUuid, uuidToReplaceWith);
		System.out.println("\n----- REPLACE with equal Addresses -> Error: FROM_UUID_EQUALS_TO_UUID -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplace);

		System.out.println("\n----- REPLACE PUBLISHED with UNPUBLISHED new Address -> Error: ENTITY_NOT_PUBLISHED -----");
		supertool.replaceAddress(uuidToReplaceWith, uuidSubaddress);
		
		System.out.println("\n----- REPLACE address has subnodes -> Error: NODE_HAS_SUBNODES -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplaceWith);

		System.out.println("\n----- DELETE Subaddress of 'Address to replace', so replace works ! -----");
		supertool.deleteAddress(uuidSubaddress, false);


		System.out.println("\n\n----- REPLACE 'Address to replace' with 'Address to replace with' \n" +
				"          -> all Addresses are replaced, all responsible users set to catadmin -----");
		supertool.replaceAddress(uuidToReplace, uuidToReplaceWith);

		System.out.println("\n----- getObjectsOf Ansprechpartner Address with OLD Ansprechpartner-> 0 Objects -----");
		supertool.getObjectsOfAddressByType(uuidToReplace, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID, null);
		System.out.println("\n----- getObjectsOf Ansprechpartner Address with NEW Ansprechpartner-> 2 Objects -----");
		supertool.getObjectsOfAddressByType(uuidToReplaceWith, MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID, null);
		
		System.out.println("\n----- getObjectsOfResponsibleUser with OLD responsible User -> 0 Objects -----");
		supertool.getObjectsOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- getObjectsOfResponsibleUser with NEW responsible User (=catadmin) -> LOTs of objects -----");
		supertool.getObjectsOfResponsibleUser(catalogAdminUuid, 50);
		
		System.out.println("\n----- getAddressesOfResponsibleUser with OLD responsible User -> 0 Addresses -----");
		supertool.getAddressesOfResponsibleUser(uuidToReplace, null);
		System.out.println("\n----- getAddressesOfResponsibleUser with NEW responsible User (=catadmin) -> LOTs of addresses -----");
		supertool.getAddressesOfResponsibleUser(catalogAdminUuid, 50);

		System.out.println("\n----- verify: ONLY ONE NEW Ansprechpartner, 'Address to replace' in all associations replaced ! -----");
		supertool.fetchObject(newObjStoredUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchObject(newObjPublishedUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n\n----- Clean Up -----");
		supertool.deleteObject(newObjStoredUuid, true);
		supertool.deleteObject(newObjPublishedUuid, true);
		supertool.deleteAddress(uuidToReplaceWith, true);
		System.out.println("\n----- 'Address to replace' may be already deleted -----");
		supertool.deleteAddress(uuidToReplace, true);

// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("REPLACE FREE LIST ENTRIES");
		System.out.println("=========================");

		System.out.println("\n----- get sys list entries -----");
		supertool.getSysLists(new Integer[]{MdekSysList.LEGIST.getDbValue()}, null);

		System.out.println("\n----- get free list entries -----");
		supertool.getFreeListEntries(MdekSysList.LEGIST);

		System.out.println("\n----- replace free list entry -----");
		supertool.replaceFreeEntryWithSyslistEntry("Nds. Deichgesetz (NDG)",
			MdekSysList.LEGIST, 39, "Nieders. Deichgesetz (NDG)");

		System.out.println("\n----- get free list entries (former entry missing, was replaced !) -----");
		supertool.getFreeListEntries(MdekSysList.LEGIST);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("SNS SpatialReferences Update");
		System.out.println("=========================");

		System.out.println("\n----- validate: get spatial references different type(s) -----");
		supertool.getSpatialReferences(new SpatialReferenceType[]{ SpatialReferenceType.FREI });
		supertool.getSpatialReferences(new SpatialReferenceType[]{ SpatialReferenceType.GEO_THESAURUS });
		supertool.getSpatialReferences(new SpatialReferenceType[]{ SpatialReferenceType.GEO_THESAURUS, SpatialReferenceType.FREI });
		supertool.getSpatialReferences(null);

		String objUuidWithSpatRefs = "7AC6048A-7018-11D3-A599-C70A0FEBD4FC"; // 8 Geo, 1 Free
//		String objUuidWithSpatRefs = "3A295152-5091-11D3-AE6C-00104B57C66D"; // 1 Geo, 1 Free (Göttingen)
//		String objUuidWithSpatRefs = "E13A483B-4FAB-11D3-AE6B-00104B57C66D"; // 1 Geo (Niedersachsen)

		System.out.println("\n----- before SNS UPDATE: validate spatial refs of object -----");
		doc = supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);
		List<IngridDocument> locationDocsMixed = (List<IngridDocument>) doc.get(MdekKeys.LOCATIONS);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to GEOTHESAURUS, SAME SNS-ID -----");
		System.out.println("----- = Keep all records, Update data -----");
		System.out.println("\n----- WITHOUT ADDITIONAL LOCATIONS IN NEW LOCATION ! (NO SUCCESSORS!!!) -----");
		
		// set up changed list from former mixed list !
		List<IngridDocument> locationDocsChanged = new ArrayList<IngridDocument>(locationDocsMixed.size());
		for (IngridDocument locationDocMixed : locationDocsMixed) {
			IngridDocument locationDocChanged = new IngridDocument();
			locationDocChanged.putAll(locationDocMixed);
			locationDocChanged.put(MdekKeys.LOCATION_NAME, "MMTEST1_" + locationDocChanged.getString(MdekKeys.LOCATION_NAME));
			locationDocChanged.put(MdekKeys.NORTH_BOUNDING_COORDINATE, 123.99);
			locationDocsChanged.add(locationDocChanged);
		}
		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsMixed, locationDocsChanged);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to GEOTHESAURUS, DIFFERENT SNS-ID -----");
		System.out.println("----- = Delete SpatialRef, Use New/Existing one -----");
		System.out.println("\n----- WITHOUT ADDITIONAL LOCATIONS IN NEW LOCATION ! (NO SUCCESSORS!!!) -----");
		
		// set up changed list from former mixed list !
		List<IngridDocument> locationDocsIdChanged = new ArrayList<IngridDocument>(locationDocsMixed.size());
		for (IngridDocument locationDocMixed : locationDocsMixed) {
			IngridDocument locationDocIdChanged = new IngridDocument();
			locationDocIdChanged.putAll(locationDocMixed);
			locationDocIdChanged.put(MdekKeys.LOCATION_SNS_ID, "MM_" + locationDocMixed.getString(MdekKeys.LOCATION_SNS_ID));
			locationDocsIdChanged.add(locationDocIdChanged);
		}
		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsChanged, locationDocsIdChanged);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to EXPIRED -----");
		System.out.println("\n----- WITHOUT ADDITIONAL LOCATIONS IN NEW LOCATION ! (NO SUCCESSORS!!!) -----");
		System.out.println("----- OLD LOCATIONS WILL BE EXPIRED (due to no successors) ! -----");

		// new spatial refs are all null !
		List<IngridDocument> locationDocsNull = Collections.nCopies(locationDocsMixed.size(), null);
		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsIdChanged, locationDocsNull);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object (now expired !) -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to GEOTHESAURUS, SAME SNS-ID -----");
		System.out.println("----- = Keep all records, Update data -----");
		System.out.println("\n----- WITH ADDITIONAL LOCATIONS IN NEW LOCATION ! (SUCCESSORS!!!) -----");
		System.out.println("----- OLD LOCATIONS WILL BE UPDATED AND SUCCESSORS ARE ADDED ! -----");

		// set up changed list WITH SUCCESSORS !
		List<IngridDocument> locationDocsWithSuccessors = new ArrayList<IngridDocument>(locationDocsIdChanged.size());
		for (int i=0; i < locationDocsIdChanged.size(); i++) {
			IngridDocument locationDocWithSucc = new IngridDocument();
			locationDocWithSucc.putAll(locationDocsIdChanged.get(i));

			List<IngridDocument> successorList = new ArrayList<IngridDocument>(2);
			IngridDocument successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "SAMESNSID_loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "SAMESNSID_loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);
			successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "SAMESNSID_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "SAMESNSID_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);

			locationDocWithSucc.put(MdekKeys.SUCCESSORS, successorList);
			locationDocsWithSuccessors.add(locationDocWithSucc);
		}
		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsIdChanged, locationDocsWithSuccessors);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to GEOTHESAURUS, DIFFERENT SNS-ID -----");
		System.out.println("----- = Delete SpatialRef, Create new one -----");
		System.out.println("\n----- WITH ADDITIONAL LOCATIONS IN NEW LOCATION ! (SUCCESSORS!!!) -----");
		System.out.println("----- OLD LOCATIONS WILL BE DELETED, NEW ONES CREATED AND SUCCESSORS ARE ADDED ! -----");
		
		// set up changed list WITH SUCCESSORS !
		List<IngridDocument> locationDocsWithSuccessors2 = new ArrayList<IngridDocument>(locationDocsMixed.size());
		for (int i=0; i < locationDocsMixed.size(); i++) {
			IngridDocument locationDocWithSucc = new IngridDocument();
			locationDocWithSucc.putAll(locationDocsIdChanged.get(i));
			locationDocWithSucc.put(MdekKeys.LOCATION_SNS_ID, "NEWSNSID_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));

			List<IngridDocument> successorList = new ArrayList<IngridDocument>(2);
			IngridDocument successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "NEWSNSID_loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "NEWSNSID_loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);
			successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "NEWSNSID_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "NEWSNSID_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);

			locationDocWithSucc.put(MdekKeys.SUCCESSORS, successorList);
			locationDocsWithSuccessors2.add(locationDocWithSucc);
		}

		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsWithSuccessors, locationDocsWithSuccessors2);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS SpatialReferences UPDATE: GEOTHESAURUS to EXPIRED -----");
		System.out.println("\n----- WITH ADDITIONAL LOCATIONS IN NEW LOCATION ! (SUCCESSORS!!!) -----");
		System.out.println("----- OLD LOCATIONS (expired ones) WILL BE DELETED DUE TO SUCCESSORS (NO EXPIRED) ! SUCCESSORS ARE ADDED ! -----");

		// set up expired list WITH SUCCESSORS !
		List<IngridDocument> locationDocsExpiredWithSuccessors = new ArrayList<IngridDocument>(locationDocsMixed.size());
		for (int i=0; i < locationDocsMixed.size(); i++) {
			IngridDocument locationDocWithSucc = new IngridDocument();
			locationDocWithSucc.put(MdekKeys.LOCATION_EXPIRED_AT, MdekUtils.dateToTimestamp(new Date()));

			List<IngridDocument> successorList = new ArrayList<IngridDocument>(2);
			IngridDocument successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "EXPIRED__loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "EXPIRED_loc" + i + "_SUCCESSOR1_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);
			successorDoc = new IngridDocument();
			successorDoc.putAll(locationDocsMixed.get(i));
			successorDoc.put(MdekKeys.LOCATION_NAME, "EXPIRED_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_NAME));
			successorDoc.put(MdekKeys.LOCATION_SNS_ID, "EXPIRED_loc" + i + "_SUCCESSOR2_" + locationDocsMixed.get(i).getString(MdekKeys.LOCATION_SNS_ID));
			successorList.add(successorDoc);

			locationDocWithSucc.put(MdekKeys.SUCCESSORS, successorList);
			locationDocsExpiredWithSuccessors.add(locationDocWithSucc);
		}

		try {
			// causes timeout ?
			supertool.updateSpatialReferences(locationDocsWithSuccessors2, locationDocsExpiredWithSuccessors);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SPATIAL_REFERENCES);

		System.out.println("\n----- after SNS UPDATE: validate spatial refs of object (now expired !) -----");
		supertool.fetchObject(objUuidWithSpatRefs, FetchQuantity.EDITOR_ENTITY);

// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println("SNS Searchterms Update");
		System.out.println("=========================");

		System.out.println("\n----- validate: get searchterms of different type(s) -----");
		supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.FREI });
		supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.GEMET });
		supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.UMTHES });
		supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.INSPIRE });
		supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.INSPIRE, SearchtermType.UMTHES });
		supertool.getSearchTerms(null);

		System.out.println("\n----- before SNS UPDATE: validate searchterms of object -----");
		doc = supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		List<IngridDocument> termDocsMixed = (List<IngridDocument>) doc.get(MdekKeys.SUBJECT_TERMS);

		System.out.println("\n----- before SNS UPDATE: validate searchterms of address -----");
		doc = supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);
		termDocsMixed.addAll((List<IngridDocument>) doc.get(MdekKeys.SUBJECT_TERMS));

//		System.out.println("\n----- UPDATE: all UMTHES !!! -----");
//		doc = supertool.getSearchTerms(new SearchtermType[]{ SearchtermType.UMTHES });
//		termDocsMixed = (List<IngridDocument>) doc.get(MdekKeys.SUBJECT_TERMS);

		List<IngridDocument> termDocsMixed_newName = termDocsMixed;

		// -----------------------------------
/* COMMENTED VERY TIME CONSUMING !
		System.out.println("\n\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: THESAURUS to FREE -----");

		// new terms are all null !
		List<IngridDocument> termDocsNull = Collections.nCopies(termDocsMixed.size(), null);
		try {
			// causes timeout ?
			supertool.updateSearchTerms(termDocsMixed, termDocsNull);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);

		System.out.println("\n----- after SNS UPDATE: validate searchterms of object -----");
		supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- after SNS UPDATE: validate searchterms of address -----");
		supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: FREE to THESAURUS -----");
		
		// set up free list from former mixed list !
		List<IngridDocument> termDocsFree = new ArrayList<IngridDocument>(termDocsMixed.size());
		for (IngridDocument termDocMixed : termDocsMixed) {
			IngridDocument termDocFree = new IngridDocument();
			termDocFree.putAll(termDocMixed);
			termDocFree.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
			termDocsFree.add(termDocFree);
		}

		// --------------------------------------------------
		// ONLY USE ONE OF THE following FREE to THESAURUS UPDATES !!!!
		// --------------------------------------------------

		// ------------- 1. Option --------------------------

//		System.out.println("\n=========================");
//		System.out.println("----- SNS Searchterms UPDATE: FREE to THESAURUS, same Name -> Replace FREE -----");
//		termDocsMixed_newName = termDocsMixed;

		// ------------- 2. Option --------------------------

		System.out.println("\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: FREE to THESAURUS, DIFFERENT Name -> Keep FREE, Add Thesaurus -----");
		
		// set up list with NEW Name from former mixed list !
		termDocsMixed_newName = new ArrayList<IngridDocument>(termDocsMixed.size());
		for (IngridDocument termDocMixed : termDocsMixed) {
			IngridDocument termDocNewName = new IngridDocument();
			termDocNewName.putAll(termDocMixed);
			if (SearchtermType.isThesaurusType(
					EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termDocNewName.getString(MdekKeys.TERM_TYPE)))) {
				termDocNewName.put(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue());				
			}
			termDocNewName.put(MdekKeys.TERM_NAME, "MMTEST1_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocNewName.put(MdekKeys.TERM_GEMET_ID, "MMTEST1_GEMETID");
			termDocNewName.put(MdekKeys.TERM_ALTERNATE_NAME, "MMTEST1_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocsMixed_newName.add(termDocNewName);
		}

		// ------------- End Option --------------------------

		try {
			// causes timeout ?
			supertool.updateSearchTerms(termDocsFree, termDocsMixed_newName);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);

		System.out.println("\n----- after SNS UPDATE: validate searchterms of object -----");
		supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- after SNS UPDATE: validate searchterms of address -----");
		supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);
*/
		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: THESAURUS to THESAURUS, NEW NAME, SAME SNS-ID -----");
		System.out.println("----- = Keep all records, Update data -----");
		
		// set up free list from former mixed list !
		List<IngridDocument> termDocsMixed_newName2 = new ArrayList<IngridDocument>(termDocsMixed.size());
		for (IngridDocument termDocMixed : termDocsMixed) {
			IngridDocument termDocNewName = new IngridDocument();
			termDocNewName.putAll(termDocMixed);
			if (SearchtermType.isThesaurusType(
					EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termDocNewName.getString(MdekKeys.TERM_TYPE)))) {
				termDocNewName.put(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue());				
			}
			termDocNewName.put(MdekKeys.TERM_NAME, "MMTEST2_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocNewName.put(MdekKeys.TERM_GEMET_ID, "MMTEST2_GEMETID");
			termDocNewName.put(MdekKeys.TERM_ALTERNATE_NAME, "MMTEST2_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocsMixed_newName2.add(termDocNewName);
		}
		try {
			// causes timeout ?
			supertool.updateSearchTerms(termDocsMixed_newName, termDocsMixed_newName2);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);

		System.out.println("\n----- after SNS UPDATE: validate searchterms of object -----");
		supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- after SNS UPDATE: validate searchterms of address -----");
		supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: THESAURUS to THESAURUS, SAME NAME, NEW SNS-ID -----");
		
		// set up free list from former mixed list !
		List<IngridDocument> termDocsMixed_newSnsId = new ArrayList<IngridDocument>(termDocsMixed.size());
		for (IngridDocument termDocMixed : termDocsMixed_newName2) {
			IngridDocument termDocNewSnSId = new IngridDocument();
			termDocNewSnSId.putAll(termDocMixed);
			if (SearchtermType.isThesaurusType(
					EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termDocNewSnSId.getString(MdekKeys.TERM_TYPE)))) {
				termDocNewSnSId.put(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue());				
			}
			termDocNewSnSId.put(MdekKeys.TERM_SNS_ID, "MMTEST3_" + termDocMixed.getString(MdekKeys.TERM_SNS_ID));
			// NAME unchanged !
//			termDocNewSnSId.put(MdekKeys.TERM_NAME, "MMTEST3_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocNewSnSId.put(MdekKeys.TERM_GEMET_ID, "MMTEST3_GEMETID");
			termDocNewSnSId.put(MdekKeys.TERM_ALTERNATE_NAME, "MMTEST3_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocsMixed_newSnsId.add(termDocNewSnSId);
		}
		try {
			// causes timeout ?
			supertool.updateSearchTerms(termDocsMixed_newName2, termDocsMixed_newSnsId);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);

		System.out.println("\n----- after SNS UPDATE: validate searchterms of object -----");
		supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- after SNS UPDATE: validate searchterms of address -----");
		supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
/* COMMENTED VERY TIME CONSUMING !
		System.out.println("\n\n=========================");
		System.out.println("----- SNS Searchterms UPDATE: THESAURUS to THESAURUS, NEW NAME, NEW SNS-ID -----");
		System.out.println("----- = New free records with old thesaurus term ! -----");
		System.out.println("----- = Update thesaurus term -----");
		
		// set up free list from former mixed list !
		List<IngridDocument> termDocsMixed_newSnsIdNewName = new ArrayList<IngridDocument>(termDocsMixed.size());
		for (IngridDocument termDocMixed : termDocsMixed) {
			IngridDocument termDocNewSnSIdNewName = new IngridDocument();
			termDocNewSnSIdNewName.putAll(termDocMixed);
			if (SearchtermType.isThesaurusType(
					EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termDocNewSnSIdNewName.getString(MdekKeys.TERM_TYPE)))) {
				termDocNewSnSIdNewName.put(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue());				
			}
			termDocNewSnSIdNewName.put(MdekKeys.TERM_SNS_ID, "MMTEST4_" + termDocMixed.getString(MdekKeys.TERM_SNS_ID));
			termDocNewSnSIdNewName.put(MdekKeys.TERM_NAME, "MMTEST4_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocNewSnSIdNewName.put(MdekKeys.TERM_GEMET_ID, "MMTEST4_GEMETID");
			termDocNewSnSIdNewName.put(MdekKeys.TERM_ALTERNATE_NAME, "MMTEST4_" + termDocMixed.getString(MdekKeys.TERM_NAME));
			termDocsMixed_newSnsIdNewName.add(termDocNewSnSIdNewName);
		}
		try {
			// causes timeout ?
			supertool.updateSearchTerms(termDocsMixed_newSnsId, termDocsMixed_newSnsIdNewName);
		} catch(Exception ex) {
			// track job info if still running !
			while (supertool.hasRunningJob()) {
				// extracted from running job info if still running
				supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);
				supertool.sleep(4000);
			}
		}
		System.out.println("\n----- after SNS UPDATE: get JobInfo -----");
		supertool.getJobInfo(JobType.UPDATE_SEARCHTERMS);

		System.out.println("\n----- after SNS UPDATE: validate searchterms of object -----");
		supertool.fetchObject(objTopChildUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- after SNS UPDATE: validate searchterms of address -----");
		supertool.fetchAddress(personAddrUuid, FetchQuantity.EDITOR_ENTITY);
*/
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
