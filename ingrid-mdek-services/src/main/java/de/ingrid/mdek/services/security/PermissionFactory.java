/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * @author joachim
 *
 */
public class PermissionFactory {

	public static EntityPermission getSingleObjectPermission(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName("object");
		p.setAction("write");
		return new EntityPermission(p, uuid);
	}
	
	public static EntityPermission getTreeObjectPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName("object");
		p.setAction("write-tree");
		return new EntityPermission(p, uuid);
	}
	
	public static EntityPermission getSingleAddressPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcAdressPermission");
		p.setName("address");
		p.setAction("write");
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeAdressPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcAdressPermission");
		p.setName("address");
		p.setAction("write-tree");
		return new EntityPermission(p, uuid);
	}
	
	public static Permission getCreateRootPermission() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("create-root");
		return p;
	}

	public static Permission getQAPermission() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("qa");
		return p;
	}
}
