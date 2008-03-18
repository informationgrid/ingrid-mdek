package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.IMdekCallerCatalog;
import de.ingrid.mdek.IMdekCallerObject;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekCallerCatalog;
import de.ingrid.mdek.MdekCallerObject;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekCallerAbstract.Quantity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;

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
		MdekCaller.initialize(new File((String) map.get("--descriptor")));
		IMdekCaller mdekCaller = MdekCaller.getInstance();

		// and our specific job callers !
		MdekCallerObject.initialize(mdekCaller);
		MdekCallerCatalog.initialize(mdekCaller);

		// wait till iPlug registered !
		System.out.println("\n###### waiting for mdek iPlug to register ######\n");
		boolean plugRegistered = false;
		while (!plugRegistered) {
			List<String> iPlugs = mdekCaller.getRegisteredIPlugs();
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
	String myUserId;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	// MDEK SERVER TO CALL !
	private String plugId = "mdek-iplug-idctest";
	
	private IMdekCaller mdekCaller;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerCatalog mdekCallerCatalog;

	public MdekExampleObjectThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserId = "EXAMPLE_USER_" + threadNumber;

		mdekCaller = MdekCaller.getInstance();
		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
	}

	public void run() {
		isRunning = true;
//		this.doFullOutput = false;

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

		//System.out.println("\n###### INVOKE testMdekEntity ######");
		//mdekCaller.testMdekEntity(threadNumber);

/*
// ====================
// test single stuff
// -----------------------------------

		// track server job !
		// ------------------
		boolean alwaysTrue = true;
		boolean timeout = false;
		try {
			copyObject("15C69C20-FE15-11D2-AF34-0060084A4596", null, true);			
		} catch(Exception ex) {
			timeout = true;
		}

		// once again -> ERROR: job running
		copyObject("15C69C20-FE15-11D2-AF34-0060084A4596", null, true);

		if (timeout) {
			// also cancels Running Job !
			trackRunningJob(3000, true);
		}

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// EH CACHE BUG !!! referenced Adress Node not fetched in refetch of 2. store !
		// ------------------
		boolean alwaysTrue = false;
		for (int i=0; i< 1; i++) {
		
			System.out.println("\n----- load 79297FDD-729B-4BC5-BF40-C1F3FB53D2F2 -----");
			oMap = fetchObject("79297FDD-729B-4BC5-BF40-C1F3FB53D2F2", Quantity.DETAIL_ENTITY);
	
			System.out.println("\n----- store 79297FDD-729B-4BC5-BF40-C1F3FB53D2F2 -----");
			oMap = storeObjectWithoutManipulation(oMap, true);
			
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
			oMap = storeObjectWithoutManipulation(oMap, true);

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

// ====================
*/

		// -----------------------------------
		// catalog

		System.out.println("\n----- CATALOG data -----");
		getCatalog();

		// -----------------------------------
		// ui: initial lists

		System.out.println("\n----- SysList Values WITHOUT language code -----");
		getSysLists(new Integer[] { 100, 1100, 1350, 3555}, null);
		System.out.println("\n----- SysList Values WITH language code -----");
		getSysLists(new Integer[] { 100, 1100, 1350, 3555}, 121);

		// -----------------------------------
		// tree: top objects

		System.out.println("\n----- top objects -----");
		fetchTopObjects();

		// -----------------------------------
		// tree: sub objects

		System.out.println("\n----- sub objects -----");
		fetchSubObjects(parentUuid);

		// -----------------------------------
		// tree: object path

		System.out.println("\n----- object path -----");
		getObjectPath(objUuid);

		// -----------------------------------
		// object: load

		System.out.println("\n----- object details -----");
		oMap = fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- object mit Verweis auf sich selbst ... -----");
		fetchObject("2F4D9A08-BCD0-11D2-A63A-444553540000", Quantity.DETAIL_ENTITY);

		// -----------------------------------
		// object: check sub tree

		System.out.println("\n----- check object subtree -----");
		checkObjectSubTree(objUuid);

		// -----------------------------------
		// object: change and store and discard changes (working <-> published version)
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST existing object");
		System.out.println("=========================");

		System.out.println("\n----- change and store existing object -> working copy ! -----");
		storeObjectWithManipulation(oMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteObjectWorkingCopy(objUuid, false);
		
		System.out.println("\n----- and reload -----");
		oMap = fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		// object: store NEW object and verify associations
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new object");
		System.out.println("=========================");

		System.out.println("\n----- first load initial data (e.g. from parent " + objUuid + ") -----");
		IngridDocument newObjDoc = new IngridDocument();
		// supply parent uuid !
		newObjDoc.put(MdekKeys.PARENT_UUID, objUuid);
		newObjDoc = getInitialObject(newObjDoc);

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

		IngridDocument oMapNew = storeObjectWithManipulation(newObjDoc);
		// uuid created !
		String newObjUuid = (String) oMapNew.get(MdekKeys.UUID);

		System.out.println("\n----- verify new subobject -> load parent subobjects -----");
		fetchSubObjects(objUuid);

		// -----------------------------------
		// tree: copy object sub tree
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy parent of new object to top (WITHOUT sub tree) -----");
		String objectFrom = objUuid;
		String objectTo = null;
		oMap = copyObject(objectFrom, objectTo, false);
		String copy1Uuid = (String)oMap.get(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("----- load original one -----");
		fetchObject(objectFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- then load copy -----");
		fetchObject(copy1Uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n\n----- verify NO copied sub objects -> load children of copy -----");
		fetchSubObjects(copy1Uuid);
		System.out.println("\n\n----- copy parent of new object to top (WITH sub tree) -----");
		oMap = copyObject(objectFrom, objectTo, true);
		String copy2Uuid = (String)oMap.get(MdekKeys.UUID);
		System.out.println("\n\n----- verify copied sub objects -> load children of copy -----");
		fetchSubObjects(copy2Uuid);
		System.out.println("\n----- verify copy, load top -> new top objects -----");
		fetchTopObjects();
		System.out.println("\n----- delete copies (WORKING COPY) -> FULL DELETE -----");
		deleteObjectWorkingCopy(copy1Uuid, true);
		deleteObjectWorkingCopy(copy2Uuid, true);
		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new object below new object (WITH sub tree) -----");
		IngridDocument subtreeCopyDoc = copyObject(objUuid, newObjUuid, true);
		String subtreeCopyUuid = subtreeCopyDoc.getString(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy -> load children of new object -----");
		fetchSubObjects(newObjUuid);
		// Following is allowed now ! Don't execute -> huge tree is copied !
//		System.out.println("\n----- do \"forbidden\" copy -----");
//		copyObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", true);

		// -----------------------------------
		// tree: move object sub tree
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- move new object WITHOUT CHECK WORKING COPIES -> ERROR (not published yet) -----");
		String oldParentUuid = objUuid;
		String newParentUuid = parentUuid;
		moveObject(newObjUuid, newParentUuid, false, false);
		System.out.println("\n----- publish new object -> create pub version/delete work version -----");
		publishObject(oMapNew, true, true);
		System.out.println("\n\n----- move new object again WITH CHECK WORKING COPIES -> ERROR (subtree has working copies) -----");
		moveObject(newObjUuid, newParentUuid, true, false);
		System.out.println("\n----- check new object subtree -----");
		checkObjectSubTree(newObjUuid);
		System.out.println("\n\n----- delete subtree -----");
		deleteObject(subtreeCopyUuid, true);
		System.out.println("\n\n----- move new object again WITH CHECK WORKING COPIES -> SUCCESS (published AND no working copies ) -----");
		moveObject(newObjUuid, newParentUuid, true, false);
		System.out.println("\n----- verify old parent subobjects (cut) -----");
		fetchSubObjects(oldParentUuid);
		System.out.println("\n----- verify new parent subobjects (added) -----");
		fetchSubObjects(newParentUuid);
		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		moveObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", false, false);

// Make another move to check via DB whether mod_time was updated for all moved nodes
//		moveObject(objUuid, null, true);

		// -----------------------------------
		// object: delete new object and verify deletion
		System.out.println("\n\n=========================");
		System.out.println("DELETE TEST");
		System.out.println("=========================");

		System.out.println("\n----- delete new object (WORKING COPY) -> NO full delete -----");
		deleteObjectWorkingCopy(newObjUuid, false);
		System.out.println("\n----- delete new object (FULL) -> full delete -----");
		deleteObject(newObjUuid, false);
		System.out.println("\n----- verify deletion of new object -----");
		fetchObject(newObjUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subobjects -----");
		fetchSubObjects(newParentUuid);

		// ----------
		System.out.println("\n----- test deletion of object references / WARNINGS -----");

		System.out.println("\n----- create new TOP OBJECT -----");
		IngridDocument toObjDoc = new IngridDocument();
		toObjDoc = getInitialObject(toObjDoc);
		toObjDoc.put(MdekKeys.TITLE, "TEST TOP OBJECT");
		toObjDoc = storeObjectWithoutManipulation(toObjDoc, true);
		// uuid created !
		String topObjUuid = (String) toObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new SUB OBJECT to be REFERENCED -----");
		// initial data from parent
		toObjDoc = new IngridDocument();
		toObjDoc.put(MdekKeys.PARENT_UUID, topObjUuid);
		toObjDoc = getInitialObject(toObjDoc);
		toObjDoc.put(MdekKeys.TITLE, "TEST SUB OBJECT -> wird referenziert");
		toObjDoc = storeObjectWithoutManipulation(toObjDoc, true);
		// uuid created !
		String toObjUuid = (String) toObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create new OBJECT_FROM is REFERENCING OBJECT_TO -----");
		IngridDocument fromObjDoc = new IngridDocument();
		fromObjDoc = getInitialObject(fromObjDoc);
		fromObjDoc.put(MdekKeys.TITLE, "TEST OBJECT_FROM -> referenziert");
		ArrayList<IngridDocument> objRefsList = new ArrayList<IngridDocument>(1);
		objRefsList.add(toObjDoc);
		fromObjDoc.put(MdekKeys.OBJ_REFERENCES_TO, objRefsList);
		fromObjDoc = storeObjectWithoutManipulation(fromObjDoc, true);
		// uuid created !
		String fromObjUuid = (String) fromObjDoc.get(MdekKeys.UUID);

		System.out.println("\n----- delete OBJECT_TO (WORKING COPY) WITHOUT refs -> Error -----");
		deleteObjectWorkingCopy(topObjUuid, false);
		System.out.println("\n----- delete OBJECT_TO (FULL) WITHOUT refs -> Error -----");
		deleteObject(topObjUuid, false);
		System.out.println("\n----- delete OBJECT_TO (WORKING COPY) WITH refs -> OK -----");
		deleteObjectWorkingCopy(topObjUuid, true);
		System.out.println("\n----- delete OBJECT_FROM (WORKING COPY) without refs -> OK -----");
		deleteObjectWorkingCopy(fromObjUuid, false);

		// -----------------------------------
		// copy object and publish ! create new object and publish !
		System.out.println("\n\n=========================");
		System.out.println("PUBLISH TEST");
		System.out.println("=========================");

		System.out.println("\n----- copy object (without subnodes) -> returns only TREE Data of object -----");
		objectFrom = newParentUuid;
		objectTo = null;
		oMap = copyObject(objectFrom, objectTo, false);
		String pub1Uuid = (String)oMap.get(MdekKeys.UUID);

		System.out.println("\n----- publish NEW SUB OBJECT immediately -> ERROR, PARENT NOT PUBLISHED ! -----");
		IngridDocument newPubDoc = new IngridDocument();
		newPubDoc.put(MdekKeys.TITLE, "TEST NEUES SUB OBJEKT DIREKT PUBLISH");
		newPubDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.AMTSINTERN.getDbValue());
		// sub object of unpublished parent !!!
		newPubDoc.put(MdekKeys.PARENT_UUID, pub1Uuid);
		publishObject(newPubDoc, true, false);

		System.out.println("\n----- refetch FULL PARENT and change title, IS UNPUBLISHED !!! -----");
		oMap = fetchObject(pub1Uuid, Quantity.DETAIL_ENTITY);
		oMap.put(MdekKeys.TITLE, "COPIED, Title CHANGED and PUBLISHED: " + oMap.get(MdekKeys.TITLE));	

		System.out.println("\n----- and publish PARENT -> create pub version/delete work version -----");
		publishObject(oMap, true, false);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		oMap = publishObject(newPubDoc, true, false);
		// uuid created !
		String pub2Uuid = (String) oMap.get(MdekKeys.UUID);

		System.out.println("\n----- verify -> load top objects -----");
		fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (WORKING COPY) -> NO DELETE -----");
		deleteObjectWorkingCopy(pub1Uuid, true);
		System.out.println("\n----- delete 2. published copy (FULL) -----");
		deleteObject(pub2Uuid, true);
		System.out.println("\n----- verify -> load top objects -----");
		fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (FULL) -----");
		deleteObject(pub1Uuid, true);

		// -----------------------------------
		// copy object and publish ! create new object and publish !
		System.out.println("\n\n=========================");
		System.out.println("PUBLICATION CONDITION TEST");
		System.out.println("=========================");

		this.doFullOutput = false;

		parentUuid = "38665130-B449-11D2-9A86-080000507261";
		String childUuid = "38665131-B449-11D2-9A86-080000507261";

		System.out.println("\n----- fetch parent -----");
		IngridDocument oMapParent = fetchObject(parentUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- sub objects -----");
		fetchSubObjects(parentUuid);

		System.out.println("\n----- fetch child -----");
		IngridDocument oMapChild = fetchObject(childUuid, Quantity.DETAIL_ENTITY);
		
		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> ERROR -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		publishObject(oMapParent, false, false);

		System.out.println("\n----- change child to INTRANET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		publishObject(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (NO forced publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		publishObject(oMapParent, true, false);

		System.out.println("\n----- change child to INTERNET -> ERROR -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		publishObject(oMapChild, false, false);

		System.out.println("\n----- change parent to INTERNET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		publishObject(oMapParent, true, true);

		System.out.println("\n----- refetch child -> STILL INTRANET -----");
		oMapChild = fetchObject(childUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change child to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		publishObject(oMapChild, true, false);

		System.out.println("\n----- change parent to INTRANET (FORCED publication condition) -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTRANET.getDbValue());
		publishObject(oMapParent, true, true);

		System.out.println("\n----- refetch child -> NOW INTRANET -----");
		oMapChild = fetchObject(childUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- verify INTERNET parent and children to MOVE -----");
		String moveUuid = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
		String moveChild1Uuid = "37D89A8E-3E4F-4907-A3FF-B01E3FE13B4C";
		String moveChild2Uuid = "2F121A74-C02F-4856-BBF1-48A7FC69D99A";
		fetchObject(moveUuid, Quantity.DETAIL_ENTITY);
		fetchSubObjects(moveUuid);
		fetchObject(moveChild1Uuid, Quantity.DETAIL_ENTITY);
		fetchObject(moveChild2Uuid, Quantity.DETAIL_ENTITY);
		
		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent -> ERROR -----");
		moveObject(moveUuid, parentUuid, false, false);

		System.out.println("\n----- test MOVE INTERNET node to INTRANET parent -> SUCCESS -----");
		moveObject(moveUuid, parentUuid, false, true);

		System.out.println("\n----- verify -> all moved nodes INTRANET ! -----");
		fetchObject(moveUuid, Quantity.DETAIL_ENTITY);
		fetchObject(moveChild1Uuid, Quantity.DETAIL_ENTITY);
		fetchObject(moveChild2Uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change parent back to INTERNET -> SUCCESS -----");
		oMapParent.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		publishObject(oMapParent, true, true);

		System.out.println("\n----- change child back to INTERNET -> SUCCESS -----");
		oMapChild.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
		publishObject(oMapChild, true, true);

		this.doFullOutput = true;


		
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
	
	private IngridDocument getCatalog() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchCatalog ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.fetchCatalog(plugId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getSysLists(Integer[] listIds, Integer langCode) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysLists ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysLists(plugId, listIds, langCode, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			Set<String> listKeys = result.keySet();
			System.out.println("SUCCESS: " + listKeys.size() + " sys-lists");
			for (String listKey : listKeys) {
				IngridDocument listDoc = (IngridDocument) result.get(listKey);
				List<IngridDocument> entryDocs =
					(List<IngridDocument>) listDoc.get(MdekKeys.LST_ENTRY_LIST);
				System.out.println("  " + listKey + ": " + entryDocs.size() + " entries");
				System.out.println("    " + entryDocs);				
			}
			
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchTopObjects() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchTopObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchTopObjects(plugId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchSubObjects(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchSubObjects(plugId, uuid, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getObjectPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectPath(plugId, uuidIn, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<String> uuidList = (List<String>) result.get(MdekKeys.PATH);
			System.out.println("SUCCESS: " + uuidList.size() + " levels");
			String indent = " ";
			for (String uuid : uuidList) {
				System.out.println(indent + uuid);
				indent += " ";
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchObject(String uuid, Quantity howMuch) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, howMuch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getInitialObject(IngridDocument newBasicObject) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialObject ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getInitialObject(plugId, newBasicObject, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument checkObjectSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkObjectSubTree ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.checkObjectSubTree(plugId, uuid, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeObjectWithoutManipulation(IngridDocument oDocIn,
			boolean refetchObject) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeObject ######");

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.storeObject(plugId, oDocIn, refetchObject, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument storeObjectWithManipulation(IngridDocument oDocIn) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeObject ######");
		
		// manipulate former loaded object !

		oDocIn.put(MdekKeys.TITLE, "BEARBEITET: " + oDocIn.get(MdekKeys.TITLE));

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
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.EXPORTS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.EXPORT_VALUE, "TEST DATA_FORMAT_NAME");
		testDoc.put(MdekKeys.EXPORT_KEY, new Integer(-1));
		docList.add(testDoc);
		oDocIn.put(MdekKeys.EXPORTS, docList);

		// add entry to LEGISLATIONS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.LEGISLATIONS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.LEGISLATION_VALUE, "TEST NEW T015Legist entry");
		testDoc.put(MdekKeys.LEGISLATION_KEY, new Integer(-1));
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
		testDoc.put(MdekKeys.FORMAT_NAME, "TEST DATA_FORMAT_NAME");
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
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_MAP, technicalDomain);
		// add TECHNICAL DOMAIN MAP - key catalog
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.KEY_CATALOG_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.SUBJECT_CAT, "TEST " + MdekKeys.SUBJECT_CAT);
		testDoc.put(MdekKeys.SUBJECT_CAT_KEY, new Integer(-1));
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
		testDoc.put(MdekKeys.SYMBOL_CAT, "TEST " + MdekKeys.SYMBOL_CAT);
		testDoc.put(MdekKeys.SYMBOL_CAT_KEY, new Integer(-1));
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
		technicalDomain.put(MdekKeys.TYPE_OF_DOCUMENT, "TEST " + MdekKeys.TYPE_OF_DOCUMENT);
		technicalDomain.put(MdekKeys.TYPE_OF_DOCUMENT_KEY, new Integer(-1));
		technicalDomain.put(MdekKeys.VOLUME, "TEST " + MdekKeys.VOLUME);
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, technicalDomain);
		
		// add TECHNICAL DOMAIN SERVICE
		technicalDomain = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		technicalDomain = (technicalDomain == null) ? new IngridDocument() : technicalDomain;
		technicalDomain.put(MdekKeys.SERVICE_TYPE, "TEST SERVICE_TYPE");
		technicalDomain.put(MdekKeys.SERVICE_TYPE_KEY, new Integer(-1));
		technicalDomain.put(MdekKeys.SYSTEM_HISTORY, "TEST SYSTEM_HISTORY");
		technicalDomain.put(MdekKeys.SYSTEM_ENVIRONMENT, "TEST SYSTEM_ENVIRONMENT");
		technicalDomain.put(MdekKeys.DATABASE_OF_SYSTEM, "TEST DATABASE_OF_SYSTEM");
		technicalDomain.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, "TEST DESCRIPTION_OF_TECH_DOMAIN");
		oDocIn.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, technicalDomain);
		// add TECHNICAL DOMAIN SERVICE - versions
		strList = (List<String>) technicalDomain.get(MdekKeys.SERVICE_VERSION_LIST);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST SERVICE_VERSION1");
		strList.add("TEST SERVICE_VERSION2");
		technicalDomain.put(MdekKeys.SERVICE_VERSION_LIST, strList);
		// add TECHNICAL DOMAIN SERVICE - operations
		docList = (List<IngridDocument>) technicalDomain.get(MdekKeys.SERVICE_OPERATION_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		testDoc = new IngridDocument();
		testDoc.put(MdekKeys.SERVICE_OPERATION_NAME, "TEST SERVICE_OPERATION_NAME");
		testDoc.put(MdekKeys.SERVICE_OPERATION_NAME_KEY, new Integer(-1));
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
		testDoc.put(MdekKeys.COMMENT, "TEST " + MdekKeys.COMMENT);
		testDoc.put(MdekKeys.CREATE_TIME, "12345678901234567");
		docList.add(testDoc);
		oDocIn.put(MdekKeys.COMMENT_LIST, docList);

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		System.out.println("storeObject WITHOUT refetching object: ");
		response = mdekCallerObject.storeObject(plugId, oDocIn, false, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			System.out.println("refetch Object");
			IngridDocument oRefetchedDoc = fetchObject(uuidStoredObject, Quantity.DETAIL_ENTITY);
			System.out.println("");
			
			if (aRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.ADR_REFERENCES_TO);
				docList.add(aRemoved);
				System.out.println("ADD REMOVED ADDRESS AGAIN: " + aRemoved);
			}
			if (oRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.OBJ_REFERENCES_TO);
				docList.add(oRemoved);
				System.out.println("ADD REMOVED OBJECT QUERVERWEIS AGAIN: " + oRemoved);
			}
			if (locRemoved != null) {
				docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.LOCATIONS);
				docList.add(locRemoved);
				System.out.println("ADD REMOVED LOCATION AGAIN: " + locRemoved);
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
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.EXPORTS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oRefetchedDoc.put(MdekKeys.EXPORTS, docList);				
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

			// store
			System.out.println("STORE");
			startTime = System.currentTimeMillis();
			System.out.println("storeObject WITH refetching object: ");
			response = mdekCallerObject.storeObject(plugId, oRefetchedDoc, true, myUserId);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);

			if (result != null) {
				System.out.println("SUCCESS: ");
				debugObjectDoc(result);
			} else {
				handleError(response);
			}					
			
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument publishObject(IngridDocument oDocIn,
			boolean withRefetch,
			boolean forcePublicationCondition) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE publishObject ######");
		System.out.println("publishObject -> " +
				"refetchObject: " + withRefetch +
				", forcePublicationCondition: " + forcePublicationCondition);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.publishObject(plugId, oDocIn, withRefetch, forcePublicationCondition, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			if (withRefetch) {
				debugObjectDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument moveObject(String fromUuid, String toUuid,
			boolean performSubtreeCheck,
			boolean forcePublicationCondition) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String performCheckInfo = (performSubtreeCheck) ? "WITH CHECK SUBTREE (working copies) " 
			: "WITHOUT CHECK SUBTREE (working copies) ";
		String forcePubCondInfo = (forcePublicationCondition) ? "WITH FORCE publicationCondition" 
				: "WITHOUT FORCE publicationCondition";
		System.out.println("\n###### INVOKE moveObject " + performCheckInfo + forcePubCondInfo + "######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.moveObject(plugId, fromUuid, toUuid, performSubtreeCheck, forcePublicationCondition, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		System.out.println("\n###### INVOKE copyObject " + copySubtreeInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.copyObject(plugId, fromUuid, toUuid, copySubtree, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
			System.out.println("Root Copy: " + result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private void trackRunningJob(int sleepTimeMillis, boolean doCancel) {
		IngridDocument response;
		IngridDocument result;
		System.out.println("\n###### INVOKE getRunningJobInfo ######");

		boolean jobIsRunning = true;
		int counter = 0;
		while (jobIsRunning) {
			if (doCancel && counter > 4) {
				cancelRunningJob();
				return;
			}

			response = mdekCaller.getRunningJobInfo(plugId, myUserId);
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
				Integer numObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
				Integer total = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);
				if (jobDescr == null) {
					// job finished !
					jobIsRunning = false;					
					System.out.println("JOB FINISHED\n");
				} else {
					System.out.println("job:" + jobDescr + ", entities:" + numObjs + ", total:" + total);
				}
			} else {
				handleError(response);
				jobIsRunning = false;
			}
			
			try {
				Thread.sleep(sleepTimeMillis);				
			} catch(Exception ex) {
				System.out.println(ex);
			}
			counter++;
		}
	}

	private void cancelRunningJob() {
		System.out.println("\n###### INVOKE cancelRunningJob ######");

		IngridDocument response = mdekCaller.cancelRunningJob(plugId, myUserId);
		IngridDocument result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
			if (jobDescr == null) {
				System.out.println("JOB FINISHED\n");
			} else {
				System.out.println("JOB CANCELED: " + result);
			}
		} else {
			handleError(response);
		}
	}

	private IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObjectWorkingCopy " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObjectWorkingCopy(plugId, uuid, forceDeleteReferences, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			Boolean fullyDeleted = (Boolean) result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED);
			System.out.println("was fully deleted: " + fullyDeleted);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObject " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObject(plugId, uuid, forceDeleteReferences, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			Boolean fullyDeleted = (Boolean) result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED);
			System.out.println("was fully deleted: " + fullyDeleted);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private void debugObjectDoc(IngridDocument o) {
		System.out.println("Object: " + o.get(MdekKeys.ID) 
			+ ", " + o.get(MdekKeys.UUID)
			+ ", " + o.get(MdekKeys.TITLE)
//		);
//		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.get(MdekKeys.PUBLICATION_CONDITION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + o);

		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			for (IngridDocument a : docList) {
				System.out.println("   " + a.get(MdekKeys.UUID) + ": " + a);								
				List<IngridDocument> coms = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
				if (coms != null) {
					System.out.println("    Communication: " + coms.size() + " Entities");
					for (IngridDocument c : coms) {
						System.out.println("     " + c);
					}					
				}
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LOCATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LINKAGES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) o.get(MdekKeys.EXPORTS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.LEGISLATIONS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATA_FORMATS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) o.get(MdekKeys.ENV_CATEGORIES);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Categories: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.ENV_TOPICS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) o.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null && intList.size() > 0) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (myDoc != null) {
			System.out.println("  technical domain MAP:");
			System.out.println("    " + myDoc);								
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.KEY_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - key catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - publication scales: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SYMBOL_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - symbol catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			strList = (List<String>) myDoc.get(MdekKeys.FEATURE_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - feature types: " + strList.size() + " entries");
				for (String str : strList) {
					System.out.println("     " + str);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.GEO_VECTOR_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - vector formats, geo vector list: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			intList = (List<Integer>) myDoc.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - spatial rep types: " + intList.size() + " entries");
				for (Integer i : intList) {
					System.out.println("     " + i);								
				}			
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (myDoc != null) {
			System.out.println("  technical domain DOCUMENT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (myDoc != null) {
			System.out.println("  technical domain SERVICE:");
			System.out.println("    " + myDoc);								
			strList = (List<String>) myDoc.get(MdekKeys.SERVICE_VERSION_LIST);
			if (strList != null && strList.size() > 0) {
				System.out.println("    SERVICE - versions: " + strList.size() + " entries");
				System.out.println("     " + strList);
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_OPERATION_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - operations: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
					strList = (List<String>) doc.get(MdekKeys.PLATFORM_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - platforms: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.DEPENDS_ON_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - dependsOns: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.CONNECT_POINT_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - connectPoints: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					List<IngridDocument> docList2 = (List<IngridDocument>) doc.get(MdekKeys.PARAMETER_LIST);
					if (docList2 != null) {
						System.out.println("      SERVICE - operation - parameters: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
					}
				}
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (myDoc != null) {
			System.out.println("  technical domain PROJECT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (myDoc != null) {
			System.out.println("  technical domain DATASET:");
			System.out.println("    " + myDoc);								
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		myDoc = (IngridDocument) o.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
	}

	private void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			

		if (!doFullOutput) {
			return;
		}

		// detailed output  
		List<MdekError> errors = mdekCaller.getErrorsFromResponse(response);
		doFullOutput = false;
		for (MdekError err : errors) {
			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				IngridDocument info = err.getErrorInfo();
				// referenced object
				debugObjectDoc(info);
				// objects referencing
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}
			}
		}
		doFullOutput = true;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
