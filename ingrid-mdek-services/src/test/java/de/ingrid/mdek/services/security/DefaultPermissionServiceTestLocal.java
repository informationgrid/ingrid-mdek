package de.ingrid.mdek.services.security;

import java.util.List;

import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;

public class DefaultPermissionServiceTestLocal extends AbstractPermissionTest {

	IdcUser user = null;

	IdcGroup group = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.mdek.services.security.AbstractPermissionTest#onSetUp()
	 */
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();

		if (group == null) {
			this.beginNewTransaction();
			group = getGroupByName("administrators");
			user = getFirstUserByGroup(group);
			this.commitTransaction();
		}
	}

	@Test
	public void testHasPermissionFor() {

		IPermissionService s = getPermissionService();
		this.beginNewTransaction();

		s.grantAddressPermission(user.getAddrUuid(), PermissionFactory.getSingleAddressPermissionTemplate("adr_uuid_1"));
		s.grantObjectPermission(user.getAddrUuid(), PermissionFactory.getSingleObjectPermissionTemplate("obj_uuid_1"));

		assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("adr_uuid_1")), true);
		assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("obj_uuid_1")), true);
		assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("adr_uuid_0")), false);
		assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("obj_uuid_0")), false);

		s.revokeAddressPermission(user.getAddrUuid(), PermissionFactory.getSingleAddressPermissionTemplate("adr_uuid_1"));
		s.revokeObjectPermission(user.getAddrUuid(), PermissionFactory.getSingleObjectPermissionTemplate("obj_uuid_1"));

		assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("adr_uuid_1")), false);
		assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("obj_uuid_1")), false);

		s.grantObjectPermission(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("15C69C20-FE15-11D2-AF34-0060084A4596"));
		assertEquals(s.hasInheritedPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("2C997C68-2247-11D3-AF51-0060084A4596")), true);
		s.revokeObjectPermission(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("15C69C20-FE15-11D2-AF34-0060084A4596"));
		assertEquals(s.hasInheritedPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("2C997C68-2247-11D3-AF51-0060084A4596")), false);

		s.grantAddressPermission(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("3761E246-69E7-11D3-BB32-1C7607C10000"));
		assertEquals(s.hasInheritedPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("C5FEA801-6AB2-11D3-BB32-1C7607C10000")), true);
		s.revokeAddressPermission(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("3761E246-69E7-11D3-BB32-1C7607C10000"));
		assertEquals(s.hasInheritedPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("C5FEA801-6AB2-11D3-BB32-1C7607C10000")), false);

		s.grantUserPermission(user.getAddrUuid(), PermissionFactory.getCreateRootPermissionTemplate());
		assertEquals(s.hasUserPermission(user.getAddrUuid(), PermissionFactory.getCreateRootPermissionTemplate()), true);
		s.revokeUserPermission(user.getAddrUuid(), PermissionFactory.getCreateRootPermissionTemplate());
		assertEquals(s.hasUserPermission(user.getAddrUuid(), PermissionFactory.getCreateRootPermissionTemplate()), false);
		// recreate user permission, revert to initial state of the database
		s.grantUserPermission(user.getAddrUuid(), PermissionFactory.getCreateRootPermissionTemplate());

		this.commitTransaction();
	}

	private IdcGroup getGroupByName(String groupName) {
		GenericHibernateDao<IdcGroup> idcGoupDao = new GenericHibernateDao<IdcGroup>(getSessionFactory(),
				IdcGroup.class);
		IdcGroup g = new IdcGroup();
		g.setName(groupName);
		return idcGoupDao.findUniqueByExample(g);

	}

	private Permission getPermissionByExample(Permission permission) {
		GenericHibernateDao<Permission> dao = new GenericHibernateDao<Permission>(getSessionFactory(), Permission.class);
		return dao.findUniqueByExample(permission);
	}

	private IdcUser getFirstUserByGroup(IdcGroup group) {
		GenericHibernateDao<IdcUser> dao = new GenericHibernateDao<IdcUser>(getSessionFactory(), IdcUser.class);
		IdcUser iu = new IdcUser();
		iu.setIdcGroupId(group.getId());
		return dao.findUniqueByExample(iu);
	}

	private List<PermissionAddr> getPermissionAddrByExample(PermissionAddr pa) {
		GenericHibernateDao<PermissionAddr> dao = new GenericHibernateDao<PermissionAddr>(getSessionFactory(),
				PermissionAddr.class);
		return dao.findByExample(pa);

	}

	private List<PermissionObj> getPermissionObjByExample(PermissionObj po) {
		GenericHibernateDao<PermissionObj> dao = new GenericHibernateDao<PermissionObj>(getSessionFactory(),
				PermissionObj.class);
		return dao.findByExample(po);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#onTearDown()
	 */
	@Override
	protected void onTearDown() throws Exception {
		// TODO Auto-generated method stub
		super.onTearDown();
		if (group != null) {
			GenericHibernateDao<IEntity> dao = new GenericHibernateDao<IEntity>(getSessionFactory(), IEntity.class);
			this.beginNewTransaction();
			EntityPermission ep = PermissionFactory.getSingleAddressPermissionTemplate("adr_uuid_1");
			Permission p = getPermissionByExample(ep.getPermission());

			PermissionAddr pa = new PermissionAddr();
			pa.setIdcGroupId(group.getId());
			List<PermissionAddr> pal = getPermissionAddrByExample(pa);
			for (PermissionAddr el : pal) {
				dao.makeTransient(el);
			}

			ep = PermissionFactory.getSingleObjectPermissionTemplate("obj_uuid_1");
			p = getPermissionByExample(ep.getPermission());
			PermissionObj po = new PermissionObj();
			po.setId(null);
			po.setIdcGroupId(group.getId());
			List<PermissionObj> pol = getPermissionObjByExample(po);
			for (PermissionObj el : pol) {
				dao.makeTransient(el);
			}
			this.commitTransaction();
		}

	}

}
