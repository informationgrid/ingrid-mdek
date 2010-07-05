package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

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

	public List<T01Object> getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		Session session = getSession();

		String hql = "select o " +
			"from T01Object o " +
			"where o.responsibleUuid = ?";
	
		Query q = session.createQuery(hql)
			.setString(0, responsibleUserUuid);
		if (maxNum != null) {
			q.setMaxResults(maxNum);
		}

		return q.setResultTransformer(new DistinctRootEntityResultTransformer())
			.list();
	}
	public String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		String hql = "select distinct o.objUuid, o.objName, o.workState " +
			"from T01Object o " +
			"where o.responsibleUuid = '" + responsibleUserUuid + "'";

		return hql;
	}
}
