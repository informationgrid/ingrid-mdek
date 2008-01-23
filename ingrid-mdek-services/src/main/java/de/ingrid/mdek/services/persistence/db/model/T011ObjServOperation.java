package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T011ObjServOperation implements IEntity {

	private Long id;
	private int version;
	private Long objServId;
	private Integer line;
	private String name;
	private String descr;
	private String invocationName;

	private Set t011ObjServOpConnpoints = new HashSet();
	private Set t011ObjServOpDependss = new HashSet();
	private Set t011ObjServOpParas = new HashSet();
	private Set t011ObjServOpPlatforms = new HashSet();

	public T011ObjServOperation() {}

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

	public Long getObjServId() {
		return objServId;
	}

	public void setObjServId(Long objServId) {
		this.objServId = objServId;
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

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getInvocationName() {
		return invocationName;
	}

	public void setInvocationName(String invocationName) {
		this.invocationName = invocationName;
	}


	public Set getT011ObjServOpConnpoints() {
		return t011ObjServOpConnpoints;
	}

	public void setT011ObjServOpConnpoints(Set t011ObjServOpConnpoints) {
		this.t011ObjServOpConnpoints = t011ObjServOpConnpoints;
	}

	public Set getT011ObjServOpDependss() {
		return t011ObjServOpDependss;
	}

	public void setT011ObjServOpDependss(Set t011ObjServOpDependss) {
		this.t011ObjServOpDependss = t011ObjServOpDependss;
	}

	public Set getT011ObjServOpParas() {
		return t011ObjServOpParas;
	}

	public void setT011ObjServOpParas(Set t011ObjServOpParas) {
		this.t011ObjServOpParas = t011ObjServOpParas;
	}

	public Set getT011ObjServOpPlatforms() {
		return t011ObjServOpPlatforms;
	}

	public void setT011ObjServOpPlatforms(Set t011ObjServOpPlatforms) {
		this.t011ObjServOpPlatforms = t011ObjServOpPlatforms;
	}

}