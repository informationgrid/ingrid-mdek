/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * Hibernate-specific implementation of the <tt>SpatialRefValue</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SpatialRefValueDaoHibernate
	extends GenericHibernateDao<SpatialRefValue>
	implements  ISpatialRefValueDao {

	private static final Logger LOG = LogManager.getLogger(SpatialRefValueDaoHibernate.class);

    public SpatialRefValueDaoHibernate(SessionFactory factory) {
        super(factory, SpatialRefValue.class);
    }
    
	/** Load SpatialRefValue according to given values. Returns null if not found. 
	 * @param type
	 * @param nameValue
	 * @param nameKey
	 * @param spatialRefSnsId id of record in SpatialRefSns
	 * @param nativekey
	 * @param objectId connected to this object, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @return SpatialRefValue or null
	 */
	private SpatialRefValue loadRefValue(String type, String nameValue, Integer nameKey,
			Long spatialRefSnsId, Long objId) {
		SpatialReferenceType spRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, type);

		SpatialRefValue spRefValue = null;
		if (SpatialReferenceType.FREI == spRefType) {
			spRefValue = loadFreiRefValue(nameValue, nameKey, objId);

		} else if (SpatialReferenceType.GEO_THESAURUS == spRefType) {
			spRefValue = loadThesaurusRefValue(spatialRefSnsId);
			
		} else {
			LOG.warn("Unknown Type of SpatialRefValue, type: " + type);
		}

		return spRefValue;
	}

	/** Load Freien SpatialRefValue according to given values. Returns null if not found. 
	 * @param nameValue set if freier eintrag (then key is -1)
	 * @param nameKey set if from syslist, then value is null !
	 * @param objectId connected to this object, HAS TO BE PASSED TO GUARANTEE LOADING OF CORRECT ENTITY !
	 * @return
	 */
	private SpatialRefValue loadFreiRefValue(String nameValue, Integer nameKey, Long objId) {
		Session session = getSession();

		String qString = "from SpatialReference spRef " +
			"left join fetch spRef.spatialRefValue spRefVal " +
			"where spRefVal.type = '" + SpatialReferenceType.FREI.getDbValue() + "' " +
			"and spRef.objId = " + objId;

		// null keys not allowed ! all freie eintraege have a key !
		if (nameKey == null) {
			throw new MdekException(new MdekError(MdekErrorType.LIST_KEY_NULL_NOT_ALLOWED));
		}
		qString = qString + " and spRefVal.nameKey = " + nameKey;

		// if freier Eintrag
		if (nameKey.equals(-1)) {
			// and no name -> Exception
			if (nameValue == null || nameValue.trim().length() == 0) {
				throw new MdekException(new MdekError(MdekErrorType.LIST_NO_KEY_NO_VALUE));
			}
			// select also via name value !
			// we have to use LIKE to work on Oracle ! can't compare CLOB (text) with =
			// NOTICE: nameValue changed to VARCHAR(4000) on ORACLE ! but we keep CLOB Version, also works !
			qString = qString + " and spRefVal.nameValue LIKE '" + nameValue + "'";
		}

		Query q = session.createQuery(qString);

		SpatialRefValue spRefValue = null;
		// we query list(), NOT uniqueResult() because mySQL doesn't differ between ss <-> ß, lower <-> uppercase ...
		// then we check all results in Java whether equal !
		List<SpatialReference> spRefs = q.list();
		for (SpatialReference spRef : spRefs) {
			if (nameValue.equals(spRef.getSpatialRefValue().getNameValue())) {
				spRefValue = spRef.getSpatialRefValue();
				break;
			}
		}

		return spRefValue; 
	}

	/** Load SNS Geo-Thesaurus SpatialRefValue according to given value. Returns null if not found. 
	 * @param spatialRefSnsId id of record in SpatialRefSns, NEVER NULL, has to exist !
	 * @return SpatialRefValue or null
	 */
	private SpatialRefValue loadThesaurusRefValue(Long spatialRefSnsId) {
		Session session = getSession();

		String qString = "from SpatialRefValue spRefVal " +
			"left join fetch spRefVal.spatialRefSns " +
			"where spRefVal.type = '" + SpatialReferenceType.GEO_THESAURUS.getDbValue() + "' ";

		// null topic ids not allowed ! all sns spatial references have a topic id
		if (spatialRefSnsId == null) {
			throw new MdekException(new MdekError(MdekErrorType.SNS_SPATIAL_REFERENCE_WITHOUT_TOPIC_ID));
		}
		qString += "and spRefVal.spatialRefSnsId = " + spatialRefSnsId;

		Query q = session.createQuery(qString);

		return (SpatialRefValue) q.uniqueResult();
	}

	public SpatialRefValue loadOrCreate(String type, 
			String nameValue, Integer nameKey, 
			SpatialRefSns spRefSns, Long objId) {
		Long spRefSnsId = (spRefSns != null) ? spRefSns.getId() : null; 
		SpatialRefValue spRefValue =
			loadRefValue(type, nameValue, nameKey, spRefSnsId, objId);
		
		if (spRefValue == null) {
			spRefValue = new SpatialRefValue();
		}

		// update with newest values
		spRefValue.setType(type);
		spRefValue.setNameValue(nameValue);
		spRefValue.setNameKey(nameKey);
		spRefValue.setSpatialRefSns(spRefSns);
		spRefValue.setSpatialRefSnsId(spRefSnsId);
		makePersistent(spRefValue);
		
		return spRefValue;
	}

	public List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType type, String name,
			String snsId) {
		Session session = getSession();
		List<SpatialRefValue> retList = null;

		String q = "from SpatialRefValue spRefVal ";
		if (SpatialReferenceType.isThesaurusType(type)) {
			q += "left join fetch spRefVal.spatialRefSns spRefSns ";
		}
		q += "where spRefVal.type = '" + type.getDbValue() + "' ";

		if (type == SpatialReferenceType.FREI) {
			// we have to use LIKE to work on Oracle ! can't compare CLOB (text) with =
			// NOTICE: nameValue changed to VARCHAR(4000) on ORACLE ! but we keep CLOB Version, also works !
			q += "and spRefVal.nameValue LIKE '" + name + "'";
			// NOTICE: we query MULTIPLE values !
			retList = session.createQuery(q).list();

		} else if (SpatialReferenceType.isThesaurusType(type)) {
			q += "and spRefSns.snsId = '" + snsId + "'";
/*
			// NOTICE: we query SINGLE value ! Has to be unique !
			retList = new ArrayList<SpatialRefValue>();
			SpatialRefValue spRefValue = (SpatialRefValue) session.createQuery(q).uniqueResult();
			if (spRefValue != null) {
				retList.add(spRefValue);
			}
*/
			// we query list(), maybe NOT uniqueResult() ! e.g. multiple imported values
			// refering to same searchtermSns.
			retList = session.createQuery(q).list();
		}

		return retList;
	}

	public List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType[] types) {
		if (types == null) {
			types = new SpatialReferenceType[0];
		}

		Session session = getSession();

		// fetch all refs referenced by Objects !
		String q = "select spRefVal " +
			"from SpatialReference spRef " +
			"inner join spRef.spatialRefValue spRefVal " +
			"left join fetch spRefVal.spatialRefSns spRefSns " +
			"where spRefSns.expiredAt is null ";

		String hqlToken = "and ( ";
		for (SpatialReferenceType type : types) {
			q += hqlToken + "spRefVal.type = '" + type.getDbValue() + "' ";
			hqlToken = "OR ";
		}
		if (hqlToken.equals("OR ")) {
			q += ")";
		}

		return  session.createQuery(q)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();
	}

	public long countObjectsOfSpatialRefValue(long idSpatialRefValue) {
		String q = "select count(distinct spRef) " +
			"from SpatialReference spRef " +
			"where spRef.spatialRefId = " + idSpatialRefValue;
		
		return (Long) getSession().createQuery(q).uniqueResult();
	}

	public List<T01Object> getObjectsOfSpatialRefValue(long idSpatialRefValue) {
		String q = "select obj " +
			"from T01Object obj " +
			"inner join obj.spatialReferences spRef " +
			"where spRef.spatialRefId = " + idSpatialRefValue;
		
		return  getSession().createQuery(q)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();
	}
	public List<Long> getObjectIdsOfSpatialRefValue(long idSpatialRefValue) {
		String q = "select distinct spRef.objId " +
			"from SpatialReference spRef " +
			"where spRef.spatialRefId = " + idSpatialRefValue;

		return  getSession().createQuery(q).list();
	}
	public List<SpatialReference> getSpatialReferences(long idSpatialRefValue) {
		String q = "from SpatialReference spRef " +
			"where spRef.spatialRefId = " + idSpatialRefValue;
		
		return  getSession().createQuery(q).list();
	}
}
