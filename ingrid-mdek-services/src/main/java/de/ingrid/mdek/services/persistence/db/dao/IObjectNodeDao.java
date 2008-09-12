package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
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

	/** Fetches sub nodes (only next level) of parent with given uuid.
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

	/** Fetches WORKING VERSION of object with given uuid containing all detailed object data. */
	ObjectNode getObjDetails(String uuid);

	/** Fetches requested version of object with given uuid containing all detailed object data. */
	ObjectNode getObjDetails(String uuid, IdcEntityVersion whichEntityVersion);

	/** Load parent of object with given uuid. Returns null if top node.  */
	ObjectNode getParent(String uuid);

	/** Fetch Objects referencing the object with the passed uuid.
	 * @param uuid 
	 * @return 2 List of objects:<br>
	 * - index 0: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version)
	 * - index 1: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version)<br>
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

	/** Get ALL Objects (also published ones) where given user is responsible user. */
	List<T01Object> getAllObjectsOfResponsibleUser(String responsibleUserUuid);

	/**
	 * Get ALL Objects where given user is QA and WORKING VERSION is in given work state.
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param userUuid QA user
	 * @param isCatAdmin true = the user is the catadmin, has to be determined outside of this dao  
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param maxNum maximum number of objects to query, pass null if all objects !
	 * @return list of objects
	 */
	List<ObjectNode> getQAObjects(String userUuid, boolean isCatAdmin,
			WorkState whichWorkState, Integer maxNum);
}
