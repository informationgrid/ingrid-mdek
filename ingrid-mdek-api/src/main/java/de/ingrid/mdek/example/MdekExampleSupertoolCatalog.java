package de.ingrid.mdek.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates Catalog example methods ...
 */
public class MdekExampleSupertoolCatalog {

	private MdekExampleSupertool supertoolGeneric;
	private IMdekCallerCatalog mdekCallerCatalog;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertoolCatalog(String plugIdToCall,
			String callingUserUuid,
			MdekExampleSupertool supertoolGeneric)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;
		this.supertoolGeneric = supertoolGeneric;

		// and our specific job caller !
		MdekCallerCatalog.initialize(MdekClientCaller.getInstance());
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
	}

	public void setPlugIdToCall(String plugIdToCall)
	{
		this.plugId = plugIdToCall;
	}

	public void setCallingUser(String callingUserUuid)
	{
		this.myUserUuid = callingUserUuid;
		System.out.println("\n###### NEW CALLING USER = " + callingUserUuid + " ######");		
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
		response = mdekCallerCatalog.getVersion(plugId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");

		result = mdekCallerCatalog.getResultFromResponse(response);
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
			supertoolGeneric.handleError(response);
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
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugCatalogDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeCatalog(IngridDocument catDocIn,
			boolean refetchCatalog) {
		// check whether we have an address
		if (catDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetchCatalog) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeCatalog " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeCatalog(plugId, catDocIn, refetchCatalog, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugCatalogDoc(result);
			
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerCatalog.getResultFromResponse(response);
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
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	/** Pass null if all gui elements requested */
	public IngridDocument getSysGuis(String[] guiIds) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysGuis ######");
		System.out.println("requested guiIds: " + guiIds);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysGuis(plugId, guiIds, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			Set entries = result.entrySet();
			System.out.println("SUCCESS: " + entries.size() + " gui elements");
			for (Object entry : entries) {
				System.out.println("  " + entry);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeSysGuis(List<IngridDocument> sysGuis,
			boolean refetch) {
		// check whether we have data
		if (sysGuis == null || sysGuis.size() == 0) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeSysGuis " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeSysGuis(plugId, sysGuis, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);

		if (result != null) {
			if (refetch) {
				Set entries = result.entrySet();
				System.out.println("SUCCESS: " + entries.size() + " gui elements");
				for (Object entry : entries) {
					System.out.println("  " + entry);
				}
			} else {
				System.out.println("SUCCESS: ");				
			}
		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}

	public IngridDocument exportObjectBranch(String rootUuid, boolean exportOnlyRoot) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportObjectBranch ######");
		System.out.println("- top node of branch:" + rootUuid);
		System.out.println("- export only top node:" + exportOnlyRoot);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjectBranch(plugId, rootUuid, exportOnlyRoot,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument exportObjects(String exportCriteria) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportObjects ######");
		System.out.println("- export tag:" + exportCriteria);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjects(plugId, exportCriteria, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument exportAddressBranch(String rootUuid, boolean exportOnlyRoot) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportAddressBranch ######");
		System.out.println("- top node of branch:" + rootUuid);
		System.out.println("- export only top node:" + exportOnlyRoot);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportAddressBranch(plugId, rootUuid, exportOnlyRoot,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
}
