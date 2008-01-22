package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeoScale implements IEntity {

	private Long id;
	private int version;
	private Long objGeoId;
	private Integer line;
	private Integer scale;
	private Double resolutionGround;
	private Double resolutionScan;


	public T011ObjGeoScale() {}

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

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Double getResolutionGround() {
		return resolutionGround;
	}

	public void setResolutionGround(Double resolutionGround) {
		this.resolutionGround = resolutionGround;
	}

	public Double getResolutionScan() {
		return resolutionScan;
	}

	public void setResolutionScan(Double resolutionScan) {
		this.resolutionScan = resolutionScan;
	}


}