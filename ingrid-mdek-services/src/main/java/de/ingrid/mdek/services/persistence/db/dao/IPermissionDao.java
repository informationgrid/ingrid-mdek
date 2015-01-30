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
package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Business DAO operations related to the <tt>Permission</tt> entity.
 * 
 * @author Joachim
 */
public interface IPermissionDao extends IGenericDao<Permission> {

	/**
	 * Get "directly" set permissions of given user on given object entity (via groups of user).
	 * NO INHERITED PERMISSIONS.
	 * @param userUuid address uuid of user
	 * @param objUuid uuid of object entity to check
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return list of permissions set for object (in group of user).
	 */
	public List<Permission> getObjectPermissions(String userUuid, String objUuid, Long groupId);

	/**
	 * Get "directly" set permissions of given user on given address entity (via group of user).
	 * NO INHERITED PERMISSIONS.
	 * @param userUuid address uuid of user
	 * @param addrUuid uuid of address entity to check
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return list of permissions set for address (in group of user).
	 */
	public List<Permission> getAddressPermissions(String userUuid, String addrUuid, Long groupId);

	/**
	 * Get user permissions of given user.
	 * @param userUuid address uuid of user
	 * @param groupId only search in this group. Pass null if  all groups should be taken into account !
	 * @return list of permissions set for user (in group of user).
	 */
	public List<Permission> getUserPermissions(String userUuid, Long groupId);
}
