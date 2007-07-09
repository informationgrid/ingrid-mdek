package de.ingrid.mdek.services.persistence.hdd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

public class HddPersistenceTest extends TestCase {

	protected File _testFolder = new File(System.getProperty("java.io.tmpdir"),
			"" + System.currentTimeMillis() + "_hdd");

	protected void setUp() throws Exception {
		assertTrue(_testFolder.mkdirs());
	}

	protected void tearDown() throws Exception {
		File[] files = _testFolder.listFiles();
		for (File file : files) {
			assertTrue(file.delete());
		}
		assertTrue(_testFolder.delete());
	}

	public void testMakePersistent() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		assertEquals(0, _testFolder.listFiles().length);
		persistence.makePersistent("foo", map);
		assertEquals(1, _testFolder.listFiles().length);
		assertEquals("foo.ser", _testFolder.listFiles()[0].getName());
	}

	public void testMakePersistentFails() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		assertEquals(0, _testFolder.listFiles().length);
		persistence.makePersistent("foo", map);
		assertEquals(1, _testFolder.listFiles().length);
		assertEquals("foo.ser", _testFolder.listFiles()[0].getName());

		try {
			persistence.makePersistent("foo", map);
			fail();
		} catch (IOException e) {
			// nothong todo
		}
	}

	public void testMakeTransient() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		persistence.makePersistent("foo", map);
		assertEquals(1, _testFolder.listFiles().length);
		persistence.makeTransient("foo");
		assertEquals(0, _testFolder.listFiles().length);
	}

	public void testMakeTransientFails() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		persistence.makePersistent("foo", map);
		assertEquals(1, _testFolder.listFiles().length);
		persistence.makeTransient("foo");
		assertEquals(0, _testFolder.listFiles().length);

		try {
			persistence.makeTransient("foo");
			fail();
		} catch (IOException e) {
			// expected
		}
	}

	public void testFindAll() throws Exception {
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		List<HashMap> list = persistence.findAll();
		assertEquals(0, list.size());
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		persistence = new HddPersistenceService<HashMap>(_testFolder);
		persistence.makePersistent("foo1", map);
		persistence.makePersistent("foo2", map);
		persistence.makePersistent("foo3", map);
		list = persistence.findAll();
		assertEquals(3, list.size());
	}

	public void testFindById() throws Exception {
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		persistence = new HddPersistenceService<HashMap>(_testFolder);

		try {
			persistence.findById("foo1", true);
			fail();
		} catch (IOException e) {
			// expected
		}

		HashMap map2 = persistence.findById("foo1", false);
		assertNull(map2);

		persistence.makePersistent("foo1", map);

		map2 = persistence.findById("foo1", true);
		assertNotNull(map2);
		assertEquals(23, map2.get("testKey"));
	}

}
