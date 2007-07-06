/*
 * Created on 03.07.2007
 */
package de.ingrid.mdek.services.persistence;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T, ID extends Serializable> {

	T loadById(ID id, boolean lock);

	T getById(ID id, boolean lock);

	T loadById(ID id);

	T getById(ID id);

	List<T> findAll();

	List<T> findByExample(T exampleInstance);

	List<T> findByExample(T exampleInstance, int maxResults);

	T findUniqueByExample(T exampleInstance);

	T makePersistent(T entity);

	void makeTransient(T entity);

}
