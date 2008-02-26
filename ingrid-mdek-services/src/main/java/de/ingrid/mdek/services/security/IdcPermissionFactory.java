/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public class IdcPermissionFactory {

	public static Permission getSingleObjectPermission(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName(uuid);
		p.setAction("write");
		return p;
	}
	
}
