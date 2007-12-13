package de.ingrid.mdek.services.persistence.db.model;

import java.io.Serializable;

// TODO: Serializable because of composite Id !!! Remove when unique id
public class T021Communication implements Serializable {

	private String adrId;
	private int line;
	private String commType;
	private String commValue;
	private String descr;

	public T021Communication() {}

	public String getAdrId() {
		return adrId;
	}

	public void setAdrId(String adrId) {
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