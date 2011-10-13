package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T01Object implements IEntity {

	private Long id;
	private int version;
	private String objUuid;
	private String objName;
	private String orgObjId;
	private Integer objClass;
	private String objDescr;
	private Long catId;
	private String infoNote;
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
	private Integer dataLanguageKey;
	private String dataLanguageValue;
	private Integer metadataCharacterSet;
	private String metadataStandardName;
	private String metadataStandardVersion;
	private Integer metadataLanguageKey;
	private String metadataLanguageValue;
	private Double verticalExtentMinimum;
	private Double verticalExtentMaximum;
	private Integer verticalExtentUnit;
	private Integer verticalExtentVdatumKey;
	private String verticalExtentVdatumValue;
	private String orderingInstructions;
	private String isCatalogData;
	private String isInspireRelevant;
	private String workState;
	private String createTime;
	private Long objMetadataId;
	private String modTime;
	private String modUuid;
	private String responsibleUuid;

	private Set additionalFieldDatas = new HashSet();
	private Set objectAccesss = new HashSet();
	private Set objectComments = new HashSet();
	private Set objectConformitys = new HashSet();
	private Set objectDataQualitys = new HashSet();
	private Set objectFormatInspires = new HashSet();
	private Set objectReferences = new HashSet();
	private Set objectUses = new HashSet();
	private Set searchtermObjs = new HashSet();
	private Set spatialReferences = new HashSet();
	private Set spatialSystems = new HashSet();
	private Set t0110AvailFormats = new HashSet();
	private Set t0112MediaOptions = new HashSet();
	private Set t0113DatasetReferences = new HashSet();
	private Set t0114EnvTopics = new HashSet();
	private Set t011ObjDatas = new HashSet();
	private Set t011ObjDataParas = new HashSet();
	private Set t011ObjGeos = new HashSet();
	private Set t011ObjLiteratures = new HashSet();
	private Set t011ObjProjects = new HashSet();
	private Set t011ObjServs = new HashSet();
	private Set t011ObjTopicCats = new HashSet();
	private Set t012ObjAdrs = new HashSet();
	private Set t014InfoImparts = new HashSet();
	private Set t015Legists = new HashSet();
	private Set t017UrlRefs = new HashSet();
	private T03Catalogue t03Catalogue;
	private AddressNode addressNodeResponsible;
	private AddressNode addressNodeMod;
	private ObjectMetadata objectMetadata;

	public T01Object() {}

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

	public String getObjUuid() {
		return objUuid;
	}

	public void setObjUuid(String objUuid) {
		this.objUuid = objUuid;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public String getOrgObjId() {
		return orgObjId;
	}

	public void setOrgObjId(String orgObjId) {
		this.orgObjId = orgObjId;
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

	public Long getCatId() {
		return catId;
	}

	public void setCatId(Long catId) {
		this.catId = catId;
	}

	public String getInfoNote() {
		return infoNote;
	}

	public void setInfoNote(String infoNote) {
		this.infoNote = infoNote;
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

	public Integer getDataLanguageKey() {
		return dataLanguageKey;
	}

	public void setDataLanguageKey(Integer dataLanguageKey) {
		this.dataLanguageKey = dataLanguageKey;
	}

	public String getDataLanguageValue() {
		return dataLanguageValue;
	}

	public void setDataLanguageValue(String dataLanguageValue) {
		this.dataLanguageValue = dataLanguageValue;
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

	public Integer getMetadataLanguageKey() {
		return metadataLanguageKey;
	}

	public void setMetadataLanguageKey(Integer metadataLanguageKey) {
		this.metadataLanguageKey = metadataLanguageKey;
	}

	public String getMetadataLanguageValue() {
		return metadataLanguageValue;
	}

	public void setMetadataLanguageValue(String metadataLanguageValue) {
		this.metadataLanguageValue = metadataLanguageValue;
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

	public Integer getVerticalExtentVdatumKey() {
		return verticalExtentVdatumKey;
	}

	public void setVerticalExtentVdatumKey(Integer verticalExtentVdatumKey) {
		this.verticalExtentVdatumKey = verticalExtentVdatumKey;
	}

	public String getVerticalExtentVdatumValue() {
		return verticalExtentVdatumValue;
	}

	public void setVerticalExtentVdatumValue(String verticalExtentVdatumValue) {
		this.verticalExtentVdatumValue = verticalExtentVdatumValue;
	}

	public String getOrderingInstructions() {
		return orderingInstructions;
	}

	public void setOrderingInstructions(String orderingInstructions) {
		this.orderingInstructions = orderingInstructions;
	}

	public String getIsCatalogData() {
		return isCatalogData;
	}

	public void setIsCatalogData(String isCatalogData) {
		this.isCatalogData = isCatalogData;
	}

	public String getIsInspireRelevant() {
		return isInspireRelevant;
	}

	public void setIsInspireRelevant(String isInspireRelevant) {
		this.isInspireRelevant = isInspireRelevant;
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

	public Long getObjMetadataId() {
		return objMetadataId;
	}

	public void setObjMetadataId(Long objMetadataId) {
		this.objMetadataId = objMetadataId;
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


	public Set getAdditionalFieldDatas() {
		return additionalFieldDatas;
	}

	public void setAdditionalFieldDatas(Set additionalFieldDatas) {
		this.additionalFieldDatas = additionalFieldDatas;
	}

	public Set getObjectAccesss() {
		return objectAccesss;
	}

	public void setObjectAccesss(Set objectAccesss) {
		this.objectAccesss = objectAccesss;
	}

	public Set getObjectComments() {
		return objectComments;
	}

	public void setObjectComments(Set objectComments) {
		this.objectComments = objectComments;
	}

	public Set getObjectConformitys() {
		return objectConformitys;
	}

	public void setObjectConformitys(Set objectConformitys) {
		this.objectConformitys = objectConformitys;
	}

	public Set getObjectDataQualitys() {
		return objectDataQualitys;
	}

	public void setObjectDataQualitys(Set objectDataQualitys) {
		this.objectDataQualitys = objectDataQualitys;
	}

	public Set getObjectFormatInspires() {
		return objectFormatInspires;
	}

	public void setObjectFormatInspires(Set objectFormatInspires) {
		this.objectFormatInspires = objectFormatInspires;
	}

	public Set getObjectReferences() {
		return objectReferences;
	}

	public void setObjectReferences(Set objectReferences) {
		this.objectReferences = objectReferences;
	}

	public Set getObjectUses() {
		return objectUses;
	}

	public void setObjectUses(Set objectUses) {
		this.objectUses = objectUses;
	}

	public Set getSearchtermObjs() {
		return searchtermObjs;
	}

	public void setSearchtermObjs(Set searchtermObjs) {
		this.searchtermObjs = searchtermObjs;
	}

	public Set getSpatialReferences() {
		return spatialReferences;
	}

	public void setSpatialReferences(Set spatialReferences) {
		this.spatialReferences = spatialReferences;
	}

	public Set getSpatialSystems() {
		return spatialSystems;
	}

	public void setSpatialSystems(Set spatialSystems) {
		this.spatialSystems = spatialSystems;
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

	public Set getT0114EnvTopics() {
		return t0114EnvTopics;
	}

	public void setT0114EnvTopics(Set t0114EnvTopics) {
		this.t0114EnvTopics = t0114EnvTopics;
	}

	public Set getT011ObjDatas() {
		return t011ObjDatas;
	}

	public void setT011ObjDatas(Set t011ObjDatas) {
		this.t011ObjDatas = t011ObjDatas;
	}

	public Set getT011ObjDataParas() {
		return t011ObjDataParas;
	}

	public void setT011ObjDataParas(Set t011ObjDataParas) {
		this.t011ObjDataParas = t011ObjDataParas;
	}

	public Set getT011ObjGeos() {
		return t011ObjGeos;
	}

	public void setT011ObjGeos(Set t011ObjGeos) {
		this.t011ObjGeos = t011ObjGeos;
	}

	public Set getT011ObjLiteratures() {
		return t011ObjLiteratures;
	}

	public void setT011ObjLiteratures(Set t011ObjLiteratures) {
		this.t011ObjLiteratures = t011ObjLiteratures;
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

	public Set getT011ObjTopicCats() {
		return t011ObjTopicCats;
	}

	public void setT011ObjTopicCats(Set t011ObjTopicCats) {
		this.t011ObjTopicCats = t011ObjTopicCats;
	}

	public Set getT012ObjAdrs() {
		return t012ObjAdrs;
	}

	public void setT012ObjAdrs(Set t012ObjAdrs) {
		this.t012ObjAdrs = t012ObjAdrs;
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

	public T03Catalogue getT03Catalogue() {
		return t03Catalogue;
	}

	public void setT03Catalogue(T03Catalogue t03Catalogue) {
		this.t03Catalogue = t03Catalogue;
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

	public ObjectMetadata getObjectMetadata() {
		return objectMetadata;
	}

	public void setObjectMetadata(ObjectMetadata objectMetadata) {
		this.objectMetadata = objectMetadata;
	}

}