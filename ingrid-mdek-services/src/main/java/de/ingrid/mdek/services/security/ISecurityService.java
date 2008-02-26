/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public interface ISecurityService {

	public boolean hasPermission(Permission p, String uuid);
	
}
