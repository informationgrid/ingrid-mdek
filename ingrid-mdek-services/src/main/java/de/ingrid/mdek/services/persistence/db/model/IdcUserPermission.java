package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class IdcUserPermission implements IEntity {

	private Long id;
	private int version;
	private Long permissionId;
	private Long idcGroupId;

	private Permission permission;

	public IdcUserPermission() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}

	public Long getIdcGroupId() {
		return idcGroupId;
	}

	public void setIdcGroupId(Long idcGroupId) {
		this.idcGroupId = idcGroupId;
	}


	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

}