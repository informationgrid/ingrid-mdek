package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
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

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao#getCatalogAdmin()
	 */
	public IdcUser getCatalogAdmin() {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.idcRole = ?").setInteger(0, MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue())
				.uniqueResult();
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao#getIdcUserByAddrUuid(java.lang.String)
	 */
	public IdcUser getIdcUserByAddrUuid(String addrUuid) {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.addrUuid = ?").setString(0, addrUuid)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<IdcUser> getIdcUsersByGroupId(Long groupId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("from IdcUser u " +
		"where u.idcGroupId = ?").setLong(0, groupId).list();
	}

	public List<IdcUser> getSubUsers(Long parentIdcUserId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("from IdcUser u " +
		"where u.parentId = ?").setLong(0, parentIdcUserId).list();
	}
}
