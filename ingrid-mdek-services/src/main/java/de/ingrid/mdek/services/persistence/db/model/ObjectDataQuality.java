package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectDataQuality implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer dqElementId;
	private Integer line;
	private Integer nameOfMeasureKey;
	private String nameOfMeasureValue;
	private String resultValue;
	private String measureDescription;


	public ObjectDataQuality() {}

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

	public Integer getDqElementId() {
		return dqElementId;
	}

	public void setDqElementId(Integer dqElementId) {
		this.dqElementId = dqElementId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getNameOfMeasureKey() {
		return nameOfMeasureKey;
	}

	public void setNameOfMeasureKey(Integer nameOfMeasureKey) {
		this.nameOfMeasureKey = nameOfMeasureKey;
	}

	public String getNameOfMeasureValue() {
		return nameOfMeasureValue;
	}

	public void setNameOfMeasureValue(String nameOfMeasureValue) {
		this.nameOfMeasureValue = nameOfMeasureValue;
	}

	public String getResultValue() {
		return resultValue;
	}

	public void setResultValue(String resultValue) {
		this.resultValue = resultValue;
	}

	public String getMeasureDescription() {
		return measureDescription;
	}

	public void setMeasureDescription(String measureDescription) {
		this.measureDescription = measureDescription;
	}


}