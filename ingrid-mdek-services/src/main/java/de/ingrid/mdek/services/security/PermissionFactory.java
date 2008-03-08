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

	public static Permission getSingleObjectPermission(String uuid) {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName(uuid);
		p.setAction("write");
		return p;
	}
	
	public static Permission getTreeObjectPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcObjectPermission");
		p.setName(uuid);
		p.setAction("write-tree");
		return p;
	}
	
	public static Permission getSingleAddressPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcAdressPermission");
		p.setName(uuid);
		p.setAction("write");
		return p;
	}

	public static Permission getTreeAdressPermission(String uuid)  {
		Permission p = new Permission();
		p.setClassName("IdcAdressPermission");
		p.setName(uuid);
		p.setAction("write-tree");
		return p;
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
