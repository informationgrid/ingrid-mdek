package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCallerAbstract.FetchQuantity;
import de.ingrid.mdek.job.MdekException;
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
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to address entity data.
 */
public class MdekAddressService {

	private static final Logger LOG = Logger.getLogger(MdekAddressService.class);

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

	/** Get The Singleton */
	public static synchronized MdekAddressService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekAddressService(daoFactory, permissionService);
	      }
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
			a = aNode.getT02AddressWork();
		}
		beanToDocMapper.mapT02Address(a, resultDoc, MappingQuantity.DETAIL_ENTITY);
		
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
	 * Fetch all sub addresses of address with given uuid
	 * @param parentUuid uuid of parent
	 * @param howMuch how much data to fetch from sub addresses
	 * @return map containing representations of all sub addresses
	 */
	public IngridDocument getSubAddresses(String parentUuid, FetchQuantity howMuch,
			String userId) {
		// Settings for fetching
		IdcEntityVersion whichVersion = IdcEntityVersion.WORKING_VERSION;
		boolean fetchSubNodesChildren = true;

		// set export specific stuff
		if (howMuch == FetchQuantity.EXPORT_ENTITY) {
			// do not fetch any details, we only need uuids in node
			whichVersion = null;
		}

		List<AddressNode> aNodes =
			daoAddressNode.getSubAddresses(parentUuid, whichVersion, fetchSubNodesChildren);
		
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(aNodes.size());
		for (AddressNode aNode : aNodes) {
			IngridDocument adrDoc = new IngridDocument();
			beanToDocMapper.mapAddressNode(aNode, adrDoc, MappingQuantity.TREE_ENTITY);
			// map details only if necessary
			if (howMuch == FetchQuantity.EDITOR_ENTITY) {
				beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), adrDoc, MappingQuantity.TREE_ENTITY);

				// add permissions the user has on given address !
				List<Permission> perms =
					permissionHandler.getPermissionsForAddress(aNode.getAddrUuid(), userId, true);
				beanToDocMapperSecurity.mapPermissionList(perms, adrDoc);				
			}

			resultList.add(adrDoc);
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.ADR_ENTITIES, resultList);

		return result;
	}

	/**
	 * Store WORKING COPY of the address represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new address !
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of stored address, will be generated if new address (no uuid passed in doc)
	 */
	public String storeWorkingCopy(IngridDocument aDocIn, String userId, boolean checkPermissions) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		String uuid = (String) aDocIn.get(MdekKeys.UUID);
		boolean isNewAddress = (uuid == null) ? true : false;
		// parentUuid only passed if new address !?
		String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);

		// set common data to transfer to working copy !
		aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);
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
		if (checkPermissions) {
			permissionHandler.checkPermissionsForStoreAddress(uuid, parentUuid, userId);
		}

		if (isNewAddress) {
			// create new uuid
			uuid = UuidGenerator.getInstance().generateUuid();
			aDocIn.put(MdekKeys.UUID, uuid);
			// NOTICE: don't add further data, is done below when checking working copy !
		}
		
		// load node
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			// create new node, also take care of correct tree path in node
			aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			pathHandler.setTreePath(aNode, parentUuid);
		}
		
		// get/create working copy
		// if no working copy then new address -> address and addressNode have to be created
		if (!hasWorkingCopy(aNode)) {
			// no working copy yet, create new address/node with BASIC data

			// set some missing data which may not be passed from client.
			// set from published version if existent
			T02Address aPub = aNode.getT02AddressPublished();
			if (aPub != null) {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, aPub.getCreateTime());				
			} else {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			}
			T02Address aWork = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
			 // save it to generate id needed for mapping
			daoT02Address.makePersistent(aWork);
			
			// create/update node
			aNode.setAddrId(aWork.getId());
			aNode.setT02AddressWork(aWork);
			daoAddressNode.makePersistent(aNode);
		}

		// transfer detailed new data
		T02Address aWork = aNode.getT02AddressWork();
		docToBeanMapper.mapT02Address(aDocIn, aWork, MappingQuantity.DETAIL_ENTITY);

		// PERFORM CHECKS BEFORE STORING/COMMITTING !!!
		checkAddressNodeForStore(aNode);

		// store when ok
		daoT02Address.makePersistent(aWork);

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateAddressIndex(aNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (isNewAddress) {
			permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId);
		}
		
		return uuid;
	}

	/**
	 * Publish the address represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new address !
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of published address, will be generated if new address (no uuid passed in doc)
	 */
	public String publishAddress(IngridDocument aDocIn, String userId, boolean checkPermissions) {
		// uuid is null when new address !
		String uuid = (String) aDocIn.get(MdekKeys.UUID);
		boolean isNewAddress = (uuid == null) ? true : false;
		// parentUuid only passed if new address !
		String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);

		// set common data to transfer
		workflowHandler.processDocOnPublish(aDocIn);
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 
		aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);
		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(aDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);				
		}

		// check permissions !
		if (checkPermissions) {
			permissionHandler.checkPermissionsForPublishAddress(uuid, parentUuid, userId);			
		}

		if (isNewAddress) {
			// create new uuid
			uuid = UuidGenerator.getInstance().generateUuid();
			aDocIn.put(MdekKeys.UUID, uuid);
		}

		// load node
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			// create new node, also take care of correct tree path in node
			aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			pathHandler.setTreePath(aNode, parentUuid);
		}
		
		// get/create published version
		T02Address aPub = aNode.getT02AddressPublished();
		if (aPub == null) {
			// set some missing data which may not be passed from client.
			aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			
			// create new address with BASIC data
			aPub = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
			// save it to generate id needed for mapping of associations
			daoT02Address.makePersistent(aPub);
		}

		// transfer new data and store.
		docToBeanMapper.mapT02Address(aDocIn, aPub, MappingQuantity.DETAIL_ENTITY);
		daoT02Address.makePersistent(aPub);
		Long aPubId = aPub.getId();

		// and update AddressNode

		// delete former working copy if set
		T02Address aWork = aNode.getT02AddressWork();
		if (aWork != null && !aPubId.equals(aWork.getId())) {
			// delete working version
			daoT02Address.makeTransient(aWork);
		}
		// and set published one; also as work version
		// set also beans for oncoming access
		aNode.setAddrId(aPubId);
		aNode.setT02AddressWork(aPub);
		aNode.setAddrIdPublished(aPubId);
		aNode.setT02AddressPublished(aPub);
		daoAddressNode.makePersistent(aNode);

		// PERFORM CHECKS ON FINAL DATA BEFORE COMMITTING !!!
		checkAddressNodeForPublish(aNode);			
		// checks ok !

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateAddressIndex(aNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (isNewAddress) {
			permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId);
		}

		return uuid;
	}

	/**
	 * Move an address with its subtree to another parent.
	 * @param fromUuid uuid of node to move (this one will be removed from its parent)
	 * @param toUuid uuid of new parent, pass null if top node
	 * @param moveToFreeAddress<br>
	 * 		true=moved node is free address, parent has to be null<br>
	 * 		false=moved node is NOT free address, parent can be set, when parent is null
	 * 		copy is "normal" top address
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! address will be moved !
	 * @return map containing info (number of moved addresses)
	 */
	public IngridDocument moveAddress(String fromUuid, String toUuid, boolean moveToFreeAddress,
			String userId, boolean checkPermissions) {
		boolean isNewRootNode = (toUuid == null) ? true : false;

		// PERFORM CHECKS

		// check permissions !
		if (checkPermissions) {
			permissionHandler.checkPermissionsForMoveAddress(fromUuid, toUuid, userId);
		}

		AddressNode fromNode = loadByUuid(fromUuid, IdcEntityVersion.WORKING_VERSION);
		checkAddressNodesForMove(fromNode, toUuid, moveToFreeAddress);

		// CHECKS OK, proceed

		// process all moved nodes including top node (e.g. change tree path or date and mod_uuid) !
		IngridDocument resultDoc = processMovedNodes(fromNode, toUuid, moveToFreeAddress, userId);

		// grant write tree permission if new root node
		if (isNewRootNode) {
			permissionHandler.grantTreePermissionForAddress(fromUuid, userId);
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
	 * @param checkPermissions true=check whether user has delete permission<br>
	 * 		false=NO check on delete permission ! object will be deleted !
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteAddressWorkingCopy(String uuid, boolean forceDeleteReferences,
			String userId, boolean checkPermissions) {
		// first check permissions
		if (checkPermissions) {
			permissionHandler.checkPermissionsForDeleteWorkingCopyAddress(uuid, userId);
		}

		// NOTICE: this one also contains Parent Association !
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		Long idPublished = aNode.getAddrIdPublished();
		Long idWorkingCopy = aNode.getAddrId();

		IngridDocument result;

		// if we have NO published version -> delete complete node !
		if (idPublished == null) {
			result = deleteAddress(uuid, forceDeleteReferences, userId, checkPermissions);

		} else {
			// delete working copy only 
			result = new IngridDocument();
			result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
			result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);

			// perform delete of working copy only if really different version
			if (!idPublished.equals(idWorkingCopy)) {
				// delete working copy, BUT REMEMBER COMMENTS -> take over to published version !  
				T02Address aWorkingCopy = aNode.getT02AddressWork();
				IngridDocument commentsDoc = beanToDocMapper.mapAddressComments(aWorkingCopy.getAddressComments(), new IngridDocument());
				daoT02Address.makeTransient(aWorkingCopy);

				// take over comments to published version
				T02Address aPublished = aNode.getT02AddressPublished();
				docToBeanMapper.updateAddressComments(commentsDoc, aPublished);
				daoT02Address.makePersistent(aPublished);

				// and set published one as working copy
				aNode.setAddrId(idPublished);
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
	 * @param checkPermissions true=check whether user is QA / has delete permission<br>
	 * 		false=NO check on QA / delete permission ! address will be deleted !
	 * @return map containing info whether address was fully deleted, marked deleted ...
	 */
	public IngridDocument deleteAddressFull(String uuid, boolean forceDeleteReferences,
			String userId, boolean checkPermissions) {
		IngridDocument result;
		// NOTICE: Always returns true if workflow disabled !
		if (!checkPermissions || permissionHandler.hasQAPermission(userId)) {
			result = deleteAddress(uuid, forceDeleteReferences, userId, checkPermissions);
		} else {
			result = markDeletedAddress(uuid, forceDeleteReferences, userId, checkPermissions);
		}

		return result;
	}

	/**
	 * Assign address to QA ! 
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of stored address, will be generated if new address (no uuid passed in doc)
	 */
	public String assignAddressToQA(IngridDocument aDocIn, 
			String userId, boolean checkPermissions) {
		// set specific data to transfer to working copy and store !
		workflowHandler.processDocOnAssignToQA(aDocIn, userId);
		return storeWorkingCopy(aDocIn, userId, checkPermissions);
	}

	/** FULL DELETE ! MAKE TRANSIENT ! */
	private IngridDocument deleteAddress(String uuid, boolean forceDeleteReferences,
			String userUuid, boolean checkPermissions) {
		// first check User Permissions
		if (checkPermissions) {
			permissionHandler.checkPermissionsForDeleteAddress(uuid, userUuid);
		}

		// NOTICE: this one also contains Parent Association !
		AddressNode aNode = loadByUuid(uuid, IdcEntityVersion.WORKING_VERSION);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// check whether address is address of idcuser !
		if (daoIdcUser.getIdcUserByAddrUuid(uuid) != null) {
			throw new MdekException(new MdekError(MdekErrorType.ADDRESS_IS_IDCUSER_ADDRESS));			
		}

		// check whether topnode/subnodes are referenced
		checkAddressTreeReferences(aNode, forceDeleteReferences);

		// delete complete Node ! rest is deleted per cascade !
		daoAddressNode.makeTransient(aNode);

		// also delete ALL PERMISSIONS (no cascade by hibernate, we keep permissions out of data model)
		permissionHandler.deletePermissionsForAddress(uuid);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);
		result.put(MdekKeys.RESULTINFO_WAS_MARKED_DELETED, false);

		return result;
	}

	/** FULL DELETE IF NOT PUBLISHED !!!<br> 
	 * If published version exists -> Mark as deleted and assign to QA (already persisted)<br>
	 * if NO published version -> perform full delete !
	 */
	private IngridDocument markDeletedAddress(String uuid, boolean forceDeleteReferences,
			String userUuid, boolean checkPermissions) {
		// first check User Permissions
		if (checkPermissions) {
			permissionHandler.checkPermissionsForDeleteAddress(uuid, userUuid);			
		}

		// NOTICE: we just load NODE to determine whether published !
		AddressNode aNode = loadByUuid(uuid, null);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument result;

		// FULL DELETE IF NOT PUBLISHED !
		if (aNode.getAddrIdPublished() == null) {
			result = deleteAddress(uuid, forceDeleteReferences, userUuid, checkPermissions);
		} else {
			// IS PUBLISHED -> mark deleted
			// now load details (prefetch data) for faster mapping (less selects !) 
			aNode = daoAddressNode.getAddrDetails(uuid);

			// assign to QA via regular process to guarantee creation of working copy !
			// we generate doc via mapper and set MARK_DELETED !
			IngridDocument addrDoc =
				beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), new IngridDocument(), MappingQuantity.COPY_ENTITY);
			addrDoc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
			assignAddressToQA(addrDoc, userUuid, checkPermissions);

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

	/** Checks whether given Address has a working copy !
	 * @param node address to check represented by node !
	 * @return true=address has different working copy or not published yet<br>
	 * 	false=no working version, same as published version !
	 */
	private boolean hasWorkingCopy(AddressNode node) {
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
		String oldRootPath = rootNode.getTreePath();
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

	/** Check whether passed node is valid for publishing !
	 * (e.g. check free address conditions ...). CHECKS PUBLISHED VERSION IN NODE !
	 * Throws MdekException if not valid.
	 */
	private void checkAddressNodeForPublish(AddressNode node)
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
		AddressNode parentNode = null;
		AddressType parentType = null;
		if (parentUuid != null) {

			// check whether a parent is not published !
			List<String> pathUuids = daoAddressNode.getAddressPath(parentUuid);
			
			for (String pathUuid : pathUuids) {
				AddressNode pathNode = loadByUuid(pathUuid, null);
				if (pathNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}
				if (pathNode.getAddrIdPublished() == null) {
					throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
				}
			}

			parentNode = loadByUuid(parentUuid, IdcEntityVersion.PUBLISHED_VERSION);
			parentType = EnumUtil.mapDatabaseToEnumConst(AddressType.class,
					parentNode.getT02AddressPublished().getAdrType());

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
				if (childType != AddressType.INSTITUTION) {
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
		Boolean moveToFreeAddress)
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

			// new parent has to be published ! -> not possible to move published nodes under unpublished parent
			T02Address toAddrPub = toNode.getT02AddressPublished();
			if (toAddrPub == null) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}

			// is target subnode ?
			if (daoAddressNode.isSubNode(toUuid, fromUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.TARGET_IS_SUBNODE_OF_SOURCE));				
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
	private void checkAddressTreeReferences(AddressNode topNode, boolean forceDeleteReferences) {
		// process all subnodes including top node

		List<AddressNode> subNodes = daoAddressNode.getAllSubAddresses(
				topNode.getAddrUuid(), IdcEntityVersion.WORKING_VERSION, false);
		subNodes.add(0, topNode);

		for (AddressNode subNode : subNodes) {
			// check whether address is "auskunft" address. AUSKUNFT CANNOT BE DELETED
			// ALWAYS CALL THIS ONE BEFORE CHECK BELOW WHICH MAY REMOVE ALL REFERENCES (forceDeleteReferences, see below)
			checkAddressIsAuskunft(subNode);

			// check
			checkAddressNodeReferences(subNode, forceDeleteReferences);
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
	private void checkAddressNodeReferences(AddressNode aNode, boolean forceDeleteReferences) {
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

	/** Checks whether given address is "auskunft" address in any object.
	 * ONLY CHECKS WORKING VERSIONS OF OBJECTS !
	 * Throws exception if address is auskunft ! */
	private void checkAddressIsAuskunft(AddressNode addrNode) {
		List<ObjectNode> oNs =
			daoAddressNode.getObjectReferencesByTypeId(addrNode.getAddrUuid(), MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID);
		if (oNs.size() > 0) {
			// throw exception
			// supply info about referencing objects in exception
			IngridDocument errInfo = new IngridDocument();			
			List<IngridDocument> oList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeys.OBJ_ENTITIES, oList);

			for (ObjectNode oN : oNs) {
				IngridDocument oDoc = beanToDocMapper.mapT01Object(oN.getT01ObjectWork(),
							new IngridDocument(), MappingQuantity.BASIC_ENTITY);
				oList.add(oDoc);
			}
			throw new MdekException(new MdekError(MdekErrorType.ADDRESS_IS_AUSKUNFT, errInfo));
		}
	}
}
