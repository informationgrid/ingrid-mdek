/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;

import org.hibernate.SessionFactory;

public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    DaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }
    
    public IGenericDao<Serializable, Serializable> getDao(Class clazz) {
        return null;
    }

}
