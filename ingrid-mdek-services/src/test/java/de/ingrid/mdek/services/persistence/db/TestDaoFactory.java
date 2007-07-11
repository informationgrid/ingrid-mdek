package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;

import org.hibernate.SessionFactory;

public class TestDaoFactory implements IDaoFactory {

	private final SessionFactory _sessionFactory;

	public TestDaoFactory(SessionFactory sessionFactory) {
		_sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;
		if (clazz.isAssignableFrom(Metadata.class)) {
			dao = new GenericHibernateDao<Metadata, Serializable>(
					_sessionFactory, Metadata.class);
		}
		return dao;
	}
}
