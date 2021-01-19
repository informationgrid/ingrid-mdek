/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

public class ObjectConformity implements IEntity {

	private Long id;
	private int version;
	private Long objId;
	private Integer line;
	private String isInspire;
	private Integer specificationKey;
	private String specificationValue;
	private Integer degreeKey;
	private String degreeValue;
	private String publicationDate;
	private String explanation;


	public ObjectConformity() {}

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

	public String getIsInspire() {
		return isInspire;
	}

	public void setIsInspire(String isInspire) {
		this.isInspire = isInspire;
	}

	public Integer getSpecificationKey() {
		return specificationKey;
	}

	public void setSpecificationKey(Integer specificationKey) {
		this.specificationKey = specificationKey;
	}

	public String getSpecificationValue() {
		return specificationValue;
	}

	public void setSpecificationValue(String specificationValue) {
		this.specificationValue = specificationValue;
	}

	public Integer getDegreeKey() {
		return degreeKey;
	}

	public void setDegreeKey(Integer degreeKey) {
		this.degreeKey = degreeKey;
	}

	public String getDegreeValue() {
		return degreeValue;
	}

	public void setDegreeValue(String degreeValue) {
		this.degreeValue = degreeValue;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}


}
