package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.job.tools.MdekIdcEntityComparer;
import de.ingrid.mdek.services.catalog.MdekAddressService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning ADDRESSES. 
 */
public class MdekIdcAddressJob extends MdekIdcJob {

	private MdekAddressService addressService;

	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;
	private MdekTreePathHandler pathHandler;

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;

	public MdekIdcAddressJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcAddressJob.class), daoFactory);

		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);
		pathHandler = MdekTreePathHandler.getInstance(daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();

		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
	}

	public IngridDocument getTopAddresses(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

			String userUuid = getCurrentUserUuid(params);
			Boolean onlyFreeAddressesIn = (Boolean) params.get(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES);
			boolean onlyFreeAddresses = (onlyFreeAddressesIn == null) ? false : onlyFreeAddressesIn;

			// fetch top Addresses
			List<AddressNode> aNs = daoAddressNode.getTopAddresses(onlyFreeAddresses);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(aNs.size());
			for (AddressNode aN : aNs) {
				IngridDocument adrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(aN, adrDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT02Address(aN.getT02AddressWork(), adrDoc, MappingQuantity.TREE_ENTITY);

				// add permissions the user has on given address !
				List<Permission> perms = permissionHandler.getPermissionsForAddress(aN.getAddrUuid(), userUuid, true);
				beanToDocMapperSecurity.mapPermissionList(perms, adrDoc);

				resultList.add(adrDoc);
			}
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.ADR_ENTITIES, resultList);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		try {
			String userUuid = getCurrentUserUuid(params);
			String uuid = (String) params.get(MdekKeys.UUID);

			daoAddressNode.beginTransaction();

			List<IngridDocument> subAddrDocs = 
				addressService.getSubAddresses(uuid, FetchQuantity.EDITOR_ENTITY, userUuid);

			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.ADR_ENTITIES, subAddrDocs);
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getAddressPath(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<String> uuidList = daoAddressNode.getAddressPath(uuid);

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.PATH, uuidList);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getAddrDetails(IngridDocument params) {
		try {
			String userUuid = getCurrentUserUuid(params);
			String uuid = (String) params.get(MdekKeys.UUID);
			IdcEntityVersion whichEntityVersion =
				(IdcEntityVersion) params.get(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION);
			int objRefsStartIndex = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);
			FetchQuantity fetchQuantity =
				(FetchQuantity) params.get(MdekKeys.REQUESTINFO_FETCH_QUANTITY);

			daoAddressNode.beginTransaction();

			if (log.isDebugEnabled()) {
				log.debug("Invoke getAddrDetails (uuid='"+uuid+"').");
			}
			IngridDocument result = addressService.getAddressDetails(uuid,
					whichEntityVersion, fetchQuantity, 
					objRefsStartIndex, objRefsMaxNum, userUuid);
			
			daoAddressNode.commitTransaction();

			return result;
			
		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/** Fetches WORKING VERSION ! */
	private IngridDocument getAddrDetails(String addrUuid, String userUuid,
			int objRefsStartIndex, int objRefsMaxNum) {
		return addressService.getAddressDetails(addrUuid,
				IdcEntityVersion.WORKING_VERSION, FetchQuantity.EDITOR_ENTITY, 
				objRefsStartIndex, objRefsMaxNum, userUuid);
	}

	public IngridDocument getAddressObjectReferences(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

//			String userUuid = getCurrentUserUuid(params);
			String addrUuid = (String) params.get(MdekKeys.UUID);
			int objRefsStartIndex = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			if (log.isDebugEnabled()) {
				log.debug("Invoke getAddressObjectReferences (uuid='"+addrUuid+"').");
			}

			// get objects referencing the given address
			HashMap fromObjectsData = 
				daoAddressNode.getObjectReferencesFrom(addrUuid, objRefsStartIndex, objRefsMaxNum);
			List<ObjectNode>[] fromLists = (List<ObjectNode>[]) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM);
			Integer objRefsTotalNum = (Integer) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM);

			// map the data to our result doc 
			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapObjectReferencesFrom(fromLists, objRefsStartIndex, objRefsTotalNum,
					IdcEntityType.ADDRESS, addrUuid, resultDoc, MappingQuantity.TABLE_ENTITY);

			daoAddressNode.commitTransaction();

			return resultDoc;
			
		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getInitialAddress(IngridDocument aDocIn) {
		String userUuid = getCurrentUserUuid(aDocIn);
		try {
			daoAddressNode.beginTransaction();
			
			// take over data from parent (if set)
			String parentUuid = aDocIn.getString(MdekKeys.PARENT_UUID);
			if (parentUuid != null) {
				AddressNode pNode = addressService.loadByUuid(parentUuid, IdcEntityVersion.WORKING_VERSION);
				if (pNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}

				T02Address aParent = pNode.getT02AddressWork();

				// supply separate parent info
				beanToDocMapper.mapAddressParentData(aParent, aDocIn);
				
				// take over initial data from parent
				beanToDocMapper.mapT02Address(aParent, aDocIn, MappingQuantity.INITIAL_ENTITY);

				// supply path info
				List<IngridDocument> pathList = daoAddressNode.getAddressPathOrganisation(parentUuid, true);
				aDocIn.put(MdekKeys.PATH_ORGANISATIONS, pathList);
			}

			// add permissions the user has on initial object !
			List<Permission> perms = permissionHandler.getPermissionsForInitialAddress(aDocIn, userUuid);
			beanToDocMapperSecurity.mapPermissionList(perms, aDocIn);

			daoAddressNode.commitTransaction();
			return aDocIn;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getWorkAddresses(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		try {
			IdcWorkEntitiesSelectionType selectionType =
				(IdcWorkEntitiesSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			IdcEntityOrderBy orderBy = (IdcEntityOrderBy) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY);
			Boolean orderAsc = (Boolean) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoAddressNode.beginTransaction();

			IngridDocument result =	daoAddressNode.getWorkAddresses(userUuid,
					selectionType, orderBy, orderAsc,
					startHit, numHits);

			List<AddressNode> aNs = (List<AddressNode>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumPaging = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			Long totalNumAssigned = (Long) result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED);
			Long totalNumReassigned = (Long) result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED);

			// map found addresses and related user addresses to docs
			ArrayList<IngridDocument> aNDocs = new ArrayList<IngridDocument>(aNs.size());
			for (int i=0; i < aNs.size(); i++) {
				AddressNode aN = aNs.get(i);
				T02Address a;
				// EXPIRED queries PUBLISHED version !
				if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED) {
					a = aN.getT02AddressPublished();
				} else {
					a = aN.getT02AddressWork();					
				}

				IngridDocument addrDoc = new IngridDocument();
				beanToDocMapper.mapT02Address(a, addrDoc, MappingQuantity.BASIC_ENTITY);

				// DEBUGGING for tests !
				if (log.isDebugEnabled()) {
					// map some user uuids for debugging on client side !
					beanToDocMapper.mapModUser(a.getModUuid(), addrDoc, MappingQuantity.INITIAL_ENTITY);
					beanToDocMapper.mapResponsibleUser(a.getResponsibleUuid(), addrDoc, MappingQuantity.INITIAL_ENTITY);
					beanToDocMapper.mapAssignerUser(a.getAddressMetadata().getAssignerUuid(), addrDoc, MappingQuantity.INITIAL_ENTITY);
				}

				// map details according to selection !
				if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED ||
					selectionType == IdcWorkEntitiesSelectionType.MODIFIED) {
					// map mod user
					AddressNode modAddressNode = a.getAddressNodeMod();
					if (modAddressNode != null) {
						beanToDocMapper.mapModUser(modAddressNode.getT02AddressWork(), addrDoc);						
					}
				}
				if (selectionType == IdcWorkEntitiesSelectionType.MODIFIED ||
					selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW ||
					selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
					beanToDocMapper.mapUserOperation(aN, addrDoc);
				}
				if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
					// map assigner user !
					beanToDocMapper.mapAddressMetadata(a.getAddressMetadata(), addrDoc, MappingQuantity.DETAIL_ENTITY);
				}

				aNDocs.add(addrDoc);
			}

			// set up result
			result = new IngridDocument();
			result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
			result.put(MdekKeys.TOTAL_NUM_QA_ASSIGNED, totalNumAssigned);
			result.put(MdekKeys.TOTAL_NUM_QA_REASSIGNED, totalNumReassigned);
			result.put(MdekKeys.ADR_ENTITIES, aNDocs);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getQAAddresses(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		try {
			WorkState whichWorkState = (WorkState) params.get(MdekKeys.REQUESTINFO_WHICH_WORK_STATE);
			IdcQAEntitiesSelectionType selectionType = (IdcQAEntitiesSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			IdcEntityOrderBy orderBy = (IdcEntityOrderBy) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY);
			Boolean orderAsc = (Boolean) params.get(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoAddressNode.beginTransaction();

			boolean isCatAdmin = permissionHandler.isCatalogAdmin(userUuid);

			IngridDocument result =
				daoAddressNode.getQAAddresses(userUuid, isCatAdmin, permissionHandler,
						whichWorkState, selectionType,
						orderBy, orderAsc,
						startHit, numHits);

			List<AddressNode> aNs = (List<AddressNode>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumPaging = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);

			// map found addresses to docs
			ArrayList<IngridDocument> aNDocs = new ArrayList<IngridDocument>(aNs.size());
			for (AddressNode aN : aNs) {
				T02Address a = aN.getT02AddressWork();
				IngridDocument addrDoc = new IngridDocument();
				beanToDocMapper.mapT02Address(a, addrDoc, MappingQuantity.BASIC_ENTITY);
				// map details according to selection (according to displayed data on QA page !)
				if (whichWorkState == WorkState.QS_UEBERWIESEN) {
					// map assigner user !
					beanToDocMapper.mapAddressMetadata(a.getAddressMetadata(), addrDoc, MappingQuantity.DETAIL_ENTITY);
				} else {
					// map mod user !
					beanToDocMapper.mapAddressMetadata(a.getAddressMetadata(), addrDoc, MappingQuantity.BASIC_ENTITY);					
					beanToDocMapper.mapModUser(a.getModUuid(), addrDoc, MappingQuantity.DETAIL_ENTITY);
				}
				if (selectionType != IdcQAEntitiesSelectionType.EXPIRED) {
					beanToDocMapper.mapUserOperation(aN, addrDoc);
				}

				aNDocs.add(addrDoc);
			}

			// set up result
			result = new IngridDocument();
			result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
			result.put(MdekKeys.ADR_ENTITIES, aNDocs);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getAddressStatistics(IngridDocument params) {
//		String userUuid = getCurrentUserUuid(params);
		try {
			String parentUuid = (String) params.get(MdekKeys.UUID);
			IdcStatisticsSelectionType selectionType = (IdcStatisticsSelectionType) params.get(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE);
			Boolean onlyFreeAddresses = (Boolean) params.get(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES);
			Integer startHit = (Integer) params.get(MdekKeys.REQUESTINFO_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.REQUESTINFO_NUM_HITS);

			daoAddressNode.beginTransaction();

			IngridDocument result =
				daoAddressNode.getAddressStatistics(parentUuid, onlyFreeAddresses, selectionType, startHit, numHits);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeAddress(IngridDocument aDocIn) {
		String userId = getCurrentUserUuid(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			int objRefsStartIndex = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			daoAddressNode.beginTransaction();

			// set specific data to transfer to working copy and store !
			workflowHandler.processDocOnStore(aDocIn);
			String uuid = addressService.storeWorkingCopy(aDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			// return uuid (may be new generated uuid if new address)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid, userId, objRefsStartIndex, objRefsMaxNum);
				daoAddressNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareAddressMaps(aDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument assignAddressToQA(IngridDocument aDocIn) {
		String userId = getCurrentUserUuid(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			int objRefsStartIndex = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			daoAddressNode.beginTransaction();

			String uuid = addressService.assignAddressToQA(aDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			// return uuid (may be new generated uuid if new address)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid, userId, objRefsStartIndex, objRefsMaxNum);
				daoAddressNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareAddressMaps(aDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn) {
		String userId = getCurrentUserUuid(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			int objRefsStartIndex = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			daoAddressNode.beginTransaction();

			// set specific data to transfer to working copy and store !
			workflowHandler.processDocOnReassignToAuthor(aDocIn, userId);
			String uuid = addressService.storeWorkingCopy(aDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			// return uuid (may be new generated uuid if new address)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid, userId, objRefsStartIndex, objRefsMaxNum);
				daoAddressNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareAddressMaps(aDocIn, result, null)) {
						log.debug("Differences in Documents after store/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument updateAddressPart(IngridDocument aPartDocIn) {
		String userId = getCurrentUserUuid(aPartDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			String uuid = (String) aPartDocIn.get(MdekKeys.UUID);
			IdcEntityVersion whichEntityVersion = (IdcEntityVersion) aPartDocIn.get(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION);

			daoAddressNode.beginTransaction();

			// check permissions !
			permissionHandler.checkWritePermissionForAddress(uuid, userId, true);

			// load node
			AddressNode aNode = addressService.loadByUuid(uuid, whichEntityVersion);
			if (aNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}

			// fetch versions to update
			Long aWorkId = null;
			if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
				aWorkId = aNode.getAddrId();
			}
			Long aPubId = null;
			if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
				whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
				aPubId = aNode.getAddrIdPublished();
			}

			// return null result if no update
			IngridDocument result = null;
			
			// update work version if requested
			if (aWorkId != null) {
				result = updateAddressPart(aNode.getT02AddressWork(), aPartDocIn);
			}
			
			// update pub version if necessary
			if (aPubId != null && !aPubId.equals(aWorkId)) {
				result = updateAddressPart(aNode.getT02AddressPublished(), aPartDocIn);
			}

			daoAddressNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	private IngridDocument updateAddressPart(T02Address a, IngridDocument aPartDocIn) {
		if (aPartDocIn.get(MdekKeys.EXPIRY_STATE) != null) {
			a.getAddressMetadata().setExpiryState((Integer) aPartDocIn.get(MdekKeys.EXPIRY_STATE));
		}
		daoT02Address.makePersistent(a);

		// not null indicates update executed
		return new IngridDocument();
	}

	public IngridDocument publishAddress(IngridDocument aDocIn) {
		String userId = getCurrentUserUuid(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.PUBLISH, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			int objRefsStartIndex = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			daoAddressNode.beginTransaction();
			
			String uuid = addressService.publishAddress(aDocIn, userId, true);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid, userId, objRefsStartIndex, objRefsMaxNum);
				daoAddressNode.commitTransaction();

				if (log.isDebugEnabled()) {
					if (!MdekIdcEntityComparer.compareAddressMaps(aDocIn, result, null)) {
						log.debug("Differences in Documents after publish/refetch detected!");
					}
				}
			}
			
			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Copy Address to new parent (with or without its subtree). Returns basic data of copied top address. */
	public IngridDocument copyAddress(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.COPY, 0, 1, false));

			daoAddressNode.beginTransaction();

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			boolean isNewRootNode = (toUuid == null) ? true : false;
			Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);
			Boolean targetIsFreeAddress = (Boolean) params.get(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS);

			// check permissions !
			permissionHandler.checkPermissionsForCopyAddress(fromUuid, toUuid, userUuid);

			// copy fromNode
			IngridDocument copyResult = createAddressNodeCopy(fromUuid, toUuid,
					copySubtree, targetIsFreeAddress, userUuid);
			AddressNode fromNodeCopy = (AddressNode) copyResult.get(MdekKeys.ADR_ENTITIES);
			Integer numCopiedAddresses = (Integer) copyResult.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES);
			if (log.isDebugEnabled()) {
				log.debug("Number of copied addresses: " + numCopiedAddresses);
			}

			// success
			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapT02Address(fromNodeCopy.getT02AddressWork(), resultDoc, MappingQuantity.TABLE_ENTITY);
			// also child info
			beanToDocMapper.mapAddressNode(fromNodeCopy, resultDoc, MappingQuantity.COPY_ENTITY);
			// and additional info
			resultDoc.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numCopiedAddresses);
			// and path info
			List<IngridDocument> pathList = daoAddressNode.getAddressPathOrganisation(fromNodeCopy.getAddrUuid(), false);
			resultDoc.put(MdekKeys.PATH_ORGANISATIONS, pathList);

			// grant write tree permission if new root node
			if (isNewRootNode) {
				permissionHandler.grantTreePermissionForAddress(fromNodeCopy.getAddrUuid(), userUuid);
			}

			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForAddress(fromNodeCopy.getAddrUuid(), userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoAddressNode.commitTransaction();
			return resultDoc;		
		
		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	/** Move Address with its subtree to new parent. */
	public IngridDocument moveAddress(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.MOVE, 0, 1, false));

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			Boolean targetIsFreeAddress = (Boolean) params.get(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS);

			daoAddressNode.beginTransaction();

			IngridDocument resultDoc = addressService.moveAddress(fromUuid, toUuid, targetIsFreeAddress,
					userUuid, true);

			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForAddress(fromUuid, userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoAddressNode.commitTransaction();

			return resultDoc;		

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	/** Checks whether subtree of address has working copies. */
	public IngridDocument checkAddressSubTree(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.CHECK, 0, 1, false));

			String rootUuid = (String) params.get(MdekKeys.UUID);

			daoAddressNode.beginTransaction();

			IngridDocument checkResult = checkAddressTreeWorkingCopies(rootUuid);

			daoAddressNode.commitTransaction();
			return checkResult;		

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Checks whether address branch has working copies (passed root is also checked !). */
	private IngridDocument checkAddressTreeWorkingCopies(String rootUuid) {
		// load "root"
		AddressNode rootNode = addressService.loadByUuid(rootUuid, null);
		if (rootNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// process all subnodes including root

		List<AddressNode> subNodes = daoAddressNode.getAllSubAddresses(rootUuid, null, false);
		subNodes.add(0, rootNode);

		boolean hasWorkingCopy = false;
		String uuidOfWorkingCopy = null;
		int numberOfCheckedAddr = 0;

		for (AddressNode subNode : subNodes) {
			numberOfCheckedAddr++;

			if (!subNode.getAddrId().equals(subNode.getAddrIdPublished())) {
				hasWorkingCopy = true;
				uuidOfWorkingCopy = subNode.getAddrUuid();
				break;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Number of checked addresses: " + numberOfCheckedAddr);
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_HAS_WORKING_COPY, hasWorkingCopy);
		result.put(MdekKeys.RESULTINFO_UUID_OF_FOUND_ENTITY, uuidOfWorkingCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCheckedAddr);

		return result;		
	}

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the address is deleted completely, meaning non existent afterwards
	 * (including all sub addresses !)
	 */
	public IngridDocument deleteAddressWorkingCopy(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			daoAddressNode.beginTransaction();
			
			IngridDocument result = addressService.deleteAddressWorkingCopy(uuid, forceDeleteReferences, userId, true);

			daoAddressNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
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
	 * - QA: full delete of address (working copy and published version) INCLUDING all subaddresses !
	 * Address is non existent afterwards !<br>
	 * - NON QA: address is just marked deleted and assigned to QA<br>
	 * If workflow disabled every user acts like a QA (when having write access)
	 */
	public IngridDocument deleteAddress(IngridDocument params) {
		String userId = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			daoAddressNode.beginTransaction();
			
			IngridDocument result = addressService.deleteAddressFull(uuid, forceDeleteReferences, userId, true);

			daoAddressNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument searchAddresses(IngridDocument params) {
		try {
			Integer inStartHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer inNumHits = ((Long) params.get(MdekKeys.TOTAL_NUM)).intValue();
			IngridDocument inSearchParams = (IngridDocument) params.get(MdekKeys.SEARCH_PARAMS);

			daoAddressNode.beginTransaction();

			long totalNumHits = daoAddressNode.searchTotalNumAddresses(inSearchParams);

			List<AddressNode> hits = new ArrayList<AddressNode>();
			if (totalNumHits > 0 &&	inStartHit < totalNumHits) {
				hits = daoAddressNode.searchAddresses(inSearchParams, inStartHit, inNumHits);
			}

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(hits.size());
			for (AddressNode hit : hits) {
				IngridDocument adrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(hit, adrDoc, MappingQuantity.BASIC_ENTITY);
				beanToDocMapper.mapT02Address(hit.getT02AddressWork(), adrDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(adrDoc);
			}

			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumHits);
			result.put(MdekKeys.ADR_ENTITIES, resultList);
			result.put(MdekKeys.TOTAL_NUM, new Long(resultList.size()));

			return result;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	/**
	 * Creates a copy of the given AddressNode and adds it under the given parent.
	 * Copies ONLY working version and IGNORES published version !
	 * Copied nodes are already Persisted !!!<br>
	 * NOTICE: Also copies whole subtree dependent from passed flag.<br>
	 * NOTICE: also supports copy of a tree to one of its subnodes !<br>
	 * @param sourceUuid copy this node
	 * @param newParentUuid under this node
	 * @param copySubtree including subtree or not
	 * @param copyToFreeAddress copy is "free address"
	 * @param userUuid current user id needed to update running jobs
	 * @return doc containing additional info (copy of source node, number copied nodes ...)
	 */
	private IngridDocument createAddressNodeCopy(String sourceUuid, String newParentUuid,
			boolean copySubtree, boolean copyToFreeAddress, String userUuid)
	{
		// PERFORM CHECKS
		
		// parent node exists ?
		// NOTICE: copy to top when newParentUuid is null
		AddressNode newParentNode = null;
		if (newParentUuid != null) {
			newParentNode = addressService.loadByUuid(newParentUuid, IdcEntityVersion.WORKING_VERSION);
			if (newParentNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
			}
		}
		// further checks
		AddressNode sourceNode = addressService.loadByUuid(sourceUuid, IdcEntityVersion.WORKING_VERSION);
		checkAddressNodesForCopy(sourceNode, newParentNode, copySubtree, copyToFreeAddress);

		// refine running jobs info
		int totalNumToCopy = 1;
		if (copySubtree) {
			// total num to copy: root + sub addresses
			totalNumToCopy = 1 + daoAddressNode.countAllSubAddresses(sourceNode.getAddrUuid());
			updateRunningJob(userUuid, createRunningJobDescription(JobType.COPY, 0, totalNumToCopy, false));				
		}

		// check whether we copy to subnode
		// then we have to check already copied nodes to avoid endless copy !
		boolean isCopyToOwnSubnode = false;
		ArrayList<String> uuidsCopiedNodes = null;
		if (newParentNode != null) {
			if (daoAddressNode.isSubNode(newParentNode.getAddrUuid(), sourceNode.getAddrUuid())) {
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

		int numberOfCopiedAddr = 0;
		AddressNode nodeCopy = null;
		while (!stack.isEmpty()) {
			nodeDoc = stack.pop();
			sourceNode = (AddressNode) nodeDoc.get("NODE");
			newParentNode = (AddressNode) nodeDoc.get("PARENT_NODE");

			if (log.isDebugEnabled()) {
				log.debug("Copying entity " + sourceNode.getAddrUuid());
			}

			// copy source work version !
			String newUuid = UuidGenerator.getInstance().generateUuid();
			T02Address targetAddrWork = createT02AddressCopy(sourceNode.getT02AddressWork(), newUuid, userUuid);

			// handle copies from/to "free address"
			addressService.processMovedOrCopiedAddress(targetAddrWork, copyToFreeAddress);

			// create new Node and set data !
			// we also set Beans in address node, so we can access them afterwards.
			Long targetAddrWorkId = targetAddrWork.getId();
			newParentUuid = null;
			if (newParentNode != null) {
				newParentUuid = newParentNode.getAddrUuid();
			}
			
			AddressNode targetNode = new AddressNode();
			targetNode.setAddrUuid(newUuid);
			targetNode.setAddrId(targetAddrWorkId);
			targetNode.setT02AddressWork(targetAddrWork);
			targetNode.setFkAddrUuid(newParentUuid);
			// also care for tree path !
			pathHandler.setTreePath(targetNode, newParentNode);
			daoAddressNode.makePersistent(targetNode);

			// add child bean to parent bean, so we can determine child info when mapping (without reloading)
			if (newParentNode != null) {
				newParentNode.getAddressNodeChildren().add(targetNode);
			}

			if (nodeCopy == null) {
				nodeCopy = targetNode;
			}

			if (isCopyToOwnSubnode) {
				uuidsCopiedNodes.add(newUuid);
			}
			
			numberOfCopiedAddr++;

			// update our job information ! may be polled from client !
			// NOTICE: also checks whether job was canceled !
			updateRunningJob(userUuid, createRunningJobDescription(
					JobType.COPY, numberOfCopiedAddr, totalNumToCopy, false));

			// copy subtree ? only if not already a copied node !
			if (copySubtree) {
				List<AddressNode> sourceSubNodes = daoAddressNode.getSubAddresses(
						sourceNode.getAddrUuid(),
						IdcEntityVersion.WORKING_VERSION, false);
				for (AddressNode sourceSubNode : sourceSubNodes) {
					if (isCopyToOwnSubnode) {
						if (uuidsCopiedNodes.contains(sourceSubNode.getAddrUuid())) {
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
		// copy of rootNode returned via ADR_ENTITIES key !
		result.put(MdekKeys.ADR_ENTITIES, nodeCopy);
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfCopiedAddr);

		return result;
	}

	/**
	 * Creates a copy of the given T02Address with the given NEW uuid. Already Persisted !
	 */
	private T02Address createT02AddressCopy(T02Address sourceAddr, String newUuid, String userUuid) {
		// create new address with new uuid and save it (to generate id !)
		T02Address targetAddr = new T02Address();
		targetAddr.setAdrUuid(newUuid);
		daoT02Address.makePersistent(targetAddr);

		// then copy content via mappers
		
		// map source bean to doc
		IngridDocument targetAddrDoc =
			beanToDocMapper.mapT02Address(sourceAddr, new IngridDocument(), MappingQuantity.COPY_ENTITY);
		
		// update changed data in doc from source for target !
		targetAddrDoc.put(MdekKeys.UUID, newUuid);
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		targetAddrDoc.put(MdekKeys.DATE_OF_CREATION, currentTime);
		targetAddrDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userUuid, targetAddrDoc, MappingQuantity.INITIAL_ENTITY);
		beanToDocMapper.mapResponsibleUser(userUuid, targetAddrDoc, MappingQuantity.INITIAL_ENTITY);				
		workflowHandler.processDocOnCopy(targetAddrDoc);

		// and transfer data from doc to new bean
		docToBeanMapper.mapT02Address(targetAddrDoc, targetAddr, MappingQuantity.COPY_ENTITY);

		daoT02Address.makePersistent(targetAddr);

		return targetAddr;
	}

	/** Check whether passed nodes are valid for copy operation
	 * (e.g. check free address conditions ...). Throws MdekException if not valid.
	 */
	private void checkAddressNodesForCopy(AddressNode fromNode, AddressNode toNode,
		Boolean copySubtree,
		Boolean copyToFreeAddress)
	{
		if (fromNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.FROM_UUID_NOT_FOUND));
		}
		AddressType fromType = EnumUtil.mapDatabaseToEnumConst(AddressType.class,
				fromNode.getT02AddressWork().getAdrType());

		// basic free address checks
		if (copyToFreeAddress) {
			if (toNode != null) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}
			if (copySubtree) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_SUBTREE));
			}
		}

		AddressType toType = null;
		if (toNode != null) {
			toType = EnumUtil.mapDatabaseToEnumConst(AddressType.class,
					toNode.getT02AddressWork().getAdrType());			
		}

		// check address type conflicts !
		addressService.checkAddressTypes(toType, fromType, copyToFreeAddress, false);
	}
}
