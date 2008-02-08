package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;

/**
 * Business DAO operations related to the <tt>AddressNode</tt> entity.
 * 
 * @author Martin
 */
public interface IAddressNodeDao
	extends IGenericDao<AddressNode> {
	
	/** Get root addresses.
	 * @param onlyFreeAddresses true= only free top addresses, false=only NOT free top addresses
	 */
	List<AddressNode> getTopAddresses(boolean onlyFreeAddresses);

	/** Fetches sub address nodes of parent with given uuid.
	 * @param parentUuid uuid of parent
	 * @param fetchAddressLevel also fetch T02Address level encapsulated by AddressNode ?
	 * @return
	 */
	List<AddressNode> getSubAddresses(String parentUuid, boolean fetchAddressLevel);

	/** Fetches address with given uuid containing all detailed address data. */
	AddressNode getAdrDetails(String uuid);
}
