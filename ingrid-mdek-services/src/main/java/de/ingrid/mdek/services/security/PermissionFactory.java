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
		Permission p = getPermissionTemplateSingle();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeObjectPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateTree();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getSingleAddressPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateSingle();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeAddressPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateTree();
		return new EntityPermission(p, uuid);
	}

	public static Permission getPermissionTemplateCreateRoot() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("create-root");
		return p;
	}

	public static Permission getPermissionTemplateQA() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("qa");
		return p;
	}

	public static Permission getPermissionTemplateSingle() {
		Permission p = new Permission();
		p.setClassName("IdcEntityPermission");
		p.setName("entity");
		p.setAction("write");
		return p;
	}

	public static Permission getPermissionTemplateTree() {
		Permission p = new Permission();
		p.setClassName("IdcEntityPermission");
		p.setName("entity");
		p.setAction("write-tree");
		return p;
	}
}
