package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class FullIndexAddr implements IEntity {

	private Long id;
	private int version;
	private Long addrNodeId;
	private String idxName;
	private String idxValue;


	public FullIndexAddr() {}

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

	public Long getAddrNodeId() {
		return addrNodeId;
	}

	public void setAddrNodeId(Long addrNodeId) {
		this.addrNodeId = addrNodeId;
	}

	public String getIdxName() {
		return idxName;
	}

	public void setIdxName(String idxName) {
		this.idxName = idxName;
	}

	public String getIdxValue() {
		return idxValue;
	}

	public void setIdxValue(String idxValue) {
		this.idxValue = idxValue;
	}


}