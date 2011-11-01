package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectConformity implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private Integer specificationKey;
	private String specificationValue;
	private Integer degreeKey;
	private String degreeValue;


	public ObjectConformity() {}

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

	public Integer getSpecificationKey() {
		return specificationKey;
	}

	public void setSpecificationKey(Integer specificationKey) {
		this.specificationKey = specificationKey;
	}

	public String getSpecificationValue() {
		return specificationValue;
	}

	public void setSpecificationValue(String specificationValue) {
		this.specificationValue = specificationValue;
	}

	public Integer getDegreeKey() {
		return degreeKey;
	}

	public void setDegreeKey(Integer degreeKey) {
		this.degreeKey = degreeKey;
	}

	public String getDegreeValue() {
		return degreeValue;
	}

	public void setDegreeValue(String degreeValue) {
		this.degreeValue = degreeValue;
	}


}