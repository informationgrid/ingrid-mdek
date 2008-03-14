package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Hibernate-specific implementation of the <tt>IPermissionDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Joachim
 */
public class PermissionDaoHibernate
	extends GenericHibernateDao<Permission>
	implements  IPermissionDao {

    public PermissionDaoHibernate(SessionFactory factory) {
        super(factory, Permission.class);
    }

	@SuppressWarnings("unchecked")
	public List<Permission> getAddressPermissions(String addrId, String uuid) {
		
		Session session = getSession();
		List<Permission> ps = session.createQuery("select distinct permission from IdcUser u " +
				"left join u.idcGroup g " +
				"left join g.permissionAddrs pA " +
				"left join pA.permission permission " +
				"where u.addrUuid = ? and pA.uuid = ?").setString(0, addrId).setString(1, uuid)
				.list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getObjectPermissions(String addrId, String uuid) {
		Session session = getSession();

		List<Permission> ps = session.createQuery("select distinct permission from IdcUser u " +
				"left join u.idcGroup g " +
				"left join g.permissionObjs pO " +
				"left join pO.permission permission " +
				"where u.addrUuid = ? and pO.uuid = ?").setString(0, addrId).setString(1, uuid)
				.list();

		return ps;
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getUserPermissions(String addrId) {
		Session session = getSession();

		List<Permission> ps = session.createQuery("select distinct permission from IdcUser u " +
				"left join u.idcUserPermissions up " +
				"left join up.getPermission permission " +
				"where u.addrId = ?").setString(0, addrId)
				.list();

		return ps;
	}

}
