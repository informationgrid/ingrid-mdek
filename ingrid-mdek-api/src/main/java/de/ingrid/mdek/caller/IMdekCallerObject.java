/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.caller;

import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning OBJECT Manipulation.
 */
public interface IMdekCallerObject extends IMdekCaller {

	/**
	 * Fetch single object with given uuid.
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @param howMuch how much data to fetch from object
	 * @param whichEntityVersion which object version should be fetched.
	 * 		NOTICE: In published state working version == published version and it is the same object instance !
	 * Only ONE version is fetched (default is WORKING_VERSION) !
	 * @return response containing result: map representation of object containing requested data
	 */
	IngridDocument fetchObject(String plugId, String uuid, FetchQuantity howMuch,
			IdcEntityVersion whichEntityVersion, String userId);

	/**
	 * Create or store object INTO WORKING COPY !
	 * @param plugId which mdek server (iplug)
	 * @param obj map representation of object.
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore immediately refetch Object after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument storeObject(String plugId, IngridDocument obj,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Assign object to QA ! 
	 * @param plugId which mdek server (iplug)
	 * @param obj map representation of object.
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore immediately refetch Object after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument assignObjectToQA(String plugId, IngridDocument obj,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Assign object from QA back to author ! 
	 * @param plugId which mdek server (iplug)
	 * @param obj map representation of object.
	 * @param refetchAfterStore immediately refetch Object after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of updated object when refetching,
	 * 		otherwise map containing uuid of updated object  
	 */
	IngridDocument reassignObjectToAuthor(String plugId, IngridDocument obj,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Update partial data of Object ! NOTICE: No working version (copy) is created !!! If object is in state
	 * published and "working version" is updated, this IS ALSO THE PUBLISHED VERSION !!! Further no
	 * Modification User or date is set !!!  
	 * @param plugId which mdek server (iplug)
	 * @param objPart map representation of object part to update (new partial data to set, same structure as full object).
	 * 		Has to contain UUID !
	 * @param whichEntityVersion which object version should be updated.
	 * 		NOTICE: In published state working version == published version and it is the same object instance, 
	 * 		so update affects both !!!
	 * @return response containing result: result is null if update failed otherwise an empty IngridDoc
	 */
	IngridDocument updateObjectPart(String plugId, IngridDocument objPart,
			IdcEntityVersion whichEntityVersion, String userId);

	/**
	 * Create or store object INTO PUBLISHED VERSION ! PERFORMS CHECKS ON DATA !
	 * @param plugId which mdek server (iplug)
	 * @param obj map representation of object.
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore true=fetch and return Object after store, false=no fetch, just store
	 * @param forcePublicationCondition apply restricted PubCondition to subobjects (true)
	 * 		or receive Error when subobjects PubCondition conflicts (false)
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument publishObject(String plugId, IngridDocument obj,
			boolean refetchAfterStore,
			boolean forcePublicationCondition,
			String userId);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @param forceDeleteReferences only relevant if deletion of working copy causes FULL DELETION (no published version !)<br>
	 * 		true=all references to this object are also deleted
	 * 		false=error if references to this object exist
	 * @return response containing result: map containing info whether address was fully deleted, marked deleted ...
	 */
	IngridDocument deleteObjectWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId);

	/**
	 * FULL DELETE: different behavior when workflow enabled<br>
	 * - QA: full delete of object (working copy and published version) INCLUDING all subobjects !
	 * Object non existent afterwards !<br>
	 * - NON QA: object is just marked deleted and assigned to QA<br>
	 * If workflow disabled every user acts like a QA (when having write access)
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @param forceDeleteReferences how to handle references to this object ?<br>
	 * 		true=all references to this object are also deleted
	 * 		false=error if references to this object exist
	 * @return response containing result: map containing info whether address was fully deleted, marked deleted ...
	 */
	IngridDocument deleteObject(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId);

	/**
	 * Fetch all top objects of tree.
	 * @param plugId which mdek server (iplug)
	 * @return response containing result: map containing representations of all root objects
	 */
	IngridDocument fetchTopObjects(String plugId, String userId);

	/**
	 * Fetch all sub objects of of object with given uuid
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @return response containing result: map containing representations of all sub objects
	 */
	IngridDocument fetchSubObjects(String plugId, String uuid,
			String userId);

	/**
	 * Get Path of object in tree starting at root
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid = end node in path (included in path !)
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getObjectPath(String plugId, String uuid,
			String userId);

	/**
	 * Check whether operations with the subtree of the given object
	 * are permitted or prohibited (e.g. no rights, subtree has working copies ...)
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid of top node
	 * @return response containing result: map containing info about examination
	 * (has working copies, uuid of found working copy, number checked objects ...)
	 */
	IngridDocument checkObjectSubTree(String plugId, String uuid,
			String userId);

	/**
	 * Copy an object to another parent.
	 * @param plugId which mdek server (iplug)
	 * @param fromUuid uuid of node to copy
	 * @param toUuid uuid of parent where to copy to (new subnode)
	 * @param copySubtree true=also copy subtree, false=only object without subObjects
	 * @param userId current user to track jobs of user
	 * @return response containing result: map containing basic data of copied object
	 * and additional info (number of copied objects ...)
	 */
	IngridDocument copyObject(String plugId, String fromUuid, String toUuid, boolean copySubtree,
			String userId);

	/**
	 * Move an object with its subtree to another parent.
	 * @param plugId which mdek server (iplug)
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent
	 * @param forcePublicationCondition apply restricted PubCondition of new parent to
	 * 		subobjects (true) or receive Error when subobjects PubCondition conflicts (false)
	 * @return response containing result: map containing info (number of moved objects, permissions on moved object, ...)
	 */
	IngridDocument moveObject(String plugId, String fromUuid, String toUuid,
			boolean forcePublicationCondition,
			String userId);

	/**
	 * Get initial data for a new object. Pass data needed to determine initial data (e.g. uuid of parent).
	 * @param plugId which mdek server (iplug)
	 * @param newBasicObject basic new object with data needed to determine initial data, e.g. parent uuid ...
	 * @return extended newObject, e.g. containing terms of parent etc.
	 */
	IngridDocument getInitialObject(String plugId, IngridDocument newBasicObject,
			String userId);

	/**
	 * WORK/RESPONSIBLE PAGE: Get ALL Objects matching given selection criteria.<br>
	 * <b>NOTICE: Also queries <b>PUBLISHED</b> objects dependent from selectionType, see Enum and REDMINE-115!</b>
	 * @param plugId which mdek server (iplug)
	 * @param selectionType selection criteria (see Enum)
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return response containing result: map representation of objects (only partial data)
	 */
	IngridDocument getWorkObjects(String plugId,
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId);

	/**
	 * QA PAGE: Get ALL Objects where given user is QA and objects WORKING VERSION matches given selection criteria.
	 * @param plugId which mdek server (iplug)
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param orderBy how to order (see Enum)
	 * @param orderAsc true=order ascending, false=order descending
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return response containing result: map representation of objects (only partial data)
	 */
	IngridDocument getQAObjects(String plugId,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId);

	/**
	 * STATISTICS PAGE: Get statistics info about the tree branch of the given object.
	 * @param plugId which mdek server (iplug)
	 * @param parentUuid root of tree branch to get statistics from, pass null if whole catalog
	 * @param selectionType what kind of statistics
	 * @param startHit paging: hit to start with (first hit is 0)
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * 		NOTICE: paging ignored when STATISTICS_CLASSES_AND_STATES 
	 * @return response containing result: map containing statistics according to protocol
	 */
	IngridDocument getObjectStatistics(String plugId, String parentUuid,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits,
			String userId);
	
	/**
	 * Get the ISO XML representation of the document.
	 * @param plugId which mdek server (iplug)
	 * @param uuid the id of the document
	 * @param userId the id of the user who executes this command
	 * @returns the xml representation of the given document
	 */
	IngridDocument getIsoXml(String plugId, String uuid, IdcEntityVersion version, String userId);

	/**
	 * Update the index of a specific object located by its plugId and uuid.
	 * @param plugId which mdek server (iplug)
	 * @param uuid the uuid of the document
	 * @return response containing result: result returns the object id of the document
	 */
	IngridDocument updateObjectIndex(String plugId, String uuid);
}
