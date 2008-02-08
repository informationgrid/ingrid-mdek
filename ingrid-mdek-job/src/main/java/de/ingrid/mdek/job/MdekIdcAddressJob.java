package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekException;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.IMdekErrors.MdekError;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.IMapper.MappingQuantity;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning ADDRESSES. 
 */
public class MdekIdcAddressJob extends MdekIdcJob {

	private IAddressNodeDao daoAddressNode;

	public MdekIdcAddressJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcAddressJob.class), daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
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

	public IngridDocument getAdrDetails(IngridDocument params) {
		try {
			daoAddressNode.beginTransaction();

			String uuid = (String) params.get(MdekKeys.UUID);
			if (log.isDebugEnabled()) {
				log.debug("Invoke getAdrDetails (uuid='"+uuid+"').");
			}
			IngridDocument result = getAdrDetails(uuid);
			
			daoAddressNode.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			daoAddressNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	private IngridDocument getAdrDetails(String uuid) {
		// first get all "internal" address data
		AddressNode aNode = daoAddressNode.getAdrDetails(uuid);
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
/*
		// get parent data
		ObjectNode pNode = daoObjectNode.getParent(uuid);
		if (pNode != null) {
			beanToDocMapper.mapParentData(pNode.getT01ObjectWork(), resultDoc);
		}
*/
		return resultDoc;
	}
}
