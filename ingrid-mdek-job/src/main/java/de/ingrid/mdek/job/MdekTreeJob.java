package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

public class MdekTreeJob extends MdekJob {

    /** Logger configured via Properties. ONLY if no logger via logservice is specified
     * for same class !. If Logservice logger is specified, this one uses
     * Logservice configuration -> writes to separate logfile for this Job. */
//    private final static Log log = LogFactory.getLog(MdekTreeJob.class);

	/** logs in separate File (job specific log file) */
	protected Logger log;

	private GenericHibernateDao<T01Object> daoT01Object;

	public MdekTreeJob(ILogService logService,
			SessionFactory sessionFactory) {
		
		super(sessionFactory);
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekTreeJob.class); 

		daoT01Object = new GenericHibernateDao<T01Object> (
				sessionFactory, T01Object.class);
	}

	public IngridDocument testMdekEntity(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		// fetch parameters
		String name = (String) params.get(MdekKeys.TITLE);
		String descr = (String) params.get(MdekKeys.ABSTRACT);
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
		            log.debug("Thread 1 DELETING OBJECT:" + o.getId());
					daoT01Object.makeTransient(o);
//		            log.debug("Thread 1 UPDATE OBJECT:" + o.getId());
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

	public IngridDocument getTopObjects() {
		IngridDocument result = new IngridDocument();
		Session session = getSession();		

		beginTransaction();

		// fetch all at once (one select with outer joins)
		List objs = session.createQuery("from T01Object obj " +
			"left join fetch obj.t012ObjObjs " +
			"where obj.root = 1")
			.list();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		Iterator iter = objs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T01Object obj = (T01Object)iter.next();
			IngridDocument doc = mapper.mapT01Object(obj);
			boolean hasChild = false;
			if (obj.getT012ObjObjs().size() > 0) {
				hasChild = true;
			}
			doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(doc);
		}

		commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		Session session = getSession();		

		// extract parameters
		String uuid = (String) params.get(MdekKeys.UUID);

		beginTransaction();

		// enable filter -> fetch only hierarchical relations
		session.enableFilter("relationTypeFilter").setParameter("relationType", new Integer(0));

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createQuery("from T01Object obj " +
			"left join fetch obj.t012ObjObjs child " +
			"left join fetch child.t012ObjObjs " +
			"where obj.id = ?")
			.setString(0, uuid)
			.uniqueResult();

		session.disableFilter("relationTypeFilter");

		if (log.isDebugEnabled()) {
			log.debug("Fetched T01Object with SubObjects: " + o);			
		}

		Set subObjs = o.getT012ObjObjs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subObjs.size());
		Iterator iter = subObjs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T01Object subObj = (T01Object)iter.next();
			IngridDocument subDoc = mapper.mapT01Object(subObj);
			boolean hasChild = false;
			if (subObj.getT012ObjObjs().size() > 0) {
				hasChild = true;
			}
			subDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(subDoc);
		}

		commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getTopAddresses() {
		IngridDocument result = new IngridDocument();
		Session session = getSession();		

		beginTransaction();

		// fetch all at once (one select with outer joins)
		List adrs = session.createQuery("from T02Address adr " +
			"left join fetch adr.t022AdrAdrs " +
			"where adr.root = 1")
			.list();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		Iterator iter = adrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T02Address adr = (T02Address)iter.next();
			IngridDocument doc = mapper.mapT02Address(adr);
			boolean hasChild = false;
			if (adr.getT022AdrAdrs().size() > 0) {
				hasChild = true;
			}
			doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(doc);
		}

		commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		Session session = getSession();		

		// extract parameters
		String uuid = (String) params.get(MdekKeys.UUID);

		beginTransaction();

		// fetch all at once (one select with outer joins)
		T02Address a = (T02Address) session.createQuery("from T02Address adr " +
			"left join fetch adr.t022AdrAdrs child " +
			"left join fetch child.t022AdrAdrs " +
			"where adr.id = ?")
			.setString(0, uuid)
			.uniqueResult();

		if (log.isDebugEnabled()) {
			log.debug("Fetched T02Address with SubAddresses: " + a);			
		}

		Set subAdrs = a.getT022AdrAdrs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subAdrs.size());
		Iterator iter = subAdrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T02Address subAdr = (T02Address)iter.next();
			IngridDocument subDoc = mapper.mapT02Address(subAdr);
			boolean hasChild = false;
			if (subAdr.getT022AdrAdrs().size() > 0) {
				hasChild = true;
			}
			subDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(subDoc);
		}

		commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getObjAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		Session session = getSession();		

		// extract parameters
		String uuid = (String) params.get(MdekKeys.UUID);

		beginTransaction();

		// enable filter -> fetch only hierarchical relations
		session.enableFilter("relationTypeFilter").setParameter("relationType", new Integer(0));

		// fetch all at once (one select with outer joins)
		T01Object o = (T01Object) session.createCriteria(T01Object.class)
			.setFetchMode("t012ObjAdrs", FetchMode.JOIN)
			.add( Restrictions.idEq(uuid) )
			.uniqueResult();

		session.disableFilter("relationTypeFilter");

		if (log.isDebugEnabled()) {
			log.debug("Fetched T01Object with Addresses: " + o);			
		}

		Set subAdrs = o.getT012ObjAdrs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subAdrs.size());
		Iterator iter = subAdrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			resultList.add(mapper.mapT02Address((T02Address)iter.next()));
		}

		commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}
}
