package de.ingrid.mdek.services.security;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.Metadata;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;

public abstract class AbstractPermissionTest extends
		AbstractDependencyInjectionSpringContextTests {

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
	
	protected IdcGroup createIdcGroup(Long id, String name) {
		IdcGroup group = new IdcGroup();
		group.setId(id);
		group.setCreateTime("12345");
		group.setModTime("32123");
		group.setModUuid("moduuid");
		group.setName(name);
		return group;
	}

	protected IdcUser createIdcUser(Long id, Long idcGroupId, String addrUuid, Integer role, Long parentId) {
		IdcUser user = new IdcUser();
		user.setId(id);
		user.setIdcGroupId(idcGroupId);
		user.setAddrUuid(addrUuid);
		user.setIdcRole(role);
		user.setParentId(parentId);
		return user;
	}
	
	
}
