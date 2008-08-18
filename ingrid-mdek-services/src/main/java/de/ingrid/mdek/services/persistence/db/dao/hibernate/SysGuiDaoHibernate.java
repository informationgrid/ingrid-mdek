package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StringType;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.model.SysGui;

/**
 * Hibernate-specific implementation of the <tt>ISysGuiDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysGuiDaoHibernate
	extends GenericHibernateDao<SysGui>
	implements  ISysGuiDao {

    public SysGuiDaoHibernate(SessionFactory factory) {
        super(factory, SysGui.class);
    }

	public List<SysGui> getSysGuis(String[] guiIds) {
		Session session = getSession();

		boolean selectIds = false;
		if (guiIds != null && guiIds.length > 0) {
			selectIds = true;
		}

		String sql = "select distinct guiElem from SysGui guiElem";
		if (selectIds) {
			sql += " where guiElem.guiId in (:idList)";
		}
		
		Query q = session.createQuery(sql);
		if (selectIds) {
			q.setParameterList("idList", guiIds, new StringType());
		}
		
		return q.list();
	}
}
