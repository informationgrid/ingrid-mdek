package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServType implements IEntity {

	private Long id;
	private int version;
	private Long objServId;
	private Integer line;
	private Integer servTypeKey;
	private String servTypeValue;


	public T011ObjServType() {}

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

	public Long getObjServId() {
		return objServId;
	}

	public void setObjServId(Long objServId) {
		this.objServId = objServId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getServTypeKey() {
		return servTypeKey;
	}

	public void setServTypeKey(Integer servTypeKey) {
		this.servTypeKey = servTypeKey;
	}

	public String getServTypeValue() {
		return servTypeValue;
	}

	public void setServTypeValue(String servTypeValue) {
		this.servTypeValue = servTypeValue;
	}


}