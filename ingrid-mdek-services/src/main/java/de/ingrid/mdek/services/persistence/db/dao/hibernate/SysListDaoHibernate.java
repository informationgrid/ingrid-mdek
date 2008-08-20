package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
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

	public List<SysList> getSysList(String lstId, String language) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = ? ";

		if (language != null) {
			qString += "and langId = ? ";
		}
		qString += "order by line";

		Query q = session.createQuery(qString);
		q.setString(0, lstId);
		if (language != null) {
			q.setString(1, language);			
		}

		return q.list();
	}

	public SysList getSysListEntry(String lstId, String entryId, String language) {
		Session session = getSession();
		
		String qString = "from SysList " +
			"where lstId = ? " +
			"and entryId = ? " +
			"and langId = ?";

		Query q = session.createQuery(qString);
		q.setString(0, lstId);
		q.setString(1, entryId);
		q.setString(2, language);

		return (SysList) q.uniqueResult();
	}
}
