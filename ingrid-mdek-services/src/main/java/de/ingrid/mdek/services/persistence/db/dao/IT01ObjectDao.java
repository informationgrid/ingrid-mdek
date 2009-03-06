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

	/** Get ALL Objects (also published ones) where given user is responsible user. */
	List<T01Object> getAllObjectsOfResponsibleUser(String responsibleUserUuid);
	/** Get according HQL Statement to fetch csv data !. */
	String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid);
}
