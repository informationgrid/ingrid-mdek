package de.ingrid.mdek.services.security;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;

public class DefaultPermissionServiceTest extends AbstractSecurityTest {

	IdcUser user = null;
	IdcGroup group = null;
	IdcUserGroup userGroup = null;

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
			GenericHibernateDao<IEntity> dao = new GenericHibernateDao<IEntity>(getSessionFactory(), IEntity.class);
			
			group = new IdcGroup();
			group.setName("admin-group");
			dao.makePersistent(group);
			
			user = new IdcUser();
			user.setAddrUuid("user-addrUuid");
			dao.makePersistent(user);

			userGroup = new IdcUserGroup();			
			userGroup.setIdcGroupId(user.getId());
			userGroup.setIdcGroupId(group.getId());
			dao.makePersistent(userGroup);


			user = new IdcUser();
			user.setAddrUuid("user-cat-admin");
			user.setIdcRole(MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue());
			dao.makePersistent(user);

			userGroup = new IdcUserGroup();			
			userGroup.setIdcGroupId(user.getId());
			userGroup.setIdcGroupId(group.getId());
			dao.makePersistent(userGroup);


			EntityPermission ep = PermissionFactory.getSingleObjectPermissionTemplate("");
			dao.makePersistent(ep.getPermission());
			ep = PermissionFactory.getSingleAddressPermissionTemplate("");
			dao.makePersistent(ep.getPermission());
			ep = PermissionFactory.getTreeObjectPermissionTemplate("");
			dao.makePersistent(ep.getPermission());
			ep = PermissionFactory.getTreeAddressPermissionTemplate("");
			dao.makePersistent(ep.getPermission());
			dao.makePersistent(PermissionFactory.getPermissionTemplateCreateRoot());
			dao.makePersistent(PermissionFactory.getPermissionTemplateQA());
			
			ObjectNode on = new ObjectNode();
			on.setObjUuid("object-uuid-1");
			dao.makePersistent(on);
			on = new ObjectNode();
			on.setObjUuid("object-uuid-2");
			on.setFkObjUuid("object-uuid-1");
			dao.makePersistent(on);
			on = new ObjectNode();
			on.setObjUuid("object-uuid-3");
			on.setFkObjUuid("object-uuid-2");
			dao.makePersistent(on);
			
			AddressNode an = new AddressNode();
			an.setAddrUuid("address-uuid-1");
			dao.makePersistent(an);
			an = new AddressNode();
			an.setAddrUuid("address-uuid-2");
			an.setFkAddrUuid("address-uuid-1");
			dao.makePersistent(an);
			an = new AddressNode();
			an.setAddrUuid("address-uuid-3");
			an.setFkAddrUuid("address-uuid-2");
			dao.makePersistent(an);
			
			this.commitTransaction();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractSingleSpringContextTests#onTearDown()
	 */
	@Override
	protected void onTearDown() throws Exception {
		super.onTearDown();
		if (group != null) {
			beginNewTransaction();
			
			GenericHibernateDao<IdcUserPermission> dao = new GenericHibernateDao<IdcUserPermission>(
					getSessionFactory(), IdcUserPermission.class);
			List<IdcUserPermission> list = dao.findAll();
			for (IdcUserPermission obj : list) {
				dao.makeTransient(obj);
			}

			GenericHibernateDao<PermissionObj> dao1 = new GenericHibernateDao<PermissionObj>(
					getSessionFactory(), PermissionObj.class);
			List<PermissionObj> list1 = dao1.findAll();
			for (PermissionObj obj : list1) {
				dao1.makeTransient(obj);
			}

			GenericHibernateDao<PermissionAddr> dao2 = new GenericHibernateDao<PermissionAddr>(
					getSessionFactory(), PermissionAddr.class);
			List<PermissionAddr> list2 = dao2.findAll();
			for (PermissionAddr obj : list2) {
				dao2.makeTransient(obj);
			}

			GenericHibernateDao<ObjectNode> dao3 = new GenericHibernateDao<ObjectNode>(
					getSessionFactory(), ObjectNode.class);
			List<ObjectNode> list3 = dao3.findAll();
			for (ObjectNode obj : list3) {
				dao3.makeTransient(obj);
			}

			GenericHibernateDao<AddressNode> dao4 = new GenericHibernateDao<AddressNode>(
					getSessionFactory(), AddressNode.class);
			List<AddressNode> list4 = dao4.findAll();
			for (AddressNode obj : list4) {
				dao4.makeTransient(obj);
			}

			GenericHibernateDao<Permission> dao5 = new GenericHibernateDao<Permission>(
					getSessionFactory(), Permission.class);
			List<Permission> list5 = dao5.findAll();
			for (Permission obj : list5) {
				dao5.makeTransient(obj);
			}

			GenericHibernateDao<IdcUser> dao6 = new GenericHibernateDao<IdcUser>(
					getSessionFactory(), IdcUser.class);
			List<IdcUser> list6 = dao6.findAll();
			for (IdcUser obj : list6) {
				dao6.makeTransient(obj);
			}
			
			commitTransaction();
		}
		
	}

	@Test
	public void testPermissionService() {

		IPermissionService s = getPermissionService();
		this.beginNewTransaction();

		s.grantAddressPermission(user.getAddrUuid(), PermissionFactory.getSingleAddressPermissionTemplate("address-uuid-1"), null);
		s.grantObjectPermission(user.getAddrUuid(), PermissionFactory.getSingleObjectPermissionTemplate("object-uuid-1"), null);

		Assert.assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-1")), true);
		Assert.assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-1")), true);
		Assert.assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-2")), false);
		Assert.assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-2")), false);

		s.revokeAddressPermission(user.getAddrUuid(), PermissionFactory.getSingleAddressPermissionTemplate("address-uuid-1"), null);
		s.revokeObjectPermission(user.getAddrUuid(), PermissionFactory.getSingleObjectPermissionTemplate("object-uuid-1"), null);

		Assert.assertEquals(s.hasPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-1")), false);
		Assert.assertEquals(s.hasPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-1")), false);

		s.grantObjectPermission(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-1"), null);
		Assert.assertEquals(s.hasInheritedPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-3")), true);
		s.revokeObjectPermission(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-1"), null);
		Assert.assertEquals(s.hasInheritedPermissionForObject(user.getAddrUuid(), PermissionFactory
				.getSingleObjectPermissionTemplate("object-uuid-3")), false);

		s.grantAddressPermission(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-1"), null);
		Assert.assertEquals(s.hasInheritedPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-3")), true);
		s.revokeAddressPermission(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-1"), null);
		Assert.assertEquals(s.hasInheritedPermissionForAddress(user.getAddrUuid(), PermissionFactory
				.getSingleAddressPermissionTemplate("address-uuid-3")), false);

		s.grantUserPermission(user.getAddrUuid(), PermissionFactory.getPermissionTemplateCreateRoot(), null);
		Assert.assertEquals(s.hasUserPermission(user.getAddrUuid(), PermissionFactory.getPermissionTemplateCreateRoot()), true);
		s.revokeUserPermission(user.getAddrUuid(), PermissionFactory.getPermissionTemplateCreateRoot(), null);
		Assert.assertEquals(s.hasUserPermission(user.getAddrUuid(), PermissionFactory.getPermissionTemplateCreateRoot()), false);

		this.commitTransaction();
	}

}
