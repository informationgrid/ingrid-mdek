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
	private Integer countryKey;
	private String countryValue;
	private String job;
	private String descr;
	private String workState;
	private String createTime;
	private String modTime;
	private Long addrMetadataId;
	private String modUuid;
	private String responsibleUuid;
	private String hideAddress;

	private Set addressComments = new HashSet();
	private Set searchtermAdrs = new HashSet();
	private Set t021Communications = new HashSet();
	private AddressMetadata addressMetadata;
	private AddressNode addressNodeResponsible;
	private AddressNode addressNodeMod;

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

	public String getWorkState() {
		return workState;
	}

	public void setWorkState(String workState) {
		this.workState = workState;
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

	public Long getAddrMetadataId() {
		return addrMetadataId;
	}

	public void setAddrMetadataId(Long addrMetadataId) {
		this.addrMetadataId = addrMetadataId;
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

	public String getHideAddress() {
		return hideAddress;
	}

	public void setHideAddress(String hideAddress) {
		this.hideAddress = hideAddress;
	}


	public Set getAddressComments() {
		return addressComments;
	}

	public void setAddressComments(Set addressComments) {
		this.addressComments = addressComments;
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

	public AddressMetadata getAddressMetadata() {
		return addressMetadata;
	}

	public void setAddressMetadata(AddressMetadata addressMetadata) {
		this.addressMetadata = addressMetadata;
	}

	public AddressNode getAddressNodeResponsible() {
		return addressNodeResponsible;
	}

	public void setAddressNodeResponsible(AddressNode addressNodeResponsible) {
		this.addressNodeResponsible = addressNodeResponsible;
	}

	public AddressNode getAddressNodeMod() {
		return addressNodeMod;
	}

	public void setAddressNodeMod(AddressNode addressNodeMod) {
		this.addressNodeMod = addressNodeMod;
	}

}