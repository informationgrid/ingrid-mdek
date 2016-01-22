/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

public class MetadataDaoTest extends AbstractDaoTest {

    @Test
	public void testSave() throws Exception {
		beginNewTransaction();

		IdcGroup group = new IdcGroup();
		group.setName("test group");

		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(
				getSessionFactory(), IdcGroup.class);

		dao.beginTransaction();
		IdcGroup byId = dao.getById(1234567890L);
		assertNull(byId);
		dao.makePersistent(group);
		dao.commitTransaction();

		dao.beginTransaction();

		byId = dao.getById(group.getId());
		Assert.assertNotNull(byId);
		Assert.assertEquals("test group", byId.getName());
	}
}
