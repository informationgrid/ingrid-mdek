/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.model.T01Object;

public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    DaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }
    
    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;
		if (clazz.isAssignableFrom(T01Object.class)) {
			dao = new GenericHibernateDao<T01Object>(_sessionFactory,
					T01Object.class);
		}
        return null;
    }

}
