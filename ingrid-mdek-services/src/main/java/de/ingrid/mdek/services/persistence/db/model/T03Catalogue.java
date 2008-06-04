package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T03Catalogue implements IEntity {

	private Long id;
	private int version;
	private String catUuid;
	private String catName;
	private String partnerName;
	private String providerName;
	private String countryCode;
	private String languageCode;
	private Long spatialRefId;
	private String workflowControl;
	private Integer expiryDuration;
	private String createTime;
	private String modUuid;
	private String modTime;

	private SpatialRefValue spatialRefValue;

	public T03Catalogue() {}

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

	public String getCatUuid() {
		return catUuid;
	}

	public void setCatUuid(String catUuid) {
		this.catUuid = catUuid;
	}

	public String getCatName() {
		return catName;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public Long getSpatialRefId() {
		return spatialRefId;
	}

	public void setSpatialRefId(Long spatialRefId) {
		this.spatialRefId = spatialRefId;
	}

	public String getWorkflowControl() {
		return workflowControl;
	}

	public void setWorkflowControl(String workflowControl) {
		this.workflowControl = workflowControl;
	}

	public Integer getExpiryDuration() {
		return expiryDuration;
	}

	public void setExpiryDuration(Integer expiryDuration) {
		this.expiryDuration = expiryDuration;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getModUuid() {
		return modUuid;
	}

	public void setModUuid(String modUuid) {
		this.modUuid = modUuid;
	}

	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}


	public SpatialRefValue getSpatialRefValue() {
		return spatialRefValue;
	}

	public void setSpatialRefValue(SpatialRefValue spatialRefValue) {
		this.spatialRefValue = spatialRefValue;
	}

}