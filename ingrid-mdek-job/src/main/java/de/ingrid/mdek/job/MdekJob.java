package de.ingrid.mdek.job;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of mdek jobs containing information about hibernate session factory, logger ...
 * and default behaviour
 * 
 * @author Martin
 */
public abstract class MdekJob implements IJob {

	private SessionFactory sessionFactory;

	public MdekJob(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see de.ingrid.mdek.job.IJob#getResults()
	 * Called when job is registered !
	 * Default Handling here returns empty IngridDocument 
	 */
	public IngridDocument getResults() {
        IngridDocument result = new IngridDocument();
		return result;
	}

	protected void commitTransaction() {
        Session session = getSession();
		if (session.getTransaction().isActive()) {
			session.getTransaction().commit();
		}
	}

	protected void beginTransaction() {
        Session session = getSession();
		if (!session.getTransaction().isActive()) {
			session.beginTransaction();
		}
	}

	protected void commitAndBeginTransaction() {
		commitTransaction();
		beginTransaction();
	}

    protected void rollbackTransaction() {
        Session session = getSession();
		if (session.getTransaction().isActive()) {
			session.getTransaction().rollback();
		}
    }

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
}
