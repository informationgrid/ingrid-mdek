package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T08Attr implements IEntity {

	private Long id;
	private int version;
	private Long attrTypeId;
	private Long objId;
	private String data;

	private T08AttrType t08AttrType;

	public T08Attr() {}

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

	public Long getAttrTypeId() {
		return attrTypeId;
	}

	public void setAttrTypeId(Long attrTypeId) {
		this.attrTypeId = attrTypeId;
	}

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}


	public T08AttrType getT08AttrType() {
		return t08AttrType;
	}

	public void setT08AttrType(T08AttrType t08AttrType) {
		this.t08AttrType = t08AttrType;
	}

}