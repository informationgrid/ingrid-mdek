/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.iplug.dsc.index.DscDocumentProducer;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
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
import de.ingrid.mdek.services.utils.EntityHelper;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler.GroupType;
import de.ingrid.mdek.services.utils.MdekRecordUtils;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.MdekWorkflowHandler;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.tool.XsltUtils;
import de.ingrid.utils.xml.XMLUtils;

/**
 * Encapsulates all Job functionality concerning ADDRESSES. 
 */
@Service
public class MdekIdcAddressJob extends MdekIdcJob {

	private MdekAddressService addressService;

	private MdekPermissionHandler permissionHandler;
	private MdekWorkflowHandler workflowHandler;
	private MdekTreePathHandler pathHandler;

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;

	private XsltUtils xsltUtils;

	@Autowired
	@Qualifier("dscDocumentProducerAddress")
	private DscDocumentProducer docProducer;
    
    @Autowired
    @Qualifier("dscRecordCreatorAddress")
    private DscRecordCreator dscRecordProducer;

    private IndexManager indexManager;

	@Autowired
	public MdekIdcAddressJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService,
            IndexManager indexManager) {
		super(logService.getLogger(MdekIdcAddressJob.class), daoFactory);

		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);
		pathHandler = MdekTreePathHandler.getInstance(daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();

		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
		this.indexManager = indexManager;
        
        xsltUtils = new XsltUtils();
	}

	public IngridDocument getTopAddresses(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();
			daoAddressNode.disableAutoFlush();

			String userUuid = getCurrentUserUuid(params);
			Boolean onlyFreeAddressesIn = (Boolean) params.get(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES);
			boolean onlyFreeAddresses = (onlyFreeAddressesIn == null) ? false : onlyFreeAddressesIn;

			// fetch top Addresses
			List<AddressNode> aNs = daoAddressNode.getTopAddresses(
					onlyFreeAddresses, IdcEntityVersion.WORKING_VERSION, true);

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

			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.ADR_ENTITIES, resultList);

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		try {
			String userUuid = getCurrentUserUuid(params);
			String uuid = (String) params.get(MdekKeys.UUID);

			daoAddressNode.beginTransaction();
			daoAddressNode.disableAutoFlush();

			List<AddressNode> aNodes =
				daoAddressNode.getSubAddresses(uuid, IdcEntityVersion.WORKING_VERSION, true);
			
			ArrayList<IngridDocument> subAddrDocs = new ArrayList<IngridDocument>(aNodes.size());
			for (AddressNode aNode : aNodes) {
				IngridDocument adrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(aNode, adrDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), adrDoc, MappingQuantity.TREE_ENTITY);

				// add permissions the user has on given address !
				List<Permission> perms =
					permissionHandler.getPermissionsForAddress(aNode.getAddrUuid(), userUuid, true);
				beanToDocMapperSecurity.mapPermissionList(perms, adrDoc);				

				subAddrDocs.add(adrDoc);
			}

			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.ADR_ENTITIES, subAddrDocs);
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getAddressPath(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();
			daoAddressNode.disableAutoFlush();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<String> uuidList = daoAddressNode.getAddressPath(uuid);

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.PATH, uuidList);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

			if (log.isDebugEnabled()) {
				log.debug("Invoke getAddrDetails (uuid='"+uuid+"').");
			}
			IngridDocument result = addressService.getAddressDetails(uuid,
					whichEntityVersion, fetchQuantity, 
					objRefsStartIndex, objRefsMaxNum, userUuid);
			
			daoAddressNode.commitTransaction();

			return result;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

//			String userUuid = getCurrentUserUuid(params);
			String addrUuid = (String) params.get(MdekKeys.UUID);
			int objRefsStartIndex = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) params.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			if (log.isDebugEnabled()) {
				log.debug("Invoke getAddressObjectReferences (uuid='"+addrUuid+"').");
			}

			// get objects referencing the given address
			HashMap<?, ?> fromObjectsData = 
				daoAddressNode.getObjectReferencesFrom(addrUuid, objRefsStartIndex, objRefsMaxNum);
			@SuppressWarnings("unchecked")
            List<ObjectNode>[] fromLists = (List<ObjectNode>[]) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM);
			Integer objRefsTotalNum = (Integer) fromObjectsData.get(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM);

			// map the data to our result doc 
			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapper.mapObjectReferencesFrom(fromLists, objRefsStartIndex, objRefsTotalNum,
					IdcEntityType.ADDRESS, addrUuid, resultDoc, MappingQuantity.TABLE_ENTITY);

			daoAddressNode.commitTransaction();

			return resultDoc;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getInitialAddress(IngridDocument aDocIn) {
		String userUuid = getCurrentUserUuid(aDocIn);
		try {
			daoAddressNode.beginTransaction();
			daoAddressNode.disableAutoFlush();

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
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

			IngridDocument result =	daoAddressNode.getWorkAddresses(userUuid,
					selectionType, orderBy, orderAsc,
					startHit, numHits);

			@SuppressWarnings("unchecked")
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
				if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST_ALL_USERS) {
					// add permissions the user has on given address !
					List<Permission> perms =
						permissionHandler.getPermissionsForAddress(aN.getAddrUuid(), userUuid, true);
					beanToDocMapperSecurity.mapPermissionList(perms, addrDoc);				
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
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

			boolean isCatAdmin = permissionHandler.isCatalogAdmin(userUuid);

			IngridDocument result =
				daoAddressNode.getQAAddresses(userUuid, isCatAdmin, permissionHandler,
						whichWorkState, selectionType,
						orderBy, orderAsc,
						startHit, numHits);

			@SuppressWarnings("unchecked")
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
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

			IngridDocument result =
				daoAddressNode.getAddressStatistics(parentUuid, onlyFreeAddresses, selectionType, startHit, numHits);

			daoAddressNode.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			String uuid = addressService.storeWorkingCopy(aDocIn, userId);

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
			RuntimeException handledExc = handleException(e);
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

			String uuid = addressService.assignAddressToQA(aDocIn, userId);

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
			RuntimeException handledExc = handleException(e);
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
			String uuid = addressService.storeWorkingCopy(aDocIn, userId);

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
			RuntimeException handledExc = handleException(e);
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
			RuntimeException handledExc = handleException(e);
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

	/** Merge address data to sub addresses. */
	public IngridDocument mergeAddressToSubAddresses(IngridDocument params) {
		String userUuid = getCurrentUserUuid(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.STORE, 0, 0, false));

			daoAddressNode.beginTransaction();

			String parentUuid = (String) params.get(MdekKeys.UUID);

			// check permissions !
			permissionHandler.checkPermissionsForMergeAddressToSubAddresses(parentUuid, userUuid);

			// merge !
			IngridDocument mergeResult = mergeAddressNodeToSubNodes(parentUuid, userUuid);
			Integer numMergedAddresses = (Integer) mergeResult.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES);

			// success
			IngridDocument resultDoc = new IngridDocument();
			// additional info
			resultDoc.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numMergedAddresses);

			daoAddressNode.commitTransaction();
			return resultDoc;		
		
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}

	public IngridDocument publishAddress(IngridDocument aDocIn) {
		String userId = getCurrentUserUuid(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.PUBLISH, 0, 1, false));

			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);
			Boolean forcePubCondition = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);
			int objRefsStartIndex = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
			int objRefsMaxNum = (Integer) aDocIn.get(MdekKeys.OBJ_REFERENCES_FROM_MAX_NUM);

			daoAddressNode.beginTransaction();
			
			String uuid = addressService.publishAddress(aDocIn, forcePubCondition, userId);

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
			
            ElasticDocument doc = docProducer.getById( result.get( "id" ).toString(), "id" );
            if (doc != null && !doc.isEmpty()) {
                indexManager.addBasicFields( doc );
                indexManager.update( docProducer.getIndexInfo(), doc, true );
                indexManager.flush();
            }
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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

			// grant write tree permission (e.g. if new root node)
			if (toUuid == null) {
		        // NEW ROOT NODE: grant write-tree permission, no permission yet ! 
				permissionHandler.grantTreePermissionForAddress(fromNodeCopy.getAddrUuid(), userUuid,
						GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION, null);
			} else if (permissionHandler.hasSubNodePermissionForAddress(toUuid, userUuid, false)) {  
		        // NEW NODE UNDER PARENT WITH SUBNODE PERMISSION: grant write-tree permission only for special group ! 
				permissionHandler.grantTreePermissionForAddress(fromNodeCopy.getAddrUuid(), userUuid,
						GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS, toUuid);				
			}


			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForAddress(fromNodeCopy.getAddrUuid(), userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoAddressNode.commitTransaction();
			return resultDoc;		
		
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			Boolean forcePubCondition = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION);

			daoAddressNode.beginTransaction();

			IngridDocument resultDoc = addressService.moveAddress(fromUuid, toUuid,
					targetIsFreeAddress,
					forcePubCondition,
					userUuid);

			// add permissions to result
			List<Permission> perms = permissionHandler.getPermissionsForAddress(fromUuid, userUuid, true);
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			daoAddressNode.commitTransaction();

			return resultDoc;		

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

			IngridDocument checkResult = checkAddressTreeWorkingCopies(rootUuid);

			daoAddressNode.commitTransaction();
			return checkResult;		

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			
			IngridDocument result = addressService.deleteAddressWorkingCopy(uuid, forceDeleteReferences, userId);

			daoAddressNode.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			
			IngridDocument result = addressService.deleteAddressFull(uuid, forceDeleteReferences, userId);

			daoAddressNode.commitTransaction();
			
			// only remove from index if object was really removed and not just marked
            if (result.getBoolean( MdekKeys.RESULTINFO_WAS_FULLY_DELETED )) {
                indexManager.delete( docProducer.getIndexInfo(), uuid, true );
                indexManager.flush();
            }

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			daoAddressNode.disableAutoFlush();

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
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}
	
	/**
     * Generate the IDF of the requested document.
     * @param id is the ID of the document to be transformed
     * @returns the document as XML
     */
    public IngridDocument getIsoXml(IngridDocument doc) {
        String id = doc.getString( MdekKeys.UUID );
        IngridDocument resultDoc = new IngridDocument();
        final IngridHit hit = new IngridHit();
        hit.setDocumentId( id );
        Record record = null;
        try {
            daoAddressNode.beginTransaction();
            Long objId = daoAddressNode.loadByUuid( id, null ).getAddrId();
            ElasticDocument elasticDocument = new ElasticDocument();
            elasticDocument.put( "t02_address.id", objId );
            record = dscRecordProducer.getRecord( elasticDocument );
            
            //record = recordLoader.getRecord( hit );
            Document nodeDoc = MdekRecordUtils.convertRecordToDocument( record );
            String isoDocAsString = null;
            if (nodeDoc != null) {
                Node isoDoc = xsltUtils.transform(nodeDoc, MdekRecordUtils.XSL_IDF_TO_ISO_FULL);
                isoDocAsString = XMLUtils.toString( (Document) isoDoc );
            }
            resultDoc.put( "record", isoDocAsString );
        } catch (Exception e) {
            log.error( "Could not get record with ID: " + id, e );
        } finally {
            daoAddressNode.commitTransaction();
        }
        return resultDoc;
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
	@SuppressWarnings("unchecked")
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
			totalNumToCopy = 1 +
				daoAddressNode.countAllSubAddresses(sourceNode.getAddrUuid(), IdcEntityVersion.ALL_VERSIONS);
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
			String newUuid = EntityHelper.getInstance().generateUuid();
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
		
		// map source bean to doc, ONLY data not specific to entity !
		IngridDocument targetAddrDoc =
			beanToDocMapper.mapT02Address(sourceAddr, new IngridDocument(), MappingQuantity.COPY_DATA);
		
		// update changed data in doc from source for target !
		targetAddrDoc.put(MdekKeys.UUID, newUuid);
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		targetAddrDoc.put(MdekKeys.DATE_OF_CREATION, currentTime);
		targetAddrDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		// REMOVE Original ID to avoid duplicate ! see INGRID-2299 
		targetAddrDoc.remove(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER);
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

	/**
	 * Merge data of parent to its subnodes.
	 * Subnodes are stored as working version (if in work state) or published (if in published state and QA)
	 * or assigned to QA (if in published state and NOT QA) !
	 * @param mergeSourceUuid uuid of parent node to merge into subnodes
	 * @param userUuid current user address uuid needed to update running jobs
	 * @return doc containing additional info (number merged nodes)
	 */
	private IngridDocument mergeAddressNodeToSubNodes(String mergeSourceUuid, String userUuid)
	{
		// merge source exists ?
		AddressNode mergeSourceNode = addressService.loadByUuid(mergeSourceUuid, IdcEntityVersion.WORKING_VERSION);
		if (mergeSourceNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// refine running jobs info
		// total num to merge: sub addresses
		int totalNumMerged =
			daoAddressNode.countAllSubAddresses(mergeSourceUuid, IdcEntityVersion.ALL_VERSIONS);
		updateRunningJob(userUuid, createRunningJobDescription(JobType.STORE, 0, totalNumMerged, false));				

		// merge iteratively via stack to avoid recursive stack overflow
		Stack<AddressNode> stack = new Stack<AddressNode>();
		// push null value to indicate start !
		stack.push(null);

		int numberOfMergedAddr = 0;
		while (!stack.isEmpty()) {
			AddressNode mergeTargetNode = stack.pop();
			
			if (mergeTargetNode == null) {
				// start ! do not merge, but fetch children from top node !
				mergeTargetNode = mergeSourceNode;
			} else {
				// do merge !
				mergeT02Address(mergeSourceNode.getT02AddressWork(), mergeTargetNode, userUuid);				
				numberOfMergedAddr++;

				// update our job information ! may be polled from client !
				// NOTICE: also checks whether job was canceled !
				updateRunningJob(userUuid, createRunningJobDescription(
						JobType.STORE, numberOfMergedAddr, totalNumMerged, false));
			}

			// get subtree of merged address
			List<AddressNode> subNodes = daoAddressNode.getSubAddresses(
					mergeTargetNode.getAddrUuid(),
					IdcEntityVersion.WORKING_VERSION, false);
			for (AddressNode subNode : subNodes) {					
				// add to stack, will be merged into
				stack.push(subNode);
			}
		}
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfMergedAddr);
		if (log.isDebugEnabled()) {
			log.debug("Number of merged addresses: " + numberOfMergedAddr);
		}

		return result;
	}

	/**
	 * Merges data of given sourceAddr into given target address node. Already Persisted !
	 * State of target node determines whether address is stored as working version or published (or assigned to QA if no QA) !
	 */
	private void mergeT02Address(T02Address sourceAddr, AddressNode targetAddrNode, String userUuid) {
		// execute merge
		if (log.isDebugEnabled()) {
			log.debug("Merging address '" + sourceAddr.getAdrUuid() + "' to address '" + targetAddrNode.getAddrUuid() + "'");
		}
		
		// first map working instance (always set, equals published instance if no working version !)
		IngridDocument targetAddrDoc =
			beanToDocMapper.mapT02Address(targetAddrNode.getT02AddressWork(), new IngridDocument(), MappingQuantity.COPY_DATA);
		
		// transfer changes (merge) !
		beanToDocMapper.mergeT02Address(sourceAddr, targetAddrDoc);

		// determine whether to store or to publish
		if (addressService.hasWorkingCopy(targetAddrNode)) {
			if (log.isDebugEnabled()) {
				log.debug("Node has working copy -> STORE merged address '" + targetAddrNode.getAddrUuid() + "' as working copy.");
			}
			addressService.storeWorkingCopy(targetAddrDoc, userUuid);
		} else {
			// do we have permission to publish (e.g. no permission if workflow enabled and not QA) ? Execute in catch block !
			try {
				// publish
				addressService.publishAddress(targetAddrDoc, false, userUuid);
				if (log.isDebugEnabled()) {
					log.debug("Node has NO working copy and \"QA\" valid -> PUBLISHed merged address '" + targetAddrNode.getAddrUuid() + "'.");
				}
			} catch (MdekException ex) {
				if (ex.getMdekError().getErrorType() == MdekErrorType.USER_HAS_NO_WORKFLOW_PERMISSION_ON_ENTITY) {
					// no right to publish, ASSIGN TO QA
					addressService.assignAddressToQA(targetAddrDoc, userUuid);
					if (log.isDebugEnabled()) {
						log.debug("Node has NO working copy and \"QA\" NOT valid -> ASSIGNed merged address '" + targetAddrNode.getAddrUuid() + "' TO QA.");
					}
				} else {
					throw ex;
				}
			}
		}
	}
}
