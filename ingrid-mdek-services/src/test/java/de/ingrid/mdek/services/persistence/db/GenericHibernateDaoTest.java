/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.StaleObjectStateException;
import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.services.persistence.db.model.IdcGroup;

public class GenericHibernateDaoTest extends AbstractDaoTest {

	IdcGroup g1 = null;
	IdcGroup g2 = null;
	
	/**
	 * "test" prefix is neccessary because of deps from Spring?
	 */
	@Test
	public void testOptimisticLockingPersistent() {
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(getSessionFactory(), IdcGroup.class);

		g1 = new IdcGroup();
		g1.setName("test group 1");
		g2 = null;

		dao.beginTransaction();
		dao.makePersistent(g1);
		dao.commitTransaction();

		dao.beginTransaction();
		g2 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		dao.beginTransaction();
		g1 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		g2.setName("test group 2");
		g1.setName("test group 1");

		dao.beginTransaction();
		dao.makePersistent(g2);
		dao.commitTransaction();

		dao.beginTransaction();
		try {
			dao.makePersistent(g1);
			dao.commitTransaction();
			Assert.fail("We override a version that we doesn't know.");
		} catch (StaleObjectStateException e) {
			dao.rollbackTransaction();
			Assert.assertTrue(true);
		}
	}

	/**
	 * "test" prefix is neccessary because of deps from Spring?
	 */
	@Test
	public void testOptimisticLockingTransient() {
		GenericHibernateDao<IdcGroup> dao = new GenericHibernateDao<IdcGroup>(getSessionFactory(), IdcGroup.class);

		g1 = new IdcGroup();
		g1.setName("test group 1");
		g2 = null;

		dao.beginTransaction();
		dao.makePersistent(g1);
		dao.commitTransaction();

		dao.beginTransaction();
		g2 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		dao.beginTransaction();
		g1 = dao.getById(g1.getId(), false);
		dao.commitTransaction();

		g2.setName("test group 2");
		g1.setName("test group 1");

		dao.beginTransaction();
		dao.makePersistent(g2);
		dao.commitTransaction();
		dao.beginTransaction();
		try {
			dao.makeTransient(g1);
			dao.commitTransaction();
			Assert.fail("We delete a version that we doesn't know.");
		} catch (StaleObjectStateException e) {
			dao.rollbackTransaction();
			Assert.assertTrue(true);
		}
	}
}
