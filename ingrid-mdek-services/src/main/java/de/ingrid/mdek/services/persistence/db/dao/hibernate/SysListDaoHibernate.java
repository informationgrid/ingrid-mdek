package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;

/**
 * Hibernate-specific implementation of the <tt>ISysListDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysListDaoHibernate
	extends GenericHibernateDao<SysList>
	implements  ISysListDao {

    public SysListDaoHibernate(SessionFactory factory) {
        super(factory, SysList.class);
    }

	public List<Integer> getSysListIds() {
		Session session = getSession();

		String qString = "select distinct lstId from SysList order by lstId";

		return session.createQuery(qString).list();
	}

	public List<SysList> getSysList(int lstId, String language) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = ? ";

		if (language != null) {
			qString += "and langId = ? ";
		}
		qString += "order by line, entryId";

		Query q = session.createQuery(qString);
		q.setInteger(0, lstId);
		if (language != null) {
			q.setString(1, language);			
		}

		return q.list();
	}

	public List<String> getFreeListEntries(MdekSysList sysLst) {
		Session session = getSession();

		List<String> freeEntries = new ArrayList<String>();

		if (sysLst == MdekSysList.LEGIST) {
			String hql = "select distinct legistValue from T015Legist " +
				"where legistKey = " + MdekSysList.FREE_ENTRY.getDbValue() +
				" order by legistValue";

			freeEntries = session.createQuery(hql).list();
		}
		
		return freeEntries;
	}

	public List<IEntity> getEntitiesOfFreeListEntry(MdekSysList sysLst, String freeEntry) {
		Session session = getSession();

		List<IEntity> entities = new ArrayList<IEntity>();

		if (sysLst == MdekSysList.LEGIST) {
			String hql = "from T015Legist " +
				"where legistKey = " + MdekSysList.FREE_ENTRY.getDbValue() +
				" and legistValue = '" + freeEntry + "'";

			entities = session.createQuery(hql).list();
		}
		
		return entities;
	}
}
