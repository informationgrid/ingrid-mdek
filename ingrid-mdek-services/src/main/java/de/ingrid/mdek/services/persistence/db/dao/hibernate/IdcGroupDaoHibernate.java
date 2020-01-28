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
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

/**
 * Hibernate-specific implementation of the <tt>IIdcGroupDao</tt> non-CRUD
 * (Create, Read, Update, Delete) data access object.
 */
public class IdcGroupDaoHibernate
	extends GenericHibernateDao<IdcGroup>
	implements IIdcGroupDao {

	public IdcGroupDaoHibernate(SessionFactory factory) {
		super(factory, IdcGroup.class);
	}

	public IdcGroup loadByName(String name) {
		if (name == null) {
			return null;
		}

		Session session = getSession();

		IdcGroup grp = (IdcGroup) session.createQuery("from IdcGroup grp " +
			"where grp.name = ?")
			.setString(0, name)
			.uniqueResult();
		
		return grp;
	}

	public List<IdcGroup> getGroups() {
		Session session = getSession();
		String query = "select group from IdcGroup group " +
			"order by group.name";
		
		List<IdcGroup> groups = session.createQuery(query).list();

		return groups;
	}

	public IdcGroup getGroupDetails(String name) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		IdcGroup grp = (IdcGroup) session.createQuery(
			createGroupDetailsQueryString() +
			"where grp.name = ?")
			.setString(0, name)
			.uniqueResult();

		return grp;
	}

	public IdcGroup getGroupDetails(Long groupId) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		IdcGroup grp = (IdcGroup) session.createQuery(
			createGroupDetailsQueryString() +
			"where grp.id = ?")
			.setLong(0, groupId)
			.uniqueResult();

		return grp;
	}
	
	private String createGroupDetailsQueryString() {
		String qString = "from IdcGroup grp " +
			"left join fetch grp.permissionAddrs permAddr " +
			"left join fetch permAddr.permission permA " +
			"left join fetch permAddr.addressNode aNode " +
			"left join fetch aNode.t02AddressWork a " +
			"left join fetch grp.permissionObjs permObj " +
			"left join fetch permObj.permission permO " +
			"left join fetch permObj.objectNode oNode " +
			"left join fetch oNode.t01ObjectWork o " +
			"left join fetch grp.idcUserPermissions permUser " +
			"left join fetch permUser.permission permU ";
		
		return qString;
	}

	private String getHQLUserUuidsOfGroup(String groupName) {
		String qString = "select distinct u.addrUuid " +
			"from IdcUserGroup uG, IdcGroup g, IdcUser u " +
			"where " +
			"uG.idcGroupId = g.id " +
			"AND uG.idcUserId = u.id " +
			"AND g.name='" + groupName + "'";
		
		return qString;
	}

	public List<Map> getGroupUsersWithObjectsNotInGivenState(String groupName,
			WorkState objWorkState) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"o.objUuid as " + KEY_ENTITY_UUID + 
				", o.modUuid as " + KEY_USER_UUID + 
			") " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork o " +
			"where " +
			"o.workState != '" + objWorkState.getDbValue() + "' " +
			"and o.modUuid in (" +
				getHQLUserUuidsOfGroup(groupName) +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}

	public List<Map> getGroupUsersWithAddressesNotInGivenState(String groupName,
			WorkState addrWorkState) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"a.adrUuid as " + KEY_ENTITY_UUID + 
				", a.modUuid as " + KEY_USER_UUID + 
			") " +
			"from AddressNode aNode " +
			"inner join aNode.t02AddressWork a " +
			"where " +
			// we do NOT check IGE USER ADDRESSES ! 
			MdekUtils.AddressType.getHQLExcludeIGEUsersViaAddress("a") +
			"and a.workState != '" + addrWorkState.getDbValue() + "' " +
			"and a.modUuid in (" +
				getHQLUserUuidsOfGroup(groupName) +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}

	public List<Map> getGroupUsersResponsibleForObjects(String groupName) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"o.objUuid as " + KEY_ENTITY_UUID + 
				", o.responsibleUuid as " + KEY_USER_UUID + 
			") " +
			"from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork o " +
			"where " +
			"o.responsibleUuid in (" +
				getHQLUserUuidsOfGroup(groupName) +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}

	public List<Map> getGroupUsersResponsibleForAddresses(String groupName) {
		Session session = getSession();

		String q = "select distinct new Map(" +
				"a.adrUuid as " + KEY_ENTITY_UUID + 
				", a.responsibleUuid as " + KEY_USER_UUID + 
			") " +
			"from AddressNode aNode " +
			"inner join aNode.t02AddressWork a " +
			"where " +
			// we do NOT check IGE USER ADDRESSES ! 
			MdekUtils.AddressType.getHQLExcludeIGEUsersViaAddress("a") +
			"and a.responsibleUuid in (" +
				getHQLUserUuidsOfGroup(groupName) +
			")";

		List<Map> maps = session.createQuery(q).list();

		return maps;
	}

	public List<Long> getGroupIdsContainingUserPermission(String userUuid, Long permId) {
		Session session = getSession();

		String qString = "select distinct uG.idcGroupId " +
			"from IdcUser u, IdcUserGroup uG, IdcUserPermission uP " +
			"where " +
			"u.id = uG.idcUserId " +
			"AND uG.idcGroupId = uP.idcGroupId " +
			"AND u.addrUuid='" + userUuid + "' " +
			"AND uP.permissionId=" + permId + "";

		return (List<Long>)session.createQuery(qString).list();
	}

	public List<Long> getGroupIdsContainingObjectPermission(String userUuid, Long permId, String objUuid) {
		Session session = getSession();

		String qString = "select distinct uG.idcGroupId" +
			" from IdcUser u, IdcUserGroup uG, PermissionObj oP" +
			" where" +
			" u.id = uG.idcUserId" +
			" AND uG.idcGroupId = oP.idcGroupId" +
			" AND u.addrUuid='" + userUuid + "'" +
			" AND oP.permissionId=" + permId +
			" AND oP.uuid='" + objUuid + "'";

		return (List<Long>)session.createQuery(qString).list();
	}

	public List<Long> getGroupIdsContainingAddressPermission(String userUuid, Long permId, String addrUuid) {
		Session session = getSession();

		String qString = "select distinct uG.idcGroupId" +
			" from IdcUser u, IdcUserGroup uG, PermissionAddr aP" +
			" where" +
			" u.id = uG.idcUserId" +
			" AND uG.idcGroupId = aP.idcGroupId" +
			" AND u.addrUuid='" + userUuid + "'" +
			" AND aP.permissionId=" + permId +
			" AND aP.uuid='" + addrUuid + "'";

		return (List<Long>)session.createQuery(qString).list();
	}
}
