package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T03Catalogue implements IEntity {

	private Long id;
	private int version;
	private String catUuid;
	private String catName;
	private String catNamespace;
	private String partnerName;
	private String providerName;
	private Integer countryKey;
	private String countryValue;
	private Integer languageKey;
	private String languageValue;
	private Long spatialRefId;
	private String workflowControl;
	private Integer expiryDuration;
	private String atomDownloadUrl;
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

	public String getCatNamespace() {
		return catNamespace;
	}

	public void setCatNamespace(String catNamespace) {
		this.catNamespace = catNamespace;
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

	public Integer getCountryKey() {
		return countryKey;
	}

	public void setCountryKey(Integer countryKey) {
		this.countryKey = countryKey;
	}

	public String getCountryValue() {
		return countryValue;
	}

	public void setCountryValue(String countryValue) {
		this.countryValue = countryValue;
	}

	public Integer getLanguageKey() {
		return languageKey;
	}

	public void setLanguageKey(Integer languageKey) {
		this.languageKey = languageKey;
	}

	public String getLanguageValue() {
		return languageValue;
	}

	public void setLanguageValue(String languageValue) {
		this.languageValue = languageValue;
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

	public String getAtomDownloadUrl() {
		return atomDownloadUrl;
	}

	public void setAtomDownloadUrl(String atomDownloadUrl) {
		this.atomDownloadUrl = atomDownloadUrl;
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