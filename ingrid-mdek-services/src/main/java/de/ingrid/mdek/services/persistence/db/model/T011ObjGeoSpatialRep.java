package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoSpatialRep implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private Integer type;


	public T011ObjGeoSpatialRep() {}

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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}


}