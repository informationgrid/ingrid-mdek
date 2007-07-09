package de.ingrid.mdek.services.persistence.db;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

public class HQLExecuterTest extends AbstractDaoTest {

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();

		beginNewTransaction();

		// create metadata
		Metadata metadata = new Metadata("testKey", "testValue");
		GenericHibernateDao<Metadata, String> dao = new GenericHibernateDao<Metadata, String>(
				getSessionFactory(), Metadata.class);
		dao.makePersistent(metadata);
		commitTransaction();

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
		commitAndBeginnNewTransaction();

		assertEquals(2, response.size());
		assertEquals(true, response.getBoolean(IHQLExecuter.HQL_STATE));
		List objects = (List) response.get("from Metadata.1");
		assertNotNull(objects);
		assertTrue(objects.size() == 1);
		assertEquals("testKey#testValue", objects.get(0).toString());
		
		pairList.remove(selectPair);
		pairList.add(updatePair);
		response = executer.execute(document);
		commitAndBeginnNewTransaction();

		pairList.add(selectPair);
		pairList.remove(updatePair);
		response = executer.execute(document);

		assertEquals(2, response.size());
		assertEquals(true, response.getBoolean(IHQLExecuter.HQL_STATE));
		objects = (List) response.get("from Metadata.1");
		assertNotNull(objects);
		assertTrue(objects.size() == 1);
		assertEquals("testKey#foo bar", objects.get(0).toString());
		

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

		System.out.println(response);
		assertEquals(3, response.size());

		List objects = (List) response.get("from Metadata.1");
		assertEquals(1, objects.size());
		assertEquals("testKey#testValue", objects.get(0).toString());

		list.clear();
		list.add(selectPair);

		document.clear();
		list.clear();
		response.clear();
		list.add(selectPair);
		document.put(IHQLExecuter.HQL_QUERIES, list);
		response = executer.execute(document);
		System.out.println(response);
		objects = (List) response.get("from Metadata.1");
		assertEquals(0, objects.size());

	}
}
