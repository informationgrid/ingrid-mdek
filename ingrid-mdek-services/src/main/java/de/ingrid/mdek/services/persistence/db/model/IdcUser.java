package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class IdcUser implements IEntity {

	private Long id;
	private int version;
	private Long parentId;
	private String addrUuid;
	private String createTime;
	private String modTime;
	private String modUuid;
	private Integer idcRole;

	private AddressNode addressNode;
	private Set idcUsers = new HashSet();
	private Set idcUserGroups = new HashSet();

	public IdcUser() {}

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

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getAddrUuid() {
		return addrUuid;
	}

	public void setAddrUuid(String addrUuid) {
		this.addrUuid = addrUuid;
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

	public Integer getIdcRole() {
		return idcRole;
	}

	public void setIdcRole(Integer idcRole) {
		this.idcRole = idcRole;
	}


	public AddressNode getAddressNode() {
		return addressNode;
	}

	public void setAddressNode(AddressNode addressNode) {
		this.addressNode = addressNode;
	}

	public Set getIdcUsers() {
		return idcUsers;
	}

	public void setIdcUsers(Set idcUsers) {
		this.idcUsers = idcUsers;
	}

	public Set getIdcUserGroups() {
		return idcUserGroups;
	}

	public void setIdcUserGroups(Set idcUserGroups) {
		this.idcUserGroups = idcUserGroups;
	}

}