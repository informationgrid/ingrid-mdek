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
package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.iplug.dsc.index.DscDocumentProducer;
import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.log.AuditService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.EntityHelper;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler.GroupType;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to address entity data.
 */
public class MdekAddressService {

	private static final Logger LOG = LogManager.getLogger(MdekAddressService.class);

	private static MdekAddressService myInstance;

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;
	private IIdcUserDao daoIdcUser;
	private IT01ObjectDao daoT01Object;
	private IGenericDao<IEntity> daoT012ObjAdr;

	private MdekTreePathHandler pathHandler;
	private MdekFullIndexHandler fullIndexHandler;
	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;

	private BeanToDocMapper beanToDocMapper;
	protected BeanToDocMapperSecurity beanToDocMapperSecurity;
	private DocToBeanMapper docToBeanMapper;

    private IndexManager indexManager = null;
    
    private DscDocumentProducer docProducer;

	/** Get The Singleton */
	public static synchronized MdekAddressService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekAddressService(daoFactory, permissionService);
	      }
		return myInstance;
	}
	
	/** Get The Singleton */
	public static synchronized MdekAddressService getInstance(DaoFactory daoFactory,
	        IPermissionService permissionService, IndexManager indexManager) {
	    if (myInstance == null) {
	        myInstance = new MdekAddressService(daoFactory, permissionService);
	    }
	    // make sure the IndexManager is initialized!
        myInstance.indexManager = indexManager;
	    return myInstance;
	}

	private MdekAddressService(DaoFactory daoFactory, IPermissionService permissionService) {
		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoIdcUser = daoFactory.getIdcUserDao();
		daoT01Object = daoFactory.getT01ObjectDao();
		daoT012ObjAdr = daoFactory.getDao(T012ObjAdr.class);

		pathHandler = MdekTreePathHandler.getInstance(daoFactory);
		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
	}

	/** Load address NODE with given uuid. Also prefetch concrete address instance in node if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param uuid address uuid
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public AddressNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		return daoAddressNode.loadByUuid(uuid, whichEntityVersion);
	}

	/** Load NODE with given ORIGINAL_ID (always queries WORKING VERSION !!!).
	 * Also prefetch concrete address instance in node if requested.
	 * @param origId id from external system (ORIGINAL_ID)
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public AddressNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion) {
		return daoAddressNode.loadByOrigId(origId, whichEntityVersion);
	}

	/**
	 * Fetch single address with given uuid. Pass parameters for paging mechanism of object references.
	 * @param addrUuid address uuid
	 * @param whichEntityVersion which address version should be fetched.
	 * 		NOTICE: In published state working version == published version and it is the same address instance !
	 * @param howMuch how much data to fetch from address
	 * @param objRefsStartIndex objects referencing the given address, object to start with (first object is 0)
	 * @param objRefsMaxNum objects referencing the given address, maximum number to fetch starting at index
	 * @param userId
	 * @return map representation of address containing requested data
	 */
	public IngridDocument getAddressDetails(String addrUuid, 
			IdcEntityVersion whichEntityVersion, FetchQuantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		// first get all "internal" address data
		AddressNode aNode = daoAddressNode.getAddrDetails(addrUuid, whichEntityVersion);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument resultDoc = new IngridDocument();
		T02Address a;
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			a = aNode.getT02AddressPublished();
		} else {
			whichEntityVersion = IdcEntityVersion.WORKING_VERSION;
			a = aNode.getT02AddressWork();
		}
		if (a == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND,
					"" + whichEntityVersion + " of address " + addrUuid + " in node IS NULL !"));
		}

		// how much to map from address ? default is DETAIL_ENTITY (called from IGE)
		MappingQuantity addrMappingQuantity = MappingQuantity.DETAIL_ENTITY;
		if (howMuch == FetchQuantity.EXPORT_ENTITY) {
			// map all when address should be exported !
			addrMappingQuantity = MappingQuantity.COPY_ENTITY;
		}
		beanToDocMapper.mapT02Address(a, resultDoc, addrMappingQuantity);
		
		// also map AddressNode for published info
		beanToDocMapper.mapAddressNode(aNode, resultDoc, MappingQuantity.DETAIL_ENTITY);

		if (howMuch == FetchQuantity.EDITOR_ENTITY) {
			// then get "external" data (objects referencing the given address ...)
			HashMap fromObjectsData = 
				daoAddressNode.getObjectReferencesFrom(addrUuid, objRefsStartIndex, objRefsMaxNum);
			// we use keys from mdek mapping for data transfer ! 
			List<ObjectNode>[] fromLists = (List<ObjectNode>[]) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM);
			int objRefsTotalNum = (Integer) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM);

			beanToDocMapper.mapObjectReferencesFrom(fromLists, objRefsStartIndex, objRefsTotalNum,
					IdcEntityType.ADDRESS, addrUuid, resultDoc, MappingQuantity.TABLE_ENTITY);

			// get parent data
			AddressNode pNode = daoAddressNode.getParent(addrUuid);
			if (pNode != null) {
				beanToDocMapper.mapAddressParentData(pNode.getT02AddressWork(), resultDoc);
			}

			// supply path info
			List<IngridDocument> pathList = daoAddressNode.getAddressPathOrganisation(addrUuid, false);
			resultDoc.put(MdekKeys.PATH_ORGANISATIONS, pathList);

			// then map detailed mod user data !
			beanToDocMapper.mapModUser(a.getModUuid(), resultDoc, MappingQuantity.DETAIL_ENTITY);

			// add permissions the user has on given address !
			List<Permission> perms = permissionHandler.getPermissionsForAddress(addrUuid, userId, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);
		}

		return resultDoc;
	}

	/**
	 * Fetch multiple address instances (versions) of address with given uuid.
	 * @param addrUuid address uuid
	 * @param whichEntityVersion which address version should be fetched.
	 * 		NOTICE: In published state working version == published version and it is the same address instance !
	 * @param howMuch how much data to fetch from address
	 * @param objRefsStartIndex objects referencing the given address, object to start with (first object is 0)
	 * @param objRefsMaxNum objects referencing the given address, maximum number to fetch starting at index
	 * @return list of docs encapsulating requested versions ! WORK_STATE in each doc determines which version.
	 * 		May be of length 0 if version not set.
	 * 		Or length 2 if ALL_VERSIONS requested and working version differs from published version.
	 * 		Then working version is delivered first !
	 */
	public List<IngridDocument> getAddressInstancesDetails(String addrUuid,
			IdcEntityVersion whichEntityVersion,
			FetchQuantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum,
			String userId) {
		List<IngridDocument> retList = new ArrayList<IngridDocument>(); 

		AddressNode aN = daoAddressNode.loadByUuid(addrUuid, null);
		if (whichEntityVersion.equals(IdcEntityVersion.ALL_VERSIONS)) {
			if (hasWorkingCopy(aN)) {
				retList.add(getAddressDetails(addrUuid, IdcEntityVersion.WORKING_VERSION, howMuch,
						objRefsStartIndex, objRefsMaxNum, userId));				
			}
			if (hasPublishedVersion(aN)) {
				retList.add(getAddressDetails(addrUuid, IdcEntityVersion.PUBLISHED_VERSION, howMuch,
						objRefsStartIndex, objRefsMaxNum, userId));				
			}
			
		} else {
			retList.add(getAddressDetails(addrUuid, whichEntityVersion, howMuch,
					objRefsStartIndex, objRefsMaxNum, userId));
		}

		return retList;
	}

	/**
	 * Store WORKING COPY of the address represented by the passed doc. Called By IGE !
	 * @see #storeWorkingCopy(IngridDocument aDocIn, String userId,	boolean calledByImporter=false)
	 */
	public String storeWorkingCopy(IngridDocument aDocIn, String userId) {
		return storeWorkingCopy(aDocIn, userId, false);
	}

	/**
	 * Store WORKING COPY of the address represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new address !
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of stored address, will be generated if new address (no uuid passed in doc)
	 */
	public String storeWorkingCopy(IngridDocument aDocIn, String userId,
			boolean calledByImporter) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// WHEN CALLED BY IGE: uuid is null when new object
		String uuid = (String) aDocIn.get(MdekKeys.UUID);
		boolean isNewAddress = (uuid == null) ? true : false;
		// WHEN CALLED BY IMPORTER: uuid is NEVER NULL, but might be NEW ADDRESS !
		// we check via select and SIMULATE IGE call (so all checks work !)
		String importerUuid = uuid;
		if (calledByImporter) {
			isNewAddress = (loadByUuid(uuid, null) == null);
			// simulate IGE call !
			if (isNewAddress) {
				uuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				aDocIn.remove(MdekKeys.UUID);
			}
		}

		// WHEN CALLED BY IGE: parentUuid only passed if new address !?
		String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);
		// Do we store a hidden USER ADDRESS (identified by virtual parent for all IGE USERS) ? see INGRID32-36
		// Then NO PERMISSION CHECKS ! Instead only user hierarchy checks in storeUser() !
		boolean isIGEUserAddress = MdekUtils.AddressType.getIGEUserParentUuid().equals(parentUuid); 

		// WHEN CALLED BY IMPORTER: parentUuid always passed. we SIMULATE IGE call (so all checks work !)
		String importerParentUuid = parentUuid;
		if (calledByImporter) {
			// simulate IGE call !
			if (!isNewAddress) {
				parentUuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				aDocIn.remove(MdekKeys.PARENT_UUID);
			}
		}

		// set common data to transfer to working copy !
		aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		String modUuid = userId;
		if (calledByImporter) {
			modUuid = docToBeanMapper.extractModUserUuid(aDocIn);
			if (modUuid == null) {
				modUuid = userId;
			}
		}
		beanToDocMapper.mapModUser(modUuid, aDocIn, MappingQuantity.INITIAL_ENTITY);

		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(aDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);				
		}
		// set correct work state if necessary
		String givenWorkState = (String) aDocIn.get(MdekKeys.WORK_STATE);
		if (givenWorkState == null || givenWorkState.equals(WorkState.VEROEFFENTLICHT.getDbValue())) {
			aDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
		}

		// check permissions !
		if (!calledByImporter && !isIGEUserAddress) {
			permissionHandler.checkPermissionsForStoreAddress(uuid, parentUuid, userId);
		}

		// End simulating IGE call when called by importer, see above ! now we use importer data !
		if (calledByImporter) {
			uuid = importerUuid;
			aDocIn.put(MdekKeys.UUID, uuid);
			parentUuid = importerParentUuid;
			aDocIn.put(MdekKeys.PARENT_UUID, parentUuid);
		} else {
			// called by IGE !
			if (isNewAddress) {
				// create new uuid
				uuid = EntityHelper.getInstance().generateUuid();
				aDocIn.put(MdekKeys.UUID, uuid);
				// NOTICE: don't add further data, is done below when checking working copy !
			}
		}
		
		// load node
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			// create new node, also take care of correct tree path in node
			aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			pathHandler.setTreePath(aNode, parentUuid);
		}
		
		// get/create working copy
		if (!hasWorkingCopy(aNode)) {
			// no working copy yet, may be NEW object or a PUBLISHED one without working copy ! 

			// set some missing data in doc which may not be passed from client.
			// set from published version if existent
			T02Address aPub = aNode.getT02AddressPublished();
			if (aPub != null) {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, aPub.getCreateTime());				
			} else {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			}
			
			// create BASIC working object
			T02Address aWork = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
			 // save it to generate id needed for mapping
			daoT02Address.makePersistent(aWork);

			// save special stuff from pub version not passed by client -> can be queried on working version !
			if (aPub != null) {
				// save orig uuid -> all data also in working version !
				aWork.setOrgAdrId(aPub.getOrgAdrId());

				// NOTICE: NO TAKEOVER OF ENTITY METADATA -> working version starts with defaults !
				// in working version we don't need expiry state, lastexporttime .... 
				// further after publish we don't remember assigner, reassigner etc.
			}

			// create/update node
			aNode.setAddrId(aWork.getId());
			aNode.setT02AddressWork(aWork);
			daoAddressNode.makePersistent(aNode);
		}

		// TRANSFER FULL DATA (if set) -> NOT PASSED FROM CLIENT, BUT E.G. PASSED WHEN IMPORTING !!!
		T02Address aWork = aNode.getT02AddressWork();
		docToBeanMapper.mapT02Address(aDocIn, aWork, MappingQuantity.COPY_ENTITY);

		// PERFORM CHECKS BEFORE STORING/COMMITTING !!!
		checkAddressNodeForStore(aNode);

		// store when ok
		daoT02Address.makePersistent(aWork);

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateAddressIndex(aNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (!calledByImporter && isNewAddress && !isIGEUserAddress) {
			if (parentUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);				
			} else if (permissionHandler.hasSubNodePermissionForAddress(parentUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS, parentUuid);				
			}
		}

		return uuid;
	}

	/**
	 * Publish the address represented by the passed doc. Called By IGE !  
	 * @see #publishAddress(IngridDocument aDocIn, String userId, boolean calledByImporter=false)
	 */
	public String publishAddress(IngridDocument aDocIn, boolean forcePubCondition,
			String userId) {
		return publishAddress(aDocIn, forcePubCondition, userId, false);
	}

	/**
	 * Publish the address represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new address !
	 * @param aDocIn doc representing address
	 * @param forcePublicationCondition apply restricted PubCondition to subnodes (true)
	 * 		or receive Error when subnodes PubCondition conflicts (false)
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of published address, will be generated if new address (no uuid passed in doc)
	 */
	public String publishAddress(IngridDocument aDocIn, boolean forcePubCondition,
			String userId,
			boolean calledByImporter) {
		// HEN CALLED BY IGE: uuid is null when new address !
		String uuid = (String) aDocIn.get(MdekKeys.UUID);
		boolean isNewAddress = (uuid == null) ? true : false;
		// WHEN CALLED BY IMPORTER: uuid is NEVER NULL, but might be NEW address !
		// we check via select and SIMULATE IGE call (so all checks work !)
		String importerUuid = uuid;
		if (calledByImporter) {
			isNewAddress = (loadByUuid(uuid, null) == null);
			// simulate IGE call !
			if (isNewAddress) {
				uuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				aDocIn.remove(MdekKeys.UUID);
			}
		}

		// WHEN CALLED BY IGE: parentUuid only passed if new address !
		String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);
		// WHEN CALLED BY IMPORTER: parentUuid always passed. we SIMULATE IGE call (so all checks work !)
		String importerParentUuid = parentUuid;
		if (calledByImporter) {
			// simulate IGE call !
			if (!isNewAddress) {
				parentUuid = null;
				// NO, if exception is thrown UUID in doc is missing (in calling method) !!!
//				aDocIn.remove(MdekKeys.PARENT_UUID);
			}
		}

		Integer pubTypeIn = (Integer) aDocIn.get(MdekKeys.PUBLICATION_CONDITION);
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// PERFORM CHECKS
		// NOTICE: passed address may NOT exist yet (new address published immediately)

		// check permissions !
		if (!calledByImporter) {
			permissionHandler.checkPermissionsForPublishAddress(uuid, parentUuid, userId);
		}

		// all parents published ?
		checkAddressPathForPublish(parentUuid, uuid);
		// publication condition of referencing objects fit to address ?
		checkAddressPublicationConditionReferencingObjects(aDocIn);
		// publication condition of parent fits to address ?
		checkAddressPublicationConditionParent(parentUuid, uuid, pubTypeIn);
		// publication conditions of sub nodes fit to address ?
		checkAddressPublicationConditionSubTree(uuid, pubTypeIn, forcePubCondition, true,
			currentTime, userId);

		// CHECKS OK, proceed

		// set common data to transfer
		workflowHandler.processDocOnPublish(aDocIn);
		aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		String modUuid = userId;
		if (calledByImporter) {
			// take over mod user from import doc ! was manipulated !
			modUuid = docToBeanMapper.extractModUserUuid(aDocIn);
			if (modUuid == null) {
				modUuid = userId;
			}
		}
		beanToDocMapper.mapModUser(modUuid, aDocIn, MappingQuantity.INITIAL_ENTITY);

		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(aDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);				
		}

		// End simulating IGE call when called by importer, see above ! now we use importer data !
		if (calledByImporter) {
			uuid = importerUuid;
			aDocIn.put(MdekKeys.UUID, uuid);
			parentUuid = importerParentUuid;
			aDocIn.put(MdekKeys.PARENT_UUID, parentUuid);
		} else {
			// called by IGE !
			if (isNewAddress) {
				// create new uuid
				uuid = EntityHelper.getInstance().generateUuid();
				aDocIn.put(MdekKeys.UUID, uuid);
			}
		}

		// load node
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			// create new node, also take care of correct tree path in node
			aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			pathHandler.setTreePath(aNode, parentUuid);
		}
		
		// get/create published version
		// NOTICE: Persist only after all checks have been executed to avoid inconsistent published version !
		T02Address aPub = aNode.getT02AddressPublished();
		boolean hasPublishedVersion = true;
		if (aPub == null) {
		    hasPublishedVersion = false;

			// set some missing data which may not be passed from client.
			aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			
			// create new address with BASIC data
			aPub = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
		}

		// if working copy then take over data !
		// NOTICE: Working instance equals published instance if not "In Bearbeitung" 
		T02Address aWork = aNode.getT02AddressWork();
		boolean hasDifferentWorkingVersion = false;
		if (aWork != null) {
		    // if different from published version, take over data
		    if (!aWork.getId().equals(aPub.getId())) {
		        hasDifferentWorkingVersion = true;

		        // save orig uuid from working version ! may be was set (e.g. in import !)
	            // NOTICE: may be overwritten by mapper below if set in doc
	            aPub.setOrgAdrId(aWork.getOrgAdrId());

	            // NOTICE: NO TAKEOVER OF ENTITY METADATA -> published version keeps old state !
	            // after publish we don't remember assigner, reassigner etc.
	            // further mark deleted and expiry state is reset when published and lastexporttime is kept
	            // (was set in published metadata when exporting, only published ones can be exported !)
		    }
        }

		// TRANSFER FULL DATA (if set) -> NOT PASSED FROM CLIENT, BUT E.G. PASSED WHEN IMPORTING !!!
		// DO NOT PERSIST WE STILL HAVE TO DO CHECKS ! 
		docToBeanMapper.mapT02Address(aDocIn, aPub, MappingQuantity.COPY_ENTITY);

		// and update AddressNode with bean to publish, so we can check full data
		aNode.setT02AddressPublished(aPub);

		// PERFORM CHECKS ON FINAL DATA BEFORE PERSISTING !!!
		// This may throw exception leading to working version !
		checkAddressNodeContentForPublish(aNode);			

		// checks ok !
		// FINALLY PERSIST PUBLISHED VERSION AND DELETE FORMER WORKING VERSION !

        // if no published version before, we have to go the long way:
		// - first create ID with persisting basic data
        // - then mapping associations using ID ! Otherwise Exception:
		// ConstraintViolationException: could not insert: [de.ingrid.mdek.services.persistence.db.model.T021Communication]
		// "NULL-Wert in Spalte »adr_id« verletzt Not-Null-Constraint" ()
        if (!hasPublishedVersion) {
            // create basic version and save it to generate id needed for mapping of associations 
            aPub = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
            daoT02Address.makePersistent(aPub);
           
            // if working copy then take over data ! 
            if (hasDifferentWorkingVersion) {
                // save orig uuid from working version ! may be was set (e.g. in import !)
                // NOTICE: may be overwritten by mapper below if set in doc
                aPub.setOrgAdrId(aWork.getOrgAdrId());
            }

            // then map full data
            docToBeanMapper.mapT02Address(aDocIn, aPub, MappingQuantity.COPY_ENTITY);
        }

        // persist final published address instance
        daoT02Address.makePersistent(aPub);

        // and persist final AddressNode
        Long aPubId = aPub.getId();
        aNode.setAddrId(aPubId);
        aNode.setT02AddressWork(aPub);
        aNode.setAddrIdPublished(aPubId);
        aNode.setT02AddressPublished(aPub);
        daoAddressNode.makePersistent(aNode);

        // delete old working instance ("in Bearbeitung") if there is one
        if (hasDifferentWorkingVersion) {
            daoT02Address.makeTransient(aWork);            
        }

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateAddressIndex(aNode);

        // grant write tree permission if not set yet (e.g. new root node)
		if (!calledByImporter && isNewAddress) {
			if (parentUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);
			} else if (permissionHandler.hasSubNodePermissionForAddress(parentUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS, parentUuid);				
			}
		}
		
        // commit transaction to make new/updated data available for next step
        daoAddressNode.commitTransaction();
        
        // and begin transaction again for next query
        daoAddressNode.beginTransaction();

        // update index
		ElasticDocument doc = docProducer.getById( aPubId.toString(), "id" );
        if (doc != null && !doc.isEmpty()) {
            indexManager.addBasicFields( doc, docProducer.getIndexInfo() );
            indexManager.update( docProducer.getIndexInfo(), doc, true );
            indexManager.flush();
        }
        
        if (AuditService.instance != null && doc != null) {
            String message = "PUBLISHED address successfully with UUID: " + uuid;
            Map<String, String> map = new HashMap<String, String>();
            map.put( "idf", (String) doc.get( "idf" ) );
            String payload = JSONObject.toJSONString( map );
            AuditService.instance.log( message, payload );
        }

		return uuid;
	}

	/**
	 * Move an address with its subtree to another parent. Called By IGE !
	 * @see #moveAddress(String fromUuid, String toUuid, boolean moveToFreeAddress, 
	 * 			String userId, boolean calledByImporter=false)
	 */
	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			boolean forcePubCondition,
			String userId) {
		return moveAddress(fromUuid, toUuid, moveToFreeAddress, forcePubCondition, userId, false);
	}

	/**
	 * Move an address with its subtree to another parent.
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent, pass null if top node
	 * @param moveToFreeAddress moved node will be a free address after move ?<br>
	 * 		true=moved node will be free address, parent has to be null<br>
	 * 		false=moved node will NOT be free address, parent can be set, when parent is null
	 * 		moved node is "normal" top address
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. DON'T check permissions<br>
	 * 		false=default behaviour when called from IGE
	 * @return map containing info (number of moved addresses)
	 */
	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			boolean forcePubCondition,
			String userId, boolean calledByImporter) {
		// PERFORM CHECKS

		// check permissions !
		if (!calledByImporter) {
			permissionHandler.checkPermissionsForMoveAddress(fromUuid, toUuid, userId);
		}

		AddressNode fromNode = loadByUuid(fromUuid, IdcEntityVersion.WORKING_VERSION);
		checkAddressNodesForMove(fromNode, toUuid, moveToFreeAddress, forcePubCondition, userId);

		// CHECKS OK, proceed

		// process all moved nodes including top node (e.g. change tree path or date and mod_uuid) !
		IngridDocument resultDoc = processMovedNodes(fromNode, toUuid, moveToFreeAddress, userId);

		// grant write tree permission (e.g. if new root node)
		if (!calledByImporter) {
			if (toUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForAddress(fromUuid, userId,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);
			} else if (permissionHandler.hasSubNodePermissionForAddress(toUuid, userId, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForAddress(fromUuid, userId,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS, toUuid);				
			}
		}

		return resultDoc;
	}

	/**
	 * DELETE ONLY WORKING COPY. Notice: If no published version exists the address is deleted 
	 * completely, meaning non existent afterwards (including all subaddresses !)
	 * @param uuid address uuid
	 * @param forceDeleteReferences only relevant if deletion of working copy causes FULL DELETION (no published version !)<br>
	 * 		true=all references to this address are also deleted
	 * 		false=error if references to this address exist
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteAddressWorkingCopy(String uuid, boolean forceDeleteReferences,
			String userId) {
		// first check permissions
		permissionHandler.checkPermissionsForDeleteWorkingCopyAddress(uuid, userId);

		// NOTICE: this one also contains Parent Association !
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument result;

		// if we have NO published version -> delete complete node !
		if (!hasPublishedVersion(aNode)) {
			result = deleteAddress(aNode, forceDeleteReferences, userId);

		} else {
			// delete working copy only 
			result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
			result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);

			// perform delete of working copy only if really different version
			if (hasWorkingCopy(aNode)) {
				// delete working copy, BUT REMEMBER COMMENTS -> take over to published version !  
				T02Address aWorkingCopy = aNode.getT02AddressWork();
				IngridDocument commentsDoc =
					beanToDocMapper.mapAddressComments(aWorkingCopy.getAddressComments(), new IngridDocument());
				daoT02Address.makeTransient(aWorkingCopy);

				// take over comments to published version
				T02Address aPublished = aNode.getT02AddressPublished();
				docToBeanMapper.updateAddressComments(commentsDoc, aPublished);
				daoT02Address.makePersistent(aPublished);

				// and set published one as working copy
				aNode.setAddrId(aNode.getAddrIdPublished());
				aNode.setT02AddressWork(aPublished);
				daoAddressNode.makePersistent(aNode);
				
				// UPDATE FULL INDEX !!!
				fullIndexHandler.updateAddressIndex(aNode);
			}
		}

		return result;
	}

	/**
	 * FULL DELETE: different behavior when workflow enabled<br>
	 * - QA: full delete of address (working copy and published version) INCLUDING all subaddresses !
	 * Address non existent afterwards !<br>
	 * - NON QA: address is just marked deleted and assigned to QA<br>
	 * If workflow disabled every user acts like a QA (when having write access)
	 * @param uuid address uuid
	 * @param forceDeleteReferences how to handle references to this address ?<br>
	 * 		true=all references to this address are also deleted
	 * 		false=error if references to this address exist
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteAddressFull(String uuid, boolean forceDeleteReferences,
			String userId) {
		IngridDocument result;

		// check User Permissions
		// Do we store a hidden USER ADDRESS (identified by virtual parent for all IGE USERS) ? see INGRID32-36
		// Then NO PERMISSION CHECKS ! Instead only user hierarchy checks in storeUser() !
		AddressNode aNode = loadByUuid(uuid, null);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}
		boolean isIGEUserAddress = MdekUtils.AddressType.getIGEUserParentUuid().equals(aNode.getFkAddrUuid());

		// NOTICE: hasQAPermission always returns true if workflow disabled !
		if (isIGEUserAddress || permissionHandler.hasQAPermission(userId)) {
			result = deleteAddress(aNode, forceDeleteReferences, userId);
		} else {
			result = markDeletedAddress(uuid, forceDeleteReferences, userId);
		}

		return result;
	}

	/**
	 * Assign address to QA. Called By IGE !
	 * @see #assignAddressToQA(IngridDocument aDocIn, String userId, boolean calledByImporter=false)
	 */
	public String assignAddressToQA(IngridDocument aDocIn, 
			String userId) {
		return assignAddressToQA(aDocIn, userId, false);
	}

	/**
	 * Assign address to QA !
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param calledByImporter true=do specials e.g. mod user is determined from passed doc<br>
	 * 		false=default behaviour when called from IGE e.g. mod user is calling user
	 * @return uuid of stored address, will be generated if new address (no uuid passed in doc)
	 */
	public String assignAddressToQA(IngridDocument aDocIn, 
			String userId,
			boolean calledByImporter) {
		// set specific data to transfer to working copy and store !
		workflowHandler.processDocOnAssignToQA(aDocIn, userId);
		return storeWorkingCopy(aDocIn, userId, calledByImporter);
	}

	/** FULL DELETE ! MAKE TRANSIENT ! */
	private IngridDocument deleteAddress(AddressNode aNode, boolean forceDeleteReferences,
			String userUuid) {

		// check User Permissions
		// Do we store a hidden USER ADDRESS (identified by virtual parent for all IGE USERS) ? see INGRID32-36
		// Then NO PERMISSION CHECKS ! Instead only user hierarchy checks in storeUser() !
		boolean isIGEUserAddress = MdekUtils.AddressType.getIGEUserParentUuid().equals(aNode.getFkAddrUuid());
		if (!isIGEUserAddress) {
			permissionHandler.checkPermissionsForDeleteAddress(aNode.getAddrUuid(), userUuid);
		}

		// check whether topnode/subnodes are referenced (also user address, responsible ...)
		checkAddressTreeReferencesForDelete(aNode, forceDeleteReferences);

		// delete complete Node ! rest is deleted per cascade (subnodes, permissions)
		daoAddressNode.makeTransient(aNode);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);
		result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);

		if (AuditService.instance != null) {
            String message = "DELETED address: " + aNode.getAddrUuid();
            AuditService.instance.log( message );
        }
		
		return result;
	}

	/** FULL DELETE IF NOT PUBLISHED !!!<br> 
	 * If published version exists -> Mark as deleted and assign to QA (already persisted)<br>
	 * if NO published version -> perform full delete !
	 */
	private IngridDocument markDeletedAddress(String uuid, boolean forceDeleteReferences,
			String userUuid) {
		// first check User Permissions
		permissionHandler.checkPermissionsForDeleteAddress(uuid, userUuid);


		// NOTICE: we just load NODE to determine whether published !
		AddressNode aNode = loadByUuid(uuid, null);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument result;

		// FULL DELETE IF NOT PUBLISHED !
		if (!hasPublishedVersion(aNode)) {
			result = deleteAddress(aNode, forceDeleteReferences, userUuid);
		} else {
			// IS PUBLISHED -> mark deleted
			// now load details (prefetch data) for faster mapping (less selects !) 
			aNode = daoAddressNode.getAddrDetails(uuid);

			// assign to QA via regular process to guarantee creation of working copy !
			// we generate doc via mapper and set MARK_DELETED !
			IngridDocument addrDoc =
				beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), new IngridDocument(), MappingQuantity.COPY_ENTITY);
			addrDoc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
			assignAddressToQA(addrDoc, userUuid);

			result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
			result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, true);
		}

		return result;
	}

	/** Returns true if given node has children. False if no children. */
	private boolean hasChildren(AddressNode node) {
    	return (node.getAddressNodeChildren().size() > 0) ? true : false;
	}

	/** Checks whether given Address has the given version.
	 * @param aNode address to check represented by node !
	 * @param whichVersion which version to check:<br>
	 * 		WORKING_VERSION: address has a working copy different from published version (or not published)?<br>
	 * 		PUBLISHED_VERSION: address has a published version ?<br>
	 * 		ALL_VERSIONS: address has any version (should always be true !)
	 * @return true=address has the given version.<br>
	 * 		false=address has NOT the given version, e.g. NOT published (PUBLISHED_VERSION) OR NO working copy (WORKING_VERSION).
	 */
	public boolean hasVersion(AddressNode aNode, IdcEntityVersion whichVersion) {
		if (whichVersion == IdcEntityVersion.WORKING_VERSION) {
			return hasWorkingCopy(aNode);
		}
		if (whichVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			return hasPublishedVersion(aNode);
		}

		return (hasWorkingCopy(aNode) || hasPublishedVersion(aNode));
	}

	/** Checks whether given Address has a published version.
	 * @param node address to check represented by node !
	 * @return true=address has a published version. NOTICE: working copy may differ<br>
	 * 	false=not published yet, only working version exists !
	 */
	public boolean hasPublishedVersion(AddressNode node) {
		Long pubId = node.getAddrIdPublished(); 
		if (pubId == null) {
			return false;
		}
		return true;
	}

	/** Checks whether given Address has a working copy !
	 * @param node address to check represented by node !
	 * @return true=address has different working copy OR not published yet<br>
	 * 	false=no working copy, working version is same as published version (OR no working version at all, should not happen)
	 */
	public boolean hasWorkingCopy(AddressNode node) {
		Long workId = node.getAddrId(); 
		Long pubId = node.getAddrIdPublished(); 
		if (workId == null || workId.equals(pubId)) {
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
	private IngridDocument processMovedNodes(AddressNode rootNode,
			String newParentUuid, boolean isNowFreeAddress,
			String modUuid)
	{
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// process root node

		// set new parent, may be null, then top node !
		rootNode.setFkAddrUuid(newParentUuid);
		// remember former tree path and set new tree path.
		String oldRootPath = pathHandler.getTreePathNotNull(rootNode);
		String newRootPath = pathHandler.setTreePath(rootNode, newParentUuid);
		daoAddressNode.makePersistent(rootNode);

		// set modification time and user only in top node, not in subnodes !
		// see http://jira.media-style.com/browse/INGRIDII-266

		// set modification time and user (in both versions when present)
		T02Address addrWork = rootNode.getT02AddressWork();
		T02Address addrPub = rootNode.getT02AddressPublished();
		
		// check whether we have a different published version !
		boolean hasDifferentPublishedVersion = false;
		if (addrPub != null && addrWork.getId() != addrPub.getId()) {
			hasDifferentPublishedVersion = true;
		}

		// change mod time and uuid
		addrWork.setModTime(currentTime);
		addrWork.setModUuid(modUuid);
		if (hasDifferentPublishedVersion) {
			addrPub.setModTime(currentTime);
			addrPub.setModUuid(modUuid);				
		}

		// handle move from/to "free address"
		processMovedOrCopiedAddress(addrWork, isNowFreeAddress);
		if (hasDifferentPublishedVersion) {
			processMovedOrCopiedAddress(addrPub, isNowFreeAddress);
		}

		daoT02Address.makePersistent(addrWork);
		if (hasDifferentPublishedVersion) {
			daoT02Address.makePersistent(addrPub);
		}

		// process all subnodes

		List<AddressNode> subNodes = daoAddressNode.getAllSubAddresses(rootNode.getAddrUuid(), null, false);
		for (AddressNode subNode : subNodes) {
			// update tree path in subnodes
			pathHandler.updateTreePathAfterMove(subNode, oldRootPath, newRootPath);
			daoAddressNode.makePersistent(subNode);
		}

		// total number: root + subaddresses
		int numberOfProcessedNodes = subNodes.size() + 1;
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfProcessedNodes);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Number of processed addresses: " + numberOfProcessedNodes);
		}

		return result;
	}

	/**
	 * Change the passed address concerning type, institution after copied/moved to new location.<br>
	 * NOTICE: changes working and published version !
	 * @param a the address as it was moved/copied
	 * @param isNowFreeAddress new location is free address
	 */
	public void processMovedOrCopiedAddress(T02Address a, boolean isNowFreeAddress) {
		boolean wasFreeAddress = AddressType.FREI.getDbValue().equals(a.getAdrType());
		if (isNowFreeAddress) {
			if (!wasFreeAddress) {

				// MOVE NON FREE ADDRESS TO FREE ADDRESS !

				// only Persons can be moved to free addresses !
				AddressType formerType = EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.getAdrType());
				if (formerType != AddressType.PERSON) {
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
				}

				a.setAdrType(AddressType.FREI.getDbValue());
				a.setInstitution("");
			}
		} else {
			if (wasFreeAddress) {

				// MOVE FREE ADDRESS TO NON FREE ADDRESS !

				a.setAdrType(AddressType.PERSON.getDbValue());
				a.setInstitution("");
			}				
		}
	}

	/** Check whether passed node is valid for storing !
	 * (e.g. check free address conditions ...). Throws MdekException if not valid.
	 */
	private void checkAddressNodeForStore(AddressNode node)
	{
		if (node == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		String parentUuid = node.getFkAddrUuid();
		T02Address aWork = node.getT02AddressWork();
		AddressType aType = EnumUtil.mapDatabaseToEnumConst(AddressType.class, aWork.getAdrType());
		if (aType == AddressType.FREI) {
			if (parentUuid != null) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}
			if (hasChildren(node)) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_SUBTREE));
			}
		}
	}
	
    public boolean isFolder(AddressNode aNode) {
        T02Address aWork = aNode.getT02AddressWork();
        if (aWork != null) {
            return aWork.getAdrType() == 1000;
        } else {
            T02Address aPub = aNode.getT02AddressPublished();
            if (aPub != null) {
                return aPub.getAdrType() == 1000;
            } else {
                throw new MdekException( "Address has no working and no published version!" );
            }

        }
    }

	/**
	 * Checks whether node has unpublished parents. Throws MdekException if so.
	 * @param parentUuid is always null, if inUuid is NOT null because this method is called at
	 * 		beginning of publish job, see there !
	 * @param inUuid is null if new node is created then parent is set (or parent is null if top node)
	 */
	private void checkAddressPathForPublish(String parentUuid, String inUuid) {
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

		List<String> pathUuids = daoAddressNode.getAddressPath(endOfPath);
		for (String pathUuid : pathUuids) {
			if (pathUuid.equals(endOfPath) && !includeEndOfPath) {
				continue;
			}
			AddressNode pathNode = loadByUuid(pathUuid, null);
			if (pathNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			
			// check
			if (!hasPublishedVersion(pathNode) && !isFolder(pathNode)) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}
		}
	}

	/** Check objects referencing the given address whether "compatible" with publication condition.
	 * Throws Exception if not ! */
	private void checkAddressPublicationConditionReferencingObjects(IngridDocument addrDoc) {
		String addrUuid = addrDoc.getString(MdekKeys.UUID);
		Integer pubTypeAddrDB = (Integer) addrDoc.get(MdekKeys.PUBLICATION_CONDITION);
		PublishType pubTypeAddr = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeAddrDB);

		// get objects referencing the given address
		HashMap fromObjectsData = 
			daoAddressNode.getObjectReferencesFrom(addrUuid, 0, Integer.MAX_VALUE);
		List<ObjectNode>[] fromLists = (List<ObjectNode>[]) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM);

		// check all published versions referencing the address !
		List<IngridDocument> errObjList = new ArrayList<IngridDocument>();
		for (List<ObjectNode> oNList : fromLists) {
			for (ObjectNode oN : oNList) {
				if (oN.getObjIdPublished() != null) {
					// get referencing object and its publish type
					T01Object o = oN.getT01ObjectPublished();
					PublishType pubTypeObj = EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.getPublishId());
					// error if publish type of address is smaller than the one in referencing object (e.g. obj.INTERNET -> addr.INTRANET)
					if (!pubTypeAddr.includes(pubTypeObj)) {
						IngridDocument oDoc = beanToDocMapper.mapT01Object(o, new IngridDocument(), MappingQuantity.BASIC_ENTITY);
						errObjList.add(oDoc);
					}
				}
			}
		}

		if (!errObjList.isEmpty()) {
			addrDoc.put(MdekKeys.OBJ_ENTITIES, errObjList);
			throw new MdekException(new MdekError(MdekErrorType.REFERENCING_OBJECTS_HAVE_LARGER_PUBLICATION_CONDITION, addrDoc));			
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
	private void checkAddressPublicationConditionParent(String parentUuid,
			String childUuid,
			Integer pubTypeChildDB) {

		PublishType pubTypeChild = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeChildDB);

		// Load Parent of child
		// NOTICE: childUuid can be null if uuid not generated yet (new address)
		if (parentUuid == null) {
			// if childUuid is null then we have a new top address !
			if (childUuid != null) {
				parentUuid = loadByUuid(childUuid, null).getFkAddrUuid();				
			}
		}
		// return if top node
		if (parentUuid == null) {
			return;
		}
		
		AddressNode parentAddress = loadByUuid(parentUuid, IdcEntityVersion.PUBLISHED_VERSION);

		if (!isFolder( parentAddress )) {
            T02Address parentPub = parentAddress.getT02AddressPublished(); 
            if (parentPub == null) {
                throw new MdekException( new MdekError( MdekErrorType.PARENT_NOT_PUBLISHED ) );
            }

            // get publish type of parent
            PublishType pubTypeParent = EnumUtil.mapDatabaseToEnumConst( PublishType.class, parentPub.getPublishId() );

            // check whether publish type of parent is smaller
            if (!pubTypeParent.includes( pubTypeChild )) {
                throw new MdekException( new MdekError( MdekErrorType.PARENT_HAS_SMALLER_PUBLICATION_CONDITION ) );
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
	private void checkAddressPublicationConditionSubTree(String rootUuid,
			Integer pubTypeTopDB,
			Boolean forcePubCondition,
			boolean skipRootNode,
			String modTime,
			String modUuid) {

		// no check if new node ! No children !
		if (rootUuid == null) {
			return;
		}

		// get current pub type. Should be set !!! (mandatory when publishing)
		PublishType pubTypeNew = EnumUtil.mapDatabaseToEnumConst(PublishType.class, pubTypeTopDB);		
		AddressNode rootNode = loadByUuid(rootUuid, IdcEntityVersion.ALL_VERSIONS);
		String topName = extractAddressName(rootNode.getT02AddressWork());

		// avoid null !
		if (!Boolean.TRUE.equals(forcePubCondition)) {
			forcePubCondition = false;			
		}
		
		// process subnodes where publication condition doesn't match

		List<AddressNode> subNodes = daoAddressNode.getSelectedSubAddresses(
				rootUuid,
				IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC, pubTypeNew);
		
		// also process top node if requested !
		if (!skipRootNode) {
			subNodes.add(0, rootNode);			
		}
		for (AddressNode subNode : subNodes) {
			// check "again" whether publication condition of node is "critical"
			T02Address addrPub = subNode.getT02AddressPublished();
			if (addrPub == null) {
				// not published yet ! skip this one
				continue;
			}

			PublishType subPubType = EnumUtil.mapDatabaseToEnumConst(PublishType.class, addrPub.getPublishId());				
			if (!pubTypeNew.includes(subPubType)) {
				// throw "warning" for user when not adapting sub tree !
				if (!forcePubCondition) {
					throw new MdekException(new MdekError(MdekErrorType.SUBTREE_HAS_LARGER_PUBLICATION_CONDITION));					
				}

				// nodes should be adapted -> in PublishedVersion !
				addrPub.setPublishId(pubTypeNew.getDbValue());
				// set time and user
				addrPub.setModTime(modTime);
				addrPub.setModUuid(modUuid);

				// add comment to SUB ADDRESS, document the automatic change of the publish condition
				if (!subNode.equals(rootNode)) {
					Set<AddressComment> commentSet = addrPub.getAddressComments();
					AddressComment newComment = new AddressComment();
					newComment.setAddrId(addrPub.getId());
					newComment.setComment("Hinweis: Durch Änderung des Wertes des Feldes 'Veröffentlichung' in der " +
						"übergeordneten Adresse '" + topName +
						"' ist der Wert dieses Feldes für diese Adresse auf '" + pubTypeNew.toString() +
						"' gesetzt worden.");
					newComment.setCreateTime(addrPub.getModTime());
					newComment.setCreateUuid(addrPub.getModUuid());
					commentSet.add(newComment);
					daoT02Address.makePersistent(addrPub);						
				}
			}
		}
	}

	/** FURTHER Checks whether passed node is valid for publishing !
	 * (e.g. check free address conditions ...). CHECKS PUBLISHED VERSION IN NODE !
	 * Throws MdekException if not valid.
	 */
	private void checkAddressNodeContentForPublish(AddressNode node)
	{
		if (node == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// check whether address has an email address !
		boolean hasEmail = false;
		Set<T021Communication> comms = node.getT02AddressPublished().getT021Communications();
		for (T021Communication comm : comms) {
			if (MdekUtils.COMM_TYPE_EMAIL.equals(comm.getCommtypeKey())) {
				hasEmail = true;
				break;
			}
		}
		if (!hasEmail) {
			throw new MdekException(new MdekError(MdekErrorType.ADDRESS_HAS_NO_EMAIL));			
		}

		AddressType nodeType = EnumUtil.mapDatabaseToEnumConst(AddressType.class,
				node.getT02AddressPublished().getAdrType());
		boolean isFreeAddress = (nodeType == AddressType.FREI);
		String parentUuid = node.getFkAddrUuid();

		// basic free address checks
		if (isFreeAddress) {
			if (parentUuid != null) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}
			if (hasChildren(node)) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_SUBTREE));
			}
		}

		// basic parent checks
		AddressType parentType = null;
		if (parentUuid != null) {
			AddressNode parentNode = null;
			String parentUuidTmp = parentUuid;
			// get first non folder parent node
			do {
			    parentNode = loadByUuid(parentUuidTmp, IdcEntityVersion.PUBLISHED_VERSION);
			    if (!isFolder(parentNode)) {
			        parentUuidTmp = parentNode.getAddrUuid();
			        break;
			    } else {
			        parentUuidTmp = parentNode.getFkAddrUuid();
			    }
			} while (parentUuidTmp != null);
            
			if (parentUuidTmp != null) {
                parentType = EnumUtil.mapDatabaseToEnumConst( AddressType.class,
                        parentNode.getT02AddressPublished().getAdrType() );
			}
		}

		// check address type conflicts !
		checkAddressTypes(parentType, nodeType, isFreeAddress, true);
	}

	/**
	 * Check whether types parent/child fit together. Throws exception if not 
	 * @param parentType pass null if no parent (top node). 
	 * @param childType type of child
	 * @param finalIsFreeAddress finally child should be free address (under the free address node) ?
	 * @param isFinalState <br>
	 * 		true=both types are already in final state<br>
	 * 		false=types are in state before copy or move operation (pre check)
	 */
	public void checkAddressTypes(AddressType parentType, AddressType childType,
			boolean finalIsFreeAddress,
			boolean isFinalState) {
		if (childType == null) {
			throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
		}

		if (finalIsFreeAddress) {
			// FREE ADDRESS !

			if (parentType != null) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}
			if (isFinalState) {
				if (childType != AddressType.FREI) {
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
				}
			} else {
				// check before copy or move operation
				if (childType != AddressType.PERSON &&
					childType != AddressType.FREI) {
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
				}
			}
		} else {
			// NO FREE ADDRESS !

			if (parentType == AddressType.FREI) {
				throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));					
			}
			if (isFinalState) {
				// final state frei not possible. FREI is copied/moved !
				if (childType == AddressType.FREI) {
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
				}
			}

			if (parentType == null) {
				// TOP ADDRESS

				// only institutions at top
				if (childType != AddressType.INSTITUTION && childType != AddressType.FOLDER) {
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));					
				}
			} else {
				// NO TOP ADDRESS

				if (parentType == AddressType.EINHEIT) {
					// only einheit and person below einheit
					if (childType == AddressType.INSTITUTION) {
						throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
					}

				} else if (parentType == AddressType.PERSON) {
					// nothing below person
					throw new MdekException(new MdekError(MdekErrorType.ADDRESS_TYPE_CONFLICT));
				}
			}
		}
	}

	/** Check whether passed nodes are valid for move operation
	 * (e.g. move to subnode not allowed). Throws MdekException if not valid.
	 */
	private void checkAddressNodesForMove(AddressNode fromNode, String toUuid,
		Boolean moveToFreeAddress,
		Boolean forcePubCondition,
		String userId)
	{
		if (fromNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
		}		
		String fromUuid = fromNode.getAddrUuid();

		T02Address fromAddrWork = fromNode.getT02AddressWork();
		AddressType fromTypeWork = EnumUtil.mapDatabaseToEnumConst(AddressType.class, fromAddrWork.getAdrType());

		// NOTICE: top node when toUuid = null
		AddressType toTypeWork = null;
		if (toUuid != null) {
			// move to a new NODE -> NO TOP NODE

			// free address has to be top node !
			if (moveToFreeAddress) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}

			// load toNode
			AddressNode toNode = loadByUuid(toUuid, IdcEntityVersion.ALL_VERSIONS);
			if (toNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
			}		

			// is target subnode ?
			if (daoAddressNode.isSubNode(toUuid, fromUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.TARGET_IS_SUBNODE_OF_SOURCE));				
			}

			// from address published ? then check compatibility !
			boolean isFromPublished = (fromNode.getT02AddressPublished() != null);
			if (isFromPublished && !isFolder( toNode )) {
				// new parent has to be published ! -> not possible to move published nodes under unpublished parent
				T02Address toAddrPub = toNode.getT02AddressPublished();
				if (toAddrPub == null) {
					throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
				}

				// are pubTypes compatible ?
				// we check and adapt ONLY PUBLISHED version !!!
				Integer publicationTypeTo = toAddrPub.getPublishId();
				// adapt all child nodes if requested !
				String currentTime = MdekUtils.dateToTimestamp(new Date()); 
				checkAddressPublicationConditionSubTree(fromUuid, publicationTypeTo, forcePubCondition, false,
					currentTime, userId);
			}
			
			T02Address toAddrWork = toNode.getT02AddressWork();
			toTypeWork = EnumUtil.mapDatabaseToEnumConst(AddressType.class, toAddrWork.getAdrType());

		} else {
			// move to TOP !
			
			if (moveToFreeAddress) {
				// free address has no subnodes !
				if (hasChildren(fromNode)) {
					throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_SUBTREE));
				}
			}
		}

		// check address type conflicts !
		checkAddressTypes(toTypeWork, fromTypeWork, moveToFreeAddress, false);
	}

	/**
	 * Checks whether address branch contains nodes referenced by other objects (passed top is also checked !).
	 * Throws Exception if forceDeleteReferences=false !
	 * @param topNode top node of tree to check (included in check !)
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkAddressTreeReferencesForDelete(AddressNode topNode, boolean forceDeleteReferences) {
		// process all subnodes INCLUDING top node

		List<AddressNode> subNodes = daoAddressNode.getAllSubAddresses(
				topNode.getAddrUuid(), IdcEntityVersion.WORKING_VERSION, false);
		subNodes.add(0, topNode);

		for (AddressNode subNode : subNodes) {
			// check whether address is address of idcuser !
			// ALWAYS CALL THIS ONE BEFORE CHECK BELOW WHICH MAY REMOVE ALL REFERENCES (forceDeleteReferences, see below)
			// NOTICE: if address is referenced as responsible user, then the address is a user address
			// and following check throws exception. If user address is changed, then all responsible
			// addresses are changed too (see updateResponsibleUserInEntities in SecurityJob !), SO
			// RESPONSIBLE ADDRESS IS ALWAYS A USER ADDRESS !
			if (daoIdcUser.getIdcUserByAddrUuid(subNode.getAddrUuid()) != null) {
				// user address !!! -> throw exception with according error info !
				IngridDocument errInfo =
					beanToDocMapper.mapT02Address(subNode.getT02AddressWork(), new IngridDocument(), MappingQuantity.BASIC_ENTITY);
				throw new MdekException(new MdekError(MdekErrorType.ADDRESS_IS_IDCUSER_ADDRESS, errInfo));
			}

			// DEPRECATED: check whether address is "verwalter" address. VERWALTER CANNOT BE DELETED
			// ALWAYS CALL THIS ONE BEFORE CHECK BELOW WHICH MAY REMOVE ALL REFERENCES (forceDeleteReferences, see below)
			// NO ! role of address does not matter anymore, see INGRID32-46
			// method removed but commented here to document history !
//			checkAddressIsVerwalter(subNode);

			// check
			checkAddressNodeObjectReferencesForDelete(subNode, forceDeleteReferences);
		}
	}

	/**
	 * Checks whether address node is referenced by other objects.
	 * Throws Exception if forceDeleteReferences=false !
	 * @param aNode address to check
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkAddressNodeObjectReferencesForDelete(AddressNode aNode, boolean forceDeleteReferences) {
		// handle references to address
		String aUuid = aNode.getAddrUuid();
		T012ObjAdr exampleRef = new T012ObjAdr();
		exampleRef.setAdrUuid(aUuid);
		List<IEntity> addrRefs = daoT012ObjAdr.findByExample(exampleRef);
		
		// throw exception with detailed errors when address referenced without reference deletion !
		if (!forceDeleteReferences) {
			int numRefs = addrRefs.size();
			if (numRefs > 0) {
				// existing references -> throw exception with according error info !

				// add info about referenced address
				IngridDocument errInfo =
					beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), new IngridDocument(), MappingQuantity.BASIC_ENTITY);

				// add info about objects referencing !
				ArrayList<IngridDocument> objList = new ArrayList<IngridDocument>(numRefs);
				for (IEntity ent : addrRefs) {
					T012ObjAdr ref = (T012ObjAdr) ent;
					// fetch object referencing the address
					T01Object o = daoT01Object.getById(ref.getObjId());
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
		for (IEntity addrRef : addrRefs) {
			daoT012ObjAdr.makeTransient(addrRef);
		}
	}

	/** Add data of address with given uuid to the given additional Info of an error.
	 * @param errInfo additional info transferred with error
	 * @param addrUuid uuid of address to be added
	 * @return again the passed errInfo
	 */
	public IngridDocument setupErrorInfoAddr(IngridDocument errInfo, String addrUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument aDoc = beanToDocMapper.mapT02Address(
				loadByUuid(addrUuid, IdcEntityVersion.WORKING_VERSION).getT02AddressWork(),
				new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		List<IngridDocument> aList = (List<IngridDocument>) errInfo.get(MdekKeys.ADR_ENTITIES);
		if (aList == null) {
			aList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeys.ADR_ENTITIES, aList);
		}
		aList.add(aDoc);
		
		return errInfo;
	}

	/** Add data of address with given uuid as USER ADDRESS to the given additional Info of an error.
	 * @param errInfo additional info transferred with error
	 * @param addrUuid uuid of address to be added as USER ADDRESS
	 * @return again the passed errInfo
	 */
	public IngridDocument setupErrorInfoUserAddress(IngridDocument errInfo, String userUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument uDoc = beanToDocMapper.mapT02Address(
				loadByUuid(userUuid, IdcEntityVersion.WORKING_VERSION).getT02AddressWork(),
				new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		List<IngridDocument> uList = (List<IngridDocument>) errInfo.get(MdekKeysSecurity.USER_ADDRESSES);
		if (uList == null) {
			uList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeysSecurity.USER_ADDRESSES, uList);
		}
		uList.add(uDoc);

		return errInfo;
	}

	/** Extract Name from given address (Person name OR institution name).
	 * @param addr
	 * @return
	 */
	public String extractAddressName(T02Address addr) {
		String name = "";

		// PERSON and FREI has person name displayed in IGE tree !
		if (AddressType.PERSON.getDbValue().equals(addr.getAdrType()) ||
			AddressType.FREI.getDbValue().equals(addr.getAdrType()))
		{
			if (MdekUtils.hasContent(addr.getLastname())) {
				name = name + addr.getLastname();
			}
			if (MdekUtils.hasContent(addr.getFirstname())) {
				if (name.length() > 0) {
					name = name + ", ";
				}
				name = name + addr.getFirstname();
			}
		}

		// otherwise institution name (displayed in brackets if FREI)
		if (AddressType.FREI.getDbValue().equals(addr.getAdrType()) ||
			AddressType.EINHEIT.getDbValue().equals(addr.getAdrType()) ||
			AddressType.INSTITUTION.getDbValue().equals(addr.getAdrType()))
		{
			if (MdekUtils.hasContent(addr.getInstitution())) {
				boolean hasPersonName = name.length() > 0;
				if (hasPersonName) {
					name = name + " (" + addr.getInstitution() + ")";
				} else {
					name = addr.getInstitution();
				}
			}
		}
		
		return name;
	}
	
    public void setDocProducer(DscDocumentProducer docProducer) {
        this.docProducer = docProducer;
    }
}
