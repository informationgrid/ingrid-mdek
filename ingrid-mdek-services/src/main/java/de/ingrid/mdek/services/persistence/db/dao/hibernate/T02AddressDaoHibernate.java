package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
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

	public List<T02Address> getTopAddresses() {
		Session session = getSession();

		// fetch top Addresses
		List adrs = session.createQuery("from T02Address adr " +
			"where adr.root = 1")
			.list();
		
		return adrs;
	}

	public Set<T02Address> getSubAddresses(String uuid) {
		Session session = getSession();

		T02Address a = (T02Address) session.createQuery("from T02Address adr " +
			"left join fetch adr.t022AdrAdrs child " +
			"left join fetch child.t022AdrAdrs " +
			"where adr.id = ?")
			.setString(0, uuid)
			.uniqueResult();
		
		Set<T02Address> adrs = a.getT022AdrAdrs();

		return adrs;
	}
}
