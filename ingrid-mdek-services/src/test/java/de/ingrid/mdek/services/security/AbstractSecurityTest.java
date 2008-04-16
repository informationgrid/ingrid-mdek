package de.ingrid.mdek.services.security;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

public abstract class AbstractSecurityTest extends AbstractDependencyInjectionSpringContextTests {

	private static final String APPLICATION_CONTEXT_XML = "security-services-test.xml";

	private static final String APPLICATION_CONTEXT_CONFIGURATION = "configuration-test.xml";

	// spring bean
	private SessionFactory _sessionFactory;

	// spring bean
	private IPermissionService _permissionService;

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		cleanDatabase();
		
	}

	private void cleanDatabase() {
		beginNewTransaction();
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(
				_sessionFactory, IdcGroup.class);
		List<IdcGroup> list = dao.findAll();
		for (IdcGroup group : list) {
			dao.makeTransient(group);
		}
		commitTransaction();
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
