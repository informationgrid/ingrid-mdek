package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.utils.IngridDocument;

public class AtomarModelPersisterTest extends AbstractDaoTest {

	@Test
	public void testInsert() throws Exception {

		DaoFactory factory = new DaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		IngridDocument document = persister.selectAll(IdcGroup.class);
		Assert.assertNotNull(document);
		System.out.println(document);
		Assert.assertEquals(2, document.size());

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			IdcGroup group = new IdcGroup();
			group.setName("foo" + i);
			list.add(group);
		}

		document = persister.insert(IdcGroup.class, list);
		Assert.assertNotNull(document);
		System.out.println(document);
		Assert.assertEquals(1, document.size());

		document = persister.selectAll(IdcGroup.class);
		Assert.assertNotNull(document);
		System.out.println(document);
		Assert.assertEquals(2, document.size());
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		Assert.assertTrue(instances.size() == 10);
	}

	@Test
	public void testUpdate() throws Exception {
		DaoFactory factory = new DaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			IdcGroup group = new IdcGroup();
			group.setName("foo" + i);
			list.add(group);
		}

		persister.insert(IdcGroup.class, list);
		IngridDocument document = persister.selectAll(IdcGroup.class);
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			Assert.assertTrue(group.getName().startsWith("foo"));
			group.setName("mb->" + group.getName());
		}
		persister.update(IdcGroup.class, instances);
		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			Assert.assertTrue(group.getName().startsWith("mb->"));
		}
	}

	@Test
	public void testDelete() throws Exception {
		DaoFactory factory = new DaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			IdcGroup group = new IdcGroup();
			group.setName("foo" + i);
			list.add(group);
		}

		persister.insert(IdcGroup.class, list);
		IngridDocument document = persister.selectAll(IdcGroup.class);
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		Assert.assertTrue(instances.size() == 10);

		List<Serializable> ids = new ArrayList<Serializable>();
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			ids.add(group.getId());
		}
		persister.delete(IdcGroup.class, ids);

		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		Assert.assertEquals(0, instances.size());
	}

}
