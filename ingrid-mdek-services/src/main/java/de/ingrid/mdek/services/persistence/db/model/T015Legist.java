package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T015Legist implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String legistValue;
	private Integer legistKey;


	public T015Legist() {}

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

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getLegistValue() {
		return legistValue;
	}

	public void setLegistValue(String legistValue) {
		this.legistValue = legistValue;
	}

	public Integer getLegistKey() {
		return legistKey;
	}

	public void setLegistKey(Integer legistKey) {
		this.legistKey = legistKey;
	}


}