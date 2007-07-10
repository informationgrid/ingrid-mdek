package de.ingrid.mdek.services.persistence.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class TransactionService implements ITransactionService {

	private final SessionFactory _sessionFactory;

	public TransactionService(SessionFactory sessionFactory) {
		assert sessionFactory != null;
		_sessionFactory = sessionFactory;
	}

	public void beginTransaction() {
		Session currentSession = getSession();
		if (!currentSession.getTransaction().isActive()) {
			currentSession.beginTransaction();
		}
	}

	public void commitTransaction() {
		Session session = getSession();
		session.getTransaction().commit();
	}

	public void rollbackTransaction() {
		Session session = getSession();
		session.getTransaction().rollback();
	}

	public Session getSession() {
		return _sessionFactory.getCurrentSession();
	}
}
