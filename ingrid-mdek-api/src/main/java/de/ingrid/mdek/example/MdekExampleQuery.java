package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.IMdekCallerAddress;
import de.ingrid.mdek.IMdekCallerObject;
import de.ingrid.mdek.IMdekCallerQuery;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekCallerAddress;
import de.ingrid.mdek.MdekCallerObject;
import de.ingrid.mdek.MdekCallerQuery;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekCallerAbstract.Quantity;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
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

		// thesaurs terms (sns ids)
		// Naturschutz = uba_thes_28749: Institutionen, Einheiten und Personen
//		String termSnsId = "uba_thes_28749";
		// Emissionsüberwachung = uba_thes_8007: Institutionen und Personen
		String termSnsId = "uba_thes_8007";

		long exampleStartTime = System.currentTimeMillis();

		// ===================================

		System.out.println("\n\n=========================");
		System.out.println(" THESAURUS QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by thesaurus term (id) -----");
		List<IngridDocument> hits = queryAddressesThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			String uuid = hits.get(0).getString(MdekKeys.UUID);
			fetchAddress(uuid, Quantity.DETAIL_ENTITY);
		}

		System.out.println("\n----- search objects by thesaurus term (id) -----");
		hits = queryObjectsThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			String uuid = hits.get(0).getString(MdekKeys.UUID);
			fetchObject(uuid, Quantity.DETAIL_ENTITY);
		}

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by hql query -----");
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
		queryHQL(hqlQueryAddr1, 0, 10);

		String hqlQueryAddr2 = "from AddressNode";
		queryHQL(hqlQueryAddr2, 0, 10);

		System.out.println("\n----- search objects by hql query -----");
		String hqlQueryObj1 = "from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join obj.searchtermObjs termObjs " +
			"inner join termObjs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			"termSns.snsId = '" + termSnsId + "' " +
			"order by obj.objClass, obj.objName";
		queryHQL(hqlQueryObj1, 0, 10);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY TO CSV");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by hql to csv -----");
		queryHQLToCsv(hqlQueryAddr1);
		queryHQLToCsv(hqlQueryAddr2);

		String hqlQueryAddr3 =
			"select distinct addr " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr " +
			"order by addr.adrType, addr.institution, addr.lastname, addr.firstname";
		queryHQLToCsv(hqlQueryAddr3);

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
			System.out.println(csvResult);
			
		} else {
			handleError(response);
		}
	}

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.GIVEN_NAME)
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

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
