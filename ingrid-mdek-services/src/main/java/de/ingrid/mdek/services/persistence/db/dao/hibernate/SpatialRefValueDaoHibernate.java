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
    
	public SpatialRefValue loadRefValue(String type, String name, Long spatialRefSnsId, String nativekey, Long objId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("type: " + type + ", name: " + name + ", SpatialRefSns_ID: " + spatialRefSnsId + ", nativeKey: " + nativekey);			
		}

		SpatialReferenceType spRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, type);

		SpatialRefValue spRefValue = null;
		if (SpatialReferenceType.FREI == spRefType) {
			spRefValue = loadFreiRefValue(name, objId);

		} else if (SpatialReferenceType.GEO_THESAURUS == spRefType) {
			spRefValue = loadThesaurusRefValue(name, spatialRefSnsId, nativekey);
			
		} else {
			LOG.warn("Unknown Type of SpatialRefValue, type: " + type);
		}

		return spRefValue;
	}

	public SpatialRefValue loadFreiRefValue(String name, Long objId) {
		Session session = getSession();

		String qString = "from SpatialReference spRef " +
			"left join spRef.spatialRefValue spRefVal " +
			"where spRefVal.type = '" + SpatialReferenceType.FREI.getDbValue() + "' " +
			"and spRefVal.name = ? " +
			"and spRef.objId = ?";
	
		Query q = session.createQuery(qString);
		q.setString(0, name);
		q.setLong(1, objId);

		SpatialRefValue spRefValue = null;
		SpatialReference spRef = (SpatialReference) q.uniqueResult();
		if (spRef != null) {
			spRefValue = spRef.getSpatialRefValue();
		}

		return spRefValue; 
	}

	public SpatialRefValue loadThesaurusRefValue(String name, Long spatialRefSnsId, String nativekey) {
		Session session = getSession();

		String qString = "from SpatialRefValue spRefVal " +
			"left join fetch spRefVal.spatialRefSns " +
			"where spRefVal.type = '" + SpatialReferenceType.GEO_THESAURUS.getDbValue() + "' ";

		if (name != null) {
			qString += "and spRefVal.name = ? ";
		} else {
			qString += "and spRefVal.name is null ";			
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
		if (name != null) {
			q.setString(nextPos++, name);
		}
		if (spatialRefSnsId != null) {
			q.setLong(nextPos++, spatialRefSnsId);			
		}
		if (nativekey != null) {
			q.setString(nextPos++, nativekey);			
		}

		return (SpatialRefValue) q.uniqueResult();
	}

	public SpatialRefValue loadOrCreate(String type, String name, SpatialRefSns spRefSns, String nativekey, Long objId) {
		Long spRefSnsId = (spRefSns != null) ? spRefSns.getId() : null; 
		SpatialRefValue spRefValue = loadRefValue(type, name, spRefSnsId, nativekey, objId);
		
		if (spRefValue == null) {
			spRefValue = new SpatialRefValue();
			spRefValue.setType(type);
			spRefValue.setName(name);
			spRefValue.setSpatialRefSns(spRefSns);
			spRefValue.setSpatialRefSnsId(spRefSnsId);
			spRefValue.setNativekey(nativekey);
			makePersistent(spRefValue);
		}
		
		return spRefValue;
	}
}
