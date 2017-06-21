package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectDataLanguage implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer dataLanguageKey;
	private String dataLanguageValue;


	public ObjectDataLanguage() {}

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

	public Integer getDataLanguageKey() {
		return dataLanguageKey;
	}

	public void setDataLanguageKey(Integer dataLanguageKey) {
		this.dataLanguageKey = dataLanguageKey;
	}

	public String getDataLanguageValue() {
		return dataLanguageValue;
	}

	public void setDataLanguageValue(String dataLanguageValue) {
		this.dataLanguageValue = dataLanguageValue;
	}


}