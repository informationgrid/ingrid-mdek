package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class T01Object extends MdekIDCEntity {

	private String orgId;
	private String objName;
	private Integer objClass;
	private String objDescr;
	private String infoNote;
	private String availAccessNote;
	private String locDescr;
	private String timeFrom;
	private String timeTo;
	private String timeDescr;
	private Integer timePeriod;
	private String timeInterval;
	private Integer timeStatus;
	private String timeAlle;
	private String timeType;
	private Integer publishId;
	private String datasetAlternateName;
	private Integer datasetCharacterSet;
	private String datasetUsage;
	private Integer metadataCharacterSet;
	private String metadataStandardName;
	private String metadataStandardVersion;
	private Integer metadataLanguage;
	private Double verticalExtentMinimum;
	private Double verticalExtentMaximum;
	private Integer verticalExtentUnit;
	private Integer verticalExtentVdatum;
	private Integer dataLanguage;
	private String fees;
	private String orderingInstructions;

	private Set t012ObjObjs = new HashSet();
	private Set t012ObjAdrs = new HashSet();

/**
	private Set t0111FunctionalCategorys = new HashSet();
	private Set t0111ThematicCategorys = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysCodelistDomains = new HashSet();
	private Set sysLanguages = new HashSet();
	private Set sysLanguages = new HashSet();
	private Set t0110AvailFormats = new HashSet();
	private Set t0112MediaOptions = new HashSet();
	private Set t0113DatasetReferences = new HashSet();
	private Set t011ObjDatas = new HashSet();
	private Set t011ObjGeos = new HashSet();
	private Set t011ObjLiteraturs = new HashSet();
	private Set t011ObjProjects = new HashSet();
	private Set t011ObjServs = new HashSet();
	private Set t014InfoImparts = new HashSet();
	private Set t015Legists = new HashSet();
	private Set t017UrlRefs = new HashSet();
	private Set t019Coordinatess = new HashSet();
	private Set t01StClasss = new HashSet();
	private Set t03Catalogues = new HashSet();
	private Set t08Attrs = new HashSet();
*/
	public T01Object() {
		super();
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public Integer getObjClass() {
		return objClass;
	}

	public void setObjClass(Integer objClass) {
		this.objClass = objClass;
	}

	public String getObjDescr() {
		return objDescr;
	}

	public void setObjDescr(String objDescr) {
		this.objDescr = objDescr;
	}

	public String getInfoNote() {
		return infoNote;
	}

	public void setInfoNote(String infoNote) {
		this.infoNote = infoNote;
	}

	public String getAvailAccessNote() {
		return availAccessNote;
	}

	public void setAvailAccessNote(String availAccessNote) {
		this.availAccessNote = availAccessNote;
	}

	public String getLocDescr() {
		return locDescr;
	}

	public void setLocDescr(String locDescr) {
		this.locDescr = locDescr;
	}

	public String getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(String timeTo) {
		this.timeTo = timeTo;
	}

	public String getTimeDescr() {
		return timeDescr;
	}

	public void setTimeDescr(String timeDescr) {
		this.timeDescr = timeDescr;
	}

	public Integer getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(Integer timePeriod) {
		this.timePeriod = timePeriod;
	}

	public String getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(String timeInterval) {
		this.timeInterval = timeInterval;
	}

	public Integer getTimeStatus() {
		return timeStatus;
	}

	public void setTimeStatus(Integer timeStatus) {
		this.timeStatus = timeStatus;
	}

	public String getTimeAlle() {
		return timeAlle;
	}

	public void setTimeAlle(String timeAlle) {
		this.timeAlle = timeAlle;
	}

	public String getTimeType() {
		return timeType;
	}

	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}

	public Integer getPublishId() {
		return publishId;
	}

	public void setPublishId(Integer publishId) {
		this.publishId = publishId;
	}

	public String getDatasetAlternateName() {
		return datasetAlternateName;
	}

	public void setDatasetAlternateName(String datasetAlternateName) {
		this.datasetAlternateName = datasetAlternateName;
	}

	public Integer getDatasetCharacterSet() {
		return datasetCharacterSet;
	}

	public void setDatasetCharacterSet(Integer datasetCharacterSet) {
		this.datasetCharacterSet = datasetCharacterSet;
	}

	public String getDatasetUsage() {
		return datasetUsage;
	}

	public void setDatasetUsage(String datasetUsage) {
		this.datasetUsage = datasetUsage;
	}

	public Integer getMetadataCharacterSet() {
		return metadataCharacterSet;
	}

	public void setMetadataCharacterSet(Integer metadataCharacterSet) {
		this.metadataCharacterSet = metadataCharacterSet;
	}

	public String getMetadataStandardName() {
		return metadataStandardName;
	}

	public void setMetadataStandardName(String metadataStandardName) {
		this.metadataStandardName = metadataStandardName;
	}

	public String getMetadataStandardVersion() {
		return metadataStandardVersion;
	}

	public void setMetadataStandardVersion(String metadataStandardVersion) {
		this.metadataStandardVersion = metadataStandardVersion;
	}

	public Integer getMetadataLanguage() {
		return metadataLanguage;
	}

	public void setMetadataLanguage(Integer metadataLanguage) {
		this.metadataLanguage = metadataLanguage;
	}

	public Double getVerticalExtentMinimum() {
		return verticalExtentMinimum;
	}

	public void setVerticalExtentMinimum(Double verticalExtentMinimum) {
		this.verticalExtentMinimum = verticalExtentMinimum;
	}

	public Double getVerticalExtentMaximum() {
		return verticalExtentMaximum;
	}

	public void setVerticalExtentMaximum(Double verticalExtentMaximum) {
		this.verticalExtentMaximum = verticalExtentMaximum;
	}

	public Integer getVerticalExtentUnit() {
		return verticalExtentUnit;
	}

	public void setVerticalExtentUnit(Integer verticalExtentUnit) {
		this.verticalExtentUnit = verticalExtentUnit;
	}

	public Integer getVerticalExtentVdatum() {
		return verticalExtentVdatum;
	}

	public void setVerticalExtentVdatum(Integer verticalExtentVdatum) {
		this.verticalExtentVdatum = verticalExtentVdatum;
	}

	public Integer getDataLanguage() {
		return dataLanguage;
	}

	public void setDataLanguage(Integer dataLanguage) {
		this.dataLanguage = dataLanguage;
	}

	public String getFees() {
		return fees;
	}

	public void setFees(String fees) {
		this.fees = fees;
	}

	public String getOrderingInstructions() {
		return orderingInstructions;
	}

	public void setOrderingInstructions(String orderingInstructions) {
		this.orderingInstructions = orderingInstructions;
	}

	// ASSOCIATIONS

	public Set getT012ObjObjs() {
		return t012ObjObjs;
	}
	public void setT012ObjObjs(Set t012ObjObjs) {
		this.t012ObjObjs = t012ObjObjs;
	}

	public Set getT012ObjAdrs() {
		return t012ObjAdrs;
	}
	public void setT012ObjAdrs(Set t012ObjAdrs) {
		this.t012ObjAdrs = t012ObjAdrs;
	}

/**
	public Set getT0111FunctionalCategorys() {
		return t0111FunctionalCategorys;
	}

	public void setT0111FunctionalCategorys(Set t0111FunctionalCategorys) {
		this.t0111FunctionalCategorys = t0111FunctionalCategorys;
	}

	public Set getT0111ThematicCategorys() {
		return t0111ThematicCategorys;
	}

	public void setT0111ThematicCategorys(Set t0111ThematicCategorys) {
		this.t0111ThematicCategorys = t0111ThematicCategorys;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysCodelistDomains() {
		return sysCodelistDomains;
	}

	public void setSysCodelistDomains(Set sysCodelistDomains) {
		this.sysCodelistDomains = sysCodelistDomains;
	}

	public Set getSysLanguages() {
		return sysLanguages;
	}

	public void setSysLanguages(Set sysLanguages) {
		this.sysLanguages = sysLanguages;
	}

	public Set getSysLanguages() {
		return sysLanguages;
	}

	public void setSysLanguages(Set sysLanguages) {
		this.sysLanguages = sysLanguages;
	}

	public Set getT0110AvailFormats() {
		return t0110AvailFormats;
	}

	public void setT0110AvailFormats(Set t0110AvailFormats) {
		this.t0110AvailFormats = t0110AvailFormats;
	}

	public Set getT0112MediaOptions() {
		return t0112MediaOptions;
	}

	public void setT0112MediaOptions(Set t0112MediaOptions) {
		this.t0112MediaOptions = t0112MediaOptions;
	}

	public Set getT0113DatasetReferences() {
		return t0113DatasetReferences;
	}

	public void setT0113DatasetReferences(Set t0113DatasetReferences) {
		this.t0113DatasetReferences = t0113DatasetReferences;
	}

	public Set getT011ObjDatas() {
		return t011ObjDatas;
	}

	public void setT011ObjDatas(Set t011ObjDatas) {
		this.t011ObjDatas = t011ObjDatas;
	}

	public Set getT011ObjGeos() {
		return t011ObjGeos;
	}

	public void setT011ObjGeos(Set t011ObjGeos) {
		this.t011ObjGeos = t011ObjGeos;
	}

	public Set getT011ObjLiteraturs() {
		return t011ObjLiteraturs;
	}

	public void setT011ObjLiteraturs(Set t011ObjLiteraturs) {
		this.t011ObjLiteraturs = t011ObjLiteraturs;
	}

	public Set getT011ObjProjects() {
		return t011ObjProjects;
	}

	public void setT011ObjProjects(Set t011ObjProjects) {
		this.t011ObjProjects = t011ObjProjects;
	}

	public Set getT011ObjServs() {
		return t011ObjServs;
	}

	public void setT011ObjServs(Set t011ObjServs) {
		this.t011ObjServs = t011ObjServs;
	}

	public Set getT014InfoImparts() {
		return t014InfoImparts;
	}

	public void setT014InfoImparts(Set t014InfoImparts) {
		this.t014InfoImparts = t014InfoImparts;
	}

	public Set getT015Legists() {
		return t015Legists;
	}

	public void setT015Legists(Set t015Legists) {
		this.t015Legists = t015Legists;
	}

	public Set getT017UrlRefs() {
		return t017UrlRefs;
	}

	public void setT017UrlRefs(Set t017UrlRefs) {
		this.t017UrlRefs = t017UrlRefs;
	}

	public Set getT019Coordinatess() {
		return t019Coordinatess;
	}

	public void setT019Coordinatess(Set t019Coordinatess) {
		this.t019Coordinatess = t019Coordinatess;
	}

	public Set getT01StClasss() {
		return t01StClasss;
	}

	public void setT01StClasss(Set t01StClasss) {
		this.t01StClasss = t01StClasss;
	}

	public Set getT03Catalogues() {
		return t03Catalogues;
	}

	public void setT03Catalogues(Set t03Catalogues) {
		this.t03Catalogues = t03Catalogues;
	}

	public Set getT08Attrs() {
		return t08Attrs;
	}

	public void setT08Attrs(Set t08Attrs) {
		this.t08Attrs = t08Attrs;
	}
*/
}