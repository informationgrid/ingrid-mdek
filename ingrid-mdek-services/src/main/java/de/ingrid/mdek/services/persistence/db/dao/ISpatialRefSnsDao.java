package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;



/**
 * Business DAO operations related to the <tt>SpatialRefSns</tt> entity.
 * 
 * @author Martin
 */
public interface ISpatialRefSnsDao
	extends IGenericDao<SpatialRefSns> {

	/** Load SpatialRefSns with given snsId. Returns null if not found.	 */
	SpatialRefSns load(String snsId);

	/** Load SpatialRefSns with given snsId. If not found create AND save it ! */
	SpatialRefSns loadOrCreate(String snsId);
}
