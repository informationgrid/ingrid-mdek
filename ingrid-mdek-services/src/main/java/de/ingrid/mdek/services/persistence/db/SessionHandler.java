package de.ingrid.mdek.services.persistence.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class SessionHandler {

	private final SessionFactory _sessionFactory;

	public SessionHandler(SessionFactory sessionFactory) {
		assert sessionFactory != null;
		_sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return _sessionFactory;
	}

	public Session getSession() {
		org.hibernate.classic.Session currentSession = _sessionFactory
				.getCurrentSession();
		if (!currentSession.getTransaction().isActive()) {
			currentSession.beginTransaction();
		}
		return _sessionFactory.getCurrentSession();
	}

	public void commit() {
		_sessionFactory.getCurrentSession().getTransaction().commit();
	}
	
	public void rollback() {
		_sessionFactory.getCurrentSession().getTransaction().rollback();
	}
}
