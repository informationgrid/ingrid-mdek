package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServOpConnpoint implements IEntity {

	private Long id;
	private int version;
	private Long objServOpId;
	private Integer line;
	private String connectPoint;


	public T011ObjServOpConnpoint() {}

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

	public Long getObjServOpId() {
		return objServOpId;
	}

	public void setObjServOpId(Long objServOpId) {
		this.objServOpId = objServOpId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getConnectPoint() {
		return connectPoint;
	}

	public void setConnectPoint(String connectPoint) {
		this.connectPoint = connectPoint;
	}


}