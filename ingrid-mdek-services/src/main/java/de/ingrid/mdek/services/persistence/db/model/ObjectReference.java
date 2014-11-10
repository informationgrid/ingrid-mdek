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
package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.services.persistence.db.IEntity;

public class ObjectReference implements IEntity {

	private Long id;
	private int version;
	private Long objFromId;
	private String objToUuid;
	private Integer line;
	private Integer specialRef;
	private String specialName;
	private String descr;

	private ObjectNode objectNode;

	public ObjectReference() {}

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

	public Long getObjFromId() {
		return objFromId;
	}

	public void setObjFromId(Long objFromId) {
		this.objFromId = objFromId;
	}

	public String getObjToUuid() {
		return objToUuid;
	}

	public void setObjToUuid(String objToUuid) {
		this.objToUuid = objToUuid;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getSpecialRef() {
		return specialRef;
	}

	public void setSpecialRef(Integer specialRef) {
		this.specialRef = specialRef;
	}

	public String getSpecialName() {
		return specialName;
	}

	public void setSpecialName(String specialName) {
		this.specialName = specialName;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}


	public ObjectNode getObjectNode() {
		return objectNode;
	}

	public void setObjectNode(ObjectNode objectNode) {
		this.objectNode = objectNode;
	}

}