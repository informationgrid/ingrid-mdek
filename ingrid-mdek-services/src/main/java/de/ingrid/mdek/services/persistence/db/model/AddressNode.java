package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class AddressNode implements IEntity {

	private Long id;
	private int version;
	private String addrUuid;
	private Long addrId;
	private Long addrIdPublished;
	private String fkAddrUuid;

	private T02Address t02AddressWork;
	private T02Address t02AddressPublished;
	private Set addressNodeChildren = new HashSet();
	private Set t012ObjAdrs = new HashSet();

	public AddressNode() {}

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

	public String getAddrUuid() {
		return addrUuid;
	}

	public void setAddrUuid(String addrUuid) {
		this.addrUuid = addrUuid;
	}

	public Long getAddrId() {
		return addrId;
	}

	public void setAddrId(Long addrId) {
		this.addrId = addrId;
	}

	public Long getAddrIdPublished() {
		return addrIdPublished;
	}

	public void setAddrIdPublished(Long addrIdPublished) {
		this.addrIdPublished = addrIdPublished;
	}

	public String getFkAddrUuid() {
		return fkAddrUuid;
	}

	public void setFkAddrUuid(String fkAddrUuid) {
		this.fkAddrUuid = fkAddrUuid;
	}


	public T02Address getT02AddressWork() {
		return t02AddressWork;
	}

	public void setT02AddressWork(T02Address t02AddressWork) {
		this.t02AddressWork = t02AddressWork;
	}

	public T02Address getT02AddressPublished() {
		return t02AddressPublished;
	}

	public void setT02AddressPublished(T02Address t02AddressPublished) {
		this.t02AddressPublished = t02AddressPublished;
	}

	public Set getAddressNodeChildren() {
		return addressNodeChildren;
	}

	public void setAddressNodeChildren(Set addressNodeChildren) {
		this.addressNodeChildren = addressNodeChildren;
	}

	public Set getT012ObjAdrs() {
		return t012ObjAdrs;
	}

	public void setT012ObjAdrs(Set t012ObjAdrs) {
		this.t012ObjAdrs = t012ObjAdrs;
	}

}