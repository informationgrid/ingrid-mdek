package de.ingrid.mdek.services.persistence.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Metadata implements IEntity {

	@Id
	private String _metadataKey;

	private String _metadataValue;

	@Version
    private Integer _version;

	public Metadata() {
		// hibernate
	}

	public Metadata(String metadatKey, String metadataValue) {
		_metadataKey = metadatKey;
		_metadataValue = metadataValue;
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

	public Integer getVersion() {
        return _version;
    }
	protected void setVersion(Integer version) {
        this._version = version;
    }

    public String getId() {
        return getMetadataKey();
    }
}
