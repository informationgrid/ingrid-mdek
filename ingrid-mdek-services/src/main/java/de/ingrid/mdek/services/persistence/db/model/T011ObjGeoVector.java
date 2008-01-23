package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoVector implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private Integer geometricObjectType;
	private Integer geometricObjectCount;


	public T011ObjGeoVector() {}

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

	public Integer getGeometricObjectType() {
		return geometricObjectType;
	}

	public void setGeometricObjectType(Integer geometricObjectType) {
		this.geometricObjectType = geometricObjectType;
	}

	public Integer getGeometricObjectCount() {
		return geometricObjectCount;
	}

	public void setGeometricObjectCount(Integer geometricObjectCount) {
		this.geometricObjectCount = geometricObjectCount;
	}


}