package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.utils.IngridDocument;



/**
 * Business DAO operations related to the <tt>ObjectNode</tt> entity.
 * 
 * @author Martin
 */
public interface IObjectNodeDao
	extends IGenericDao<ObjectNode> {
	
	/** Load object with given uuid. Returns null if not found.	 */
	ObjectNode loadByUuid(String uuid);

	/** Get root objects. */
	List<ObjectNode> getTopObjects();

	/** Fetches sub object nodes of parent with given uuid.
	 * @param parentUuid uuid of parent
	 * @param fetchObjectLevel also fetch T01Object level encapsulated by ObjectNode ?
	 * @return
	 */
	List<ObjectNode> getSubObjects(String parentUuid, boolean fetchObjectLevel);

	/** Get sub uuids of parent with given uuid (only next level) */
	List<String> getSubObjectUuids(String parentUuid);

	/** Get total number of subobjects in subtree (all levels) */
	int countSubObjects(String parentUuid);

	/** Get Path (list of uuids) of object in tree starting at root. */
	List<String> getObjectPath(String uuid);

	/** Checks whether the given uuid is ANYWHERE below the given parent uuid. */
	boolean isSubNode(String uuidToCheck, String uuidParent);

	/** Fetches object with given uuid containing all detailed object data. */
	ObjectNode getObjDetails(String uuid);

	/** Load parent of object with given uuid. Returns null if top node.  */
	ObjectNode getParent(String uuid);

	/** Fetch Objects referencing the object with the passed uuid.
	 * @param uuid 
	 * @return 2 List of objects:<br>
	 * - index 0: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version)<br>
	 * - index 1: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version)
	 */
	List<ObjectNode>[] getObjectReferencesFrom(String uuid);

	/** Query total number of objects associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @return number of found addresses
	 */
	long queryObjectsThesaurusTermTotalNum(String termSnsId);

	/**
	 * Query objects associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return list of found objects
	 */
	List<ObjectNode> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits);

	/** Query total number of objects containing passed term in full text index.
	 * @param searchTerm term to search for
	 * @return number of found objects
	 */
	long queryObjectsFullTextTotalNum(String searchTerm);

	/**
	 * Query objects containing passed term in full text index.
	 * @param searchTerm term to search for
	 * @param startHit hit to start with (first hit is 0) 
	 * @param numHits number of hits requested, beginning from startHit
	 * @return list of found objects
	 */
	List<ObjectNode> queryObjectsFullText(String searchTerm,
			int startHit, int numHits);

	
	/**
	 * Query objects according to the parameters supplied.
	 * 
	 * @param searchParams The parameters (see mdek_data.xsd -> SEARCH_EXT_PARAMS_MAP)
	 * @param numHits 
	 * @param startHit 
	 * @return list of found objects
	 */
	List<ObjectNode> queryObjectsExtended(IngridDocument searchParams, int startHit, int numHits);

	
	/**
	 * Query total number of objects according to the parameters supplied.
	 * 
	 * @param searchParams
	 * @return
	 */
	long queryObjectsExtendedTotalNum(IngridDocument searchParams);

}
