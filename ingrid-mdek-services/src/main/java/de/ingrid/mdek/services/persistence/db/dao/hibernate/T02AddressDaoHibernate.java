package de.ingrid.mdek.services.persistence.db.dao.hibernate;

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
}
