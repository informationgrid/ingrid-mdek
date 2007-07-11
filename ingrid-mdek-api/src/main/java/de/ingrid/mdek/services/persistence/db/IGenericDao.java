/*
 * Created on 03.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T extends IEntity> extends
		ITransactionService {

	T loadById(Serializable id, boolean lock);

	T getById(Serializable id, boolean lock);

	T loadById(Serializable id);

	T getById(Serializable id);

	List<T> findAll();

	List<T> findByExample(T exampleInstance);

	List<T> findByExample(T exampleInstance, int maxResults);

	T findUniqueByExample(T exampleInstance);

	void makePersistent(T entity);

	void makeTransient(T entity);

}
