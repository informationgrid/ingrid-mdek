package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;

/**
 * Hibernate-specific implementation of the <tt>ISysListDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysListDaoHibernate
	extends GenericHibernateDao<SysList>
	implements  ISysListDao {

    public SysListDaoHibernate(SessionFactory factory) {
        super(factory, SysList.class);
    }

	public List<SysList> getSysList(int lstId) {
		Session session = getSession();

		List<SysList> retList = session.createQuery("from SysList " +
				"where lstId = ? " +
				"order by entryId")
				.setInteger(0, lstId)
				.list();

		return retList;
	}
}
