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
	public List<Permission> getAddressPermissions(String userUuid, String addrUuid) {

		Session session = getSession();
		List<Permission> ps = session.createQuery(
				"select distinct permission from IdcUser u " +
					"inner join u.idcGroup g " +
					"inner join g.permissionAddrs pA " +
					"inner join pA.permission permission " +
					"where u.addrUuid = ? and pA.uuid = ?")
					.setString(0, userUuid).setString(1, addrUuid).list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getObjectPermissions(String userUuid, String objUuid) {
		Session session = getSession();

		List<Permission> ps = session.createQuery(
				"select distinct permission from IdcUser u " +
					"inner join u.idcGroup g " +
					"inner join g.permissionObjs pO " +
					"inner join pO.permission permission " +
					"where u.addrUuid = ? and pO.uuid = ?")
					.setString(0, userUuid).setString(1, objUuid).list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getUserPermissions(String userUuid) {
		Session session = getSession();

		List<Permission> ps = session.createQuery(
			"select distinct permission from IdcUser u " + 
				"inner join u.idcGroup g " +
				"inner join g.idcUserPermissions pU " + 
				"inner join pU.permission permission " +
				"where u.addrUuid = ?")
				.setString(0, userUuid).list();

		return ps;
	}
}
