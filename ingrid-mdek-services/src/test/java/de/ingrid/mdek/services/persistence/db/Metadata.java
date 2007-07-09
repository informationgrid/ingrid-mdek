package de.ingrid.mdek.services.persistence.db;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Metadata {

	@Id
	private String _metadataKey;

	private String _metadataValue;

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

	public String getMetadataKey() {
		return _metadataKey;
	}

	public void setMetadataKey(String key) {
		_metadataKey = key;
	}

	public void setMetadataValue(String value) {
		_metadataValue = value;
	}

	@Override
	public String toString() {
		return _metadataKey + "#" + _metadataValue;
	}
}
