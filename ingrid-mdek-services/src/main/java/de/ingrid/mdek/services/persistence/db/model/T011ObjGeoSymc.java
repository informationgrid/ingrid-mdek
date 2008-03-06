package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoSymc implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private Integer symbolCatKey;
	private String symbolCatValue;
	private String symbolDate;
	private String edition;


	public T011ObjGeoSymc() {}

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

	public Long getObjGeoId() {
		return objGeoId;
	}

	public void setObjGeoId(Long objGeoId) {
		this.objGeoId = objGeoId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getSymbolCatKey() {
		return symbolCatKey;
	}

	public void setSymbolCatKey(Integer symbolCatKey) {
		this.symbolCatKey = symbolCatKey;
	}

	public String getSymbolCatValue() {
		return symbolCatValue;
	}

	public void setSymbolCatValue(String symbolCatValue) {
		this.symbolCatValue = symbolCatValue;
	}

	public String getSymbolDate() {
		return symbolDate;
	}

	public void setSymbolDate(String symbolDate) {
		this.symbolDate = symbolDate;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}


}