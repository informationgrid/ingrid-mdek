package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectMetadata implements IEntity {

	private Long id;
	private int version;
	private Integer expiryState;
	private String lastexportTime;
	private String markDeleted;


	public ObjectMetadata() {}

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

	public Integer getExpiryState() {
		return expiryState;
	}

	public void setExpiryState(Integer expiryState) {
		this.expiryState = expiryState;
	}

	public String getLastexportTime() {
		return lastexportTime;
	}

	public void setLastexportTime(String lastexportTime) {
		this.lastexportTime = lastexportTime;
	}

	public String getMarkDeleted() {
		return markDeleted;
	}

	public void setMarkDeleted(String markDeleted) {
		this.markDeleted = markDeleted;
	}


}