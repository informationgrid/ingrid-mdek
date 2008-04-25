package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerObject;
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
		MdekCallerObject.initialize(mdekCaller);
		MdekCallerAddress.initialize(mdekCaller);

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
	String myUserUuid;
	boolean doFullOutput = true;
	
	private boolean isRunning = false;

	// MDEK SERVER TO CALL !
	private String plugId = "mdek-iplug-idctest";
	
	private IMdekCaller mdekCaller;
	private IMdekCallerSecurity mdekCallerSecurity;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerAddress mdekCallerAddress;

	public MdekExampleSecurityThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		myUserUuid = "EXAMPLE_USER_" + threadNumber;
		
		mdekCaller = MdekCaller.getInstance();
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
	}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		String addrUuid;
		String objUuid;

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

		IngridDocument newGroupDoc = new IngridDocument();
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = createGroup(newGroupDoc, true);
		Long newGroupId = (Long) newGroupDoc.get(MdekKeysSecurity.IDC_GROUP_ID);

		System.out.println("\n----- get group details -----");
		newGroupDoc = getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- change name of group and store -----");
		nameNewGrp += " CHANGED!";
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = storeGroup(newGroupDoc, true);

		// ===================================

		System.out.println("\n----------------------------");
		System.out.println("\n----- TEST USER STUFF -----");
		System.out.println("\n----------------------------");

		System.out.println("\n----- get catalog admin -----");
		IngridDocument catalogAdminDoc = getCatalogAdmin();
		Long catalogAdminId = (Long) catalogAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = catalogAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		
		System.out.println("\n----- create new user METADATA_ADMINISTRATOR -----");
		IngridDocument newMetaAdminDoc = new IngridDocument();
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE6-FE15-11D2-AF34-0060084A4596");
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdminDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdminDoc = createUser(newMetaAdminDoc, true);
		Long newMetaAdminId = (Long) newMetaAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdminUuid = newMetaAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new user METADATA_AUTHOR -----");
		IngridDocument newMetaAuthorDoc = new IngridDocument();
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "386645BC-B449-11D2-9A86-080000507261");
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_AUTHOR.getDbValue());
		newMetaAuthorDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdminId);
		newMetaAuthorDoc = createUser(newMetaAuthorDoc, true);
		Long newMetaAuthorId = (Long) newMetaAuthorDoc.get(MdekKeysSecurity.IDC_USER_ID);

		System.out.println("\n----- get sub users -----");
		getSubUsers(catalogAdminId);
		getSubUsers(newMetaAdminId);

		System.out.println("\n----- change addr uuid of user and store -----");
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "6C6A3485-59E0-11D3-AE74-00104B57C66D");
		newMetaAuthorDoc = storeUser(newMetaAuthorDoc, true);		
		String newMetaAuthorUuid = newMetaAuthorDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		
		// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("\n----- TEST PERMISSIONS -----");
		System.out.println("\n----------------------------");

		System.out.println("\n--------------------------------------");
		System.out.println("\n----- ADDRESS/OBJECT PERMISSIONS -----");
		System.out.println("\n--------------------------------------");

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		myUserUuid = catalogAdminUuid;

		System.out.println("\n----- delete address working copy -> ALLOWED -----");
		addrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object working copy -> ALLOWED  -----");
		objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";
		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (no permissions) -----");
		myUserUuid = newMetaAuthorUuid;

		System.out.println("\n----- delete address working copy -> NOT ALLOWED -----");
		deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object working copy -> NOT ALLOWED -----");
		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- add address/object permissions to group -----");
		// object permission
		List<IngridDocument> perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, objUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE.getDbValue());
		perms.add(newPerm);
		// address permission
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, addrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE.getDbValue());
		perms.add(newPerm);
		newGroupDoc = storeGroup(newGroupDoc, true);

		System.out.println("\n----- delete address working copy -> ALLOWED -----");
		deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object working copy -> ALLOWED -----");
		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- remove permissions from group -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroupDoc = storeGroup(newGroupDoc, true);

		System.out.println("\n----------------------------");
		System.out.println("\n----- USER PERMISSIONS -----");
		System.out.println("\n----------------------------");

//		System.out.println("\n----- create top object -> NOT ALLOWED -----");
//		deleteObjectWorkingCopy(objUuid, true);

//		System.out.println("\n----- create top address -> NOT ALLOWED -----");
//		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- add user permissions -----");
		perms = (List<IngridDocument>) newMetaAuthorDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.CREATE_ROOT.getDbValue());
		perms.add(newPerm);
		newMetaAuthorDoc = storeUser(newMetaAuthorDoc, true);

		// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("\n----- CLEAN UP -----");
		System.out.println("\n----------------------------");
		
		System.out.println("\n----- remove users -----");
		deleteUser(newMetaAdminId);
		deleteUser(newMetaAuthorId);

		System.out.println("\n----- remove group -----");
		deleteGroup(newGroupId);

		// ===================================

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
		response = mdekCallerSecurity.getGroups(plugId, myUserUuid);
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
		response = mdekCallerSecurity.createGroup(plugId, docIn, refetch, myUserUuid);
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
		response = mdekCallerSecurity.storeGroup(plugId, docIn, refetch, myUserUuid);
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
		response = mdekCallerSecurity.getGroupDetails(plugId, grpName, myUserUuid);
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
		response = mdekCallerSecurity.getUserDetails(plugId, addrUuid, myUserUuid);
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
		response = mdekCallerSecurity.getCatalogAdmin(plugId, myUserUuid);
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
		response = mdekCallerSecurity.createUser(plugId, docIn, refetch, myUserUuid);
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
		response = mdekCallerSecurity.storeUser(plugId, docIn, refetch, myUserUuid);
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
		response = mdekCallerSecurity.deleteUser(plugId, idcUserId, myUserUuid);
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

	private IngridDocument getSubUsers(Long parentUserId) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSubUsers ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getSubUsers(plugId, parentUserId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				debugUserDoc((IngridDocument)o);
			}
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
		response = mdekCallerSecurity.deleteGroup(plugId, idcGroupId, myUserUuid);
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
		response = mdekCallerObject.deleteObjectWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
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
		response = mdekCallerAddress.deleteAddressWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
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

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}

	private void debugUserDoc(IngridDocument u) {
		System.out.println("User: " + u.get(MdekKeysSecurity.IDC_USER_ID) 
			+ ", " + u.get(MdekKeysSecurity.IDC_USER_ADDR_UUID)
			+ ", name: " + u.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + u.get(MdekKeys.GIVEN_NAME)
			+ " " + u.get(MdekKeys.NAME)
			+ ", organisation: " + u.get(MdekKeys.ORGANISATION)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + u.get(MdekKeys.MOD_UUID)
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + u);

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) u.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  User Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
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
