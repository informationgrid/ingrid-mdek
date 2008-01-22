package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;



/**
 * Business DAO operations related to the <tt>SearchtermValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermValueDao
	extends IGenericDao<SearchtermValue> {

	/** Load SearchtermValue according to given values. Returns null if not found. 
	 * @param type
	 * @param term
	 * @param searchtermSnsId id of record in SearchtermSns
	 * @param objectId connected to this object
	 * @return SearchtermValue or null
	 */
	SearchtermValue loadSearchterm(String type, String term, Long searchtermSnsId, Long objId);

	/** Load Freien SearchtermValue according to given values. Returns null if not found. 
	 * @param term
	 * @param objectId connected to this object
	 * @return SearchtermValue or null
	 */
	SearchtermValue loadFreiSearchterm(String term, Long objId);

	/** Load Thesaurus SearchtermValue according to given values. Returns null if not found.
	 * Pass both search criteria or only one of em.
	 * @param term pass null if only searchtermSnsId
	 * @param searchtermSnsId id of record in SearchtermSns, pass null if only term
	 * @return SearchtermValue or null
	 */
	SearchtermValue loadThesaurusSearchterm(String term, Long searchtermSnsId);

	/** Load SearchtermValue according to given values. If not found create AND save it !
	 * @param type
	 * @param term
	 * @param termSns according bean (or null)
	 * @param objectId connected to this object
	 * @return persisted SearchtermValue (with Id)
	 */
	SearchtermValue loadOrCreate(String type, String term, SearchtermSns termSns, Long objectId);
}
