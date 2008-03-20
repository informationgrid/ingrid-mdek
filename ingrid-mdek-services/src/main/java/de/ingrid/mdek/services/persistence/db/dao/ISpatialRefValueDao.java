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

	/** Load SpatialRefValue according to given values. If not found create AND save it !
	 * @param type
	 * @param nameValue
	 * @param nameKey
	 * @param spRefSns according bean (or null)
	 * @param nativekey
	 * @param objectId connected to this object
	 * @return persisted SpatialRefValue (with Id)
	 */
	SpatialRefValue loadOrCreate(String type, String nameValue, Integer nameKey, SpatialRefSns spRefSns, String nativekey, Long objectId);

}
