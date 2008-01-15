package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

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

	/** Load SpatialRefValue according to given values. Returns null if not found. 
	 * @param type
	 * @param name
	 * @param spatialRefSnsId id of record in SpatialRefSns
	 * @param nativekey
	 * @param objectId connected to this object
	 * @return SpatialRefValue or null
	 */
	SpatialRefValue loadRefValue(String type, String name, Long spatialRefSnsId, String nativekey, Long objId);

	/** Load Freien SpatialRefValue according to given values. Returns null if not found. 
	 * @param name
	 * @param objectId connected to this object
	 * @return SpatialRefValue or null
	 */
	SpatialRefValue loadFreiRefValue(String name, Long objId);

	/** Load SNS Geo-Thesaurus SpatialRefValue according to given values. Returns null if not found. 
	 * @param name
	 * @param spatialRefSnsId id of record in SpatialRefSns
	 * @param nativekey
	 * @return SpatialRefValue or null
	 */
	SpatialRefValue loadThesaurusRefValue(String name, Long spatialRefSnsId, String nativekey);

	/** Load SpatialRefValue according to given values. If not found create AND save it !
	 * @param type
	 * @param name
	 * @param spatialRefSnsId id of record in SpatialRefSns
	 * @param nativekey
	 * @param objectId connected to this object
	 * @return persisted SpatialRefValue (with Id)
	 */
	SpatialRefValue loadOrCreate(String type, String name, SpatialRefSns spRefSns, String nativekey, Long objectId);

	/** returns a list of all names (unique) of the freien SpatialRefValues ordered by name */
	List<String> getFreieRefValueNames();

}
