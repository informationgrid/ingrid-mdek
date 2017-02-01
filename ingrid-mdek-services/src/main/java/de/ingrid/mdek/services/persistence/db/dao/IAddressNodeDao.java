/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import java.util.HashMap;
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
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Business DAO operations related to the <tt>AddressNode</tt> entity.
 * 
 * @author Martin
 */
public interface IAddressNodeDao
	extends IGenericDao<AddressNode> {
	
	/** Load address NODE with given uuid. Also prefetch concrete address instance in node if requested.
	 * @param uuid address uuid
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	AddressNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion);

	/** Load address NODE with given ORIGINAL_ID (always queries WORKING VERSION !!!).
	 * Also prefetch concrete address instance in node if requested.
	 * @param origId address ORIGINAL_ID = id from external system
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return first node found or null if not found. Logs WARNING if multiple nodes found !!!
	 */
	AddressNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion);

	/**
	 * Get top root addresses. ALWAYS prefetches concrete address instance in nodes.
	 * @param onlyFreeAddresses true=only top free addresses, false=only top NON free addresses
	 * @param whichEntityVersion which address Version to prefetch in node. DO NOT PASS NULL, we need
	 * 		an address version for determining type of address (free address or not) ! DEFAULT IS WORKING VERSION !
	 * @param fetchSubNodesChildren also fetch children in fetched topnodes to determine whether subnodes ?
	 * @return
	 */
	List<AddressNode> getTopAddresses(boolean onlyFreeAddresses,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren);

	/**
	 * Fetches sub nodes (next level) of parent with given uuid. 
	 * Also prefetch concrete address instance in nodes if requested.
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	List<AddressNode> getSubAddresses(String parentUuid, 
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren);

	/**
	 * Fetches ALL sub nodes (whole branch) of parent with given uuid. 
	 * Also prefetch concrete address instance in nodes if requested.
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	List<AddressNode> getAllSubAddresses(String parentUuid, 
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
	List<AddressNode> getSelectedSubAddresses(String parentUuid,
			IdcChildrenSelectionType whichChildren,
			PublishType parentPubType);

	/**
	 * Get total number of subaddresses in subtree (all levels)
	 * @param parentUuid uuid of parent node
	 * @param versionOfSubAddressesToCount which subaddresses version should be counted e.g.:<br>
	 * 		WORKING_VERSION: count only subaddresses where working version != published version<br>
	 * 		PUBLISHED_VERSION: count only subaddresses where published version exists<br>
	 * 		ALL_VERSIONS: count all subaddresses, no matter in which version. If different address
	 * 			versions exist object is counted only once !
	 * @return
	 */
	int countAllSubAddresses(String parentUuid, IdcEntityVersion versionOfSubAddressesToCount);

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

	/** Fetches WORKING VERSION of address with given uuid containing all detailed address data. */
	AddressNode getAddrDetails(String uuid);

	/** Fetches requested version of address with given uuid containing all detailed address data. */
	AddressNode getAddrDetails(String uuid, IdcEntityVersion whichEntityVersion);

	/** Load parent of address with given uuid. Returns null if top node.  */
	AddressNode getParent(String uuid);

	/** Get Objects (CHECK WORKING AND PUBLISHED VERSIONS) referencing the address with the passed uuid.
	 * @param addressUuid address which is referenced by objects
	 * @param startIndex objects referencing the address, start with this object (first object is index 0).<br>
	 * 	<i>NOTICE: Objects are processed in following order: FIRST the objects referencing the address ONLY 
	 * 	in their published version, then the ones referencing in their working version. So return list with 
	 * 	index 0 is filled first !!!</i>
	 * @param maxNum objects referencing the address, maximum number to fetch
	 * @return HashMap containing info about referencing objects in following keys:<br>
	 * <i>OBJ_REFERENCES_FROM_TOTAL_NUM</i>: total number of objects referencing the address<br>
	 * <i>OBJ_REFERENCES_FROM</i>: 2 List of ObjectNodes passed inside an array:<br>
	 * - index 0: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version). This
	 * 		list has highest priority and will be "filled" before list below.<br>
	 * - index 1: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version). This list will only be "filled" if
	 * 		still objects needed after list above is filled.<br>
	 */
	HashMap getObjectReferencesFrom(String addressUuid, int startIndex, int maxNum);

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

	/**
	 * WORK/RESPONSIBLE PAGE: Get ALL Addresses matching given selection criteria. 
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param selectionType selection criteria (see Enum)
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	IngridDocument getWorkAddresses(String userUuid, 
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits);

	/**
	 * QA PAGE: Get ALL Addresses where given user is QA and addresses WORKING VERSION matches given selection criteria.
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param userUuid QA user
	 * @param isCatAdmin true = the user is the catadmin, has to be determined outside of this dao  
	 * @param permHandler permission handler needed for checking QA permissions
	 * @param whichWorkState only return addresses in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all addresses
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	IngridDocument getQAAddresses(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits);

	/**
	 * STATISTICS PAGE: Get statistics info about the tree branch of the given address.
	 * @param parentUuid root of tree branch to get statistics from, pass null if whole catalog
	 * @param onlyFreeAddresses only evaluated if passed parent is null -> 
	 * 		true=only free addresses, false=all addresses (whole catalog)
	 * @param selectionType what kind of statistic analysis
	 * @param startHit paging: hit to start with (first hit is 0)
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @return doc containing statistic info according to protocol
	 */
	IngridDocument getAddressStatistics(String parentUuid, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits);

	/** Get ALL Address Node UUIDs (distinct) */
	List<String> getAllAddressUuids();

	/** Get AddressNode with prefetched data for generating Index !!! */
	AddressNode getAddressForIndex(String uuid);
}
