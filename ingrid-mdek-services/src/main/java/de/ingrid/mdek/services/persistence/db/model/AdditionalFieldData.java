package de.ingrid.mdek.services.persistence.db.model;

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class AdditionalFieldData implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer sort;
	private String fieldKey;
	private String listItemId;
	private String data;
	private Long parentFieldId;

	private Set additionalFieldDatas = new HashSet();

	public AdditionalFieldData() {}

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

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public void setFieldKey(String fieldKey) {
		this.fieldKey = fieldKey;
	}

	public String getListItemId() {
		return listItemId;
	}

	public void setListItemId(String listItemId) {
		this.listItemId = listItemId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getParentFieldId() {
		return parentFieldId;
	}

	public void setParentFieldId(Long parentFieldId) {
		this.parentFieldId = parentFieldId;
	}


	public Set getAdditionalFieldDatas() {
		return additionalFieldDatas;
	}

	public void setAdditionalFieldDatas(Set additionalFieldDatas) {
		this.additionalFieldDatas = additionalFieldDatas;
	}

}