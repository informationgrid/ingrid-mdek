package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;

/**
 * Business DAO operations related to the <tt>T02Address</tt> entity.
 * 
 * @author Martin
 */
public interface IT02AddressDao
	extends IGenericDao<T02Address> {
	
	/** Get objects (no matter whether published or not !) referencing the address with the passed uuid.
	 * @param addressUuid the address being referenced
	 * @param referenceTypeId type of reference=entry id in syslist; PASS NULL, IF ALL TYPES !
	 * @return list of objects referencing the address in the given way (type)
	 */
	List<T01Object> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId);

	/** Get ALL Addresses (also published ones) where given user is responsible user. */
	List<T02Address> getAllAddressesOfResponsibleUser(String responsibleUserUuid);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLAllAddressesOfResponsibleUser(String responsibleUserUuid);
}
