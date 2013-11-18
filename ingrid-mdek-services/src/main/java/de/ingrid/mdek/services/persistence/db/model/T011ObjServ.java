package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServ implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer typeKey;
	private String typeValue;
	private String history;
	private String environment;
	private String base;
	private String description;
	private String hasAccessConstraint;
	private String couplingType;
	private String hasAtomDownload;

	private Set t011ObjServOperations = new HashSet();
	private Set t011ObjServScales = new HashSet();
	private Set t011ObjServTypes = new HashSet();
	private Set t011ObjServUrls = new HashSet();
	private Set t011ObjServVersions = new HashSet();

	public T011ObjServ() {}

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

	public Integer getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(Integer typeKey) {
		this.typeKey = typeKey;
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHasAccessConstraint() {
		return hasAccessConstraint;
	}

	public void setHasAccessConstraint(String hasAccessConstraint) {
		this.hasAccessConstraint = hasAccessConstraint;
	}

	public String getCouplingType() {
		return couplingType;
	}

	public void setCouplingType(String couplingType) {
		this.couplingType = couplingType;
	}

	public String getHasAtomDownload() {
		return hasAtomDownload;
	}

	public void setHasAtomDownload(String hasAtomDownload) {
		this.hasAtomDownload = hasAtomDownload;
	}


	public Set getT011ObjServOperations() {
		return t011ObjServOperations;
	}

	public void setT011ObjServOperations(Set t011ObjServOperations) {
		this.t011ObjServOperations = t011ObjServOperations;
	}

	public Set getT011ObjServScales() {
		return t011ObjServScales;
	}

	public void setT011ObjServScales(Set t011ObjServScales) {
		this.t011ObjServScales = t011ObjServScales;
	}

	public Set getT011ObjServTypes() {
		return t011ObjServTypes;
	}

	public void setT011ObjServTypes(Set t011ObjServTypes) {
		this.t011ObjServTypes = t011ObjServTypes;
	}

	public Set getT011ObjServUrls() {
		return t011ObjServUrls;
	}

	public void setT011ObjServUrls(Set t011ObjServUrls) {
		this.t011ObjServUrls = t011ObjServUrls;
	}

	public Set getT011ObjServVersions() {
		return t011ObjServVersions;
	}

	public void setT011ObjServVersions(Set t011ObjServVersions) {
		this.t011ObjServVersions = t011ObjServVersions;
	}

}