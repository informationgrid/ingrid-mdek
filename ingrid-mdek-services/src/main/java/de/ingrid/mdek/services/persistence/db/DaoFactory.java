/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T01ObjectDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T02AddressDaoHibernate;

public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    DaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    public IT01ObjectDao getT01ObjectDao() {
        return new T01ObjectDaoHibernate(_sessionFactory);
    }

    public IT02AddressDao getT02AddressDao() {
        return new T02AddressDaoHibernate(_sessionFactory);
    }

    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;
/*
		if (clazz.isAssignableFrom(T01Object.class)) {
			dao = new T01ObjectDao(_sessionFactory);
		}
*/
        return dao;
    }

}
