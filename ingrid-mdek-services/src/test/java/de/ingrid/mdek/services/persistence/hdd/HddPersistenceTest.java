package de.ingrid.mdek.services.persistence.hdd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HddPersistenceTest  {

	protected File _testFolder = new File(System.getProperty("java.io.tmpdir"),
			"" + System.currentTimeMillis() + "_hdd");

	@Before
	public void setUp() throws Exception {
		Assert.assertTrue(_testFolder.mkdirs());
	}

	@After
	public void tearDown() throws Exception {
		File[] files = _testFolder.listFiles();
		for (File file : files) {
		    Assert.assertTrue(file.delete());
		}
		Assert.assertTrue(_testFolder.delete());
	}

	@Test
	public void testMakePersistent() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		Assert.assertEquals(0, _testFolder.listFiles().length);
		persistence.makePersistent("foo", map);
		Assert.assertEquals(1, _testFolder.listFiles().length);
		Assert.assertEquals("foo.ser", _testFolder.listFiles()[0].getName());
	}

	@Test
	public void testMakePersistentFails() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		Assert.assertEquals(0, _testFolder.listFiles().length);
		persistence.makePersistent("foo", map);
		Assert.assertEquals(1, _testFolder.listFiles().length);
		Assert.assertEquals("foo.ser", _testFolder.listFiles()[0].getName());

		try {
			persistence.makePersistent("foo", map);
			Assert.fail();
		} catch (IOException e) {
			// nothong todo
		}
	}

	@Test
	public void testMakeTransient() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		persistence.makePersistent("foo", map);
		Assert.assertEquals(1, _testFolder.listFiles().length);
		persistence.makeTransient("foo");
		Assert.assertEquals(0, _testFolder.listFiles().length);
	}

	@Test
	public void testMakeTransientFails() throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		persistence.makePersistent("foo", map);
		Assert.assertEquals(1, _testFolder.listFiles().length);
		persistence.makeTransient("foo");
		Assert.assertEquals(0, _testFolder.listFiles().length);

		try {
			persistence.makeTransient("foo");
			Assert.fail();
		} catch (IOException e) {
			// expected
		}
	}

	@Test
	public void testFindAll() throws Exception {
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		List<HashMap> list = persistence.findAll();
		Assert.assertEquals(0, list.size());
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		persistence = new HddPersistenceService<HashMap>(_testFolder);
		persistence.makePersistent("foo1", map);
		persistence.makePersistent("foo2", map);
		persistence.makePersistent("foo3", map);
		list = persistence.findAll();
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testFindById() throws Exception {
		IHddPersistence<HashMap> persistence = new HddPersistenceService<HashMap>(
				_testFolder);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("testKey", 23);
		persistence = new HddPersistenceService<HashMap>(_testFolder);

		try {
			persistence.findById("foo1", true);
			Assert.fail();
		} catch (IOException e) {
			// expected
		}

		HashMap map2 = persistence.findById("foo1", false);
		Assert.assertNull(map2);

		persistence.makePersistent("foo1", map);

		map2 = persistence.findById("foo1", true);
		Assert.assertNotNull(map2);
		Assert.assertEquals(23, map2.get("testKey"));
	}

}
