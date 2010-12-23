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
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
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

		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
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
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		String parentObjUuid = "D40743ED-1FC3-11D3-AF50-0060084A4596";

		// children of parents above
		String addrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		String objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";

		boolean alwaysTrue = true;
		IngridDocument doc;

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		IngridDocument catAdminDoc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) catAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = catAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- ENABLE/DISABLE WORKFLOW in catalog -----");
		IngridDocument catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		catDoc = supertool.storeCatalog(catDoc, true);


// ====================
// test single stuff
// -----------------------------------
/*
		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ====================

		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- GROUP: Remove Object/Address/User-Permissions -> only allowed if user has according permission himself (no matter from which group) ! -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- create new group1 -----");
		String nameNewGrp1 = "TEST Gruppe1";
		IngridDocument newGroup1Doc = new IngridDocument();
		newGroup1Doc.put(MdekKeys.NAME, nameNewGrp1);
		newGroup1Doc = supertool.createGroup(newGroup1Doc, true);
		Long newGroup1Id = (Long) newGroup1Doc.get(MdekKeysSecurity.ID);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR GROUP 1' -----");
		IngridDocument newMetaAdminGroup1Doc = new IngridDocument();
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "38664584-B449-11D2-9A86-080000507261");
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroup1Id});
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdminGroup1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdminGroup1Doc = supertool.createUser(newMetaAdminGroup1Doc, true);
		Long newMetaAdminGroup1Id = (Long) newMetaAdminGroup1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdminGroup1Uuid = newMetaAdminGroup1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new group2 -----");
		String nameNewGrp2 = "TEST Gruppe2";
		IngridDocument newGroup2Doc = new IngridDocument();
		newGroup2Doc.put(MdekKeys.NAME, nameNewGrp2);
		newGroup2Doc = supertool.createGroup(newGroup2Doc, true);
		Long newGroup2Id = (Long) newGroup2Doc.get(MdekKeysSecurity.ID);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR GROUP 2' -----");
		IngridDocument newMetaAdminGroup2Doc = new IngridDocument();
		newMetaAdminGroup2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "10646604-D21F-11D2-BB32-006097FE70B1");
		newMetaAdminGroup2Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroup2Id});
		newMetaAdminGroup2Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdminGroup2Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdminGroup2Doc = supertool.createUser(newMetaAdminGroup2Doc, true);
		Long newMetaAdminGroup2Id = (Long) newMetaAdminGroup2Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdminGroup2Uuid = newMetaAdminGroup2Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("--- Add Permissions to group -> ALL ALLOWED AS CATADMIN -----");
		System.out.println("----- add user permission CREATE_ROOT to group 1 -----");
		System.out.println("----- add address/object WRITE_SINGLE permissions to group 1 -----");
		supertool.addUserPermissionToGroupDoc(newGroup1Doc, MdekUtilsSecurity.IdcPermission.CREATE_ROOT);
		supertool.addObjPermissionToGroupDoc(newGroup1Doc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroup1Doc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		newGroup1Doc = supertool.storeGroup(newGroup1Doc, true);

		System.out.println("\n----- REMOVE ALL permissions and store group -> ALLOWED as catadmin ! -----");
		newGroup1Doc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroup1Doc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroup1Doc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		newGroup1Doc = supertool.storeGroup(newGroup1Doc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission CREATE_ROOT to group 1 -----");
		System.out.println("----- add address/object WRITE_SINGLE permissions to group 1 -----");
		supertool.addUserPermissionToGroupDoc(newGroup1Doc, MdekUtilsSecurity.IdcPermission.CREATE_ROOT);
		supertool.addObjPermissionToGroupDoc(newGroup1Doc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroup1Doc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		newGroup1Doc = supertool.storeGroup(newGroup1Doc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO MD_ADMINISTRATOR GROUP 2 (NO permissions) -----");
		supertool.setCallingUser(newMetaAdminGroup2Uuid);

		System.out.println("\n----- REMOVE OBJECT permissions and store group -> NOT ALLOWED ! -----");
		List<IngridDocument> permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		newGroup1Doc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		supertool.storeGroup(newGroup1Doc, true);
		newGroup1Doc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, permList);

		System.out.println("\n----- REMOVE ADDRESS permissions and store group -> NOT ALLOWED ! -----");
		permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		newGroup1Doc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		supertool.storeGroup(newGroup1Doc, true);
		newGroup1Doc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, permList);

		System.out.println("\n----- REMOVE USER permissions and store group -> NOT ALLOWED ! -----");
		permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		newGroup1Doc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		supertool.storeGroup(newGroup1Doc, true);
		newGroup1Doc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, permList);

		System.out.println("\n----- ADD OBJECT permission and store group -> NOT ALLOWED ! -----");
		permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		supertool.addObjPermissionToGroupDoc(newGroup1Doc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroup1Doc, true);
		permList.remove(permList.size()-1);

		System.out.println("\n----- ADD ADDRESS permission and store group -> NOT ALLOWED ! -----");
		permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		supertool.addAddrPermissionToGroupDoc(newGroup1Doc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroup1Doc, true);
		permList.remove(permList.size()-1);

		System.out.println("\n----- ADD USER permission QUALITY_ASSURANCE and store group -> NOT ALLOWED IF OWN GROUP NOT QA (may be allowed here if granted ...) -----");
		permList = (List<IngridDocument>) newGroup1Doc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		supertool.addUserPermissionToGroupDoc(newGroup1Doc, MdekUtilsSecurity.IdcPermission.QUALITY_ASSURANCE);
		supertool.storeGroup(newGroup1Doc, true);
		permList.remove(permList.size()-1);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO MD_ADMINISTRATOR GROUP 1 (ALL permissions OF GROUP1) -----");
		supertool.setCallingUser(newMetaAdminGroup1Uuid);

		System.out.println("\n----- ADD OBJECT, ADDRESS, USER permission to GROUP2 -> ALLOWED, cause has these permissions from group1 ! -----");
		supertool.addObjPermissionToGroupDoc(newGroup2Doc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroup2Doc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addUserPermissionToGroupDoc(newGroup2Doc, MdekUtilsSecurity.IdcPermission.CREATE_ROOT);
		newGroup2Doc = supertool.storeGroup(newGroup2Doc, true);

		System.out.println("\n----- REMOVE ALL permissions from GROUP2 -> ALLOWED ! -----");
		newGroup2Doc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroup2Doc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroup2Doc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		newGroup2Doc = supertool.storeGroup(newGroup2Doc, true);

		System.out.println("\n----- REMOVE ALL permissions from OWN GROUP1 -> ALLOWED as user of group ! -----");
		newGroup1Doc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		newGroup1Doc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		newGroup1Doc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);
		newGroup1Doc = supertool.storeGroup(newGroup1Doc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- delete users  -----");
		supertool.deleteUser(newMetaAdminGroup1Id);
		supertool.deleteUser(newMetaAdminGroup2Id);

		System.out.println("\n----- delete groups -----");
		supertool.deleteGroup(newGroup1Id, false);
		supertool.deleteGroup(newGroup2Id, false);
/*
		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/

// ===================================

		System.out.println("\n----------------------------");
		System.out.println("----- TEST GROUP STUFF -----");
		System.out.println("----------------------------");

		// -----------------------------------
		System.out.println("\n----- get all groups -----");
		supertool.getGroups(true);
		supertool.getGroups(false);

		System.out.println("\n----- get group users of group 'administrators'-----");
		supertool.getUsersOfGroup("administrators");
		
		System.out.println("\n----- create new group -----");
		String nameNewGrp = "neue TEST-Gruppe";

		IngridDocument newGroupDoc = new IngridDocument();
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = supertool.createGroup(newGroupDoc, true);
		Long newGroupId = (Long) newGroupDoc.get(MdekKeysSecurity.ID);

		System.out.println("\n----- create 2nd new group !!! NOW MULTIPLE GROUPS PER USER !!! -----");
		String nameNewAdditionalGrp = "new TEST-Gruppe ADDITIONAL";
		IngridDocument newAdditionalGroupDoc = new IngridDocument();
		newAdditionalGroupDoc.put(MdekKeys.NAME, nameNewAdditionalGrp);
		newAdditionalGroupDoc = supertool.createGroup(newAdditionalGroupDoc, true);
		Long newAdditionalGroupId = (Long) newAdditionalGroupDoc.get(MdekKeysSecurity.ID);

		System.out.println("\n----- get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- get group users -----");
		supertool.getUsersOfGroup(nameNewGrp);
		
		System.out.println("\n----- change name of group and store -----");
		nameNewGrp += " CHANGED!";
		newGroupDoc.put(MdekKeys.NAME, nameNewGrp);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- VALIDATE GROUP OBJECT PERMISSION STRUCTURE -----");

		System.out.println("\n----- double permission on OBJECT -> ERROR -----");
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- tree below tree permission on OBJECT -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single below tree permission on OBJECT -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single ABOVE tree permission on OBJECT -> OK -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- subnode below tree permission on OBJECT -> ERROR -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- tree below subnode permission on OBJECT -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- single below subnode permission on OBJECT -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- single above subnode permission on OBJECT -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.storeGroup(newGroupDoc, true);
        
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- VALIDATE GROUP ADDRESS PERMISSION STRUCTURE -----");

		System.out.println("\n----- double permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- tree below tree permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single below tree permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single ABOVE tree permission on ADDRESS -> OK -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- subnode below tree permission on ADDRESS -> ERROR -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- tree below subnode permission on ADDRESS -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- single below subnode permission on ADDRESS -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
        supertool.storeGroup(newGroupDoc, true);

        System.out.println("\n----- subnode below single permission on ADDRESS -> OK -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.storeGroup(newGroupDoc, true);
        
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- clear all permissions ! -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

// ===================================

		System.out.println("\n----------------------------");
		System.out.println("----- TEST USER STUFF (ROLES, HIERARCHY) -----");
		System.out.println("----------------------------");

		System.out.println("\n----------------------------");
		System.out.println("----- CREATE USERS -----");
		System.out.println("----------------------------");

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- CREATE USER AS CATALOG_ADMIN -> ALL ALLOWED, EXCEPT CREATE NEW CATALOG ADMIN ! -----");

		System.out.println("\n----- create new 2ND CAT_ADMIN -> ERROR -----");
		String addrUuidNotUsedForUser = "ADEED0B6-545F-11D3-AE6F-00104B57C66D";
		IngridDocument secondCatAdminDoc = new IngridDocument();
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId});
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue());
		secondCatAdminDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, null);
		supertool.createUser(secondCatAdminDoc, true);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR 1' with MULTIPLE GROUPS ! -> ALLOWED -----");
		IngridDocument newMetaAdmin1Doc = new IngridDocument();
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE6-FE15-11D2-AF34-0060084A4596");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId, newAdditionalGroupId});
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdmin1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdmin1Doc = supertool.createUser(newMetaAdmin1Doc, true);
		Long newMetaAdmin1Id = (Long) newMetaAdmin1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdmin1Uuid = newMetaAdmin1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new user 'MD_AUTHOR 1' with MULTIPLE GROUPS ! -> ALLOWED -----");
		IngridDocument newMetaAuthor1Doc = new IngridDocument();
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "386645BC-B449-11D2-9A86-080000507261");
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId, newAdditionalGroupId});
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_AUTHOR.getDbValue());
		newMetaAuthor1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdmin1Id);
		newMetaAuthor1Doc = supertool.createUser(newMetaAuthor1Doc, true);
		Long newMetaAuthor1Id = (Long) newMetaAuthor1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAuthor1Uuid = newMetaAuthor1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create 2ND new user 'MD_ADMINISTRATOR 2' -> ALLOWED -----");
		IngridDocument newMetaAdmin2Doc = new IngridDocument();
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE4-FE15-11D2-AF34-0060084A4596");
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId});
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdmin2Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdmin2Doc = supertool.createUser(newMetaAdmin2Doc, true);
		Long newMetaAdmin2Id = (Long) newMetaAdmin2Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdmin2Uuid = newMetaAdmin2Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new user WITH SAME ADDRESS -> ERROR: ENTITY_ALREADY_EXISTS -----");
		doc = new IngridDocument();
		doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE4-FE15-11D2-AF34-0060084A4596");
		doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId});
		doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		supertool.createUser(newMetaAdmin2Doc, true);


		System.out.println("\n----- get sub users -----");
		supertool.getSubUsers(catalogAdminId);
		supertool.getSubUsers(newMetaAdmin1Id);

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- CREATE USER AS 'MD_ADMIN 1' -> ONLY 'MD_AUTHOR' underneath 'MD_ADMIN 1' ALLOWED ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO 'MD_ADMIN 1' 111111 -----");
		supertool.setCallingUser(newMetaAdmin1Uuid);

		System.out.println("\n----- create new 2ND CAT_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		supertool.createUser(secondCatAdminDoc, true);

		System.out.println("\n----- create new user MD_ADMINISTRATOR -> ERROR: USER_HAS_WRONG_ROLE -----");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		supertool.createUser(newMetaAdmin1Doc, true);
		
		System.out.println("\n----- create new user 'MD_AUTHOR 2' UNDER 'MD_ADMINISTRATOR 2' = not subuser !!! -> ERROR: USER_HIERARCHY_WRONG (calling user not parent) -----");
		IngridDocument newMetaAuthor2Doc = new IngridDocument();
		newMetaAuthor2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "0C2EA4F9-18DE-11D3-AF47-0060084A4596");
		newMetaAuthor2Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newGroupId});
		newMetaAuthor2Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_AUTHOR.getDbValue());
		newMetaAuthor2Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdmin2Id);
		supertool.createUser(newMetaAuthor2Doc, true);

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO 'MD_ADMIN 2' 2222222 -----");
		supertool.setCallingUser(newMetaAdmin2Uuid);

		System.out.println("\n----- create new user 'MD_AUTHOR 2' UNDER 'MD_ADMINISTRATOR 2' = subuser !!! -> ALLOWED (calling user is parent 'MD_ADMINISTRATOR 2') -----");
		newMetaAuthor2Doc = supertool.createUser(newMetaAuthor2Doc, true);
		Long newMetaAuthor2Id = (Long) newMetaAuthor2Doc.get(MdekKeysSecurity.IDC_USER_ID);

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- CREATE USER AS MD_AUTHOR -> ERROR ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO MD_AUTHOR -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- create new 2ND CAT_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		supertool.createUser(secondCatAdminDoc, true);

		System.out.println("\n----- create new user MD_ADMINISTRATOR -> ERROR: USER_HAS_WRONG_ROLE -----");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		supertool.createUser(newMetaAdmin1Doc, true);
		
		System.out.println("\n----- create new user MD_AUTHOR -> ERROR: USER_HAS_WRONG_ROLE -----");
		newMetaAuthor2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		supertool.createUser(newMetaAuthor2Doc, true);

// ===================================

		System.out.println("\n----------------------------");
		System.out.println("----- UPDATE USERS -----");
		System.out.println("----------------------------");

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- UPDATE USER AS CATALOG_ADMIN -> ALL ALLOWED -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO CATALOG_ADMIN -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- store CAT_ADMIN -> ALLOWED -----");
		catAdminDoc = supertool.storeUser(catAdminDoc, true);		

		System.out.println("\n----- store MD_ADMIN -> ALLOWED -----");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, newMetaAdmin1Uuid);
		newMetaAdmin1Doc = supertool.storeUser(newMetaAdmin1Doc, true);

		System.out.println("\n----- store MD_ADMIN WITH CHANGED ROLE -> ERROR: USER_HAS_WRONG_ROLE (Change of Role NOT ALLOWED) -----");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_ROLE, IdcRole.METADATA_AUTHOR.getDbValue());
		supertool.storeUser(newMetaAdmin1Doc, false);
		// reset doc
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_ROLE, IdcRole.METADATA_ADMINISTRATOR.getDbValue());

		System.out.println("\n----- store MD_AUTHOR WITH CHANGED ADDRESS OF OTHER USER -> ERROR: ENTITY_ALREADY_EXISTS -----");
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE6-FE15-11D2-AF34-0060084A4596");
		supertool.storeUser(newMetaAuthor1Doc, true);		

		System.out.println("\n----- store MD_AUTHOR (change addr_uuid to no user address) -> ALLOWED -----");
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "6C6A3485-59E0-11D3-AE74-00104B57C66D");
		newMetaAuthor1Doc = supertool.storeUser(newMetaAuthor1Doc, true);		
		newMetaAuthor1Uuid = newMetaAuthor1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- store MD_AUTHOR WITH NEW PARENT -> ERROR: USER_HIERARCHY_WRONG (change of parent NOT ALLOWED) -----");
		newMetaAuthor1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdmin2Id);
		supertool.storeUser(newMetaAuthor1Doc, false);
		// reset doc
		newMetaAuthor1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdmin1Id);

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- UPDATE USER AS 'MD_ADMIN 1' -> ONLY 'MD_AUTHOR' underneath 'MD_ADMIN 1' ALLOWED ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO 'MD_ADMIN 1' 111111 -----");
		supertool.setCallingUser(newMetaAdmin1Uuid);

		System.out.println("\n----- store CAT_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.storeUser(catAdminDoc, true);		

		System.out.println("\n----- store MD_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.storeUser(newMetaAdmin1Doc, true);		

		System.out.println("\n----- store 'MD_AUTHOR 2' = not subuser -> ERROR: USER_HIERARCHY_WRONG (calling user not parent) -----");
		supertool.storeUser(newMetaAuthor2Doc, true);
		
		System.out.println("\n----- store 'MD_AUTHOR 1' = subuser !!! -> ALLOWED (calling user is parent 'MD_ADMINISTRATOR 1') -----");
		newMetaAuthor1Doc = supertool.storeUser(newMetaAuthor1Doc, true);		

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- UPDATE USER AS MD_AUTHOR -> ERROR -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO MD_AUTHOR -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- store CAT_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		catAdminDoc = supertool.storeUser(catAdminDoc, true);		

		System.out.println("\n----- store MD_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.storeUser(newMetaAdmin1Doc, true);		

		System.out.println("\n----- store MD_AUTHOR -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.storeUser(newMetaAuthor1Doc, true);		

// ===================================

		System.out.println("\n----------------------------");
		System.out.println("----- DELETE USERS -----");
		System.out.println("----------------------------");

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- DELETE USER AS MD_AUTHOR -> ERROR -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO MD_AUTHOR -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- delete MD_AUTHOR -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.deleteUser(newMetaAuthor2Id);		

		System.out.println("\n----- delete MD_ADMIN -> ERROR: USER_HAS_SUBUSERS -----");
		supertool.deleteUser(newMetaAdmin2Id);		

		System.out.println("\n----- delete CAT_ADMIN -> ERROR: USER_IS_CATALOG_ADMIN -----");
		supertool.deleteUser(catalogAdminId);		

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- DELETE USER AS 'MD_ADMIN 2' -> ONLY 'MD_AUTHOR' underneath 'MD_ADMIN 2' ALLOWED ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO 'MD_ADMIN 2' 22222 -----");
		supertool.setCallingUser(newMetaAdmin2Uuid);

		System.out.println("\n----- delete 'MD_AUTHOR 1' = not subuser -> ERROR: USER_HIERARCHY_WRONG (calling user not parent) -----");
		supertool.deleteUser(newMetaAuthor1Id);		
		
		System.out.println("\n----- delete 'MD_AUTHOR 2' = subuser !!! -> ALLOWED (calling user is parent 'MD_ADMINISTRATOR 2') -----");
		supertool.deleteUser(newMetaAuthor2Id);		

		System.out.println("\n----- delete MD_ADMIN -> ERROR: USER_HAS_WRONG_ROLE -----");
		supertool.deleteUser(newMetaAdmin2Id);		

		System.out.println("\n----- delete CAT_ADMIN -> ERROR: USER_IS_CATALOG_ADMIN -----");
		supertool.deleteUser(catalogAdminId);		

		System.out.println("\n---------------------------------------------------");
		System.out.println("----- DELETE USER AS CAT_ADMIN -> ALL ALLOWED (except CAT_ADMIN !) -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO CAT_ADMIN -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- delete CAT_ADMIN -> ERROR: USER_IS_CATALOG_ADMIN -----");
		supertool.deleteUser(catalogAdminId);

// ===================================

		System.out.println("\n\n----------------------------");
		System.out.println("----- TEST RESPONSIBLE USER UPDATE IN ENTITY ON CHANGE OF ADDRESS IN USER");
		System.out.println("----- !!! is also updated IN PUBLISHED Entities !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("----------------------------");
		
		System.out.println("\n----------------------------");
		System.out.println("----- set 'MD_ADMIN 2' as responsible user in OBJECT ! -----");

		System.out.println("\n----- fetch object -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);

        System.out.println("\n----- verify permissions for object -----");
        supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n----- set Responsible User and store object -> working copy with 'MD_ADMIN 2' as ResponsibleUser -----");
		System.out.println("----- !!! ResponsibleUser = " + newMetaAdmin2Uuid);
		setResponsibleUuidInDoc(newMetaAdmin2Uuid, doc);
		supertool.storeObject(doc, false);

		System.out.println("\n----------------------------");
		System.out.println("----- set 'MD_ADMIN 2' as responsible user in ADDRESS ! -----");

		System.out.println("\n----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);

		System.out.println("\n----- set Responsible User and store address -> working copy with 'MD_ADMIN 2' as ResponsibleUser -----");
		System.out.println("----- !!! ResponsibleUser = " + newMetaAdmin2Uuid);
		setResponsibleUuidInDoc(newMetaAdmin2Uuid, doc);
		supertool.storeAddress(doc, false);

		System.out.println("\n----------------------------");
		System.out.println("----- change Address of 'MD_ADMIN 2' (= ResponsibleUser) -> Address of ResponsibleUser in Object/Address adapted -----");
		System.out.println("\n\n----- !!! NEW ResponsibleUser = " + addrUuidNotUsedForUser);
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuidNotUsedForUser);
		newMetaAdmin2Doc = supertool.storeUser(newMetaAdmin2Doc, true);

		System.out.println("\n----------------------------");
		System.out.println("----- verify CHANGED ResponsibleUser in Object -----");
		System.out.println("----- fetch object -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		String respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Object ResponsibleUser = " + respUuid);
		if (!respUuid.equals(addrUuidNotUsedForUser)) {
			throw new RuntimeException("ERROR: Object ResponsibleUser NOT ADAPTED on User Address change");
		}

		System.out.println("\n----- verify CHANGED ResponsibleUser in ADDRESS -----");
		System.out.println("----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Address ResponsibleUser = " + respUuid);
		if (!respUuid.equals(addrUuidNotUsedForUser)) {
			throw new RuntimeException("ERROR: Address ResponsibleUser NOT ADAPTED on User Address change");
		}

// ===================================
		
		System.out.println("\n\n----------------------------");
		System.out.println("----- TEST RESPONSIBLE USER UPDATE IN ENTITY ON DELETE OF USER (set to Parent of User)");
		System.out.println("----- !!! is also updated IN PUBLISHED Entities !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("----------------------------");
		
		System.out.println("\n----------------------------");
		System.out.println("----- delete 'MD_ADMIN 2' (= ResponsibleUser) ->  Address of ResponsibleUser in Object/Address adapted to PARENT USER !!! -----");
		System.out.println("\n\n----- !!! NEW ResponsibleUser = " + catalogAdminUuid + " (= CAT_ADMIN)");

		System.out.println("\n----- delete 'MD_ADMIN 2' -> ALLOWED (calling user is CAT_ADMIN) -----");
		supertool.deleteUser(newMetaAdmin2Id);		

		System.out.println("\n----------------------------");
		System.out.println("----- verify CHANGED ResponsibleUser in Object -----");
		System.out.println("----- fetch object -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Object ResponsibleUser = " + respUuid);
		if (!respUuid.equals(catalogAdminUuid)) {
			throw new RuntimeException("ERROR: Object ResponsibleUser NOT ADAPTED on User DELETE");
		}

		System.out.println("\n----- verify CHANGED ResponsibleUser in ADDRESS -----");
		System.out.println("----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Address ResponsibleUser = " + respUuid);
		if (!respUuid.equals(catalogAdminUuid)) {
			throw new RuntimeException("ERROR: Address ResponsibleUser NOT ADAPTED on User DELETE");
		}

		System.out.println("\n----------------------------");
		System.out.println("----- CLEAN UP: delete working copies of entities");

		supertool.deleteObjectWorkingCopy(objUuid, true);
		supertool.deleteAddressWorkingCopy(addrUuid, true);


// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("----- TEST PERMISSIONS -----");
		System.out.println("----------------------------");

		System.out.println("\n------------------------------------------------");
		System.out.println("----- CATALOG: \"WRITE\" PERMISSION (only cat admin) -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- store catalog -> ALLOWED -----");
		System.out.println("-- first fetch catalog");
		doc = supertool.getCatalog();
		doc = supertool.storeCatalog(doc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (no permissions) -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- store catalog -> NOT ALLOWED -----");
		supertool.storeCatalog(doc, false);


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
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- delete address FULL -> NOT ALLOWED -----");
		supertool.deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED -----");
		supertool.deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- store address -> NOT ALLOWED -----");
		System.out.println("-- first fetch address");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- publish address -> NOT ALLOWED -----");
		supertool.publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- store object -> NOT ALLOWED -----");
		System.out.println("-- first fetch object");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeObject(doc, false);

		System.out.println("\n----- publish object -> NOT ALLOWED -----");
		supertool.publishObject(doc, false, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add address/object WRITE_SINGLE permissions to INITIAL group -----");
		supertool.addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("----- add SAME address/object WRITE_SINGLE permissions to ADDITIONAL group (duplicate permissions ok when multiple groups) -----");
		supertool.addObjPermissionToGroupDoc(newAdditionalGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(newAdditionalGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		newAdditionalGroupDoc = supertool.storeGroup(newAdditionalGroupDoc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (now with WRITE_SINGLE permissions) -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- verify permissions for object -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n----- verify permissions for address -----");
		supertool.getAddressPermissions(addrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- delete address FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		supertool.deleteAddress(addrUuid, true);

		System.out.println("\n----- delete object FULL -> NOT ALLOWED (no WRITE_TREE) -----");
		supertool.deleteObject(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- write address -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch address");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- delete address WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n----- publish address -> ALLOWED (WRITE_SINGLE) -----");
		supertool.publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- write object -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch object");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeObject(doc, false);

		System.out.println("\n----- delete object WORKING COPY -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- publish object -> ALLOWED (WRITE_SINGLE) -----");
		supertool.publishObject(doc, false, false);

// ===================================

		System.out.println("\n------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: Get All Users with Write Permission");

		System.out.println("\n----- users with write permission for object: " + objUuid);
		supertool.getUsersWithWritePermissionForObject(objUuid, true, false);
		supertool.getUsersWithWritePermissionForObject(objUuid, true, true);

		System.out.println("\n----- users with write permission for address: " + addrUuid);
		supertool.getUsersWithWritePermissionForAddress(addrUuid, true, false);
		supertool.getUsersWithWritePermissionForAddress(addrUuid, true, true);

// ===================================
		
		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE_ROOT\" PERMISSION -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top address -> NOT ALLOWED -----");
		IngridDocument newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, null);
		supertool.getInitialAddress(newAdrDoc);
		// extend doc with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
		System.out.println("\n----- try to store own document -> NOT ALLOWED -----");
		supertool.storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- move address to top -> NOT ALLOWED -----");
		supertool.moveAddress(addrUuid, null, false);

		System.out.println("\n----- copy address to top -> NOT ALLOWED -----");
		supertool.copyAddress(addrUuid, null, false, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create top object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for top object -> NOT ALLOWED -----");
		IngridDocument newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, null);
		supertool.getInitialObject(newObjDoc);
		// extend doc with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- try to store own document -> NOT ALLOWED -----");
		supertool.storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- move object to top -> NOT ALLOWED -----");
		supertool.moveObject(objUuid, null, false);

		System.out.println("\n----- copy object to top -> NOT ALLOWED -----");
		supertool.copyObject(objUuid, null, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission CREATE_ROOT to ADDITIONAL group -----");
		supertool.addUserPermissionToGroupDoc(newAdditionalGroupDoc, MdekUtilsSecurity.IdcPermission.CREATE_ROOT);
		newAdditionalGroupDoc = supertool.storeGroup(newAdditionalGroupDoc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (now with CREATE_ROOT permissions) -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

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
		System.out.println("----- verify granted WRITE_TREE permissions on new roots -> ONLY IN ADDITIONAL GROUP with create-root permission -----");
		supertool.getGroupDetails(nameNewAdditionalGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- INITIAL GROUP has NO WRITE_TREE permissions -> ONLY IN GROUP with create-root permission -----");
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
		newAdditionalGroupDoc = supertool.getGroupDetails(nameNewAdditionalGrp);

// ===================================
		
		System.out.println("\n\n--------------------------------------------------------");
		System.out.println("----- ADDRESS/OBJECT: \"CREATE\" SUBNODE PERMISSION -----");
		System.out.println("--------------------------------------------------------");

		System.out.println("\n\n-------------------------------------");
		System.out.println("----- create sub address -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub address -> NOT ALLOWED -----");
		String newParentAddrUuid = topAddrUuid;
		newAdrDoc = new IngridDocument();
		newAdrDoc.put(MdekKeys.PARENT_UUID, newParentAddrUuid);
		supertool.getInitialAddress(newAdrDoc);
		// extend doc with own data !
		newAdrDoc.put(MdekKeys.NAME, "testNAME");
		newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION");
		newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
		newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		System.out.println("\n----- try to store own document -> NOT ALLOWED -----");
		supertool.storeAddress(newAdrDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy address to new parent -> NOT ALLOWED -----");
		supertool.copyAddress(addrUuid, newParentAddrUuid, false, false);

		System.out.println("\n----- move address to new parent -> NOT ALLOWED -----");
		supertool.moveAddress(addrUuid, newParentAddrUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- create sub object -> NOT ALLOWED -----");
		System.out.println("----- first get initial data for sub object -> NOT ALLOWED -----");
		String newParentObjUuid = topObjUuid;
		newObjDoc = new IngridDocument();
		newObjDoc.put(MdekKeys.PARENT_UUID, newParentObjUuid);
		supertool.getInitialObject(newObjDoc);
		// extend doc with own data !
		newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		System.out.println("\n----- try to store own document -> NOT ALLOWED -----");
		supertool.storeObject(newObjDoc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- copy object to new parent -> NOT ALLOWED -----");
		supertool.copyObject(objUuid, newParentObjUuid, false);

		System.out.println("\n----- move object to new parent -> NOT ALLOWED -----");
		supertool.moveObject(objUuid, newParentObjUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add user permission WRITE_TREE on target parent -----");
		supertool.addObjPermissionToGroupDoc(newGroupDoc, newParentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addAddrPermissionToGroupDoc(newGroupDoc, newParentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (now with WRITE_TREE permissions) -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

		System.out.println("\n----- verify permissions for object -----");
		supertool.getObjectPermissions(newParentObjUuid, true);

		System.out.println("\n----- verify permissions for address -----");
		supertool.getAddressPermissions(newParentAddrUuid, true);

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

		System.out.println("\n------------------------------------------------");
		System.out.println("----- CHECK Delete of Address which is Address of IdcUser ! -> Not Allowed");
		System.out.println("------------------------------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- create new user MD_AUTHOR with Address to delete -----");
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, newParentAddrUuid);
		doc = supertool.createUser(newMetaAuthor1Doc, true);
		Long tmpUserId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);

		System.out.println("\n----- delete Address of User -> ERROR: ADDRESS_IS_IDCUSER_ADDRESS -----");
		supertool.deleteAddress(newParentAddrUuid, true);

		System.out.println("\n----- delete user  -----");
		supertool.deleteUser(tmpUserId);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR -----");
		supertool.setCallingUser(newMetaAuthor1Uuid);

// ===================================
		
		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- GROUP: User CURRENTLY WORKING ON OBJECT -> Remove Permission fails / Delete Group fails ! -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n----- write object -> USER HAS WORKING COPY !");
		System.out.println("-- first fetch object");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.storeObject(doc, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- REMOVE permission in ADDITIONAL GROUP and store -> NO ERROR: Still permissions from INITIAL group -----");
		newAdditionalGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		supertool.storeGroup(newAdditionalGroupDoc, true);

		System.out.println("\n----- REMOVE permission in INITIAL GROUP and store group -> ERROR: USER_EDITING_OBJECT_PERMISSION_MISSING -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- DELETE group -> ERROR: USER_EDITING_OBJECT_PERMISSION_MISSING -----");
		supertool.deleteGroup(newGroupId, true);

		System.out.println("\n----- validate group: still write permissions ! -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- delete object WORKING COPY -> ALLOWED (still write permission in Group !) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- GROUP: User RESPONSIBLE FOR OBJECT -> Remove Permission fails / Delete Group fails ! -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n----- publish user as responsible in object -> user NOT editing and is responsible");
		System.out.println("-- first fetch object");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		String origResponsibleUuid = getResponsibleUuidFromDoc(doc);
		setResponsibleUuidInDoc(supertool.getCallingUserUuid(), doc);
		supertool.publishObject(doc, false, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- REMOVE permission and store group -> ERROR: USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- DELETE group -> ERROR: USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING -----");
		supertool.deleteGroup(newGroupId, true);

		System.out.println("\n----- validate group: still write permissions ! -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- clean up: set responsible user back to former one -----");
		setResponsibleUuidInDoc(origResponsibleUuid, doc);
		supertool.publishObject(doc, false, false);

		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- GROUP: User CURRENTLY WORKING ON ADDRESS -> Remove Permission fails / Delete Group fails ! -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n----- write address -> USER HAS WORKING COPY !");
		System.out.println("-- first fetch address");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- REMOVE permission in ADDITIONAL GROUP and store -> NO ERROR: Still permissions from INITIAL group -----");
		newAdditionalGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		supertool.storeGroup(newAdditionalGroupDoc, true);

		System.out.println("\n----- REMOVE permission and store group -> ERROR: USER_EDITING_ADDRESS_PERMISSION_MISSING -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- DELETE group -> ERROR: USER_EDITING_ADDRESS_PERMISSION_MISSING -----");
		supertool.deleteGroup(newGroupId, true);

		System.out.println("\n----- validate group: still write permissions ! -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- delete address WORKING COPY -> ALLOWED (still write permission in Group !) -----");
		System.out.println("----- WOULD THROW \"NO_PERM\" EXCEPTION if full delete ! -----");
		supertool.deleteAddressWorkingCopy(addrUuid, true);

		System.out.println("\n\n------------------------------------------------");
		System.out.println("----- GROUP: User RESPONSIBLE FOR ADDRESS -> Remove Permission fails / Delete Group fails ! -----");
		System.out.println("------------------------------------------------");

		System.out.println("\n----- publish user as responsible in address -> user NOT editing and is responsible");
		System.out.println("-- first fetch address");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY);
		supertool.setFullOutput(true);
		origResponsibleUuid = getResponsibleUuidFromDoc(doc);
		setResponsibleUuidInDoc(supertool.getCallingUserUuid(), doc);
		supertool.publishAddress(doc, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- REMOVE permission and store group -> ERROR: USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING -----");
		newGroupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- DELETE group -> ERROR: USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING -----");
		supertool.deleteGroup(newGroupId, true);

		System.out.println("\n----- validate group: still write permissions ! -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		
		System.out.println("\n----- clean up: set responsible user back to former one -----");
		setResponsibleUuidInDoc(origResponsibleUuid, doc);
		supertool.publishAddress(doc, false);

// ===================================

        System.out.println("\n\n--------------------------------------------------------");
        System.out.println("----- ADDRESS/OBJECT: \"CREATE\" SUBNODE PERMISSION -----");
        System.out.println("--------------------------------------------------------");
		
        System.out.println("\n-------------------------------------");
        System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
        supertool.setCallingUser(catalogAdminUuid);
		
        System.out.println("\n-------------------------------------");
        System.out.println("----- create top address -> ALLOWED (CATADMIN) -----");
        System.out.println("----- first get initial data for top address -> ALLOWED -----");
        newAdrDoc = new IngridDocument();
        newAdrDoc.put(MdekKeys.PARENT_UUID, null);
        supertool.getInitialAddress(newAdrDoc);
        // extend doc with own data !
        newAdrDoc.put(MdekKeys.NAME, "testNAME ADDRESS PARENT write-subnode");
        newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME ADDRESS PARENT write-subnode");
        newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION ADDRESS PARENT write-subnode");
        newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
        newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.INSTITUTION.getDbValue());
        System.out.println("\n----- try to store own document -> ALLOWED (CATADMIN) -----");
        newAdrDoc = supertool.storeAddress(newAdrDoc, false);
        newParentAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

        System.out.println("\n-------------------------------------");
        System.out.println("----- create top object -> ALLOWED (CATADMIN) -----");
        System.out.println("----- first get initial data for top object -> ALLOWED -----");
        newObjDoc = new IngridDocument();
        newObjDoc.put(MdekKeys.PARENT_UUID, null);
        supertool.getInitialObject(newObjDoc);
        // extend doc with own data !
        newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT PARENT write-subnode");
        System.out.println("\n----- try to store own document -> ALLOWED (CATADMIN) -----");
        newObjDoc = supertool.storeObject(newObjDoc, false);
        newParentObjUuid = (String) newObjDoc.get(MdekKeys.UUID);
        
        
        System.out.println("\n-------------------------------------");
        System.out.println("----- add permission WRITE_SUBNODE on new top nodes in group " + nameNewGrp + " -----");
        clearPermissionsOfGroupDoc(newGroupDoc);
        supertool.addObjPermissionToGroupDoc(newGroupDoc, newParentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addAddrPermissionToGroupDoc(newGroupDoc, newParentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        newGroupDoc = supertool.storeGroup(newGroupDoc, true);
        clearPermissionsOfGroupDoc(newAdditionalGroupDoc);
        newAdditionalGroupDoc = supertool.storeGroup(newAdditionalGroupDoc, true);

		System.out.println("\n----- create new group -----");
		String nameSubnodeGrp = "neue TEST-Gruppe Subnode recht";
		IngridDocument subnodeGrpDoc = new IngridDocument();
		subnodeGrpDoc.put(MdekKeys.NAME, nameSubnodeGrp);
		subnodeGrpDoc = supertool.createGroup(subnodeGrpDoc, true);
		Long subnodeGrpId = (Long) subnodeGrpDoc.get(MdekKeysSecurity.ID);

        System.out.println("\n-------------------------------------");
        System.out.println("----- add permission WRITE_SUBNODE on new top nodes in group " + nameSubnodeGrp + " -----");
        supertool.addObjPermissionToGroupDoc(subnodeGrpDoc, newParentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        supertool.addAddrPermissionToGroupDoc(subnodeGrpDoc, newParentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SUBNODE);
        subnodeGrpDoc = supertool.storeGroup(subnodeGrpDoc, true);

		System.out.println("\n----- remove user 'MD_ADMINISTRATOR 1' from group with write-subnode, add to 2. subnode group -----");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{newAdditionalGroupId, subnodeGrpId});
		newMetaAdmin1Doc = supertool.storeUser(newMetaAdmin1Doc, true);



        System.out.println("\n-------------------------------------");
        System.out.println("----- !!! SWITCH \"CALLING USER\" TO NEW META AUTHOR (now with WRITE_SUBNODE permissions on new parent entities) -----");
        supertool.setCallingUser(newMetaAuthor1Uuid);
        supertool.getUserDetails();
		supertool.getUsersOfGroup(nameNewGrp);
        supertool.getUsersOfGroup(nameNewAdditionalGrp);
        supertool.getUsersOfGroup(nameSubnodeGrp);

        System.out.println("\n----- verify permissions for object -----");
        supertool.getObjectPermissions(newParentObjUuid, true);
        supertool.getUsersWithWritePermissionForObject(newParentObjUuid, true, true);
        supertool.getResponsibleUsersForNewObject(newParentObjUuid, true, true);
        supertool.getUsersWithPermissionForObject(newParentObjUuid, true, true);

        System.out.println("\n----- verify permissions for address -----");
        supertool.getAddressPermissions(newParentAddrUuid, true);
        supertool.getUsersWithWritePermissionForAddress(newParentAddrUuid, true, true);
        supertool.getResponsibleUsersForNewAddress(newParentAddrUuid, true, true);
        supertool.getUsersWithPermissionForAddress(newParentAddrUuid, true, true);

        
        System.out.println("\n-------------------------------------");
        System.out.println("----- store parent object -> NOT ALLOWED (WRITE_SUBNODE in parent!) -----");
        doc = supertool.fetchObject(newParentObjUuid, FetchQuantity.EDITOR_ENTITY);
        supertool.setFullOutput(true);
        supertool.storeObject(doc, false);

        System.out.println("\n-------------------------------------");
        System.out.println("----- store parent address -> NOT ALLOWED (WRITE_SUBNODE in parent!) -----");
        doc = supertool.fetchAddress(newParentAddrUuid, FetchQuantity.EDITOR_ENTITY);
        supertool.setFullOutput(true);
        supertool.storeAddress(doc, false);
        
        System.out.println("\n-------------------------------------");
        System.out.println("----- create sub address -> ALLOWED ! + write-tree added on group with SUBNODE permission !!! -----");
        newAdrDoc = new IngridDocument();
        newAdrDoc.put(MdekKeys.PARENT_UUID, newParentAddrUuid);
        supertool.getInitialAddress(newAdrDoc);
        // extend doc with own data !
        newAdrDoc.put(MdekKeys.NAME, "testNAME ADDRESS SUBNODE");
        newAdrDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME ADDRESS SUBNODE");
        newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION, "testTITLE_OR_FUNCTION ADDRESS SUBNODE");
        newAdrDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, new Integer(-1));
        newAdrDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
        newAdrDoc = supertool.storeAddress(newAdrDoc, false);
        newAddrUuid = (String) newAdrDoc.get(MdekKeys.UUID);

        System.out.println("\n----- verify permissions of new address -----");
        supertool.getAddressPermissions(newAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify granted WRITE_TREE permissions on subnode -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP has NO WRITE_TREE permissions -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewAdditionalGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP with write-subnode has NO WRITE_TREE permissions -> ONLY IN write-subnode GROUP of User -----");
		supertool.getGroupDetails(nameSubnodeGrp);


        System.out.println("\n----- create sub object -> ALLOWED ! + write-tree added on group with SUBNODE permission !!! -----");
        newObjDoc = new IngridDocument();
        newObjDoc.put(MdekKeys.PARENT_UUID, newParentObjUuid);
        supertool.getInitialObject(newObjDoc);
        // extend doc with own data !
        newObjDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT SUBNODE");
        newObjDoc = supertool.storeObject(newObjDoc, false);
        newObjUuid = (String) newObjDoc.get(MdekKeys.UUID);

        System.out.println("\n----- verify permissions of new object -----");
        supertool.getObjectPermissions(newObjUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify granted WRITE_TREE permissions on subnode -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP has NO WRITE_TREE permissions -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewAdditionalGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP with write-subnode has NO WRITE_TREE permissions -> ONLY IN write-subnode GROUP of User -----");
		supertool.getGroupDetails(nameSubnodeGrp);


        System.out.println("\n-------------------------------------");
        System.out.println("----- move new parent object -> NOT ALLOWED -----");
        supertool.moveObject(newParentObjUuid, objUuid, false);

        System.out.println("\n----- move new parent address -> NOT ALLOWED -----");
        doc = supertool.moveAddress(newParentAddrUuid, addrUuid, false);

        
        System.out.println("\n-------------------------------------");
        System.out.println("----- copy object to new parent -> ALLOWED -----");
        doc = supertool.copyObject(objUuid, newParentObjUuid, false);
        copiedObjUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

        System.out.println("\n----- copy address to new parent -> ALLOWED -----");
        doc = supertool.copyAddress(addrUuid, newParentAddrUuid, false, false);
        copiedAddrUuid = (doc == null) ? null : doc.getString(MdekKeys.UUID);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify granted WRITE_TREE permissions on subnode -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP has NO WRITE_TREE permissions -> ONLY IN GROUP of User with write-subnode permission -----");
		supertool.getGroupDetails(nameNewAdditionalGrp);

		System.out.println("\n-------------------------------------");
		System.out.println("----- OTHER GROUP with write-subnode has NO WRITE_TREE permissions -> ONLY IN write-subnode GROUP of User -----");
		supertool.getGroupDetails(nameSubnodeGrp);

		
        System.out.println("\n-------------------------------------");
        System.out.println("----- move copy to child node object -> ALLOWED DUE TO CHILD NODE HAS write-tree added in SUBNODE GROUP !-----");
        supertool.moveObject(copiedObjUuid, newObjUuid, false);

//        System.out.println("\n----- move copy to child node address -> ALLOWED DUE TO CHILD NODE HAS write-tree added in SUBNODE GROUP !-----");
//        doc = supertool.moveAddress(copiedAddrUuid, newAddrUuid, false);

        System.out.println("\n-------------------------------------");
        System.out.println("----- and delete children of sub object -> ALLOWED (write-tree in parent!) -----");
        supertool.deleteAddressWorkingCopy(copiedAddrUuid, true);           

        System.out.println("\n-------------------------------------");
        System.out.println("----- and delete direct sub address of write-subnode parent -> ALLOWED (write-subnode in parent!) -----");
        supertool.deleteObjectWorkingCopy(copiedObjUuid, true);


        
        System.out.println("\n-------------------------------------");
        System.out.println("----- !!! SWITCH \"CALLING USER\" TO newMetaAdmin1 WITH 2. write-subnode group -----");
        supertool.setCallingUser(newMetaAdmin1Uuid);

        System.out.println("\n-------------------------------------");
        System.out.println("\n----- verify permissions for new object and address, NO PERMISSIONS -----");
        supertool.getObjectPermissions(newObjUuid, true);
        supertool.getAddressPermissions(newAddrUuid, true);
        
        System.out.println("----- and delete new object and address -> NOT ALLOWED -----");
        supertool.deleteAddressWorkingCopy(newAddrUuid, true);
        supertool.deleteObjectWorkingCopy(newObjUuid, true);

        System.out.println("\n-------------------------------------");
        System.out.println("\n----- verify permissions for new parent entities, only WRITE_SUBNODE -----");
        supertool.getObjectPermissions(newParentObjUuid, true);
        supertool.getAddressPermissions(newParentAddrUuid, true);
        
        System.out.println("----- and delete new parent entities -> NOT ALLOWED (only WRITE_SUBNODE) -----");
        supertool.deleteObject(newParentObjUuid, true);
        supertool.deleteAddress(newParentAddrUuid, true);


        
        System.out.println("\n-------------------------------------");
        System.out.println("----- !!! SWITCH \"CALLING USER\" BACK TO NEW META AUTHOR WITH permissions -----");
        supertool.setCallingUser(newMetaAuthor1Uuid);
        
        System.out.println("\n-------------------------------------");
        System.out.println("\n----- verify permissions for new object and address, has PERMISSIONS -----");
        supertool.getObjectPermissions(newObjUuid, true);
        supertool.getAddressPermissions(newAddrUuid, true);       
        
        System.out.println("----- and delete new object and address -> ALLOWED (write-tree) -----");
        supertool.deleteAddressWorkingCopy(newAddrUuid, true);
        supertool.deleteObjectWorkingCopy(newObjUuid, true);

        System.out.println("\n-------------------------------------");
        System.out.println("\n----- verify permissions for new parent entities, only WRITE_SUBNODE -----");
        supertool.getObjectPermissions(newParentObjUuid, true);
        supertool.getAddressPermissions(newParentAddrUuid, true);
        
        System.out.println("----- and delete new parent entities -> NOT ALLOWED (WRITE_SUBNODE) -----");
        supertool.deleteObject(newParentObjUuid, true);
        supertool.deleteAddress(newParentAddrUuid, true);



        System.out.println("\n-------------------------------------");
        System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
        supertool.setCallingUser(catalogAdminUuid);
        
        System.out.println("\n-------------------------------------");
        System.out.println("----- and delete new parent entities -> ALLOWED (CAT_ADMIN) -----");
        supertool.deleteObject(newParentObjUuid, true);
        supertool.deleteAddress(newParentAddrUuid, true);
        
		System.out.println("\n----- delete 2. write-subnode group, WITH FORCE DELETE WHEN HAVING USERS -> returns 'groupless' users of deleted group -----");
		supertool.deleteGroup(subnodeGrpId, true);

// ===================================
		
		System.out.println("\n----------------------------");
		System.out.println("----- CLEAN UP -----");
		System.out.println("----------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify no wrong permissions in groups -> get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);
		newAdditionalGroupDoc = supertool.getGroupDetails(nameNewAdditionalGrp);

		System.out.println("\n----- delete group, NO FORCE DELETE WHEN HAVING USERS -> ERROR: GROUP_HAS_USERS -----");
		supertool.deleteGroup(newGroupId, false);
		supertool.deleteGroup(newAdditionalGroupId, false);

		System.out.println("\n----- delete group, WITH FORCE DELETE WHEN HAVING USERS -> returns 'groupless' users of deleted group -----");
		supertool.deleteGroup(newGroupId, true);
		supertool.deleteGroup(newAdditionalGroupId, true);

		System.out.println("\n----- delete users from \"low to high\" -----");
		supertool.deleteUser(newMetaAuthor1Id);
		supertool.deleteUser(newMetaAdmin1Id);

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

	private void clearPermissionsOfGroupDoc(IngridDocument groupDoc) {
		groupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, null);
		groupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, null);
		groupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, null);		
	}

	private void setResponsibleUuidInDoc(String userUuid, IngridDocument entityDoc) {
		IngridDocument respUserDoc = new IngridDocument();
		respUserDoc.put(MdekKeys.UUID, userUuid);
		entityDoc.put(MdekKeys.RESPONSIBLE_USER, respUserDoc);
	}
	private String getResponsibleUuidFromDoc(IngridDocument entityDoc) {
		return ((IngridDocument)entityDoc.get(MdekKeys.RESPONSIBLE_USER)).getString(MdekKeys.UUID);
	}
}
