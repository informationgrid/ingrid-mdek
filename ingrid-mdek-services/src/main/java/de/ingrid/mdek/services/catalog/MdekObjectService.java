package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
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
	 * NOTICE: PARENT_UUID has to be set in doc when new object !
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
}
