package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * Hibernate-specific implementation of the <tt>IT01ObjectDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T01ObjectDaoHibernate
	extends GenericHibernateDao<T01Object>
	implements  IT01ObjectDao {

    public T01ObjectDaoHibernate(SessionFactory factory) {
        super(factory, T01Object.class);
    }

	public List<T01Object> getAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		List<T01Object> retList = new ArrayList<T01Object>();

		Session session = getSession();

		retList = session.createQuery("select distinct o " +
			"from T01Object o " +
			"where o.responsibleUuid = ?")
			.setString(0, responsibleUserUuid)
			.list();

		return retList;
	}
	public String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		String hql = "select distinct o.objUuid, o.objName, o.workState " +
			"from T01Object o " +
			"where o.responsibleUuid = '" + responsibleUserUuid + "'";

		return hql;
	}
}
