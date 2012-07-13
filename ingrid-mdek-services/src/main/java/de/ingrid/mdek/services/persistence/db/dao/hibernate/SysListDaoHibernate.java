package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.utils.ige.MdekUtils.MdekSysList;

/**
 * Hibernate-specific implementation of the <tt>ISysListDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysListDaoHibernate
	extends GenericHibernateDao<SysList>
	implements  ISysListDao {

	private static final Logger LOG = Logger.getLogger(SysListDaoHibernate.class);

    public SysListDaoHibernate(SessionFactory factory) {
        super(factory, SysList.class);
    }

	public List<Object[]> getSysListInfos() {
		Session session = getSession();

		String qString = "select distinct lstId, maintainable from SysList order by lstId";

		return session.createQuery(qString).list();
	}

	public List<SysList> getSysList(int lstId, String language) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = " + lstId;

		if (language != null) {
			qString += " and langId = '" + language + "'";
		}
		qString += " order by line, entryId";

		Query q = session.createQuery(qString);
		return q.list();
	}

	public List<String> getFreeListEntries(MdekSysList sysLst) {
		Session session = getSession();

		List<String> freeEntries = new ArrayList<String>();

		// extract table, key column and value column from description of syslist
		String[] listMetadata = sysLst.getMetadata();
		if (listMetadata.length == 3) {
			String tableName = listMetadata[0];
			String keyCol = listMetadata[1];
			String valueCol = listMetadata[2];

			String hql = "select distinct " + valueCol + " from " + tableName +
					" where " + keyCol + " = " + MdekSysList.FREE_ENTRY.getDbValue() +
					" order by " + valueCol;
			freeEntries = session.createQuery(hql).list();
		} else {
			LOG.error("Metadata of syslist " + sysLst.getDbValue() + " '" + sysLst + "' not set ! We cannot process free entries !");
		}
		
		return freeEntries;
	}

	public List<IEntity> getEntitiesOfFreeListEntry(MdekSysList sysLst, String freeEntry) {
		Session session = getSession();

		List<IEntity> entities = new ArrayList<IEntity>();

		// extract table, key column and value column from description of syslist
		String[] listMetadata = sysLst.getMetadata();
		if (listMetadata.length == 3) {
			String tableName = listMetadata[0];
			String keyCol = listMetadata[1];
			String valueCol = listMetadata[2];

			String hql = "from " + tableName + 
				" where " + keyCol + " = " + MdekSysList.FREE_ENTRY.getDbValue() +
				" and " + valueCol + " = '" + freeEntry + "'";
			entities = session.createQuery(hql).list();
		} else {
			LOG.error("Metadata of syslist " + sysLst.getDbValue() + " '" + sysLst + "' not set ! We cannot process free entries !");
		}
		
		return entities;
	}
}
