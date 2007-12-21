package de.ingrid.mdek.services.persistence.db.model;



public class T012ObjObj extends MdekDbEntity {

	private String objectFromUuid;
	private String objectToUuid;
	private int type;
	private int line;
	private Integer specialRef;
	private String specialName;
	private String descr;
	
	private T01Object toT01Object;

	public T012ObjObj() {}

	public String getObjectFromUuid() {
		return objectFromUuid;
	}

	public void setObjectFromUuid(String objectFromUuid) {
		this.objectFromUuid = objectFromUuid;
	}

	public String getObjectToUuid() {
		return objectToUuid;
	}

	public void setObjectToUuid(String objectToUuid) {
		this.objectToUuid = objectToUuid;
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

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public T01Object getToT01Object() {
		return toT01Object;
	}

	public void setToT01Object(T01Object toT01Object) {
		this.toT01Object = toT01Object;
	}
}