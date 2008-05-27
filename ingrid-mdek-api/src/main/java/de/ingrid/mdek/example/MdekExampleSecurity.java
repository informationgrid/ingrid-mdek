package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
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
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		String parentObjUuid = "D40743ED-1FC3-11D3-AF50-0060084A4596";

		// children of parents above
		String addrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		String objUuid = "128EFA64-436E-11D3-A599-70A253C18B13";


		IngridDocument doc;

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		IngridDocument catAdminDoc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) catAdminDoc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = catAdminDoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

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

		System.out.println("\n---------------------------------------------");
		System.out.println("----- VALIDATE GROUP OBJECT PERMISSION STRUCTURE -----");

		System.out.println("\n----- double permission on OBJECT -> ERROR -----");
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- tree below tree permission on OBJECT -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single below tree permission on OBJECT -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single ABOVE tree permission on OBJECT -> OK -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addObjPermissionToGroupDoc(newGroupDoc, parentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- VALIDATE GROUP ADDRESS PERMISSION STRUCTURE -----");

		System.out.println("\n----- double permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- tree below tree permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single below tree permission on ADDRESS -> ERROR -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- single ABOVE tree permission on ADDRESS -> OK -----");
		clearPermissionsOfGroupDoc(newGroupDoc);
		addAddrPermissionToGroupDoc(newGroupDoc, parentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
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
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		secondCatAdminDoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue());
		secondCatAdminDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, null);
		supertool.createUser(secondCatAdminDoc, true);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR 1' -> ALLOWED -----");
		IngridDocument newMetaAdmin1Doc = new IngridDocument();
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE6-FE15-11D2-AF34-0060084A4596");
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAdmin1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdmin1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdmin1Doc = supertool.createUser(newMetaAdmin1Doc, true);
		Long newMetaAdmin1Id = (Long) newMetaAdmin1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdmin1Uuid = newMetaAdmin1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create new user 'MD_AUTHOR 1' -> ALLOWED -----");
		IngridDocument newMetaAuthor1Doc = new IngridDocument();
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "386645BC-B449-11D2-9A86-080000507261");
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAuthor1Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_AUTHOR.getDbValue());
		newMetaAuthor1Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, newMetaAdmin1Id);
		newMetaAuthor1Doc = supertool.createUser(newMetaAuthor1Doc, true);
		Long newMetaAuthor1Id = (Long) newMetaAuthor1Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAuthor1Uuid = newMetaAuthor1Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n----- create 2ND new user 'MD_ADMINISTRATOR 2' -> ALLOWED -----");
		IngridDocument newMetaAdmin2Doc = new IngridDocument();
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "15C69BE4-FE15-11D2-AF34-0060084A4596");
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
		newMetaAdmin2Doc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		newMetaAdmin2Doc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		newMetaAdmin2Doc = supertool.createUser(newMetaAdmin2Doc, true);
		Long newMetaAdmin2Id = (Long) newMetaAdmin2Doc.get(MdekKeysSecurity.IDC_USER_ID);
		String newMetaAdmin2Uuid = newMetaAdmin2Doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

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
		newMetaAuthor2Doc.put(MdekKeysSecurity.IDC_GROUP_ID, newGroupId);
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

		System.out.println("\n----- store MD_AUTHOR (change addr uuid of user and store) -> ALLOWED -----");
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
		newMetaAdmin1Doc = supertool.storeUser(newMetaAdmin1Doc, true);		

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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);

		System.out.println("\n----- set Responsible User and store object -> working copy with 'MD_ADMIN 2' as ResponsibleUser -----");
		System.out.println("----- !!! ResponsibleUser = " + newMetaAdmin2Uuid);
		setResponsibleUuidInDoc(newMetaAdmin2Uuid, doc);
		supertool.storeObject(doc, false);

		System.out.println("\n----------------------------");
		System.out.println("----- set 'MD_ADMIN 2' as responsible user in ADDRESS ! -----");

		System.out.println("\n----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		String respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Object ResponsibleUser = " + respUuid);
		if (!respUuid.equals(addrUuidNotUsedForUser)) {
			throw new RuntimeException("ERROR: Object ResponsibleUser NOT ADAPTED on User Address change");
		}

		System.out.println("\n----- verify CHANGED ResponsibleUser in ADDRESS -----");
		System.out.println("----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		respUuid = getResponsibleUuidFromDoc(doc);
		System.out.println("----- !!! NEW Object ResponsibleUser = " + respUuid);
		if (!respUuid.equals(catalogAdminUuid)) {
			throw new RuntimeException("ERROR: Object ResponsibleUser NOT ADAPTED on User DELETE");
		}

		System.out.println("\n----- verify CHANGED ResponsibleUser in ADDRESS -----");
		System.out.println("----- fetch address -----");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
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
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- publish address -> NOT ALLOWED -----");
		supertool.publishAddress(doc, false);

		System.out.println("\n-------------------------------------");
		System.out.println("\n----- store object -> NOT ALLOWED -----");
		System.out.println("-- first fetch object");
		supertool.setFullOutput(false);
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		supertool.debugPermissionsDocBoolean(doc);
		supertool.storeObject(doc, false);

		System.out.println("\n----- publish object -> NOT ALLOWED -----");
		supertool.publishObject(doc, false, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add address/object WRITE_SINGLE permissions to group -----");
		addObjPermissionToGroupDoc(newGroupDoc, objUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		addAddrPermissionToGroupDoc(newGroupDoc, addrUuid, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
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
		System.out.println("----- write address -> ALLOWED (WRITE_SINGLE) -----");
		System.out.println("-- first fetch address");
		supertool.setFullOutput(false);
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
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
		supertool.getUsersWithWritePermissionForObject(objUuid, false);
		supertool.getUsersWithWritePermissionForObject(objUuid, true);

		System.out.println("\n----- users with write permission for address: " + addrUuid);
		supertool.getUsersWithWritePermissionForAddress(addrUuid, false);
		supertool.getUsersWithWritePermissionForAddress(addrUuid, true);

// ===================================
		
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
		addUserPermissionToGroupDoc(newGroupDoc, MdekUtilsSecurity.IdcPermission.CREATE_ROOT);
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

// ===================================
		
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
		addObjPermissionToGroupDoc(newGroupDoc, newParentObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		addAddrPermissionToGroupDoc(newGroupDoc, newParentAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		supertool.storeObject(doc, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

		System.out.println("\n----- REMOVE permission and store group -> ERROR: USER_EDITING_OBJECT_PERMISSION_MISSING -----");
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
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
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
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
		supertool.setFullOutput(true);
		supertool.storeAddress(doc, false);

		System.out.println("\n----- store group KEEPING permission -> OK, group stored ! -----");
		newGroupDoc = supertool.storeGroup(newGroupDoc, true);

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
		doc = supertool.fetchAddress(addrUuid, Quantity.DETAIL_ENTITY);
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
		
		System.out.println("\n----------------------------");
		System.out.println("----- CLEAN UP -----");
		System.out.println("----------------------------");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- verify no wrong permissions in group -> get group details -----");
		newGroupDoc = supertool.getGroupDetails(nameNewGrp);

		System.out.println("\n----- delete group, NO FORCE DELETE WHEN HAVING USERS -> ERROR: GROUP_HAS_USERS -----");
		supertool.deleteGroup(newGroupId, false);

		System.out.println("\n----- delete group, WITH FORCE DELETE WHEN HAVING USERS -> returns 'groupless' users of deleted group -----");
		supertool.deleteGroup(newGroupId, true);

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

	private void addObjPermissionToGroupDoc(IngridDocument groupDoc, String objUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, objUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	private void addAddrPermissionToGroupDoc(IngridDocument groupDoc, String addrUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, addrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	private void addUserPermissionToGroupDoc(IngridDocument groupDoc, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
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
