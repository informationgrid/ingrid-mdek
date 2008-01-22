package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekCaller.Quantity;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;

public class MdekExample {

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

		// INITIALIZE MDEK INTERFACE ONCE !
		MdekCaller.initialize(new File((String) map.get("--descriptor")));

		// start threads calling job
		System.out.println("\n###### OUTPUT THREADS ######\n");
		MdekThread[] threads = new MdekThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekThread(i+1);
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

class MdekThread extends Thread {

	private int threadNumber;
	
	private boolean isRunning = false;

	public MdekThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
	}

	public void run() {
		isRunning = true;

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
		//IMdekCaller mdekCaller = MdekCaller.getInstance();
		//mdekCaller.testMdekEntity(threadNumber);

		// -----------------------------------
		// ui: initial lists

		System.out.println("\n----- UI List Values -----");
		getUiListValues();

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
		storeObject(oMap);

		System.out.println("\n----- discard changes -> back to published version -----");
		deleteObjectWorkingCopy(objUuid);
		
		System.out.println("\n----- and reload -----");
		oMap = fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------
		// object: store NEW object and verify associations
		System.out.println("\n\n=========================");
		System.out.println("STORE TEST new object");
		System.out.println("=========================");

		System.out.println("\n----- store new object (with address, object references, spatial refs ...) -----");
		IngridDocument objDoc = new IngridDocument();
		objDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		objDoc.put(MdekKeys.ADR_REFERENCES_TO, oMap.get(MdekKeys.ADR_REFERENCES_TO));
		objDoc.put(MdekKeys.OBJ_REFERENCES_TO, oMap.get(MdekKeys.OBJ_REFERENCES_TO));
		objDoc.put(MdekKeys.LOCATIONS, oMap.get(MdekKeys.LOCATIONS));
		objDoc.put(MdekKeys.SUBJECT_TERMS, oMap.get(MdekKeys.SUBJECT_TERMS));
		// supply parent uuid !
		objDoc.put(MdekKeys.PARENT_UUID, parentUuid);

		oMap = storeObject(objDoc);
		// uuid created !
		String newObjUuid = (String) oMap.get(MdekKeys.UUID);

		System.out.println("\n----- verify new subobject -> load parent subobjects -----");
		fetchSubObjects(parentUuid);

		// -----------------------------------
		// tree: move object sub tree
		System.out.println("\n\n=========================");
		System.out.println("MOVE TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- move new object WITHOUT CHECK -----");
		String oldParentUuid = parentUuid;
		String newParentUuid = objUuid;
		moveObject(newObjUuid, newParentUuid, false);
		System.out.println("\n----- verify old parent subobjects (cut) -----");
		fetchSubObjects(oldParentUuid);
		System.out.println("\n----- verify new parent subobjects (added) -----");
		fetchSubObjects(newParentUuid);
		System.out.println("\n----- check new parent subtree -----");
		checkObjectSubTree(newParentUuid);
		System.out.println("\n\n----- move new parent WITH CHECK (not allowed) -----");
		moveObject(newParentUuid, null, true);
		System.out.println("\n----- do \"forbidden\" move (move to subnode) -----");
		moveObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", false);

		// -----------------------------------
		// tree: copy object sub tree
		System.out.println("\n\n=========================");
		System.out.println("COPY TEST");
		System.out.println("=========================");

		System.out.println("\n\n----- copy parent of new object to top (WITHOUT sub tree) -----");
		String objectFrom = newParentUuid;
		String objectTo = null;
		oMap = copyObject(objectFrom, objectTo, false);
		String copy1Uuid = (String)oMap.get(MdekKeys.UUID);
		System.out.println("\n\n----- verify copy  -----");
		System.out.println("\n\n----- load original one -----");
		fetchObject(objectFrom, Quantity.DETAIL_ENTITY);
		System.out.println("\n\n----- load copy -----");
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
		deleteObjectWorkingCopy(copy1Uuid);
		deleteObjectWorkingCopy(copy2Uuid);
		System.out.println("\n\n----- copy tree to own subnode !!! copy parent of new object below new object (WITH sub tree) -----");
		copyObject(newParentUuid, newObjUuid, true);
		System.out.println("\n\n----- verify copy -> load children of new object -----");
		fetchSubObjects(newObjUuid);
		// Following is allowed now ! Don't execute -> huge tree is copied !
//		System.out.println("\n----- do \"forbidden\" copy -----");
//		copyObject("3866463B-B449-11D2-9A86-080000507261", "15C69C20-FE15-11D2-AF34-0060084A4596", true);

		// -----------------------------------
		// object: delete new object and verify deletion
		System.out.println("\n\n=========================");
		System.out.println("DELETE TEST");
		System.out.println("=========================");

		System.out.println("\n----- delete new object (WORKING COPY) -> FULL DELETE -----");
		deleteObjectWorkingCopy(newObjUuid);
		System.out.println("\n----- verify deletion of new object -----");
		fetchObject(newObjUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subobjects -----");
		fetchSubObjects(newParentUuid);

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
		// sub object of unpublished parent !!!
		newPubDoc.put(MdekKeys.PARENT_UUID, pub1Uuid);
		publishObject(newPubDoc, true);

		System.out.println("\n----- refetch FULL PARENT and change title, IS UNPUBLISHED !!! -----");
		oMap = fetchObject(pub1Uuid, Quantity.DETAIL_ENTITY);
		oMap.put(MdekKeys.TITLE, "COPIED, Title CHANGED and PUBLISHED: " + oMap.get(MdekKeys.TITLE));	

		System.out.println("\n----- and publish PARENT -> create pub version/delete work version -----");
		publishObject(oMap, true);

		System.out.println("\n----- NOW CREATE AND PUBLISH OF NEW CHILD POSSIBLE -> create pub version, set also as work version -----");
		oMap = publishObject(newPubDoc, true);
		// uuid created !
		String pub2Uuid = (String) oMap.get(MdekKeys.UUID);

		System.out.println("\n----- verify -> load top objects -----");
		fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (WORKING COPY) -> NO DELETE -----");
		deleteObjectWorkingCopy(pub1Uuid);
		System.out.println("\n----- delete 2. published copy (FULL) -----");
		deleteObject(pub2Uuid);
		System.out.println("\n----- verify -> load top objects -----");
		fetchTopObjects();
		System.out.println("\n----- delete 1. published copy (FULL) -----");
		deleteObject(pub1Uuid);


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

/*
		System.out.println("\n###### INVOKE fetchTopAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchTopAddresses();
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchSubAddresses("0DAE03C6-373D-45FE-AF45-4D8359750A08");
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
*/

		isRunning = false;
	}
	
	private IngridDocument getUiListValues() {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUiListValues ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.getUiListValues();
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
			List l = (List) result.get(MdekKeys.UI_FREE_SPATIAL_REFERENCES);
			System.out.println("  freie Raumbezüge (" + l.size() + "): " + l);
			
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument fetchTopObjects() {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchTopObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchTopObjects();
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
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchSubObjects(uuid);
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
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.getObjectPath(uuidIn);
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
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchObject(uuid, howMuch);
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
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkObjectSubTree ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.checkObjectSubTree(uuid);
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

	private IngridDocument storeObject(IngridDocument oDocIn) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
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
		IngridDocument newTerm = new IngridDocument();
		newTerm.put(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue());
		newTerm.put(MdekKeys.TERM_NAME, "TEST Freier Searchterm !");
		System.out.println("ADD NEW SUBJECT TERM: " + newTerm);
		docList.add(newTerm);

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
		List<String> strList = (List<String>) oDocIn.get(MdekKeys.EXPORTS);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST NEW T014InfoImpart entry");
		oDocIn.put(MdekKeys.EXPORTS, strList);

		// add entry to LEGISLATIONS
		strList = (List<String>) oDocIn.get(MdekKeys.LEGISLATIONS);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST NEW T015Legist entry");
		oDocIn.put(MdekKeys.LEGISLATIONS, strList);

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
		
		// add entry to DATA_FORMATS
		docList = (List<IngridDocument>) oDocIn.get(MdekKeys.DATA_FORMATS);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
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

		// add entry to ENV_CATEGORIES
		strList = (List<String>) oDocIn.get(MdekKeys.ENV_CATEGORIES);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST NEW T0114EnvCategory entry");
		oDocIn.put(MdekKeys.ENV_CATEGORIES, strList);

		// add entry to ENV_TOPICS
		strList = (List<String>) oDocIn.get(MdekKeys.ENV_TOPICS);
		strList = (strList == null) ? new ArrayList<String>() : strList;
		strList.add("TEST NEW T0114EnvTopic entry");
		oDocIn.put(MdekKeys.ENV_TOPICS, strList);

		// add entry to TOPIC_CATEGORIES
		List<Integer> intList = (List<Integer>) oDocIn.get(MdekKeys.TOPIC_CATEGORIES);
		intList = (intList == null) ? new ArrayList<Integer>() : intList;
		intList.add(1);
		oDocIn.put(MdekKeys.TOPIC_CATEGORIES, intList);


		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		System.out.println("storeObject WITHOUT refetching object: ");
		response = mdekCaller.storeObject(oDocIn, false);
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
			strList = (List<String>) oRefetchedDoc.get(MdekKeys.EXPORTS);
			if (strList != null && strList.size() > 0) {
				strList.remove(strList.size()-1);
				oDocIn.put(MdekKeys.EXPORTS, strList);				
			}

			// LEGISLATIONS wieder wie vorher !
			strList = (List<String>) oRefetchedDoc.get(MdekKeys.LEGISLATIONS);
			if (strList != null && strList.size() > 0) {
				strList.remove(strList.size()-1);
				oDocIn.put(MdekKeys.LEGISLATIONS, strList);				
			}

			// DATA_FORMATS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.DATA_FORMATS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oDocIn.put(MdekKeys.DATA_FORMATS, docList);				
			}

			// MEDIUM_OPTIONS wieder wie vorher !
			docList = (List<IngridDocument>) oRefetchedDoc.get(MdekKeys.MEDIUM_OPTIONS);
			if (docList != null && docList.size() > 0) {
				docList.remove(docList.size()-1);
				oDocIn.put(MdekKeys.MEDIUM_OPTIONS, docList);				
			}

			// ENV_CATEGORIES wieder wie vorher !
			strList = (List<String>) oRefetchedDoc.get(MdekKeys.ENV_CATEGORIES);
			if (strList != null && strList.size() > 0) {
				strList.remove(strList.size()-1);
				oDocIn.put(MdekKeys.ENV_CATEGORIES, strList);				
			}

			// ENV_TOPICS wieder wie vorher !
			strList = (List<String>) oRefetchedDoc.get(MdekKeys.ENV_TOPICS);
			if (strList != null && strList.size() > 0) {
				strList.remove(strList.size()-1);
				oDocIn.put(MdekKeys.ENV_TOPICS, strList);				
			}

			// TOPIC_CATEGORIES wieder wie vorher !
			intList = (List<Integer>) oRefetchedDoc.get(MdekKeys.TOPIC_CATEGORIES);
			if (intList != null && intList.size() > 0) {
				intList.remove(intList.size()-1);
				oDocIn.put(MdekKeys.TOPIC_CATEGORIES, intList);				
			}


			// store
			System.out.println("STORE");
			startTime = System.currentTimeMillis();
			System.out.println("storeObject WITH refetching object: ");
			response = mdekCaller.storeObject(oRefetchedDoc, true);
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

	private IngridDocument publishObject(IngridDocument oDocIn, boolean withRefetch) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE publishObject ######");
		startTime = System.currentTimeMillis();
		System.out.println("publishObject -> refetching object: " + withRefetch);
		response = mdekCaller.publishObject(oDocIn, withRefetch);
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

	private IngridDocument moveObject(String fromUuid, String toUuid, boolean performCheck) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String performCheckInfo = (performCheck) ? "WITH CHECK SUBTREE (working copies)" 
			: "WITHOUT CHECK SUBTREE (working copies)";
		System.out.println("\n###### INVOKE moveObject " + performCheckInfo + "######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.moveObject(fromUuid, toUuid, performCheck);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		System.out.println("\n###### INVOKE copyObject " + copySubtreeInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.copyObject(fromUuid, toUuid, copySubtree);
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

	private IngridDocument deleteObjectWorkingCopy(String uuid) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteObjectWorkingCopy ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.deleteObjectWorkingCopy(uuid);
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

	private IngridDocument deleteObject(String uuid) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteObject ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.deleteObject(uuid);
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
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
		);
		System.out.println(" " + o);
		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			for (IngridDocument a : docList) {
				System.out.println("   " + a);								
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
		if (docList != null) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LINKAGES);
		if (docList != null) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) o.get(MdekKeys.EXPORTS);
		if (strList != null) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.LEGISLATIONS);
		if (strList != null) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (myDoc != null) {
			System.out.println("  technical domain map:");
			System.out.println("   " + myDoc);								
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATA_FORMATS);
		if (docList != null) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) o.get(MdekKeys.ENV_CATEGORIES);
		if (strList != null) {
			System.out.println("  Env Categories: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.ENV_TOPICS);
		if (strList != null) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) o.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}
	}

	private void handleError(IngridDocument response) {
		IMdekCaller mdekCaller = MdekCaller.getInstance();
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			
		
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
