/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ObjectNodeDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SearchtermSnsDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SearchtermValueDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefSnsDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefValueDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T01ObjectDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T02AddressDaoHibernate;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvCategory;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;

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

    public ISearchtermValueDao getSearchtermValueDao() {
        return new SearchtermValueDaoHibernate(_sessionFactory);
    }

    public ISearchtermSnsDao getSearchtermSnsDao() {
        return new SearchtermSnsDaoHibernate(_sessionFactory);
    }

    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;

		if (clazz.isAssignableFrom(SpatialReference.class)) {
			dao = new GenericHibernateDao<SpatialReference>(_sessionFactory, SpatialReference.class);
		} else if (clazz.isAssignableFrom(SearchtermObj.class)) {
			dao = new GenericHibernateDao<SearchtermObj>(_sessionFactory, SearchtermObj.class);
		} else if (clazz.isAssignableFrom(T012ObjAdr.class)) {
			dao = new GenericHibernateDao<T012ObjAdr>(_sessionFactory, T012ObjAdr.class);
		} else if (clazz.isAssignableFrom(ObjectReference.class)) {
			dao = new GenericHibernateDao<ObjectReference>(_sessionFactory, ObjectReference.class);
		} else if (clazz.isAssignableFrom(T017UrlRef.class)) {
			dao = new GenericHibernateDao<T017UrlRef>(_sessionFactory, T017UrlRef.class);
		} else if (clazz.isAssignableFrom(T0113DatasetReference.class)) {
			dao = new GenericHibernateDao<T0113DatasetReference>(_sessionFactory, T0113DatasetReference.class);
		} else if (clazz.isAssignableFrom(T014InfoImpart.class)) {
			dao = new GenericHibernateDao<T014InfoImpart>(_sessionFactory, T014InfoImpart.class);
		} else if (clazz.isAssignableFrom(T015Legist.class)) {
			dao = new GenericHibernateDao<T015Legist>(_sessionFactory, T015Legist.class);
		} else if (clazz.isAssignableFrom(T0110AvailFormat.class)) {
			dao = new GenericHibernateDao<T0110AvailFormat>(_sessionFactory, T0110AvailFormat.class);
		} else if (clazz.isAssignableFrom(T0112MediaOption.class)) {
			dao = new GenericHibernateDao<T0112MediaOption>(_sessionFactory, T0112MediaOption.class);
		} else if (clazz.isAssignableFrom(T0114EnvCategory.class)) {
			dao = new GenericHibernateDao<T0114EnvCategory>(_sessionFactory, T0114EnvCategory.class);
		} else if (clazz.isAssignableFrom(T0114EnvTopic.class)) {
			dao = new GenericHibernateDao<T0114EnvTopic>(_sessionFactory, T0114EnvTopic.class);
		} 

        return dao;
    }

}
