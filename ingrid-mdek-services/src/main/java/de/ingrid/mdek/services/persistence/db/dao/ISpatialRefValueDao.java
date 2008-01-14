package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;



/**
 * Business DAO operations related to the <tt>SpatialRefValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISpatialRefValueDao
	extends IGenericDao<SpatialRefValue> {

	/** Load SpatialRefValue with given values. Returns null if not found.	 */
	SpatialRefValue load(String type, String name, Long spatialRefSnsId, String nativekey);

	/** Load SpatialRefValue with given values. If not found create AND save it ! */
	SpatialRefValue loadOrCreate(String type, String name, SpatialRefSns spRefSns, String nativekey);
}
