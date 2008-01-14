package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;

/**
 * Hibernate-specific implementation of the <tt>SpatialRefSns</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SpatialRefSnsDaoHibernate
	extends GenericHibernateDao<SpatialRefSns>
	implements  ISpatialRefSnsDao {

    public SpatialRefSnsDaoHibernate(SessionFactory factory) {
        super(factory, SpatialRefSns.class);
    }

	public SpatialRefSns load(String snsId) {
		Session session = getSession();

		SpatialRefSns spRefSns = (SpatialRefSns) session.createQuery("from SpatialRefSns " +
			"where snsId = ?")
			.setString(0, snsId)
			.uniqueResult();
		
		return spRefSns;
	}

	public SpatialRefSns loadOrCreate(String snsId) {
		SpatialRefSns spRefSns = load(snsId);
		
		if (spRefSns == null) {
			spRefSns = new SpatialRefSns();
			spRefSns.setSnsId(snsId);
			makePersistent(spRefSns);			
		}
		
		return spRefSns;
	}

}
