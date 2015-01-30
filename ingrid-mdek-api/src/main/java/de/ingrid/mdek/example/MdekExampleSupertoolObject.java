/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.example;

import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates Object example methods ...
 */
public class MdekExampleSupertoolObject {

	private MdekExampleSupertool supertoolGeneric;
	private IMdekCallerObject mdekCallerObject;
	
	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertoolObject(String plugIdToCall,
			String callingUserUuid,
			MdekExampleSupertool supertoolGeneric)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;
		this.supertoolGeneric = supertoolGeneric;

		// and our specific job caller !
		MdekCallerObject.initialize(MdekClientCaller.getInstance());
		mdekCallerObject = MdekCallerObject.getInstance();
	}

	public void setPlugIdToCall(String plugIdToCall)
	{
		this.plugId = plugIdToCall;
	}

	public void setCallingUser(String callingUserUuid)
	{
		this.myUserUuid = callingUserUuid;
	}

	public void setFullOutput(boolean doFullOutput)
	{
		this.doFullOutput = doFullOutput;
	}

	public IngridDocument getObjectPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPath ######");
		System.out.println("- uuid: " + uuidIn);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectPath(plugId, uuidIn, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			List<String> uuidList = (List<String>) result.get(MdekKeys.PATH);
			System.out.println("SUCCESS: " + uuidList.size() + " levels");
			String indent = " ";
			for (String uuid : uuidList) {
				System.out.println(indent + uuid);
				indent += " ";
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getInitialObject(IngridDocument newBasicObject) {
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
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugObjectDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchTopObjects() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchTopObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchTopObjects(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchSubObjects(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubObjects ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchSubObjects(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/** Fetches WORKING VERSION of object ! */
	public IngridDocument fetchObject(String uuid, FetchQuantity howMuch) {
		return fetchObject(uuid, howMuch, IdcEntityVersion.WORKING_VERSION);
		
	}

	public IngridDocument fetchObject(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- fetch entity version: " + whichVersion);
		System.out.println("- fetch quantity: " + howMuch);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, howMuch, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugObjectDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument checkObjectSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkObjectSubTree ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.checkObjectSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeObject(IngridDocument oDocIn,
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
		System.out.println("- uuid (may be null if new object?): " + oDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchObject);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.storeObject(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugObjectDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument updateObjectPart(IngridDocument oPartDocIn, IdcEntityVersion whichVersion) {
		// check whether we have an object
		if (oPartDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateObjectPart ######");
		System.out.println("- uuid: " + oPartDocIn.get(MdekKeys.UUID));
		System.out.println("- in whichVersion: " + whichVersion);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.updateObjectPart(plugId, oPartDocIn, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS");
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument assignObjectToQA(IngridDocument oDocIn,
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

		System.out.println("\n###### INVOKE assignObjectToQA ######");
		System.out.println("- uuid (may be null if new object?): " + oDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchObject);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.assignObjectToQA(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugObjectDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument reassignObjectToAuthor(IngridDocument oDocIn,
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

		System.out.println("\n###### INVOKE reassignObjectToAuthor ######");
		System.out.println("- uuid: " + oDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchObject);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.reassignObjectToAuthor(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugObjectDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument publishObject(IngridDocument oDocIn,
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
		System.out.println("- uuid (may be null if new object): " + oDocIn.get(MdekKeys.UUID));
		System.out.println("- refetchObject: " + withRefetch);
		System.out.println("- forcePublicationCondition: " + forcePublicationCondition);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.publishObject(plugId, oDocIn, withRefetch, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			if (withRefetch) {
				supertoolGeneric.debugObjectDoc(result);
			}
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument moveObject(String fromUuid, String toUuid,
			boolean forcePublicationCondition) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE moveObject ######");
		System.out.println("- from (node moved): " + fromUuid);
		System.out.println("- to (new parent): " + toUuid);
		System.out.println("- forcePublicationCondition: " + forcePublicationCondition);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.moveObject(plugId, fromUuid, toUuid, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE copyObject ######");
		System.out.println("- from (node copied): " + fromUuid);
		System.out.println("- to (new parent): " + toUuid);
		System.out.println("- copySubtree: " + copySubtree);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.copyObject(plugId, fromUuid, toUuid, copySubtree, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
			System.out.println("Root Copy: " + result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteObjectWorkingCopy ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- forceDeleteReferences: " + forceDeleteReferences);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObjectWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));

		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteObject ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- forceDeleteReferences: " + forceDeleteReferences);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObject(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getWorkObjects(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getWorkObjects ######");
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getWorkObjects(plugId,
				selectionType, orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
				System.out.println("  - total num QA:  assigned=" + result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED) + ", " +
				" reassigned=" + result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED));
			}
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				supertoolGeneric.debugObjectDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/**
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAObjects(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getQAObjects ######");
		System.out.println("- work state: " + whichWorkState);
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getQAObjects(plugId, 
				whichWorkState, selectionType, 
				orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				supertoolGeneric.debugObjectDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectStatistics(String uuidIn,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectStatistics ######");
		System.out.println("- statistics type:" + whichType);
		System.out.println("- top node of branch:" + uuidIn);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectStatistics(plugId, uuidIn, whichType, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerObject.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
}
