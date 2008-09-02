package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
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
	private IHQLDao daoHQL;

	public MdekIdcQueryJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcQueryJob.class), daoFactory);

		daoAddressNode = daoFactory.getAddressNodeDao();
		daoObjectNode = daoFactory.getObjectNodeDao();
		daoHQL = daoFactory.getHQLDao();
	}

	public IngridDocument queryAddressesFullText(IngridDocument params) {
		try {
			Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
			IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_PARAMS);
			String searchTerm = searchParams.getString(MdekKeys.SEARCH_TERM);

			daoAddressNode.beginTransaction();

			long totalNumHits = daoAddressNode.queryAddressesFullTextTotalNum(searchTerm);

			List<AddressNode> hits = new ArrayList<AddressNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoAddressNode.queryAddressesFullText(searchTerm, startHit, numHits);
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

	public IngridDocument queryObjectsFullText(IngridDocument params) {
		try {
			Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
			IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_PARAMS);
			String searchTerm = searchParams.getString(MdekKeys.SEARCH_TERM);

			daoObjectNode.beginTransaction();

			long totalNumHits = daoObjectNode.queryObjectsFullTextTotalNum(searchTerm);

			List<ObjectNode> hits = new ArrayList<ObjectNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoObjectNode.queryObjectsFullText(searchTerm, startHit, numHits);
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

	public IngridDocument queryHQL(IngridDocument params) {
		try {
			Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
			String hqlQuery = params.getString(MdekKeys.HQL_QUERY);

			daoHQL.beginTransaction();

			long totalNumHits = daoHQL.queryHQLTotalNum(hqlQuery);

			IngridDocument queryDoc = new IngridDocument();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				queryDoc = daoHQL.queryHQL(hqlQuery, startHit, numHits);
			}
			
			// default is object beans
			List hits = new ArrayList(0);
			IdcEntityType entityType = null;
			if (queryDoc.get(MdekKeys.OBJ_ENTITIES) != null) {
				// do we have object entities ?
				hits = (List) queryDoc.get(MdekKeys.OBJ_ENTITIES);
				entityType = IdcEntityType.OBJECT;
			} else if (queryDoc.get(MdekKeys.ADR_ENTITIES) != null) {
				// or do we have address entities ?
				hits = (List) queryDoc.get(MdekKeys.ADR_ENTITIES);
				entityType = IdcEntityType.ADDRESS;				
			}

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(hits.size());
			for (Object hit : hits) {
				IngridDocument hitDoc = new IngridDocument();
				if (entityType == IdcEntityType.OBJECT) {
					ObjectNode oNode = (ObjectNode) hit;
					beanToDocMapper.mapObjectNode(oNode, hitDoc, MappingQuantity.BASIC_ENTITY);
					beanToDocMapper.mapT01Object(oNode.getT01ObjectWork(), hitDoc, MappingQuantity.BASIC_ENTITY);					
				} else {
					AddressNode aNode = (AddressNode) hit;
					beanToDocMapper.mapAddressNode(aNode, hitDoc, MappingQuantity.BASIC_ENTITY);
					beanToDocMapper.mapT02Address(aNode.getT02AddressWork(), hitDoc, MappingQuantity.BASIC_ENTITY);
				}
				resultList.add(hitDoc);
			}

			daoHQL.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.SEARCH_TOTAL_NUM_HITS, totalNumHits);
			if (entityType == IdcEntityType.OBJECT) {
				result.put(MdekKeys.OBJ_ENTITIES, resultList);
			} else {
				result.put(MdekKeys.ADR_ENTITIES, resultList);
			}
			result.put(MdekKeys.SEARCH_NUM_HITS, resultList.size());

			return result;

		} catch (RuntimeException e) {
			daoHQL.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument queryHQLToCsv(IngridDocument params) {
		try {
			String hqlQuery = params.getString(MdekKeys.HQL_QUERY);

			daoHQL.beginTransaction();

			IngridDocument result = daoHQL.queryHQLToCsv(hqlQuery);

			daoHQL.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoHQL.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument queryHQLToMap(IngridDocument params) {
		try {
			String hqlQuery = params.getString(MdekKeys.HQL_QUERY);
			Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);

			daoHQL.beginTransaction();

			IngridDocument result = daoHQL.queryHQLToMap(hqlQuery, numHits);

			daoHQL.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoHQL.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument queryObjectsExtended(IngridDocument params) {
		Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
		Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
		IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_EXT_PARAMS);
		
		// execute the query
		try {
			daoObjectNode.beginTransaction();
			
			long totalNumHits = daoObjectNode.queryObjectsExtendedTotalNum(searchParams);

			List<ObjectNode> hits = new ArrayList<ObjectNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoObjectNode.queryObjectsExtended(searchParams, startHit, numHits);
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

	public IngridDocument queryAddressesExtended(IngridDocument params) {
		Integer startHit = (Integer) params.get(MdekKeys.SEARCH_START_HIT);
		Integer numHits = (Integer) params.get(MdekKeys.SEARCH_NUM_HITS);
		IngridDocument searchParams = (IngridDocument) params.get(MdekKeys.SEARCH_EXT_PARAMS);
		
		// execute the query
		try {
			daoObjectNode.beginTransaction();
			
			long totalNumHits = daoAddressNode.queryAddressesExtendedTotalNum(searchParams);

			List<AddressNode> hits = new ArrayList<AddressNode>();
			if (totalNumHits > 0 &&	startHit < totalNumHits) {
				hits = daoAddressNode.queryAddressesExtended(searchParams, startHit, numHits);
			}

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(hits.size());
			for (AddressNode hit : hits) {
				IngridDocument addrDoc = new IngridDocument();
				beanToDocMapper.mapAddressNode(hit, addrDoc, MappingQuantity.BASIC_ENTITY);
				beanToDocMapper.mapT02Address(hit.getT02AddressWork(), addrDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(addrDoc);
			}

			daoObjectNode.commitTransaction();

			IngridDocument result = new IngridDocument();
			result.put(MdekKeys.SEARCH_TOTAL_NUM_HITS, totalNumHits);
			result.put(MdekKeys.ADR_ENTITIES, resultList);
			result.put(MdekKeys.SEARCH_NUM_HITS, resultList.size());
			
			return result;
			
		} catch (RuntimeException e) {
			daoObjectNode.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}	
	
}
