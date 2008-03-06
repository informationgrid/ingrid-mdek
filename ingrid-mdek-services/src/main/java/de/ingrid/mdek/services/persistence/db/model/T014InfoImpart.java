package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T014InfoImpart implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String impartValue;
	private Integer impartKey;


	public T014InfoImpart() {}

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

	public String getImpartValue() {
		return impartValue;
	}

	public void setImpartValue(String impartValue) {
		this.impartValue = impartValue;
	}

	public Integer getImpartKey() {
		return impartKey;
	}

	public void setImpartKey(Integer impartKey) {
		this.impartKey = impartKey;
	}


}