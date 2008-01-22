package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SearchtermObj implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Long searchtermId;

	private SearchtermValue searchtermValue;

	public SearchtermObj() {}

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