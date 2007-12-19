package de.ingrid.mdek.services.persistence.db.model;

import java.io.Serializable;


// Serializable because of composite Id !!! Remove when unique id
public class T012ObjAdr  extends MdekDbEntity implements Serializable {

	private String objId;
	private String adrId;
	private int type;
	private int line;
	private Integer specialRef;
	private String specialName;
	private String modTime;
	
	private T02Address t02Address;

	public T012ObjAdr() {}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getAdrId() {
		return adrId;
	}

	public void setAdrId(String adrId) {
		this.adrId = adrId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public Integer getSpecialRef() {
		return specialRef;
	}

	public void setSpecialRef(Integer specialRef) {
		this.specialRef = specialRef;
	}

	public String getSpecialName() {
		return specialName;
	}

	public void setSpecialName(String specialName) {
		this.specialName = specialName;
	}

	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

	public T02Address getT02Address() {
		return t02Address;
	}

	public void setT02Address(T02Address t02Address) {
		this.t02Address = t02Address;
	}
}