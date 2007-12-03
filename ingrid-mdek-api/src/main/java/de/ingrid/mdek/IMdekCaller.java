package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller {

	IngridDocument testMdekEntity(int threadNumber);

	IngridDocument fetchSubObjects(String objUuid);
	IngridDocument fetchObjAddresses(String objUuid);
}
