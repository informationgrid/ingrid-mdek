package de.ingrid.mdek.example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
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
			if (listIds != null) {
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
				// all syslist IDs
				System.out.println("SUCCESS: ");
				System.out.println(result);
			}
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getSysAdditionalFields(Long[] fieldIds, String languageCode) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysAdditionalFields ######");
		System.out.println("- requested fieldIds: " + fieldIds);
		System.out.println("- requested language code: " + languageCode);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysAdditionalFields(plugId, fieldIds, languageCode, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			Set<String> fieldsKeys = result.keySet();
			System.out.println("SUCCESS: " + fieldsKeys.size() + " additional fields");
			for (String fieldsKey : fieldsKeys) {
				IngridDocument fieldDoc = (IngridDocument) result.get(fieldsKey);
				System.out.println("  " + fieldsKey + ": "+ fieldDoc);
				Set<String> keysInField = fieldDoc.keySet();
				for (String keyInField : keysInField) {
					if (keyInField.startsWith(MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX)) {
						String langCode = keyInField.substring(MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX.length());
						String[] items = (String[]) fieldDoc.get(keyInField);
						System.out.print("    items '" + langCode + "': ");
						for (String item : items) {
							System.out.print(item + ", ");
						}
						System.out.println();
					}
				}
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
		System.out.println("- requested guiIds: " + guiIds);
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

	/** Pass null if all generic keys requested */
	public IngridDocument getSysGenericKeys(String[] keyNames) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysGenericKeys ######");
		System.out.println("- requested keyNames: " + keyNames);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysGenericKeys(plugId, keyNames, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			Set entries = result.entrySet();
			System.out.println("SUCCESS: " + entries.size() + " generic keys");
			for (Object entry : entries) {
				System.out.println("  " + entry);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeSysGenericKeys(String[] keyNames, String[] keyValues) {
		// check whether we have data
		if (keyNames == null || keyNames.length == 0) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeSysGenericKeys ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeSysGenericKeys(plugId, keyNames, keyValues, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);

		if (result != null) {
			Set entries = result.entrySet();
			System.out.println("SUCCESS: " + entries.size() + " generic keys");
			for (Object entry : entries) {
				System.out.println("  " + entry);
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
		System.out.println("- top node of branch: " + rootUuid);
		System.out.println("- export only top node: " + exportOnlyRoot);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjectBranch(plugId, rootUuid, exportOnlyRoot,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
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
		System.out.println("- export tag: " + exportCriteria);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjects(plugId, exportCriteria, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument exportAddressBranch(String rootUuid,
			boolean exportOnlyRoot,
			AddressArea addressArea) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportAddressBranch ######");
		System.out.println("- top node of branch: " + rootUuid);
		System.out.println("- export only top node: " + exportOnlyRoot);
		System.out.println("- addressArea (if top node NULL): " + addressArea);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportAddressBranch(plugId, rootUuid,
				exportOnlyRoot, addressArea,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getExportInfo(boolean includeExportData) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getExportInfo ######");
		System.out.println("- includeExportData: " + includeExportData);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getExportInfo(plugId, includeExportData, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS:");
			supertoolGeneric.debugJobInfoDoc(result);

			byte[] exportResultZipped = (byte[]) result.get(MdekKeys.EXPORT_RESULT);
			if (exportResultZipped != null) {
				System.out.println("- size zipped XML=" + (exportResultZipped.length / 1024) + " KB");

				if (doFullOutput) {
					String exportResultUnzipped = "";
					try {
						exportResultUnzipped = MdekUtils.decompressZippedByteArray(exportResultZipped);
					} catch(Exception ex) {
						System.out.println(ex);
					}
					System.out.println("XML:\n" + exportResultUnzipped);				
				}
			}

		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument importEntities(byte[] importData,
			String objectImportNodeUuid, String addressImportNodeUuid,
			boolean publishImmediately, boolean doSeparateImport) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE importEntities ######");
		System.out.println("- object import node: " + objectImportNodeUuid);
		System.out.println("- address import node: " + addressImportNodeUuid);
		System.out.println("- publish immediately: " + publishImmediately);
		System.out.println("- doSeparateImport: " + doSeparateImport);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.importEntities(plugId, importData,
				objectImportNodeUuid, addressImportNodeUuid,
				publishImmediately, doSeparateImport,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getImportInfo() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getImportInfo ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getImportInfo(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument analyze() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE analyze ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.analyze(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result);
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectsOfAuskunftAddress(String auskunftAddressUuid, Integer maxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectsOfAuskunftAddress ######");
		System.out.println("- auskunftAddressUuid: " + auskunftAddressUuid);
		System.out.println("- maxNum: " + maxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getObjectsOfAuskunftAddress(plugId, auskunftAddressUuid, maxNum,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
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

	public IngridDocument getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectsOfResponsibleUser ######");
		System.out.println("- responsibleUserUuid: " + responsibleUserUuid);
		System.out.println("- maxNum: " + maxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getObjectsOfResponsibleUser(plugId, responsibleUserUuid, maxNum,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
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

	public IngridDocument getAddressesOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressesOfResponsibleUser ######");
		System.out.println("- responsibleUserUuid: " + responsibleUserUuid);
		System.out.println("- maxNum: " + maxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getAddressesOfResponsibleUser(plugId, responsibleUserUuid, maxNum,
				myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);
			}
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument replaceAddress(String oldUuid, String newUuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE replaceAddress ######");
		System.out.println("- oldUuid: " + oldUuid);
		System.out.println("- newUuid: " + newUuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.replaceAddress(plugId, oldUuid, newUuid,
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

	public void getCsvData(MdekUtils.CsvRequestType csvType, String uuid) {
		try {
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			System.out.println("\n###### INVOKE getCsvData ######");
			System.out.println("- csvType:" + csvType);
			System.out.println("- uuid:" + uuid);
			startTime = System.currentTimeMillis();
			response = mdekCallerCatalog.getCsvData(plugId, csvType, uuid, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCallerCatalog.getResultFromResponse(response);
			if (result != null) {
				Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM);
				System.out.println("SUCCESS: " + totalNumHits + " csvLines returned (and additional title-line)");

				byte[] csvResultZipped = (byte[]) result.get(MdekKeys.CSV_RESULT);
				if (csvResultZipped != null) {
					System.out.println("- size zipped XML=" + (csvResultZipped.length / 1024) + " KB");
					String csvResult = "";
					try {
						csvResult = MdekUtils.decompressZippedByteArray(csvResultZipped);
					} catch(Exception ex) {
						System.out.println(ex);
					}
					if (csvResult.length() > 3000) {
						int endIndex = csvResult.indexOf("\n", 3000);
						System.out.print(csvResult.substring(0, endIndex));					
						System.out.println("...");					
					} else {
						System.out.println(csvResult);					
					}
				}
			} else {
				supertoolGeneric.handleError(response);
			}			
		} catch (Throwable t) {
			supertoolGeneric.printThrowable(t);
		}
	}
}
