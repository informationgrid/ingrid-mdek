package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;


/**
 * Abstract superclass of all hibernate beans encapsulating common data.
 *
 * @author Martin
 */
public abstract class MdekDbEntity implements IEntity, java.io.Serializable {

	private String id;
	private long version;

	protected MdekDbEntity() {
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public long getVersion() {
        return version;
    }

	protected void setVersion(long version) {
        this.version = version;
    }
	
	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.persistence.db.IEntity#getTimestamp()
	 */
	public long getTimestamp() {
		return version;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.persistence.db.IEntity#setTimestamp(long)
	 */
	public void setTimestamp(long timestamp) {
        this.version = timestamp;		
	}

    /* (non-Javadoc)
     * @see de.ingrid.mdek.services.persistence.db.IEntity#getID()
     */
    public String getID() {
        return getId();
    }
}