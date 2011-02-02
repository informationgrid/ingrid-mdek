package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T08AttrList implements IEntity {

	private Long id;
	private int version;
	private Long attrTypeId;
	private String type;
	private Integer listitemLine;
	private String listitemValue;
	private String langCode;


	public T08AttrList() {}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getListitemLine() {
		return listitemLine;
	}

	public void setListitemLine(Integer listitemLine) {
		this.listitemLine = listitemLine;
	}

	public String getListitemValue() {
		return listitemValue;
	}

	public void setListitemValue(String listitemValue) {
		this.listitemValue = listitemValue;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}


}