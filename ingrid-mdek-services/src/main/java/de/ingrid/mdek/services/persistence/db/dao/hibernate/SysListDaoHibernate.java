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

	public List<SysList> getSysList(int lstId, String language) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = ? " +
			// skip 0 entries -> invalid entries for select boxes !
			"and entryId != 0 ";

		if (language != null) {
			qString += "and langId = ? ";
		}

		Query q = session.createQuery(qString);
		q.setInteger(0, lstId);
		if (language != null) {
			q.setString(1, language);			
		}

		return q.list();
	}
}
