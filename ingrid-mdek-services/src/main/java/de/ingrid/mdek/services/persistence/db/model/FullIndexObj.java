package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class FullIndexObj implements IEntity {

	private Long id;
	private int version;
	private Long objNodeId;
	private String idxName;
	private String idxValue;


	public FullIndexObj() {}

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

	public Long getObjNodeId() {
		return objNodeId;
	}

	public void setObjNodeId(Long objNodeId) {
		this.objNodeId = objNodeId;
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