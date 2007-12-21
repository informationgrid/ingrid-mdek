package de.ingrid.mdek.services.persistence.db.model;

import java.io.Serializable;

public class T021Communication extends MdekDbEntity {

	private long adrId;
	private int line;
	private String commType;
	private String commValue;
	private String descr;

	public T021Communication() {}

	public long getAdrId() {
		return adrId;
	}

	public void setAdrId(long adrId) {
		this.adrId = adrId;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getCommType() {
		return commType;
	}

	public void setCommType(String commType) {
		this.commType = commType;
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