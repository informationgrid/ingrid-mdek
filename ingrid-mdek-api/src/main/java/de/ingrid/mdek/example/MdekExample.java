package de.ingrid.mdek.example;

import java.io.File;
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

		String parentUuid = "3866463B-B449-11D2-9A86-080000507261";
		String objUuid = "5CE671D3-5475-11D3-A172-08002B9A1D1D";
		IngridDocument oMap;

		//System.out.println("\n###### INVOKE testMdekEntity ######");
		//IMdekCaller mdekCaller = MdekCaller.getInstance();
		//mdekCaller.testMdekEntity(threadNumber);

		// -----------------------------------

		System.out.println("\n----- top objects -----");
		fetchTopObjects();

		// -----------------------------------

		System.out.println("\n----- sub objects -----");
		fetchSubObjects(parentUuid);

		// -----------------------------------

		System.out.println("\n----- object path -----");
		getObjectPath(objUuid);

		// -----------------------------------

		System.out.println("\n----- object details -----");
		oMap = fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		// -----------------------------------

		// change and store existing object
		System.out.println("\n----- change and store object -----");
		storeObject(oMap);

		// store NEW object and get Uuid
		System.out.println("\n----- store new object -----");
		IngridDocument objDoc = new IngridDocument();
		objDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		// supply parent uuid !
		objDoc.put(MdekKeys.PARENT_UUID, objUuid);
		oMap = storeObject(objDoc);
		// uuid created !
		String newUuid = (String) oMap.get(MdekKeys.UUID);

		// verify new Subobject
		System.out.println("\n----- verify new subobject -> load parent subobjects -----");
		fetchSubObjects(objUuid);

		// -----------------------------------

		// delete new Object
		System.out.println("\n----- delete new object (WORKING COPY) -----");
		deleteObjectWorkingCopy(newUuid);
		System.out.println("\n----- verify deletion of new object -----");
		fetchObject(newUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- verify \"deletion of parent association\" -> load parent subobjects -----");
		fetchSubObjects(objUuid);

		// -----------------------------------

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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}
*/

		isRunning = false;
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}
		
		return result;
	}

	private IngridDocument storeObject(IngridDocument obj) {
		// check whether we have an object
		if (obj == null) {
			return null;
		}

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeObject ######");
		
		// first manipulat former loaded object !
//		obj.put(MdekKeys.TITLE, obj.get(MdekKeys.TITLE) + "Hallo");

		// remove first address !
		List<IngridDocument> adrs = (List<IngridDocument>) obj.get(MdekKeys.ADR_REFERENCES_TO);
		IngridDocument aRemoved = null;
		if (adrs != null && adrs.size() > 0) {
			aRemoved = adrs.get(0);
			System.out.println("REMOVE FIRST RELATED ADDRESS: " + aRemoved);
			adrs.remove(0);			
		}

		// remove first object Querverweis !
		List<IngridDocument> objs = (List<IngridDocument>) obj.get(MdekKeys.OBJ_REFERENCES_TO);
		IngridDocument oRemoved = null;
		if (objs != null && objs.size() > 0) {
			oRemoved = objs.get(0);
			System.out.println("REMOVE FIRST OBJECT QUERVERWEIS: " + oRemoved);
			objs.remove(0);			
		}

		// store
		System.out.println("STORE");
		startTime = System.currentTimeMillis();
		System.out.println("storeObject WITHOUT refetching object: ");
		response = mdekCaller.storeObject(obj, false);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			System.out.println("refetch Object");
			IngridDocument oMap = fetchObject(uuidStoredObject, Quantity.DETAIL_ENTITY);
			System.out.println("");
			
			if (aRemoved != null) {
				// and add address again
				adrs.add(aRemoved);
				System.out.println("ADD REMOVED ADDRESS AGAIN: " + aRemoved);
			}
			if (oRemoved != null) {
				// and add object again
				objs.add(oRemoved);
				System.out.println("ADD REMOVED OBJECT QUERVERWEIS AGAIN: " + oRemoved);
			}
			if (aRemoved != null || oRemoved != null) {
				System.out.println("STORE");
				startTime = System.currentTimeMillis();
				System.out.println("storeObject WITH refetching object: ");
				response = mdekCaller.storeObject(obj, true);
				endTime = System.currentTimeMillis();
				neededTime = endTime - startTime;
				System.out.println("EXECUTION TIME: " + neededTime + " ms");
				result = mdekCaller.getResultFromResponse(response);

				if (result != null) {
					System.out.println("SUCCESS: ");
					debugObjectDoc(result);
				} else {
					System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
				}					
			}
			
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
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
		List<IngridDocument> objs = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (objs != null) {
			System.out.println("  Objects TO (Querverweise): " + objs.size() + " Entities");
			for (IngridDocument oTo : objs) {
				System.out.println("   " + oTo);								
			}			
		}
		objs = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (objs != null) {
			System.out.println("  Objects FROM (Querverweise): " + objs.size() + " Entities");
			for (IngridDocument oFrom : objs) {
				System.out.println("   " + oFrom);								
			}			
		}
		List<IngridDocument> adrs = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (adrs != null) {
			System.out.println("  Addresses TO: " + adrs.size() + " Entities");
			for (IngridDocument a : adrs) {
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
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
