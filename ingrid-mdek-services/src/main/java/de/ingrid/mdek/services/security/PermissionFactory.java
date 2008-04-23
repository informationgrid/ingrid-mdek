/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Factory for different types of entity permissions.
 * 
 * @author joachim
 * 
 */
public class PermissionFactory {

	public static EntityPermission getSingleObjectPermissionTemplate(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName("object");
		p.setAction("write");
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeObjectPermissionTemplate(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName("object");
		p.setAction("write-tree");
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getSingleAddressPermissionTemplate(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcAddressPermission");
		p.setName("address");
		p.setAction("write");
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeAddressPermissionTemplate(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcAddressPermission");
		p.setName("address");
		p.setAction("write-tree");
		return new EntityPermission(p, uuid);
	}

	public static Permission getCreateRootPermissionTemplate() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("create-root");
		return p;
	}

	public static Permission getQAPermissionTemplate() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("qa");
		return p;
	}
}
