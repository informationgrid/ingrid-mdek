/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import java.util.HashSet;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class SearchtermValue implements IEntity {

	private Long id;
	private int version;
	private String type;
	private Long searchtermSnsId;
	private Integer entryId;
	private String term;
	private String alternateTerm;

	private SearchtermSns searchtermSns;

	public SearchtermValue() {}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getSearchtermSnsId() {
		return searchtermSnsId;
	}

	public void setSearchtermSnsId(Long searchtermSnsId) {
		this.searchtermSnsId = searchtermSnsId;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getAlternateTerm() {
		return alternateTerm;
	}

	public void setAlternateTerm(String alternateTerm) {
		this.alternateTerm = alternateTerm;
	}


	public SearchtermSns getSearchtermSns() {
		return searchtermSns;
	}

	public void setSearchtermSns(SearchtermSns searchtermSns) {
		this.searchtermSns = searchtermSns;
	}

}
