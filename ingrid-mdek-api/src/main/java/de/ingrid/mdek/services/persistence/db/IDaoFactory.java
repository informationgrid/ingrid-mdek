package de.ingrid.mdek.services.persistence.db;


public interface IDaoFactory {

	// TODO wemove implement
	IGenericDao<IEntity> getDao(Class clazz);

}
