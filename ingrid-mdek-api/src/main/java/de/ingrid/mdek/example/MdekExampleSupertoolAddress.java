/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates Address example methods ...
 */
public class MdekExampleSupertoolAddress {

	private MdekExampleSupertool supertoolGeneric;
	private IMdekCallerAddress mdekCallerAddress;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertoolAddress(String plugIdToCall,
			String callingUserUuid,
			MdekExampleSupertool supertoolGeneric)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;
		this.supertoolGeneric = supertoolGeneric;

		// and our specific job caller !
		MdekCallerAddress.initialize(MdekClientCaller.getInstance());
		mdekCallerAddress = MdekCallerAddress.getInstance();
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

	public IngridDocument getAddressPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPath ######");
		System.out.println("- uuid: " + uuidIn);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressPath(plugId, uuidIn, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
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

	public IngridDocument getInitialAddress(IngridDocument newBasicAddress) {
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
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugAddressDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String onlyFreeAddressesInfo = (onlyFreeAddresses) ? "ONLY FREE ADDRESSES" : "ONLY NON FREE ADDRESSES";
		System.out.println("\n###### INVOKE fetchTopAddresses " + onlyFreeAddressesInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchTopAddresses(plugId, myUserUuid, onlyFreeAddresses);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			if (!doFullOutput) {
				System.out.println("  " + l);				
			} else {
				for (Object o : l) {
					doFullOutput = false;
					supertoolGeneric.debugAddressDoc((IngridDocument)o);
					doFullOutput = true;
				}				
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchSubAddresses(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchSubAddresses(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				supertoolGeneric.debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/** Fetches WORKING VERSION of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION, 0, 50);
	}

	/** Fetches requested version of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion) {
		return fetchAddress(uuid, howMuch, whichVersion, 0, 50);
	}

	/** Fetches WORKING VERSION of address ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION,
				objRefsStartIndex, objRefsMaxNum);
	}

	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion,
			int objRefsStartIndex, int objRefsMaxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- fetch entity version: " + whichVersion);
		System.out.println("- fetch quantity: " + howMuch);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, howMuch, whichVersion,
				objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugAddressDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchAddressObjectReferences(String uuid, int objRefsStartIndex, int objRefsMaxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddressObjectReferences ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddressObjectReferences(plugId, uuid, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			boolean formerFullOutput = doFullOutput;
			setFullOutput(true);
			supertoolGeneric.debugAddressDoc(result);
			setFullOutput(formerFullOutput);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument checkAddressSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkAddressSubTree ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.checkAddressSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
		return storeAddress(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeAddress ######");
		System.out.println("- uuid (may be null if new object?): " + aDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchAddress);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugAddressDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument updateAddressPart(IngridDocument aPartDocIn, IdcEntityVersion whichVersion) {
		// check whether we have an object
		if (aPartDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateAddressPart ######");
		System.out.println("- uuid: " + aPartDocIn.get(MdekKeys.UUID));
		System.out.println("- in whichVersion: " + whichVersion);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.updateAddressPart(plugId, aPartDocIn, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS");
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress) {
		return assignAddressToQA(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE assignAddressToQA ######");
		System.out.println("- uuid (may be null if new object?): " + aDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchAddress);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.assignAddressToQA(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugAddressDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress) {
		return reassignAddressToAuthor(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE reassignAddressToAuthor ######");
		System.out.println("- uuid: " + aDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + refetchAddress);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.reassignAddressToAuthor(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugAddressDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean refetchAddress, boolean forcePublicationCondition) {
		return publishAddress(aDocIn, refetchAddress, forcePublicationCondition, 0, 50);
	}

	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch,
			boolean forcePublicationCondition,
			int objRefsStartIndex, int objRefsMaxNum) {
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE publishAddress  ######");
		System.out.println("- uuid (may be null if new object): " + aDocIn.get(MdekKeys.UUID));
		System.out.println("- refetch: " + withRefetch);
		System.out.println("- forcePublicationCondition: " + forcePublicationCondition);
		System.out.println("- fetch objRefs start: " + objRefsStartIndex);
		System.out.println("- fetch objRefs maxNum: " + objRefsMaxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.publishAddress(plugId, aDocIn, withRefetch, forcePublicationCondition, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuid = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuid);
			if (withRefetch) {
				supertoolGeneric.debugAddressDoc(result);
			}
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			boolean forcePublicationCondition)
	{
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE moveAddress ######");
		System.out.println("- from (node moved): " + fromUuid);
		System.out.println("- to (new parent): " + toUuid);
		System.out.println("- moveToFreeAddress: " + moveToFreeAddress);
		System.out.println("- forcePublicationCondition: " + forcePublicationCondition);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.moveAddress(plugId, fromUuid, toUuid, moveToFreeAddress, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument copyAddress(String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress)
		{
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			System.out.println("\n###### INVOKE copyAddress ######");
			System.out.println("- from (node copied): " + fromUuid);
			System.out.println("- to (new parent): " + toUuid);
			System.out.println("- copyToFreeAddress: " + copyToFreeAddress);
			System.out.println("- copySubtree: " + copySubtree);
			startTime = System.currentTimeMillis();
			response = mdekCallerAddress.copyAddress(plugId, fromUuid, toUuid, copySubtree, copyToFreeAddress, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCallerAddress.getResultFromResponse(response);
			if (result != null) {
				System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
				System.out.println("Copy Node (rudimentary): ");
				supertoolGeneric.debugAddressDoc(result);
			} else {
				supertoolGeneric.handleError(response);
			}
			
			return result;
		}

	public IngridDocument mergeAddressToSubAddresses(String parentUuid)
	{
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE mergeAddressToSubAddresses ######");
		System.out.println("- parent of merge: " + parentUuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.mergeAddressToSubAddresses(plugId, parentUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " subnodes merged !");
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteAddressWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteAddressWorkingCopy ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- forceDeleteReferences: " + forceDeleteReferences);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddressWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteAddress(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteAddress ######");
		System.out.println("- uuid: " + uuid);
		System.out.println("- forceDeleteReferences: " + forceDeleteReferences);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddress(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument searchAddress(IngridDocument searchParams,
			int startHit, int numHits) {
		if (searchParams == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE searchAddress ######");
		System.out.println("- searchParams:" + searchParams);
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.searchAddresses(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);

		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + l.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument a : l) {
				supertoolGeneric.debugAddressDoc(a);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument getWorkAddresses(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getWorkAddresses ######");
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getWorkAddresses(plugId,
				selectionType, orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
				System.out.println("  - total num QA:  assigned=" + result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED) + ", " +
				" reassigned=" + result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED));
			}
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				supertoolGeneric.debugAddressDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/**
	 * @param whichWorkState only return addresses in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all addresses
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAAddresses(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getQAAddresses ######");
		System.out.println("- work state: " + whichWorkState);
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getQAAddresses(plugId,
				whichWorkState, selectionType,
				orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				supertoolGeneric.debugAddressDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressStatistics(String uuidIn, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressStatistics ######");
		System.out.println("- statistics type:" + whichType);
		System.out.println("- top node of branch:" + uuidIn);
		System.out.println("- only free addresses:" + onlyFreeAddresses);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressStatistics(plugId, uuidIn, onlyFreeAddresses,
				whichType, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerAddress.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
}
