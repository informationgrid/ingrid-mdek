package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SpatialReference implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Long spatialRefId;

	private SpatialRefValue spatialRefValue;

	public SpatialReference() {}

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

	public Long getSpatialRefId() {
		return spatialRefId;
	}

	public void setSpatialRefId(Long spatialRefId) {
		this.spatialRefId = spatialRefId;
	}


	public SpatialRefValue getSpatialRefValue() {
		return spatialRefValue;
	}

	public void setSpatialRefValue(SpatialRefValue spatialRefValue) {
		this.spatialRefValue = spatialRefValue;
	}

}