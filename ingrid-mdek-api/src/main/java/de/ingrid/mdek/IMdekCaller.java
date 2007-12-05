package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller {

	IngridDocument testMdekEntity(int threadNumber);

	IngridDocument fetchTopObjects();
	IngridDocument fetchTopAddresses();
	IngridDocument fetchSubObjects(String objUuid);
	IngridDocument fetchSubAddresses(String adrUuid);
	IngridDocument fetchObjAddresses(String objUuid);

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
