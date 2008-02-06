package de.ingrid.mdek;

import java.util.List;

import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.utils.IngridDocument;


/**
 * Defines common stuff used by Object and Address MdekCaller Interfaces.
 */
public interface IMdekCallerCommon {

	/** How much data to fetch from requested entity ? */
	// TODO implement other quantities of fetching object ?
	public enum Quantity {
		DETAIL_ENTITY // client: edit dialogue -> request maximum data
	}

//	IngridDocument testMdekEntity(int threadNumber);

	/** Returns a map containing the entries of lists with given ids.
	 * Pass null as languageCode if it doesn't matter. */
	IngridDocument getSysLists(Integer[] listIds, Integer languageCode,
			String userId);

	/**
	 * Fetch The catalog object, represented by an CATALOG_MAP type in xsd.
	 * @return response containing result: map representation of the catalog object
	 */
	IngridDocument fetchCatalog(String userId);

	/**
	 * Returns information about currently running job of passed user.
	 * @param userId user identifier
	 * @return response containing result: map containing job infos or empty map if no job running !
	 */
	IngridDocument getRunningJobInfo(String userId);

	/**
	 * Cancel the currently running job of passed user.
	 * @param userId user identifier
	 * @return response containing result: map containing infos about canceled job, or empty map if no job running
	 * (or null if something went wrong)<br>
	 */
	IngridDocument cancelRunningJob(String userId);

	// uncomment when needed ! then recheck functionality and implementation !
//	IngridDocument fetchTopAddresses();
//	IngridDocument fetchSubAddresses(String adrUuid);

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
