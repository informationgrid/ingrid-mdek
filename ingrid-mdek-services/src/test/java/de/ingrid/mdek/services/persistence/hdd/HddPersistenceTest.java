/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.hdd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HddPersistenceTest  {

	protected File _testFolder = new File(System.getProperty("java.io.tmpdir"),
			"" + System.currentTimeMillis() + "_hdd");

	@BeforeEach
	public void setUp() throws Exception {
		assertTrue(_testFolder.mkdirs());
	}

	@AfterEach
	public void tearDown() throws Exception {
		File[] files = _testFolder.listFiles();
		for (File file : files) {
		    assertTrue(file.delete());
		}
		assertTrue(_testFolder.delete());
	}

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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
