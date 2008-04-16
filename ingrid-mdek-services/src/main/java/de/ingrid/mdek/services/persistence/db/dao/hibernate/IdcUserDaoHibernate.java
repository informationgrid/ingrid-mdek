package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;

/**
 * Hibernate-specific implementation of the <tt>IIdcUserDao</tt> non-CRUD
 * (Create, Read, Update, Delete) data access object.
 * 
 * @author Joachim
 */
public class IdcUserDaoHibernate extends GenericHibernateDao<IdcUser> implements IIdcUserDao {

	public static int IDC_ROLE_CATALOG_ADMINISTRATOR = 1;
	
	public static int IDC_ROLE_METADATA_ADMINISTRATOR = 2;
	
	public static int IDC_ROLE_METADATA_AUTHOR = 3;
	
	
	public IdcUserDaoHibernate(SessionFactory factory) {
		super(factory, IdcUser.class);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao#getCatalogAdmin()
	 */
	public IdcUser getCatalogAdmin() {
		Session session = getSession();
		return (IdcUser)session.createQuery("from IdcUser u " +
				"where u.idcRole = ?").setInteger(0, IDC_ROLE_CATALOG_ADMINISTRATOR)
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

}
