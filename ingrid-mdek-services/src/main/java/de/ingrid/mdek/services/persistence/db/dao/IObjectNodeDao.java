package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.utils.IngridDocument;



/**
 * Business DAO operations related to the <tt>ObjectNode</tt> entity.
 * 
 * @author Martin
 */
public interface IObjectNodeDao
	extends IGenericDao<ObjectNode> {
	
	/** Load object NODE with given uuid. Also prefetch concrete object instance in node if requested.
	 * @param uuid object uuid
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion);

	/** Get root objects. */
	List<ObjectNode> getTopObjects();

	/**
	 * Fetches sub nodes (next level) of parent with given uuid. 
	 * Also prefetch concrete object instance in nodes if requested.
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren);

	/** Get total number of subobjects in subtree (all levels) */
	int countSubObjects(String parentUuid);

	/** Get Path of UUIDS in tree starting at root, INCLUDING given uuid. */
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
	 * WORK/RESPONSIBLE PAGE: Get ALL Objects where WORKING VERSION matches given selection criteria. 
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param selectionType selection criteria (see Enum)
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	IngridDocument getWorkObjects(String userUuid, 
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits);

	/**
	 * QA PAGE: Get ALL Objects where given user is QA and objects WORKING VERSION matches given selection criteria.
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param userUuid QA user
	 * @param isCatAdmin true = the user is the catadmin, has to be determined outside of this dao  
	 * @param permHandler permission handler needed for checking QA permissions
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	IngridDocument getQAObjects(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			int startHit, int numHits);

	/**
	 * STATISTICS PAGE: Get statistics info about the tree branch of the given object.
	 * @param parentUuid top object of tree branch
	 * @param selectionType what kind of statistic analysis
	 * @param startHit paging: hit to start with (first hit is 0)
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @return doc containing statistic info according to protocol
	 */
	IngridDocument getObjectStatistics(String parentUuid, 
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits);
}
