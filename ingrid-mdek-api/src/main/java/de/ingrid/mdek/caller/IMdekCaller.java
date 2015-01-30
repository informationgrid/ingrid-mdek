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
package de.ingrid.mdek.caller;

import java.util.List;

import de.ingrid.mdek.MdekError;
import de.ingrid.utils.IngridDocument;

/**
 * Interface for all mdek job callers implementing common methods and data types.
 */
public interface IMdekCaller {

	/** How much data to fetch from requested entity ? */
	public enum FetchQuantity {
		/** request all data to be exported */
		EXPORT_ENTITY,
		/** request all data to be displayed in IGE */
		EDITOR_ENTITY,
	}

	/** Helper enum when selected address node is virtual (e.g. virtual Root Node of Free Addresses) to
	 * determine which addresses to process */
	public enum AddressArea {
		/** includes free and non-free addresses */
		ALL_ADDRESSES,
		ALL_FREE_ADDRESSES,
		ALL_NON_FREE_ADDRESSES,
	}

	/**
	 * Returns info about the version of the MdekServer (iPlug).
	 * @param plugId which mdek server (iplug)
	 * @return response containing result: map containing version info
	 */
	public IngridDocument getVersion(String plugId);

	/**
	 * Returns a list of mdek plug ids registered at the mdek bus.
	 */
	public List<String> getRegisteredIPlugs();

	/**
	 * Returns "generic" information about currently running job of passed user.
	 * @param plugId which mdek server (iplug)
	 * @param userId user identifier
	 * @return response containing result: map containing job infos or empty map if no job running !
	 */
	IngridDocument getRunningJobInfo(String plugId, String userId);

	/**
	 * Cancel the currently running job of passed user.
	 * @param plugId which mdek server (iplug)
	 * @param userId user identifier
	 * @return response containing result: map containing infos about canceled job, or empty map if no job running
	 * (or null if something went wrong)<br>
	 */
	IngridDocument cancelRunningJob(String plugId, String userId);

	/**
	 * Get pure requested result data from response of mdek call (without "protocol" overhead in response).
	 * @param mdekResponse response from mdek call
	 * @return null if errors occured otherwise IngridDocument containing results
	 */
	IngridDocument getResultFromResponse(IngridDocument mdekResponse);

	/**
	 * Get "Global" Error Message from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return global error message, should never be null and
	 * contains exception string when unknown error occured
	 */
	String getErrorMsgFromResponse(IngridDocument mdekResponse);

	/**
	 * Get Detailed Mdek Errors from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return list of detected errors, may be null when unknown error occured
	 * -> then <code>getErrorMsgFromResponse</code> contains exception
	 */
	List<MdekError> getErrorsFromResponse(IngridDocument mdekResponse);
}
