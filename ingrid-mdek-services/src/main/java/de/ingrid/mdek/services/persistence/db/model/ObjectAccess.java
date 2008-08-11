package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectAccess implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer restrictionKey;
	private String restrictionValue;
	private String termsOfUse;


	public ObjectAccess() {}

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

	public Integer getRestrictionKey() {
		return restrictionKey;
	}

	public void setRestrictionKey(Integer restrictionKey) {
		this.restrictionKey = restrictionKey;
	}

	public String getRestrictionValue() {
		return restrictionValue;
	}

	public void setRestrictionValue(String restrictionValue) {
		this.restrictionValue = restrictionValue;
	}

	public String getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}


}