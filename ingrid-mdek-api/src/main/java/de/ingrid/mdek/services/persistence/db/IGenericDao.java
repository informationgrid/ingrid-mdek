/*
 * Created on 03.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T extends IEntity> extends
		ITransactionService {

	/** Never returns null ! if object not exists returns empty proxy ! */
	T loadById(Serializable id, boolean lock);

	/** Returns null if object not exists */
	T getById(Serializable id, boolean lock);

	/** Never returns null ! if object not exists returns empty proxy ! */
	T loadById(Serializable id);

	/** Returns null if object not exists */
	T getById(Serializable id);

	List<T> findAll();

	T findFirst();

	List<T> findByExample(T exampleInstance);

	List<T> findByExample(T exampleInstance, int maxResults);

	T findUniqueByExample(T exampleInstance);

	void makePersistent(T entity);

	void makeTransient(T entity);

}
