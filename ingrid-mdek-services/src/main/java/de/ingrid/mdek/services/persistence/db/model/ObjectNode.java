package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectNode implements IEntity {

	private Long id;
	private int version;
	private String objUuid;
	private Long objId;
	private Long objIdPublished;
	private String fkObjUuid;

	private Set fullIndexObjs = new HashSet();
	private T01Object t01ObjectWork;
	private Set objectNodeChildren = new HashSet();
	private T01Object t01ObjectPublished;

	public ObjectNode() {}

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

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}

	public Long getObjIdPublished() {
		return objIdPublished;
	}

	public void setObjIdPublished(Long objIdPublished) {
		this.objIdPublished = objIdPublished;
	}

	public String getFkObjUuid() {
		return fkObjUuid;
	}

	public void setFkObjUuid(String fkObjUuid) {
		this.fkObjUuid = fkObjUuid;
	}


	public Set getFullIndexObjs() {
		return fullIndexObjs;
	}

	public void setFullIndexObjs(Set fullIndexObjs) {
		this.fullIndexObjs = fullIndexObjs;
	}

	public T01Object getT01ObjectWork() {
		return t01ObjectWork;
	}

	public void setT01ObjectWork(T01Object t01ObjectWork) {
		this.t01ObjectWork = t01ObjectWork;
	}

	public Set getObjectNodeChildren() {
		return objectNodeChildren;
	}

	public void setObjectNodeChildren(Set objectNodeChildren) {
		this.objectNodeChildren = objectNodeChildren;
	}

	public T01Object getT01ObjectPublished() {
		return t01ObjectPublished;
	}

	public void setT01ObjectPublished(T01Object t01ObjectPublished) {
		this.t01ObjectPublished = t01ObjectPublished;
	}

}