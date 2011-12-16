package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectTypesCatalogue implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer titleKey;
	private String titleValue;
	private String typeDate;
	private String typeVersion;


	public ObjectTypesCatalogue() {}

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

	public Integer getTitleKey() {
		return titleKey;
	}

	public void setTitleKey(Integer titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleValue() {
		return titleValue;
	}

	public void setTitleValue(String titleValue) {
		this.titleValue = titleValue;
	}

	public String getTypeDate() {
		return typeDate;
	}

	public void setTypeDate(String typeDate) {
		this.typeDate = typeDate;
	}

	public String getTypeVersion() {
		return typeVersion;
	}

	public void setTypeVersion(String typeVersion) {
		this.typeVersion = typeVersion;
	}


}