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
	private Long idcGroupId;
	private Integer idcRole;

	private AddressNode addressNode;
	private IdcGroup idcGroup;
	private Set idcUsers = new HashSet();
	private Set idcUserPermissions = new HashSet();

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

	public Long getIdcGroupId() {
		return idcGroupId;
	}

	public void setIdcGroupId(Long idcGroupId) {
		this.idcGroupId = idcGroupId;
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

	public IdcGroup getIdcGroup() {
		return idcGroup;
	}

	public void setIdcGroup(IdcGroup idcGroup) {
		this.idcGroup = idcGroup;
	}

	public Set getIdcUsers() {
		return idcUsers;
	}

	public void setIdcUsers(Set idcUsers) {
		this.idcUsers = idcUsers;
	}

	public Set getIdcUserPermissions() {
		return idcUserPermissions;
	}

	public void setIdcUserPermissions(Set idcUserPermissions) {
		this.idcUserPermissions = idcUserPermissions;
	}

}