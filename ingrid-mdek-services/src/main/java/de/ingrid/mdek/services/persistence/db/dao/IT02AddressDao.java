package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;
import java.util.Set;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T02Address;

/**
 * Business DAO operations related to the <tt>T02Address</tt> entity.
 * 
 * @author Martin
 */
public interface IT02AddressDao
	extends IGenericDao<T02Address> {

	List<T02Address> getTopAddresses();
	
	/** Fetches sub addresses of address with given id */
	Set<T02Address> getSubAddresses(String uuid);
}
