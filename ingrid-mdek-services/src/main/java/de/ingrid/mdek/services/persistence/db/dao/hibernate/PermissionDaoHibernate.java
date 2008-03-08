package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.SysList;

/**
 * Hibernate-specific implementation of the <tt>IPermissionDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Joachim
 */
public class PermissionDaoHibernate
	extends GenericHibernateDao<SysList>
	implements  IPermissionDao {

    public PermissionDaoHibernate(SessionFactory factory) {
        super(factory, SysList.class);
    }

	public List<Permission> getAddressPermissions(String addrId, String uuid) {
		
		Session session = getSession();
		List<Permission> ps = session.createQuery("select distinct p from IdcUser u " +
				"left join u.idcGroup g " +
				"left join g.permissionAddrs pA " +
				"left join g.permission p " +
				"where u.addrUuid = ? and pA.uuid = ?").setString(0, addrId).setString(1, uuid)
				.list();

		return ps;
	}

	public List<Permission> getObjectPermissions(String addrId, String uuid) {
		Session session = getSession();

		List<Permission> ps = session.createQuery("select distinct p from IdcUser u " +
				"left join u.idcGroup g " +
				"left join g.permissionObjs pA " +
				"left join g.permission p " +
				"where u.addrUuid = ? and pA.uuid = ?").setString(0, addrId).setString(1, uuid)
				.list();

		return ps;
	}

	public List<Permission> getUserPermissions(String addrId) {
		Session session = getSession();

		List<Permission> ps = session.createQuery("select distinct p from IdcUser u " +
				"left join u.idcUserPermissions up " +
				"left join up.getPermission p " +
				"where u.addrId = ?").setString(0, addrId)
				.list();

		return ps;
	}

}
