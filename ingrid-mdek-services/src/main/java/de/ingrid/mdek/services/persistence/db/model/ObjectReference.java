package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectReference implements IEntity {

	private Long id;
	private Integer version;
	private Long objFromId;
	private String objToUuid;
	private Integer line;
	private Integer specialRef;
	private String specialName;
	private String descr;

	private ObjectNode objectNode;

	public ObjectReference() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getObjFromId() {
		return objFromId;
	}

	public void setObjFromId(Long objFromId) {
		this.objFromId = objFromId;
	}

	public String getObjToUuid() {
		return objToUuid;
	}

	public void setObjToUuid(String objToUuid) {
		this.objToUuid = objToUuid;
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

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public ObjectNode getObjectNode() {
		return objectNode;
	}

	public void setObjectNode(ObjectNode objectNode) {
		this.objectNode = objectNode;
	}
}