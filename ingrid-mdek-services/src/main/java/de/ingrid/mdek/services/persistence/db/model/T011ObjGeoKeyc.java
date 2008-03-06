package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoKeyc implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private Integer keycKey;
	private String keycValue;
	private String keyDate;
	private String edition;


	public T011ObjGeoKeyc() {}

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

	public Integer getKeycKey() {
		return keycKey;
	}

	public void setKeycKey(Integer keycKey) {
		this.keycKey = keycKey;
	}

	public String getKeycValue() {
		return keycValue;
	}

	public void setKeycValue(String keycValue) {
		this.keycValue = keycValue;
	}

	public String getKeyDate() {
		return keyDate;
	}

	public void setKeyDate(String keyDate) {
		this.keyDate = keyDate;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}


}