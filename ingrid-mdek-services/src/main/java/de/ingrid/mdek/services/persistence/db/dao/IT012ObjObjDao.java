package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T012ObjObj;

/**
 * Business DAO operations related to the <tt>T012ObjObj</tt> entity.
 * 
 * @author Martin
 */
public interface IT012ObjObjDao
	extends IGenericDao<T012ObjObj> {

	/** Get object object association where given object uuid is child */
	T012ObjObj getParentAssociation(String objUuid);
}
