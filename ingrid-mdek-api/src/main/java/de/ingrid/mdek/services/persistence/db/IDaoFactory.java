package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;

public interface IDaoFactory {

	// TODO wemove implement
	IGenericDao<Serializable, Serializable> getDao(Class clazz);

}
