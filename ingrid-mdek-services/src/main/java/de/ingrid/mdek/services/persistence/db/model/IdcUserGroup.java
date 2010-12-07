package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class IdcUserGroup implements IEntity {

	private Long id;
	private int version;
	private Long idcUserId;
	private Long idcGroupId;


	public IdcUserGroup() {}

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

	public Long getIdcUserId() {
		return idcUserId;
	}

	public void setIdcUserId(Long idcUserId) {
		this.idcUserId = idcUserId;
	}

	public Long getIdcGroupId() {
		return idcGroupId;
	}

	public void setIdcGroupId(Long idcGroupId) {
		this.idcGroupId = idcGroupId;
	}


}