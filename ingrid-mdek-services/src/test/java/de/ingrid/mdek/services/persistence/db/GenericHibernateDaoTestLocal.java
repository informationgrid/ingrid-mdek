/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.StaleObjectStateException;
import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

public class GenericHibernateDaoTestLocal extends AbstractDaoTest {

	IdcGroup g1 = null;
	IdcGroup g2 = null;
	
	/**
	 * "test" prefix is neccessary because of deps from Spring?
	 */
	@Test
	public void testOptimisticLockingPersistent() {
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(getSessionFactory(), IdcGroup.class);

		g1 = new IdcGroup();
		g1.setName("test group 1");
		g2 = null;

		dao.beginTransaction();
		dao.makePersistent(g1);
		dao.commitTransaction();

		dao.beginTransaction();
		g2 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		dao.beginTransaction();
		g1 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		g2.setName("test group 2");
		g1.setName("test group 1");

		dao.beginTransaction();
		dao.makePersistent(g2);
		dao.commitTransaction();

		dao.beginTransaction();
		try {
			dao.makePersistent(g1);
			dao.commitTransaction();
			fail("We override a version that we doesn't know.");
		} catch (StaleObjectStateException e) {
			dao.rollbackTransaction();
			assertTrue(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractSingleSpringContextTests#onTearDown()
	 */
	@Override
	protected void onTearDown() throws Exception {
		super.onTearDown();
		
		if (g1 != null) {
			GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(getSessionFactory(), IdcGroup.class);
			dao.beginTransaction();
			g1 = dao.getById(g1.getId(), false);
			dao.makeTransient(g1);
			dao.commitTransaction();
		}
	}

	/**
	 * "test" prefix is neccessary because of deps from Spring?
	 */
	@Test
	public void testOptimisticLockingTransient() {
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(getSessionFactory(), IdcGroup.class);

		g1 = new IdcGroup();
		g1.setName("test group 1");
		g2 = null;

		dao.beginTransaction();
		dao.makePersistent(g1);
		dao.commitTransaction();

		dao.beginTransaction();
		g2 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		dao.beginTransaction();
		g1 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		g2.setName("test group 2");
		g1.setName("test group 1");

		dao.beginTransaction();
		dao.makePersistent(g2);
		dao.commitTransaction();
		dao.beginTransaction();
		try {
			dao.makeTransient(g1);
			dao.commitTransaction();
			fail("We delete a version that we doesn't know.");
		} catch (StaleObjectStateException e) {
			dao.rollbackTransaction();
			assertTrue(true);
		}
	}
}
