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
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Hibernate-specific implementation of the <tt>IPermissionDao</tt> non-CRUD
 * (Create, Read, Update, Delete) data access object.
 * 
 * @author Joachim
 */
public class PermissionDaoHibernate extends GenericHibernateDao<Permission> implements IPermissionDao {

	public PermissionDaoHibernate(SessionFactory factory) {
		super(factory, Permission.class);
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getAddressPermissions(String userUuid, String addrUuid, Long groupId) {
		String sql = "select distinct p " +
			"from IdcUser u, IdcUserGroup uG, IdcGroup g, " +
			"PermissionAddr pA, Permission p " +
			"where " +
			"u.id = uG.idcUserId " +
			"AND uG.idcGroupId = g.id " +
			"AND g.id = pA.idcGroupId " +
			"AND pA.permissionId = p.id " +
			"AND u.addrUuid = ?1 and pA.uuid = ?";
		if (groupId != null) {
			sql = sql + " AND g.id = " + groupId;
		}
		
		Session session = getSession();
		List<Permission> ps = session.createQuery(sql)
					.setString(1, userUuid).setString(1, addrUuid).list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getObjectPermissions(String userUuid, String objUuid, Long groupId) {
		String sql = "select distinct p " +
			"from IdcUser u, IdcUserGroup uG, IdcGroup g, " +
			"PermissionObj pO, Permission p " +
			"where " +
			"u.id = uG.idcUserId " +
			"AND uG.idcGroupId = g.id " +
			"AND g.id = pO.idcGroupId " +
			"AND pO.permissionId = p.id " +
			"AND u.addrUuid = ?1 and pO.uuid = ?";
		if (groupId != null) {
			sql = sql + " AND g.id = " + groupId;
		}

		Session session = getSession();
		List<Permission> ps = session.createQuery(sql)
					.setString(1, userUuid).setString(1, objUuid).list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getUserPermissions(String userUuid, Long groupId) {
		String sql = "select distinct p " +
			"from IdcUser u, IdcUserGroup uG, IdcGroup g, " +
			"IdcUserPermission pU, Permission p " +
			"where " +
			"u.id = uG.idcUserId " +
			"AND uG.idcGroupId = g.id " +
			"AND g.id = pU.idcGroupId " +
			"AND pU.permissionId = p.id " +
			"AND u.addrUuid = ?1";
		if (groupId != null) {
			sql = sql + " AND g.id = " + groupId;
		}

		Session session = getSession();
		List<Permission> ps = session.createQuery(sql)
					.setString(1, userUuid).list();

		return ps;
	}
}
