package de.ingrid.mdek.caller;

import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning OBJECT Manipulation.
 */
public interface IMdekCallerObject {

	/**
	 * Fetch single object with given uuid.
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @param howMuch how much data to fetch from object
	 * @return response containing result: map representation of object containing requested data
	 */
	IngridDocument fetchObject(String plugId, String uuid, Quantity howMuch,
			String userId);

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
	 * @return response containing result: map containing info whether object was fully deleted
	 */
	IngridDocument deleteObjectWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId);

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object non existent afterwards !
	 * @param plugId which mdek server (iplug)
	 * @param uuid object uuid
	 * @param forceDeleteReferences how to handle references to this object ?<br>
	 * 		true=all references to this object are also deleted
	 * 		false=error if references to this object exist
	 * @return response containing result: map containing info about success
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
	 * @param performSubtreeCheck
	 * 	true=check whether move is possible (e.g. subtree contains no working copies)<br>
	 * 	false=no check, move subtree as it is -> <code>checkObjectSubTree</code> should be
	 * 	called before moving !
	 * @param forcePublicationCondition apply restricted PubCondition of new parent to
	 * 		subobjects (true) or receive Error when subobjects PubCondition conflicts (false)
	 * @return response containing result: map containing info (number of moved objects ...)
	 */
	IngridDocument moveObject(String plugId, String fromUuid, String toUuid, boolean performSubtreeCheck,
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
}
