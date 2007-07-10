package de.ingrid.mdek.services.persistence.db;

import de.ingrid.utils.IngridDocument;

public class MetadataDaoTest extends AbstractDaoTest {

	public void testSave() throws Exception {
		beginNewTransaction();

		Metadata metadata = new Metadata("testKey", "testValue");

		GenericHibernateDao<Metadata, String> dao = new GenericHibernateDao<Metadata, String>(
				getSessionFactory(), Metadata.class);

		dao.beginTransaction();
		Metadata byId = dao.getById(IngridDocument.class.getName());
		assertNull(byId);
		dao.makePersistent(metadata);
		dao.commitTransaction();
		
		dao.beginTransaction();

		byId = dao.getById("testKey");
		assertNotNull(byId);
		assertEquals("testKey", byId.getMetadataKey());
		assertEquals("testValue", byId.getMetadataValue());
	}
}
