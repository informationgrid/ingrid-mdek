package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.utils.IngridDocument;

public class MdekExampleSecurity {

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
		MdekCallerSecurity.initialize(mdekCaller);

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
		MdekExampleSecurityThread[] threads = new MdekExampleSecurityThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleSecurityThread(i+1);
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

class MdekExampleSecurityThread extends Thread {

	private int threadNumber;
	String myUserId;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	// MDEK SERVER TO CALL !
	private String plugId = "mdek-iplug-idctest";
	
	private IMdekCaller mdekCaller;
	private IMdekCallerSecurity mdekCallerSecurity;

	public MdekExampleSecurityThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserId = "EXAMPLE_USER_" + threadNumber;
		
		mdekCaller = MdekCaller.getInstance();
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
	}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

// ====================
// test single stuff
// -----------------------------------
/*
		boolean alwaysTrue = true;

		// add functionality !

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ====================

		// ===================================
		System.out.println("\n----------------------------");
		System.out.println("\n----- TEST GROUP STUFF -----");
		System.out.println("\n----------------------------");

		// -----------------------------------
		System.out.println("\n----- get all groups -----");
		getGroups();

		System.out.println("\n----- create new group -----");
		String nameNewGrp = "neue TEST-Gruppe";

		IngridDocument doc = new IngridDocument();
		doc.put(MdekKeys.NAME, nameNewGrp);
		doc = createGroup(doc, true);

		System.out.println("\n----- get group details -----");
		doc = getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- change name of group and store -----");
		nameNewGrp += " CHANGED!";
		doc.put(MdekKeys.NAME, nameNewGrp);
		doc = storeGroup(doc, true);

		// ===================================
		System.out.println("\n----------------------------");
		System.out.println("\n----- TEST USER STUFF -----");
		System.out.println("\n----------------------------");

		System.out.println("\n----- create new user -----");
		String addrUuid = "15C69BE6-FE15-11D2-AF34-0060084A4596";

		IngridDocument group = getGroupDetails(nameNewGrp);

		doc = new IngridDocument();
		doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuid);
		doc.put(MdekKeysSecurity.IDC_GROUP_ID, group.get(MdekKeysSecurity.IDC_GROUP_ID));
		doc.put(MdekKeysSecurity.IDC_ROLE, MdekKeysSecurity.IDC_ROLE_METADATA_ADMINISTRATOR);
		doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, (Long)(getCatalogAdmin().get(MdekKeysSecurity.IDC_USER_ID)));
		doc = createUser(doc, true);

		System.out.println("\n----- change addr uuid of user and store -----");
		addrUuid = "6C6A3485-59E0-11D3-AE74-00104B57C66D";
		doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuid);
		doc = storeUser(doc, true);		
		
		System.out.println("\n----- fetch user details -----");
		getUserDetails("6C6A3485-59E0-11D3-AE74-00104B57C66D");
		
		
		System.out.println("\n----- remove user -----");
		deleteUser((Long)doc.get(MdekKeysSecurity.IDC_USER_ID));

		System.out.println("\n----- remove group -----");
		deleteGroup((Long)doc.get(MdekKeysSecurity.IDC_GROUP_ID));
		
		
		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	private IngridDocument getGroups() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getGroups ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroups(plugId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.GROUPS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugGroupDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument createGroup(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE createGroup " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.createGroup(plugId, docIn, refetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeGroup " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.storeGroup(plugId, docIn, refetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	private IngridDocument getGroupDetails(String grpName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getGroupDetails ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroupDetails(plugId, grpName, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getUserDetails(String addrUuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserDetails ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserDetails(plugId, addrUuid, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	private IngridDocument getCatalogAdmin() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getCatalogAdmin ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getCatalogAdmin(plugId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	private IngridDocument createUser(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE createUser " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.createUser(plugId, docIn, refetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}	

	
	private IngridDocument storeUser(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeUser " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.storeUser(plugId, docIn, refetch, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}	

	
	private IngridDocument deleteUser(Long idcUserId) {
		if (idcUserId == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteUser ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.deleteUser(plugId, idcUserId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
		} else {
			handleError(response);
		}

		return result;
	}	

	private IngridDocument deleteGroup(Long idcGroupId) {
		if (idcGroupId == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteGroup ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.deleteGroup(plugId, idcGroupId, myUserId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
		} else {
			handleError(response);
		}

		return result;
	}	
	
	
	private void debugGroupDoc(IngridDocument g) {
		System.out.println("Group: " + g.get(MdekKeysSecurity.IDC_GROUP_ID) 
			+ ", " + g.get(MdekKeys.NAME)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + g.get(MdekKeys.MOD_UUID)
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + g);
	}

	private void debugUserDoc(IngridDocument g) {
		System.out.println("User: " + g.get(MdekKeysSecurity.IDC_USER_ID) 
			+ ", " + g.get(MdekKeysSecurity.IDC_USER_ADDR_UUID)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + g.get(MdekKeys.MOD_UUID)
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + g);
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
