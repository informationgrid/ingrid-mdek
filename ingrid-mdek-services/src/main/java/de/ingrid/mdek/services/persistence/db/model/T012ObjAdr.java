package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T012ObjAdr implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private String adrUuid;
	private Integer type;
	private Integer line;
	private Integer specialRef;
	private String specialName;
	private String modTime;

	private AddressNode addressNode;
	private T01Object t01Object;

	public T012ObjAdr() {}

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

	public String getAdrUuid() {
		return adrUuid;
	}

	public void setAdrUuid(String adrUuid) {
		this.adrUuid = adrUuid;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
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


	public AddressNode getAddressNode() {
		return addressNode;
	}

	public void setAddressNode(AddressNode addressNode) {
		this.addressNode = addressNode;
	}

	public T01Object getT01Object() {
		return t01Object;
	}

	public void setT01Object(T01Object t01Object) {
		this.t01Object = t01Object;
	}

}