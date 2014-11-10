/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
