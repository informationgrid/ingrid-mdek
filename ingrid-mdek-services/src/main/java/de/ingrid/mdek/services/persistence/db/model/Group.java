package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class Group implements IEntity {

	private Long id;
	private int version;
	private String name;
	private String createTime;
	private String modTime;
	private String modUuid;

	private Set permissionAddrs = new HashSet();
	private Set permissionObjs = new HashSet();

	public Group() {}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

	public String getModUuid() {
		return modUuid;
	}

	public void setModUuid(String modUuid) {
		this.modUuid = modUuid;
	}


	public Set getPermissionAddrs() {
		return permissionAddrs;
	}

	public void setPermissionAddrs(Set permissionAddrs) {
		this.permissionAddrs = permissionAddrs;
	}

	public Set getPermissionObjs() {
		return permissionObjs;
	}

	public void setPermissionObjs(Set permissionObjs) {
		this.permissionObjs = permissionObjs;
	}

}