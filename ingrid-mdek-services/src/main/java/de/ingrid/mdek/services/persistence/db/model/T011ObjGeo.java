package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjGeo implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private String specialBase;
	private String dataBase;
	private String method;
	private Double recExact;
	private Double recGrade;
	private Integer hierarchyLevel;
	private Integer vectorTopologyLevel;
	private Double posAccuracyVertical;
	private Integer keycInclWDataset;
	private String datasourceUuid;

	private Set t011ObjGeoKeycs = new HashSet();
	private Set t011ObjGeoScales = new HashSet();
	private Set t011ObjGeoSpatialReps = new HashSet();
	private Set t011ObjGeoSupplinfos = new HashSet();
	private Set t011ObjGeoSymcs = new HashSet();
	private Set t011ObjGeoVectors = new HashSet();

	public T011ObjGeo() {}

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

	public String getSpecialBase() {
		return specialBase;
	}

	public void setSpecialBase(String specialBase) {
		this.specialBase = specialBase;
	}

	public String getDataBase() {
		return dataBase;
	}

	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Double getRecExact() {
		return recExact;
	}

	public void setRecExact(Double recExact) {
		this.recExact = recExact;
	}

	public Double getRecGrade() {
		return recGrade;
	}

	public void setRecGrade(Double recGrade) {
		this.recGrade = recGrade;
	}

	public Integer getHierarchyLevel() {
		return hierarchyLevel;
	}

	public void setHierarchyLevel(Integer hierarchyLevel) {
		this.hierarchyLevel = hierarchyLevel;
	}

	public Integer getVectorTopologyLevel() {
		return vectorTopologyLevel;
	}

	public void setVectorTopologyLevel(Integer vectorTopologyLevel) {
		this.vectorTopologyLevel = vectorTopologyLevel;
	}

	public Double getPosAccuracyVertical() {
		return posAccuracyVertical;
	}

	public void setPosAccuracyVertical(Double posAccuracyVertical) {
		this.posAccuracyVertical = posAccuracyVertical;
	}

	public Integer getKeycInclWDataset() {
		return keycInclWDataset;
	}

	public void setKeycInclWDataset(Integer keycInclWDataset) {
		this.keycInclWDataset = keycInclWDataset;
	}

	public String getDatasourceUuid() {
		return datasourceUuid;
	}

	public void setDatasourceUuid(String datasourceUuid) {
		this.datasourceUuid = datasourceUuid;
	}


	public Set getT011ObjGeoKeycs() {
		return t011ObjGeoKeycs;
	}

	public void setT011ObjGeoKeycs(Set t011ObjGeoKeycs) {
		this.t011ObjGeoKeycs = t011ObjGeoKeycs;
	}

	public Set getT011ObjGeoScales() {
		return t011ObjGeoScales;
	}

	public void setT011ObjGeoScales(Set t011ObjGeoScales) {
		this.t011ObjGeoScales = t011ObjGeoScales;
	}

	public Set getT011ObjGeoSpatialReps() {
		return t011ObjGeoSpatialReps;
	}

	public void setT011ObjGeoSpatialReps(Set t011ObjGeoSpatialReps) {
		this.t011ObjGeoSpatialReps = t011ObjGeoSpatialReps;
	}

	public Set getT011ObjGeoSupplinfos() {
		return t011ObjGeoSupplinfos;
	}

	public void setT011ObjGeoSupplinfos(Set t011ObjGeoSupplinfos) {
		this.t011ObjGeoSupplinfos = t011ObjGeoSupplinfos;
	}

	public Set getT011ObjGeoSymcs() {
		return t011ObjGeoSymcs;
	}

	public void setT011ObjGeoSymcs(Set t011ObjGeoSymcs) {
		this.t011ObjGeoSymcs = t011ObjGeoSymcs;
	}

	public Set getT011ObjGeoVectors() {
		return t011ObjGeoVectors;
	}

	public void setT011ObjGeoVectors(Set t011ObjGeoVectors) {
		this.t011ObjGeoVectors = t011ObjGeoVectors;
	}

}