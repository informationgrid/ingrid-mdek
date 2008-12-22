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

		System.out.println("\n----- export objects ONLY PARENT NODE -----");
		supertool.setFullOutput(true);
		supertool.exportObjectBranch(objUuid, true);
		supertool.getExportInfo(true);

		supertool.setFullOutput(false);

		System.out.println("\n----- export objects FULL BRANCH UNDER PARENT -----");
		String exportObjsUnzipped = "";
		try {
			// causes timeout
//			supertool.exportObjectBranch(topObjUuid, false);
//			supertool.getExportInfo(true);

			supertool.exportObjectBranch(objUuid, false);
			// extract XML result !
			IngridDocument result = supertool.getExportInfo(true);
			exportObjsUnzipped = MdekUtils.decompressZippedByteArray((byte[]) result.get(MdekKeys.EXPORT_RESULT));
//			System.out.println(exportObjsUnzipped);

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

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- Import: UPDATE EXISTING OBJECT(S) -----");
		System.out.println("\n-------------------------------------");

		System.out.println("\n----- import as WORKING VERSION -----");
		// first change data to import
		String importObjsUnzipped = exportObjsUnzipped.replace("<title>", "<title>MMTest: ");
		byte[] importObjsZipped = new byte[0];
		try {
			importObjsZipped = MdekUtils.compressString(importObjsUnzipped);						
		} catch (Exception ex) {
			System.out.println(ex);			
		}
		supertool.importEntities(null, "objUuid", "addrUuid", false);

		System.out.println("\n----- import as PUBLISHED -----");
		supertool.importEntities(null, "objUuid", "addrUuid", true);

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
