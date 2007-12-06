package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller {

	IngridDocument testMdekEntity(int threadNumber);

	IngridDocument fetchObjDetails(String objUuid);
	IngridDocument fetchObjAddresses(String objUuid);

	IngridDocument fetchTopObjects();
	IngridDocument fetchSubObjects(String objUuid);

	IngridDocument fetchTopAddresses();
	IngridDocument fetchSubAddresses(String adrUuid);

	/**
	 * Get result data from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return null if errors occured otherwise IngridDocument containing results
	 */
	IngridDocument getResultFromResponse(IngridDocument mdekResponse);

	/**
	 * Get Error Message from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return null if no error message set in response
	 */
	String getErrorMsgFromResponse(IngridDocument mdekResponse);
}
