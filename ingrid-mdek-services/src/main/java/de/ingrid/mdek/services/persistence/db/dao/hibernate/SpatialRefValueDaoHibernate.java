package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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

/**
 * Hibernate-specific implementation of the <tt>SpatialRefValue</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SpatialRefValueDaoHibernate
	extends GenericHibernateDao<SpatialRefValue>
	implements  ISpatialRefValueDao {

	private static final Logger LOG = Logger.getLogger(SpatialRefValueDaoHibernate.class);

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
	private SpatialRefValue loadRefValue(String type, String nameValue, Integer nameKey, Long spatialRefSnsId, String nativekey,
			Long objId) {
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("type: " + type + ", nameKey: " + nameKey + ", nameValue: " + nameValue + ", SpatialRefSns_ID: " + spatialRefSnsId + ", nativeKey: " + nativekey);			
//		}

		SpatialReferenceType spRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, type);

		SpatialRefValue spRefValue = null;
		if (SpatialReferenceType.FREI == spRefType) {
			spRefValue = loadFreiRefValue(nameValue, nameKey, objId);

		} else if (SpatialReferenceType.GEO_THESAURUS == spRefType) {
			spRefValue = loadThesaurusRefValue(nameValue, spatialRefSnsId, nativekey);
			
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
			qString = qString + " and spRefVal.nameValue = '" + nameValue + "'";
		}

		Query q = session.createQuery(qString);

		SpatialReference spRef = (SpatialReference) q.uniqueResult();
		SpatialRefValue spRefValue = null;
		if (spRef != null) {
			spRefValue = spRef.getSpatialRefValue();
		}

		return spRefValue; 
	}

	/** Load SNS Geo-Thesaurus SpatialRefValue according to given values. Returns null if not found. 
	 * @param nameValue
	 * @param spatialRefSnsId id of record in SpatialRefSns
	 * @param nativekey
	 * @return SpatialRefValue or null
	 */
	private SpatialRefValue loadThesaurusRefValue(String nameValue, Long spatialRefSnsId, String nativekey) {
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

	public SpatialRefValue loadOrCreate(String type, String nameValue, Integer nameKey, SpatialRefSns spRefSns, String nativekey, Long objId) {
		Long spRefSnsId = (spRefSns != null) ? spRefSns.getId() : null; 
		SpatialRefValue spRefValue = loadRefValue(type, nameValue, nameKey, spRefSnsId, nativekey, objId);
		
		if (spRefValue == null) {
			spRefValue = new SpatialRefValue();
			spRefValue.setType(type);
			spRefValue.setNameValue(nameValue);
			spRefValue.setNameKey(nameKey);
			spRefValue.setSpatialRefSns(spRefSns);
			spRefValue.setSpatialRefSnsId(spRefSnsId);
			spRefValue.setNativekey(nativekey);
			makePersistent(spRefValue);
		}
		
		return spRefValue;
	}

	public List<SpatialRefValue> getSpatialReferences(SpatialReferenceType[] types) {
		if (types == null) {
			types = new SpatialReferenceType[0];
		}

		Session session = getSession();

		String q = "from SpatialRefValue spRefVal " +
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
		
		return  session.createQuery(q).list();
	}
}
