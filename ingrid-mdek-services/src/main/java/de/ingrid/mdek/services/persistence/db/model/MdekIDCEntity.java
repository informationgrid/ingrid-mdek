package de.ingrid.mdek.services.persistence.db.model;


/**
 * Abstract class further encapsulating common Object/Address data.
 *
 * @author Martin
 */
public abstract class MdekIDCEntity extends MdekDbEntity {

	private String catId;
	private Integer root;

	private String createTime;
	private String modId;
	private String modTime;

	public MdekIDCEntity() {
		super();
	}

	public String getCatId() {
		return catId;
	}
	public void setCatId(String catId) {
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

	public String getModId() {
		return modId;
	}
	public void setModId(String modId) {
		this.modId = modId;
	}

	public String getModTime() {
		return modTime;
	}
	public void setModTime(String modTime) {
		this.modTime = modTime;
	}
}