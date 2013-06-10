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
        Session currentSession = getSession();
        if (currentSession.getTransaction().isActive()) {
        	// flush before commit since during tests sometimes errors occured
        	// during save ("Duplicate entry '15925253' for key 'PRIMARY'")
        	// Let's see if this helps!
        	currentSession.flush();
        	currentSession.getTransaction().commit();
        }
    }

    public void rollbackTransaction() {
        Session currentSession = getSession();
        if (currentSession.getTransaction().isActive()) {
            currentSession.getTransaction().rollback();
        }
    }

    public Session getSession() {
        return _sessionFactory.getCurrentSession();
    }
}
