package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;

/**
 * Hibernate-specific implementation of the <tt>IT02AddressDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T02AddressDaoHibernate
	extends GenericHibernateDao<T02Address>
	implements  IT02AddressDao {

    public T02AddressDaoHibernate(SessionFactory factory) {
        super(factory, T02Address.class);
    }

	public List<T01Object> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId) {
		Session session = getSession();
		
		String sql = "select distinct obj from T01Object obj " +
			"left join fetch obj.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = ?";
		
		if (referenceTypeId != null) {
			sql += " and objAdr.type = " + referenceTypeId;
		}

		List<T01Object> objs = session.createQuery(sql)
				.setString(0, addressUuid)
				.list();
		
		return objs;
	}
	public String getCsvHQLObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId) {
		String hql = "select distinct o.objUuid, o.objName, o.workState " +
			"from T01Object o " +
			"left join o.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = '" + addressUuid + "'";
		
		if (referenceTypeId != null) {
			hql += " and objAdr.type = " + referenceTypeId;
		}
		
		return hql;
	}

	public List<T02Address> getAllAddressesOfResponsibleUser(String responsibleUserUuid) {
		List<T02Address> retList = new ArrayList<T02Address>();

		Session session = getSession();

		retList = session.createQuery("select distinct a " +
			"from T02Address a " +
			"where a.responsibleUuid = ?")
			.setString(0, responsibleUserUuid)
			.list();

		return retList;
	}
	public String getCsvHQLAllAddressesOfResponsibleUser(String responsibleUserUuid) {
		String hql = "select distinct a.adrUuid, a.institution, a.lastname, a.firstname, a.workState " +
			"from T02Address a " +
			"where a.responsibleUuid = '" + responsibleUserUuid + "'";

		return hql;
	}
}
