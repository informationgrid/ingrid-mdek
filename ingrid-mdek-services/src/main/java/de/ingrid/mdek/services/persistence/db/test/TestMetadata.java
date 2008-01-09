package de.ingrid.mdek.services.persistence.db.test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import de.ingrid.mdek.services.persistence.db.IEntity;

@Entity
public class TestMetadata implements IEntity, java.io.Serializable {

	@Id
    private Long id;
	private String _metadataKey;
	private String _metadataValue;
	@Version
    private int _version;

	public TestMetadata() {
		// hibernate
	}

	public TestMetadata(String metadatKey, String metadataValue) {
		_metadataKey = metadatKey;
		_metadataValue = metadataValue;
	}

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

	public String getMetadataValue() {
		return _metadataValue;
	}
	public void setMetadataValue(String value) {
		_metadataValue = value;
	}

	public String getMetadataKey() {
		return _metadataKey;
	}
	public void setMetadataKey(String key) {
		_metadataKey = key;
	}
	
	@Override
	public String toString() {
		return _metadataKey + "#" + _metadataValue;
	}

	public int getVersion() {
        return _version;
    }
	protected void setVersion(int version) {
        this._version = version;
    }
}
