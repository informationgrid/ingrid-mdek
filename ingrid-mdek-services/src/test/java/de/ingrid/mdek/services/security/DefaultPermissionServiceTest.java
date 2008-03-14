package de.ingrid.mdek.services.security;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.DummyDaoFactory;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

public class DefaultPermissionServiceTest extends AbstractPermissionTest {

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.services.security.AbstractPermissionTest#onSetUp()
	 */
	@Override
	protected void onSetUp() throws Exception {
		// TODO Auto-generated method stub
		super.onSetUp();
        
        GenericHibernateDao<IEntity> dao = new GenericHibernateDao<IEntity>(getSessionFactory(), IEntity.class);
        IdcGroup group = this.createIdcGroup(null, "group1");
        this.beginNewTransaction();
        dao.makePersistent(group);
        this.commitAndBeginnNewTransaction();
        IdcUser user = this.createIdcUser(null, group.getId(), "addr1", 1, null);
        dao.makePersistent(user);
        this.commitAndBeginnNewTransaction();
        EntityPermission ep = PermissionFactory.getSingleAddressPermission("adr_uuid_1"); 
        dao.makePersistent(ep.getPermission());
        this.commitAndBeginnNewTransaction();
        
        PermissionAddr pa = new PermissionAddr();
        pa.setId(null);
        pa.setIdcGroupId(group.getId());
        pa.setPermissionId(ep.getPermission().getId());
        pa.setUuid(ep.getUuid());
        
        dao.makePersistent(pa);
        this.commitTransaction();
	}

	

	@Test
	public void testHasPermissionForAddress() {
		
		IPermissionService s = getPermissionService();
		this.beginNewTransaction();
		assertEquals(s.hasPermissionForAddress("addr1", PermissionFactory.getSingleAddressPermission("adr_uuid_1")), true);
		this.commitTransaction();
	}
}
