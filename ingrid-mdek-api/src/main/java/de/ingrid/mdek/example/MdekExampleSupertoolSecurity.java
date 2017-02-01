/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates Security example methods ...
 */
public class MdekExampleSupertoolSecurity {

	private MdekExampleSupertool supertoolGeneric;
	private IMdekCallerSecurity mdekCallerSecurity;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertoolSecurity(String plugIdToCall,
			String callingUserUuid,
			MdekExampleSupertool supertoolGeneric)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;
		this.supertoolGeneric = supertoolGeneric;

		// and our specific job caller !
		MdekCallerSecurity.initialize(MdekClientCaller.getInstance());
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
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

	public IngridDocument getCatalogAdmin() {
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugUserDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getGroups(boolean includeCatAdminGroup) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoCatAdminGroup = (includeCatAdminGroup) ? "WITH CatAdmin group" : "WITHOUT CatAdmin group";
		System.out.println("\n###### INVOKE getGroups " + infoCatAdminGroup + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroups(plugId, myUserUuid, includeCatAdminGroup);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.GROUPS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				supertoolGeneric.debugGroupDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getGroupDetails(String grpName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getGroupDetails of group '" + grpName + "' ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroupDetails(plugId, grpName, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugGroupDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersOfGroup(String grpName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUsersOfGroup of group '" + grpName + "' ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersOfGroup(plugId, grpName, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
/*
	public IngridDocument getUserDetails(String addrUuid) {
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugUserDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
*/
	public IngridDocument getSubUsers(Long parentUserId) {
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersWithWritePermissionForObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForObject " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForObject(plugId, objUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersWithWritePermissionForAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForAddress " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForAddress(plugId, addrUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getResponsibleUsersForNewObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getResponsibleUsersForNewObject " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getResponsibleUsersForNewObject(plugId, objUuid, myUserUuid,
				checkWorkflow, getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getResponsibleUsersForNewAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getResponsibleUsersForNewAddress " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getResponsibleUsersForNewAddress(plugId, addrUuid, myUserUuid,
				checkWorkflow, getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getUsersWithPermissionForObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithPermissionForObject " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithPermissionForObject(plugId, objUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersWithPermissionForAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithPermissionForAddress " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithPermissionForAddress(plugId, addrUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				supertoolGeneric.debugUserDoc((IngridDocument)o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getObjectPermissions(String objUuid, boolean checkWorkflow) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPermissions ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getObjectPermissions(plugId, objUuid, myUserUuid, checkWorkflow);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugPermissionsDoc(result, "");
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressPermissions(String addrUuid, boolean checkWorkflow) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPermissions ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getAddressPermissions(plugId, addrUuid, myUserUuid, checkWorkflow);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugPermissionsDoc(result, "");
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUserPermissions() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserPermissions ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserPermissions(plugId, myUserUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugPermissionsDoc(result, "");
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument createGroup(IngridDocument docIn,
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugGroupDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument createUser(IngridDocument docIn,
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugUserDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}	

	/** ALWAYS ADDS QA user-permission to group to avoid conflicts when workflow is enabled !!! */
	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch) {
		return storeGroup(docIn, refetch, true);
	}

	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch,
			boolean alwaysAddQA) {
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

		System.out.println("  ADD QA: " + alwaysAddQA);
		if (alwaysAddQA) {
			supertoolGeneric.addUserPermissionToGroupDoc(docIn, MdekUtilsSecurity.IdcPermission.QUALITY_ASSURANCE);
		}
		
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.storeGroup(plugId, docIn, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugGroupDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument storeUser(IngridDocument docIn,
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugUserDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}	

	public IngridDocument getUserDetails() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserDetails ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserDetails(plugId, myUserUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugUserDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteUser(Long idcUserId) {
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
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}	

	public IngridDocument deleteGroup(Long idcGroupId,
			boolean forceDeleteGroupWhenUsers) {
		if (idcGroupId == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String forceDeleteInfo = (forceDeleteGroupWhenUsers) ? "WITH " : "NO ";
		System.out.println("\n###### INVOKE deleteGroup " + forceDeleteInfo + " FORCE DELETE WHEN USERS ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.deleteGroup(plugId, idcGroupId, forceDeleteGroupWhenUsers, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerSecurity.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugIdcUsersDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}
}
