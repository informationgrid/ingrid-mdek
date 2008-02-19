package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * Business DAO operations related to the <tt>AddressNode</tt> entity.
 * 
 * @author Martin
 */
public interface IAddressNodeDao
	extends IGenericDao<AddressNode> {
	
	/** Load address with given uuid. Returns null if not found.	 */
	AddressNode loadByUuid(String uuid);

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
	AddressNode getAddrDetails(String uuid);

	/** Load parent of address with given uuid. Returns null if top node.  */
	AddressNode getParent(String uuid);

	/** Fetch Objects referencing the address with the passed uuid */
	List<ObjectNode> getObjectReferencesFrom(String addressUuid);
}
