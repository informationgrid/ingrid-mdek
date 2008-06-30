package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T08AttrType implements IEntity {

	private Long id;
	private int version;
	private String name;
	private Integer length;
	private String type;

	private Set t08AttrLists = new HashSet();

	public T08AttrType() {}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public Set getT08AttrLists() {
		return t08AttrLists;
	}

	public void setT08AttrLists(Set t08AttrLists) {
		this.t08AttrLists = t08AttrLists;
	}

}