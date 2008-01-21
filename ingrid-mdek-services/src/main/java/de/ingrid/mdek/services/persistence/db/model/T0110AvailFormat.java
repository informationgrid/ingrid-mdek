package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T0110AvailFormat implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String name;
	private String ver;
	private String fileDecompressionTechnique;
	private String specification;


	public T0110AvailFormat() {}

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

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public String getFileDecompressionTechnique() {
		return fileDecompressionTechnique;
	}

	public void setFileDecompressionTechnique(String fileDecompressionTechnique) {
		this.fileDecompressionTechnique = fileDecompressionTechnique;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}


}