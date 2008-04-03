package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T02Address implements IEntity {

	private Long id;
	private int version;
	private String adrUuid;
	private String orgAdrId;
	private Integer adrType;
	private String institution;
	private String lastname;
	private String firstname;
	private Integer addressKey;
	private String addressValue;
	private Integer titleKey;
	private String titleValue;
	private String street;
	private String postcode;
	private String postbox;
	private String postboxPc;
	private String city;
	private String countryCode;
	private String job;
	private String descr;
	private String lastexportTime;
	private String expiryTime;
	private String workState;
	private Integer workVersion;
	private String markDeleted;
	private String createTime;
	private String modTime;
	private String modUuid;
	private String responsibleUuid;

	private Set addressComments = new HashSet();
	private Set fullIndexAddrs = new HashSet();
	private Set searchtermAdrs = new HashSet();
	private Set t021Communications = new HashSet();

	public T02Address() {}

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

	public String getAdrUuid() {
		return adrUuid;
	}

	public void setAdrUuid(String adrUuid) {
		this.adrUuid = adrUuid;
	}

	public String getOrgAdrId() {
		return orgAdrId;
	}

	public void setOrgAdrId(String orgAdrId) {
		this.orgAdrId = orgAdrId;
	}

	public Integer getAdrType() {
		return adrType;
	}

	public void setAdrType(Integer adrType) {
		this.adrType = adrType;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Integer getAddressKey() {
		return addressKey;
	}

	public void setAddressKey(Integer addressKey) {
		this.addressKey = addressKey;
	}

	public String getAddressValue() {
		return addressValue;
	}

	public void setAddressValue(String addressValue) {
		this.addressValue = addressValue;
	}

	public Integer getTitleKey() {
		return titleKey;
	}

	public void setTitleKey(Integer titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleValue() {
		return titleValue;
	}

	public void setTitleValue(String titleValue) {
		this.titleValue = titleValue;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getPostbox() {
		return postbox;
	}

	public void setPostbox(String postbox) {
		this.postbox = postbox;
	}

	public String getPostboxPc() {
		return postboxPc;
	}

	public void setPostboxPc(String postboxPc) {
		this.postboxPc = postboxPc;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getLastexportTime() {
		return lastexportTime;
	}

	public void setLastexportTime(String lastexportTime) {
		this.lastexportTime = lastexportTime;
	}

	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

	public String getWorkState() {
		return workState;
	}

	public void setWorkState(String workState) {
		this.workState = workState;
	}

	public Integer getWorkVersion() {
		return workVersion;
	}

	public void setWorkVersion(Integer workVersion) {
		this.workVersion = workVersion;
	}

	public String getMarkDeleted() {
		return markDeleted;
	}

	public void setMarkDeleted(String markDeleted) {
		this.markDeleted = markDeleted;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

	public String getModUuid() {
		return modUuid;
	}

	public void setModUuid(String modUuid) {
		this.modUuid = modUuid;
	}

	public String getResponsibleUuid() {
		return responsibleUuid;
	}

	public void setResponsibleUuid(String responsibleUuid) {
		this.responsibleUuid = responsibleUuid;
	}


	public Set getAddressComments() {
		return addressComments;
	}

	public void setAddressComments(Set addressComments) {
		this.addressComments = addressComments;
	}

	public Set getFullIndexAddrs() {
		return fullIndexAddrs;
	}

	public void setFullIndexAddrs(Set fullIndexAddrs) {
		this.fullIndexAddrs = fullIndexAddrs;
	}

	public Set getSearchtermAdrs() {
		return searchtermAdrs;
	}

	public void setSearchtermAdrs(Set searchtermAdrs) {
		this.searchtermAdrs = searchtermAdrs;
	}

	public Set getT021Communications() {
		return t021Communications;
	}

	public void setT021Communications(Set t021Communications) {
		this.t021Communications = t021Communications;
	}

}