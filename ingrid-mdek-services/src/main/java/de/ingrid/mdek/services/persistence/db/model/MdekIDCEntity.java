package de.ingrid.mdek.services.persistence.db.model;


/**
 * Abstract class further encapsulating common Object/Address data.
 *
 * @author Martin
 */
public abstract class MdekIDCEntity extends MdekDbEntity {

	private Long catId;
	private Integer root;

	private String lastexportTime;
	private String expiryTime;
	private String workState;
	private Integer workVersion;
	private String markDeleted;
	private String createTime;
	private String modTime;
	private Long modId;
	private Long responsibleId;

	public MdekIDCEntity() {
		super();
	}

	public Long getCatId() {
		return catId;
	}
	public void setCatId(Long catId) {
		this.catId = catId;
	}

	public Integer getRoot() {
		return root;
	}
	public void setRoot(Integer root) {
		this.root = root;
	}

	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Long getModId() {
		return modId;
	}
	public void setModId(Long modId) {
		this.modId = modId;
	}

	public String getModTime() {
		return modTime;
	}
	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

	public String getLastexportTime() {
		return lastexportTime;
	}
	public void setLastexportTime(String lastexportTime) {
		this.lastexportTime = lastexportTime;
	}

	public String getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

	public String getWorkState() {
		return workState;
	}
	public void setWorkState(String workState) {
		this.workState = workState;
	}

	public Integer getWorkVersion() {
		return workVersion;
	}
	public void setWorkVersion(Integer workVersion) {
		this.workVersion = workVersion;
	}

	public String getMarkDeleted() {
		return markDeleted;
	}
	public void setMarkDeleted(String markDeleted) {
		this.markDeleted = markDeleted;
	}

	public Long getResponsibleId() {
		return responsibleId;
	}
	public void setResponsibleId(Long responsibleId) {
		this.responsibleId = responsibleId;
	}
}