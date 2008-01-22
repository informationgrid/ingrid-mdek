package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;

/**
 * Hibernate-specific implementation of the <tt>SearchtermSns</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SearchtermSnsDaoHibernate
	extends GenericHibernateDao<SearchtermSns>
	implements  ISearchtermSnsDao {

    public SearchtermSnsDaoHibernate(SessionFactory factory) {
        super(factory, SearchtermSns.class);
    }

	public SearchtermSns load(String snsId) {
		Session session = getSession();

		SearchtermSns termSns = (SearchtermSns) session.createQuery("from SearchtermSns " +
			"where snsId = ?")
			.setString(0, snsId)
			.uniqueResult();
		
		return termSns;
	}

	public SearchtermSns loadOrCreate(String snsId) {
		SearchtermSns termSns = load(snsId);
		
		if (termSns == null) {
			termSns = new SearchtermSns();
			termSns.setSnsId(snsId);
			makePersistent(termSns);			
		}
		
		return termSns;
	}
}
