/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class IdcGroup implements IEntity {

	private Long id;
	private int version;
	private String name;
	private String createTime;
	private String modTime;
	private String modUuid;

	private Set idcUserGroups = new HashSet();
	private Set idcUserPermissions = new HashSet();
	private Set permissionAddrs = new HashSet();
	private Set permissionObjs = new HashSet();

	public IdcGroup() {}

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


	public Set getIdcUserGroups() {
		return idcUserGroups;
	}

	public void setIdcUserGroups(Set idcUserGroups) {
		this.idcUserGroups = idcUserGroups;
	}

	public Set getIdcUserPermissions() {
		return idcUserPermissions;
	}

	public void setIdcUserPermissions(Set idcUserPermissions) {
		this.idcUserPermissions = idcUserPermissions;
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
