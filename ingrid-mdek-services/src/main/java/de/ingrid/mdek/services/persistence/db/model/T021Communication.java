package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T021Communication implements IEntity {

	private Long id;
	private int version;
	private Long adrId;
	private Integer line;
	private Integer commtypeKey;
	private String commtypeValue;
	private String commValue;
	private String descr;


	public T021Communication() {}

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

	public Long getAdrId() {
		return adrId;
	}

	public void setAdrId(Long adrId) {
		this.adrId = adrId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getCommtypeKey() {
		return commtypeKey;
	}

	public void setCommtypeKey(Integer commtypeKey) {
		this.commtypeKey = commtypeKey;
	}

	public String getCommtypeValue() {
		return commtypeValue;
	}

	public void setCommtypeValue(String commtypeValue) {
		this.commtypeValue = commtypeValue;
	}

	public String getCommValue() {
		return commValue;
	}

	public void setCommValue(String commValue) {
		this.commValue = commValue;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}


}