package de.ingrid.mdek.services.persistence.db;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractDaoTest extends
		AbstractDependencyInjectionSpringContextTests {

	private static final String APPLICATION_CONTEXT_XML = "datasource-services.xml";

	// spring bean
	private SessionFactory _sessionFactory;

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		cleanDatabase();
	}

	private void cleanDatabase() {
		beginNewTransaction();
		GenericHibernateDao<Metadata> dao = new GenericHibernateDao<Metadata>(
				_sessionFactory, Metadata.class);
		List<Metadata> list = dao.findAll();
		for (Metadata metadata : list) {
			dao.makeTransient(metadata);
		}
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
