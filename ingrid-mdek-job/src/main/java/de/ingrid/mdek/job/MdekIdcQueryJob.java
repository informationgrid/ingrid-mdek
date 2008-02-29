package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning QUERYING (inquiry of entities). 
 */
public class MdekIdcQueryJob extends MdekIdcJob {

	private IAddressNodeDao daoAddressNode;
	private IObjectNodeDao daoObjectNode;

	public MdekIdcQueryJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcQueryJob.class), daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoObjectNode = daoFactory.getObjectNodeDao();
	}

	public IngridDocument queryAddressesThesaurusTerm(IngridDocument params) {
		try {
			Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
			IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_PARAMS);
			String termSnsId = searchParams.getString(MdekKeys.TERM_SNS_ID);

			daoAddressNode.beginTransaction();

			long totalNumHits = daoAddressNode.queryAddressesThesaurusTermTotalNum(termSnsId);

			List<AddressNode> hits = new ArrayList<AddressNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoAddressNode.queryAddressesThesaurusTerm(termSnsId, startHit, numHits);
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

	public IngridDocument queryObjectsThesaurusTerm(IngridDocument params) {
		try {
			Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
			IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_PARAMS);
			String termSnsId = searchParams.getString(MdekKeys.TERM_SNS_ID);

			daoObjectNode.beginTransaction();

			long totalNumHits = daoObjectNode.queryObjectsThesaurusTermTotalNum(termSnsId);

			List<ObjectNode> hits = new ArrayList<ObjectNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoObjectNode.queryObjectsThesaurusTerm(termSnsId, startHit, numHits);
			}

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(hits.size());
			for (ObjectNode hit : hits) {
				IngridDocument objDoc = new IngridDocument();
				beanToDocMapper.mapObjectNode(hit, objDoc, MappingQuantity.BASIC_ENTITY);
				beanToDocMapper.mapT01Object(hit.getT01ObjectWork(), objDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(objDoc);
			}

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.SEARCH_TOTAL_NUM_HITS, totalNumHits);
			result.put(MdekKeys.OBJ_ENTITIES, resultList);
			result.put(MdekKeys.SEARCH_NUM_HITS, resultList.size());

			return result;

		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}
}
