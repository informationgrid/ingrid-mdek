package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

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

	/** Get sub uuids of parent with given uuid (only next level) */
	List<String> getSubAddressUuids(String parentUuid);

	/** Get total number of subaddresses in subtree (all levels) */
	int countSubAddresses(String parentUuid);

	/** Get Path of UUIDS in tree starting at root, INCLUDING given uuid. */
	List<String> getAddressPath(String uuid);

	/** Get Path of ORGANISATIONS in tree starting at root.
	 * @param uuid uuid of endNode of path
	 * @param includeEndNode determines whether endNode is included in path (true) or not (false).
	 * @return list of organizations as IngridDocs containing orga-name and address type. 
	 */
	List<IngridDocument> getAddressPathOrganisation(String uuid, boolean includeEndNode);

	/** Checks whether the given uuid is ANYWHERE below the given parent uuid. */
	boolean isSubNode(String uuidToCheck, String uuidParent);

	/** Fetches address with given uuid containing all detailed address data. */
	AddressNode getAddrDetails(String uuid);

	/** Load parent of address with given uuid. Returns null if top node.  */
	AddressNode getParent(String uuid);

	/** Get Objects (CHECK WORKING AND PUBLISHED VERSIONS) referencing the address with the passed uuid.
	 * @param addressUuid address which is referenced by objects
	 * @param startIndex objects referencing the address, start with this object (first object is index 0).<br>
	 * 	<i>NOTICE: Objects are processed in following order: FIRST the objects referencing the address ONLY 
	 * 	in their published version, then the ones referencing in their working version. So return list with 
	 * 	index 0 is filled first !!!</i>
	 * @param maxNum objects referencing the address, maximum number to fetch
	 * @return 2 List of objects:<br>
	 * - index 0: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version). This
	 * 		list has highest priority and will be "filled" before list below.<br>
	 * - index 1: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version). This list will only be "filled" if
	 * 		still objects needed after list above is filled.<br>
	 */
	List<ObjectNode>[] getObjectReferencesFrom(String addressUuid, int startIndex, int maxNum);

	/** Get objects (CHECK ONLY WORKING VERSION) referencing the address with the passed uuid.
	 * @param addressUuid the address being referenced
	 * @param referenceTypeId type of reference=entry id in syslist; PASS NULL, IF ALL TYPES !
	 * @return list of object nodes referencing the address in the given way (type)
	 */
	List<ObjectNode> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId);

	/** Get total number of addresses matching the given search parameters.
	 * @param searchParams search parameters (Key:Value pairs in map)
	 * @return number of found addresses
	 */
	long searchTotalNumAddresses(IngridDocument searchParams);

	/**
	 * Search Addresses according to given parameters in map.
	 * @param searchParams search parameters (Key:Value pairs in map)
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return list of found addresses
	 */
	List<AddressNode> searchAddresses(IngridDocument searchParams,
			int startHit, int numHits);

	/** Query total number of addresses associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @return number of found addresses
	 */
	long queryAddressesThesaurusTermTotalNum(String termSnsId);

	/**
	 * Query addresses associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return list of found addresses
	 */
	List<AddressNode> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits);

	/** Query total number of addresses containing passed term in full text index.
	 * @param searchTerm term to search for
	 * @return number of found addresses
	 */
	long queryAddressesFullTextTotalNum(String searchTerm);

	/**
	 * Query addresses containing passed term in full text index.
	 * @param searchTerm term to search for
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return list of found addresses
	 */
	List<AddressNode> queryAddressesFullText(String searchTerm,
			int startHit, int numHits);
	
	
	/**
	 * Query addresses according to the searchParams supplied.
	 * 
	 * @param searchParams The parameters (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * @param startHit
	 * @param numHits
	 * @return list of found addresses
	 */
	public List<AddressNode> queryAddressesExtended(IngridDocument searchParams, int startHit, int numHits);

	/**
	 * Query total number of addresses according to the parameters supplied.
	 * 
	 * @param searchParams
	 * @return
	 */
	long queryAddressesExtendedTotalNum(IngridDocument searchParams);

	/** Get ALL Addresses (also published ones) where given user is responsible user. */
	List<T02Address> getAllAddressesOfResponsibleUser(String responsibleUserUuid);
}
