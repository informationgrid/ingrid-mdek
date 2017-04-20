/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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

	/** HIBERNATE per default flushes before EVERY QUERY causing huge performance loss (dependent from
	 * number of beans in session). This method disables auto flushing and switches to
	 * manual flushing. This mode is very efficient for read only transactions. 
	 */
	void disableAutoFlush();
	/** HIBERNATE: manually flush ! Flushing is the process of synchronizing the underlying
	 * persistent store with persistable state held in memory. 
	 */
	void flush();
}
