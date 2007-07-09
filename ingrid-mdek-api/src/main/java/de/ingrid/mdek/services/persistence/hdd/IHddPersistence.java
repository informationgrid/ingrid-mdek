package de.ingrid.mdek.services.persistence.hdd;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface IHddPersistence<T extends Serializable> {

	T findById(String id, boolean shouldExists) throws IOException;

	List<T> findAll() throws IOException;

	void makePersistent(String id, T entity) throws IOException;

	void makeTransient(String id) throws IOException;

}
