package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.utils.ige.MdekUtils.IdcEntityType;
import de.ingrid.utils.ige.MdekUtils.SearchtermType;



/**
 * Business DAO operations related to the <tt>SearchtermValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermValueDao
	extends IGenericDao<SearchtermValue> {

	/** Load SearchtermValue according to given values. If not found create AND save it !
	 * @param type type of term NEVER null
	 * @param term term name. always pass (used for loading FREE term; set when creating new term !) 
	 * @param alternateTerm alternate term name. Ignored for loading term, but set when creating/updating term !
	 * @param entryId syslist entry id of term (null if no syslist entry; used for loading INSPIRE term)
	 * @param termSns according bean (or null if NO SNS term)
	 * @param entityId connected to this object/address (used for loading FREE term).
	 * 		PASS NULL IF CONNECTION DOESN'T MATTER (or new FREE term should be created)
	 * @param entityType type of entity (object or address. used for loading FREE term).
	 * 		PASS NULL IF CONNECTION DOESN'T MATTER (or new FREE term should be created)
	 * @return persisted SearchtermValue (with Id)
	 */
	SearchtermValue loadOrCreate(String type,
		String term, String alternateTerm,
		Integer entryId,
		SearchtermSns termSns,
		Long entityId, IdcEntityType entityType);

	/** Load SearchtermValue according to given values. Returns null if not found.
	 * @param type type of term NEVER null
	 * @param term term name, pass for loading FREE term 
	 * @param entryId syslist entry id of term, pass for loading INSPIRE term
	 * @param searchtermSnsId sns id of term, pass for loading THESAURUS term
	 * @param entityId connected to this object/address, pass for loading FREE term
	 * @param entityType type of entity connected to (object or address), pass for loading FREE term
	 * @return found searchterm value or null
	 */
	SearchtermValue loadSearchterm(String type,
			String term, Integer entryId, Long searchtermSnsId,
			Long entityId, IdcEntityType entityType);

	/** Load SearchtermValues according to given parameters. Passed type determines how to
	 * fetch term. NOTICE: even for SNS terms multiple results can be delivered, e.g. "Messdaten"
	 * and "Meßdaten" were imported and comparison equals true in MySQL due to configuration of MySQL !
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
	/** Get all DISTINCT object ids where the given term is connected to */
	List<Long> getSearchtermObj_objIds(long searchtermValueId);

	/** Get all references to Addresses of given searchterm */
	List<SearchtermAdr> getSearchtermAdrs(long searchtermValueId);
	/** Get all DISTINCT address ids where the given term is connected to */
	List<Long> getSearchtermAdr_adrIds(long searchtermValueId);
}
