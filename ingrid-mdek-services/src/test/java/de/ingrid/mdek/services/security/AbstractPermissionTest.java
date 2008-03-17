package de.ingrid.mdek.services.security;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractPermissionTest extends AbstractDependencyInjectionSpringContextTests {

	private static final String APPLICATION_CONTEXT_XML = "security-services-test.xml";

	private static final String APPLICATION_CONTEXT_CONFIGURATION = "configuration-test.xml";

	// spring bean
	private SessionFactory _sessionFactory;

	// spring bean
	private IPermissionService _permissionService;

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { APPLICATION_CONTEXT_XML, APPLICATION_CONTEXT_CONFIGURATION };
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		_sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return _sessionFactory;
	}

	public void setPermissionService(IPermissionService permissionService) {
		_permissionService = permissionService;
	}

	public IPermissionService getPermissionService() {
		return _permissionService;
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
