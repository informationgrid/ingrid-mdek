package de.ingrid.mdek.services.persistence.db;

import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

public class MetadataDaoTestLocal extends AbstractDaoTest {

    @Test
	public void testSave() throws Exception {
		beginNewTransaction();

		IdcGroup group = new IdcGroup();
		group.setName("test group");

		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(
				getSessionFactory(), IdcGroup.class);

		dao.beginTransaction();
		IdcGroup byId = dao.getById(1234567890L);
		assertNull(byId);
		dao.makePersistent(group);
		dao.commitTransaction();

		dao.beginTransaction();

		byId = dao.getById(group.getId());
		assertNotNull(byId);
		assertEquals("test group", byId.getName());
	}
}
