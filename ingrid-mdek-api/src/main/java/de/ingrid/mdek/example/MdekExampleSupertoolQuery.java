/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates Query example methods ...
 */
public class MdekExampleSupertoolQuery {

	private MdekExampleSupertool supertoolGeneric;
	private IMdekCallerQuery mdekCallerQuery;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertoolQuery(String plugIdToCall,
			String callingUserUuid,
			MdekExampleSupertool supertoolGeneric)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;
		this.supertoolGeneric = supertoolGeneric;

		// and our specific job caller !
		MdekCallerQuery.initialize(MdekClientCaller.getInstance());
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

	public void setFullOutput(boolean doFullOutput)
	{
		this.doFullOutput = doFullOutput;
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				supertoolGeneric.debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
		result = mdekCallerQuery.getResultFromResponse(response);
		if (result != null) {
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			IdcEntityType type = IdcEntityType.OBJECT;
			List<IngridDocument> hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
				type = IdcEntityType.ADDRESS;				
			}
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				if (IdcEntityType.OBJECT.equals(type)) {
					supertoolGeneric.debugObjectDoc(hit);
				} else {
					supertoolGeneric.debugAddressDoc(hit);
				}
			}
			doFullOutput = true;
		} else {
			supertoolGeneric.handleError(response);
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
			result = mdekCallerQuery.getResultFromResponse(response);
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

//					if (doFullOutput) {
//						System.out.println(csvResult);
//					} else {
						if (csvResult.length() > 5000) {
							int endIndex = csvResult.indexOf("\n", 3000);
							System.out.print(csvResult.substring(0, endIndex));					
							System.out.println("...");					
						} else {
							System.out.println(csvResult);					
						}
//					}
				}

			} else {
				supertoolGeneric.handleError(response);
			}			
		} catch (Throwable t) {
			System.out.println("\nCatched Throwable in Example queryHQLToCsv:");
			supertoolGeneric.printThrowable(t);
		}
	}

	public IngridDocument queryHQLToMap(String qString, Integer maxNumHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryHQLToMap ######");
		System.out.println("- query:" + qString);
		System.out.println("- maxNumHits:" + maxNumHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryHQLToMap(plugId, qString, maxNumHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCallerQuery.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			}
			Long numHits = (Long) result.get(MdekKeys.TOTAL_NUM);
			if (numHits != hits.size()) {
				throw new MdekException(
					"Returned listsize of entities (" +	hits.size() + ") != returned numHits (" + numHits + "");
			}
			System.out.println("SUCCESS: " + numHits + " Entities");
			for (IngridDocument hit : hits) {
				System.out.println("  " + hit);
			}

		} else {
			supertoolGeneric.handleError(response);
		}

		return result;
	}
}
