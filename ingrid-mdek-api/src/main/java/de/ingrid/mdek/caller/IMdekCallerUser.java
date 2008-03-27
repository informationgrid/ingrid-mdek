package de.ingrid.mdek.caller;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning USER MANAGEMENT (permissions etc.).
 */
public interface IMdekCallerUser {

	/**
	 * Get all groups.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing groups
	 */
	IngridDocument getGroups(String plugId,
			String userId);
}
