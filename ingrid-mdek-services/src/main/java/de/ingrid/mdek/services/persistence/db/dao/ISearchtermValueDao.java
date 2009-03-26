package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;



/**
 * Business DAO operations related to the <tt>SearchtermValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermValueDao
	extends IGenericDao<SearchtermValue> {

	/** Load SearchtermValue according to given values. If not found create AND save it !
	 * @param type type of term NEVER null
	 * @param term term name
	 * @param entryId syslist entry id of term (null if no syslist entry)
	 * @param termSns according bean (or null if NO SNS term)
	 * @param entityId connected to this entity (object or address), PASS NULL IF CONNECTION DOESN'T MATTER
	 * @param entityType type of entity (object or address), PASS NULL IF CONNECTION DOESN'T MATTER
	 * @return persisted SearchtermValue (with Id)
	 */
	SearchtermValue loadOrCreate(String type, String term, Integer entryId,
		SearchtermSns termSns,
		Long entityId, IdcEntityType entityType);

	/** Load SearchtermValues according to given parameters. Passed type determines how to
	 * fetch term. NOTICE: even for SNS terms multiple results can be delivered, e.g. "Messdaten"
	 * and "Meﬂdaten" were imported and comparison equals true in MySQL due to configuration of MySQL !
	 * @param term pass if type FREI
	 * @param snsId pass if type UMTHES or GEMET
	 * @return list of terms or empty list if not found
	 */
	List<SearchtermValue> getSearchtermValues(SearchtermType type, String term, String snsId);

	/** get all REFERENCED searchterms of given type(s).
	 * NOTICE: only returns terms REFERENCED by Objects/Addresses (e.g. NOT unused thesaurus terms) !
	 * @param types pass types to fetch. Pass null or empty array if all types !
	 * @return list of searchterms.
	 * 	NOTICE: NOT distinct, e.g. same free searchterm may exist in multiple DIFFERENCT SearchtermValues !
	 */
	List<SearchtermValue> getSearchtermValues(SearchtermType[] types);

	/** Return number of objects referencing the given searchterm.*/
	long countObjectsOfSearchterm(long searchtermId);
	/** Return number of addresses referencing the given searchterm */
	long countAddressesOfSearchterm(long searchtermId);

	/** Get all references to Objects of given searchterm */
	List<SearchtermObj> getSearchtermObjs(long searchtermValueId);
	/** Get all references to Addresses of given searchterm */
	List<SearchtermAdr> getSearchtermAdrs(long searchtermValueId);
}
