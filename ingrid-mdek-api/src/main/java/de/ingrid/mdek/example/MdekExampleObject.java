package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.udk.UtilsLanguageCodelist;

public class MdekExampleObject {

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
		MdekExampleObjectThread[] threads = new MdekExampleObjectThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleObjectThread(i+1);
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
	}
}

class MdekExampleObjectThread extends Thread {

	private int threadNumber;	
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleObjectThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
	}

	public void run() {
		isRunning = true;

		supertool.setFullOutput(true);

		long exampleStartTime = System.currentTimeMillis();

		// TOP OBJECT
//		String parentUuid = "3866463B-B449-11D2-9A86-080000507261";
		// parent of object 2C997C68-2247-11D3-AF51-0060084A4596 -> 343 Subobjects
		String parentUuid = "15C69C20-FE15-11D2-AF34-0060084A4596";
		// Obj/Adr Refs, Spatial Refs + URL Refs
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		// Obj/Adr Refs
//		String objUuid = "5CE671D3-5475-11D3-A172-08002B9A1D1D";
		// Spatial Refs
//		String objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";
		// URL Refs
//		String objUuid = "43D34D1A-55BA-11D6-8840-0000F4ABB4D8";
		// Spatial Refs + URL Refs + 180 kids !
//		String objUuid = "15C69C29-FE15-11D2-AF34-0060084A4596";
		
		IngridDocument oMap;
		IngridDocument newObjDoc;
		String newObjUuid;

		//System.out.println("\n###### INVOKE testMdekEntity ######");
		//mdekCaller.testMdekEntity(threadNumber);

		boolean alwaysTrue = true;

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		IngridDocument doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

// ====================
// test single stuff
// -----------------------------------
/*
		// Test / Fehler beim Laden Objekt ?
		
		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
//		oMap = supertool.fetchObject("38664938-B449-11D2-9A86-080000507261", Quantity.DETAIL_ENTITY);
//		oMap = supertool.fetchObject("909581C0-9540-4480-A2A3-F93D43ACC20C", Quantity.DETAIL_ENTITY);
//		oMap = supertool.fetchObject("087855F8-9A52-11D6-BE8E-00D0B783B9AA", Quantity.DETAIL_ENTITY);
		// ST, multiple SNS searchterm values, see http://jira.101tec.com/browse/INGRIDII-283
		objUuid = "0099B9A8-C10C-4DC8-A26C-A886EE7A5E92";
		oMap = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- test store -----");
		oMap = supertool.storeObject(oMap, true);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, false);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// track server job !
		// ------------------
		boolean timeout = false;
		try {
			// copy 344 objects !!!
			supertool.copyObject("15C69C20-FE15-11D2-AF34-0060084A4596", null, true);

			// copy ~50 objects
//			supertool.copyObject("38665183-B449-11D2-9A86-080000507261", "3892B136-D1F3-4E45-9E5F-E1CEF117AA74", true);

		} catch(Exception ex) {
			timeout = true;
		}

		// once again -> ERROR: job running
		supertool.copyObject("15C69C20-FE15-11D2-AF34-0060084A4596", null, true);

		if (timeout) {
			// also cancels Running Job !
			supertool.trackRunningJob(3000, true);
		}

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// EH CACHE BUG !!! referenced Adress Node not fetched in refetch of 2. store !
		// ------------------
		for (int i=0; i< 1; i++) {
		
			System.out.println("\n----- load 79297FDD-729B-4BC5-BF40-C1F3FB53D2F2 -----");
			oMap = supertool.fetchObject("79297FDD-729B-4BC5-BF40-C1F3FB53D2F2", Quantity.DETAIL_ENTITY);
	
			System.out.println("\n----- store 79297FDD-729B-4BC5-BF40-C1F3FB53D2F2 -----");
			oMap = supertool.storeObject(oMap, true);
			
			if (oMap.get(MdekKeys.ADR_REFERENCES_TO) == null || ((List)oMap.get(MdekKeys.ADR_REFERENCES_TO)).size() == 0) {
				System.out.println("\n----- MISSING ADDRESS IN MAPPED DATA, EXITING TEST!! -----");
				break;
			}
			if (((HashMap)((List)oMap.get(MdekKeys.ADR_REFERENCES_TO)).get(0)) == null) {
				System.out.println("\n----- MISSING ADDRESS IN MAPPED DATA, EXITING TEST!! -----");
				break;
			}
	
	//		oMap = fetchObject("79297FDD-729B-4BC5-BF40-C1F3FB53D2F2", Quantity.DETAIL_ENTITY);
			
			System.out.println("\n----- store 79297FDD-729B-4BC5-BF40-C1F3FB53D2F2 -----");
			oMap = supertool.storeObject(oMap, true);

			if (oMap.get(MdekKeys.ADR_REFERENCES_TO) == null || ((List)oMap.get(MdekKeys.ADR_REFERENCES_TO)).size() == 0) {
				System.out.println("\n----- MISSING ADDRESS IN MAPPED DATA, EXITING TEST!! -----");
				break;
			}
			if (((HashMap)((List)oMap.get(MdekKeys.ADR_REFERENCES_TO)).get(0)) == null) {
				System.out.println("\n----- MISSING ADDRESS IN MAPPED DATA, EXITING TEST!! -----");
				break;
			}
		
		}

//		oMap = fetchObject("79297FDD-729B-4BC5-BF40-C1F3FB53D2F2", Quantity.DETAIL_ENTITY);
		
		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// Raumbezuege BUG !!! Freie gab Class Cast Exception see INGRIDII-116
		// ------------------

		IngridDocument newDoc = new IngridDocument();
		newDoc = supertool.getInitialObject(newDoc);

		// extend initial object with own data !
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		// add locations !!!
		ArrayList<IngridDocument> locs = new ArrayList<IngridDocument>();
		IngridDocument locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "Hintertupfingen");
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, -1);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, 5);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "Oberammergau");
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, -1);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "freie Fläche");
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, -1);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "blah");
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, -1);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "54353");
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, -1);
		locDoc.put(MdekKeys.LOCATION_TYPE, "F");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "Alpen");
		locDoc.put(MdekKeys.LOCATION_TYPE, "G");
		locDoc.put(MdekKeys.LOCATION_CODE, null);
		locDoc.put(MdekKeys.LOCATION_SNS_ID, "NATURRAUM5000");
		locs.add(locDoc);
		locDoc = new IngridDocument();
		locDoc.put(MdekKeys.LOCATION_NAME, "Allgäuer Alpen");
		locDoc.put(MdekKeys.LOCATION_TYPE, "G");
		locDoc.put(MdekKeys.LOCATION_CODE, null);
		locDoc.put(MdekKeys.LOCATION_SNS_ID, "GEBIRGE218");
		locs.add(locDoc);
		newDoc.put(MdekKeys.LOCATIONS, locs);

		oMap = supertool.publishObject(newDoc, true, false);
		// uuid created !
		String newUuid = (String)oMap.get(MdekKeys.UUID);

		// once again with same data ! references should be there !
		supertool.publishObject(oMap, true, false);

		deleteObject(newUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// Verweise auf gleiche Adresse unterschiedlicher VerweisTyp BUG !!! wurde nur einmal gespeichert see INGRIDII-113
		// ------------------
		IngridDocument newDoc = new IngridDocument();
		newDoc = supertool.getInitialObject(newDoc);

		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		ArrayList<IngridDocument> addrs = new ArrayList<IngridDocument>();
		IngridDocument addressDoc = new IngridDocument();
		addressDoc.put(MdekKeys.RELATION_TYPE_ID, 7);
		addressDoc.put(MdekKeys.RELATION_TYPE_NAME, "Auskunft");
		addressDoc.put(MdekKeys.RELATION_TYPE_REF, null);
		addressDoc.put(MdekKeys.UUID, "6E04D073-BC3A-11D2-A63A-444553540000");
		addrs.add(addressDoc);
		addressDoc = new IngridDocument();
		addressDoc.put(MdekKeys.RELATION_TYPE_ID, 6);
		addressDoc.put(MdekKeys.RELATION_TYPE_NAME, "Herkunft");
		addressDoc.put(MdekKeys.RELATION_TYPE_REF, null);
		addressDoc.put(MdekKeys.UUID, "6E04D073-BC3A-11D2-A63A-444553540000");
		addrs.add(addressDoc);
		addressDoc = new IngridDocument();
		addressDoc.put(MdekKeys.RELATION_TYPE_ID, 5);
		addressDoc.put(MdekKeys.RELATION_TYPE_NAME, "Vertrieb");
		addressDoc.put(MdekKeys.RELATION_TYPE_REF, null);
		addressDoc.put(MdekKeys.UUID, "6E04D073-BC3A-11D2-A63A-444553540000");
		addrs.add(addressDoc);
		newDoc.put(MdekKeys.ADR_REFERENCES_TO, addrs);

		oMap = supertool.publishObject(newDoc, true, false);
		// uuid created !
		String newUuid = (String)oMap.get(MdekKeys.UUID);

		deleteObject(newUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// Change Request 22, see INGRIDII-127
		// deliver "from object references" from published versions which were deleted in working version
		// ------------------
		String objFrom = "BCB59E87-17A0-11D5-8835-0060084A4596";
		String objTo = "05438065-16C3-11D5-8834-0060084A4596";

		System.out.println("\n----- load object from -----");
		oMap = supertool.fetchObject(objFrom, Quantity.DETAIL_ENTITY);
		List<IngridDocument> docList = (List<IngridDocument>) oMap.get(MdekKeys.OBJ_REFERENCES_TO);
		// find object to remove
		IngridDocument docToRemove = null;
		for (IngridDocument doc : docList) {
			if (objTo.equals(doc.get(MdekKeys.UUID))) {
				docToRemove = doc;
				break;
			}
		}

		System.out.println("\n----- remove reference to object / store working version -----");
		if (docToRemove != null) {
			docList.remove(docToRemove);
		}
		supertool.storeObject(oMap, true);

		System.out.println("\n----- load referenced object -> has reference from published object only ! -----");
		supertool.fetchObject(objTo, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteObjectWorkingCopy(objFrom, true);

		System.out.println("\n----- verify from object, no working version and references to object again ! -----");
		supertool.fetchObject(objFrom, Quantity.DETAIL_ENTITY);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// check object manipulation -> "create user" stored in comment

		supertool.setFullOutput(true);

		System.out.println("\n----- object details -----");
		oMap = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change and store existing object -> working copy ! -----");
		storeObjectWithManipulation(oMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, false);
		
		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// check storing of cat_id when creating new object !

		supertool.setFullOutput(true);

		System.out.println("\n----- STORE new top object -> working copy ! -----");
		System.out.println("\n----- initial data for TOP OBJECT -----");
		newObjDoc = new IngridDocument();
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT STORE");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());

		System.out.println("\n----- STORE new top object -> working copy ! -----");
		newObjDoc = supertool.storeObject(newObjDoc, true);
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- then publish -----");
		newObjDoc = supertool.publishObject(newObjDoc, true, false);

		System.out.println("\n----- delete NEW TOP OBJECT (FULL) -----");
		supertool.deleteObject(newObjUuid, true);

		
		System.out.println("\n----- publish NEW TOP OBJECT immediately -----");
		System.out.println("----- first get initial top object -----");
		newObjDoc = new IngridDocument();
		newObjDoc = supertool.getInitialObject(newObjDoc);
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT DIREKT PUBLISH");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());

		System.out.println("----- then publish -----");
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		// uuid created !
		newObjUuid = (String)newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete NEW TOP OBJECT (FULL) -----");
		supertool.deleteObject(newObjUuid, true);

		
		System.out.println("\n----- copy object (without subnodes) -> returns only TREE Data of object -----");
		oMap = supertool.copyObject("3866463B-B449-11D2-9A86-080000507261", null, false);
		// uuid created !
		newObjUuid = (String)oMap.get(MdekKeys.UUID);

		System.out.println("\n----- object details -----");
		supertool.fetchObject(newObjUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- delete NEW TOP OBJECT (FULL) -----");
		supertool.deleteObject(newObjUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// INSPIRE Tests ! IDC version has to be >= 1.0.3 !!!
		// ==============
	
		// tests here with BW catalog data !

		// technical domain DATASET (2.1) -> NI catalog !
		objUuid = "7AC6048A-7018-11D3-A599-C70A0FEBD4FC";

		// technical domain MAP
		// - 2.2: new field t011_obj_geo.datasource_uuid
		// - 2.4: t011_obj_geo.special_base not null and has default value 'Unbekannt' (if not set)
		//objUuid = "F1AA9C98-9A46-11D2-9A5E-006008649C7A";

		// object_access data (2.3)
		// - migration nur beschraenkung:
		// objUuid = "0F65578A-0C8C-4FF0-B81C-E8CA98FBC6D3"; // 946
		// objUuid = "1B8961C4-B1FD-4F1D-88C2-4AFCC147256F"; // 1387
		// objUuid = "1B721B27-D603-4CF4-99A8-125DE448E268"; // 867
		// - migration nur fee:
		// objUuid = "104BFC6B-4BA8-46C2-B6C3-EB16AB3A8452"; // 1006
		// objUuid = "1C44EA80-6832-4F68-A120-E912F90372D1"; // 1388
		// - migration beides:
//		objUuid = "D3424511-F995-11D3-BB92-0010A4FE557C"; // 1050
		// objUuid = "57F97284-5E8B-4ABD-807F-BBD35BC9AAF0"; // 1007
		// objUuid = "A8BBBD5F-1AF3-459A-87DF-7D3DFCA84136"; // 869

		// technical domain SERVICE (2.5, 2.6)
//		objUuid = "E7D2FE39-DAEE-11D3-BACC-00104B168367";

		// check object manipulation

		supertool.setFullOutput(true);
		
		System.out.println("\n----- object details -----");
		oMap = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change and store existing object -> working copy ! -----");
		storeObjectWithManipulation(oMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, false);

		System.out.println("\n----- original object details again -----");
		oMap = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// TEST handling of ORG_ID, should be unique (and test SPECIAL DATA e.g. DATASET_CHARACTER_SET, METADATA_CHARACTER_SET ...) !
		// ==============

		System.out.println("\n----- Create new object working version with ORG_ID + SPECIAL DATA (hack, org_id never passed from client, we simulate import !) -----");
		newObjDoc = supertool.newObjectDoc(null);
		newObjDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, "ORIGINAL_CONTROL_IDENTIFIER");
		newObjDoc.put(MdekKeys.DATASET_CHARACTER_SET, 6);
		newObjDoc.put(MdekKeys.METADATA_CHARACTER_SET, 6);
		newObjDoc.put(MdekKeys.METADATA_STANDARD_NAME, "METADATA_STANDARD_NAME");
		newObjDoc.put(MdekKeys.METADATA_STANDARD_VERSION, "METADATA_STANDARD_VERSION");			
		newObjDoc = supertool.storeObject(newObjDoc, true);
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- Publish new object -> ORG_ID + SPECIAL DATA in published version (check database) -----");
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		supertool.deleteObject(newObjUuid, false);

		System.out.println("\n\n\n----- Publish new object immediately with ORG_ID + SPECIAL DATA (hack, org_id never passed from client, we simulate import !) -----");
		newObjDoc = supertool.newObjectDoc(null);
		newObjDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, "ORIGINAL_CONTROL_IDENTIFIER");
		newObjDoc.put(MdekKeys.DATASET_CHARACTER_SET, 6);
		newObjDoc.put(MdekKeys.METADATA_CHARACTER_SET, 6);
		newObjDoc.put(MdekKeys.METADATA_STANDARD_NAME, "METADATA_STANDARD_NAME");
		newObjDoc.put(MdekKeys.METADATA_STANDARD_VERSION, "METADATA_STANDARD_VERSION");			
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- Store working version of new object -> ORG_ID + SPECIAL DATA in working version (check database) -----");
		newObjDoc = supertool.storeObject(newObjDoc, true);

		System.out.println("\n----- Copy new object -> ORG_ID NOT copied, SPECIAL DATA copied (check database) -----");
		oMap = supertool.copyObject(newObjUuid, null, false);
		String copyUuid = (String)oMap.get(MdekKeys.UUID);

		System.out.println("\n----- Clean up -----");
		supertool.deleteObject(newObjUuid, false);
		supertool.deleteObject(copyUuid, false);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// TEST of GEMET searchterms and INSPIRE searchterms
		// ==============

		System.out.println("\n----- PUBLISH new TERM-Object with FREE, UMTHES, GEMET and INSPIRE searchterms -----");
		newObjDoc = supertool.newObjectDoc(null);
		// extend initial object with searchterms !
		List<IngridDocument> myTerms = new ArrayList<IngridDocument>();
		newObjDoc.put(MdekKeys.SUBJECT_TERMS, myTerms);
		IngridDocument myTerm;
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.UMTHES.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "Geographie");
		myTerm.put(MdekKeys.TERM_SNS_ID, "uba_thes_10946");
		myTerms.add(myTerm);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "TEST Freier Searchterm !");
		myTerms.add(myTerm);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.GEMET.getDbValue());
		myTerm.put(MdekKeys.TERM_NAME, "Feinstaub");
		myTerm.put(MdekKeys.TERM_SNS_ID, "uba_thes_9320");
		myTerm.put(MdekKeys.TERM_GEMET_ID, "3209");
		myTerms.add(myTerm);
		// extend initial object with searchterms INSPIRE !
		myTerms = new ArrayList<IngridDocument>();
		newObjDoc.put(MdekKeys.SUBJECT_TERMS_INSPIRE, myTerms);
		myTerm = new IngridDocument();
		myTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.INSPIRE.getDbValue());
		myTerm.put(MdekKeys.TERM_ENTRY_ID, 311);
		myTerms.add(myTerm);
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		// uuid created !
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- STORE again -> NEW working copy, so NEW FREE searchterm created in DB ! same THESAURUS/INSPIRE terms ! -----");
		newObjDoc = supertool.storeObject(newObjDoc, true);

		System.out.println("\n----- STORE again -> NO NEW FREE searchterm, because term already connected to same object -----");
		newObjDoc = supertool.storeObject(newObjDoc, true);

		System.out.println("\n----- Get initial data for new SUB Object inheriting thesaurus and INSPIRE terms -----");
		supertool.newObjectDoc(newObjUuid);

		System.out.println("\n\n=========================");
		System.out.println("EXPORT TERM-OBJECT");

		System.out.println("\n----- export TERM-Object -----");
		supertool.exportObjectBranch(newObjUuid, true);
		supertool.setFullOutput(true);
		IngridDocument result = supertool.getExportInfo(true);
		byte[] exportTermObjZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);

		System.out.println("\n\n----- Remove all terms from TERM-Object and STORE ! -> working copy has no terms -----");
		newObjDoc.put(MdekKeys.SUBJECT_TERMS, new ArrayList<IngridDocument>());
		newObjDoc.put(MdekKeys.SUBJECT_TERMS_INSPIRE, new ArrayList<IngridDocument>());
		newObjDoc = supertool.storeObject(newObjDoc, true);

		System.out.println("\n\n=========================");
		System.out.println("IMPORT TERM-OBJECT");

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

		System.out.println("\n----- import TERM-Object as WORKING VERSION -> all terms again in working version -----");
		supertool.importEntities(exportTermObjZipped, objImpNodeUuid, addrImpNodeUuid, false, false);
		supertool.fetchObject(newObjUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- Clean Up -----");
		supertool.deleteObject(newObjUuid, true);
		supertool.deleteObject(objImpNodeUuid, true);
		supertool.deleteAddress(addrImpNodeUuid, true);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// ====================
*/
		// -----------------------------------
		// tree: top objects

		System.out.println("\n----- top objects -----");
		supertool.fetchTopObjects();

		// -----------------------------------
		// tree: sub objects

		System.out.println("\n----- sub objects -----");
//		supertool.fetchSubObjects(parentUuid);
		// > 400 subobjects !
		supertool.fetchSubObjects("81171714-018E-11D5-87AF-00600852CACF");

		// -----------------------------------
		// tree: object path

		System.out.println("\n----- object path -----");
		supertool.getObjectPath(objUuid);

		// -----------------------------------
		// object: load

		System.out.println("\n----- object details -----");
		oMap = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- object mit Verweis auf sich selbst ... -----");
		supertool.fetchObject("2F4D9A08-BCD0-11D2-A63A-444553540000", FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
		// object: check sub tree

		System.out.println("\n----- check object subtree -----");
		supertool.checkObjectSubTree(objUuid);

		// -----------------------------------
		// object: change and store and discard changes (working <-> published version)
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing object");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing object -> working copy ! -----");
		storeObjectWithManipulation(oMap);
		
		System.out.println("\n----- COMPARE OBJECT WORKING/PUBLISHED VERSION -----");
		System.out.println("\nWORKING VERSION:");
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION);
		System.out.println("\nPUBLISHED VERSION:");
		supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, false);
		
		System.out.println("\n----- and reload -----");
		oMap = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);

		// -----------------------------------
		// object: store NEW object and verify associations
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new object");
		System.out.println("=========================");

		System.out.println("\n----- check load initial data for TOP OBJECT -----");
		// set no parent
		newObjDoc = new IngridDocument();
		supertool.getInitialObject(newObjDoc);

		System.out.println("\n----- check load initial data from parent " + objUuid + " -----");
		newObjDoc = new IngridDocument();
		// supply parent uuid !
		newObjDoc.put(MdekKeys.PARENT_UUID, objUuid);
		newObjDoc = supertool.getInitialObject(newObjDoc);
		Object initialAddressList = newObjDoc.get(MdekKeys.ADR_REFERENCES_TO);

		System.out.println("\n----- extend initial object (with address, object references, spatial refs, free term ...) and store -----");
		// extend initial object with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		newObjDoc.put(MdekKeys.ADR_REFERENCES_TO, oMap.get(MdekKeys.ADR_REFERENCES_TO));
		newObjDoc.put(MdekKeys.OBJ_REFERENCES_TO, oMap.get(MdekKeys.OBJ_REFERENCES_TO));
		newObjDoc.put(MdekKeys.LOCATIONS, oMap.get(MdekKeys.LOCATIONS));
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		List<IngridDocument> terms = (List<IngridDocument>) newObjDoc.get(MdekKeys.SUBJECT_TERMS);
		IngridDocument newTerm = new IngridDocument();
		newTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
		newTerm.put(MdekKeys.TERM_NAME, "TEST Freier Searchterm !");
		System.out.println("ADD NEW SUBJECT TERM: " + newTerm);
		terms.add(newTerm);
		newObjDoc = storeObjectWithManipulation(newObjDoc);
		// uuid created !
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- check publish WITHOUT Auskunft Address ! -> Error AUSKUNFT_ADDRESS_NOT_SET -----");
		newObjDoc.put(MdekKeys.ADR_REFERENCES_TO, null);
		supertool.publishObject(newObjDoc, false,false);

		System.out.println("\n----- set Auskunft address and store (no publish, due to possible tests afterwards) -----");
		newObjDoc.put(MdekKeys.ADR_REFERENCES_TO, initialAddressList);
		newObjDoc = supertool.storeObject(newObjDoc, true);
		
		System.out.println("\n----- verify new subobject -> load parent subobjects -----");
		supertool.fetchSubObjects(objUuid);

		// -----------------------------------
		// tree: copy object sub tree
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy parent of new object to top (WITHOUT sub tree) -----");
		String objectFrom = objUuid;
		String objectTo = null;
		oMap = supertool.copyObject(objectFrom, objectTo, false);
		String copy1Uuid = (String)oMap.get(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		supertool.fetchObject(objectFrom, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- then load copy -----");
		supertool.fetchObject(copy1Uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n\n----- verify NO copied sub objects -> load children of copy -----");
		supertool.fetchSubObjects(copy1Uuid);
		System.out.println("\n\n----- copy parent of new object to top (WITH sub tree) -----");
		oMap = supertool.copyObject(objectFrom, objectTo, true);
		String copy2Uuid = (String)oMap.get(MdekKeys.UUID);
		System.out.println("\n\n----- verify copied sub objects -> load children of copy -----");
		supertool.fetchSubObjects(copy2Uuid);
		System.out.println("\n----- verify copy, load top -> new top objects -----");
		supertool.fetchTopObjects();
		System.out.println("\n----- delete copies (WORKING COPY) -> FULL DELETE -----");
		supertool.deleteObjectWorkingCopy(copy1Uuid, true);
		supertool.deleteObjectWorkingCopy(copy2Uuid, true);
		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new object below new object (WITH sub tree) -----");
		IngridDocument subtreeCopyDoc = supertool.copyObject(objUuid, newObjUuid, true);
		String subtreeCopyUuid = subtreeCopyDoc.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy -> load children of new object -----");
		supertool.fetchSubObjects(newObjUuid);
		// Following is allowed now ! Don't execute -> huge tree is copied !
//		System.out.println("\n----- do \"forbidden\" copy -----");
//		supertool.copyObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", true);

		// -----------------------------------
		// tree: move object sub tree
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n----- create NEW TOP OBJECT (parent to move to) = INTRANET -----");
		IngridDocument newTopObjDoc = new IngridDocument();
		newTopObjDoc = supertool.getInitialObject(newTopObjDoc);
		newTopObjDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT");
		newTopObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		newTopObjDoc = supertool.storeObject(newTopObjDoc, true);
		// uuid created !
		String newTopObjUuid = (String) newTopObjDoc.get(MdekKeys.UUID);

		System.out.println("\n\n----- move new object to NEW TOP OBJECT -> ERROR (new parent not published) -----");
		String oldParentUuid = objUuid;
		String newParentUuid = newTopObjUuid;
		supertool.moveObject(newObjUuid, newParentUuid, true);
		System.out.println("\n----- publish NEW TOP OBJECT -----");
		supertool.publishObject(newTopObjDoc, true, true);
		System.out.println("\n----- set new object (the one to move) to INTERNET and publish (pub=work=INTERNET) -----");
		newObjDoc = supertool.fetchObject(newObjUuid, FetchQuantity.EDITOR_ENTITY);
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		newObjDoc = supertool.publishObject(newObjDoc, true, false);
		System.out.println("\n----- create work version -> change title (pub=INTERNET, work=INTERNET) -----");
		newObjDoc.put(MdekKeys.TITLE, "TEST CHANGED!!! TITLE");
		newObjDoc = supertool.storeObject(newObjDoc, true);
		System.out.println("\n\n----- move new object to TOP INTRANET -> ERROR (pub=INTERNET) -----");
		supertool.moveObject(newObjUuid, newParentUuid, false);
		System.out.println("\n\n----- move new object again with forcePubCondition -> SUCCESS: but only pubVersion was adapted (pub=INTRANET, work=INTERNET !) -----");
		supertool.moveObject(newObjUuid, newParentUuid, true);
		System.out.println("\n\n----- verify moved object (work=INTERNET although parent=INTRANET) -----");
		newObjDoc = supertool.fetchObject(newObjUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n\n----- publish moved object -> ERROR: parent INTRANET -----");
		supertool.publishObject(newObjDoc, true, false);
		System.out.println("\n----- change moved object to INTRANET and store -----");
		newObjDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		newObjDoc = supertool.storeObject(newObjDoc, true);
		System.out.println("\n\n----- publish again -> SUCCESS (pub=work=INTRANET) ALTHOUGH subNodes are INTERNET, but these are NOT published yet !!! -----");
		supertool.publishObject(newObjDoc, true, false);
		System.out.println("\n----- check new object subtree (was also moved) -> has working copies (were copied above) -----");
		supertool.checkObjectSubTree(newObjUuid);
		System.out.println("\n----- fetch subobjects of new object (was moved) -----");
		System.out.println("----- NO CHANGE OF MODIFICATION TIME AND USER IN MOVED SUBTREE, see http://jira.media-style.com/browse/INGRIDII-266 -----");
		System.out.println("----- OK, not to see here, because subobjects were created by same user :-), but tested in mdek app ! -----");
		doc = supertool.fetchSubObjects(newObjUuid);
		List l = (List) doc.get(MdekKeys.OBJ_ENTITIES);
		if (l.size() > 0) {
			System.out.println("\n----- fetch first subobject details -----");
			String uuid = ((IngridDocument)l.get(0)).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		System.out.println("\n----- verify old parent subaddresses (cut) -----");
		supertool.fetchSubObjects(oldParentUuid);
		System.out.println("\n----- verify new parent subaddresses (added) -----");
		supertool.fetchSubObjects(newParentUuid);

		System.out.println("\n\n----- delete subtree of new Object -----");
		supertool.deleteObject(subtreeCopyUuid, true);
		System.out.println("\n\n----- move new object again to new parent -----");
		newParentUuid = parentUuid;
		supertool.moveObject(newObjUuid, newParentUuid, false);
		System.out.println("\n----- delete NEW TOP ADDRESS -----");
		supertool.deleteObject(newTopObjUuid, true);

		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		supertool.moveObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", false);

		// -----------------------------------
		// object: delete new object and verify deletion
		System.out.println("\n\n=========================");
		System.out.println("DELETE TEST");
		System.out.println("=========================");

		System.out.println("\n----- delete new object (WORKING COPY) -> NO full delete -----");
		supertool.deleteObjectWorkingCopy(newObjUuid, false);
		System.out.println("\n----- delete new object (FULL) -> full delete -----");
		supertool.deleteObject(newObjUuid, true);
		System.out.println("\n----- verify deletion of new object -----");
		supertool.fetchObject(newObjUuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subobjects -----");
		supertool.fetchSubObjects(newParentUuid);

		// ----------
		System.out.println("\n----- test deletion of object references / WARNINGS -----");

		System.out.println("\n----- create new TOP OBJECT -----");
		IngridDocument toObjDoc = new IngridDocument();
		toObjDoc = supertool.getInitialObject(toObjDoc);
		toObjDoc.put(MdekKeys.TITLE, "TEST TOP OBJECT");
		toObjDoc = supertool.storeObject(toObjDoc, true);
		// uuid created !
		String topObjUuid = (String) toObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new SUB OBJECT to be REFERENCED -----");
		// initial data from parent
		toObjDoc = new IngridDocument();
		toObjDoc.put(MdekKeys.PARENT_UUID, topObjUuid);
		toObjDoc = supertool.getInitialObject(toObjDoc);
		toObjDoc.put(MdekKeys.TITLE, "TEST SUB OBJECT -> wird referenziert");
		toObjDoc = supertool.storeObject(toObjDoc, true);
		// uuid created !
		String toObjUuid = (String) toObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new OBJECT_FROM is REFERENCING OBJECT_TO -----");
		IngridDocument fromObjDoc = new IngridDocument();
		fromObjDoc = supertool.getInitialObject(fromObjDoc);
		fromObjDoc.put(MdekKeys.TITLE, "TEST OBJECT_FROM -> referenziert");
		ArrayList<IngridDocument> objRefsList = new ArrayList<IngridDocument>(1);
		objRefsList.add(toObjDoc);
		fromObjDoc.put(MdekKeys.OBJ_REFERENCES_TO, objRefsList);
		fromObjDoc = supertool.storeObject(fromObjDoc, true);
		// uuid created !
		String fromObjUuid = (String) fromObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete TOP OBJECT (WORKING COPY) WITHOUT refs -> Error (Subtree has references) -----");
		supertool.deleteObjectWorkingCopy(topObjUuid, false);
		System.out.println("\n----- delete TOP OBJECT (FULL) WITHOUT refs -> Error (Subtree has references) -----");
		supertool.deleteObject(topObjUuid, false);
		System.out.println("\n----- delete TOP OBJECT (FULL) WITH refs -> OK -----");
		supertool.deleteObject(topObjUuid, true);
		System.out.println("\n----- delete OBJECT_FROM (FULL) WITHOUT refs -> OK -----");
		supertool.deleteObject(fromObjUuid, false);

		// -----------------------------------
		// copy object and publish ! create new object and publish !
		System.out.println("\n\n=========================");
		System.out.println("PUBLISH TEST");
		System.out.println("=========================");

		System.out.println("\n----- publish NEW TOP OBJECT immediately -----");
		System.out.println("----- first get initial top object -----");
		IngridDocument newTopDoc = new IngridDocument();
		newTopDoc = supertool.getInitialObject(newTopDoc);
		newTopDoc.put(MdekKeys.TITLE, "TEST NEUES TOP OBJEKT DIREKT PUBLISH");
		newTopDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		System.out.println("----- then publish -----");
		oMap = supertool.publishObject(newTopDoc, true, false);
		// uuid created !
		String newTopUuid = (String)oMap.get(MdekKeys.UUID);

		System.out.println("\n----- delete NEW TOP OBJECT (FULL) -----");
		supertool.deleteObject(newTopUuid, true);

		System.out.println("\n----- copy object (without subnodes) -> returns only TREE Data of object -----");
		objectFrom = newParentUuid;
		objectTo = null;
		oMap = supertool.copyObject(objectFrom, objectTo, false);
		// uuid created !
		String pub1Uuid = (String)oMap.get(MdekKeys.UUID);

		System.out.println("\n----- publish NEW SUB OBJECT immediately -> ERROR, PARENT NOT PUBLISHED ! -----");
		System.out.println("----- first get initial top object -----");
		IngridDocument newPubDoc = new IngridDocument();
		newPubDoc = supertool.getInitialObject(newPubDoc);
		newPubDoc.put(MdekKeys.TITLE, "TEST NEUES SUB OBJEKT DIREKT PUBLISH");
		newPubDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		// sub object of unpublished parent !!!
		newPubDoc.put(MdekKeys.PARENT_UUID, pub1Uuid);
		System.out.println("----- then publish -----");
		supertool.publishObject(newPubDoc, true, false);

		System.out.println("\n----- refetch FULL PARENT and change title, IS UNPUBLISHED !!! -----");
		oMap = supertool.fetchObject(pub1Uuid, FetchQuantity.EDITOR_ENTITY);
		oMap.put(MdekKeys.TITLE, "COPIED, Title CHANGED and PUBLISHED: " + oMap.get(MdekKeys.TITLE));	

		System.out.println("\n----- and publish PARENT -> create pub version/delete work version -----");
		supertool.publishObject(oMap, true, false);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		oMap = supertool.publishObject(newPubDoc, true, false);
		// uuid created !
		String pub2Uuid = (String) oMap.get(MdekKeys.UUID);

		System.out.println("\n----- verify -> load top objects -----");
		supertool.fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (WORKING COPY) -> NO DELETE -----");
		supertool.deleteObjectWorkingCopy(pub1Uuid, true);
		System.out.println("\n----- delete 2. published copy (FULL) -----");
		supertool.deleteObject(pub2Uuid, true);
//		System.out.println("\n----- verify -> load top objects -----");
//		fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (FULL) -----");
		supertool.deleteObject(pub1Uuid, true);

		// -----------------------------------
		// copy object and publish ! create new object and publish !
		System.out.println("\n\n=========================");
		System.out.println("PUBLICATION CONDITION TEST");
		System.out.println("NOTICE: manipulates database !!!!!!");
		System.out.println("=========================");

		supertool.setFullOutput(false);

		System.out.println("\n\n===== TEST simple change of publication condition in hierarchy  =====");

		parentUuid = "38665130-B449-11D2-9A86-080000507261";
		String childUuid = "38665131-B449-11D2-9A86-080000507261";

		System.out.println("\n----- fetch parent -----");
		IngridDocument oMapParent = supertool.fetchObject(parentUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- sub objects -----");
		supertool.fetchSubObjects(parentUuid);

		System.out.println("\n----- fetch child -----");
		IngridDocument oMapChild = supertool.fetchObject(childUuid, FetchQuantity.EDITOR_ENTITY);
		
		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> ERROR -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishObject(oMapParent, false, false);

		System.out.println("\n----- change child to INTRANET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishObject(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishObject(oMapParent, true, false);

		System.out.println("\n----- change child to INTERNET -> ERROR -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapChild, false, false);

		System.out.println("\n----- change parent to INTERNET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapParent, true, true);

		System.out.println("\n----- refetch child -> STILL INTRANET -----");
		oMapChild = supertool.fetchObject(childUuid, FetchQuantity.EDITOR_ENTITY);

		System.out.println("\n----- change child to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		supertool.publishObject(oMapParent, true, true);

		System.out.println("\n----- refetch child -> NOW INTRANET -----");
		oMapChild = supertool.fetchObject(childUuid, FetchQuantity.EDITOR_ENTITY);


		System.out.println("\n\n===== TEST change of publication condition VIA MOVE ! =====");
		
		System.out.println("\n----- verify INTERNET parent and children to MOVE -----");

		// NOTICE: UUid to move is TOP OBJECT !
		String moveUuid = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
		String moveChild1Uuid = "37D89A8E-3E4F-4907-A3FF-B01E3FE13B4C";
		String moveChild2Uuid = "2F121A74-C02F-4856-BBF1-48A7FC69D99A";
		supertool.fetchObject(moveUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchSubObjects(moveUuid);
		supertool.fetchObject(moveChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		supertool.fetchObject(moveChild2Uuid, FetchQuantity.EDITOR_ENTITY);
		
		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent -> ERROR -----");
		supertool.moveObject(moveUuid, parentUuid, false);

		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent -> SUCCESS -----");
		supertool.moveObject(moveUuid, parentUuid, true);

		System.out.println("\n----- verify -> all moved nodes INTRANET ! -----");
		IngridDocument oMapMoved1 = supertool.fetchObject(moveUuid, FetchQuantity.EDITOR_ENTITY);
		IngridDocument oMapMoved2 = supertool.fetchObject(moveChild1Uuid, FetchQuantity.EDITOR_ENTITY);
		IngridDocument oMapMoved3 = supertool.fetchObject(moveChild2Uuid, FetchQuantity.EDITOR_ENTITY);

		
		System.out.println("\n===== Clean Up ! back to old state of DB ! =====");
		
		System.out.println("\n----- move node back to top -----");
		supertool.moveObject(moveUuid, null, true);
		oMapMoved1.put(MdekKeys.PARENT_UUID, null);

		System.out.println("\n----- and change all moved nodes back to INTERNET -> SUCCESS -----");
		oMapMoved1.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapMoved1, true, true);
		oMapMoved2.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapMoved2, true, true);
		oMapMoved3.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapMoved3, true, true);

		System.out.println("\n----- change parent back to INTERNET -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapParent, true, true);

		System.out.println("\n----- change child back to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		supertool.publishObject(oMapChild, true, true);

		supertool.setFullOutput(true);
		
/*		
		System.out.println("\n\n=========================");
		System.out.println("CACHE TEST");
		System.out.println("=========================");
		System.out.println("\n----- loading 2 objects 10 times -----");
		
		long startTime = System.currentTimeMillis();
		for (int i=0; i< 10; i++) {
			mdekCaller.fetchObject(parentUuid, Quantity.DETAIL_ENTITY);
			mdekCaller.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		}
		System.out.println("EXECUTION TIME: " + (System.currentTimeMillis() - startTime)  + " ms");
*/
/*
		System.out.println("\n\n----- DELETE TEST (DELETES WHOLE SUBTREE) -----");
		
		String objectToDelete = "D3200435-53B7-11D3-A172-08002B9A1D1D";
//		String objectToDelete = objUuid;

		System.out.println("\n----- fetch object for deletion -----");
		fetchObject(objectToDelete, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- delete object (WORKING COPY) -----");
		deleteObjectWorkingCopy(objectToDelete);

		System.out.println("\n----- fetch object (now PUBLISHED VERSION) -----");
		fetchObject(objectToDelete, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- delete object (FULL) -----");
		deleteObject(objectToDelete);

		System.out.println("\n----- fetch object (non existent) -----");
		fetchObject(objectToDelete, Quantity.DETAIL_ENTITY);
*/
		// -----------------------------------

		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}
	
	private IngridDocument storeObjectWithManipulation(IngridDocument oDocIn) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		IngridDocument result;

		System.out.println("\n###### CALLED storeObjectWithManipulation !!!");
		
		// manipulate former loaded object !

		oDocIn.put(MdekKeys.TITLE, "BEARBEITET: " + oDocIn.get(MdekKeys.TITLE));
		Integer origDataLanguageCode = (Integer) oDocIn.get(MdekKeys.DATA_LANGUAGE_CODE);
		oDocIn.put(MdekKeys.DATA_LANGUAGE_CODE, UtilsLanguageCodelist.getCodeFromShortcut("en"));
		Integer origMetadataLanguageCode = (Integer) oDocIn.get(MdekKeys.METADATA_LANGUAGE_CODE);
		oDocIn.put(MdekKeys.METADATA_LANGUAGE_CODE, UtilsLanguageCodelist.getCodeFromShortcut("en"));

		// remove first address !
		List<IngridDocument> docList = (List<IngridDocument>) oDocIn.get(MdekKeys.ADR_REFERENCES_TO);
		IngridDocument aRemoved = null;
		if (docList != null && docList.size() > 0) {
			aRemoved = docList.get(0);
			System.out.println("REMOVE FIRST RELATED ADDRESS: " + aRemoved);
			docList.remove(0);			
		}

		// remove first object Querverweis !
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.OBJ_REFERENCES_TO);
		IngridDocument oRemoved = null;
		if (docList != null && docList.size() > 0) {
			oRemoved = docList.get(0);
			System.out.println("REMOVE FIRST OBJECT QUERVERWEIS: " + oRemoved);
			docList.remove(0);			
		}

		// remove first spatial reference !
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.LOCATIONS);
		IngridDocument locRemoved = null;
		if (docList != null && docList.size() > 0) {
			locRemoved = docList.get(0);
			System.out.println("REMOVE FIRST LOCATION: " + locRemoved);
			docList.remove(0);			
		}

		// remove first searchterm !
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.SUBJECT_TERMS);
		IngridDocument termRemoved = null;
		if (docList != null && docList.size() > 0) {
			termRemoved = docList.get(0);
			System.out.println("REMOVE FIRST SUBJECT TERM: " + termRemoved);
			docList.remove(0);			
		}

		// remove first url reference !
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.LINKAGES);
		IngridDocument urlRemoved = null;
		if (docList != null && docList.size() > 0) {
			urlRemoved = docList.get(0);
			System.out.println("REMOVE FIRST URL: " + urlRemoved);
			docList.remove(0);			
		}

		// remove first data reference !
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.DATASET_REFERENCES);
		IngridDocument refRemoved = null;
		if (docList != null && docList.size() > 0) {
			refRemoved = docList.get(0);
			System.out.println("REMOVE FIRST DATASET REFERENCE: " + refRemoved);
			docList.remove(0);			
		}

		// add entry to EXPORTS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.EXPORT_CRITERIA);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		// check EXPORT_KEY -> EXPORT_VALUE is stored via syslist
		testDoc.put(MdekKeys.EXPORT_CRITERION_KEY, 1);
		docList.add(testDoc);
		oDocIn.put(MdekKeys.EXPORT_CRITERIA, docList);

		// add entry to LEGISLATIONS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.LEGISLATIONS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check LEGISLATION_KEY -> LEGISLATION_VALUE is stored via syslist
		testDoc.put(MdekKeys.LEGISLATION_KEY, 11);
		docList.add(testDoc);
		oDocIn.put(MdekKeys.LEGISLATIONS, docList);

		// add entry to ENV_CATEGORIES
		List<Integer> intList = (List<Integer>) oDocIn.get(MdekKeys.ENV_CATEGORIES);
		intList = (intList == null) ? new ArrayList<Integer>() : intList;
		intList.add(new Integer(1234));
		oDocIn.put(MdekKeys.ENV_CATEGORIES, intList);

		// add entry to ENV_TOPICS
		intList = (List<Integer>) oDocIn.get(MdekKeys.ENV_TOPICS);
		intList = (intList == null) ? new ArrayList<Integer>() : intList;
		intList.add(new Integer(122));
		oDocIn.put(MdekKeys.ENV_TOPICS, intList);

		// add entry to TOPIC_CATEGORIES
		intList = (List<Integer>) oDocIn.get(MdekKeys.TOPIC_CATEGORIES);
		intList = (intList == null) ? new ArrayList<Integer>() : intList;
		intList.add(1);
		oDocIn.put(MdekKeys.TOPIC_CATEGORIES, intList);

		// add entry to DATA_FORMATS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.DATA_FORMATS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check FORMAT_NAME_KEY -> FORMAT_NAME is stored via syslist
		testDoc.put(MdekKeys.FORMAT_NAME_KEY, 10);
		testDoc.put(MdekKeys.FORMAT_VERSION, "TEST DATA_FORMAT_VERSION");
		testDoc.put(MdekKeys.FORMAT_SPECIFICATION, "TEST DATA_FORMAT_SPECIFICATION");
		testDoc.put(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE, "TEST DATA_FORMAT_FILE_DECOMPRESSION_TECHNIQUE");
		docList.add(testDoc);
		oDocIn.put(MdekKeys.DATA_FORMATS, docList);

		// add entry to MEDIUM_OPTIONS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.MEDIUM_OPTIONS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.MEDIUM_NAME, new Integer(1));
		testDoc.put(MdekKeys.MEDIUM_TRANSFER_SIZE, new Double(1.11));
		testDoc.put(MdekKeys.MEDIUM_NOTE, "TEST MEDIUM_NOTE");
		docList.add(testDoc);
		oDocIn.put(MdekKeys.MEDIUM_OPTIONS, docList);

		// add TECHNICAL DOMAIN MAP
		IngridDocument technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		technicalDomain.put(MdekKeys.TECHNICAL_BASE, "technical-base");
		technicalDomain.put(MdekKeys.DATA, "data");
		technicalDomain.put(MdekKeys.METHOD_OF_PRODUCTION, "method-of-production");
		technicalDomain.put(MdekKeys.COORDINATE_SYSTEM, "coordinate-system");
		technicalDomain.put(MdekKeys.RESOLUTION, new Double(1.1));
		technicalDomain.put(MdekKeys.DEGREE_OF_RECORD, new Double(1.2));
		technicalDomain.put(MdekKeys.HIERARCHY_LEVEL, new Integer(5));
		technicalDomain.put(MdekKeys.VECTOR_TOPOLOGY_LEVEL, new Integer(6));
		technicalDomain.put(MdekKeys.REFERENCESYSTEM_ID, new Integer(7));
		technicalDomain.put(MdekKeys.POS_ACCURACY_VERTICAL, new Double(1.5));
		technicalDomain.put(MdekKeys.KEYC_INCL_W_DATASET, new Integer(8));
		technicalDomain.put(MdekKeys.DATASOURCE_UUID, "TEST_DATASOURCE_UUID:" + oDocIn.get(MdekKeys.UUID));
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_MAP, technicalDomain);
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
		// add TECHNICAL DOMAIN MAP - publication scale
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.PUBLICATION_SCALE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.SCALE, new Integer(123));
		testDoc.put(MdekKeys.RESOLUTION_GROUND, new Double(1.123));
		testDoc.put(MdekKeys.RESOLUTION_SCAN, new Double(1.456));
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.PUBLICATION_SCALE_LIST, docList);
		// add TECHNICAL DOMAIN MAP - symbol catalog
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SYMBOL_CATALOG_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check SYMBOL_CAT_KEY -> SYMBOL_CAT is stored via syslist
		testDoc.put(MdekKeys.SYMBOL_CAT_KEY, 1);
		testDoc.put(MdekKeys.SYMBOL_DATE, "TEST " + MdekKeys.SYMBOL_DATE);
		testDoc.put(MdekKeys.SYMBOL_EDITION, "TEST " + MdekKeys.SYMBOL_EDITION);
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.SYMBOL_CATALOG_LIST, docList);
		// add TECHNICAL DOMAIN MAP - feature types
		List<String> strList = (List<String>) technicalDomain.get(MdekKeys.FEATURE_TYPE_LIST);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST feature type");
		technicalDomain.put(MdekKeys.FEATURE_TYPE_LIST, strList);
		// add TECHNICAL DOMAIN MAP - vector format -> geo vector list
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.GEO_VECTOR_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.GEOMETRIC_OBJECT_TYPE, new Integer(1));
		testDoc.put(MdekKeys.GEOMETRIC_OBJECT_COUNT, new Integer(100));
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.GEO_VECTOR_LIST, docList);
		// add TECHNICAL DOMAIN MAP - spatial representations
		intList = (List<Integer>) technicalDomain.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
		intList = (intList == null) ? new ArrayList<Integer>() : intList;
		intList.add(new Integer(1001));
		technicalDomain.put(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST, intList);
		
		// add TECHNICAL DOMAIN DOCUMENT
		technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		technicalDomain.put(MdekKeys.AUTHOR, "TEST " + MdekKeys.AUTHOR);
		technicalDomain.put(MdekKeys.SOURCE, "TEST " + MdekKeys.SOURCE);
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST " + MdekKeys.DESCRIPTION_OF_TECH_DOMAIN);
		technicalDomain.put(MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO, "TEST " + MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO);
		technicalDomain.put(MdekKeys.ISBN, "TEST " + MdekKeys.ISBN);
		technicalDomain.put(MdekKeys.LOCATION, "TEST " + MdekKeys.LOCATION);
		technicalDomain.put(MdekKeys.EDITOR, "TEST " + MdekKeys.EDITOR);
		technicalDomain.put(MdekKeys.PUBLISHED_IN, "TEST " + MdekKeys.PUBLISHED_IN);
		technicalDomain.put(MdekKeys.PUBLISHER, "TEST " + MdekKeys.PUBLISHER);
		technicalDomain.put(MdekKeys.PUBLISHING_PLACE, "TEST " + MdekKeys.PUBLISHING_PLACE);
		technicalDomain.put(MdekKeys.YEAR, "TEST " + MdekKeys.YEAR);
		technicalDomain.put(MdekKeys.PAGES, "TEST " + MdekKeys.PAGES);
		// check TYPE_OF_DOCUMENT_KEY -> TYPE_OF_DOCUMENT is stored via syslist
		technicalDomain.put(MdekKeys.TYPE_OF_DOCUMENT_KEY, 4);
		technicalDomain.put(MdekKeys.VOLUME, "TEST " + MdekKeys.VOLUME);
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, technicalDomain);
		
		// add TECHNICAL DOMAIN SERVICE
		technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		// check SERVICE_TYPE_KEY -> SERVICE_TYPE is stored via syslist
		technicalDomain.put(MdekKeys.SERVICE_TYPE_KEY, 2);
		technicalDomain.put(MdekKeys.SYSTEM_HISTORY, "TEST SYSTEM_HISTORY");
		technicalDomain.put(MdekKeys.SYSTEM_ENVIRONMENT, "TEST SYSTEM_ENVIRONMENT");
		technicalDomain.put(MdekKeys.DATABASE_OF_SYSTEM, "TEST DATABASE_OF_SYSTEM");
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST DESCRIPTION_OF_TECH_DOMAIN");
		technicalDomain.put(MdekKeys.HAS_ACCESS_CONSTRAINT, "Y");
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, technicalDomain);
		// add TECHNICAL DOMAIN SERVICE - versions
		strList = (List<String>) technicalDomain.get(MdekKeys.SERVICE_VERSION_LIST);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST SERVICE_VERSION1");
		strList.add("TEST SERVICE_VERSION2");
		technicalDomain.put(MdekKeys.SERVICE_VERSION_LIST, strList);
		// add TECHNICAL DOMAIN SERVICE - types
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_TYPE2_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.SERVICE_TYPE2_KEY, new Integer(110));
		testDoc.put(MdekKeys.SERVICE_TYPE2_VALUE, "ERROR, should be overwritten with true value !");
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.SERVICE_TYPE2_LIST, docList);
		// add TECHNICAL DOMAIN SERVICE - publication scale
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.PUBLICATION_SCALE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.SCALE, new Integer(123));
		testDoc.put(MdekKeys.RESOLUTION_GROUND, new Double(1.123));
		testDoc.put(MdekKeys.RESOLUTION_SCAN, new Double(1.456));
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.PUBLICATION_SCALE_LIST, docList);
		// add TECHNICAL DOMAIN SERVICE - operations
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_OPERATION_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check SERVICE_OPERATION_NAME_KEY -> SERVICE_OPERATION_NAME is stored via syslist
		// NOTICE: "interacts" with SERVICE_TYPE_KEY
		testDoc.put(MdekKeys.SERVICE_OPERATION_NAME_KEY, 4);
		testDoc.put(MdekKeys.SERVICE_OPERATION_DESCRIPTION, "TEST SERVICE_OPERATION_DESCRIPTION");
		testDoc.put(MdekKeys.INVOCATION_NAME, "TEST INVOCATION_NAME");
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.SERVICE_OPERATION_LIST, docList);
		// add TECHNICAL DOMAIN SERVICE - operation platforms
		strList = new ArrayList<String>();
		strList.add("TEST PLATFORM1");
		strList.add("TEST PLATFORM2");
		testDoc.put(MdekKeys.PLATFORM_LIST, strList);
		// add TECHNICAL DOMAIN SERVICE - dependsOns
		strList = new ArrayList<String>();
		strList.add("TEST DEPENDS_ON1");
		strList.add("TEST DEPENDS_ON2");
		testDoc.put(MdekKeys.DEPENDS_ON_LIST, strList);
		// add TECHNICAL DOMAIN SERVICE - connectPoints
		strList = new ArrayList<String>();
		strList.add("TEST CONNECT_POINT1");
		strList.add("TEST CONNECT_POINT2");
		testDoc.put(MdekKeys.CONNECT_POINT_LIST, strList);
		// add TECHNICAL DOMAIN SERVICE - params
		List<IngridDocument> docList2 = new ArrayList<IngridDocument>();
		IngridDocument testDoc2 = new IngridDocument();
		testDoc2.put(MdekKeys.PARAMETER_NAME, "TEST PARAMETER_NAME");
		testDoc2.put(MdekKeys.DIRECTION, "TEST DIRECTION");
		testDoc2.put(MdekKeys.DESCRIPTION, "TEST DESCRIPTION");
		testDoc2.put(MdekKeys.OPTIONALITY, 1);
		testDoc2.put(MdekKeys.REPEATABILITY, 2);
		docList2.add(testDoc2);
		testDoc.put(MdekKeys.PARAMETER_LIST, docList2);
		// add TECHNICAL DOMAIN SERVICE - Urls
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.URL_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.NAME, "url1 NAME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		testDoc.put(MdekKeys.URL, "http://www.test.url1.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		testDoc.put(MdekKeys.DESCRIPTION, "url1 DESCRIPTION !!!!!!!!!!!!!!!");
		docList.add(testDoc);
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.NAME, "url2 NAME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		testDoc.put(MdekKeys.URL, "http://www.test.url2.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		testDoc.put(MdekKeys.DESCRIPTION, "url2 DESCRIPTION !!!!!!!!!!!!!!!");
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.URL_LIST, docList);

		// add TECHNICAL DOMAIN PROJECT
		technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		technicalDomain.put(MdekKeys.LEADER_DESCRIPTION, "TEST LEADER_DESCRIPTION");
		technicalDomain.put(MdekKeys.MEMBER_DESCRIPTION, "TEST MEMBER_DESCRIPTION");
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST DESCRIPTION_OF_TECH_DOMAIN");
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_PROJECT, technicalDomain);

		// add TECHNICAL DOMAIN DATASET
		technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		technicalDomain.put(MdekKeys.METHOD, "TEST METHOD");
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST DESCRIPTION_OF_TECH_DOMAIN");
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.PARAMETERS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.PARAMETER, "TEST PARAMETER");
		testDoc.put(MdekKeys.SUPPLEMENTARY_INFORMATION, "TEST SUPPLEMENTARY_INFORMATION");
		docList.add(testDoc);
		technicalDomain.put(MdekKeys.PARAMETERS, docList);
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_DATASET, technicalDomain);

		// add OBJECT COMMENT
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.COMMENT_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMENT, "TEST COMMENT");
		testDoc.put(MdekKeys.CREATE_TIME, MdekUtils.dateToTimestamp(new Date()));
		IngridDocument createUserDoc = new IngridDocument();
		createUserDoc.put(MdekKeys.UUID, supertool.getCallingUserUuid());
		testDoc.put(MdekKeys.CREATE_USER, createUserDoc);
		docList.add(testDoc);
		oDocIn.put(MdekKeys.COMMENT_LIST, docList);

		// add entry to OBJECT CONFORMITY
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.CONFORMITY_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.CONFORMITY_SPECIFICATION, "TEST CONFORMITY_SPECIFICATION");
		testDoc.put(MdekKeys.CONFORMITY_PUBLICATION_DATE, MdekUtils.dateToTimestamp(new Date()));
		// check CONFORMITY_DEGREE_KEY -> CONFORMITY_DEGREE_VALUE is stored via syslist
		testDoc.put(MdekKeys.CONFORMITY_DEGREE_KEY, 1);
		docList.add(testDoc);
		oDocIn.put(MdekKeys.CONFORMITY_LIST, docList);

		// add entry to OBJECT ACCESS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.ACCESS_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		// check ACCESS_RESTRICTION_KEY -> ACCESS_RESTRICTION_VALUE is stored via syslist
		testDoc.put(MdekKeys.ACCESS_RESTRICTION_KEY, 1);
		docList.add(testDoc);
		oDocIn.put(MdekKeys.ACCESS_LIST, docList);

		// add entry to OBJECT USE
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.USE_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.USE_TERMS_OF_USE, "TEST USE_TERMS_OF_USE");
		docList.add(testDoc);
		oDocIn.put(MdekKeys.USE_LIST, docList);

		// add entries to OBJECT DATA QUALITY
		MdekSysList[] dqSyslists = new MdekSysList[] {
				MdekSysList.DQ_CompletenessComission,
				MdekSysList.DQ_CompletenessOmission,
				MdekSysList.DQ_ConceptualConsistency,
				MdekSysList.DQ_DomainConsistency,
				MdekSysList.DQ_FormatConsistency,
				MdekSysList.DQ_TopologicalConsistency,
				MdekSysList.DQ_AbsoluteExternalPositionalAccuracy,
				MdekSysList.DQ_TemporalConsistency,
				MdekSysList.DQ_ThematicClassificationCorrectness,
				MdekSysList.DQ_NonQuantitativeAttributeAccuracy,
				MdekSysList.DQ_QuantitativeAttributeAccuracy };
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.DATA_QUALITY_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		oDocIn.put(MdekKeys.DATA_QUALITY_LIST, docList);
		for (MdekSysList dqSyslist : dqSyslists) {
			// also add free entry for every list (key = -1)
			int key = 1;
			while (key >= -1) {
				testDoc = new IngridDocument();
				testDoc.put(MdekKeys.DQ_ELEMENT_ID, dqSyslist.getDqElementId());
				testDoc.put(MdekKeys.NAME_OF_MEASURE_KEY, key);
				testDoc.put(MdekKeys.NAME_OF_MEASURE_VALUE, "Free NAME_OF_MEASURE_VALUE !!!?");
				testDoc.put(MdekKeys.RESULT_VALUE, "Test RESULT_VALUE " + dqSyslist.getDqElementId());
				testDoc.put(MdekKeys.MEASURE_DESCRIPTION, "Test MEASURE_DESCRIPTION " + dqSyslist.getDqElementId());
				docList.add(testDoc);
				key = key - 2;
			}
		}

		// store
		System.out.println("STORE");
		result = supertool.storeObject(oDocIn, false);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			System.out.println("refetch Object");
			IngridDocument oRefetchedDoc = supertool.fetchObject(uuidStoredObject, FetchQuantity.EDITOR_ENTITY);
			System.out.println("");

			System.out.println("MANIPULATE OBJECT: back to origin");

			oRefetchedDoc.put(MdekKeys.DATA_LANGUAGE_CODE, origDataLanguageCode);
			oRefetchedDoc.put(MdekKeys.METADATA_LANGUAGE_CODE, origMetadataLanguageCode);

			if (aRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.ADR_REFERENCES_TO);
				docList.add(aRemoved);
				System.out.println("ADD REMOVED ADDRESS AGAIN: " + aRemoved);
				// check special type -> RELATION_TYPE_NAME is stored via syslist
				System.out.println("CHANGE RELATION TYPE OF REMOVED ADDRESS");
				aRemoved.put(MdekKeys.RELATION_TYPE_ID, 3360);			
				aRemoved.put(MdekKeys.RELATION_TYPE_REF, 2010);			
			}
			if (oRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.OBJ_REFERENCES_TO);
				docList.add(oRemoved);
				System.out.println("ADD REMOVED OBJECT QUERVERWEIS AGAIN: " + oRemoved);
				// check type -> RELATION_TYPE_NAME is stored via syslist
				System.out.println("CHANGE RELATION_TYPE_REF OF OBJECT QUERVERWEIS");
				oRemoved.put(MdekKeys.RELATION_TYPE_REF, 3570);			
			}
			if (locRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.LOCATIONS);
				docList.add(locRemoved);
				System.out.println("ADD REMOVED LOCATION AGAIN: " + locRemoved);
				// check LOCATION_NAME_KEY -> LOCATION_NAME is stored via syslist
				System.out.println("CHANGE LOCATION_NAME_KEY OF LOCATION");
				locRemoved.put(MdekKeys.LOCATION_NAME_KEY, 5);			
			}
			if (termRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.SUBJECT_TERMS);
				docList.add(termRemoved);
				System.out.println("ADD REMOVED SUBJECT TERM AGAIN: " + termRemoved);
			}

			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.LINKAGES);
			if (urlRemoved != null) {
				docList.add(urlRemoved);
				System.out.println("ADD REMOVED URL AGAIN: " + urlRemoved);
				// check LINKAGE_DATATYPE_KEY -> LINKAGE_DATATYPE is stored via syslist
				System.out.println("CHANGE LINKAGE_DATATYPE_KEY OF URL");
				urlRemoved.put(MdekKeys.LINKAGE_DATATYPE_KEY, 1);			
				// check LINKAGE_REFERENCE_ID -> LINKAGE_REFERENCE is stored via syslist
				System.out.println("CHANGE LINKAGE_REFERENCE_ID OF URL");
				urlRemoved.put(MdekKeys.LINKAGE_REFERENCE_ID, 3100);			
			}
			// add new URL
			IngridDocument newUrl = new IngridDocument();
			newUrl.put(MdekKeys.LINKAGE_URL, "http://www.wemove.com");
			newUrl.put(MdekKeys.LINKAGE_NAME, "WEMOVE");
			System.out.println("ADD NEW URL AT FIRST POS: " + newUrl);
			docList.add(0, newUrl);

			if (refRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.DATASET_REFERENCES);
				docList.add(refRemoved);
				System.out.println("ADD REMOVED DATASET REFERENCE AGAIN: " + refRemoved);
			}

			// EXPORTS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.EXPORT_CRITERIA);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.EXPORT_CRITERIA, docList);				
			}

			// LEGISLATIONS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.LEGISLATIONS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.LEGISLATIONS, docList);				
			}
			
			// DATA_FORMATS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.DATA_FORMATS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.DATA_FORMATS, docList);				
			}

			// MEDIUM_OPTIONS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.MEDIUM_OPTIONS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.MEDIUM_OPTIONS, docList);				
			}

			// ENV_CATEGORIES wieder wie vorher !
			intList = (List<Integer>) oRefetchedDoc.get(MdekKeys.ENV_CATEGORIES);
			if (intList != null && intList.size() > 0) {
				intList.remove(intList.size()-1);
				oRefetchedDoc.put(MdekKeys.ENV_CATEGORIES, intList);				
			}

			// ENV_TOPICS wieder wie vorher !
			intList = (List<Integer>) oRefetchedDoc.get(MdekKeys.ENV_TOPICS);
			if (intList != null && intList.size() > 0) {
				intList.remove(intList.size()-1);
				oRefetchedDoc.put(MdekKeys.ENV_TOPICS, intList);				
			}

			// TOPIC_CATEGORIES wieder wie vorher !
			intList = (List<Integer>) oRefetchedDoc.get(MdekKeys.TOPIC_CATEGORIES);
			if (intList != null && intList.size() > 0) {
				intList.remove(intList.size()-1);
				oRefetchedDoc.put(MdekKeys.TOPIC_CATEGORIES, intList);				
			}

			// REMOVE TECHNICAL DOMAIN MAP
			oRefetchedDoc.remove(MdekKeys.TECHNICAL_DOMAIN_MAP);

			// REMOVE TECHNICAL DOMAIN DOCUMENT
			oRefetchedDoc.remove(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
			
			// TECHNICAL_DOMAIN_SERVICE raus !
			oRefetchedDoc.remove(MdekKeys.TECHNICAL_DOMAIN_SERVICE);

			// TECHNICAL_DOMAIN_PROJECT raus !
			oRefetchedDoc.remove(MdekKeys.TECHNICAL_DOMAIN_PROJECT);

			// TECHNICAL_DOMAIN_DATASET raus !
			oRefetchedDoc.remove(MdekKeys.TECHNICAL_DOMAIN_DATASET);

			// COMMENT wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.COMMENT_LIST);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.COMMENT_LIST, docList);				
			}

			// OBJECT CONFORMITY wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.CONFORMITY_LIST);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.CONFORMITY_LIST, docList);				
			}

			// OBJECT ACCESS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.ACCESS_LIST);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.ACCESS_LIST, docList);
			}

			// OBJECT USE wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.USE_LIST);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.USE_LIST, docList);
			}

			// OBJECT USE wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.DATA_QUALITY_LIST);
			if (docList != null && docList.size() > 0) {
				// remove all added test dq elements
				for (int i=0; i < dqSyslists.length; i++) {
					docList.remove(docList.size()-1);
					docList.remove(docList.size()-1);
				}
			}

			// store
			System.out.println("STORE");
			result = supertool.storeObject(oRefetchedDoc, true);
		}

		return result;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
