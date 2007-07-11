package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.ingrid.utils.IngridDocument;

public class AtomarModelPersisterTest extends AbstractDaoTest {

	@Test
	public void testInsert() throws Exception {

		TestDaoFactory factory = new TestDaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		IngridDocument document = persister.selectAll(Metadata.class);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(2, document.size());

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			list.add(new Metadata("foo" + i, "bar" + i));
		}

		document = persister.insert(Metadata.class, list);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(1, document.size());

		document = persister.selectAll(Metadata.class);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(2, document.size());
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(10, instances.size());
	}

	@Test
	public void testUpdate() throws Exception {
		TestDaoFactory factory = new TestDaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			list.add(new Metadata("foo" + i, "bar" + i));
		}

		persister.insert(Metadata.class, list);
		IngridDocument document = persister.selectAll(Metadata.class);
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		for (Object object : instances) {
			Metadata metadata = (Metadata) object;
			assertTrue(metadata.getMetadataValue().startsWith("bar"));
			metadata
					.setMetadataValue("mb" + "->" + metadata.getMetadataValue());
		}
		persister.update(Metadata.class, instances);
		document = persister.selectAll(Metadata.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		for (Object object : instances) {
			Metadata metadata = (Metadata) object;
			assertTrue(metadata.getMetadataValue().startsWith("mb->"));
		}
	}

	public void testDelete() throws Exception {
		TestDaoFactory factory = new TestDaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			list.add(new Metadata("foo" + i, "bar" + i));
		}

		persister.insert(Metadata.class, list);
		IngridDocument document = persister.selectAll(Metadata.class);
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(10, instances.size());

		List<Serializable> ids = new ArrayList<Serializable>();
		for (Object object : instances) {
			Metadata metadata = (Metadata) object;
			ids.add(metadata.getMetadataKey());
		}
		persister.delete(Metadata.class, ids);

		document = persister.selectAll(Metadata.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(0, instances.size());
	}

}
