package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SearchtermValue implements IEntity {

	private Long id;
	private int version;
	private String type;
	private Long searchtermSnsId;
	private Integer entryId;
	private String term;

	private SearchtermSns searchtermSns;

	public SearchtermValue() {}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getSearchtermSnsId() {
		return searchtermSnsId;
	}

	public void setSearchtermSnsId(Long searchtermSnsId) {
		this.searchtermSnsId = searchtermSnsId;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}


	public SearchtermSns getSearchtermSns() {
		return searchtermSns;
	}

	public void setSearchtermSns(SearchtermSns searchtermSns) {
		this.searchtermSns = searchtermSns;
	}

}