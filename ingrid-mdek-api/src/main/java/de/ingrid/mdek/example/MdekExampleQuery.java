package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
import de.ingrid.utils.IngridDocument;

public class MdekExampleQuery {

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

		// and our specific job caller !
		MdekCallerQuery.initialize(mdekCaller);
		MdekCallerAddress.initialize(mdekCaller);
		MdekCallerObject.initialize(mdekCaller);

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
		MdekExampleQueryThread[] threads = new MdekExampleQueryThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleQueryThread(i+1);
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

class MdekExampleQueryThread extends Thread {

	private int threadNumber;
	String myUserId;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	// MDEK SERVER TO CALL !
	private String plugId = "mdek-iplug-idctest";
	
	private IMdekCaller mdekCaller;
	private IMdekCallerQuery mdekCallerQuery;
	private IMdekCallerAddress mdekCallerAddress;
	private IMdekCallerObject mdekCallerObject;

	public MdekExampleQueryThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserId = "EXAMPLE_USER_" + threadNumber;
		
		mdekCaller = MdekCaller.getInstance();
		mdekCallerQuery = MdekCallerQuery.getInstance();
		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
	}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		// thesaurs terms (sns ids)
		// Naturschutz = uba_thes_28749: Institutionen, Einheiten und Personen
//		String termSnsId = "uba_thes_28749";
		// Emissionsüberwachung = uba_thes_8007: Institutionen und Personen
		String termSnsId = "uba_thes_8007";

		String uuid;
		String searchterm;
		IngridDocument doc;
		List<IngridDocument> hits;

		boolean alwaysTrue = true;

		String hqlQueryAddr1 =
			"select distinct aNode, addr.adrUuid, addr.adrType, addr.institution, addr.lastname, termVal.term " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr " +
			"inner join addr.searchtermAdrs termAdrs " +
			"inner join termAdrs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			"termSns.snsId = '" + termSnsId + "' " +
			"order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		String hqlQueryAddr2 = "from AddressNode";

		String hqlQueryObj1 = "select distinct oNode, obj.objName, termVal.term " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join obj.searchtermObjs termObjs " +
			"inner join termObjs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			"termSns.snsId = '" + termSnsId + "' " +
			"order by obj.objClass, obj.objName";

		String hqlQueryObj2 = "from ObjectNode";

// ====================
// test single stuff
// -----------------------------------
/*
		// add functionality !

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// ===================================
*/

		System.out.println("\n\n=========================");
		System.out.println(" QUERY/UPDATE FULL TEXT ADDRESS");
		System.out.println("=========================");

		System.out.println("\n----- search addresses via full text (searchterm is syslist entry !) -----");
		searchterm = "Prof. Dr.";
		queryAddressesFullText(searchterm, 0, 20);

		System.out.println("\n----- check: update address index on STORE -----");
		System.out.println("----- search address via full text -> no result -----");
		searchterm = "sdfhljkhfösh";
		queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch arbitrary address -----");
		uuid = "095130C2-DDE9-11D2-BB32-006097FE70B1";
		doc = fetchAddress(uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- change organization to searchterm and STORE (result is WORKING COPY !!!) -----");
		doc.put(MdekKeys.ORGANISATION, searchterm);
		storeAddress(doc, true);
		System.out.println("\n----- search again via full text -> RESULT (is working copy !) -----");
		queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up -----");
		deleteAddressWorkingCopy(uuid, true);

		System.out.println("\n----- check: update address index on PUBLISH -----");
		System.out.println("----- search address via full text -> no result -----");
		queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch again -----");
		doc = fetchAddress(uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- change organization to searchterm and PUBLISH -----");
		String origOrganization = doc.getString(MdekKeys.ORGANISATION);
		doc.put(MdekKeys.ORGANISATION, searchterm);
		doc = publishAddress(doc, true);
		System.out.println("\n----- search again via full text -> RESULT (is published one, no separate working copy) -----");
		queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up (set orig data and publish) -----");
		doc.put(MdekKeys.ORGANISATION, origOrganization);
		publishAddress(doc, true);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" QUERY/UPDATE FULL TEXT OBJECT");
		System.out.println("=========================");

		System.out.println("\n----- search objects via full text (searchterm is syslist entry !) -----");
		searchterm = "Basisdaten";
		queryObjectsFullText(searchterm, 0, 20);

		System.out.println("\n----- check: update object index on STORE -----");
		System.out.println("----- search object via full text -> no result -----");
		searchterm = "sdfhljkhfösh";
		queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch arbitrary object -----");
		uuid = "3A295152-5091-11D3-AE6C-00104B57C66D";
		doc = fetchObject(uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- change title to searchterm and STORE (result is WORKING COPY !!!) -----");
		doc.put(MdekKeys.TITLE, searchterm);
		storeObject(doc, true);
		System.out.println("\n----- search again via full text -> RESULT (is working copy !) -----");
		queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up -----");
		deleteObjectWorkingCopy(uuid, true);

		System.out.println("\n----- check: update object index on PUBLISH -----");
		System.out.println("----- search object via full text -> no result -----");
		queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch again -----");
		doc = fetchObject(uuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- change title to searchterm and PUBLISH -----");
		String origTitle = doc.getString(MdekKeys.TITLE);
		doc.put(MdekKeys.TITLE, searchterm);
		doc = publishObject(doc, true, false);
		System.out.println("\n----- search again via full text -> RESULT (is published one, no separate working copy) -----");
		queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up (set orig data and publish) -----");
		doc.put(MdekKeys.TITLE, origTitle);
		publishObject(doc, true, false);
/*
		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" THESAURUS QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by thesaurus term (id) -----");
		hits = queryAddressesThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			fetchAddress(uuid, Quantity.DETAIL_ENTITY);
		}

		System.out.println("\n----- search objects by thesaurus term (id) -----");
		hits = queryObjectsThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			fetchObject(uuid, Quantity.DETAIL_ENTITY);
		}

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by hql query -----");
		queryHQL(hqlQueryAddr1, 0, 10);
		queryHQL(hqlQueryAddr2, 0, 10);

		System.out.println("\n----- search objects by hql query -----");
		queryHQL(hqlQueryObj1, 0, 10);
		queryHQL(hqlQueryObj2, 0, 10);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY TO CSV");
		System.out.println("=========================");

		doFullOutput = false;

		System.out.println("\n----- search objects by hql to csv -----");
		queryHQLToCsv(hqlQueryObj1);
		String hqlQueryObj3 = "select distinct obj " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"order by obj.objClass, obj.objName";
		queryHQLToCsv(hqlQueryObj3);

		System.out.println("\n----- search addresses by hql to csv -----");
		queryHQLToCsv(hqlQueryAddr1);
		String hqlQueryAddr3 = "select distinct addr " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr " +
			"order by addr.adrType, addr.institution, addr.lastname, addr.firstname";
		queryHQLToCsv(hqlQueryAddr3);

		doFullOutput = true;

		// ===================================

		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	private IngridDocument fetchAddress(String uuid, Quantity howMuch) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, howMuch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeAddress(IngridDocument aDocIn, boolean refetchAddress) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchAddressInfo = (refetchAddress) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeAddress " + refetchAddressInfo + " ######");

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, refetchAddress, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			doFullOutput = false;
			debugAddressDoc(result);
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch) {
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String withRefetchInfo = (withRefetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE publishAddress  " + withRefetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.publishAddress(plugId, aDocIn, withRefetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuid = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuid);
			if (withRefetch) {
				debugAddressDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument deleteAddressWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddressWorkingCopy " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddressWorkingCopy(plugId, uuid, forceDeleteReferences, myUserId);
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

	private IngridDocument storeObject(IngridDocument oDocIn,
			boolean refetchObject) {
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetchObject) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeObject " + refetchInfo + " ######");

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

	private List<IngridDocument> queryAddressesFullText(String queryTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- queryTerm:" + queryTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesFullText(plugId, queryTerm, startHit, numHits, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	private List<IngridDocument> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	private List<IngridDocument> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchTerm:" + searchTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsFullText(plugId, searchTerm, startHit, numHits, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	private List<IngridDocument> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	private void queryHQL(String qString,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryHQL ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- query:" + qString);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryHQL(plugId, qString, startHit, numHits, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			IdcEntityType type = IdcEntityType.OBJECT;
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
				type = IdcEntityType.ADDRESS;				
			}
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				if (IdcEntityType.OBJECT.equals(type)) {
					debugObjectDoc(hit);
				} else {
					debugAddressDoc(hit);
				}
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}
	}

	private void queryHQLToCsv(String qString) {
		try {
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			System.out.println("\n###### INVOKE queryHQLToCsv ######");
			System.out.println("- query:" + qString);
			startTime = System.currentTimeMillis();
			response = mdekCallerQuery.queryHQLToCsv(plugId, qString, myUserId);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
				System.out.println("SUCCESS: " + totalNumHits + " csvLines returned (and additional title-line)");
				String csvResult = result.getString(MdekKeys.CSV_RESULT);			
				if (doFullOutput) {
					System.out.println(csvResult);
				} else {
					if (csvResult.length() > 5000) {
						int endIndex = csvResult.indexOf("\n", 3000);
						System.out.print(csvResult.substring(0, endIndex));					
						System.out.println("...");					
					} else {
						System.out.println(csvResult);					
					}
				}

			} else {
				handleError(response);
			}			
		} catch (Throwable t) {
			System.out.println("\nCatched Throwable in Example:");
			printThrowable(t);
		}
	}

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", title (key/value): " + a.get(MdekKeys.TITLE_OR_FUNCTION_KEY)
			+ " " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ ", name: " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
//		);
//		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + a);

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
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

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
	}

	private void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			
		
	}

	private void printThrowable(Throwable t) {
		System.out.println(t);
		System.out.println("   Stack Trace:");
		StackTraceElement[] st = t.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	        System.out.println(stackTraceElement);
        }
		Throwable cause = t.getCause();
		if (cause != null) {
			System.out.println("   Cause:");
			printThrowable(cause);			
		}
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
