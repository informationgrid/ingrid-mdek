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

	public IdcUser getCatalogAdmin() {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.idcRole = ?").setInteger(0, MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue())
				.uniqueResult();
	}

	public IdcUser getIdcUserByAddrUuid(String addrUuid) {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.addrUuid = ?").setString(0, addrUuid)
				.uniqueResult();
	}

	public List<IdcUser> getIdcUsersByGroupId(Long groupId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("from IdcUser u " +
		"where u.idcGroupId = ?").setLong(0, groupId).list();
	}

	public List<IdcUser> getIdcUsersByGroupName(String groupName) {
		Session session = getSession();
		String query = "select distinct idcUser from IdcUser idcUser " +
		"inner join fetch idcUser.idcGroup idcGroup " +
		"where idcGroup.name = ?";

		return (List<IdcUser>)session.createQuery(query).setString(0, groupName).list();
	}

	public List<IdcUser> getSubUsers(Long parentIdcUserId) {
		Session session = getSession();
		return (List<IdcUser>)session.createQuery("from IdcUser u " +
		"where u.parentId = ?").setLong(0, parentIdcUserId).list();
	}
}
