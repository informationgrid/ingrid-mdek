/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class T0112MediaOption implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String mediumNote;
	private Integer mediumName;
	private Double transferSize;


	public T0112MediaOption() {}

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

	public String getMediumNote() {
		return mediumNote;
	}

	public void setMediumNote(String mediumNote) {
		this.mediumNote = mediumNote;
	}

	public Integer getMediumName() {
		return mediumName;
	}

	public void setMediumName(Integer mediumName) {
		this.mediumName = mediumName;
	}

	public Double getTransferSize() {
		return transferSize;
	}

	public void setTransferSize(Double transferSize) {
		this.transferSize = transferSize;
	}


}
