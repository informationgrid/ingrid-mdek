package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SpatialRefValue implements IEntity {

	private Long id;
	private int version;
	private String type;
	private Long spatialRefSnsId;
	private String name;
	private String nativekey;
	private Double x1;
	private Double y1;
	private Double x2;
	private Double y2;

	private SpatialRefSns spatialRefSns;

	public SpatialRefValue() {}

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

	public Long getSpatialRefSnsId() {
		return spatialRefSnsId;
	}

	public void setSpatialRefSnsId(Long spatialRefSnsId) {
		this.spatialRefSnsId = spatialRefSnsId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNativekey() {
		return nativekey;
	}

	public void setNativekey(String nativekey) {
		this.nativekey = nativekey;
	}

	public Double getX1() {
		return x1;
	}

	public void setX1(Double x1) {
		this.x1 = x1;
	}

	public Double getY1() {
		return y1;
	}

	public void setY1(Double y1) {
		this.y1 = y1;
	}

	public Double getX2() {
		return x2;
	}

	public void setX2(Double x2) {
		this.x2 = x2;
	}

	public Double getY2() {
		return y2;
	}

	public void setY2(Double y2) {
		this.y2 = y2;
	}


	public SpatialRefSns getSpatialRefSns() {
		return spatialRefSns;
	}

	public void setSpatialRefSns(SpatialRefSns spatialRefSns) {
		this.spatialRefSns = spatialRefSns;
	}

}