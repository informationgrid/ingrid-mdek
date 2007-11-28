package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

public class MdekTreeJob extends MdekJob {

	GenericHibernateDao<T01Object> daoT01Object;

	public MdekTreeJob(ILogService logService,
			SessionFactory sessionFactory) {
		
		super(logService.getLogger(MdekTreeJob.class),
				sessionFactory);

		daoT01Object = new GenericHibernateDao<T01Object> (
				sessionFactory, T01Object.class);
	}

	public IngridDocument testMdekEntity(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		// fetch parameters
		String type = (String) params.get(MdekKeys.ENTITY_TYPE);
		String name = (String) params.get(MdekKeys.ENTITY_NAME);
		String descr = (String) params.get(MdekKeys.ENTITY_DESCRIPTION);
		Integer threadNumber = (Integer) params.get("THREAD_NUMBER");

		T01Object objTemplate = new T01Object();
		objTemplate.setObjName(name);
		objTemplate.setObjDescr(descr);

		beginTransaction();

		List<T01Object> objs = daoT01Object.findByExample(objTemplate);

		// thread 1 -> WAIT so we can test staled Object
		if (threadNumber == 1) {
			// wait time in ms
			long waitTime = 1000;
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < waitTime) {
				// do nothing
			}
		}

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		if (objs.size() > 0) {
			for (T01Object o : objs) {
				Integer oClass = o.getObjClass();
				oClass = (oClass == null ? 1 : oClass+1);
				o.setObjClass(oClass);
				
				if (threadNumber == 1) {
					// test update/deletion of staled Object !
					System.out.println("Thread 1 DELETING OBJECT:" + o.getId());
					daoT01Object.makeTransient(o);
//					System.out.println("Thread 1 UPDATE OBJECT:" + o.getId());
//					daoT01Object.makePersistent(o);
				} else {
					daoT01Object.makePersistent(o);
				}
				resultList.add(mapper.mapT01Object(o));
			}			
		} else {
			daoT01Object.makePersistent(objTemplate);
			
			T01Object o = daoT01Object.loadById(objTemplate.getId());
			resultList.add(mapper.mapT01Object(o));
		}

		commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);

		return result;
	}

	public IngridDocument getSubTree(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		// fetch parameters
		String uuid = (String) params.get(MdekKeys.ENTITY_UUID);
		Integer depth = (Integer) params.get(MdekKeys.DEPTH);
		String type = (String) params.get(MdekKeys.ENTITY_TYPE);

		beginTransaction();

		long startTime = System.currentTimeMillis();
		Session session = getSession();
		
//		T01Object o = daoT01Object.loadById(uuid);
		
		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createCriteria(T01Object.class)
			.setFetchMode("t012ObjObjs", FetchMode.JOIN)
			.setFetchMode("t012ObjAdrs", FetchMode.JOIN)
			.add( Restrictions.idEq(uuid) )
			.uniqueResult();

		System.out.println("Found Object: " + o.getId() + " / " + o.getObjName());

		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		Set subObjs = o.getT012ObjObjs();
		ArrayList<IngridDocument> resultObjs = new ArrayList<IngridDocument>(subObjs.size());
		Iterator iter = subObjs.iterator();
		while (iter.hasNext()) {
			resultObjs.add(mapper.mapT01Object((T01Object)iter.next()));
		}

		Set subAdrs = o.getT012ObjAdrs();
		ArrayList<IngridDocument> resultAdrs = new ArrayList<IngridDocument>(subAdrs.size());
		iter = subAdrs.iterator();
		while (iter.hasNext()) {
			resultAdrs.add(mapper.mapT02Address((T02Address)iter.next()));
		}

		commitTransaction();

		long endTime = System.currentTimeMillis();
		long neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");

		result.put(MdekKeys.OBJ_ENTITIES, resultObjs);
		result.put(MdekKeys.ADR_ENTITIES, resultAdrs);

		return result;
	}
}
