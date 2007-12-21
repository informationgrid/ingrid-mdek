package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;


/**
 * Abstract superclass of all hibernate beans encapsulating common data.
 *
 * @author Martin
 */
public abstract class MdekDbEntity implements IEntity, java.io.Serializable {

	private Long id;
	private int version;

	protected MdekDbEntity() {
	}

	public Long getId() {
		return id;
	}
	protected void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
        return version;
    }
	public void setVersion(int version) {
        this.version = version;
    }
}