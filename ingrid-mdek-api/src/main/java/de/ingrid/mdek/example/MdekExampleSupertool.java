package de.ingrid.mdek.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates common example methods ...
 */
public class MdekExampleSupertool {

	private IMdekCaller mdekCaller;
	private IMdekCallerSecurity mdekCallerSecurity;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerAddress mdekCallerAddress;
	private IMdekCallerCatalog mdekCallerCatalog;
	private IMdekCallerQuery mdekCallerQuery;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertool(String plugIdToCall,
			String callingUserUuid)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;

		mdekCaller = MdekCaller.getInstance();
		
		// and our specific job caller !
		MdekCallerSecurity.initialize(mdekCaller);
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
		MdekCallerObject.initialize(mdekCaller);
		mdekCallerObject = MdekCallerObject.getInstance();
		MdekCallerAddress.initialize(mdekCaller);
		mdekCallerAddress = MdekCallerAddress.getInstance();
		MdekCallerCatalog.initialize(mdekCaller);
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
		MdekCallerQuery.initialize(mdekCaller);
		mdekCallerQuery = MdekCallerQuery.getInstance();
	}

	public void setPlugIdToCall(String plugIdToCall)
	{
		this.plugId = plugIdToCall;
	}

	public void setCallingUser(String callingUserUuid)
	{
		this.myUserUuid = callingUserUuid;
	}
	public String getCallingUserUuid()
	{
		return myUserUuid;
	}

	public void setFullOutput(boolean doFullOutput)
	{
		this.doFullOutput = doFullOutput;
	}

	public void getVersion() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getVersion ######");
		startTime = System.currentTimeMillis();
		// ACHTUNG: ist DIREKT result ! sollte nie null sein (hoechstens leer)
		response = mdekCaller.getVersion(plugId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");

		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("All entries in Map: ");
			Set<Map.Entry> entries = result.entrySet();
			for (Map.Entry entry : entries) {
				System.out.println("  " + entry);
			}
			System.out.println("Explicit read of entries: ");
			System.out.println("  API_BUILD_NAME: " + result.get(MdekKeys.API_BUILD_NAME));
			System.out.println("  API_BUILD_VERSION: " + result.get(MdekKeys.API_BUILD_VERSION));
			System.out.println("  API_BUILD_NUMBER: " + result.get(MdekKeys.API_BUILD_NUMBER));
			System.out.println("  API_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.API_BUILD_TIMESTAMP)));
			System.out.println("  SERVER_BUILD_NAME: " + result.get(MdekKeys.SERVER_BUILD_NAME));
			System.out.println("  SERVER_BUILD_VERSION: " + result.get(MdekKeys.SERVER_BUILD_VERSION));
			System.out.println("  SERVER_BUILD_NUMBER: " + result.get(MdekKeys.SERVER_BUILD_NUMBER));
			System.out.println("  SERVER_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.SERVER_BUILD_TIMESTAMP)));

		} else {
			handleError(response);
		}
	}

	public IngridDocument getCatalog() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchCatalog ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.fetchCatalog(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getGroups() {
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

	public IngridDocument getGroupDetails(String grpName) {
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

	public IngridDocument getUsersWithWritePermissionForObject(String objUuid, boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForObject " + infoDetailedPermissions + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForObject(plugId, objUuid, myUserUuid, getDetailedPermissions);
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

	public IngridDocument getUsersWithWritePermissionForAddress(String addrUuid, boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForAddress " + infoDetailedPermissions + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForAddress(plugId, addrUuid, myUserUuid, getDetailedPermissions);
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

	public IngridDocument getObjectPermissions(String objUuid) {
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
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressPermissions(String addrUuid) {
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
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
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
		response = mdekCallerSecurity.getUserPermissions(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getSysLists(Integer[] listIds, String language) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysLists, language: " + language + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysLists(plugId, listIds, language, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			Set<String> listKeys = result.keySet();
			System.out.println("SUCCESS: " + listKeys.size() + " sys-lists");
			for (String listKey : listKeys) {
				IngridDocument listDoc = (IngridDocument) result.get(listKey);
				List<IngridDocument> entryDocs =
					(List<IngridDocument>) listDoc.get(MdekKeys.LST_ENTRY_LIST);
				System.out.println("  " + listKey + ": " + entryDocs.size() + " entries");
				System.out.println("    " + entryDocs);				
			}
			
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectPath(plugId, uuidIn, myUserUuid);
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
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressPath(plugId, uuidIn, myUserUuid);
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
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String onlyFreeAddressesInfo = (onlyFreeAddresses) ? "ONLY FREE ADDRESSES" : "ONLY NO FREE ADDRESSES";
		System.out.println("\n###### INVOKE fetchTopAddresses " + onlyFreeAddressesInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchTopAddresses(plugId, myUserUuid, onlyFreeAddresses);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
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
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchSubObjects(plugId, uuid, myUserUuid);
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
			handleError(response);
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
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchSubAddresses(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchObject(String uuid, Quantity howMuch) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, howMuch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchAddress(String uuid, Quantity howMuch) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, howMuch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
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
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.checkObjectSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
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
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.checkAddressSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
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

		String refetchObjectInfo = (refetchObject) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeObject " + refetchObjectInfo + " ######");

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
			debugObjectDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument storeAddress(IngridDocument aDocIn,
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
			debugAddressDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument storeGroup(IngridDocument docIn,
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
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
				debugObjectDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument publishAddress(IngridDocument aDocIn,
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
				debugAddressDoc(result);
			}
		} else {
			handleError(response);
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

		String forcePubCondInfo = (forcePublicationCondition) ? "WITH FORCE publicationCondition" 
				: "WITHOUT FORCE publicationCondition";
		System.out.println("\n###### INVOKE moveObject " + forcePubCondInfo + "######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.moveObject(plugId, fromUuid, toUuid, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress)
	{
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String moveToFreeAddressInfo = (moveToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE moveAddress " + moveToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.moveAddress(plugId, fromUuid, toUuid, moveToFreeAddress, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		System.out.println("\n###### INVOKE copyObject " + copySubtreeInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.copyObject(plugId, fromUuid, toUuid, copySubtree, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
			System.out.println("Root Copy: " + result);
		} else {
			handleError(response);
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

			String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
			String copyToFreeAddressInfo = (copyToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
			System.out.println("\n###### INVOKE copyAddress " + copySubtreeInfo + copyToFreeAddressInfo + " ######");
			startTime = System.currentTimeMillis();
			response = mdekCallerAddress.copyAddress(plugId, fromUuid, toUuid, copySubtree, copyToFreeAddress, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
				System.out.println("Copy Node (rudimentary): ");
				debugAddressDoc(result);
			} else {
				handleError(response);
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

	public IngridDocument deleteAddressWorkingCopy(String uuid,
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

	public IngridDocument deleteObject(String uuid,
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

	public IngridDocument deleteAddress(String uuid,
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
		} else {
			handleError(response);
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
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugIdcUsersDoc(result);
		} else {
			handleError(response);
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
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.searchAddresses(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + l.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument a : l) {
				debugAddressDoc(a);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return result;
	}

	public List<IngridDocument> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchTerm:" + searchTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsFullText(plugId, searchTerm, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsExtended ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsExtended(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesFullText(String queryTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- queryTerm:" + queryTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesFullText(plugId, queryTerm, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAdressesExtended ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesExtended(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}	
	
	public void queryHQL(String qString,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryHQL ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- query:" + qString);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryHQL(plugId, qString, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
			IdcEntityType type = IdcEntityType.OBJECT;
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
				type = IdcEntityType.ADDRESS;				
			}
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				if (IdcEntityType.OBJECT.equals(type)) {
					debugObjectDoc(hit);
				} else {
					debugAddressDoc(hit);
				}
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}
	}

	public void queryHQLToCsv(String qString) {
		try {
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			System.out.println("\n###### INVOKE queryHQLToCsv ######");
			System.out.println("- query:" + qString);
			startTime = System.currentTimeMillis();
			response = mdekCallerQuery.queryHQLToCsv(plugId, qString, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				Long totalNumHits = (Long) result.get(MdekKeys.SEARCH_TOTAL_NUM_HITS);
				System.out.println("SUCCESS: " + totalNumHits + " csvLines returned (and additional title-line)");
				String csvResult = result.getString(MdekKeys.CSV_RESULT);			
//				if (doFullOutput) {
//					System.out.println(csvResult);
//				} else {
					if (csvResult.length() > 5000) {
						int endIndex = csvResult.indexOf("\n", 3000);
						System.out.print(csvResult.substring(0, endIndex));					
						System.out.println("...");					
					} else {
						System.out.println(csvResult);					
					}
//				}

			} else {
				handleError(response);
			}			
		} catch (Throwable t) {
			System.out.println("\nCatched Throwable in Example:");
			printThrowable(t);
		}
	}

	public void trackRunningJob(int sleepTimeMillis, boolean doCancel) {
		IngridDocument response;
		IngridDocument result;
		System.out.println("\n###### INVOKE getRunningJobInfo ######");

		boolean jobIsRunning = true;
		int counter = 0;
		while (jobIsRunning) {
			if (doCancel && counter > 4) {
				cancelRunningJob();
				return;
			}

			response = mdekCaller.getRunningJobInfo(plugId, myUserUuid);
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
				Integer numObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
				Integer total = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);
				if (jobDescr == null) {
					// job finished !
					jobIsRunning = false;					
					System.out.println("JOB FINISHED\n");
				} else {
					System.out.println("job:" + jobDescr + ", entities:" + numObjs + ", total:" + total);
				}
			} else {
				handleError(response);
				jobIsRunning = false;
			}
			
			try {
				Thread.sleep(sleepTimeMillis);				
			} catch(Exception ex) {
				System.out.println(ex);
			}
			counter++;
		}
	}

	public void cancelRunningJob() {
		System.out.println("\n###### INVOKE cancelRunningJob ######");

		IngridDocument response = mdekCaller.cancelRunningJob(plugId, myUserUuid);
		IngridDocument result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
			if (jobDescr == null) {
				System.out.println("JOB FINISHED\n");
			} else {
				System.out.println("JOB CANCELED: " + result);
			}
		} else {
			handleError(response);
		}
	}

	public String extractModUserData(IngridDocument inDoc) {
		if (inDoc == null) {
			return null; 
		}

		String user = inDoc.getString(MdekKeys.UUID);
		if (inDoc.get(MdekKeys.NAME) != null) {
			user += " " + inDoc.get(MdekKeys.NAME);
		}
		if (inDoc.get(MdekKeys.GIVEN_NAME) != null) {
			user += " " + inDoc.get(MdekKeys.GIVEN_NAME);
		}
		if (inDoc.get(MdekKeys.ORGANISATION) != null) {
			user += " " + inDoc.get(MdekKeys.ORGANISATION);
		}
		
		return user;
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
			+ ", modUuid: " + extractModUserData((IngridDocument)u.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + u);

		debugPermissionsDoc(u, "  ");
	}
	
	private void debugGroupDoc(IngridDocument g) {
		System.out.println("Group: " + g.get(MdekKeysSecurity.IDC_GROUP_ID) 
			+ ", " + g.get(MdekKeys.NAME)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + extractModUserData((IngridDocument)g.get(MdekKeys.MOD_USER))
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

	private void debugPermissionsDoc(IngridDocument p, String indent) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println(indent + "Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println(indent + "  " + doc);								
			}			
		}
	}
	
	public void debugPermissionsDocBoolean(IngridDocument p) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		System.out.println("HAS_WRITE_ACCESS: " + MdekUtilsSecurity.hasWritePermission(docList));
		System.out.println("HAS_WRITE_TREE_ACCESS: " + MdekUtilsSecurity.hasWriteTreePermission(docList));
		System.out.println("HAS_WRITE_SINGLE_ACCESS: " + MdekUtilsSecurity.hasWriteSinglePermission(docList));
	}
	
	private void debugIdcUsersDoc(IngridDocument u) {
		List<IngridDocument> docList = (List<IngridDocument>) u.get(MdekKeysSecurity.IDC_USERS);
		if (docList != null && docList.size() > 0) {
			System.out.println("Users: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}
	
	private void debugObjectDoc(IngridDocument o) {
		System.out.println("Object: " + o.get(MdekKeys.ID) 
			+ ", " + o.get(MdekKeys.UUID)
			+ ", " + o.get(MdekKeys.TITLE)
		);
		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.get(MdekKeys.PUBLICATION_CONDITION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + extractModUserData((IngridDocument)o.get(MdekKeys.MOD_USER))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + o);

		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			for (IngridDocument a : docList) {
				System.out.println("   " + a.get(MdekKeys.UUID) + ": " + a);								
				List<IngridDocument> coms = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
				if (coms != null) {
					System.out.println("    Communication: " + coms.size() + " Entities");
					for (IngridDocument c : coms) {
						System.out.println("     " + c);
					}					
				}
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LOCATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LINKAGES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) o.get(MdekKeys.EXPORTS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.LEGISLATIONS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATA_FORMATS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) o.get(MdekKeys.ENV_CATEGORIES);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Categories: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.ENV_TOPICS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) o.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null && intList.size() > 0) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (myDoc != null) {
			System.out.println("  technical domain MAP:");
			System.out.println("    " + myDoc);								
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.KEY_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - key catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - publication scales: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SYMBOL_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - symbol catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			strList = (List<String>) myDoc.get(MdekKeys.FEATURE_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - feature types: " + strList.size() + " entries");
				for (String str : strList) {
					System.out.println("     " + str);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.GEO_VECTOR_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - vector formats, geo vector list: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			intList = (List<Integer>) myDoc.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - spatial rep types: " + intList.size() + " entries");
				for (Integer i : intList) {
					System.out.println("     " + i);								
				}			
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (myDoc != null) {
			System.out.println("  technical domain DOCUMENT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (myDoc != null) {
			System.out.println("  technical domain SERVICE:");
			System.out.println("    " + myDoc);								
			strList = (List<String>) myDoc.get(MdekKeys.SERVICE_VERSION_LIST);
			if (strList != null && strList.size() > 0) {
				System.out.println("    SERVICE - versions: " + strList.size() + " entries");
				System.out.println("     " + strList);
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_OPERATION_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - operations: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
					strList = (List<String>) doc.get(MdekKeys.PLATFORM_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - platforms: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.DEPENDS_ON_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - dependsOns: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.CONNECT_POINT_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - connectPoints: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					List<IngridDocument> docList2 = (List<IngridDocument>) doc.get(MdekKeys.PARAMETER_LIST);
					if (docList2 != null) {
						System.out.println("      SERVICE - operation - parameters: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
					}
				}
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (myDoc != null) {
			System.out.println("  technical domain PROJECT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (myDoc != null) {
			System.out.println("  technical domain DATASET:");
			System.out.println("    " + myDoc);								
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		myDoc = (IngridDocument) o.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
	}

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.TITLE_OR_FUNCTION_KEY)			
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
		);
		System.out.println("         "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + extractModUserData((IngridDocument)a.get(MdekKeys.MOD_USER))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + a);

		IngridDocument myDoc;
		List<IngridDocument> docList;
		List<String> strList;

		docList = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Communication: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		myDoc = (IngridDocument) a.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
		strList = (List<String>) a.get(MdekKeys.PATH);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Path: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.PATH_ORGANISATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Path Organisations: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}			
		}
	}

	private void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			

		if (!doFullOutput) {
			return;
		}

		// detailed output  
		List<MdekError> errors = mdekCaller.getErrorsFromResponse(response);
		doFullOutput = false;
		for (MdekError err : errors) {
			IngridDocument info = err.getErrorInfo();
			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				// referenced object
				debugObjectDoc(info);
				// objects referencing
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}
			} else if (err.getErrorType().equals(MdekErrorType.GROUP_HAS_USERS)) {
				debugIdcUsersDoc(info);
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT)) {
				System.out.println("    Object with multiple Permissions: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS)) {
				System.out.println("    Address with multiple Permissions: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with TREE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with TREE Permission: " + addrs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with SINGLE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with SINGLE Permission: " + addrs.get(1));
			}
		}
		doFullOutput = true;
	}

	private void printThrowable(Throwable t) {
		System.out.println(t);
		System.out.println("   Stack Trace:");
		StackTraceElement[] st = t.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	        System.out.println(stackTraceElement);
        }
		Throwable cause = t.getCause();
		if (cause != null) {
			System.out.println("   Cause:");
			printThrowable(cause);			
		}
	}
}
