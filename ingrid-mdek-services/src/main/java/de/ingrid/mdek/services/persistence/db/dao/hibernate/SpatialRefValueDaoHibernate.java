package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;

/**
 * Hibernate-specific implementation of the <tt>SpatialRefValue</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SpatialRefValueDaoHibernate
	extends GenericHibernateDao<SpatialRefValue>
	implements  ISpatialRefValueDao {

    public SpatialRefValueDaoHibernate(SessionFactory factory) {
        super(factory, SpatialRefValue.class);
    }
    
	public SpatialRefValue load(String type, String name, Long spatialRefSnsId, String nativekey) {
		Session session = getSession();

		String qString = "from SpatialRefValue spRefVal " +
			"left join fetch spRefVal.spatialRefSns " +
			"where spRefVal.type = ? ";
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
		q.setString(0, type);
		int nextPos = 1;
		if (name != null) {
			q.setString(nextPos++, name);
		}
		if (spatialRefSnsId != null) {
			q.setLong(nextPos++, spatialRefSnsId);			
		}
		if (nativekey != null) {
			q.setString(nextPos++, nativekey);			
		}

		SpatialRefValue spRefValue = (SpatialRefValue) q.uniqueResult();
		
		return spRefValue;
	}

	public SpatialRefValue loadOrCreate(String type, String name, SpatialRefSns spRefSns, String nativekey) {
		Long spRefSnsId = (spRefSns != null) ? spRefSns.getId() : null; 
		SpatialRefValue spRefValue = load(type, name, spRefSnsId, nativekey);
		
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
