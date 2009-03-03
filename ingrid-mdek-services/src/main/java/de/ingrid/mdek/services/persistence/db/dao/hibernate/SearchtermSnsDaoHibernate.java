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

	public SearchtermSns load(String snsId, String gemetId) {
		Session session = getSession();

		String qString = "from SearchtermSns " +
			"where snsId = '" + snsId + "' ";

		if (gemetId != null && gemetId.length() > 0) {
			qString += "and gemetId = '" + gemetId + "'";
		}

		SearchtermSns termSns = (SearchtermSns) session.createQuery(qString)
			.uniqueResult();
		
		return termSns;
	}

	public SearchtermSns loadOrCreate(String snsId, String gemetId) {
		SearchtermSns termSns = load(snsId, gemetId);
		
		if (termSns == null) {
			termSns = new SearchtermSns();
			termSns.setSnsId(snsId);
			termSns.setGemetId(gemetId);
			makePersistent(termSns);			
		}
		
		return termSns;
	}
}
