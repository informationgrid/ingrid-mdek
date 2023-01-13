/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.utils.IngridDocument;
import org.junit.jupiter.api.Test;

public class HQLExecuterTest extends AbstractDaoTest {

	IdcGroup group = null;
	
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();

		// create metadata
		group = new IdcGroup();
		group.setName("test group");
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(
				getSessionFactory(), IdcGroup.class);
		dao.beginTransaction();
		dao.makePersistent(group);
		dao.commitTransaction();

	}

	@Test
	public void testUpdate() throws Exception {

		IngridDocument document = new IngridDocument();

		Pair selectPair = new Pair(IHQLExecuter.HQL_SELECT, "from IdcGroup");
		Pair updatePair = new Pair(
				IHQLExecuter.HQL_UPDATE,
				"update IdcGroup m set m.name = 'test group 1' where m.name is 'test group'");
		List<Pair> pairList = new ArrayList<Pair>();

		HQLExecuter executer = new HQLExecuter(getSessionFactory());

		pairList.add(selectPair);
		document.put(IHQLExecuter.HQL_QUERIES, pairList);
		IngridDocument response = executer.execute(document);
		System.out.println(response);
		commitAndBeginnNewTransaction();

		assertEquals(2, response.size());
		assertEquals(true, response.getBoolean(IHQLExecuter.HQL_STATE));
		List<Pair> list = (List<Pair>) response.get(IHQLExecuter.HQL_RESULT);
		Pair pair = list.get(0);
		assertNotNull(pair);
		assertEquals("from IdcGroup", pair.getKey());
		System.out.println(pair.getValue().getClass().getName());
		assertEquals("test group", ((IdcGroup)((List) pair.getValue()).get(0)
				).getName());

		pairList.remove(selectPair);
		pairList.add(updatePair);
		response = executer.execute(document);
		commitAndBeginnNewTransaction();

		pairList.add(selectPair);
		pairList.remove(updatePair);
		response = executer.execute(document);

		assertEquals(2, response.size());
		assertEquals(true, response.getBoolean(IHQLExecuter.HQL_STATE));
		list = (List<Pair>) response.get(IHQLExecuter.HQL_RESULT);
		assertTrue(list.size() == 1);
		pair = list.get(0);
		assertNotNull(pair);
		assertEquals("from IdcGroup", pair.getKey());
		assertEquals("test group 1", ((IdcGroup)((List) pair.getValue()).get(0)
		).getName());

	}

	@Test
	public void testDelete() throws Exception {
		System.out.println("HQLExecuterTest.testDelete()");
		IngridDocument document = new IngridDocument();
		Pair selectPair = new Pair(IHQLExecuter.HQL_SELECT, "from IdcGroup");
		Pair deletePair = new Pair(IHQLExecuter.HQL_DELETE,
				"delete IdcGroup where name is 'test group'");
		List<Pair> list = new ArrayList<Pair>();
		list.add(selectPair);
		list.add(deletePair);

		document.put(IHQLExecuter.HQL_QUERIES, list);
		HQLExecuter executer = new HQLExecuter(getSessionFactory());

		IngridDocument response = executer.execute(document);

		commitAndBeginnNewTransaction();

		assertEquals(2, response.size());
		List listResponse = (List) response.get(IHQLExecuter.HQL_RESULT);
		assertEquals(2, listResponse.size());
		Pair pair = (Pair) listResponse.get(0);
		assertEquals("from IdcGroup", pair.getKey());
		assertEquals("test group", ((IdcGroup)((List) pair.getValue()).get(0)
		).getName());
		pair = (Pair) listResponse.get(1);
		assertEquals("delete IdcGroup where name is 'test group'", pair
				.getKey());
		assertEquals(1, pair.getValue());

		list.clear();
		list.add(selectPair);

		document.clear();
		list.clear();
		response.clear();
		list.add(selectPair);
		document.put(IHQLExecuter.HQL_QUERIES, list);
		response = executer.execute(document);
		System.out.println("-" + response);
		assertTrue(response.getBoolean(IHQLExecuter.HQL_STATE));
		listResponse = (List) response.get(IHQLExecuter.HQL_RESULT);
		assertEquals(1, listResponse.size());
		pair = (Pair) listResponse.get(0);
		assertEquals("from IdcGroup", pair.getKey());
		assertTrue(((List) pair.getValue()).size() == 0);

	}
}
