package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectOpenDataCategory implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer categoryKey;
	private String categoryValue;


	public ObjectOpenDataCategory() {}

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

	public Integer getCategoryKey() {
		return categoryKey;
	}

	public void setCategoryKey(Integer categoryKey) {
		this.categoryKey = categoryKey;
	}

	public String getCategoryValue() {
		return categoryValue;
	}

	public void setCategoryValue(String categoryValue) {
		this.categoryValue = categoryValue;
	}


}