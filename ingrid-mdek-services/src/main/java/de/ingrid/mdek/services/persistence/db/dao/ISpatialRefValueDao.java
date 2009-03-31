package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.T01Object;



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
	 * @param objectId connected to this object, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @return persisted SpatialRefValue (with Id)
	 */
	SpatialRefValue loadOrCreate(String type, String nameValue, Integer nameKey,
		SpatialRefSns spRefSns, String nativekey, Long objectId);

	/** Load SpatialRefValues according to given parameters. Passed type determines how to
	 * fetch.
	 * @param name pass if type FREI
	 * @param snsId pass if type GEO_THESAURUS
	 * @return list of SpatialRefValues or empty list if not found
	 */
	List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType type, String name, String snsId);

	/** get REFERENCED NON EXPIRED spatial reference values of given type(s).
	 * NOTICE: only returns spatial ref values REFERENCED by Objects/Addresses (e.g. NOT unused ones) !
	 * @param types pass types to fetch. Pass null or empty array if all types !
	 * @return list of spatial reference values.
	 * 	NOTICE: NOT distinct, e.g. same free spatial ref value may exist in multiple DIFFERENT SpatialRefValue !
	 */
	List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType[] types);

	/** Return number of objects referencing the given spatial ref.*/
	long countObjectsOfSpatialRefValue(long idSpatialRefValue);

	/** Get all referencing objects of given spatial ref */
	List<T01Object> getObjectsOfSpatialRefValue(long idSpatialRefValue);
}
