package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
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
    
	public SpatialRefValue loadRefValue(String type, String nameValue, Integer nameKey, Long spatialRefSnsId, String nativekey, Long objId) {
		if (LOG.isDebugEnabled()) {
//			LOG.debug("type: " + type + ", name: " + name + ", SpatialRefSns_ID: " + spatialRefSnsId + ", nativeKey: " + nativekey);			
		}

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

	public SpatialRefValue loadFreiRefValue(String nameValue, Integer nameKey, Long objId) {
		Session session = getSession();

		String qString = "from SpatialReference spRef " +
			"left join spRef.spatialRefValue spRefVal " +
			"where spRefVal.type = '" + SpatialReferenceType.FREI.getDbValue() + "' " +
			"and spRef.objId = ? ";
		Query q;
		if (nameKey != null && nameKey.equals("-1")) {
			if (nameValue != null) {
				qString = qString + "and spRefVal.nameValue = ?";
				q = session.createQuery(qString);
				q.setLong(0, objId);
				q.setString(0, nameValue);
			} else {
				qString = qString + "and spRefVal.nameValue is null";
				q = session.createQuery(qString);
				q.setLong(0, objId);
			}
		} else if (nameKey != null && !nameKey.equals("-1")) {
			qString = qString + "and spRefVal.nameKey = ?";
			q = session.createQuery(qString);
			q.setLong(0, objId);
			q.setInteger(1, nameKey);
		} else {
			if (nameKey != null && nameValue != null) {
				qString = qString + "and spRefVal.nameKey = ? and spRefVal.nameValue = ?";
				q = session.createQuery(qString);
				q.setLong(0, objId);
				q.setInteger(1, nameKey);
				q.setString(2, nameValue);
			} else if (nameKey == null && nameValue != null) {
				qString = qString + "and spRefVal.nameKey is null and spRefVal.nameValue = ?";
				q = session.createQuery(qString);
				q.setLong(0, objId);
				q.setString(2, nameValue);
			} else if (nameKey != null && nameValue == null) {
				qString = qString + "and spRefVal.nameKey = ? and spRefVal.nameValue is null";
				q = session.createQuery(qString);
				q.setLong(0, objId);
				q.setInteger(1, nameKey);
			} else {
				qString = qString + "and spRefVal.nameKey is null and spRefVal.nameValue is null";
				q = session.createQuery(qString);
				q.setLong(0, objId);
			}
		}

		SpatialRefValue spRefValue = null;
		SpatialReference spRef = (SpatialReference) q.uniqueResult();
		if (spRef != null) {
			spRefValue = spRef.getSpatialRefValue();
		}

		return spRefValue; 
	}

	public SpatialRefValue loadThesaurusRefValue(String nameValue, Long spatialRefSnsId, String nativekey) {
		Session session = getSession();

		String qString = "from SpatialRefValue spRefVal " +
			"left join fetch spRefVal.spatialRefSns " +
			"where spRefVal.type = '" + SpatialReferenceType.GEO_THESAURUS.getDbValue() + "' ";

		if (nameValue != null) {
			qString += "and spRefVal.nameValue = ? ";
		} else {
			qString += "and spRefVal.nameValue is null ";			
		}
		if (spatialRefSnsId != null) {
			qString += "and spRefVal.spatialRefSnsId = ? ";
		} else {
			qString += "and spRefVal.spatialRefSnsId is null ";			
		}
		if (nativekey != null) {
			qString += "and spRefVal.nativekey = ? ";
		} else {
			qString += "and spRefVal.nativekey is null ";			
		}
	
		Query q = session.createQuery(qString);
		int nextPos = 0;
		if (nameValue != null) {
			q.setString(nextPos++, nameValue);
		}
		if (spatialRefSnsId != null) {
			q.setLong(nextPos++, spatialRefSnsId);			
		}
		if (nativekey != null) {
			q.setString(nextPos++, nativekey);			
		}

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
}
