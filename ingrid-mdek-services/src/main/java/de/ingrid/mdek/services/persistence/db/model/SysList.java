package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SysList implements IEntity {

	private Long id;
	private int version;
	private Integer lstId;
	private Integer entryId;
	private String langId;
	private String name;
	private String description;
	private Integer maintainable;


	public SysList() {}

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

	public Integer getLstId() {
		return lstId;
	}

	public void setLstId(Integer lstId) {
		this.lstId = lstId;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getLangId() {
		return langId;
	}

	public void setLangId(String langId) {
		this.langId = langId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getMaintainable() {
		return maintainable;
	}

	public void setMaintainable(Integer maintainable) {
		this.maintainable = maintainable;
	}


}