package de.ingrid.mdek.services.persistence.db;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

public class HQLExecuter extends TransactionService implements IHQLExecuter {

	private static final Logger LOG = Logger.getLogger(HQLExecuter.class);

	public HQLExecuter(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	public IngridDocument execute(IngridDocument document) {

		// TODO optimistic locking
		IngridDocument ret = new IngridDocument();
		List<Pair> pairList = (List<Pair>) document.get(HQL_QUERIES);
		beginTransaction();
		Session session = getSession();
		int i = 0;
		for (Pair pair : pairList) {
			i++;
			String key = pair.getKey();
			String hqlString = (String) pair.getValue();
			try {
				Query query = session.createQuery(hqlString);
				Object result = null;
				if (key.equals(IHQLExecuter.HQL_SELECT)) {
					result = query.list();
				} else if (key.equals(IHQLExecuter.HQL_UPDATE)) {
					result = query.executeUpdate();
				} else if (key.equals(IHQLExecuter.HQL_DELETE)) {
					result = query.executeUpdate();
				}
				ret.put(hqlString + "." + i, result);
			} catch (HibernateException e) {
				if (LOG.isEnabledFor(Level.ERROR)) {
					LOG.error("error by execution of hqlQuery [" + hqlString
							+ "]", e);
				}
				ret.put(HQL_EXCEPTION, e.getMessage());
				ret.putBoolean(HQL_STATE, false);
				rollbackTransaction();
				return ret;
			}
		}
		commitTransaction();
		ret.putBoolean(HQL_STATE, true);
		return ret;
	}
}
