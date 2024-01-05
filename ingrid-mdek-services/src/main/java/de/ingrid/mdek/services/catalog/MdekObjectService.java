/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.EntityHelper;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler.GroupType;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to object entity data.
 */
public class MdekObjectService {

	private static final Logger LOG = LogManager.getLogger(MdekObjectService.class);

	private static MdekObjectService myInstance;

	private IObjectNodeDao daoObjectNode;
	private IT01ObjectDao daoT01Object;
	private IGenericDao<IEntity> daoObjectReference;

	private MdekCatalogService catalogService;
	private MdekAddressService addressService;
	private MdekTreePathHandler pathHandler;
	private MdekFullIndexHandler fullIndexHandler;
	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;
    private MdekJobHandler jobHandler;

	private BeanToDocMapper beanToDocMapper;
	private BeanToDocMapperSecurity beanToDocMapperSecurity;
	private DocToBeanMapper docToBeanMapper;
	
    /** Get The Singleton */
	public static synchronized MdekObjectService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekObjectService(daoFactory, permissionService);
	    }
		return myInstance;
	}
	
	private MdekObjectService(DaoFactory daoFactory, IPermissionService permissionService) {
		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoObjectReference = daoFactory.getDao(ObjectReference.class);

		catalogService = MdekCatalogService.getInstance(daoFactory);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);
		pathHandler = MdekTreePathHandler.getInstance(daoFactory);
		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);
        jobHandler = MdekJobHandler.getInstance(daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
	}

	/** Load object NODE with given uuid. Also prefetch concrete object instance in node if requested.
	 * @param uuid object uuid. if null is passed we return null !
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found or null was passed !
	 */
	public ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		return daoObjectNode.loadByUuid(uuid, whichEntityVersion);
	}

	/** Load object NODE with given ORIGINAL_ID (always queries WORKING VERSION !!!).
	 * Also prefetch concrete object instance in node if requested.
	 * @param origId object ORIGINAL_ID = id from external system
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public ObjectNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion) {
		return daoObjectNode.loadByOrigId(origId, whichEntityVersion);
	}

	/**
	 * Fetch single object with given uuid.
	 * @param objUuid object uuid
	 * @param whichEntityVersion which object version should be fetched.
	 * 		NOTICE: In published state working version == published version and it is the same object instance !
	 * @param howMuch how much data to fetch from object
	 * @return map representation of object containing requested data
	 */
	public IngridDocument getObjectDetails(String objUuid,
			IdcEntityVersion whichEntityVersion, FetchQuantity howMuch,
			String userId) {
		// first get all "internal" object data (referenced addresses ...)
		ObjectNode oNode = daoObjectNode.getObjDetails(objUuid, whichEntityVersion);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument resultDoc = new IngridDocument();
		T01Object o;
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			o = oNode.getT01ObjectPublished();
		} else {
			whichEntityVersion = IdcEntityVersion.WORKING_VERSION;
			o = oNode.getT01ObjectWork();
		}
		if (o == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND,
				"" + whichEntityVersion + " of object " + objUuid + " in node IS NULL !"));			
		}

		// how much to map from object ? default is DETAIL_ENTITY (called from IGE)
		MappingQuantity objMappingQuantity = MappingQuantity.DETAIL_ENTITY;
		if (howMuch == FetchQuantity.EXPORT_ENTITY) {
			// map all when object should be exported !
			objMappingQuantity = MappingQuantity.COPY_ENTITY;
		}
		beanToDocMapper.mapT01Object(o, resultDoc, objMappingQuantity);

		// also map ObjectNode for published info
		beanToDocMapper.mapObjectNode(oNode, resultDoc, MappingQuantity.DETAIL_ENTITY);
	
		if (howMuch == FetchQuantity.EDITOR_ENTITY) {
			// get "external" data (objects referencing the given object ...)
			List<ObjectNode>[] fromLists = daoObjectNode.getObjectReferencesFrom(objUuid);
			beanToDocMapper.mapObjectReferencesFrom(fromLists, null, null,
					IdcEntityType.OBJECT, objUuid, resultDoc, MappingQuantity.TABLE_ENTITY);

			// get parent data
			ObjectNode pNode = daoObjectNode.getParent(objUuid);
			if (pNode != null) {
				beanToDocMapper.mapObjectParentData(pNode.getT01ObjectWork(), resultDoc);
			}

			// then map detailed mod user data !
			beanToDocMapper.mapModUser(o.getModUuid(), resultDoc, MappingQuantity.DETAIL_ENTITY);

			// add permissions the user has on given object !
			List<Permission> perms = permissionHandler.getPermissionsForObject(objUuid, userId, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);
		}

		return resultDoc;
	}

	/**
	 * Fetch multiple object instances (versions) of object with given uuid.
	 * @param objUuid object uuid
	 * @param whichEntityVersion which object version should be fetched.
	 * 		NOTICE: In published state working version == published version and it is the same object instance !
	 * @param howMuch how much data to fetch from object
	 * @return list of docs encapsulating requested versions ! WORK_STATE in each doc determines which version.
	 * 		May be of length 0 if version not set.
	 * 		Or length 2 if ALL_VERSIONS requested and working version differs from published version.
	 * 		Then working version is delivered first !
	 */
	public List<IngridDocument> getObjectInstancesDetails(String objUuid,
			IdcEntityVersion whichEntityVersion,
			FetchQuantity howMuch,
			String userId) {
		List<IngridDocument> retList = new ArrayList<IngridDocument>(); 

		ObjectNode oN = daoObjectNode.loadByUuid(objUuid, null);
		if (whichEntityVersion.equals(IdcEntityVersion.ALL_VERSIONS)) {
			if (hasWorkingCopy(oN)) {
				retList.add(getObjectDetails(objUuid, IdcEntityVersion.WORKING_VERSION, howMuch, userId));				
			}
			if (hasPublishedVersion(oN)) {
				retList.add(getObjectDetails(objUuid, IdcEntityVersion.PUBLISHED_VERSION, howMuch, userId));				
			}
			
		} else {
			retList.add(getObjectDetails(objUuid, whichEntityVersion, howMuch, userId));
		}

		return retList;
	}

	/**
	 * Store WORKING COPY of the object represented by the passed doc. Called By IGE !
	 * @see #storeWorkingCopy(IngridDocument oDocIn, String userId,	boolean calledByImporter=false)
	 */
	public String storeWorkingCopy(IngridDocument oDocIn, String userId) {
		return storeWorkingCopy(oDocIn, userId, false);
	}

	/**
	 * Store WORKING COPY of the object represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new object !
	 * @param oDocIn doc representing object
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of stored object, will be generated if new object (no uuid passed in doc)
	 */
	public String storeWorkingCopy(IngridDocument oDocIn, String userId,
			boolean calledByImporter) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// WHEN CALLED BY IGE: uuid is null when new object
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		boolean isNewObject = (uuid == null) ? true : false;	
		// WHEN CALLED BY IMPORTER: uuid is NEVER NULL, but might be NEW OBJECT !
		// we check via select and SIMULATE IGE call (so all checks work !)
		String importerUuid = uuid;
		if (calledByImporter) {
			isNewObject = (loadByUuid(uuid, null) == null);
			// simulate IGE call !
			if (isNewObject) {
				uuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				oDocIn.remove(MdekKeys.UUID);
			}
		}

		// WHEN CALLED BY IGE: parentUuid only passed if new object
		String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
		// WHEN CALLED BY IMPORTER: parentUuid always passed. we SIMULATE IGE call (so all checks work !)
		String importerParentUuid = parentUuid;
		if (calledByImporter) {
			// simulate IGE call !
			if (!isNewObject) {
				parentUuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				oDocIn.remove(MdekKeys.PARENT_UUID);
			}
		}
		
		// set common data to transfer to working copy !
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		String modUuid = userId;
		if (calledByImporter) {
			modUuid = docToBeanMapper.extractModUserUuid(oDocIn);
			if (modUuid == null) {
				modUuid = userId;
			}
		}
		beanToDocMapper.mapModUser(modUuid, oDocIn, MappingQuantity.INITIAL_ENTITY);

		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(oDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, oDocIn, MappingQuantity.INITIAL_ENTITY);				
		}
		// set correct work state if necessary
		String givenWorkState = (String) oDocIn.get(MdekKeys.WORK_STATE);
		if (givenWorkState == null || givenWorkState.equals(WorkState.VEROEFFENTLICHT.getDbValue())) {
			oDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
		}

		// check permissions !
		if (!calledByImporter) {
			permissionHandler.checkPermissionsForStoreObject(uuid, parentUuid, userId);			
		}

		// End simulating IGE call when called by importer, see above ! now we use importer data !
		if (calledByImporter) {
			uuid = importerUuid;
			oDocIn.put(MdekKeys.UUID, uuid);
			parentUuid = importerParentUuid;
			oDocIn.put(MdekKeys.PARENT_UUID, parentUuid);
		} else {
			// called by IGE !
			if (isNewObject) {
				// create new uuid
				uuid = EntityHelper.getInstance().generateUuid();
				oDocIn.put(MdekKeys.UUID, uuid);
				// NOTICE: don't add further data, is done below when checking working copy !
			}			
		}
		
		// load node
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode == null) {
			// create new node, also take care of correct tree path in node
			oNode = docToBeanMapper.mapObjectNode(oDocIn, new ObjectNode());
			pathHandler.setTreePath(oNode, parentUuid);
		}
		
		// get/create working copy
		if (!hasWorkingCopy(oNode)) {
			// no working copy yet, may be NEW object or a PUBLISHED one without working copy ! 

			// set some missing data in doc which is NOT passed from client.
			// set from published version if existent.
			T01Object oPub = oNode.getT01ObjectPublished();
			if (oPub != null) {
				oDocIn.put(MdekKeys.DATE_OF_CREATION, oPub.getCreateTime());				
				oDocIn.put(MdekKeys.CATALOGUE_IDENTIFIER, oPub.getCatId());
			} else {
				oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
				oDocIn.put(MdekKeys.CATALOGUE_IDENTIFIER, catalogService.getCatalogId());
			}
			
			// create BASIC working object
			T01Object oWork = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
			// save it to generate id needed for mapping of associations
			daoT01Object.makePersistent(oWork);

			// save special stuff from pub version not passed by client -> can be queried on working version !
			if (oPub != null) {
				// save orig uuid and special stuff -> all data also in working version !
				oWork.setOrgObjId(oPub.getOrgObjId());
				oWork.setDatasetCharacterSet(oPub.getDatasetCharacterSet());
				oWork.setMetadataCharacterSet(oPub.getMetadataCharacterSet());
				oWork.setMetadataStandardName(oPub.getMetadataStandardName());
				oWork.setMetadataStandardVersion(oPub.getMetadataStandardVersion());
				
				// NOTICE: NO TAKEOVER OF ENTITY METADATA -> working version starts with defaults !
				// in working version we don't need expiry state, lastexporttime .... 
				// further after publish we don't remember assigner, reassigner etc.
			}

			// create/update node
			oNode.setObjId(oWork.getId());
			oNode.setT01ObjectWork(oWork);
			daoObjectNode.makePersistent(oNode);
		}

		// TRANSFER FULL DATA (if set) -> NOT PASSED FROM CLIENT, BUT E.G. PASSED WHEN IMPORTING !!!
		T01Object oWork = oNode.getT01ObjectWork();
		docToBeanMapper.mapT01Object(oDocIn, oWork, MappingQuantity.COPY_ENTITY);
		daoT01Object.makePersistent(oWork);

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateObjectIndex(oNode);
		
		// check for changes made in "linksFrom" table due to bidirectional connection
		// of class 1 and 3! -> NOT DONE SINCE WE DECIDED TO USE ONE-WAY ONLY!
		//updateChangesToObjectReferences(oWork, (List<IngridDocument>) oDocIn.get(MdekKeys.OBJ_REFERENCES_FROM)); // and oPub?!

		// grant write tree permission if not set yet (e.g. new root node)
		if (!calledByImporter && isNewObject) {
			if (parentUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);				
			} else if (permissionHandler.hasSubNodePermissionForObject(parentUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_OBJECT, parentUuid);				
			}
		}

		return uuid;
	}

	// -> NOT DONE SINCE WE DECIDED TO USE ONE-WAY ONLY! THEREFORE NO CHANGE IN BACKEND!
	/*
	private void updateChangesToObjectReferences(T01Object oWork, List<IngridDocument> newFromList) {
	    // only do this for object classes 1 and 3!
	    if (oWork.getObjClass() == 1 || oWork.getObjClass() == 3) {
	        // in case there are no fromLinks
	        if (newFromList == null) newFromList = new ArrayList<IngridDocument>();
	        
    	    // get original list of incoming links
    	    List<ObjectNode>[] fromLists = daoObjectNode.getObjectReferencesFrom(oWork.getObjUuid());
    	    // FIXME: does not really need conversion I guess!
    	    IngridDocument resultDoc = new IngridDocument();
    	    beanToDocMapper.mapObjectReferencesFrom(fromLists, null, null, IdcEntityType.OBJECT, oWork.getObjUuid(), resultDoc, MappingQuantity.TABLE_ENTITY);
            
    	    // compare both lists for changes
    	    // 1) There cannot be a new one in 'newFromList'!
    	    // 2) The same reference exists in both lists but has changed -> 'update' neccessary values!
    	    // 3) A reference is missing in 'newFromList' -> remove it from the source reference!
    	    for (IngridDocument objRef : (List<IngridDocument>)resultDoc.get(MdekKeys.OBJ_REFERENCES_FROM)) {
    	        T01Object t01Object = null;
    	        boolean found = false;
                for (IngridDocument newObjRef : newFromList) {
                    // check for changes in from-reference table
                    if (objRef.get(MdekKeys.UUID).equals(newObjRef.get(MdekKeys.UUID))) {
                        t01Object = updateChangesInReference(objRef, newObjRef);
                        found = true;
                    }
                }
                // remove from source if not found
                if (!found) {
                    t01Object = removeReferenceFromObject(oWork, objRef);
                }
                // if changed were found
                if (t01Object != null) {
                    daoT01Object.makePersistent(t01Object);
                }
            }
	    }
    }
	*/
	
    /**
     * If the from object is only available as published version then we have to create a working copy and
     * do the changes there!
     * 
     * @param oWork
     * @param objRef
     * @return
     */
    /*
    private T01Object removeReferenceFromObject(T01Object oWork, IngridDocument objRef) {
        T01Object t01Object = null;
        ObjectNode oNode = loadByUuid(String.valueOf(objRef.get(MdekKeys.UUID)), null);
        if (oNode.getObjId() == objRef.getLong(MdekKeys.ID)) {
            t01Object = oNode.getT01ObjectWork();
//        } else if (oNode.getObjIdPublished() == objRef.getLong(MdekKeys.ID)) { // we always get the current version with getObjId()!!!
//            t01Object = oNode.getT01ObjectPublished();
        } else {
            LOG.error("objId not found: " + objRef.getLong(MdekKeys.ID) + " in " + objRef.get(MdekKeys.UUID));
            return null;
        }
        
        ObjectReference[] t01ObjectRefs = (ObjectReference[]) t01Object.getObjectReferences().toArray(new ObjectReference[0]);
        for (ObjectReference objectReference : t01ObjectRefs) {
            if (objectReference.getObjToUuid().equals(oWork.getObjUuid())) {
                t01Object.getObjectReferences().remove(objectReference);
                break;
            }
        }
        return t01Object;
    }
    */
    
    /**
	 * Publish the object represented by the passed doc. Called By IGE !  
	 * @see #publishObject(IngridDocument oDocIn, boolean forcePubCondition, String userId,
	 * 		boolean calledByImporter=false)
	 */
	public String publishObject(IngridDocument oDocIn, boolean forcePubCondition,
			String userId) {
		return publishObject(oDocIn, forcePubCondition, userId, false);
	}

	/**
	 * Publish the object represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new object !
	 * @param oDocIn doc representing object
	 * @param forcePubCondition apply restricted PubCondition to subnodes (true)
	 * 		or receive Error when subnodes PubCondition conflicts (false)
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of published object, will be generated if new object (no uuid passed in doc)
	 */
	public String publishObject(IngridDocument oDocIn, boolean forcePubCondition,
			String userId,
			boolean calledByImporter) {
		// WHEN CALLED BY IGE: uuid is null when new object
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		boolean isNewObject = (uuid == null) ? true : false;	
		// WHEN CALLED BY IMPORTER: uuid is NEVER NULL, but might be NEW OBJECT !
		// we check via select and SIMULATE IGE call (so all checks work !)
		String importerUuid = uuid;
		if (calledByImporter) {
			isNewObject = (loadByUuid(uuid, null) == null);
			// simulate IGE call !
			if (isNewObject) {
				uuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				oDocIn.remove(MdekKeys.UUID);
			}
		}

		// WHEN CALLED BY IGE: parentUuid only passed if new object
		String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
		// WHEN CALLED BY IMPORTER: parentUuid always passed. we SIMULATE IGE call (so all checks work !)
		String importerParentUuid = parentUuid;
		if (calledByImporter) {
			// simulate IGE call !
			if (!isNewObject) {
				parentUuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				oDocIn.remove(MdekKeys.PARENT_UUID);
			}
		}

		Integer pubTypeIn = (Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION);
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// PERFORM CHECKS
		// NOTICE: passed object may NOT exist yet (new object published immediately)

		// check permissions !
		if (!calledByImporter) {
			permissionHandler.checkPermissionsForPublishObject(uuid, parentUuid, userId);
		}

		// no referenced address set
		if (!hasAddressReference(oDocIn)) {
			throw new MdekException(new MdekError(MdekErrorType.REFERENCED_ADDRESS_NOT_SET));
		}
		// all referenced addresses published ?
		checkReferencedAddressesForPublish(oDocIn);
		// all parents published ?
		checkObjectPathForPublish(parentUuid, uuid);
		// publication condition of parent fits to object ?
		checkObjectPublicationConditionParent(parentUuid, uuid, pubTypeIn);
		// publication conditions of sub nodes fit to object ?
		checkObjectPublicationConditionSubTree(uuid, pubTypeIn, forcePubCondition, true,
			currentTime, userId);

		// CHECKS OK, proceed

		// set common data to transfer
		workflowHandler.processDocOnPublish(oDocIn);
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		String modUuid = userId;
		if (calledByImporter) {
			// take over mod user from import doc ! was manipulated !
			modUuid = docToBeanMapper.extractModUserUuid(oDocIn);
			if (modUuid == null) {
				modUuid = userId;
			}
		}
		beanToDocMapper.mapModUser(modUuid, oDocIn, MappingQuantity.INITIAL_ENTITY);

		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(oDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, oDocIn, MappingQuantity.INITIAL_ENTITY);				
		}

		// End simulating IGE call when called by importer, see above ! now we use importer data !
		if (calledByImporter) {
			uuid = importerUuid;
			oDocIn.put(MdekKeys.UUID, uuid);
			parentUuid = importerParentUuid;
			oDocIn.put(MdekKeys.PARENT_UUID, parentUuid);
		} else {
			// called by IGE !
			if (isNewObject) {
				// create new uuid
				uuid = EntityHelper.getInstance().generateUuid();
				oDocIn.put(MdekKeys.UUID, uuid);
			}			
		}

		// load node
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode == null) {
			// create new node, also take care of correct tree path in node
			oNode = docToBeanMapper.mapObjectNode(oDocIn, new ObjectNode());
			pathHandler.setTreePath(oNode, parentUuid);
		}
		
		// get/create published version
		T01Object oPub = oNode.getT01ObjectPublished();
		if (oPub == null) {
			// set some missing data which may not be passed from client.
			oDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			oDocIn.put(MdekKeys.CATALOGUE_IDENTIFIER, catalogService.getCatalogId());
			// TODO: set default SPECIAL DATA in doc when NEW OBJECT (DatasetCharacterSet, MetadataCharacterSet ...) ?

			// create new object with BASIC data
			oPub = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
			// save it to generate id needed for mapping of associations
			daoT01Object.makePersistent(oPub);
		}
		Long oPubId = oPub.getId();

		// if working copy then take over data and delete it ! 
		T01Object oWork = oNode.getT01ObjectWork();
		if (oWork != null && !oPubId.equals(oWork.getId())) {
			// save orig uuid and special stuff from working version ! may be was set (e.g. in import !)
			// NOTICE: may be overwritten by mapper below if set in doc
			oPub.setOrgObjId(oWork.getOrgObjId());
			oPub.setDatasetCharacterSet(oWork.getDatasetCharacterSet());
			oPub.setMetadataCharacterSet(oWork.getMetadataCharacterSet());
			oPub.setMetadataStandardName(oWork.getMetadataStandardName());
			oPub.setMetadataStandardVersion(oWork.getMetadataStandardVersion());

			// NOTICE: NO TAKEOVER OF ENTITY METADATA -> published version keeps old state !
			// after publish we don't remember assigner, reassigner etc.
			// further mark deleted and expiry state is reset when published and lastexporttime is kept
			// (was set in published metadata when exporting, only published ones can be exported !)

			// delete working version
			daoT01Object.makeTransient(oWork);
		}
		
		// TRANSFER FULL DATA (if set) -> NOT PASSED FROM CLIENT, BUT E.G. PASSED WHEN IMPORTING !!!
		docToBeanMapper.mapT01Object(oDocIn, oPub, MappingQuantity.COPY_ENTITY);
		daoT01Object.makePersistent(oPub);

		// and update ObjectNode, also beans, so we can access them afterwards (index)
		oNode.setObjId(oPubId);
		oNode.setT01ObjectWork(oPub);
		oNode.setObjIdPublished(oPubId);
		oNode.setT01ObjectPublished(oPub);
		daoObjectNode.makePersistent(oNode);
		
		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateObjectIndex(oNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (!calledByImporter && isNewObject) {
			if (parentUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);
			} else if (permissionHandler.hasSubNodePermissionForObject(parentUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_OBJECT, parentUuid);				
			}
		}

        // then set IDs in doc and update changed entities in JobInfo
		oDocIn.put( MdekKeys.ID, oPub.getId() );
		oDocIn.put( MdekKeys.UUID, oPub.getObjUuid() );
		oDocIn.put( MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, oPub.getOrgObjId() );
        jobHandler.updateRunningJobChangedEntities(userId,
                IdcEntityType.OBJECT, WorkState.VEROEFFENTLICHT, oDocIn,
                "PUBLISHED document successfully");

		return uuid;
	}

	/**
	 * Move an object with its subtree to another parent. Called By IGE !
	 * @see #moveObject(String fromUuid, String toUuid, boolean forcePubCondition, 
	 * 			String userId, boolean calledByImporter=false)
	 */
	public IngridDocument moveObject(String fromUuid, String toUuid, boolean forcePubCondition,
			String userId) {
		return moveObject(fromUuid, toUuid, forcePubCondition, userId, false);
	}

	/**
	 * Move an object with its subtree to another parent.
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent, pass null if top node
	 * @param forcePubCondition apply restricted PublicationCondition of new parent to
	 * 		subobjects (true) or receive Error when subobjects PubCondition conflicts with
	 * 		new parent (false)
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. DON'T check permissions<br>
	 * 		false=default behaviour when called from IGE
	 * @return map containing info (number of moved objects)
	 */
	public IngridDocument moveObject(String fromUuid, String toUuid, boolean forcePubCondition,
			String userId, boolean calledByImporter) {
		// PERFORM CHECKS

		// check permissions !
		if (!calledByImporter) {
			permissionHandler.checkPermissionsForMoveObject(fromUuid, toUuid, userId);
		}

		ObjectNode fromNode = loadByUuid(fromUuid, null);
		checkObjectNodesForMove(fromNode, toUuid, forcePubCondition, userId);

		// CHECKS OK, proceed

		// process all moved nodes including top node (e.g. change tree path or date and mod_uuid) !
		IngridDocument resultDoc = processMovedNodes(fromNode, toUuid, userId);

		// grant write tree permission (e.g. if new root node)
		if (!calledByImporter) {
			if (toUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForObject(fromUuid, userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);
			} else if (permissionHandler.hasSubNodePermissionForObject(toUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForObject(fromUuid, userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_OBJECT, toUuid);				
			}
		}

		return resultDoc;		
	}

	/**
	 * DELETE ONLY WORKING COPY. Notice: If no published version exists the object is deleted
	 * completely, meaning non existent afterwards (including all subobjects !)
	 * @param uuid object uuid
	 * @param forceDeleteReferences only relevant if deletion of working copy causes FULL DELETION
	 * (no published version !)<br>
	 * 		true=all references to this object are also deleted<br>
	 * 		false=error if references to this object exist
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteObjectWorkingCopy(String uuid, boolean forceDeleteReferences,
			String userId) {
		// check permissions !
		permissionHandler.checkPermissionsForDeleteWorkingCopyObject(uuid, userId);			

		// NOTICE: this one also contains Parent Association !
		ObjectNode oNode = daoObjectNode.getObjDetails(uuid);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument result;

		// if we have NO published version -> delete complete node !
		if (!hasPublishedVersion(oNode)) {
			result = deleteObject(uuid, forceDeleteReferences, userId);

		} else {
			// delete working copy only 
			result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);			
			result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);			

			// perform delete of working copy only if really different version
			if (hasWorkingCopy(oNode)) {
				// delete working copy, BUT REMEMBER COMMENTS -> take over to published version !  
				T01Object oWorkingCopy = oNode.getT01ObjectWork();
				IngridDocument commentsDoc =
					beanToDocMapper.mapObjectComments(oWorkingCopy.getObjectComments(), new IngridDocument());
				daoT01Object.makeTransient(oWorkingCopy);

				// take over comments to published version
				T01Object oPublished = oNode.getT01ObjectPublished();
				docToBeanMapper.updateObjectComments(commentsDoc, oPublished);
				daoT01Object.makePersistent(oPublished);

				//  and set published one as working copy
				oNode.setObjId(oNode.getObjIdPublished());
				oNode.setT01ObjectWork(oPublished);
				daoObjectNode.makePersistent(oNode);
				
				// UPDATE FULL INDEX !!!
				fullIndexHandler.updateObjectIndex(oNode);
			}
		}

		return result;
	}

	/**
	 * FULL DELETE: different behavior when workflow enabled<br>
	 * - QA: full delete of object (working copy and published version) INCLUDING all subobjects !
	 * Object non existent afterwards !<br>
	 * - NON QA: object is just marked deleted and assigned to QA<br>
	 * If workflow disabled every user acts like a QA (when having write access)
	 * @param uuid object uuid
	 * @param forceDeleteReferences how to handle references to this object ?<br>
	 * 		true=all references to this object are also deleted
	 * 		false=error if references to this object exist
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteObjectFull(String uuid, boolean forceDeleteReferences, String userId) {
		IngridDocument result;
		// NOTICE: Always returns true if workflow disabled !
		if (permissionHandler.hasQAPermission(userId)) {
			result = deleteObject(uuid, forceDeleteReferences, userId);
		} else {
			result = markDeletedObject(uuid, forceDeleteReferences, userId);
		}

		return result;
	}
	
	public IngridDocument deleteObjectByOrigId(String origId, boolean forceDeleteReferences, String userId) {
	    
	    // NOTICE: this one also contains Parent Association !
        ObjectNode oNode = loadByOrigId( origId, IdcEntityVersion.WORKING_VERSION);
        if (oNode == null) {
            throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND, "Missing ORID_UUID: " + origId));
        }
        
        String objectUuid = oNode.getObjUuid();

        IngridDocument result = deleteObjectFull(objectUuid, forceDeleteReferences, userId);
        result.put(MdekKeys.UUID, objectUuid);
        return result;
	}

	/**
	 * Assign object to QA. Called By IGE !
	 * @see #assignObjectToQA(IngridDocument oDocIn, String userId, boolean calledByImporter=false)
	 */
	public String assignObjectToQA(IngridDocument oDocIn,
			String userId) {
		return assignObjectToQA(oDocIn, userId, false);
	}

	/**
	 * Assign object to QA !
	 * @param oDocIn doc representing object
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of stored object, will be generated if new object (no uuid passed in doc)
	 */
	public String assignObjectToQA(IngridDocument oDocIn,
			String userId,
			boolean calledByImporter) {
		// set specific data to transfer to working copy !
		workflowHandler.processDocOnAssignToQA(oDocIn, userId);
		return storeWorkingCopy(oDocIn, userId, calledByImporter);
	}

	/** Checks whether given object document has any address set, type of address reference (role) does NOT matter, see INGRID32-46. */
	public boolean hasAddressReference(IngridDocument oDoc) {
		List<IngridDocument> oAs = (List<IngridDocument>) oDoc.get(MdekKeys.ADR_REFERENCES_TO);
		if (oAs == null) {
			oAs = new ArrayList<IngridDocument>();
		}

		for (IngridDocument oA : oAs) {
			// DEPRECATED: No special type check (Verwalter) anymore, instead just check whether address present and any relation type, see INGRID32-46
			String addrUuid = oA.getString(MdekKeys.UUID);
			String typeName = oA.getString(MdekKeys.RELATION_TYPE_NAME);
			Integer typeKey = (Integer) oA.get(MdekKeys.RELATION_TYPE_ID);

			boolean uuidOk = addrUuid != null && addrUuid.trim().length() > 0;
			boolean typeNameOk = typeName != null && typeName.trim().length() > 0; // check for free value
			boolean typeKeyOk = typeKey != null && typeKey > 0; // check for syslist value
			if (uuidOk && (typeKeyOk || typeNameOk)) {
				return true;
			}
		}
		
		return false;
	}

	/** Check all referenced Addresses whether ok for object beeing published, e.g. addresses
	 * have to be published and publication condition of address has to fit. 
	 * Throws Exception if not ! */
	private void checkReferencedAddressesForPublish(IngridDocument oDoc) {
		List<IngridDocument> oAs = (List<IngridDocument>) oDoc.get(MdekKeys.ADR_REFERENCES_TO);
		if (oAs == null) {
			oAs = new ArrayList<IngridDocument>();
		}

		// fetch all address nodes
		List<AddressNode> aNodes = new ArrayList<AddressNode>(oAs.size());
		for (IngridDocument oA : oAs) {
			aNodes.add(addressService.loadByUuid(oA.getString(MdekKeys.UUID), IdcEntityVersion.PUBLISHED_VERSION));
		}

		// check for errors

		// first check whether published
		IngridDocument errInfo = new IngridDocument();
		for (AddressNode aNode : aNodes) {
		    // if address is not a system user and not published address
			if (!( "IGE_USER".equals( aNode.getFkAddrUuid() ) || addressService.hasPublishedVersion(aNode))) {
				addressService.setupErrorInfoAddr(errInfo, aNode.getAddrUuid());
			}
		}
		if (!errInfo.isEmpty()) {
			throw new MdekException(new MdekError(MdekErrorType.REFERENCED_ADDRESSES_NOT_PUBLISHED, errInfo));			
		}

		// all published, so we check publication condition of addresses
		Integer pubTypeObjDB = (Integer) oDoc.get(MdekKeys.PUBLICATION_CONDITION);
		PublishType pubTypeObj = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeObjDB);
		List<IngridDocument> errAddrList = new ArrayList<IngridDocument>();
		for (AddressNode aNode : aNodes) {
		    // if address is a system user then we skip the check, since system users can have state working version
		    if ("IGE_USER".equals( aNode.getFkAddrUuid() )) continue;
		    
			// get referenced address and its publish type
			T02Address a = aNode.getT02AddressPublished();
			PublishType pubTypeAddr = EnumUtil.mapDatabaseToEnumConst(PublishType.class, a.getPublishId());
			// error if publish type of referenced address is smaller than the one in object (e.g. obj.INTERNET -> addr.INTRANET)
			if (!pubTypeAddr.includes(pubTypeObj)) {
				IngridDocument aDoc = beanToDocMapper.mapT02Address(a, new IngridDocument(), MappingQuantity.BASIC_ENTITY);
				errAddrList.add(aDoc);
			}
		}
		if (!errAddrList.isEmpty()) {
			oDoc.put(MdekKeys.ADR_ENTITIES, errAddrList);
			throw new MdekException(new MdekError(MdekErrorType.REFERENCED_ADDRESSES_HAVE_SMALLER_PUBLICATION_CONDITION, oDoc));			
		}
	}

	/** FULL DELETE ! MAKE TRANSIENT ! */
	private IngridDocument deleteObject(String uuid, boolean forceDeleteReferences,
			String userUuid) {
		// first check User Permissions
		permissionHandler.checkPermissionsForDeleteObject(uuid, userUuid);			

		// NOTICE: this one also contains Parent Association !
		ObjectNode oNode = loadByUuid(uuid, IdcEntityVersion.WORKING_VERSION);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND, "Missing UUID: " + uuid));
		}

		List<ObjectNode> subNodes = checkObjectTreeReferencesForDelete(oNode, forceDeleteReferences);

		// delete complete Node ! rest is deleted per cascade (subnodes, permissions)
		daoObjectNode.makeTransient(oNode);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);
		result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);
		
		List<String> deletedUuids = subNodes.stream()
				.map(ObjectNode::getObjUuid)
				.collect(Collectors.toList());
		
		updateRunningJobChangedEntities(userUuid, deletedUuids);
		
        // and we also update changed entities in result
        result.put(MdekKeys.CHANGED_ENTITIES, jobHandler.getRunningJobChangedEntities(userUuid));

		return result;
	}
	
	private void updateRunningJobChangedEntities(String userUuid, List<String> deletedUuids) {
		// update and audit all deleted nodes
		deletedUuids.forEach(uuid -> {
			IngridDocument resultSub = new IngridDocument();
			resultSub.put( MdekKeys.UUID, uuid );
			jobHandler.updateRunningJobChangedEntities(userUuid,
					IdcEntityType.OBJECT, WorkState.DELETED, resultSub,
					"DELETED document");
		});
	}

	/** FULL DELETE IF NOT PUBLISHED !!!<br> 
	 * If published version exists -> Mark as deleted and assign to QA (already persisted)<br>
	 * if NO published version -> perform full delete !
	 */
	private IngridDocument markDeletedObject(String uuid, boolean forceDeleteReferences,
			String userUuid) {
		// first check User Permissions
		permissionHandler.checkPermissionsForDeleteObject(uuid, userUuid);			

		// NOTICE: we just load NODE to determine whether published !
		ObjectNode oNode = loadByUuid(uuid, null);
		if (oNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument result;

		// FULL DELETE IF NOT PUBLISHED !
		if (!hasPublishedVersion(oNode)) {
			result = deleteObject(uuid, forceDeleteReferences, userUuid);

		} else {
			// IS PUBLISHED -> mark deleted
			// now load details (prefetch data) for faster mapping (less selects !) 
			oNode = daoObjectNode.getObjDetails(uuid);

			// assign to QA via regular process to guarantee creation of working copy !
			// we generate doc via mapper and set MARK_DELETED !
			IngridDocument objDoc =
				beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), new IngridDocument(), MappingQuantity.COPY_ENTITY);
			objDoc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
			assignObjectToQA(objDoc, userUuid);

			result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
			result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, true);			
		}

		return result;
	}

	/** Checks whether given Object has the given version.
	 * @param oNode object to check represented by node !
	 * @param whichVersion which version to check:<br>
	 * 		WORKING_VERSION: object has a working copy different from published version (or not published)?<br>
	 * 		PUBLISHED_VERSION: object has a published version ?<br>
	 * 		ALL_VERSIONS: object has any version (should always be true !)
	 * @return true=object has the given version.<br>
	 * 		false=object has NOT the given version, e.g. NOT published (PUBLISHED_VERSION) OR NO working copy (WORKING_VERSION).
	 */
	public boolean hasVersion(ObjectNode oNode, IdcEntityVersion whichVersion) {
		if (whichVersion == IdcEntityVersion.WORKING_VERSION) {
			return hasWorkingCopy(oNode);
		}
		if (whichVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			return hasPublishedVersion(oNode);
		}

		return (hasWorkingCopy(oNode) || hasPublishedVersion(oNode));
	}
	
	public boolean isFolder(ObjectNode oNode) {
	    T01Object oWork = oNode.getT01ObjectWork();
		if (oWork != null) {
			return oWork.getObjClass() == 1000;
		} else {
		    T01Object oPub = oNode.getT01ObjectPublished();
		    if (oPub != null) {
		        return oPub.getObjClass() == 1000;
		    } else {
		        throw new MdekException( "Object has no working and no published version!" );
		    }
		    
		}
	}

	/** Checks whether given Object has a published version.
	 * @param oNode object to check represented by node !
	 * @return true=object has a published version. NOTICE: working version may be different<br>
	 * 	false=not published yet, only working version exists !
	 */
	public boolean hasPublishedVersion(ObjectNode oNode) {
		Long oPubId = oNode.getObjIdPublished(); 
		if (oPubId == null) {
			return false;
		}
		return true;
	}

	/** Checks whether given Object has a working copy !
	 * @param oNode object to check represented by node !
	 * @return true=object has different working copy OR not published yet<br>
	 * 	false=no working copy, working version is same as published version (OR no working version at all, should not happen)
	 */
	public boolean hasWorkingCopy(ObjectNode oNode) {
		Long oWorkId = oNode.getObjId(); 
		Long oPubId = oNode.getObjIdPublished(); 
		if (oWorkId == null || oWorkId.equals(oPubId)) {
			return false;
		}
		
		return true;
	}

	/**
	 * Process the moved tree, meaning set new tree path or modification date and user in node (in
	 * published and working version !).
	 * NOTICE: date and user isn't set in subnodes, see http://jira.media-style.com/browse/INGRIDII-266
	 * @param rootNode root node of moved tree branch
	 * @param newParentUuid node uuid of new parent
	 * @param modUuid user uuid to set as modification user
	 * @return doc containing additional info (number processed nodes ...)
	 */
	private IngridDocument processMovedNodes(ObjectNode rootNode, String newParentUuid, String modUuid)
	{
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// process root node

		// set new parent, may be null, then top node !
		rootNode.setFkObjUuid(newParentUuid);
		// remember former tree path and set new tree path.
		String oldRootPath = pathHandler.getTreePathNotNull(rootNode);
		String newRootPath = pathHandler.setTreePath(rootNode, newParentUuid);
		daoObjectNode.makePersistent(rootNode);

		// set modification time and user only in top node, not in subnodes !
		// see http://jira.media-style.com/browse/INGRIDII-266

		// set modification time and user (in both versions when present)
		T01Object objWork = rootNode.getT01ObjectWork();
		T01Object objPub = rootNode.getT01ObjectPublished();

		// check whether we have a different published version !
		boolean hasDifferentPublishedVersion = false;
		if (objPub != null && objWork.getId() != objPub.getId()) {
			hasDifferentPublishedVersion = true;
		}

		// change mod time and uuid
		objWork.setModTime(currentTime);
		objWork.setModUuid(modUuid);

		if (hasDifferentPublishedVersion) {
			objPub.setModTime(currentTime);
			objPub.setModUuid(modUuid);				
		}

		daoT01Object.makePersistent(objWork);
		if (hasDifferentPublishedVersion) {
			daoT01Object.makePersistent(objPub);
		}				

		// process all subnodes

		List<ObjectNode> subNodes = daoObjectNode.getAllSubObjects(rootNode.getObjUuid(), null, false);
		for (ObjectNode subNode : subNodes) {
			// update tree path
			pathHandler.updateTreePathAfterMove(subNode, oldRootPath, newRootPath);
			daoObjectNode.makePersistent(subNode);
		}

		// total number: root + subobjects
		int numberOfProcessedObj = subNodes.size() + 1;
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfProcessedObj);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Number of moved objects: " + numberOfProcessedObj);
		}

		return result;
	}

	/**
	 * Checks whether node has unpublished parents. Throws MdekException if so.
	 * @param parentUuid is always null, if inUuid is NOT null because this method is called at
	 * beginning of publish job, see there !
	 * @param inUuid is null if new node is created then parent is set (or parent is null if top node)
	 */
	private void checkObjectPathForPublish(String parentUuid, String inUuid) {
		// default
		String endOfPath = inUuid;
		boolean includeEndOfPath = false;

		// new node (uuid not generated yet) ? parent should be passed !
		if (inUuid == null) {
			endOfPath = parentUuid;
			includeEndOfPath = true;
		}
		
		// no check if top node !
		if (endOfPath == null) {
			return;
		}
		
		// check whether a parent is not published

		List<String> pathUuids = daoObjectNode.getObjectPath(endOfPath);
		for (String pathUuid : pathUuids) {
			if (pathUuid.equals(endOfPath) && !includeEndOfPath) {
				continue;
			}
			ObjectNode pathNode = loadByUuid(pathUuid, null);
			if (pathNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			
			// check
			if (!hasPublishedVersion(pathNode) && !isFolder( pathNode )) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}
		}
	}

	/**
	 * Check whether publication condition of parent fits to publication condition of child.<br>
	 * NOTICE: PublishedVersion of parent is checked !<br>
	 * Throws Exception if not fitting.
	 * @param parentUuid is always null, if inUuid is NOT null because this method is called at
	 * 		beginning of publish job, see there !
	 * @param childUuid is null if new node is created then parent is set (or parent is null if top node)
	 * @param pubTypeChildDB publication condition of child database value
	 */
	private void checkObjectPublicationConditionParent(String parentUuid,
			String childUuid,
			Integer pubTypeChildDB) {

		PublishType pubTypeChild = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeChildDB);

		// Load Parent of child
		// NOTICE: childUuid can be null if uuid not generated yet (new object)
		if (parentUuid == null) {
			// if childUuid is null then we have a new top object !
			if (childUuid != null) {
				parentUuid = loadByUuid(childUuid, null).getFkObjUuid();				
			}
		}
		// return if top node
		if (parentUuid == null) {
			return;
		}
		ObjectNode parentObj = loadByUuid(parentUuid, IdcEntityVersion.PUBLISHED_VERSION);
		
		if (!isFolder( parentObj )) {
    		T01Object parentObjPub = parentObj.getT01ObjectPublished(); 
    		if (parentObjPub == null) {
    			throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
    		}
    		
    		// get publish type of parent
    		PublishType pubTypeParent = EnumUtil.mapDatabaseToEnumConst(PublishType.class, parentObjPub.getPublishId());
    
    		// check whether publish type of parent is smaller
    		if (!pubTypeParent.includes(pubTypeChild)) {
    			throw new MdekException(new MdekError(MdekErrorType.PARENT_HAS_SMALLER_PUBLICATION_CONDITION));					
    		}
		}
	}

	/** Checks whether a tree fits to a new publication condition.
	 * !!! ALSO ADAPTS Publication Conditions IF REQUESTED !!!<br>
	 * NOTICE: ONLY PUBLISHED versions of subnodes are checked and adapted !!!
	 * @param rootUuid uuid of top node of tree
	 * @param pubTypeTopDB new publication type (DataBase value !)
	 * @param forcePubCondition force change of nodes (modification time, publicationType, ...)
	 * @param skipRootNode check/change top node, e.g. when moving (true) or not, e.g. when publishing (false)
	 * @param modTime modification time to store in modified nodes
	 * @param modUuid user uuid to set as modification user
	 */
	private void checkObjectPublicationConditionSubTree(String rootUuid,
			Integer pubTypeTopDB,
			Boolean forcePubCondition,
			boolean skipRootNode,
			String modTime,
			String modUuid) {

		// no check if new object ! No children !
		if (rootUuid == null) {
			return;
		}

		// get current pub type. Should be set !!! (mandatory when publishing)
		PublishType pubTypeNew = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeTopDB);		
		ObjectNode rootNode = loadByUuid(rootUuid, IdcEntityVersion.ALL_VERSIONS);
		String topName = rootNode.getT01ObjectWork().getObjName();

		// avoid null !
		if (!Boolean.TRUE.equals(forcePubCondition)) {
			forcePubCondition = false;			
		}
		
		// process subnodes where publication condition doesn't match

		List<ObjectNode> subNodes = daoObjectNode.getSelectedSubObjects(
				rootUuid,
				IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC, pubTypeNew);
		
		// also process top node if requested !
		if (!skipRootNode) {
			subNodes.add(0, rootNode);			
		}
		for (ObjectNode subNode : subNodes) {
			// check "again" whether publication condition of node is "critical"
			T01Object objPub = subNode.getT01ObjectPublished();
			if (objPub == null) {
				// not published yet ! skip this one
				continue;
			}

			PublishType subPubType = EnumUtil.mapDatabaseToEnumConst(PublishType.class, objPub.getPublishId());				
			if (!pubTypeNew.includes(subPubType)) {
				// throw "warning" for user when not adapting sub tree !
				if (!forcePubCondition) {
					throw new MdekException(new MdekError(MdekErrorType.SUBTREE_HAS_LARGER_PUBLICATION_CONDITION));					
				}

				// nodes should be adapted -> in PublishedVersion !
				objPub.setPublishId(pubTypeNew.getDbValue());
				// set time and user
				objPub.setModTime(modTime);
				objPub.setModUuid(modUuid);

				// add comment to SUB OBJECT, document the automatic change of the publish condition
				if (!subNode.equals(rootNode)) {
					Set<ObjectComment> commentSet = objPub.getObjectComments();
					ObjectComment newComment = new ObjectComment();
					newComment.setObjId(objPub.getId());
					newComment.setComment("Hinweis: Durch Änderung des Wertes des Feldes 'Veröffentlichung' im " +
						"übergeordneten Objekt '" + topName +
						"' ist der Wert dieses Feldes für dieses Objekt auf '" + pubTypeNew.toString() +
						"' gesetzt worden.");
					newComment.setCreateTime(objPub.getModTime());
					newComment.setCreateUuid(objPub.getModUuid());
					commentSet.add(newComment);
					daoT01Object.makePersistent(objPub);						
				}
			}
		}
	}

	/** Check whether passed nodes are valid for move operation
	 * (e.g. move to subnode not allowed). Throws MdekException if not valid.
	 */
	private void checkObjectNodesForMove(ObjectNode fromNode, String toUuid,
		Boolean forcePubCondition,
		String userId)
	{
		if (fromNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
		}		
		String fromUuid = fromNode.getObjUuid();

		// NOTICE: top node when toUuid = null
		if (toUuid != null) {
			// load toNode
			ObjectNode toNode = loadByUuid(toUuid, IdcEntityVersion.PUBLISHED_VERSION);
			if (toNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
			}

			// is target subnode ?
			if (daoObjectNode.isSubNode(toUuid, fromUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.TARGET_IS_SUBNODE_OF_SOURCE));				
			}
			
			// from object published ? then check compatibility !
			boolean isFromPublished = (fromNode.getT01ObjectPublished() != null);
			if (isFromPublished && !isFolder( toNode )) {
				// new parent has to be published ! -> not possible to move published nodes under unpublished parent
				T01Object toObjPub = toNode.getT01ObjectPublished();
				if (toObjPub == null) {
					throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
				}				

				// are pubTypes compatible ?
				// we check and adapt ONLY PUBLISHED version !!!
				Integer publicationTypeTo = toObjPub.getPublishId();
				// adapt all child nodes if requested !
				String currentTime = MdekUtils.dateToTimestamp(new Date()); 
				checkObjectPublicationConditionSubTree(fromUuid, publicationTypeTo, forcePubCondition, false,
					currentTime, userId);
			}
		}
	}

	/**
	 * Checks whether object branch contains nodes referenced by other objects (passed top is also checked !). 
	 * All references to a node are taken into account, no matter whether from a working or a published version !
	 * Throws Exception if forceDeleteReferences=false !
	 * @param topNode top node of tree to check (included in check !)
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private List<ObjectNode> checkObjectTreeReferencesForDelete(ObjectNode topNode, boolean forceDeleteReferences) {
		// process all subnodes including top node

		List<ObjectNode> subNodes = daoObjectNode.getAllSubObjects(
				topNode.getObjUuid(), IdcEntityVersion.WORKING_VERSION, false);
		subNodes.add(0, topNode);

		for (ObjectNode subNode : subNodes) {
			// check
			checkObjectNodeReferencesForDelete(subNode, forceDeleteReferences);			
		}
		
		return subNodes;
	}

	/**
	 * Checks whether object node is referenced by other objects.
	 * All references to node are taken into account, no matter whether from a working or a published version !
	 * Throws Exception if forceDeleteReferences=false !
	 * @param oNode object to check
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkObjectNodeReferencesForDelete(ObjectNode oNode, boolean forceDeleteReferences) {
		// handle references to object
		String oUuid = oNode.getObjUuid();
		ObjectReference exampleRef = new ObjectReference();
		exampleRef.setObjToUuid(oUuid);
		List<IEntity> objRefs = daoObjectReference.findByExample(exampleRef);
		
		// throw exception with detailed errors when address referenced without reference deletion !
		if (!forceDeleteReferences) {
			int numRefs = objRefs.size();
			if (numRefs > 0) {
				// existing references -> throw exception with according error info !

				// add info about referenced object
				IngridDocument errInfo =
					beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), new IngridDocument(), MappingQuantity.BASIC_ENTITY);

				// add info about objects referencing !
				ArrayList<IngridDocument> objList = new ArrayList<IngridDocument>(numRefs);
				for (IEntity ent : objRefs) {
					ObjectReference ref = (ObjectReference) ent;
					// fetch object referencing the address
					T01Object o = daoT01Object.getById(ref.getObjFromId());
					IngridDocument objInfo =
						beanToDocMapper.mapT01Object(o, new IngridDocument(), MappingQuantity.BASIC_ENTITY);
					objList.add(objInfo);
				}
				errInfo.put(MdekKeys.OBJ_ENTITIES, objList);

				// and throw exception encapsulating errors
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_REFERENCED_BY_OBJ, errInfo));
			}
		}
		
		// delete references (querverweise)
		for (IEntity objRef : objRefs) {
			daoObjectReference.makeTransient(objRef);
		}
	}

	/** Add data of object with given uuid to the given additional Info of an error.
	 * @param errInfo additional info transferred with error
	 * @param objUuid uuid of object to be added
	 * @return again the passed errInfo
	 */
	public IngridDocument setupErrorInfoObj(IngridDocument errInfo, String objUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument oDoc = 
			beanToDocMapper.mapT01Object(
					loadByUuid(objUuid, IdcEntityVersion.WORKING_VERSION).getT01ObjectWork(),
					new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		List<IngridDocument> oList = (List<IngridDocument>) errInfo.get(MdekKeys.OBJ_ENTITIES);
		if (oList == null) {
			oList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeys.OBJ_ENTITIES, oList);
		}
		oList.add(oDoc);
		
		return errInfo;
	}
}
