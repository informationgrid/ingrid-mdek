package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to object entity data.
 */
public class MdekObjectService {

	private IObjectNodeDao daoObjectNode;
	private IT01ObjectDao daoT01Object;

	private MdekCatalogService catalogService;
	private MdekTreePathHandler pathHandler;
	private MdekFullIndexHandler fullIndexHandler;
	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;

	protected BeanToDocMapper beanToDocMapper;
	protected DocToBeanMapper docToBeanMapper;

	private static MdekObjectService myInstance;

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

		catalogService = MdekCatalogService.getInstance(daoFactory);
		pathHandler = MdekTreePathHandler.getInstance(daoFactory);
		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
	}

	/** Load object NODE with given uuid. Also prefetch concrete object instance in node if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param uuid object uuid
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		return daoObjectNode.loadByUuid(uuid, whichEntityVersion);
	}

	/**
	 * Fetches sub nodes (next level) of parent with given uuid. 
	 * Also prefetch concrete object instance in nodes if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	public List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		List<ObjectNode> oNs = daoObjectNode.getSubObjects(
				parentUuid, whichEntityVersion, fetchSubNodesChildren);
		return oNs;
	}

	/**
	 * Store WORKING COPY of the object represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new object !
	 * @param oDocIn doc representing object
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of stored object, will be generated if new object (no uuid passed in doc)
	 */
	public String storeWorkingCopy(IngridDocument oDocIn, String userId, boolean checkPermissions) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		boolean isNewObject = (uuid == null) ? true : false;
		// parentUuid only passed if new object !?
		String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);

		// set common data to transfer to working copy !
		oDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userId, oDocIn, MappingQuantity.INITIAL_ENTITY);
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
		if (checkPermissions) {
			permissionHandler.checkPermissionsForStoreObject(uuid, parentUuid, userId);			
		}

		if (isNewObject) {
			// create new uuid
			uuid = UuidGenerator.getInstance().generateUuid();
			oDocIn.put(MdekKeys.UUID, uuid);
			// NOTICE: don't add further data, is done below when checking working copy !
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

			// set some missing data which is NOT passed from client.
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

			// also SAVE t08_attrs from published in work version (copy) -> NOT MAPPED VIA 
			// docToBeanMapper (at the moment NOT part of UI) !
			// we do this, so querying t08_attribs in edited objects (working version) works (otherwise
			// could only be queried in published version) !
			// NOTICE: when publishing the work version, its t08_attribs aren't mapped (the OLD published
			// object is loaded containing the original t08_attrs) !
			if (oPub != null) {
				docToBeanMapper.updateT08Attrs(
						beanToDocMapper.mapT08Attrs(oPub.getT08Attrs(), new IngridDocument()),
						oWork);					
			}

			// update node
			oNode.setObjId(oWork.getId());
			oNode.setT01ObjectWork(oWork);
			daoObjectNode.makePersistent(oNode);
		}

		// transfer new data and store.
		T01Object oWork = oNode.getT01ObjectWork();
		docToBeanMapper.mapT01Object(oDocIn, oWork, MappingQuantity.DETAIL_ENTITY);
		daoT01Object.makePersistent(oWork);

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateObjectIndex(oNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (isNewObject) {
			permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId);
		}

		return uuid;
	}

	/**
	 * Publish the object represented by the passed doc.<br>
	 * NOTICE: pass PARENT_UUID in doc when new object !
	 * @param oDocIn doc representing object
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of published object, will be generated if new object (no uuid passed in doc)
	 */
	public String publishObject(IngridDocument oDocIn, String userId, boolean checkPermissions) {
		// uuid is null when new object !
		String uuid = (String) oDocIn.get(MdekKeys.UUID);
		boolean isNewObject = (uuid == null) ? true : false;
		// parentUuid only passed if new object !
		String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);

		Integer pubTypeIn = (Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION);
		Boolean forcePubCondition = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		// PERFORM CHECKS
		// NOTICE: passed object may NOT exist yet (new object published immediately)

		// check permissions !
		if (checkPermissions) {
			permissionHandler.checkPermissionsForPublishObject(uuid, parentUuid, userId);
		}

		// "auskunft" address set
		if (!hasAuskunftAddress(oDocIn)) {
			throw new MdekException(new MdekError(MdekErrorType.AUSKUNFT_ADDRESS_NOT_SET));
		}
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
		beanToDocMapper.mapModUser(userId, oDocIn, MappingQuantity.INITIAL_ENTITY);
		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(oDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, oDocIn, MappingQuantity.INITIAL_ENTITY);				
		}

		if (isNewObject) {
			// create new uuid
			uuid = UuidGenerator.getInstance().generateUuid();
			oDocIn.put(MdekKeys.UUID, uuid);
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

			// create new object with BASIC data
			oPub = docToBeanMapper.mapT01Object(oDocIn, new T01Object(), MappingQuantity.BASIC_ENTITY);
			// save it to generate id needed for mapping of associations
			daoT01Object.makePersistent(oPub);
		}

		// transfer new data and store.
		docToBeanMapper.mapT01Object(oDocIn, oPub, MappingQuantity.DETAIL_ENTITY);
		daoT01Object.makePersistent(oPub);
		Long oPubId = oPub.getId();

		// and update ObjectNode

		// delete former working copy if set
		T01Object oWork = oNode.getT01ObjectWork();
		if (oWork != null && !oPubId.equals(oWork.getId())) {
			// delete working version
			daoT01Object.makeTransient(oWork);
		}
		
		// set data on node, also beans, so we can access them afterwards (index)
		oNode.setObjId(oPubId);
		oNode.setT01ObjectWork(oPub);
		oNode.setObjIdPublished(oPubId);
		oNode.setT01ObjectPublished(oPub);
		daoObjectNode.makePersistent(oNode);
		
		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateObjectIndex(oNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (isNewObject) {
			permissionHandler.grantTreePermissionForObject(oNode.getObjUuid(), userId);
		}

		return uuid;
	}

	/** Checks whether given Object has a working copy !
	 * @param oNode object to check represented by node !
	 * @return true=object has different working copy or not published yet<br>
	 * 	false=no working version, same as published version !
	 */
	public boolean hasWorkingCopy(ObjectNode oNode) {
		Long oWorkId = oNode.getObjId(); 
		Long oPubId = oNode.getObjIdPublished(); 
		if (oWorkId == null || oWorkId.equals(oPubId)) {
			return false;
		}
		
		return true;
	}

	/** Checks whether given object document has an "Auskunft" address set. */
	public boolean hasAuskunftAddress(IngridDocument oDoc) {
		List<IngridDocument> oAs = (List<IngridDocument>) oDoc.get(MdekKeys.ADR_REFERENCES_TO);
		if (oAs == null) {
			oAs = new ArrayList<IngridDocument>();
		}

		for (IngridDocument oA : oAs) {
			boolean typeOk = MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID.equals(oA.get(MdekKeys.RELATION_TYPE_ID));
			boolean listOk = MdekSysList.OBJ_ADR_TYPE.getDbValue().equals(oA.get(MdekKeys.RELATION_TYPE_REF));
			if (typeOk && listOk) {
				return true;
			}
		}
		
		return false;
	}

	/** Checks whether node has unpublished parents. Throws MdekException if so. */
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
			if (pathNode.getObjIdPublished() == null) {
				throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
			}
		}
	}

	/** Check whether publication condition of parent fits to publication condition of child.<br>
	 * NOTICE: PublishedVersion of parent is checked !<br>
	 * Throws Exception if not fitting */
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
		T01Object parentObjPub =
			loadByUuid(parentUuid, IdcEntityVersion.PUBLISHED_VERSION).getT01ObjectPublished();
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

	/** Checks whether a tree fits to a new publication condition.
	 * !!! ALSO ADAPTS Publication Conditions IF REQUESTED !!!<br>
	 * NOTICE: ONLY PUBLISHED versions of subnodes are checked and adapted !!!
	 * @param rootUuid uuid of top node of tree
	 * @param pubTypeTopDB new publication type
	 * @param forcePubCondition force change of nodes (modification time, publicationType, ...)
	 * @param skipRootNode check/change top node, e.g. when moving (true) or not, e.g. when publishing (false)
	 * @param modTime modification time to store in modified nodes
	 * @param modUuid user uuid to set as modification user
	 */
	public void checkObjectPublicationConditionSubTree(String rootUuid,
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

			PublishType objPubType = EnumUtil.mapDatabaseToEnumConst(PublishType.class, objPub.getPublishId());				
			if (!pubTypeNew.includes(objPubType)) {
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
}
