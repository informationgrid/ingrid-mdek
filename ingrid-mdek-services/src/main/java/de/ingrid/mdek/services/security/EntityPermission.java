/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public class EntityPermission extends Permission {

	String uuid;
	Permission permission;

	
	public EntityPermission(Permission p, String uuid) {
		this.uuid = uuid;
		this.permission = p;
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the permission
	 */
	public Permission getPermission() {
		return permission;
	}

	/**
	 * @param permission the permission to set
	 */
	public void setPermission(Permission p) {
		this.permission = p;
	}

	public boolean equalsPermission(Permission p) {
		if (p.getAction().equals(permission.getAction())
				&& p.getClassName().equals(permission.getClassName())
				&& p.getName().equals(permission.getName())
				) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
