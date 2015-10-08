package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectUseConstraint implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer licenseKey;
	private String licenseValue;


	public ObjectUseConstraint() {}

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

	public Integer getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(Integer licenseKey) {
		this.licenseKey = licenseKey;
	}

	public String getLicenseValue() {
		return licenseValue;
	}

	public void setLicenseValue(String licenseValue) {
		this.licenseValue = licenseValue;
	}


}