/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;

/**
 * Hibernate-specific implementation of the <tt>IIdcUserDao</tt> non-CRUD
 * (Create, Read, Update, Delete) data access object.
 * 
 * @author Joachim
 */
public class IdcUserDaoHibernate extends GenericHibernateDao<IdcUser> implements IIdcUserDao {

	public IdcUserDaoHibernate(SessionFactory factory) {
		super(factory, IdcUser.class);
	}

	public IdcUser getCatalogAdmin() {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.idcRole = ?1").setInteger(1, MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue())
				.uniqueResult();
	}

	public IdcUser getIdcUserByAddrUuid(String addrUuid) {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.addrUuid = ?1").setString(1, addrUuid)
				.uniqueResult();
	}

	public List<IdcUser> getIdcUsersByGroupId(Long groupId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("select distinct u " +
				"from IdcUser u, IdcUserGroup uG " +
				"where " +
				"u.id = uG.idcUserId " +
				"AND uG.idcGroupId = ?1").setLong(1, groupId).list();
	}

	public List<IdcUser> getIdcUsersByGroupName(String groupName) {
		Session session = getSession();
		String query = "select distinct u " +
				"from IdcUser u, IdcUserGroup uG, IdcGroup g " +
				"where " +
				"u.id = uG.idcUserId " +
				"AND uG.idcGroupId = g.id " +
				"AND g.name= ?1";

		return (List<IdcUser>)session.createQuery(query).setString(1, groupName).list();
	}

	public List<IdcGroup> getGroupsOfUser(String userUuid) {
		Session session = getSession();
		String query = "select distinct g " +
				"from IdcUser u, IdcUserGroup uG, IdcGroup g " +
				"where " +
				"u.id = uG.idcUserId " +
				"AND uG.idcGroupId = g.id " +
				"AND u.addrUuid= ?1";

		return (List<IdcGroup>)session.createQuery(query).setString(1, userUuid).list();
	}

	public List<IdcUser> getSubUsers(Long parentIdcUserId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("from IdcUser u " +
		"where u.parentId = ?1").setLong(1, parentIdcUserId).list();
	}
}
