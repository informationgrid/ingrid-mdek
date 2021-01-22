/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.IJob.JobType;
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
			Set<Map.Entry<Object, Object>> entries = result.entrySet();
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
			String[] sysValues = (String[]) result.get(MdekKeys.SYS_GENERIC_KEY_VALUES);
			System.out.println("  SYS_GENERIC_KEY_VALUES[0] (= IGC Schema version): " + sysValues[0]);

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

		System.out.println("\n###### INVOKE storeCatalog ######");
		System.out.println("- refetch: " + refetchCatalog);
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

		System.out.println("\n###### INVOKE getSysLists ######");
		supertoolGeneric.debugArray(listIds, "- listIds: ");
		System.out.println("- language: " + language);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysLists(plugId, listIds, language, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			if (listIds != null) {
				Set<Object> listKeys = result.keySet();
				System.out.println("SUCCESS: " + listKeys.size() + " sys-lists");
				for (Object myListKey : listKeys) {
				    String listKey = (String) myListKey;
					IngridDocument syslstDoc = (IngridDocument) result.get(listKey);
					Integer[] entryIds = (Integer[]) syslstDoc.get(MdekKeys.LST_ENTRY_IDS);
					Integer lstDefaultIndex = (Integer) syslstDoc.get(MdekKeys.LST_DEFAULT_ENTRY_INDEX);
					int defaultIndex = (lstDefaultIndex==null) ? -1 : lstDefaultIndex;
					String[] entryNames = (String[]) syslstDoc.get(MdekKeys.LST_ENTRY_NAMES);
					System.out.println("  " + listKey + ": " + entryIds.length + " entries");
					for (int i=0; i<entryIds.length; i++) {
						System.out.println("    id:" + entryIds[i] + ", default:" + (i==defaultIndex) +
							((entryNames != null) ? (", name:" + entryNames[i]) : "") );
					}
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

	public IngridDocument storeSysList(int listId, boolean maintainable, Integer defaultEntryIndex,
			Integer[] entryIds, String[] entryNames_de, String[] entryNames_en, String[] data) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeSysList ######");
		System.out.println("- listId: " + listId);
		System.out.println("- maintainable: " + maintainable);
		System.out.println("- index of defaultEntry: " + defaultEntryIndex);
		supertoolGeneric.debugArray(entryIds, "- entryIds: ");
		supertoolGeneric.debugArray(entryNames_de, "- entryNames_de: ");
		supertoolGeneric.debugArray(entryNames_en, "- entryNames_en: ");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeSysList(plugId,
				listId, maintainable, defaultEntryIndex,
				entryIds, entryNames_de, entryNames_en,
				data, myUserUuid);
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

	public IngridDocument storeSysLists(List<IngridDocument> listDocs) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE storeSysLists ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeSysLists(plugId, listDocs, -1L, myUserUuid);
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

	/** Pass null if all generic keys requested */
	public IngridDocument getSysGenericKeys(String[] keyNames) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysGenericKeys ######");
		supertoolGeneric.debugArray(keyNames, "- requested keyNames: ");
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
		supertoolGeneric.debugArray(keyNames, "- keyNames: ");
		supertoolGeneric.debugArray(keyValues, "- keyValues: ");
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

	public IngridDocument exportObjectBranch(String rootUuid,
			boolean exportOnlyRoot,
			boolean includeWorkingCopies) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportObjectBranch ######");
		System.out.println("- top node of branch: " + rootUuid);
		System.out.println("- export only top node: " + exportOnlyRoot);
		System.out.println("- include working copies: " + includeWorkingCopies);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjectBranch(plugId, rootUuid,
				exportOnlyRoot,
				includeWorkingCopies,
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

	public IngridDocument exportObjects(String exportCriteria,
			boolean includeWorkingCopies) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportObjects ######");
		System.out.println("- export tag: " + exportCriteria);
		System.out.println("- include working copies: " + includeWorkingCopies);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportObjects(plugId, exportCriteria, includeWorkingCopies, myUserUuid);
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
			AddressArea addressArea,
			boolean includeWorkingCopies) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE exportAddressBranch ######");
		System.out.println("- top node of branch: " + rootUuid);
		System.out.println("- export only top node: " + exportOnlyRoot);
		System.out.println("- addressArea (if top node NULL): " + addressArea);
		System.out.println("- include working copies: " + includeWorkingCopies);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.exportAddressBranch(plugId, rootUuid,
				exportOnlyRoot, addressArea,
				includeWorkingCopies,
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

	/** Calls analyzeImportData + importEntities for every passed file (importDataList)
	 * and returns result of import of last file.
	 */
	public IngridDocument importEntities(List<byte[]> importDataList,
			String objectImportNodeUuid, String addressImportNodeUuid,
			boolean publishImmediately,
			boolean doSeparateImport, boolean copyNodeIfPresent,
			String frontendMappingProtocol) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result = null;

        startTime = System.currentTimeMillis();

        System.out.println("\n###### START IMPORT: Loop of analyzeImportData + importEntities for every file ! ######");
        System.out.println("- multiple import files ?: " + (importDataList.size() > 1));

        boolean startNewAnalysis = true;

        for (int i = 0; i < importDataList.size(); i++) {
            if (i > 0) {
                // multiple files, we keep existing Analysis/Import Info !
                startNewAnalysis = false;                
            }

            System.out.println("\n###### INVOKE analyzeImportData ######");
            System.out.println("- object import node: " + objectImportNodeUuid);
            System.out.println("- address import node: " + addressImportNodeUuid);
            System.out.println("- publish immediately: " + publishImmediately);
            System.out.println("- doSeparateImport: " + doSeparateImport);
            System.out.println("- copyNodeIfPresent: " + copyNodeIfPresent);
            System.out.println("- frontendMappingProtocol included ?: " + (frontendMappingProtocol != null && !frontendMappingProtocol.isEmpty()));
            System.out.println("- startNewAnalysis: " + startNewAnalysis);
            response = mdekCallerCatalog.analyzeImportData(plugId, importDataList.get(i),
                    objectImportNodeUuid, addressImportNodeUuid,
                    publishImmediately, doSeparateImport, copyNodeIfPresent,
                    frontendMappingProtocol, startNewAnalysis,
                    myUserUuid);
            
            System.out.println("\n###### INVOKE importEntities ######");
            System.out.println("- object import node: " + objectImportNodeUuid);
            System.out.println("- address import node: " + addressImportNodeUuid);
            System.out.println("- publish immediately: " + publishImmediately);
            System.out.println("- doSeparateImport: " + doSeparateImport);
            System.out.println("- copyNodeIfPresent: " + copyNodeIfPresent);
            System.out.println("- frontendMappingProtocol included ?: " + (frontendMappingProtocol != null && !frontendMappingProtocol.isEmpty()));
            startTime = System.currentTimeMillis();
            response = mdekCallerCatalog.importEntities(plugId,
                    objectImportNodeUuid, addressImportNodeUuid,
                    publishImmediately, doSeparateImport, copyNodeIfPresent,
                    frontendMappingProtocol,
                    myUserUuid);
            result = mdekCallerCatalog.getResultFromResponse(response);
            if (result != null) {
                System.out.println("SUCCESS: ");
                supertoolGeneric.debugJobInfoDoc(result);
            } else {
                supertoolGeneric.handleError(response);
            }
        }
		
        endTime = System.currentTimeMillis();
        neededTime = endTime - startTime;
        System.out.println("EXECUTION TIME: " + neededTime + " ms");

		return result;
	}

	public IngridDocument getJobInfo(JobType jobType) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getJobInfo ######");
		System.out.println("- jobType: " + jobType);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getJobInfo(plugId, jobType, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result, jobType);
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

	public IngridDocument getObjectsOfAddressbyType(String addressUuid, Integer referenceTypeId, Integer maxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectsOfAddressbyType ######");
		System.out.println("- addressUuid: " + addressUuid);
    System.out.println("- referenceTypeId: " + referenceTypeId);
		System.out.println("- maxNum: " + maxNum);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getObjectsOfAddressByType(plugId, addressUuid, referenceTypeId, maxNum,
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

	public IngridDocument getFreeListEntries(MdekSysList sysLst) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getFreeListEntries ######");
		System.out.println("- syslist: " + sysLst);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getFreeListEntries(plugId, sysLst, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			String[] entryNames = (String[]) result.get(MdekKeys.LST_FREE_ENTRY_NAMES);
			System.out.println("SUCCESS: " + entryNames.length + " free entries");
			for (String entryName : entryNames) {
				System.out.println("  " + entryName);
			}
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument replaceFreeEntryWithSyslistEntry(String freeEntry,
			MdekSysList sysLst, int sysLstEntryId, String sysLstEntryName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE replaceFreeEntryWithSyslistEntry ######");
		System.out.println("- freeEntry: " + freeEntry);
		System.out.println("- syslist: " + sysLst);
		System.out.println("- sysLstEntryId: " + sysLstEntryId);
		System.out.println("- sysLstEntryName: " + sysLstEntryName);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.replaceFreeEntryWithSyslistEntry(plugId, freeEntry,
				sysLst, sysLstEntryId, sysLstEntryName, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			Integer numReplaced = (Integer) result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES);
			System.out.println("SUCCESS: " + numReplaced + " free entries replaced with syslist entry");
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument rebuildSyslistData() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE rebuildSyslistData ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.rebuildSyslistData(plugId, myUserUuid);
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

	public IngridDocument getSearchTerms(SearchtermType[] termTypes) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSearchTerms ######");
		supertoolGeneric.debugArray(termTypes, "- termTypes: ");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSearchTerms(plugId, termTypes, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> termDocs = (List<IngridDocument>) result.get(MdekKeys.SUBJECT_TERMS);
			System.out.println("SUCCESS: " + termDocs.size() + " searchterms");
			int cnt = 0;
			for (IngridDocument termDoc : termDocs) {
				System.out.println("  " + termDoc);
				cnt++;
				if (cnt > 4) break;
			}
			System.out.println("  ...");
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument getSpatialReferences(SpatialReferenceType[] refTypes) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSpatialReferences ######");
		supertoolGeneric.debugArray(refTypes, "- spatialRefTypes: ");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSpatialReferences(plugId, refTypes, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> refDocs = (List<IngridDocument>) result.get(MdekKeys.LOCATIONS);
			System.out.println("SUCCESS: " + refDocs.size() + " spatial references");
			int cnt = 0;
			for (IngridDocument refDoc : refDocs) {
				System.out.println("  " + refDoc);
				cnt++;
				if (cnt > 4) break;
			}
			System.out.println("  ...");
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument updateSearchTerms(List<IngridDocument> oldTerms,
			List<IngridDocument> newTerms) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateSearchTerms ######");
		System.out.println("- num oldTerms: " + oldTerms.size());
		System.out.println("- num newTerms: " + newTerms.size());
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.updateSearchTerms(plugId, oldTerms, newTerms, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result, JobType.UPDATE_SEARCHTERMS);
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}

	public IngridDocument updateSpatialReferences(List<IngridDocument> oldSpatialRefs,
			List<IngridDocument> newSpatialRefs) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateSpatialReferences ######");
		System.out.println("- num oldSpatialRefs: " + oldSpatialRefs.size());
		System.out.println("- num newSpatialRefs: " + newSpatialRefs.size());
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.updateSpatialReferences(plugId, oldSpatialRefs, newSpatialRefs, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerCatalog.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			supertoolGeneric.debugJobInfoDoc(result, JobType.UPDATE_SPATIAL_REFERENCES);
			
		} else {
			supertoolGeneric.handleError(response);
		}
		
		return result;
	}
}
