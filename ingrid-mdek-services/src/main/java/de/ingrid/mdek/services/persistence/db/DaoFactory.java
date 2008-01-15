/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ObjectNodeDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefSnsDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefValueDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T01ObjectDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T02AddressDaoHibernate;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;

public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    DaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    public IObjectNodeDao getObjectNodeDao() {
        return new ObjectNodeDaoHibernate(_sessionFactory);
    }

    public IT01ObjectDao getT01ObjectDao() {
        return new T01ObjectDaoHibernate(_sessionFactory);
    }

    public IT02AddressDao getT02AddressDao() {
        return new T02AddressDaoHibernate(_sessionFactory);
    }

    public ISpatialRefValueDao getSpatialRefValueDao() {
        return new SpatialRefValueDaoHibernate(_sessionFactory);
    }

    public ISpatialRefSnsDao getSpatialRefSnsDao() {
        return new SpatialRefSnsDaoHibernate(_sessionFactory);
    }

    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;

		if (clazz.isAssignableFrom(SpatialReference.class)) {
			dao = new GenericHibernateDao<SpatialReference>(_sessionFactory, SpatialReference.class);
		}

        return dao;
    }

}
