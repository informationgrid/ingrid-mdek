/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.utils.IngridDocument;
import org.junit.jupiter.api.Test;

public class AtomarModelPersisterTest extends AbstractDaoTest {

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
		assertTrue(instances.size() == 10);
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
			assertTrue(group.getName().startsWith("foo"));
			group.setName("mb->" + group.getName());
		}
		persister.update(IdcGroup.class, instances);
		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			assertTrue(group.getName().startsWith("mb->"));
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
		assertTrue(instances.size() == 10);

		List<Serializable> ids = new ArrayList<Serializable>();
		for (Object object : instances) {
			IdcGroup group = (IdcGroup) object;
			ids.add(group.getId());
		}
		persister.delete(IdcGroup.class, ids);

		document = persister.selectAll(IdcGroup.class);
		instances = (List) document.get(IAtomarModelPersister.MODEL_INSTANCES);
		assertEquals(0, instances.size());
	}

}
