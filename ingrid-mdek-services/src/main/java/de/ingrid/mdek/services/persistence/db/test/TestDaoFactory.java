/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db.test;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IDaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;

public class TestDaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    TestDaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }
    
    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;
		if (clazz.isAssignableFrom(TestMetadata.class)) {
			dao = new GenericHibernateDao<TestMetadata>(_sessionFactory,
					TestMetadata.class);
		}
		return dao;
    }
}
