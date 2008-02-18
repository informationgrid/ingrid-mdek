package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SearchtermAdr implements IEntity {

	private Long id;
	private int version;
	private Long adrId;
	private Integer line;
	private Long searchtermId;

	private SearchtermValue searchtermValue;

	public SearchtermAdr() {}

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

	public Long getAdrId() {
		return adrId;
	}

	public void setAdrId(Long adrId) {
		this.adrId = adrId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Long getSearchtermId() {
		return searchtermId;
	}

	public void setSearchtermId(Long searchtermId) {
		this.searchtermId = searchtermId;
	}


	public SearchtermValue getSearchtermValue() {
		return searchtermValue;
	}

	public void setSearchtermValue(SearchtermValue searchtermValue) {
		this.searchtermValue = searchtermValue;
	}

}