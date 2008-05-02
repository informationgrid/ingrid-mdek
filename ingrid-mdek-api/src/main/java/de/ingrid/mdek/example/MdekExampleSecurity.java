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
import de.ingrid.mdek.caller.MdekCaller;
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
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleSecurityThread(int threadNumber)
	{
		this.threadNumber = threadNumber;

		supertool = new MdekExampleSupertool("mdek-iplug-idctest", "EXAMPLE_USER_" + threadNumber);
	}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		// top nodes, not affecting nodes below !
		String topAddrUuid = "3866447A-B449-11D2-9A86-080000507261";
		String topObjUuid = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";

		// top nodes of nodes below !!!
//		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
//		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";

		// parents of children below
//		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
//		String parentObjUuid = "D40743ED-1FC3-11D3-AF50-0060084A4596";

		// children of parents above
		String addrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		String objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";


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
		supertool.getGroups();

		System.out.println("\n----- create new group -----");
		String nameNewGrp = "neue TEST-Gruppe";

		IngridDocument newGroupDoc = new IngridDocument();
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = supertool.createGroup(newGroupDoc, true);
		Long newGroupId = (Long) newGroupDoc.get(MdekKeysSecurity.IDC_GROUP_ID);

		System.out.println("\n----- get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- change name of group and store -----");
		nameNewGrp += " CHANGED!";
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		// ===================================

		System.out.println("\n----------------------------");
		System.out.println("----- TEST USER STUFF -----");
		System.out.println("----------------------------");

		System.out.println("\n----- get catalog admin -----");
		IngridDocument catalogAdminDoc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) catalogAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = catalogAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		
		System.out.println("\n----- create new user METADATA_ADMINISTRATOR -----");
		IngridDocument newMetaAdminDoc = new IngridDocument();
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE6-FE15-11D2-AF34-0060084A4596");
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAdminDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdminDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdminDoc = supertool.createUser(newMetaAdminDoc, true);
		Long newMetaAdminId = (Long) newMetaAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdminUuid = newMetaAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new user METADATA_AUTHOR -----");
		IngridDocument newMetaAuthorDoc = new IngridDocument();
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "386645BC-B449-11D2-9A86-080000507261");
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_AUTHOR.getDbValue());
		newMetaAuthorDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdminId);
		newMetaAuthorDoc = supertool.createUser(newMetaAuthorDoc, true);
		Long newMetaAuthorId = (Long) newMetaAuthorDoc.get(MdekKeysSecurity.IDC_USER_ID);

		System.out.println("\n----- get sub users -----");
		supertool.getSubUsers(catalogAdminId);
		supertool.getSubUsers(newMetaAdminId);

		System.out.println("\n----- change addr uuid of user and store -----");
		newMetaAuthorDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "6C6A3485-59E0-11D3-AE74-00104B57C66D");
		newMetaAuthorDoc = supertool.storeUser(newMetaAuthorDoc, true);		
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
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- delete address WORKING COPY -> ALLOWED -----");
		supertool.deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object WORKING COPY -> ALLOWED  -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (no permissions) -----");
		supertool.setCallingUser(newMetaAuthorUuid);

		System.out.println("\n----- delete address FULL -> NOT ALLOWED -----");
		supertool.deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED -----");
		supertool.deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- store address -> NOT ALLOWED -----");
		System.out.println("-- first fetch address");
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- publish address -> NOT ALLOWED -----");
		supertool.publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- store object -> NOT ALLOWED -----");
		System.out.println("-- first fetch object");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.storeObject(doc, false);

		System.out.println("\n----- publish object -> NOT ALLOWED -----");
		supertool.publishObject(doc, false, false);

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
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- verify permissions for object -----");
		supertool.getObjectPermissions(objUuid);

		System.out.println("\n----- verify permissions for address -----");
		supertool.getAddressPermissions(addrUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- delete address FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		supertool.deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		supertool.deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- delete address WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- delete object WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- write address -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch address");
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- publish address -> ALLOWED (WRITE_SINGLE) -----");
		supertool.publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- write object -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch object");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.storeObject(doc, false);

		System.out.println("\n----- publish object -> ALLOWED (WRITE_SINGLE) -----");
		supertool.publishObject(doc, false, false);


		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE_ROOT\" PERMISSION -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top address -----");
		IngridDocument newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, null);
		newAdrDoc = supertool.getInitialAddress(newAdrDoc);
		// extend initial address with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		supertool.storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- move address to top -> NOT ALLOWED -----");
		supertool.moveAddress(addrUuid, null, false);

		System.out.println("\n----- copy address to top -> NOT ALLOWED -----");
		supertool.copyAddress(addrUuid, null, false, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top object -----");
		IngridDocument newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, null);
		newObjDoc = supertool.getInitialObject(newObjDoc);
		// extend initial address with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		supertool.storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- move object to top -> NOT ALLOWED -----");
		supertool.moveObject(objUuid, null, false);

		System.out.println("\n----- copy object to top -> NOT ALLOWED -----");
		supertool.copyObject(objUuid, null, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission CREATE_ROOT to group -----");
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.CREATE_ROOT.getDbValue());
		perms.add(newPerm);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- verify user permission -----");
		supertool.getUserPermissions();

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top address -> ALLOWED -----");
		newAdrDoc = supertool.storeAddress(newAdrDoc, false);
		String newAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create top object -> ALLOWED -----");
		newObjDoc = supertool.storeObject(newObjDoc, false);
		String newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy object to top -> ALLOWED -----");
		doc = supertool.copyObject(objUuid, null, false);
		String copiedObjUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

		System.out.println("\n----- copy address to top -> ALLOWED BUT WRONG ADDRESS TYPE :) -----");
		doc = supertool.copyAddress(addrUuid, null, false, false);
		String copiedAddrUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify granted WRITE_TREE permissions on new roots -> get group details -----");
		supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- and delete new top entities -> ALLOWED (WRITE_TREE granted on new root) -----");
		supertool.deleteAddressWorkingCopy(newAddrUuid, true);
		supertool.deleteObjectWorkingCopy(newObjUuid, true);
		if (copiedAddrUuid != null) {
			supertool.deleteAddressWorkingCopy(copiedAddrUuid, true);			
		}
		if (copiedObjUuid != null) {
			supertool.deleteObjectWorkingCopy(copiedObjUuid, true);			
		}

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify deletion of permissions on deleted entities -> get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);

		
		System.out.println("\n\n--------------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE\" SUBNODE PERMISSION -----");
		System.out.println("--------------------------------------------------------");

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- create sub address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub address -----");
		String newParentAddrUuid = topAddrUuid;
		newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, newParentAddrUuid);
		newAdrDoc = supertool.getInitialAddress(newAdrDoc);
		// extend initial address with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		supertool.storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy address to new parent -> NOT ALLOWED -----");
		supertool.copyAddress(addrUuid, newParentAddrUuid, false, false);

		System.out.println("\n----- move address to new parent -> NOT ALLOWED -----");
		supertool.moveAddress(addrUuid, newParentAddrUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub object -----");
		String newParentObjUuid = topObjUuid;
		newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, newParentObjUuid);
		newObjDoc = supertool.getInitialObject(newObjDoc);
		// extend initial address with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- then store -> NOT ALLOWED -----");
		supertool.storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy object to new parent -> NOT ALLOWED -----");
		supertool.copyObject(objUuid, newParentObjUuid, false);

		System.out.println("\n----- move object to new parent -> NOT ALLOWED -----");
		supertool.moveObject(objUuid, newParentObjUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission WRITE_TREE on target parent -----");
		// object permission
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, newParentObjUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_TREE.getDbValue());
		perms.add(newPerm);
		// address permission
		perms = (List<IngridDocument>) newGroupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, newParentAddrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, MdekUtilsSecurity.IdcPermission.WRITE_TREE.getDbValue());
		perms.add(newPerm);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- verify permissions for object -----");
		supertool.getObjectPermissions(newParentObjUuid);

		System.out.println("\n----- verify permissions for address -----");
		supertool.getAddressPermissions(newParentAddrUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub address -> ALLOWED -----");
		newAdrDoc = supertool.storeAddress(newAdrDoc, false);
		newAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

		System.out.println("\n----- create sub object -> ALLOWED -----");
		newObjDoc = supertool.storeObject(newObjDoc, false);
		newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy object to new parent -> ALLOWED -----");
		doc = supertool.copyObject(objUuid, newParentObjUuid, false);
		copiedObjUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

		System.out.println("\n----- copy address to new parent -> ALLOWED -----");
		doc = supertool.copyAddress(addrUuid, newParentAddrUuid, false, false);
		copiedAddrUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- move object to new parent -> NOT ALLOWED (no WRITE_TREE on object to move) -----");
		supertool.moveObject(objUuid, newParentObjUuid, false);

		System.out.println("\n----- move address to new parent -> NOT ALLOWED (no WRITE_TREE on address to move) -----");
		supertool.moveAddress(addrUuid, newParentAddrUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- and delete new sub entities -> ALLOWED (WRITE_TREE in parent!) -----");
		supertool.deleteAddressWorkingCopy(newAddrUuid, true);
		supertool.deleteObjectWorkingCopy(newObjUuid, true);
		if (copiedAddrUuid != null) {
			supertool.deleteAddressWorkingCopy(copiedAddrUuid, true);			
		}
		if (copiedObjUuid != null) {
			supertool.deleteObjectWorkingCopy(copiedObjUuid, true);			
		}

		// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("----- CLEAN UP -----");
		System.out.println("----------------------------");
		
		System.out.println("\n----- remove users -----");
		supertool.deleteUser(newMetaAdminId);
		supertool.deleteUser(newMetaAuthorId);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify no wrong permissions in group -> get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n----- remove permissions from group -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- remove group -----");
		supertool.deleteGroup(newGroupId);

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
