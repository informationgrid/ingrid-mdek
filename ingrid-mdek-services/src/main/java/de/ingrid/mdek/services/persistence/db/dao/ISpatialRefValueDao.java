package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
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
	 * @param objectId connected to this object, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @return persisted SpatialRefValue (with Id)
	 */
	SpatialRefValue loadOrCreate(String type, String nameValue, Integer nameKey,
		SpatialRefSns spRefSns, String nativekey, Long objectId);

	/** get NON EXPIRED spatial references of given type(s).
	 * @param types pass types to fetch. Pass null or empty array if all types !
	 * @return list of searchterms.
	 * 		NOTICE: NOT distinct, e.g. same free spatial references exist multiple times !
	 */
	List<SpatialRefValue> getSpatialReferences(SpatialReferenceType[] types);
}
