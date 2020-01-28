/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.TransactionService;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
import de.ingrid.mdek.services.utils.csv.ExcelCSVPrinter;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public class HQLDaoHibernate
	extends TransactionService
	implements IHQLDao {

	private static final Logger LOG = LogManager.getLogger(HQLDaoHibernate.class);

	/** Value: IdcEntityType */
	private static String KEY_ENTITY_TYPE = "_ENTITY_TYPE";
	/** Value: String */
	private static String KEY_ENTITY_ALIAS = "_ENTITY_ALIAS";
	/** Value: Integer */
	private static String KEY_FROM_START_INDEX = "_FROM_START_INDEX";
	/** Value: Boolean */
	private static String KEY_REMOVED_DISTINCT = "_REMOVED_DISTINCT";

	public HQLDaoHibernate(SessionFactory factory) {
        super(factory);
    }

	public long queryHQLTotalNum(String hqlQuery) {

		IngridDocument hqlDoc = preprocessHQL(hqlQuery, false, false);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);

		if (qString == null) {
			return 0;
		}

		String qStringFrom = qString.substring(fromStartIndex);
		
		if (entityAlias == null) {
			qString = "select count(*) "
				+ qStringFrom;			
		} else {
			// WORKS on ORACLE !
			qString = "select count(distinct "
				+ entityAlias + ") "
				+ qStringFrom;
		}

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public IngridDocument queryHQL(String hqlQuery,
			int startHit, int numHits) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery, false, false);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		IdcEntityType entityType = (IdcEntityType) hqlDoc.get(KEY_ENTITY_TYPE);
		String entityAlias = hqlDoc.getString(KEY_ENTITY_ALIAS);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);

		List entityList = new ArrayList();
		if (qString != null) {
			
			String qStringFrom = qString.substring(fromStartIndex);

			if (entityAlias != null) {
				qString = "select "
					+ entityAlias + " " 
					+ qStringFrom;
			}
			
			Session session = getSession();

			entityList = session.createQuery(qString)
				.setFirstResult(startHit)				
				.setMaxResults(numHits)				
				.setResultTransformer(new DistinctRootEntityResultTransformer())
				.list();
		}

		IngridDocument result = new IngridDocument();
		if (entityType == IdcEntityType.OBJECT) {
			result.put(MdekKeys.OBJ_ENTITIES, entityList);
		} else {
			result.put(MdekKeys.ADR_ENTITIES, entityList);
		}

		return result;
	}

	public IngridDocument queryHQLToCsv(String hqlQuery, boolean allowQueryDirectInstances) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery, allowQueryDirectInstances, false);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);
		Boolean distinctRemoved = (Boolean) hqlDoc.get(KEY_REMOVED_DISTINCT);
		
		List hits = new ArrayList();
		if (qString != null) {
			Session session = getSession();
			Query q = session.createQuery(qString);
			if (distinctRemoved) {
				// NO ! "destroys" result columns
//				q.setResultTransformer(new DistinctRootEntityResultTransformer());
			}
			hits = q.list();
		}

		StringWriter sw = new StringWriter();
		try {
			// our csv writer !
			ExcelCSVPrinter ecsvp = null;
			for (Object hit : hits) {
				if (ecsvp == null) {
					// START !!!
					ecsvp = new ExcelCSVPrinter(sw);

					List<String> titles = extractCsvTitles(qString.substring(0, fromStartIndex), hit);
					ecsvp.writeln(titles.toArray(new String[titles.size()]));
				}

				List<Object> csvValues = extractCsvValues(hit);
				ecsvp.writeln(csvValues.toArray(new Object[csvValues.size()]));
			}

			if (ecsvp != null) {
				ecsvp.close();
			}
			
		} catch (Exception ex) {
			LOG.error("Problems writing csv file !", ex);
			throw new MdekException(new MdekError(MdekErrorType.CSV_WRITER_PROBLEMS));
		}

		byte[] compressedCsv = new byte[0];
		try {
			compressedCsv = MdekUtils.compressString(sw.toString());						
		} catch (Exception ex) {
			LOG.error("Problems compressing csv file !", ex);
			throw new MdekException(new MdekError(MdekErrorType.CSV_WRITER_PROBLEMS));
		}

		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.CSV_RESULT, compressedCsv);
		result.put(MdekKeys.TOTAL_NUM, new Long(hits.size()));

		return result;
	}
	
	public IngridDocument queryHQLToMap(String hqlQuery, Integer maxNumHits) {
		IngridDocument hqlDoc = preprocessHQL(hqlQuery, false, false);
		String qString = hqlDoc.getString(MdekKeys.HQL_QUERY);
		IdcEntityType entityType = (IdcEntityType) hqlDoc.get(KEY_ENTITY_TYPE);
		Integer fromStartIndex = (Integer) hqlDoc.get(KEY_FROM_START_INDEX);
		Boolean distinctRemoved = (Boolean) hqlDoc.get(KEY_REMOVED_DISTINCT);

		List hits = new ArrayList();
		if (qString != null) {
			Session session = getSession();
			
			Query q = session.createQuery(qString);
			if (maxNumHits != null) {
				q.setMaxResults(maxNumHits);				
			}
			if (distinctRemoved) {
				q.setResultTransformer(new DistinctRootEntityResultTransformer());
			}
			hits = q.list();
		}

		List<IngridDocument> resultDocs = new ArrayList<IngridDocument>();
		if (hits.size() > 0) {
			List<String> titles = extractCsvTitles(qString.substring(0, fromStartIndex), hits.get(0));
			for (Object hit : hits) {
				List<Object> values = extractCsvValues(hit);
				IngridDocument resultDoc = new IngridDocument();
				for (int i=0; i < titles.size(); i++) {
					resultDoc.put(titles.get(i), values.get(i));
				}
				resultDocs.add(resultDoc);
			}
		}

		IngridDocument result = new IngridDocument();
		if (entityType == IdcEntityType.OBJECT) {
			result.put(MdekKeys.OBJ_ENTITIES, resultDocs);
		} else {
			result.put(MdekKeys.ADR_ENTITIES, resultDocs);
		}
		result.put(MdekKeys.TOTAL_NUM, new Long(hits.size()));

		return result;
	}
	
	/**
	 * Process HQL query string for querying entities (objects or addresses).
	 * Performs checks etc.
	 * @param hqlQuery query to execute. NOTICE: entity to select is restricted !
	 * @param allowQueryDirectInstances false=only nodes allowed in FROM clause (FROM OBJECTNODE, FROM ADDRESSNODE)<br>
	 * 		true=also direct instances allowed in FROM clause (FROM T01Object, FROM T02Address)
	 * @param removeDistinct false=keep DISTINCT in select if present<br>
	 * 		true=remove DISTINCT from select
	 * @return document containing preprocessed HQL stuff
	 */
	private IngridDocument preprocessHQL(String hqlQuery,
			boolean allowQueryDirectInstances,
			boolean removeDistinct) {
		IngridDocument result = new IngridDocument();
		
		hqlQuery = MdekUtils.processStringParameter(hqlQuery);
		if (hqlQuery == null) {
			return result;
		}

		// check DML statements !
		// ----------
		String hqlUPPERCASE = hqlQuery.toUpperCase();
		// add blank at end to avoid conflict with e.g. markDeleted column
		String[] notAllowed_UPPERCASE = new String[] {
			"DELETE ", "UPDATE ", "INSERT "
		};
		for (String noGo : notAllowed_UPPERCASE) {
			if (hqlUPPERCASE.indexOf(noGo) != -1) {
				throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
			}
		}

		// check "fetch"
		// ----------
		// fetch not allowed ! Problems with select count()
		if (hqlUPPERCASE.indexOf("JOIN FETCH") != -1) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));			
		}

		// Remove distinct because of Oracle ! not possible with CLOBS
		// ----------
		Boolean distinctRemoved = false;
		if (removeDistinct) {
			Integer distinctIndex = hqlUPPERCASE.indexOf("DISTINCT");
			while (distinctIndex != -1) {
				distinctRemoved = true;
				// remove distinct
				hqlQuery = hqlQuery.substring(0, distinctIndex) +
					hqlQuery.substring(distinctIndex + 8);
				hqlUPPERCASE = hqlQuery.toUpperCase();
				distinctIndex = hqlUPPERCASE.indexOf("DISTINCT");
			}			
		}

		// check which entity
		// ----------
		IdcEntityType entityType = null;
		Integer fromStartIndex = hqlUPPERCASE.indexOf("FROM OBJECTNODE");
		if (fromStartIndex != -1) {
			entityType = IdcEntityType.OBJECT;
		}
		Integer tmpFromStartIndex = hqlUPPERCASE.indexOf("FROM ADDRESSNODE");
		if (tmpFromStartIndex != -1) {
			boolean isAddressQuery = true;
			if (entityType != null) {
				// also "FROM OBJECTNODE" in query
				// DO NOT THROW Exception, instead use first FROM in select as entityType. Select may be inner select like:
				// SELECT ... FROM ObjectNode oNode ... WHERE ... IN (SELECT ... FROM AddressNode aNode ...)
				//throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
				if (tmpFromStartIndex > fromStartIndex) {
					isAddressQuery = false;
				}
			}
			
			if (isAddressQuery) {
				entityType = IdcEntityType.ADDRESS;
				fromStartIndex = tmpFromStartIndex;
			}
		}
		// querying direct instances allowed ?
		if (allowQueryDirectInstances) {
			tmpFromStartIndex = hqlUPPERCASE.indexOf("FROM T01OBJECT");
			if (tmpFromStartIndex != -1) {
				if (entityType != null) {
					throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
				}
				entityType = IdcEntityType.OBJECT;
				fromStartIndex = tmpFromStartIndex;
			}
			tmpFromStartIndex = hqlUPPERCASE.indexOf("FROM T02ADDRESS");
			if (tmpFromStartIndex != -1) {
				if (entityType != null) {
					throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
				}
				entityType = IdcEntityType.ADDRESS;
				fromStartIndex = tmpFromStartIndex;
			}			
		}
		// wrong entity queried ?
		if (entityType == null) {
			throw new MdekException(new MdekError(MdekErrorType.HQL_NOT_VALID));
		}

		// determine entity alias!
		// ----------
		String entityAlias = extractEntityAlias(hqlQuery.substring(fromStartIndex));

		result.put(KEY_ENTITY_TYPE, entityType);
		result.put(KEY_ENTITY_ALIAS, entityAlias);
		result.put(KEY_FROM_START_INDEX, fromStartIndex);
		result.put(KEY_REMOVED_DISTINCT, distinctRemoved);
		result.put(MdekKeys.HQL_QUERY, hqlQuery);

		return result;
	}

	private String extractEntityAlias(String hqlFromClause) {
		String entityAlias = null;

		String[] tokens = hqlFromClause.split(" ");
		int numTokens = 0;
		boolean aliasLongVersion = false;
		for (String token : tokens) {
			// skip empty strings (when multiple " " in a row)
			if (token.length() == 0) {
				continue;
			}
			numTokens++;
			if (numTokens == 3) {
				entityAlias = token;
				if (entityAlias.toUpperCase().equals("AS")) {
					entityAlias = null;
					aliasLongVersion = true;
					continue;
				}
			}
			// take next token after AS
			if (aliasLongVersion) {
				entityAlias = token;
			}
			if (entityAlias != null) {
				break;
			}
		}
		
		return entityAlias;
	}

	private ArrayList<String> extractCsvTitles(String hqlSelectClause, Object hit) {
		ArrayList<String> titles = new ArrayList<String>();

		// first extract titles from select attributes !
		String[] tokens = hqlSelectClause.split(",");
		for (String token : tokens) {
			token = token.trim();
			String tokenUpp = token.toUpperCase();
			if (tokenUpp.startsWith("SELECT") ||
				tokenUpp.startsWith("DISTINCT")) {
				int startIndex = token.lastIndexOf(" ");
				token = token.substring(startIndex+1);
			}
			titles.add(token);				
		}

		// now we have plain hql select "attributes"
		// check whether there is an IEntity queried and resolve attributes of IEntity as titles !
		if (hit instanceof Object[]) {
			Object[] objs = (Object[]) hit;
			for (int i=0; i < objs.length; i++) {
				resolveCsvTitlesOfEntity(objs[i], titles, i);
			}
		} else {
			resolveCsvTitlesOfEntity(hit, titles, 0);
		}

		return titles;
	}

	private void resolveCsvTitlesOfEntity(Object resultObject, ArrayList<String> titles, int indexTitleToResolve) {
		if (resultObject instanceof IEntity) {
			String entityAlias = titles.get(indexTitleToResolve);
			List<String> entityTitles = extractCsvTitlesFromEntity((IEntity)resultObject, entityAlias);
			// replace entity alias with entity properties ! 
			titles.remove(indexTitleToResolve);
			titles.addAll(indexTitleToResolve, entityTitles);
		}
	}

	private ArrayList<String> extractCsvTitlesFromEntity(IEntity mdekEntity, String entityAlias) {
		ArrayList<String> titles = new ArrayList<String>();

		String prefix = "";
		if (entityAlias != null && entityAlias.trim().length() > 0) {
			prefix = entityAlias + ".";			
		}

		PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(mdekEntity.getClass());
		for (PropertyDescriptor prop : props) {
			// skip collections !
			if (!skipProperty(prop)) {
				titles.add(prefix + prop.getDisplayName());
			}
		}

		return titles;
	}
	
	private boolean skipProperty(PropertyDescriptor prop) {
		boolean skip = false;
		
		Class propClass = prop.getPropertyType();
		if (propClass == null) {
			skip = true;
		} else if (Collection.class.isAssignableFrom(propClass)) {
			skip = true;
		} else if (IEntity.class.isAssignableFrom(propClass)) {
			skip = true;
		} else if (propClass.isAssignableFrom(Serializable.class)) {
			// id is serializabel !
			// or compare property name ?
			skip = false;
		} else if (propClass.isAssignableFrom(Class.class)) {
			skip = true;
		}
		
		return skip;
	}

	private List<Object> extractCsvValues(Object hit) {
		ArrayList<Object> values = new ArrayList<Object>();
		
		if (hit instanceof Object[]) {
			Object[] objs = (Object[]) hit;
			for (Object obj : objs) {
				appendCsvValues(obj, values);
			}

		} else {
			appendCsvValues(hit, values);
		}
		
		return values;
	}

	private void appendCsvValues(Object resultObject, ArrayList<Object> values) {
		if (resultObject instanceof IEntity) {
			values.addAll(extractCsvValuesFromEntity((IEntity) resultObject));
			
		} else {
			values.add(resultObject);
		}
	}

	private ArrayList<Object> extractCsvValuesFromEntity(IEntity mdekEntity) {
		ArrayList<Object> values = new ArrayList<Object>();

		PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(mdekEntity.getClass());
		for (PropertyDescriptor prop : props) {
			// skip collections !
			if (!skipProperty(prop)) {
				Object val;
				try {
					val = prop.getReadMethod().invoke(mdekEntity);
				} catch (Exception ex) {
					LOG.error("Problems getting IEntity value via reflection !", ex);
					val = "!!! Error extracting value via reflection !!!";
				}
				values.add(val);
			}
		}

		return values;
	}
}
