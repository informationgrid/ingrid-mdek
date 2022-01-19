/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
import de.ingrid.mdek.services.persistence.db.model.T02Address;

/**
 * Business DAO operations related to the <tt>T02Address</tt> entity.
 * 
 * @author Martin
 */
public interface IT02AddressDao
	extends IGenericDao<T02Address> {
	
	/** Get objects (no matter whether published or not !) referencing the address with the passed uuid.
	 * @param addressUuid the address being referenced
	 * @param referenceTypeId type of reference=entry id in syslist; PASS NULL, IF ALL TYPES !
	 * @param maxNum maximum number to fetch, pass null to fetch ALL objects
	 * @return list of objects referencing the address in the given way (type)
	 */
	List<T01Object> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId, Integer maxNum);
	/** Get according HQL Statement to fetch csv data !
	 * @param addressUuid uuid of address to be referenced
	 * @param referenceTypeId pass null if all reference types should be fetched
	 * @return HQL for fetching objects
	 */
	String getCsvHQLObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId);

	/** Get Addresses (also published ones) where given user is responsible user.
	 * Pass maxNum or NULL if all addresses. */
	List<T02Address> getAddressesOfResponsibleUser(String responsibleUserUuid, Integer maxNum);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLAllAddressesOfResponsibleUser(String responsibleUserUuid);
}
