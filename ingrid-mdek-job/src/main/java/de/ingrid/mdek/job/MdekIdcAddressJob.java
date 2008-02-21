package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.UuidGenerator;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.IMapper.MappingQuantity;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning ADDRESSES. 
 */
public class MdekIdcAddressJob extends MdekIdcJob {

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;

	public MdekIdcAddressJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcAddressJob.class), daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();
	}

	public IngridDocument getTopAddresses(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

			Boolean onlyFreeAddressesIn = (Boolean) params.get(MdekKeys.REQUESTINFO_ONLY_FREE_ADDRESSES);
			boolean onlyFreeAddresses = (onlyFreeAddressesIn == null) ? false : onlyFreeAddressesIn;

			// fetch top Objects
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
			throw new MdekException(MdekError.UUID_NOT_FOUND);
		}

		IngridDocument resultDoc = new IngridDocument();
		beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), resultDoc, MappingQuantity.DETAIL_ENTITY);
		
		// also map AddressNode for published info
		beanToDocMapper.mapAddressNode(aNode, resultDoc, MappingQuantity.DETAIL_ENTITY);

		// then get "external" data (objects referencing the given address ...)
		List<ObjectNode> oNs = daoAddressNode.getObjectReferencesFrom(uuid);
		beanToDocMapper.mapObjectReferencesFrom(oNs, uuid, resultDoc, MappingQuantity.TABLE_ENTITY);

		// get parent data
		AddressNode pNode = daoAddressNode.getParent(uuid);
		if (pNode != null) {
			beanToDocMapper.mapAddressParentData(pNode.getT02AddressWork(), resultDoc);
		}

		// supply path info
		List<String> pathList = daoAddressNode.getAddressPathOrganisation(uuid, false);
		resultDoc.put(MdekKeys.PATH, pathList);

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
					throw new MdekException(MdekError.UUID_NOT_FOUND);
				}

				T02Address aParent = pNode.getT02AddressWork();

				// supply separate parent info
				beanToDocMapper.mapAddressParentData(aParent, aDocIn);
				
				// take over initial data from parent
				beanToDocMapper.mapT02Address(aParent, aDocIn, MappingQuantity.INITIAL_ENTITY);

				// supply path info
				List<String> pathList = daoAddressNode.getAddressPathOrganisation(parentUuid, true);
				aDocIn.put(MdekKeys.PATH, pathList);
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
			if (!hasWorkingCopy(aNode)) {
				// no working copy yet, create new address with BASIC data

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
				
				// update node
				aNode.setAddrId(aWork.getId());
				aNode.setT02AddressWork(aWork);
				daoAddressNode.makePersistent(aNode);
			}

			// transfer new data
			T02Address aWork = aNode.getT02AddressWork();
			docToBeanMapper.mapT02Address(aDocIn, aWork, MappingQuantity.DETAIL_ENTITY);

			// PERFORM CHECKS
			// check: "free address" has to be root node
			checkFreeAddress(aWork, aNode.getFkAddrUuid());

			// store when ok
			daoT02Address.makePersistent(aWork);

			// return uuid (may be new generated uuid if new address)
			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.UUID, uuid);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data !
			daoAddressNode.commitTransaction();

			if (refetchAfterStore) {
				daoAddressNode.beginTransaction();
				result = getAddrDetails(uuid);
				daoAddressNode.commitTransaction();
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
			Boolean copyToFreeAddress = (Boolean) params.get(MdekKeys.REQUESTINFO_COPY_TO_FREE_ADDRESS);

			// copy fromNode
			IngridDocument copyResult = createAddressNodeCopy(fromUuid, toUuid,
					copySubtree, copyToFreeAddress, userId);
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
			List<String> pathList = daoAddressNode.getAddressPathOrganisation(fromNodeCopy.getAddrUuid(), false);
			resultDoc.put(MdekKeys.PATH, pathList);

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

			// NOTICE: this one also contains Parent Association !
			AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
			if (aNode == null) {
				throw new MdekException(MdekError.UUID_NOT_FOUND);
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
				result = deleteAddress(uuid);
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

	private IngridDocument deleteAddress(String uuid) {
		// NOTICE: this one also contains Parent Association !
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			throw new MdekException(MdekError.UUID_NOT_FOUND);
		}

		// delete complete Node ! rest is deleted per cascade !
		daoAddressNode.makeTransient(aNode);

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);

		return result;
	}

	/**
	 * Creates a copy of the given AddressNode and adds it under the given parent.
	 * Also copies whole subtree dependent from passed flag.
	 * NOTICE: also supports copy of a tree to one of its subnodes !
	 * Copied nodes are already Persisted !!!
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
				throw new MdekException(MdekError.TO_UUID_NOT_FOUND);
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
			boolean isFreeAddress = AddressType.FREI.getDbValue().equals(targetAddrWork.getAdrType());
			if (copyToFreeAddress) {
				if (!isFreeAddress) {
					targetAddrWork.setAdrType(AddressType.FREI.getDbValue());
					targetAddrWork.setInstitution("");
				}
			} else {
				if (isFreeAddress) {
					targetAddrWork.setAdrType(AddressType.PERSON.getDbValue());
					targetAddrWork.setInstitution("");
				}				
			}

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

	/** Check whether passed nodes are valid for copy operation
	 * (e.g. check free address conditions ...). Throws MdekException if not valid.
	 */
	private void checkAddressNodesForCopy(AddressNode fromNode, AddressNode toNode,
		Boolean copySubtree,
		Boolean copyToFreeAddress)
	{
		if (fromNode == null) {
			throw new MdekException(MdekError.FROM_UUID_NOT_FOUND);
		}

		// rudimentary checks
		if (copyToFreeAddress) {
			if (toNode != null) {
				throw new MdekException(MdekError.FREE_ADDRESS_WITH_PARENT);
			}
			if (copySubtree) {
				throw new MdekException(MdekError.FREE_ADDRESS_WITH_SUBTREE);
			}
		}
	}

	/** Checks whether address is "free address" and valid (free addresses have NO parent). */
	private void checkFreeAddress(T02Address a, String parentUuid) {
		AddressType aType = EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.getAdrType());
		if (aType == AddressType.FREI) {
			if (parentUuid != null) {
				throw new MdekException(MdekError.FREE_ADDRESS_WITH_PARENT);
			}
		}
	}
}
