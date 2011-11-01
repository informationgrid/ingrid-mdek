package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectUse implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer termsOfUseKey;
	private String termsOfUseValue;


	public ObjectUse() {}

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

	public Integer getTermsOfUseKey() {
		return termsOfUseKey;
	}

	public void setTermsOfUseKey(Integer termsOfUseKey) {
		this.termsOfUseKey = termsOfUseKey;
	}

	public String getTermsOfUseValue() {
		return termsOfUseValue;
	}

	public void setTermsOfUseValue(String termsOfUseValue) {
		this.termsOfUseValue = termsOfUseValue;
	}


}