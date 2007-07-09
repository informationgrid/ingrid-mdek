package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractDaoTest extends
		AbstractDependencyInjectionSpringContextTests {

	private static final String APPLICATION_CONTEXT_XML = "datasource.xml";

	// spring bean
	private SessionFactory _sessionFactory;

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		cleanDatabase();
	}

	private void cleanDatabase() {
		beginNewTransaction();

		commitTransaction();
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { APPLICATION_CONTEXT_XML };
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		_sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return _sessionFactory;
	}

	protected void commitTransaction() {
		if (_sessionFactory.getCurrentSession().getTransaction().isActive()) {
			_sessionFactory.getCurrentSession().getTransaction().commit();
		}
	}

	protected void beginNewTransaction() {
		if (!_sessionFactory.getCurrentSession().getTransaction().isActive()) {
			_sessionFactory.getCurrentSession().beginTransaction();
		}
	}

	protected void commitAndBeginnNewTransaction() {
		commitTransaction();
		beginNewTransaction();
	}

}
