package de.ingrid.mdek;

import java.util.List;

import de.ingrid.mdek.MdekErrors.MdekError;
import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller {

	/** How much data to fetch from requested entity ? */
	// TODO implement other quantities of fetching object ?
	public enum Quantity {
		DETAIL_ENTITY // client: edit dialogue -> request maximum data
	}

//	IngridDocument testMdekEntity(int threadNumber);

	/** Returns a map containing the entries of lists with given ids.
	 * Pass null as languageCode if it doesn't matter. */
	IngridDocument getSysLists(Integer[] listIds, Integer languageCode,
			String userId);

	/**
	 * Fetch single object with given uuid.
	 * @param uuid object uuid
	 * @param howMuch how much data to fetch from object
	 * @return response containing result: map representation of object containing requested data
	 */
	IngridDocument fetchObject(String uuid, Quantity howMuch,
			String userId);

	/**
	 * Create or store object INTO WORKING COPY !
	 * @param obj map representation of object.
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore immediately refetch Object after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument storeObject(IngridDocument obj,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Create or store object INTO PUBLISHED VERSION ! PERFORMS CHECKS ON DATA !
	 * @param obj map representation of object.
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore true=fetch and return Object after store, false=no fetch, just store
	 * @param forcePublicationCondition apply restricted PubCondition to subobjects (true)
	 * 		or receive Error when subobjects PubCondition conflicts (false)
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument publishObject(IngridDocument obj,
			boolean refetchAfterStore,
			boolean forcePublicationCondition,
			String userId);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 * @param uuid object uuid
	 * @return response containing result: map containing info whether object was fully deleted
	 */
	IngridDocument deleteObjectWorkingCopy(String uuid,
			String userId);

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object non existent afterwards !
	 * @param uuid object uuid
	 * @return response containing result: map containing info about success
	 */
	IngridDocument deleteObject(String uuid,
			String userId);

	/**
	 * Fetch all top objects of tree.
	 * @return response containing result: map containing representations of all root objects
	 */
	IngridDocument fetchTopObjects(String userId);

	/**
	 * Fetch all sub objects of of object with given uuid
	 * @param uuid object uuid
	 * @return response containing result: map containing representations of all sub objects
	 */
	IngridDocument fetchSubObjects(String uuid,
			String userId);

	/**
	 * Get Path of object in tree starting at root
	 * @param uuid object uuid
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getObjectPath(String uuid,
			String userId);

	/**
	 * Check whether operations with the subtree of the given object
	 * are permitted or prohibited (e.g. no rights, subtree has working copies ...)
	 * @param uuid object uuid of top node
	 * @return response containing result: map containing info about examination
	 * (has working copies, uuid of found working copy, number checked objects ...)
	 */
	IngridDocument checkObjectSubTree(String uuid,
			String userId);

	/**
	 * Copy an object to another parent.
	 * @param fromUuid uuid of node to copy
	 * @param toUuid uuid of parent where to copy to (new subnode)
	 * @param copySubtree true=also copy subtree, false=only object without subObjects
	 * @param userId current user to track jobs of user
	 * @return response containing result: map containing basic data of copied object
	 * and additional info (number of copied objects ...)
	 */
	IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree,
			String userId);

	/**
	 * Move an object with its subtree to another parent.
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
	IngridDocument moveObject(String fromUuid, String toUuid, boolean performSubtreeCheck,
			boolean forcePublicationCondition,
			String userId);

	/**
	 * Get initial data for a new object. Pass data needed to determine initial data (e.g. uuid of parent).
	 * @param newBasicObject basic new object with data needed to determine initial data, e.g. parent uuid ...
	 * @return extended newObject, e.g. containing terms of parent etc.
	 */
	IngridDocument getInitialObject(IngridDocument newBasicObject,
			String userId);

	/**
	 * Fetch The catalog object, represented by an CATALOG_MAP type in xsd.
	 * @return response containing result: map representation of the catalog object
	 */
	IngridDocument fetchCatalog(String userId);

	/**
	 * Returns information about currently running job of passed user.
	 * @param userId user identifier
	 * @return response containing result: map containing job infos or empty map if no job running !
	 */
	IngridDocument getRunningJobInfo(String userId);

	/**
	 * Cancel the currently running job of passed user.
	 * @param userId user identifier
	 * @return response containing result: map containing infos about canceled job, or empty map if no job running
	 * (or null if something went wrong)<br>
	 */
	IngridDocument cancelRunningJob(String userId);

	// uncomment when needed ! then recheck functionality and implementation !
//	IngridDocument fetchTopAddresses();
//	IngridDocument fetchSubAddresses(String adrUuid);

	/**
	 * Get pure requested result data from response of mdek call (without "protocol" overhead in response).
	 * @param mdekResponse response from mdek call
	 * @return null if errors occured otherwise IngridDocument containing results
	 */
	IngridDocument getResultFromResponse(IngridDocument mdekResponse);

	/**
	 * Get "Global" Error Message from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return global error message, should never be null and
	 * contains exception string when unknown error occured
	 */
	String getErrorMsgFromResponse(IngridDocument mdekResponse);

	/**
	 * Get Detailed Mdek Errors from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return list of detected errors, may be null when unknown error occured
	 * -> then <code>getErrorMsgFromResponse</code> contains exception
	 */
	List<MdekError> getErrorsFromResponse(IngridDocument mdekResponse);
}
