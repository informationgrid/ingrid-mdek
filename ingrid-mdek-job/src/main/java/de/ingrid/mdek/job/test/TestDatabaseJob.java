/*
 * **************************************************-
 * ingrid-mdek-job
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
package de.ingrid.mdek.job.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IAtomarModelPersister;
import de.ingrid.mdek.services.persistence.db.IDaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.IHQLExecuter;
import de.ingrid.mdek.services.persistence.db.test.TestMetadata;
import de.ingrid.utils.IngridDocument;

public class TestDatabaseJob implements IJob {

	private Logger _logger;
	private IAtomarModelPersister _persister;
	private IDaoFactory _daoFactory;
	private IHQLExecuter _hqlExecuter;
	private SessionFactory _sessionFactory;

	public TestDatabaseJob(ILogService logService,
			IAtomarModelPersister persister,
			IDaoFactory daoFactory,
			IHQLExecuter hqlExecuter,
			SessionFactory sessionFactory) {
		_logger = logService.getLogger(TestDatabaseJob.class);
		_persister = persister;
		_daoFactory = daoFactory;
		_hqlExecuter = hqlExecuter;
		_sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public IngridDocument getResults() {
		// called when job is registered ! return empty document
        IngridDocument result = new IngridDocument();
		return result;
	}

	public IngridDocument testDao() {
		GenericHibernateDao<TestMetadata> dao = new GenericHibernateDao<TestMetadata>(
				_sessionFactory, TestMetadata.class);
		
		TestMetadata metadata = new TestMetadata("testDaoKey", "testDaoValue");
		dao.beginTransaction();
		dao.makePersistent(metadata);
		dao.commitTransaction();

		dao.beginTransaction();
		TestMetadata byId = dao.getById(metadata.getId());

		IngridDocument result = new IngridDocument();
		result.put("metadata key", byId.getMetadataKey());
		result.put("metadata value", byId.getMetadataValue());
		
		// THROWS EXCEPTION WHEN TRANSMITTING !!!
//		result.put("metadata", byId);

		return result;
	}

	public IngridDocument testDaoFactory() {
		IGenericDao<IEntity> dao = _daoFactory.getDao(TestMetadata.class);

		TestMetadata metadata = new TestMetadata("testDaoFactoryKey", "testDaoFactoryValue");
		dao.beginTransaction();
		dao.makePersistent(metadata);
		dao.commitTransaction();

		dao.beginTransaction();
		TestMetadata byId = (TestMetadata) dao.getById(metadata.getId());

		IngridDocument result = new IngridDocument();
		result.put("metadata key", byId.getMetadataKey());
		result.put("metadata value", byId.getMetadataValue());
		
		// THROWS EXCEPTION WHEN TRANSMITTING !!!
//		result.put("metadata", byId);

		return result;
	}

	public IngridDocument testPersister() {
		ArrayList<IEntity> list = new ArrayList<IEntity>();
		for (int i = 0; i < 2; i++) {
			list.add(new TestMetadata("testPersisterKey" + i, "testPersisterValue" + i));
		}
		IngridDocument result = _persister.insert(TestMetadata.class, list);

		if (_logger.isInfoEnabled()) {
			_logger.info("persister.insert result: " + result);
		}

		result = _persister.selectAll(TestMetadata.class);
		List<TestMetadata> objs = (List<TestMetadata>) result.get(_persister.MODEL_INSTANCES);

		if (_logger.isInfoEnabled()) {
			_logger.info("persister.selectAll result: " + result);
		}

		ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(objs.size());
		for (TestMetadata o : objs) {
			IngridDocument doc = new IngridDocument();
			doc.put("key", o.getMetadataKey());
			doc.put("value", o.getMetadataValue());
			
			docList.add(doc);
		}

		IngridDocument ret = new IngridDocument();
		ret.put("result", docList);

		return ret;
	}

	public IngridDocument testHQLExecuter() {
		IngridDocument document = new IngridDocument();

		// SELECT
		System.out.println("### HQL SELECT ###");
		String hqlQuery = "from TestMetadata";
		System.out.println("HQL = " + hqlQuery);

		List<Pair> pairList = new ArrayList<Pair>();
		Pair selectPair = new Pair(IHQLExecuter.HQL_SELECT, hqlQuery);
		pairList.add(selectPair);
		
		document.put(IHQLExecuter.HQL_QUERIES, pairList);
		IngridDocument response = _hqlExecuter.execute(document);
		debugHQLResponse(response);

		commitAndBeginNewTransaction();
		
		// UPDATE
		System.out.println("### HQL UPDATE ###");
		hqlQuery = "update TestMetadata m set m.metadataValue = 'testHQLValue' where m.metadataKey is 'testPersisterKey1'";
		System.out.println("HQL = " + hqlQuery);

		pairList = new ArrayList<Pair>();
		Pair updatePair = new Pair(IHQLExecuter.HQL_UPDATE, hqlQuery);
		pairList.add(updatePair);

		document.put(IHQLExecuter.HQL_QUERIES, pairList);
		response = _hqlExecuter.execute(document);
		debugHQLResponse(response);

		commitAndBeginNewTransaction();

		// SELECT
		System.out.println("### HQL SELECT ###");
		pairList.remove(updatePair);
		pairList.add(selectPair);
		response = _hqlExecuter.execute(document);
		debugHQLResponse(response);
		
		IngridDocument result = new IngridDocument();
//		result.put("metadata key", byId.getMetadataKey());
//		result.put("metadata value", byId.getMetadataValue());
		
		// THROWS EXCEPTION WHEN TRANSMITTING !!!
//		result.put("metadata", byId);

		return result;
	}

	protected void debugHQLResponse(IngridDocument response) {
		System.out.println("response = " + response);

		boolean state = response.getBoolean(IHQLExecuter.HQL_STATE);
		System.out.println("response state = " + state);
		Object exc = response.get(IHQLExecuter.HQL_EXCEPTION);
		System.out.println("response exception = " + exc);
		List<Pair> list = (List<Pair>) response.get(IHQLExecuter.HQL_RESULT);
		System.out.println("response result = " + list);
		for (int i=0; i<list.size(); i++) {
			Pair pair = list.get(i);
			System.out.println("response pair[" + i + "] key = " + pair.getKey());
			System.out.println("response pair[" + i + "] value = " + pair.getValue());
			System.out.println("response pair[" + i + "] value class = " + pair.getValue().getClass().getName());
		}
	}

	protected void commitTransaction() {
		if (_sessionFactory.getCurrentSession().getTransaction().isActive()) {
			_sessionFactory.getCurrentSession().getTransaction().commit();
		}
	}

	protected void beginNewTransaction() {
		if (!_sessionFactory.getCurrentSession().getTransaction().isActive()) {
			_sessionFactory.getCurrentSession().beginTransaction();
		}
	}

	protected void commitAndBeginNewTransaction() {
		commitTransaction();
		beginNewTransaction();
	}

}
