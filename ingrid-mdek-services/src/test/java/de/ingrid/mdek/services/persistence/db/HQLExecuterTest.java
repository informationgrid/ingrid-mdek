package de.ingrid.mdek.services.persistence.db;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

public class HQLExecuterTest extends AbstractDaoTest {

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();

		// create metadata
		Metadata metadata = new Metadata("testKey", "testValue");
		GenericHibernateDao<Metadata> dao = new GenericHibernateDao<Metadata>(
				getSessionFactory(), Metadata.class);
		dao.beginTransaction();
		dao.makePersistent(metadata);
		dao.commitTransaction();

	}

	public void testUpdate() throws Exception {

		IngridDocument document = new IngridDocument();

		Pair selectPair = new Pair(IHQLExecuter.HQL_SELECT, "from Metadata");
		Pair updatePair = new Pair(
				IHQLExecuter.HQL_UPDATE,
				"update Metadata m set m._metadataValue = 'foo bar' where m._metadataKey is 'testKey'");
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
		assertEquals("from Metadata", pair.getKey());
		System.out.println(pair.getValue().getClass().getName());
		assertEquals("testKey#testValue", ((List) pair.getValue()).get(0)
				.toString());

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
		assertEquals("from Metadata", pair.getKey());
		assertEquals("testKey#foo bar", ((List) pair.getValue()).get(0)
				.toString());

	}

	public void testDelete() throws Exception {
		System.out.println("HQLExecuterTest.testDelete()");
		IngridDocument document = new IngridDocument();
		Pair selectPair = new Pair(IHQLExecuter.HQL_SELECT, "from Metadata");
		Pair deletePair = new Pair(IHQLExecuter.HQL_DELETE,
				"delete Metadata where _metadataKey is 'testKey'");
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
		assertEquals("from Metadata", pair.getKey());
		assertEquals("testKey#testValue", ((List) pair.getValue()).get(0)
				.toString());
		pair = (Pair) listResponse.get(1);
		assertEquals("delete Metadata where _metadataKey is 'testKey'", pair
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
		assertEquals("from Metadata", pair.getKey());
		assertTrue(((List) pair.getValue()).isEmpty());

	}
}
