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
package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;



/**
 * Business DAO operations related to the <tt>T01Object</tt> entity.
 * 
 * @author Martin
 */
public interface IT01ObjectDao
	extends IGenericDao<T01Object> {

	/** Get Objects (also published ones) where given user is responsible user.
	 * Pass maxNum or NULL if all addresses. */
	List<T01Object> getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid);
	
	/**
	 * Check for specific object address reference. If referenceTypeId is null
	 * the method checks if the object is related to the address no matter what
	 * type the reference has. 
	 * 
	 * @param objectUuid The object UUID
	 * @param addressUuid The address UUID.
	 * @param referenceTypeId the type of the relation from syslist
	 * 
	 * @return True if the object has a specific address reference, false if not.
	 */
	boolean hasAddressRelation(String objectUuid, String addressUuid, Integer referenceTypeId);
}
