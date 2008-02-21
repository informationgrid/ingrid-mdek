package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		
		// also map ObjectNode for published info
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

	private boolean hasWorkingCopy(AddressNode node) {
		Long workId = node.getAddrId(); 
		Long pubId = node.getAddrIdPublished(); 
		if (workId == null || workId.equals(pubId)) {
			return false;
		}
		
		return true;
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
