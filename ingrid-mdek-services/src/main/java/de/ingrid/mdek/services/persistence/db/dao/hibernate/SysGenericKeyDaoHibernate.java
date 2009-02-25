package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StringType;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;

/**
 * Hibernate-specific implementation of the <tt>ISysGenericKeyDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysGenericKeyDaoHibernate
	extends GenericHibernateDao<SysGenericKey>
	implements  ISysGenericKeyDao {

    public SysGenericKeyDaoHibernate(SessionFactory factory) {
        super(factory, SysGenericKey.class);
    }

	public List<SysGenericKey> getSysGenericKeys(String[] keyNames) {
		Session session = getSession();

		boolean selectKeys = false;
		if (keyNames != null && keyNames.length > 0) {
			selectKeys = true;
		}

		String sql = "select distinct genericKey from SysGenericKey genericKey";
		if (selectKeys) {
			sql += " where genericKey.keyName in (:keyNameList)";
		}
		
		Query q = session.createQuery(sql);
		if (selectKeys) {
			q.setParameterList("keyNameList", keyNames, new StringType());
		}
		
		return q.list();
	}
}
