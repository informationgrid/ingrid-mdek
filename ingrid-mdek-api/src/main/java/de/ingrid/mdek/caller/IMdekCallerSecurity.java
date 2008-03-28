package de.ingrid.mdek.caller;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning SECURITY / USER MANAGEMENT.
 */
public interface IMdekCallerSecurity {

	/**
	 * Get all groups.
	 * @param plugId which mdek server (iplug)
	 * @param userId calling user
	 * @return response containing result: map containing groups
	 */
	IngridDocument getGroups(String plugId,
			String userId);
}
