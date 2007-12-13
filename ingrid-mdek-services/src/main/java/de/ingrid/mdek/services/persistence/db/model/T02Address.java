package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("serial")
public class T02Address extends MdekIDCEntity {

	private String orgAdrId;
	private Integer typ;
	private String institution;
	private String lastname;
	private String firstname;
	private String address;
	private String title;
	private String street;
	private String stateId;
	private String postcode;
	private String city;
	private String postboxPc;
	private String postbox;
	private String job;
	private String descr;

	private Set t022AdrAdrs = new HashSet();
	private Set t021Communications = new HashSet();

/*
	private Set t02Addresss = new HashSet();
	private Set t02Addresss = new HashSet();
	private Set t03Catalogues = new HashSet();
	private Set t07Countrys = new HashSet();
*/

	public T02Address() {
		super();
	}

	public String getOrgAdrId() {
		return orgAdrId;
	}

	public void setOrgAdrId(String orgAdrId) {
		this.orgAdrId = orgAdrId;
	}

	public Integer getTyp() {
		return typ;
	}

	public void setTyp(Integer typ) {
		this.typ = typ;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostboxPc() {
		return postboxPc;
	}

	public void setPostboxPc(String postboxPc) {
		this.postboxPc = postboxPc;
	}

	public String getPostbox() {
		return postbox;
	}

	public void setPostbox(String postbox) {
		this.postbox = postbox;
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

	// ASSOCIATIONS

	public Set getT022AdrAdrs() {
		return t022AdrAdrs;
	}

	public void setT022AdrAdrs(Set t022AdrAdrs) {
		this.t022AdrAdrs = t022AdrAdrs;
	}

	public Set getT021Communications() {
		return t021Communications;
	}

	public void setT021Communications(Set t021Communications) {
		this.t021Communications = t021Communications;
	}

/*
	public Set getT02Addresss() {
		return t02Addresss;
	}

	public void setT02Addresss(Set t02Addresss) {
		this.t02Addresss = t02Addresss;
	}

	public Set getT02Addresss() {
		return t02Addresss;
	}

	public void setT02Addresss(Set t02Addresss) {
		this.t02Addresss = t02Addresss;
	}

	public Set getT03Catalogues() {
		return t03Catalogues;
	}

	public void setT03Catalogues(Set t03Catalogues) {
		this.t03Catalogues = t03Catalogues;
	}

	public Set getT07Countrys() {
		return t07Countrys;
	}

	public void setT07Countrys(Set t07Countrys) {
		this.t07Countrys = t07Countrys;
	}
*/
}