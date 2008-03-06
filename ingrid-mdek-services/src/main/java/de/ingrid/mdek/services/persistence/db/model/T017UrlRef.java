package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T017UrlRef implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String urlLink;
	private Integer specialRef;
	private String specialName;
	private String content;
	private Integer datatypeKey;
	private String datatypeValue;
	private String volume;
	private String icon;
	private String iconText;
	private String descr;
	private Integer urlType;


	public T017UrlRef() {}

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

	public String getUrlLink() {
		return urlLink;
	}

	public void setUrlLink(String urlLink) {
		this.urlLink = urlLink;
	}

	public Integer getSpecialRef() {
		return specialRef;
	}

	public void setSpecialRef(Integer specialRef) {
		this.specialRef = specialRef;
	}

	public String getSpecialName() {
		return specialName;
	}

	public void setSpecialName(String specialName) {
		this.specialName = specialName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getDatatypeKey() {
		return datatypeKey;
	}

	public void setDatatypeKey(Integer datatypeKey) {
		this.datatypeKey = datatypeKey;
	}

	public String getDatatypeValue() {
		return datatypeValue;
	}

	public void setDatatypeValue(String datatypeValue) {
		this.datatypeValue = datatypeValue;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIconText() {
		return iconText;
	}

	public void setIconText(String iconText) {
		this.iconText = iconText;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public Integer getUrlType() {
		return urlType;
	}

	public void setUrlType(Integer urlType) {
		this.urlType = urlType;
	}


}