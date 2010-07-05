package de.ingrid.mdek.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
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
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleQueryThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("EXAMPLE_USER_" + threadNumber);
}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		// thesaurs terms (sns ids)
		// Naturschutz = uba_thes_28749: Institutionen, Einheiten und Personen
//		String termSnsId = "uba_thes_28749";
		// Emissionsüberwachung = uba_thes_8007: Institutionen und Personen
		String termSnsId = "uba_thes_8007";
		String objUuid = "3A295152-5091-11D3-AE6C-00104B57C66D";
		String adrUuid = "095130C2-DDE9-11D2-BB32-006097FE70B1";

		String uuid;
		String searchterm;
		IngridDocument doc;
		List<IngridDocument> hits;
		IngridDocument searchParams;

		boolean alwaysTrue = true;

		String hqlQueryAddr1 =
			"select aNode, addr.adrUuid, addr.adrType, addr.institution, addr.lastname, termVal.term\n\n\n" +
			"from AddressNode as aNode\n" +
			"inner join aNode.t02AddressWork addr\n\t\t\t" +
			"inner join addr.searchtermAdrs termAdrs\n\n" +
			"inner join termAdrs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where\n\n" +
			"termSns.snsId = '" + termSnsId + "' " +
			"order by addr.adrType, addr.institution, addr.lastname, addr.firstname";

		String hqlQueryAddr2 = "from AddressNode";

		String hqlQueryObj1 = "select oNode, obj.objName, termVal.term\n\n\n" +
			"from ObjectNode oNode\n" +
			"inner join oNode.t01ObjectWork obj\n\t\t\t" +
			"inner join obj.searchtermObjs termObjs " +
			"inner join termObjs.searchtermValue termVal\n\n" +
			"inner join termVal.searchtermSns termSns " +
			"where\n\n" +
			"termSns.snsId = '" + termSnsId + "' " +
			"order by obj.objClass, obj.objName";

		String hqlQueryObj2 = "from ObjectNode";

		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

// ====================
// test single stuff
// -----------------------------------
/*
		System.out.println("\n----- search objects via full text (searchterm is syslist entry !) -----");
		supertool.queryObjectsFullText("Dessau (District)", 0, 20);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
// -----------------------------------

		// Erweiterte Suche: Zeit
		// ----------------------

		searchParams = new IngridDocument();

		// additional search for title
//		searchParams.put(MdekKeys.QUERY_TERM, "wwwtest 3");
//		searchParams.put(MdekKeys.RELATION, new Integer(0));

		// "AM"
		// no intersect/contains -> 1 result (results-start AND end = TIME_AT)
//		searchParams.put(MdekKeys.TIME_AT, "20001222000000000");
		// with intersect -> 5 results (additional: results-start OR end = TIME_AT)
//		searchParams.put(MdekKeys.TIME_INTERSECT, true);
		// with contains -> 538 results (additional: results-start < TIME_AT, results-end > TIME_AT)
//		searchParams.put(MdekKeys.TIME_CONTAINS, true);
		// with intersect AND contains -> 542 results = 538 + 5 - 1 (contained in both) -> OK (see above)

		// "SEIT"
		// no intersect/contains -> 13 results (results-start >= TIME_FROM)
//		searchParams.put(MdekKeys.TIME_FROM, "20001222000000000");
		// with intersect -> 44 results (additional: results-start < TIME_FROM, results-end NOT NULL AND >= TIME_FROM)
//		searchParams.put(MdekKeys.TIME_INTERSECT, true);
		// with contains -> 519 results (additional: results-start < TIME_FROM, results-end is NULL)
//		searchParams.put(MdekKeys.TIME_CONTAINS, true);
		// with intersect AND contains -> 550 results = 519 + 44 - 13 (contained in both) -> OK (see above)

		// "BIS"
		// no intersect/contains -> 975 results (results-end <= TIME_TO)
//		searchParams.put(MdekKeys.TIME_TO, "20001222000000000");
		// with intersect -> 1516 results (additional: results-start NOT NULL AND <= TIME_TO, results-end > TIME_TO)
//		searchParams.put(MdekKeys.TIME_INTERSECT, true);
		// with contains -> 975 results (additional: results-start is NULL, results-end > TIME_TO)
//		searchParams.put(MdekKeys.TIME_CONTAINS, true);
		// with intersect AND contains -> 1516 results = 975 + 1516 - 975 (contained in both) -> OK (see above)

		// "VON" (- BIS)
		// no intersect/contains -> 4 results (results-start >= TIME_FROM, results-end <= TIME_TO)
		searchParams.put(MdekKeys.TIME_FROM, "20001222000000000");
		searchParams.put(MdekKeys.TIME_TO, "20021231000000000");
		// with intersect -> 44 results
		// (additional: results-start < TIME_FROM AND results-end >= TIME_FROM AND <= TIME_TO || results-start >= TIME_FROM AND <= TIME_TO AND results-end > TIME_TO)
		searchParams.put(MdekKeys.TIME_INTERSECT, true);
		// with contains -> 510 results (additional: results-start < TIME_FROM, results-end > TIME_TO)
		searchParams.put(MdekKeys.TIME_CONTAINS, true);
		// with intersect AND contains -> 550 results = 510 + 44 - 4 (contained in both) -> OK (see above)

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\nHits: Detailed data");
		}
		for (IngridDocument hit : hits) {
			supertool.setFullOutput(false);
			doc = supertool.fetchObject(hit.getString(MdekKeys.UUID), Quantity.DETAIL_ENTITY);
		}

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// T08_Attrs test 1 (SH catalog)
		// ----------------

		String hqlQueryObj = "select distinct OBJNODE.objUuid, OBJ.objUuid, OBJ.objName, OBJ.objClass, " +
			"T08T_1.name, T08_1.data " +
			", T08T_2.name, T08_2.data " +
			"from " +
			"ObjectNode OBJNODE " +
			"join OBJNODE.t01ObjectWork OBJ " +
			"left join OBJ.t08Attrs T08_1 " +
			"left join OBJ.t08Attrs T08_2 " +
			"left join T08_1.t08AttrType T08T_1 " +
			"left join T08_2.t08AttrType T08T_2 " +
			"where " +
			"upper(T08T_1.name) like 'DATEN NUTZUNG/PFLEGE' " +
			"and upper(T08_1.data) like 'NUTZUNG' " +
			"and upper(T08T_2.name) like 'PERSONENKREIS%ZIELGRUPPE' " +
			"and upper(T08_2.data) like 'LANUINTERN' " +
			"and OBJ.markDeleted != 'Y' " +
			"order by OBJ.objName";

		// ist das resultat obiger query
		uuid = "672E78C5-B399-11D8-92B8-0030052557A8";

		supertool.setFullOutput(true);

		System.out.println("\n----- search objects by hql query -----");
		supertool.queryHQL(hqlQueryObj, 0, 10);

		System.out.println("\n----- fetch object details -----");
		IngridDocument oMap = supertool.fetchObject(uuid, Quantity.DETAIL_ENTITY);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}

// -----------------------------------

		// T08_Attrs test 2 (SH catalog)
		// ----------------

		String hqlQueryObj = "select distinct oNode.objUuid, obj.objName, attrType.name, attr.data, attr.attrTypeId " +
			"from ObjectNode oNode " +
			"join oNode.t01ObjectWork obj " +
			"join obj.t08Attrs attr " +
			"join attr.t08AttrType attrType " +
			"where " +
			"attrType.name like '%daten%' " +
			"and attr.data like '%daten%' " +
			"order by obj.objName, attrType.name";

		supertool.setFullOutput(true);

		System.out.println("\n----- search objects by hql query -----");
		supertool.queryHQL(hqlQueryObj, 0, 10);

		System.out.println("\n----- search objects by hql to csv -----");
		supertool.queryHQLToCsv(hqlQueryObj);

		System.out.println("\n----- fetch object details -----");
		uuid = "7F15DD44-6C51-11D8-AF71-00300531999B";
//		uuid = "DA5ADBC1-846A-470F-A4C2-AF28CD77F2EC";
		IngridDocument oMap = supertool.fetchObject(uuid, Quantity.DETAIL_ENTITY);
		
		System.out.println("\n----- TEST: take over T08 attribs from published to working version ! -----");

		System.out.println("\n----- store unchanged object and refetch -> t08_attrs taken over to working version ! -----");
		supertool.storeObject(oMap, true);
		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(uuid, false);

		System.out.println("\n----- TEST: keep T08 attribs in published version when publishing ! -----");

		System.out.println("\n----- fetch object details -----");
		doc = supertool.fetchObject(uuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- remove t08 attribs (in doc) and publish -> no change of T08 attribs -----");
		doc.remove(MdekKeys.ADDITIONAL_FIELDS);
		supertool.publishObject(doc, true, false);

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ===================================

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY TO MAP");
		System.out.println("=========================");

		System.out.println("\n----- search objects by hql to MAP -----");
		String hqlQuery = "select obj.id " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj";
		supertool.queryHQLToMap(hqlQuery, 10);

		System.out.println("\n----- search objects by hql to MAP, ALL RESULTS -----");
		hqlQuery = "select obj.id " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"where oNode.fkObjUuid IS NULL";
		supertool.queryHQLToMap(hqlQuery, null);

		System.out.println("\n----- search addresses by hql to MAP -----");
		hqlQuery = "select addr.id " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr";
		supertool.queryHQLToMap(hqlQuery, 10);

		System.out.println("\n----- search addresses by hql to MAP, ALL RESULTS -----");
		hqlQuery = "select addr.id " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr " +
			"where aNode.fkAddrUuid IS NULL";
		supertool.queryHQLToMap(hqlQuery, null);
		
		// -----------------------------------

		System.out.println("\n\n-------------------------");
		System.out.println(" EXAMPLES FROM IGE FRONTEND");

		// CatalogManagementServiceImpl.getDuplicateObjects()
		// ---------------------------------------
		System.out.println("\n----- CatalogManagementServiceImpl.getDuplicateObjects() -----");
		System.out.println("----- Exception catched, may take too long or exceed message Size -----");
		String qString = "select obj.objUuid, obj.objClass, obj.objName, obj.objDescr "
			+ "from ObjectNode oNode "
				+ "inner join oNode.t01ObjectPublished obj "
			+ "where oNode.objIdPublished = oNode.objId "
			+ "order by obj.objName";
		try {
			supertool.queryHQLToMap(qString, null);			
		} catch (Exception ex) {
			// takes to long ! SELECT MAKES NO SENSE ???!!!
			System.out.println(ex.toString());
		}

		// CheckForExpiredDatasetsJob.getExpiredObjects()
		// ---------------------------------------
		System.out.println("\n----- CheckForExpiredDatasetsJob.getExpiredObjects() -----");
		qString = "select obj.objUuid, obj.objName, obj.modTime, comm.commValue," +
		"modUserAddr.institution, modUserAddr.firstname, modUserAddr.lastname " +
"from ObjectNode oNode " +
		"inner join oNode.t01ObjectPublished obj " +
		"inner join obj.objectMetadata oMeta, " +
	"AddressNode as responsibleUserNode " +
		"inner join responsibleUserNode.t02AddressWork responsibleUserAddr " +
		"inner join responsibleUserAddr.t021Communications comm, " +
	"AddressNode as modUserNode " +
		"inner join modUserNode.t02AddressWork modUserAddr " +
"where " +
	"oMeta.expiryState <= " + de.ingrid.mdek.MdekUtils.ExpiryState.INITIAL.getDbValue() +
	" and obj.responsibleUuid = responsibleUserNode.addrUuid " +
	" and comm.commtypeKey = " + de.ingrid.mdek.MdekUtils.COMM_TYPE_EMAIL +
	" and obj.modTime <= " + de.ingrid.mdek.MdekUtils.dateToTimestamp(new Date()) +
	" and modUserNode.addrUuid = obj.modUuid" +
	" and obj.modTime >= " + de.ingrid.mdek.MdekUtils.dateToTimestamp(new Date()) +
	" order by obj.objClass, obj.objName";
		supertool.queryHQLToMap(qString, 10);

		// CheckForExpiredDatasetsJob.getExpiredAddresses()
		// ---------------------------------------
		System.out.println("\n----- CheckForExpiredDatasetsJob.getExpiredAddresses() -----");
		qString = "select adr.adrUuid, adr.institution, adr.firstname, adr.lastname, adr.modTime, comm.commValue," +
		"modUserAddr.institution, modUserAddr.firstname, modUserAddr.lastname " +
"from AddressNode addrNode " +
		"inner join addrNode.t02AddressPublished adr " +
		"inner join adr.addressMetadata aMeta, " +
	"AddressNode as responsibleUserNode " +
		"inner join responsibleUserNode.t02AddressWork responsibleUserAddr " +
		"inner join responsibleUserAddr.t021Communications comm, " +
	"AddressNode as modUserNode " +
		"inner join modUserNode.t02AddressWork modUserAddr " +
"where " +
	"aMeta.expiryState <= " + de.ingrid.mdek.MdekUtils.ExpiryState.INITIAL.getDbValue() +
	" and adr.responsibleUuid = responsibleUserNode.addrUuid " +
	" and comm.commtypeKey = " + de.ingrid.mdek.MdekUtils.COMM_TYPE_EMAIL +
	" and adr.modTime <= " + de.ingrid.mdek.MdekUtils.dateToTimestamp(new Date()) +
	" and modUserNode.addrUuid = adr.modUuid" +
	" and adr.modTime >= " + de.ingrid.mdek.MdekUtils.dateToTimestamp(new Date());
		supertool.queryHQLToMap(qString, 10);
		
		// MdekEmailUtils.getAssignUserUuid(MdekDataBean)
		// ---------------------------------------
		System.out.println("\n----- MdekEmailUtils.getAssignUserUuid(MdekDataBean) -----");
		qString = "select distinct oMeta.assignerUuid " +
		"from ObjectNode oNode, " +
			" T01Object obj, " +
			" ObjectMetadata oMeta " +
		"where " +
			" oNode.objUuid = '"+objUuid+"'" +
			" and oNode.objId = obj.id " +
			" and obj.objMetadataId = oMeta.id";
		supertool.queryHQLToMap(qString, null);

		// MdekEmailUtils.getAssignUserUuid(MdekAddressBean)
		// ---------------------------------------
		System.out.println("\n----- MdekEmailUtils.getAssignUserUuid(MdekAddressBean) -----");
		qString = "select distinct aMeta.assignerUuid " +
		"from AddressNode aNode, " +
			" T02Address adr, " +
			" AddressMetadata aMeta " +
		"where " +
			" aNode.addrUuid = '"+adrUuid+"'" +
			" and aNode.addrId = adr.id " +
			" and adr.addrMetadataId = aMeta.id";
		supertool.queryHQLToMap(qString, null);

		// MdekEmailUtils.getEmailAddressesForUsers(...)
		// ---------------------------------------
		System.out.println("\n----- MdekEmailUtils.getEmailAddressesForUsers(...) -----");
		qString = "select distinct comm.commValue " +
		"from AddressNode aNode, " +
			" T021Communication comm " +
		"where " +
			" aNode.addrId = comm.adrId " +
			" and comm.commtypeKey = " + de.ingrid.mdek.MdekUtils.COMM_TYPE_EMAIL;
		qString += " and (aNode.addrUuid = '"+adrUuid+"')";
		supertool.queryHQLToMap(qString, null);

		// URLValidatorJob.fetchUrls()
		// ---------------------------------------
		System.out.println("\n----- URLValidatorJob.fetchUrls() -----");
		qString = "select obj.objUuid, obj.objName, obj.objClass, " +
		"urlRef.urlLink, urlRef.content " +
	"from ObjectNode oNode " +
		"inner join oNode.t01ObjectPublished obj " +
		"inner join obj.t017UrlRefs urlRef " +
	"where oNode.objIdPublished = oNode.objId " +
	"order by urlRef.urlLink";
		supertool.queryHQLToMap(qString, null);


		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" QUERY/UPDATE FULL TEXT ADDRESS");
		System.out.println("=========================");

		System.out.println("\n----- search addresses via full text (searchterm is syslist entry !) -----");
		searchterm = "Prof. Dr.";
		supertool.queryAddressesFullText(searchterm, 0, 20);

		System.out.println("\n----- check: update address index on STORE -----");
		System.out.println("----- search address via full text -> no result -----");
		searchterm = "sdfhljkhfösh";
		supertool.queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch arbitrary address -----");
		uuid = "095130C2-DDE9-11D2-BB32-006097FE70B1";
		doc = supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- change organization to searchterm and STORE (result is WORKING COPY !!!) -----");
		doc.put(MdekKeys.ORGANISATION, searchterm);
		supertool.setFullOutput(false);
		supertool.storeAddress(doc, true);
		supertool.setFullOutput(true);
		System.out.println("\n----- search again via full text -> RESULT (is working copy !) -----");
		supertool.queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up -----");
		supertool.deleteAddressWorkingCopy(uuid, true);

		System.out.println("\n----- check: update address index on PUBLISH -----");
		System.out.println("----- search address via full text -> no result -----");
		supertool.queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch again -----");
		doc = supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- change organization to searchterm and PUBLISH -----");
		String origOrganization = doc.getString(MdekKeys.ORGANISATION);
		doc.put(MdekKeys.ORGANISATION, searchterm);
		doc = supertool.publishAddress(doc, true);
		System.out.println("\n----- search again via full text -> RESULT (is published one, no separate working copy) -----");
		supertool.queryAddressesFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up (set orig data and publish) -----");
		doc.put(MdekKeys.ORGANISATION, origOrganization);
		supertool.publishAddress(doc, true);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" QUERY/UPDATE FULL TEXT OBJECT");
		System.out.println("=========================");

		System.out.println("\n----- search objects via full text (searchterm is syslist entry !) -----");
		searchterm = "Basisdaten";
		supertool.queryObjectsFullText(searchterm, 0, 20);

		System.out.println("\n----- check: update object index on STORE -----");
		System.out.println("----- search object via full text -> no result -----");
		searchterm = "sdfhljkhfösh";
		supertool.queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch arbitrary object -----");
		uuid = "3A295152-5091-11D3-AE6C-00104B57C66D";
		doc = supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- change title to searchterm and STORE (result is WORKING COPY !!!) -----");
		doc.put(MdekKeys.TITLE, searchterm);
		supertool.storeObject(doc, true);
		System.out.println("\n----- search again via full text -> RESULT (is working copy !) -----");
		supertool.queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up -----");
		supertool.deleteObjectWorkingCopy(uuid, true);

		System.out.println("\n----- check: update object index on PUBLISH -----");
		System.out.println("----- search object via full text -> no result -----");
		supertool.queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- fetch again -----");
		doc = supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		System.out.println("\n----- change title to searchterm and PUBLISH -----");
		String origTitle = doc.getString(MdekKeys.TITLE);
		doc.put(MdekKeys.TITLE, searchterm);
		doc = supertool.publishObject(doc, true, false);
		System.out.println("\n----- search again via full text -> RESULT (is published one, no separate working copy) -----");
		supertool.queryObjectsFullText(searchterm, 0, 20);
		System.out.println("\n----- clean up (set orig data and publish) -----");
		doc.put(MdekKeys.TITLE, origTitle);
		supertool.publishObject(doc, true, false);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" THESAURUS QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by thesaurus term (id) -----");
		hits = supertool.queryAddressesThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		System.out.println("\n----- search objects by thesaurus term (id) -----");
		hits = supertool.queryObjectsThesaurusTerm(termSnsId, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY");
		System.out.println("=========================");

		System.out.println("\n----- search addresses by hql query -----");
		supertool.queryHQL(hqlQueryAddr1, 0, 10);
		supertool.queryHQL(hqlQueryAddr2, 0, 10);

		System.out.println("\n----- search objects by hql query -----");
		supertool.queryHQL(hqlQueryObj1, 0, 10);
		supertool.queryHQL(hqlQueryObj2, 0, 10);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" HQL QUERY TO CSV");
		System.out.println("=========================");

		supertool.setFullOutput(false);

		System.out.println("\n----- search objects by hql to csv -----");
		supertool.queryHQLToCsv(hqlQueryObj1);
		String hqlQueryObj3 = "select distinct obj.objUuid, obj.objName, obj.objClass " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"order by obj.objClass, obj.objName";
		supertool.queryHQLToCsv(hqlQueryObj3);

		System.out.println("\n----- search addresses by hql to csv -----");
		supertool.queryHQLToCsv(hqlQueryAddr1);
		String hqlQueryAddr3 = "select distinct addr.adrUuid, addr.adrType, addr.institution, addr.lastname, addr.firstname " +
			"from AddressNode as aNode " +
			"inner join aNode.t02AddressWork addr " +
			"order by addr.adrType, addr.institution, addr.lastname, addr.firstname";
		supertool.queryHQLToCsv(hqlQueryAddr3);

		supertool.setFullOutput(true);

		// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println(" EXTENDED SEARCH OBJECTS");
		System.out.println("=========================");
		
		System.out.println("\n----- search objects by extended search query: Göttingen -----");
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "Göttingen");
		searchParams.put(MdekKeys.RELATION, new Integer(0));

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);

		System.out.println("\n----- search objects by extended search query: \"Göttingen Wasserrecht\" -----");
		searchParams.put(MdekKeys.QUERY_TERM, "Göttingen Wasserrecht");
		searchParams.put(MdekKeys.RELATION, new Integer(0));

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);

		System.out.println("\n----- search objects by extended search query: Göttingen, partial word -----");
		searchParams.put(MdekKeys.QUERY_TERM, "Göttingen");
		searchParams.put(MdekKeys.SEARCH_TYPE, new Integer(1));
		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		
		System.out.println("\n----- search objects by extended search query: Göttingen, partial word, Object class=0-----");
		List<Integer> aList = new ArrayList<Integer>();
		aList.add(0);
		searchParams.put(MdekKeys.OBJ_CLASSES, aList);

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		
		System.out.println("\n----- search objects by extended search query: Göttingen, partial word, Object class=0, sns_thesaurus_id:uba_thes_11450 & uba_thes_28711 -----");
		List<IngridDocument> docList = new ArrayList<IngridDocument>();
		doc = new IngridDocument();
		doc.put(MdekKeys.TERM_SNS_ID, "uba_thes_11450");
		docList.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.TERM_SNS_ID, "uba_thes_28711");
		docList.add(doc);
		searchParams.put(MdekKeys.THESAURUS_TERMS, docList);
		searchParams.put(MdekKeys.THESAURUS_RELATION, new Integer(0));
		
		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}		
		
		System.out.println("\n----- search objects by extended search query: geothesaurus.location-sns-id:BUNDESLAND03 -----");
		docList = new ArrayList<IngridDocument>();
		doc = new IngridDocument();
		doc.put(MdekKeys.LOCATION_SNS_ID, "BUNDESLAND03");
		docList.add(doc);
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.GEO_THESAURUS_TERMS, docList);
		searchParams.put(MdekKeys.GEO_THESAURUS_RELATION, new Integer(1));

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);

		System.out.println("\n----- search objects by extended search query: geothesaurus.location-sns-id:BUNDESLAND03 & KREIS0346200000 -----");
		docList = new ArrayList<IngridDocument>();
		doc = new IngridDocument();
		doc.put(MdekKeys.LOCATION_SNS_ID, "BUNDESLAND03");
		docList.add(doc);
		doc = new IngridDocument();
		doc.put(MdekKeys.LOCATION_SNS_ID, "KREIS0346200000");
		docList.add(doc);
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.GEO_THESAURUS_TERMS, docList);
		searchParams.put(MdekKeys.GEO_THESAURUS_RELATION, new Integer(0));

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		System.out.println("\n----- search objects by extended search query: Informationssystem thesaurus.term-sns-id:uba_thes_8007 -----");
		docList = new ArrayList<IngridDocument>();
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "Informationssystem");
		searchParams.put(MdekKeys.RELATION, new Integer(0));
		aList = new ArrayList<Integer>();
		aList.add(0);
		aList.add(1);
		aList.add(2);
		aList.add(3);
		aList.add(4);
		aList.add(5);
		searchParams.put(MdekKeys.OBJ_CLASSES, aList);

		doc = new IngridDocument();
		doc.put(MdekKeys.TERM_SNS_ID, "uba_thes_8007");
		docList.add(doc);
		searchParams.put(MdekKeys.THESAURUS_TERMS, docList);
		searchParams.put(MdekKeys.THESAURUS_RELATION, new Integer(0));

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}		

		System.out.println("\n----- search objects by extended search query: time-from:19690821000000000 time-to:20081231000000000 -----");
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.TIME_FROM, "19690821000000000");
		searchParams.put(MdekKeys.TIME_TO, "20081231000000000");

		hits = supertool.queryObjectsExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchObject(uuid, FetchQuantity.EDITOR_ENTITY);
		}
		
		// -----------------------------------
		
		System.out.println("\n\n=========================");
		System.out.println(" EXTENDED SEARCH ADDRESSES");
		System.out.println("=========================");
		
		System.out.println("\n----- search addresses by extended search query: Wirtschaft -----");
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "Wirtschaft");
		searchParams.put(MdekKeys.RELATION, new Integer(0));
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}
		
		System.out.println("\n----- search addresses by extended search query: Wirtschaft, partial word -----");
		searchParams.put(MdekKeys.SEARCH_TYPE, new Integer(1));
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}
		
		System.out.println("\n----- search addresses by extended search query: Wirtschaft, partial word, partial-index -----");
		searchParams.put(MdekKeys.SEARCH_RANGE, new Integer(1));
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}

		System.out.println("\n----- search addresses by extended search query: Wirtschaft, partial word, partial-index, city:Braunschweig -----");
		searchParams.put(MdekKeys.CITY, "Braunschweig");
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}		

		System.out.println("\n----- search addresses by extended search query: Peter -----");
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "Peter");
		searchParams.put(MdekKeys.RELATION, new Integer(0));
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}
		
		System.out.println("\n----- search addresses by extended search query: Peter search-range:1 -----");
		searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "Peter");
		searchParams.put(MdekKeys.RELATION, new Integer(0));
		searchParams.put(MdekKeys.SEARCH_RANGE, new Integer(1));
		
		hits = supertool.queryAddressesExtended(searchParams, 0, 20);
		if (hits.size() > 0) {
			System.out.println("\n----- verify: fetch first result ! -----");
			uuid = hits.get(0).getString(MdekKeys.UUID);
			supertool.fetchAddress(uuid, FetchQuantity.EDITOR_ENTITY);
		}
		
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
