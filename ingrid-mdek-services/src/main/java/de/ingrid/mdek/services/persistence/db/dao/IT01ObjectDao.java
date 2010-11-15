package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;



/**
 * Business DAO operations related to the <tt>T01Object</tt> entity.
 * 
 * @author Martin
 */
public interface IT01ObjectDao
	extends IGenericDao<T01Object> {

	/** Get Objects (also published ones) where given user is responsible user.
	 * Pass maxNum or NULL if all addresses. */
	List<T01Object> getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid);
	
	/**
	 * Check for specific object address reference. If referenceTypeId is null
	 * the method checks if the object is related to the address no matter what
	 * type the reference has. 
	 * 
	 * @param objectUuid The object UUID
	 * @param addressUuid The address UUID.
	 * @param referenceTypeId The address reference type ("Auskunft").
	 * 
	 * @return True if the object has a specific address reference, false if not.
	 */
	boolean hasAddressRelation(String objectUuid, String addressUuid, Integer referenceTypeId);
}
