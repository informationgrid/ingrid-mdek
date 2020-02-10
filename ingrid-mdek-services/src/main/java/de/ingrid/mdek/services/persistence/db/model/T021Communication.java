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

public class T021Communication implements IEntity {

	private Long id;
	private int version;
	private Long adrId;
	private Integer line;
	private Integer commtypeKey;
	private String commtypeValue;
	private String commValue;
	private String descr;


	public T021Communication() {}

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

	public Long getAdrId() {
		return adrId;
	}

	public void setAdrId(Long adrId) {
		this.adrId = adrId;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getCommtypeKey() {
		return commtypeKey;
	}

	public void setCommtypeKey(Integer commtypeKey) {
		this.commtypeKey = commtypeKey;
	}

	public String getCommtypeValue() {
		return commtypeValue;
	}

	public void setCommtypeValue(String commtypeValue) {
		this.commtypeValue = commtypeValue;
	}

	public String getCommValue() {
		return commValue;
	}

	public void setCommValue(String commValue) {
		this.commValue = commValue;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}


}
