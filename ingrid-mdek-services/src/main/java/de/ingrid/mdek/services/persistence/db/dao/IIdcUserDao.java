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
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;

/**
 * Business DAO operations related to the <tt>IdcUser</tt> entity.
 * 
 * @author Joachim
 */
public interface IIdcUserDao extends IGenericDao<IdcUser> {

	/** Get a IdcUser by it's addrUuid. The addrUuid is unique for all IdcUsers in this catalog. */
	IdcUser getIdcUserByAddrUuid(String addrUuid);

	/** Get the catalog administrator for this catalog. */
	IdcUser getCatalogAdmin();
	
	/** Returns all users belonging to a group defined by groupId. */
	List<IdcUser> getIdcUsersByGroupId(Long groupId);

	/** Returns all users belonging to a group defined by groupName. */
	List<IdcUser> getIdcUsersByGroupName(String groupName);

	/** Returns all groups belonging to a user. */
	List<IdcGroup> getGroupsOfUser(String userUuid);

	/** Returns all subusers of user with given userId. */
	List<IdcUser> getSubUsers(Long parentIdcUserId);
}
