package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.utils.IngridDocument;

public class AtomarModelPersisterTestLocal extends AbstractDaoTest {

	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractSingleSpringContextTests#onTearDown()
	 */
	@Override
	protected void onTearDown() throws Exception {
		super.onTearDown();
		DaoFactory factory = new DaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		IngridDocument document = persister.selectAll(IdcGroup.class);
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);

		List<Serializable> ids = new ArrayList<Serializable>();
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			// exclude default data
			if (!group.getName().equalsIgnoreCase("administrators")) {
				ids.add(group.getId());
			}
		}
		persister.delete(IdcGroup.class, ids);
		
	}

	@Test
	public void testInsert() throws Exception {

		DaoFactory factory = new DaoFactory(getSessionFactory());
		AtomarModelPersister persister = new AtomarModelPersister(factory);

		IngridDocument document = persister.selectAll(IdcGroup.class);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(2, document.size());

		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 10; i++) {
			IdcGroup group = new IdcGroup();
			group.setName("foo" + i);
			list.add(group);
		}

		document = persister.insert(IdcGroup.class, list);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(1, document.size());

		document = persister.selectAll(IdcGroup.class);
		assertNotNull(document);
		System.out.println(document);
		assertEquals(2, document.size());
		List instances = (List) document
				.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(10 < instances.size(), true);
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
		boolean renamed = false;
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			if (group.getName().startsWith("foo")) {
				group.setName("mb" + "->" + group.getName());
				renamed = true;
			}
		}
		assertTrue(renamed);
		persister.update(IdcGroup.class, instances);
		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		boolean checkOk = false;
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			if (group.getName().startsWith("mb->")) {
				checkOk = true;
			}
		}
		assertTrue(checkOk);
	}

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
		assertEquals(10 < instances.size(), true);

		List<Serializable> ids = new ArrayList<Serializable>();
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			// exclude default data
			if (!group.getName().equalsIgnoreCase("administrators")) {
				ids.add(group.getId());
			}
		}
		persister.delete(IdcGroup.class, ids);

		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(1, instances.size());
	}

}
