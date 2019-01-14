/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
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
	 * @return node or null if not found. Throws Exception if multiple nodes found !
	 */
	ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion);

	/** Load object NODE with given ORIGINAL_ID (always queries WORKING VERSION !!!).
	 * Also prefetch concrete object instance in node if requested.
	 * @param origId object ORIGINAL_ID = id from external system
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return first node found or null if not found. Logs WARNING if multiple nodes found !!!
	 */
	ObjectNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion);

	/**
	 * Get top root objects. Also prefetch concrete object instance in nodes if requested.
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched topnodes to determine whether subnodes ?
	 * @return
	 */
	List<ObjectNode> getTopObjects(IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren);

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

	/**
	 * Fetches ALL sub nodes (whole branch) of parent with given uuid. 
	 * Also prefetch concrete object instance in nodes if requested.
	 * @param parentUuid uuid of branch root node
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	List<ObjectNode> getAllSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren);

	/**
	 * Fetches selected sub nodes in branch MATCHING special selection criteria !!!
	 * The according entity instances (work/publish version) are prefetched, according to selection criteria.
	 * @param parentUuid uuid of branch root node
	 * @param whichChildren selection criteria children have to match 
	 * @param parentPubType publication condition of branch root node, needed 
	 * 		when selecting children where pub condition does not match !
	 * @return
	 */
	List<ObjectNode> getSelectedSubObjects(String parentUuid,
			IdcChildrenSelectionType whichChildren,
			PublishType parentPubType);

	/**
	 * Get total number of subobjects in subtree (all levels)
	 * @param parentUuid uuid of parent node
	 * @param versionOfSubObjectsToCount which subobjects version should be counted e.g.:<br>
	 * 		WORKING_VERSION: count only subobjects where working version != published version<br>
	 * 		PUBLISHED_VERSION: count only subobjects where published version exists<br>
	 * 		ALL_VERSIONS: count all subobjects, no matter in which version. If different object
	 * 			versions exist object is counted only once !
	 * @return
	 */
	int countAllSubObjects(String parentUuid, IdcEntityVersion versionOfSubObjectsToCount);

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

	/**
	 * WORK/RESPONSIBLE PAGE: Get ALL Objects matching given selection criteria. 
	 * We return nodes, so we can evaluate whether published version exists !
	 * NOTICE: IdcWorkEntitiesSelectionType WAS EXPANDED TO ALSO SELECT published Objects (for dashboard), see enumeration !
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
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	IngridDocument getQAObjects(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
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

	/**
	 * Find objects marked with given export criterion
	 * @param exportCriterion "tagged value" used to mark objects for export
	 * @return uuids of objects to export
	 */
	List<String> getObjectUuidsForExport(String exportCriterion);

	/** Get ALL Object Node UUIDs (distinct) */
	List<String> getAllObjectUuids();

	/** Get ObjectNode with prefetched data for generating Index !!! */
	ObjectNode getObjectForIndex(String uuid);
}
