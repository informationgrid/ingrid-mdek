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
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
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

		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";

		String addrUuid;
		String objUuid;
		IngridDocument doc;

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
		System.out.println("----- TEST GROUP STUFF -----");
		System.out.println("----------------------------");

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
		System.out.println("----- TEST USER STUFF -----");
		System.out.println("----------------------------");

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
		System.out.println("----- TEST PERMISSIONS -----");
		System.out.println("----------------------------");

		System.out.println("\n------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"DELETE/WRITE\" PERMISSION -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		myUserUuid = catalogAdminUuid;

		System.out.println("\n----- delete address WORKING COPY -> ALLOWED -----");
		addrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object WORKING COPY -> ALLOWED  -----");
		objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";
		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (no permissions) -----");
		myUserUuid = newMetaAuthorUuid;

		System.out.println("\n----- delete address FULL -> NOT ALLOWED -----");
		deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED -----");
		deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- store address -> NOT ALLOWED -----");
		System.out.println("-- first fetch address");
		doc = getAddressDetails(addrUuid);
		storeAddress(doc, false);

		System.out.println("\n----- publish address -> NOT ALLOWED -----");
		publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- store object -> NOT ALLOWED -----");
		System.out.println("-- first fetch object");
		doc = getObjectDetails(objUuid);
		storeObject(doc, false);

		System.out.println("\n----- publish object -> NOT ALLOWED -----");
		publishObject(doc, false, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add address/object WRITE_SINGLE permissions to group -----");
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

		System.out.println("\n----- verify permissions for object -----");
		getObjectPermissions(objUuid);

		System.out.println("\n----- verify permissions for address -----");
		getAddressPermissions(addrUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- delete address FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- delete address WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- write address -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch address");
		doc = getAddressDetails(addrUuid);
		storeAddress(doc, false);

		System.out.println("\n----- publish address -> ALLOWED ALLOWED (WRITE_SINGLE) -----");
		publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- write object -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch object");
		doc = getObjectDetails(objUuid);
		storeObject(doc, false);

		System.out.println("\n----- publish object -> ALLOWED (WRITE_SINGLE) -----");
		publishObject(doc, false, false);

		System.out.println("\n------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE_ROOT\" PERMISSION -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top address -----");
		IngridDocument newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, null);
		newAdrDoc = getInitialAddress(newAdrDoc);
		// extend initial address with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top object -----");
		IngridDocument newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, null);
		newObjDoc = getInitialObject(newObjDoc);
		// extend initial address with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission CREATE_ROOT to group -----");
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.CREATE_ROOT.getDbValue());
		perms.add(newPerm);
		newGroupDoc = storeGroup(newGroupDoc, true);

		System.out.println("\n----- verify user permission -----");
		getUserPermissions();

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top address -> ALLOWED -----");
		newAdrDoc = storeAddress(newAdrDoc, false);
		String newAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create top object -> ALLOWED -----");
		newObjDoc = storeObject(newObjDoc, false);
		String newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify granted WRITE_TREE permissions on new roots -> get group details -----");
		getGroupDetails(nameNewGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- and delete new top entities -> ALLOWED (WRITE_TREE granted on new root) -----");
		deleteAddressWorkingCopy(newAddrUuid, true);
		deleteObjectWorkingCopy(newObjUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify deletion of permissions on deleted entities -> get group details -----");
		newGroupDoc = getGroupDetails(nameNewGrp);

		System.out.println("\n--------------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE\" SUBNODE PERMISSION -----");
		System.out.println("--------------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub address -----");
		String parentAddrUuid = topAddrUuid;
		newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, parentAddrUuid);
		newAdrDoc = getInitialAddress(newAdrDoc);
		// extend initial address with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub object -----");
		String parentObjUuid = topObjUuid;
		newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, parentObjUuid);
		newObjDoc = getInitialObject(newObjDoc);
		// extend initial address with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission WRITE_TREE on parent -----");
		// object permission
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, parentObjUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_TREE.getDbValue());
		perms.add(newPerm);
		// address permission
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, parentAddrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_TREE.getDbValue());
		perms.add(newPerm);
		newGroupDoc = storeGroup(newGroupDoc, true);

		System.out.println("\n----- verify permissions for object -----");
		getObjectPermissions(parentObjUuid);

		System.out.println("\n----- verify permissions for address -----");
		getAddressPermissions(parentAddrUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub address -> ALLOWED -----");
		newAdrDoc = storeAddress(newAdrDoc, false);
		newAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create sub object -> ALLOWED -----");
		newObjDoc = storeObject(newObjDoc, false);
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- and delete new sub entities -> ALLOWED (WRITE_TREE in parent!) -----");
		deleteAddressWorkingCopy(newAddrUuid, true);
		deleteObjectWorkingCopy(newObjUuid, true);

		// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("----- CLEAN UP -----");
		System.out.println("----------------------------");
		
		System.out.println("\n----- remove users -----");
		deleteUser(newMetaAdminId);
		deleteUser(newMetaAuthorId);

		System.out.println("\n----- remove permissions from group -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		newGroupDoc = storeGroup(newGroupDoc, true);

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

	private IngridDocument getObjectPermissions(String objUuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPermissions ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getObjectPermissions(plugId, objUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getAddressPermissions(String addrUuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPermissions ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getAddressPermissions(plugId, addrUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getUserPermissions() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserPermissions ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserPermissions(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result);
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

	private IngridDocument getInitialObject(IngridDocument newBasicObject) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialObject ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getInitialObject(plugId, newBasicObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getObjectDetails(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, Quantity.DETAIL_ENTITY, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeObject(IngridDocument oDocIn,
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
		response = mdekCallerObject.storeObject(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugObjectDoc(result);
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
		response = mdekCallerObject.publishObject(plugId, oDocIn, withRefetch, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			if (withRefetch) {
//				debugObjectDoc(result);
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
		response = mdekCallerObject.deleteObject(plugId, uuid, forceDeleteReferences, myUserUuid);
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

	private IngridDocument deleteAddress(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddress " + deleteRefsInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddress(plugId, uuid, forceDeleteReferences, myUserUuid);
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

	private IngridDocument getInitialAddress(IngridDocument newBasicAddress) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialAddress ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getInitialAddress(plugId, newBasicAddress, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getAddressDetails(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, Quantity.DETAIL_ENTITY, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
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
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, refetchAddress, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
//			debugAddressDoc(result);
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
		response = mdekCallerAddress.publishAddress(plugId, aDocIn, withRefetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuid = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuid);
			if (withRefetch) {
//				debugAddressDoc(result);
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

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  User Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}

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
	}
	
	private void debugPermissionsDoc(IngridDocument p) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("Permissions: " + docList.size() + " Entries");
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
