package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServVersion implements IEntity {

	private Long id;
	private int version;
	private Long objServId;
	private Integer line;
	private String servVersion;


	public T011ObjServVersion() {}

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

	public String getServVersion() {
		return servVersion;
	}

	public void setServVersion(String servVersion) {
		this.servVersion = servVersion;
	}


}