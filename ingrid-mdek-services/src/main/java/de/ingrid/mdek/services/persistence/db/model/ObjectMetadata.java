/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

public class ObjectMetadata implements IEntity {

	private Long id;
	private int version;
	private Integer expiryState;
	private String lastexportTime;
	private String markDeleted;
	private String assignerUuid;
	private String assignTime;
	private String reassignerUuid;
	private String reassignTime;

	private AddressNode addressNodeAssigner;

	public ObjectMetadata() {}

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

	public Integer getExpiryState() {
		return expiryState;
	}

	public void setExpiryState(Integer expiryState) {
		this.expiryState = expiryState;
	}

	public String getLastexportTime() {
		return lastexportTime;
	}

	public void setLastexportTime(String lastexportTime) {
		this.lastexportTime = lastexportTime;
	}

	public String getMarkDeleted() {
		return markDeleted;
	}

	public void setMarkDeleted(String markDeleted) {
		this.markDeleted = markDeleted;
	}

	public String getAssignerUuid() {
		return assignerUuid;
	}

	public void setAssignerUuid(String assignerUuid) {
		this.assignerUuid = assignerUuid;
	}

	public String getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(String assignTime) {
		this.assignTime = assignTime;
	}

	public String getReassignerUuid() {
		return reassignerUuid;
	}

	public void setReassignerUuid(String reassignerUuid) {
		this.reassignerUuid = reassignerUuid;
	}

	public String getReassignTime() {
		return reassignTime;
	}

	public void setReassignTime(String reassignTime) {
		this.reassignTime = reassignTime;
	}


	public AddressNode getAddressNodeAssigner() {
		return addressNodeAssigner;
	}

	public void setAddressNodeAssigner(AddressNode addressNodeAssigner) {
		this.addressNodeAssigner = addressNodeAssigner;
	}

}
