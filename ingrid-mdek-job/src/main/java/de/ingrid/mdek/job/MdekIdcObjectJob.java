package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.tools.MdekIdcEntityComparer;
import de.ingrid.mdek.services.catalog.MdekAddressService;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekObjectService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning OBJECTS. 
 */
public class MdekIdcObjectJob extends MdekIdcJob {

	private MdekCatalogService catalogService;
	private MdekObjectService objectService;
	private MdekAddressService addressService;

	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;
	private MdekTreePathHandler pathHandler;

	private IObjectNodeDao daoObjectNode;
	private IT01ObjectDao daoT01Object;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;

	public MdekIdcObjectJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcObjectJob.class), daoFactory);

		catalogService = MdekCatalogService.getInstance(daoFactory);
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);
		pathHandler = MdekTreePathHandler.getInstance(daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoT01Object = daoFactory.getT01ObjectDao();

		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
	}

	public IngridDocument getTopObjects(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			String userUuid = getCurrentUserUuid(params);

			// fetch top Objects
			List<ObjectNode> oNs = daoObjectNode.getTopObjects(IdcEntityVersion.WORKING_VERSION, true);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(oNs.size());
			for (ObjectNode oN : oNs) {
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.TREE_ENTITY);
				
				// add permissions the user has on given object !
				List<Permission> perms = permissionHandler.getPermissionsForObject(oN.getObjUuid(), userUuid, true);
				beanToDocMapperSecurity.mapPermissionList(perms, objDoc);

				resultList.add(objDoc);
			}

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, resultList);

			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		try {
			String userUuid = getCurrentUserUuid(params);
			String uuid = (String) params.get(MdekKeys.UUID);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			List<ObjectNode> oNs = 
				daoObjectNode.getSubObjects(uuid, IdcEntityVersion.WORKING_VERSION, true);

			ArrayList<IngridDocument> subObjDocs = new ArrayList<IngridDocument>(oNs.size());
			for (ObjectNode oN : oNs) {
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapObjectNode(oN, objDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT01Object(oN.getT01ObjectWork(), objDoc, MappingQuantity.TREE_ENTITY);

				// add permissions the user has on given object !
				List<Permission> perms = 
					permissionHandler.getPermissionsForObject(oN.getObjUuid(), userUuid, true);
				beanToDocMapperSecurity.mapPermissionList(perms, objDoc);				

				subObjDocs.add(objDoc);
			}

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.OBJ_ENTITIES, subObjDocs);
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectPath(IngridDocument params) {
		try {
			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<String> uuidList = daoObjectNode.getObjectPath(uuid);

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.PATH, uuidList);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjDetails(IngridDocument params) {
		try {
			String userUuid = getCurrentUserUuid(params);
			String uuid = (String) params.get(MdekKeys.UUID);
			IdcEntityVersion whichEntityVersion =
				(IdcEntityVersion) params.get(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION);
			FetchQuantity fetchQuantity =
				(FetchQuantity) params.get(MdekKeys.REQUESTINFO_FETCH_QUANTITY);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			if (log.isDebugEnabled()) {
				log.debug("Invoke getObjDetails (uuid='"+uuid+"').");
			}
			IngridDocument result =	objectService.getObjectDetails(uuid,
					whichEntityVersion, fetchQuantity, userUuid);
			
			daoObjectNode.commitTransaction();

			return result;
			
		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/** Fetches WORKING VERSION ! */
	private IngridDocument getObjDetails(String objUuid, String userUuid) {
		return objectService.getObjectDetails(objUuid,
				IdcEntityVersion.WORKING_VERSION, FetchQuantity.EDITOR_ENTITY, userUuid);
	}
			
	public IngridDocument getInitialObject(IngridDocument oDocIn) {
		String userUuid = getCurrentUserUuid(oDocIn);
		try {
			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			// take over data from parent (if set)
			String parentUuid = oDocIn.getString(MdekKeys.PARENT_UUID);
			if (parentUuid != null) {
				ObjectNode pNode = objectService.loadByUuid(parentUuid, IdcEntityVersion.WORKING_VERSION);
				if (pNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}

				T01Object oParent = pNode.getT01ObjectWork();

				// supply separate parent info
				beanToDocMapper.mapObjectParentData(oParent, oDocIn);

				// take over initial data from parent
				beanToDocMapper.mapT01Object(oParent, oDocIn, MappingQuantity.INITIAL_ENTITY);
			}
			
			// "auskunft" address set ? set calling user as "Auskunft" if nothing set
			if (!objectService.hasAuskunftAddress(oDocIn)) {
				AddressNode addrNode = addressService.loadByUuid(userUuid, IdcEntityVersion.WORKING_VERSION);
				addAuskunftAddress(oDocIn, addrNode);
			}

			// take over spatial reference from catalog
			T03Catalogue catalog = catalogService.getCatalog();
			IngridDocument catalogDoc = new IngridDocument();
			beanToDocMapper.mapT03Catalog(catalog, catalogDoc);
			IngridDocument catDocLoc = (IngridDocument) catalogDoc.get(MdekKeys.CATALOG_LOCATION);
			if (catDocLoc != null) {
				ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(1);
				locList.add(catDocLoc);					
				oDocIn.put(MdekKeys.LOCATIONS, locList);
			}

			// INSPIRE: always add default values (will be displayed when new object is set to according class in frontend)
			beanToDocMapper.mapObjectConformitys(
					beanToDocMapper.createObjectConformitySet(MdekUtils.OBJ_CONFORMITY_SPECIFICATION_INSPIRE,
							MdekUtils.OBJ_CONFORMITY_NOT_EVALUATED),
					oDocIn);

			// add permissions the user has on initial object !
			List<Permission> perms = permissionHandler.getPermissionsForInitialObject(oDocIn, userUuid);
			beanToDocMapperSecurity.mapPermissionList(perms, oDocIn);

			daoObjectNode.commitTransaction();
			return oDocIn;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getWorkObjects(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		try {
			IdcWorkEntitiesSelectionType selectionType =
				(IdcWorkEntitiesSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			IdcEntityOrderBy orderBy = (IdcEntityOrderBy) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY);
			Boolean orderAsc = (Boolean) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result =	daoObjectNode.getWorkObjects(userUuid,
					selectionType, orderBy, orderAsc,
					startHit, numHits);

			List<ObjectNode> oNs = (List<ObjectNode>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumPaging = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			Long totalNumAssigned = (Long) result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED);
			Long totalNumReassigned = (Long) result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED);

			// map found objects and related user addresses to docs
			ArrayList<IngridDocument> oNDocs = new ArrayList<IngridDocument>(oNs.size());
			for (int i=0; i < oNs.size(); i++) {
				ObjectNode oN = oNs.get(i);
				T01Object o;
				// EXPIRED queries PUBLISHED version !
				if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED) {
					o = oN.getT01ObjectPublished();
				} else {
					o = oN.getT01ObjectWork();
				}

				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapT01Object(o, objDoc, MappingQuantity.BASIC_ENTITY);

				// DEBUGGING for tests !
				if (log.isDebugEnabled()) {
					// map some user uuids for debugging on client side !
					beanToDocMapper.mapModUser(o.getModUuid(), objDoc, MappingQuantity.INITIAL_ENTITY);
					beanToDocMapper.mapResponsibleUser(o.getResponsibleUuid(), objDoc, MappingQuantity.INITIAL_ENTITY);
					beanToDocMapper.mapAssignerUser(o.getObjectMetadata().getAssignerUuid(), objDoc, MappingQuantity.INITIAL_ENTITY);
				}

				// map details according to selection !
				if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED ||
					selectionType == IdcWorkEntitiesSelectionType.MODIFIED) {
					// map mod user
					AddressNode modAddressNode = o.getAddressNodeMod();
					if (modAddressNode != null) {
						beanToDocMapper.mapModUser(modAddressNode.getT02AddressWork(), objDoc);						
					}
				}
				if (selectionType == IdcWorkEntitiesSelectionType.MODIFIED ||
					selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW ||
					selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
					beanToDocMapper.mapUserOperation(oN, objDoc);
				}
				if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
					// map assigner user !
					beanToDocMapper.mapObjectMetadata(o.getObjectMetadata(), objDoc, MappingQuantity.DETAIL_ENTITY);
				}

				oNDocs.add(objDoc);
			}

			// set up result
			result = new IngridDocument();
			result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
			result.put(MdekKeys.TOTAL_NUM_QA_ASSIGNED, totalNumAssigned);
			result.put(MdekKeys.TOTAL_NUM_QA_REASSIGNED, totalNumReassigned);
			result.put(MdekKeys.OBJ_ENTITIES, oNDocs);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getQAObjects(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		try {
			WorkState whichWorkState = (WorkState) params.get(MdekKeys.REQUESTINFO_WHICH_WORK_STATE);
			IdcQAEntitiesSelectionType selectionType =
				(IdcQAEntitiesSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			IdcEntityOrderBy orderBy = (IdcEntityOrderBy) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY);
			Boolean orderAsc = (Boolean) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			boolean isCatAdmin = permissionHandler.isCatalogAdmin(userUuid);

			IngridDocument result =
				daoObjectNode.getQAObjects(userUuid, isCatAdmin, permissionHandler,
						whichWorkState, selectionType,
						orderBy, orderAsc,
						startHit, numHits);

			List<ObjectNode> oNs = (List<ObjectNode>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumPaging = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);

			// map found objects to docs
			ArrayList<IngridDocument> oNDocs = new ArrayList<IngridDocument>(oNs.size());
			for (ObjectNode oN : oNs) {
				T01Object o = oN.getT01ObjectWork();
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapT01Object(o, objDoc, MappingQuantity.BASIC_ENTITY);
				// map details according to selection (according to displayed data on QA page !)
				if (whichWorkState == WorkState.QS_UEBERWIESEN) {
					// map assigner user !
					beanToDocMapper.mapObjectMetadata(o.getObjectMetadata(), objDoc, MappingQuantity.DETAIL_ENTITY);
				} else {
					// map mod user !
					beanToDocMapper.mapObjectMetadata(o.getObjectMetadata(), objDoc, MappingQuantity.BASIC_ENTITY);					
					beanToDocMapper.mapModUser(o.getModUuid(), objDoc, MappingQuantity.DETAIL_ENTITY);
				}
				if (selectionType != IdcQAEntitiesSelectionType.EXPIRED) {
					beanToDocMapper.mapUserOperation(oN, objDoc);
				}

				oNDocs.add(objDoc);
			}

			// set up result
			result = new IngridDocument();
			result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
			result.put(MdekKeys.OBJ_ENTITIES, oNDocs);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectStatistics(IngridDocument params) {
//		String userUuid = getCurrentUserUuid(params);
		try {
			String parentUuid = (String) params.get(MdekKeys.UUID);
			IdcStatisticsSelectionType selectionType =
				(IdcStatisticsSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument result =
				daoObjectNode.getObjectStatistics(parentUuid, selectionType, startHit, numHits);

			daoObjectNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeObject(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			daoObjectNode.beginTransaction();

			// set specific data to transfer to working copy and store !
			workflowHandler.processDocOnStore(oDocIn);
			String uuid = objectService.storeWorkingCopy(oDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !
			daoObjectNode.commitTransaction();

			// return uuid (may be new generated uuid if new object)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid, userId);
				daoObjectNode.commitTransaction();
				
				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument assignObjectToQA(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			daoObjectNode.beginTransaction();

			String uuid = objectService.assignObjectToQA(oDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !
			daoObjectNode.commitTransaction();

			// return uuid (may be new generated uuid if new object)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid, userId);
				daoObjectNode.commitTransaction();
				
				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument reassignObjectToAuthor(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			daoObjectNode.beginTransaction();

			// set common data to transfer to working copy !
			workflowHandler.processDocOnReassignToAuthor(oDocIn, userId);
			String uuid = objectService.storeWorkingCopy(oDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !?
			daoObjectNode.commitTransaction();

			// return uuid (may be new generated uuid if new object)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid, userId);
				daoObjectNode.commitTransaction();
				
				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument updateObjectPart(IngridDocument oPartDocIn) {
		String userId = getCurrentUserUuid(oPartDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			String uuid = (String) oPartDocIn.get(MdekKeys.UUID);
			IdcEntityVersion whichEntityVersion = 
				(IdcEntityVersion) oPartDocIn.get(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION);

			daoObjectNode.beginTransaction();

			// check permissions !
			permissionHandler.checkWritePermissionForObject(uuid, userId, true);

			// load node
			ObjectNode oNode = objectService.loadByUuid(uuid, whichEntityVersion);
			if (oNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}

			// fetch versions to update
			Long oWorkId = null;
			if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
				oWorkId = oNode.getObjId();
			}
			Long oPubId = null;
			if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
				oPubId = oNode.getObjIdPublished();
			}

			// return null result if no update
			IngridDocument result = null;
			
			// update work version if requested
			if (oWorkId != null) {
				result = updateObjectPart(oNode.getT01ObjectWork(), oPartDocIn);
			}
			
			// update pub version if necessary
			if (oPubId != null && !oPubId.equals(oWorkId)) {
				result = updateObjectPart(oNode.getT01ObjectPublished(), oPartDocIn);
			}

			daoObjectNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	private IngridDocument updateObjectPart(T01Object o, IngridDocument oPartDocIn) {
		if (oPartDocIn.get(MdekKeys.EXPIRY_STATE) != null) {
			o.getObjectMetadata().setExpiryState((Integer) oPartDocIn.get(MdekKeys.EXPIRY_STATE));
		}
		daoT01Object.makePersistent(o);

		// not null indicates update executed
		return new IngridDocument();
	}

	public IngridDocument publishObject(IngridDocument oDocIn) {
		String userId = getCurrentUserUuid(oDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.PUBLISH, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			Boolean forcePubCondition = (Boolean) oDocIn.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);

			daoObjectNode.beginTransaction();
			
			String uuid = objectService.publishObject(oDocIn, forcePubCondition, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !?
			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoObjectNode.beginTransaction();
				result = getObjDetails(uuid, userId);
				daoObjectNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareObjectMaps(oDocIn, result, null)) {
						log.debug("Differences in Documents after publish/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 */
	public IngridDocument deleteObjectWorkingCopy(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			daoObjectNode.beginTransaction();

			IngridDocument result = objectService.deleteObjectWorkingCopy(uuid, forceDeleteReferences, userId, true);

			daoObjectNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/**
	 * FULL DELETE: different behavior when workflow enabled<br>
	 * - QA: full delete of object (working copy and published version) INCLUDING all subobjects !
	 * Object is non existent afterwards !<br>
	 * - NON QA: object is just marked deleted and assigned to QA<br>
	 * If workflow disabled every user acts like a QA (when having write access)
	 */
	public IngridDocument deleteObject(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			daoObjectNode.beginTransaction();

			IngridDocument result = objectService.deleteObjectFull(uuid, forceDeleteReferences, userId, true);

			daoObjectNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Move Object with its subtree to new parent. */
	public IngridDocument moveObject(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.MOVE, 0, 1, false));

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			Boolean forcePubCondition = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);

			daoObjectNode.beginTransaction();

			IngridDocument resultDoc = objectService.moveObject(
					fromUuid, toUuid, forcePubCondition, userUuid, true);

			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForObject(fromUuid, userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoObjectNode.commitTransaction();

			return resultDoc;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	/** Checks whether subtree of object has working copies. */
	public IngridDocument checkObjectSubTree(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.CHECK, 0, 1, false));

			String rootUuid = (String) params.get(MdekKeys.UUID);

			daoObjectNode.beginTransaction();
			daoObjectNode.disableAutoFlush();

			IngridDocument checkResult = checkObjectTreeWorkingCopies(rootUuid);

			daoObjectNode.commitTransaction();
			return checkResult;		

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Checks whether object branch has working copies (passed root is also checked !). */
	private IngridDocument checkObjectTreeWorkingCopies(String rootUuid) {
		// load "root"
		ObjectNode rootNode = objectService.loadByUuid(rootUuid, null);
		if (rootNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// process all subnodes including root

		List<ObjectNode> subNodes = daoObjectNode.getAllSubObjects(rootUuid, null, false);
		subNodes.add(0, rootNode);

		boolean hasWorkingCopy = false;
		String uuidOfWorkingCopy = null;
		int numberOfCheckedObj = 0;

		for (ObjectNode subNode : subNodes) {
			numberOfCheckedObj++;

			if (!subNode.getObjId().equals(subNode.getObjIdPublished())) {
				hasWorkingCopy = true;
				uuidOfWorkingCopy = subNode.getObjUuid();
				break;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Number of checked objects: " + numberOfCheckedObj);
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_HAS_WORKING_COPY, hasWorkingCopy);
		result.put(MdekKeys.RESULTINFO_UUID_OF_FOUND_ENTITY, uuidOfWorkingCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCheckedObj);

		return result;		
	}

	/** Copy Object to new parent (with or without its subtree). Returns basic data of copied top object. */
	public IngridDocument copyObject(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.COPY, 0, 1, false));

			daoObjectNode.beginTransaction();

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			boolean isNewRootNode = (toUuid == null) ? true : false;
			Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);

			// check permissions !
			permissionHandler.checkPermissionsForCopyObject(fromUuid, toUuid, userUuid);

			// perform checks
			ObjectNode fromNode = objectService.loadByUuid(fromUuid, IdcEntityVersion.WORKING_VERSION);
			if (fromNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
			}

			ObjectNode toNode = null;
			// NOTICE: copy to top when toUuid is null
			if (toUuid != null) {
				toNode = objectService.loadByUuid(toUuid, null);
				if (toNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
				}
			}

			// copy fromNode
			IngridDocument copyResult = createObjectNodeCopy(fromNode, toNode, copySubtree, userUuid);
			ObjectNode fromNodeCopy = (ObjectNode) copyResult.get(MdekKeys.OBJ_ENTITIES);
			Integer numCopiedObjects = (Integer) copyResult.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES);
			if (log.isDebugEnabled()) {
				log.debug("Number of copied objects: " + numCopiedObjects);
			}

			// success
			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapT01Object(fromNodeCopy.getT01ObjectWork(), resultDoc, MappingQuantity.TABLE_ENTITY);
			// also child info
			beanToDocMapper.mapObjectNode(fromNodeCopy, resultDoc, MappingQuantity.COPY_ENTITY);
			// and additional info
			resultDoc.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numCopiedObjects);

			// grant write tree permission if new root node
			if (isNewRootNode) {
				permissionHandler.grantTreePermissionForObject(fromNodeCopy.getObjUuid(), userUuid);
			}

			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForObject(fromNodeCopy.getObjUuid(), userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoObjectNode.commitTransaction();
			return resultDoc;		
		
		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	/**
	 * Creates a copy of the given ObjectNode and adds it under the given parent.
	 * Copies ONLY working version and IGNORES published version !
	 * Copied nodes are already Persisted !!!<br>
	 * NOTICE: Also copies whole subtree dependent from passed flag.<br>
	 * NOTICE: also supports copy of a tree to one of its subnodes !
	 * @param sourceNode copy this node
	 * @param newParentNode under this node
	 * @param copySubtree including subtree or not
	 * @param userUuid current user id needed to update running jobs
	 * @return doc containing additional info (copy of source node, number copied objects ...)
	 */
	private IngridDocument createObjectNodeCopy(ObjectNode sourceNode, ObjectNode newParentNode,
			boolean copySubtree, String userUuid)
	{
		// refine running jobs info
		int totalNumToCopy = 1;
		if (copySubtree) {
			// total num to copy: root + sub objects
			totalNumToCopy = 1 + 
				daoObjectNode.countAllSubObjects(sourceNode.getObjUuid(), IdcEntityVersion.ALL_VERSIONS);
			updateRunningJob(userUuid, createRunningJobDescription(JobType.COPY, 0, totalNumToCopy, false));				
		}

		// check whether we copy to subnode
		// then we have to check already copied nodes to avoid endless copy !
		boolean isCopyToOwnSubnode = false;
		ArrayList<String> uuidsCopiedNodes = null;
		if (newParentNode != null) {
			if (daoObjectNode.isSubNode(newParentNode.getObjUuid(), sourceNode.getObjUuid())) {
				isCopyToOwnSubnode = true;
				uuidsCopiedNodes = new ArrayList<String>();
			}
		}

		// copy iteratively via stack to avoid recursive stack overflow
		Stack<IngridDocument> stack = new Stack<IngridDocument>();
		IngridDocument nodeDoc = new IngridDocument();
		nodeDoc.put("NODE", sourceNode);
		nodeDoc.put("PARENT_NODE", newParentNode);
		stack.push(nodeDoc);

		int numberOfCopiedObj = 0;
		ObjectNode rootNodeCopy = null;
		while (!stack.isEmpty()) {
			nodeDoc = stack.pop();
			sourceNode = (ObjectNode) nodeDoc.get("NODE");
			newParentNode = (ObjectNode) nodeDoc.get("PARENT_NODE");

			if (log.isDebugEnabled()) {
				log.debug("Copying entity " + sourceNode.getObjUuid());
			}

			// copy source work version !
			String newUuid = UuidGenerator.getInstance().generateUuid();
			T01Object targetObjWork = createT01ObjectCopy(sourceNode.getT01ObjectWork(), newUuid, userUuid);
			
			// create new Node and set data !
			// we also set Beans in object node, so we can access them afterwards.
			Long targetObjWorkId = targetObjWork.getId();
			String newParentUuid = null;
			if (newParentNode != null) {
				newParentUuid = newParentNode.getObjUuid();
			}
			
			ObjectNode targetNode = new ObjectNode();
			targetNode.setObjUuid(newUuid);
			targetNode.setObjId(targetObjWorkId);
			targetNode.setT01ObjectWork(targetObjWork);
			targetNode.setFkObjUuid(newParentUuid);
			// also care for tree path !
			pathHandler.setTreePath(targetNode, newParentNode);
			daoObjectNode.makePersistent(targetNode);

			// add child bean to parent bean, so we can determine child info when mapping (without reloading)
			if (newParentNode != null) {
				newParentNode.getObjectNodeChildren().add(targetNode);
			}

			if (rootNodeCopy == null) {
				rootNodeCopy = targetNode;
			}
			if (isCopyToOwnSubnode) {
				uuidsCopiedNodes.add(newUuid);
			}

			numberOfCopiedObj++;

			// update our job information ! may be polled from client !
			// NOTICE: also checks whether job was canceled !
			updateRunningJob(userUuid, createRunningJobDescription(
					JobType.COPY, numberOfCopiedObj, totalNumToCopy, false));

			// copy subtree ? only if not already a copied node !
			if (copySubtree) {
				List<ObjectNode> sourceSubNodes = daoObjectNode.getSubObjects(sourceNode.getObjUuid(),
						IdcEntityVersion.WORKING_VERSION, false);
				for (ObjectNode sourceSubNode : sourceSubNodes) {
					if (isCopyToOwnSubnode) {
						if (uuidsCopiedNodes.contains(sourceSubNode.getObjUuid())) {
							// skip this node ! is one of our copied ones !
							continue;
						}
					}
					
					// add to stack, will be copied
					nodeDoc = new IngridDocument();
					nodeDoc.put("NODE", sourceSubNode);
					nodeDoc.put("PARENT_NODE", targetNode);
					stack.push(nodeDoc);
				}
			}
		}
		
		IngridDocument result = new IngridDocument();
		// copy of rootNode returned via OBJ_ENTITIES key !
		result.put(MdekKeys.OBJ_ENTITIES, rootNodeCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCopiedObj);

		return result;
	}

	/**
	 * Creates a copy of the given T01Object with the given NEW uuid. Already Persisted !
	 */
	private T01Object createT01ObjectCopy(T01Object sourceObj, String newUuid, String userUuid) {
		// create new object with new uuid and save it (to generate id !)
		T01Object targetObj = new T01Object();
		targetObj.setObjUuid(newUuid);
		daoT01Object.makePersistent(targetObj);

		// then copy content via mappers
		
		// map source bean to doc, ONLY data not specific to entity !
		IngridDocument targetObjDoc =
			beanToDocMapper.mapT01Object(sourceObj, new IngridDocument(), MappingQuantity.COPY_DATA);
		
		// update changed data in doc from source for target !
		targetObjDoc.put(MdekKeys.UUID, newUuid);
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		targetObjDoc.put(MdekKeys.DATE_OF_CREATION, currentTime);
		targetObjDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userUuid, targetObjDoc, MappingQuantity.INITIAL_ENTITY);
		beanToDocMapper.mapResponsibleUser(userUuid, targetObjDoc, MappingQuantity.INITIAL_ENTITY);				
		workflowHandler.processDocOnCopy(targetObjDoc);

		// and transfer data from doc to new bean
		docToBeanMapper.mapT01Object(targetObjDoc, targetObj, MappingQuantity.COPY_ENTITY);

		daoT01Object.makePersistent(targetObj);

		return targetObj;
	}

	/** Add Auskunft address to given object document.
	 * @param oDoc map representation of object
	 * @param addrNode add this address as auskunft. Also basic address data is mapped.
	 */
	private void addAuskunftAddress(IngridDocument oDoc, AddressNode addrNode) {
		List<IngridDocument> oAs = (List<IngridDocument>) oDoc.get(MdekKeys.ADR_REFERENCES_TO);
		if (oAs == null) {
			oAs = new ArrayList<IngridDocument>();
			oDoc.put(MdekKeys.ADR_REFERENCES_TO, oAs);
		}

		// simulate entities and map them one by one.
		// We can't map via "mapT012ObjAdrs" cause then entities have to be bound to database to fetch address node ...
		T012ObjAdr oA = new T012ObjAdr();
		oA.setType(MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID);
		oA.setSpecialRef(MdekSysList.OBJ_ADR_TYPE.getDbValue());
		IngridDocument oADoc = beanToDocMapper.mapT012ObjAdr(oA, new IngridDocument());
		beanToDocMapper.mapT02Address(addrNode.getT02AddressWork(), oADoc, MappingQuantity.TABLE_ENTITY);
		oAs.add(oADoc);					
	}

	/** Create Working Copy in given node, meaning the published object is copied and set "In Bearbeitung".<br>
	 * NOTICE: published object has to exist, so don't use this method when generating a NEW Node !<br>
	 * NOTICE: ALREADY PERSISTED (Node and T01Object !) */
/*
	private T01Object createWorkingCopyFromPublished(ObjectNode oNode) {
		if (!hasWorkingCopy(oNode)) {
			T01Object objWork = createT01ObjectCopy(oNode.getT01ObjectPublished(), oNode.getObjUuid());
			// set in Bearbeitung !
			objWork.setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());
			
			oNode.setObjId(objWork.getId());
			oNode.setT01ObjectWork(objWork);
			daoObjectNode.makePersistent(oNode);			
		}
		
		return oNode.getT01ObjectWork();
	}
*/
}
