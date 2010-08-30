package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T08Attr;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;


/**
 * Business DAO operations related to the <tt>T08AttrType</tt> entity.
 * 
 * @author Martin
 */
public interface IT08AttrTypeDao
	extends IGenericDao<T08AttrType> {

	/**
	 * Get additional fields of given IDs AS LIST OF BEANS. PASS null if all fields ! 
	 * Also fetch selection list of given language if present.
	 * @param fieldIds pass null if all fields
	 * @param languageCode pass null if all languages (selection lists in all languages)
	 * @return list of beans with prefetched selection lists
	 */
	List<T08AttrType> getT08AttrTypes(Long[] fieldIds, String languageCode);

	/**
	 * Get additional fields of given NAMESs AS LIST OF BEANS. PASS null if all fields ! 
	 * Also fetch selection list of given language if present.
	 * @param fieldNames pass null if all fields
	 * @param languageCode pass null if all languages (selection lists in all languages)
	 * @return list of beans with prefetched selection lists
	 */
	List<T08AttrType> getT08AttrTypesByName(String[] fieldNames, String languageCode);

	/**
	 * Get all input data (connected to objects) of additional field of given id. 
	 * @param fieldId additional field identifier
	 * @return list of data entries
	 */
	List<T08Attr> getT08Attr(Long fieldId);
}
