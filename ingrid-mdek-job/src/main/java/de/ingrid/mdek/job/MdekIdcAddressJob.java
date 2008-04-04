package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.tools.MdekFullIndexHandler;
import de.ingrid.mdek.job.tools.MdekIdcEntityComparer;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.UuidGenerator;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning ADDRESSES. 
 */
public class MdekIdcAddressJob extends MdekIdcJob {

	protected MdekFullIndexHandler fullIndexHandler;

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;
	private IGenericDao<IEntity> daoT012ObjAdr;
	private IT01ObjectDao daoT01Object;

	public MdekIdcAddressJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcAddressJob.class), daoFactory);

		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();
		daoT012ObjAdr = daoFactory.getDao(T012ObjAdr.class);
		daoT01Object = daoFactory.getT01ObjectDao();
	}

	public IngridDocument getTopAddresses(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

			Boolean onlyFreeAddressesIn = (Boolean) params.get(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES);
			boolean onlyFreeAddresses = (onlyFreeAddressesIn == null) ? false : onlyFreeAddressesIn;

			// fetch top Addresses
			List<AddressNode> aNs = daoAddressNode.getTopAddresses(onlyFreeAddresses);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(aNs.size());
			for (AddressNode aN : aNs) {
				IngridDocument adrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(aN, adrDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT02Address(aN.getT02AddressWork(), adrDoc, MappingQuantity.BASIC_ENTITY);
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
			daoAddressNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			List<AddressNode> aNodes = daoAddressNode.getSubAddresses(uuid, true);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(aNodes.size());
			for (AddressNode aNode : aNodes) {
				IngridDocument adrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(aNode, adrDoc, MappingQuantity.TREE_ENTITY);
				beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), adrDoc, MappingQuantity.BASIC_ENTITY);
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
			daoAddressNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			if (log.isDebugEnabled()) {
				log.debug("Invoke getAddrDetails (uuid='"+uuid+"').");
			}
			IngridDocument result = getAddrDetails(uuid);
			
			daoAddressNode.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	private IngridDocument getAddrDetails(String uuid) {
		// first get all "internal" address data
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		IngridDocument resultDoc = new IngridDocument();
		beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);
		
		// also map AddressNode for published info
		beanToDocMapper.mapAddressNode(aNode, resultDoc, MappingQuantity.DETAIL_ENTITY);

		// then get "external" data (objects referencing the given address ...)
		List<ObjectNode>[] fromLists = daoAddressNode.getObjectReferencesFrom(uuid);
		beanToDocMapper.mapObjectReferencesFrom(fromLists, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);

		// get parent data
		AddressNode pNode = daoAddressNode.getParent(uuid);
		if (pNode != null) {
			beanToDocMapper.mapAddressParentData(pNode.getT02AddressWork(), resultDoc);
		}

		// supply path info
		List<IngridDocument> pathList = daoAddressNode.getAddressPathOrganisation(uuid, false);
		resultDoc.put(MdekKeys.PATH_ORGANISATIONS, pathList);

		return resultDoc;
	}

	public IngridDocument getInitialAddress(IngridDocument aDocIn) {
		try {
			daoAddressNode.beginTransaction();
			
			// take over data from parent (if set)
			String parentUuid = aDocIn.getString(MdekKeys.PARENT_UUID);
			if (parentUuid != null) {
				AddressNode pNode = daoAddressNode.loadByUuid(parentUuid);
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

			daoAddressNode.commitTransaction();
			return aDocIn;

		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument storeAddress(IngridDocument aDocIn) {
		String userId = getCurrentUserId(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoAddressNode.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			String uuid = (String) aDocIn.get(MdekKeys.UUID);
			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer to working copy !
			aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			aDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
			
			if (uuid == null) {
				// NEW Address !

				// create new uuid
				uuid = UuidGenerator.getInstance().generateUuid();
				aDocIn.put(MdekKeys.UUID, uuid);
			}
			
			// load node
			AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
			if (aNode == null) {
				aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
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
			// TODO: hier AddressNode uebergeben ? IndexHandler kann dann entscheiden welche Daten in den Index geschrieben werden !?
			fullIndexHandler.updateAddressIndex(aWork);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			// return uuid (may be new generated uuid if new address)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid);
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

	public IngridDocument publishAddress(IngridDocument aDocIn) {
		String userId = getCurrentUserId(aDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_PUBLISH, 0, 1, false));

			daoAddressNode.beginTransaction();

			// uuid may be null when new address !
			String uuid = (String) aDocIn.get(MdekKeys.UUID);
			// NOTICE: parent in doc only set if NEW node !
			String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);
			Integer childType = (Integer) aDocIn.get(MdekKeys.CLASS);
			// additional info
			Boolean refetchAfterStore = (Boolean) aDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer
			String currentTime = MdekUtils.dateToTimestamp(new Date()); 
			aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			aDocIn.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());

			if (uuid == null) {
				// NEW NODE !!!!
				// create new uuid
				uuid = UuidGenerator.getInstance().generateUuid();
				aDocIn.put(MdekKeys.UUID, uuid);
			}

			// load node
			AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
			if (aNode == null) {
				aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			}
			
			// get/create published version
			T02Address aPub = aNode.getT02AddressPublished();
			if (aPub == null) {
				// set some missing data which may not be passed from client.
				aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
				
				// create new address with BASIC data
				aPub = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
				 // save it to generate id needed for mapping
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

			// PERFORM CHECKS BEFORE COMMITTING !!!
			checkAddressNodeForPublish(aNode);			
			// checks ok !

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoAddressNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid);
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
		String userId = getCurrentUserId(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_COPY, 0, 1, false));

			daoAddressNode.beginTransaction();

			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);
			Boolean copySubtree = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_SUBTREE);
			Boolean targetIsFreeAddress = (Boolean) params.get(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS);

			// copy fromNode
			IngridDocument copyResult = createAddressNodeCopy(fromUuid, toUuid,
					copySubtree, targetIsFreeAddress, userId);
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

			daoAddressNode.commitTransaction();
			return resultDoc;		
		
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

	/** Move Address with its subtree to new parent. */
	public IngridDocument moveAddress(IngridDocument params) {
		String userId = getCurrentUserId(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_MOVE, 0, 1, false));

			Boolean targetIsFreeAddress = (Boolean) params.get(MdekKeys.REQUESTINFO_TARGET_IS_FREE_ADDRESS);
			String fromUuid = (String) params.get(MdekKeys.FROM_UUID);
			String toUuid = (String) params.get(MdekKeys.TO_UUID);

			daoAddressNode.beginTransaction();

			// PERFORM CHECKS

			AddressNode fromNode = daoAddressNode.loadByUuid(fromUuid);
			checkAddressNodesForMove(fromNode, toUuid, targetIsFreeAddress);

			// CHECKS OK, proceed

			// set new parent, may be null, then top node !
			fromNode.setFkAddrUuid(toUuid);		
			daoAddressNode.makePersistent(fromNode);

			// change date and mod_uuid of all moved nodes !
			IngridDocument result = processMovedNodes(fromNode, targetIsFreeAddress, userId);

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

	/** Checks whether subtree of address has working copies. */
	public IngridDocument checkAddressSubTree(IngridDocument params) {
		String userId = getCurrentUserId(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_CHECK, 0, 1, false));

			String rootUuid = (String) params.get(MdekKeys.UUID);

			daoAddressNode.beginTransaction();

			IngridDocument checkResult = checkAddressSubTreeWorkingCopies(rootUuid);

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

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the address is deleted completely, meaning non existent afterwards
	 * (including all sub addresses !)
	 */
	public IngridDocument deleteAddressWorkingCopy(IngridDocument params) {
		String userId = getCurrentUserId(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoAddressNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			// NOTICE: this one also contains Parent Association !
			AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
			if (aNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}

			boolean performFullDelete = false;
			Long idPublished = aNode.getAddrIdPublished();
			Long idWorkingCopy = aNode.getAddrId();

			// if we have NO published version -> delete complete node !
			IngridDocument result = new IngridDocument();
			if (idPublished == null) {
				performFullDelete = true;
			} else {
				result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);			

				// perform delete of working copy only if really different version
				if (!idPublished.equals(idWorkingCopy)) {
					// remove already fetched working copy from node 
					T02Address aWorkingCopy = aNode.getT02AddressWork();
					// and delete it
					daoT02Address.makeTransient(aWorkingCopy);
					
					// and set published one as working copy
					aNode.setAddrId(idPublished);
					aNode.setT02AddressWork(aNode.getT02AddressPublished());
					daoAddressNode.makePersistent(aNode);
				}
			}

			if (performFullDelete) {
				result = deleteAddress(uuid, forceDeleteReferences);
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

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subaddresses !
	 * Address is non existent afterwards !
	 */
	public IngridDocument deleteAddress(IngridDocument params) {
		String userId = getCurrentUserId(params);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoAddressNode.beginTransaction();
			String uuid = (String) params.get(MdekKeys.UUID);
			Boolean forceDeleteReferences = (Boolean) params.get(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES);

			IngridDocument result = deleteAddress(uuid, forceDeleteReferences);

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

	private IngridDocument deleteAddress(String uuid, boolean forceDeleteReferences) {
		// NOTICE: this one also contains Parent Association !
		AddressNode aNode = daoAddressNode.loadByUuid(uuid);
		if (aNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		checkAddressSubTreeReferences(aNode, forceDeleteReferences);

		// delete complete Node ! rest is deleted per cascade !
		daoAddressNode.makeTransient(aNode);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);

		return result;
	}

	public IngridDocument searchAddresses(IngridDocument params) {
		try {
			Integer inStartHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer inNumHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
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
			result.put(MdekKeys.SEARCH_TOTAL_NUM_HITS, totalNumHits);
			result.put(MdekKeys.ADR_ENTITIES, resultList);
			result.put(MdekKeys.SEARCH_NUM_HITS, resultList.size());

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
	 * @param userId current user id needed to update running jobs
	 * @return doc containing additional info (copy of source node, number copied nodes ...)
	 */
	private IngridDocument createAddressNodeCopy(String sourceUuid, String newParentUuid,
			boolean copySubtree, boolean copyToFreeAddress, String userId)
	{
		// PERFORM CHECKS
		
		// parent node exists ?
		// NOTICE: copy to top when newParentUuid is null
		AddressNode newParentNode = null;
		if (newParentUuid != null) {
			newParentNode = daoAddressNode.loadByUuid(newParentUuid);
			if (newParentNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.TO_UUID_NOT_FOUND));
			}
		}
		// further checks
		AddressNode sourceNode = daoAddressNode.loadByUuid(sourceUuid);
		checkAddressNodesForCopy(sourceNode, newParentNode, copySubtree, copyToFreeAddress);

		// refine running jobs info
		int totalNumToCopy = 1;
		if (copySubtree) {
			totalNumToCopy = daoAddressNode.countSubAddresses(sourceNode.getAddrUuid());
			updateRunningJob(userId, createRunningJobDescription(JOB_DESCR_COPY, 0, totalNumToCopy, false));				
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
			T02Address targetAddrWork = createT02AddressCopy(sourceNode.getT02AddressWork(), newUuid);

			// Process copy
			
			// in bearbeitung !
			targetAddrWork.setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());

			// handle copies from/to "free address"
			processMovedOrCopiedAddress(targetAddrWork, copyToFreeAddress);

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
			daoAddressNode.makePersistent(targetNode);
			numberOfCopiedAddr++;
			// update our job information ! may be polled from client !
			// NOTICE: also checks whether job was canceled !
			updateRunningJob(userId, createRunningJobDescription(
				JOB_DESCR_COPY, numberOfCopiedAddr, totalNumToCopy, false));

			if (nodeCopy == null) {
				nodeCopy = targetNode;
			}

			if (isCopyToOwnSubnode) {
				uuidsCopiedNodes.add(newUuid);
			}
			
			// add child bean to parent bean, so we can determine child info when mapping (without reloading)
			if (newParentNode != null) {
				newParentNode.getAddressNodeChildren().add(targetNode);
			}

			// copy subtree ? only if not already a copied node !
			if (copySubtree) {
				List<AddressNode> sourceSubNodes = daoAddressNode.getSubAddresses(sourceNode.getAddrUuid(), true);
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
	private T02Address createT02AddressCopy(T02Address sourceAddr, String newUuid) {
		// create new address with new uuid and save it (to generate id !)
		T02Address targetAddr = new T02Address();
		targetAddr.setAdrUuid(newUuid);
		daoT02Address.makePersistent(targetAddr);

		// then copy content via mappers
		
		// map source bean to doc
		IngridDocument sourceAddrDoc =
			beanToDocMapper.mapT02Address(sourceAddr, new IngridDocument(), MappingQuantity.COPY_ENTITY);
		
		// update new data in doc !
		sourceAddrDoc.put(MdekKeys.UUID, newUuid);

		// and transfer data from doc to new bean
		docToBeanMapper.mapT02Address(sourceAddrDoc, targetAddr, MappingQuantity.COPY_ENTITY);

		daoT02Address.makePersistent(targetAddr);

		return targetAddr;
	}

	private boolean hasWorkingCopy(AddressNode node) {
		Long workId = node.getAddrId(); 
		Long pubId = node.getAddrIdPublished(); 
		if (workId == null || workId.equals(pubId)) {
			return false;
		}
		
		return true;
	}

	private boolean hasChildren(AddressNode node) {
    	return (node.getAddressNodeChildren().size() > 0) ? true : false;
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
				AddressNode pathNode = daoAddressNode.loadByUuid(pathUuid);
				if (pathNode == null) {
					throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
				}
				if (pathNode.getAddrIdPublished() == null) {
					throw new MdekException(new MdekError(MdekErrorType.PARENT_NOT_PUBLISHED));
				}
			}

			parentNode = daoAddressNode.loadByUuid(parentUuid);
			parentType = EnumUtil.mapDatabaseToEnumConst(AddressType.class,
					parentNode.getT02AddressPublished().getAdrType());

		}

		// check address type conflicts !
		checkAddressTypes(parentType, nodeType, isFreeAddress, true);
	}

	/**
	 * Checks whether address node is referenced by other objects.
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
		checkAddressTypes(toType, fromType, copyToFreeAddress, false);
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
			AddressNode toNode = daoAddressNode.loadByUuid(toUuid);
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

	/** Checks whether subtree of address has working copies. */
	private IngridDocument checkAddressSubTreeWorkingCopies(String rootUuid) {
		// load "root"
		AddressNode rootNode = daoAddressNode.loadByUuid(rootUuid);
		if (rootNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		// traverse iteratively via stack
		Stack<AddressNode> stack = new Stack<AddressNode>();
		stack.push(rootNode);

		boolean hasWorkingCopy = false;
		String uuidOfWorkingCopy = null;
		int numberOfCheckedAddr = 0;
		while (!hasWorkingCopy && !stack.isEmpty()) {
			AddressNode node = stack.pop();
			
			// check
			numberOfCheckedAddr++;
			if (!node.getAddrId().equals(node.getAddrIdPublished())) {
				hasWorkingCopy = true;
				uuidOfWorkingCopy = node.getAddrUuid();
			}

			if (!hasWorkingCopy) {
				List<AddressNode> subNodes =
					daoAddressNode.getSubAddresses(node.getAddrUuid(), false);
				for (AddressNode subNode : subNodes) {
					stack.push(subNode);
				}					
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
	 * Checks whether address tree contains nodes referenced by other objects.
	 * @param topNode top node of tree to check (included in check !)
	 * @param forceDeleteReferences<br>
	 * 		true=delete all references found, no exception<br>
	 * 		false=don't delete references, throw exception
	 */
	private void checkAddressSubTreeReferences(AddressNode topNode, boolean forceDeleteReferences) {
		// traverse iteratively via stack
		Stack<AddressNode> stack = new Stack<AddressNode>();
		stack.push(topNode);

		while (!stack.isEmpty()) {
			AddressNode node = stack.pop();
			
			// check
			checkAddressNodeReferences(node, forceDeleteReferences);

			List<AddressNode> subNodes =
				daoAddressNode.getSubAddresses(node.getAddrUuid(), false);
			for (AddressNode subNode : subNodes) {
				stack.push(subNode);
			}					
		}
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
	private void checkAddressTypes(AddressType parentType, AddressType childType,
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

	/**
	 * Process the moved tree, meaning set modification date and user in every node (in
	 * published and working version !).
	 * @param rootNode root node of moved tree
	 * @param modUuid user uuid to set as modification user
	 * @return doc containing additional info (number processed nodes ...)
	 */
	private IngridDocument processMovedNodes(AddressNode rootNode,
			boolean isNowFreeAddress,
			String modUuid)
	{
		String currentTime = MdekUtils.dateToTimestamp(new Date()); 

		// process iteratively via stack to avoid recursive stack overflow
		Stack<AddressNode> stack = new Stack<AddressNode>();
		stack.push(rootNode);

		// NOTICE: when free address node there should be NO children ! (check was performed before!)
		int numberOfProcessedNodes = 0;
		while (!stack.isEmpty()) {
			AddressNode node = stack.pop();
			
			// set modification time and user (in both versions when present)
			T02Address addrWork = node.getT02AddressWork();
			T02Address addrPub = node.getT02AddressPublished();
			
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
			numberOfProcessedNodes++;

			List<AddressNode> subNodes = daoAddressNode.getSubAddresses(node.getAddrUuid(), true);
			for (AddressNode subNode : subNodes) {
				// add to stack, will be processed
				stack.push(subNode);
			}
		}
		
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES, numberOfProcessedNodes);
		if (log.isDebugEnabled()) {
			log.debug("Number of processed addresses: " + numberOfProcessedNodes);
		}

		return result;
	}

	/**
	 * Change the passed address concerning type, institution after copied/moved to new location.<br>
	 * NOTICE: changes working and published version !
	 * @param a the address as it was moved/copied
	 * @param isNowFreeAddress new location is free address
	 */
	private void processMovedOrCopiedAddress(T02Address a, boolean isNowFreeAddress) {
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
}
