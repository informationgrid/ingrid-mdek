/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IngridDocument;

@Service
public class AtomarModelPersister implements IAtomarModelPersister {

	private final IDaoFactory _daoFactory;

	@Autowired
	public AtomarModelPersister(IDaoFactory daoFactory) {
		_daoFactory = daoFactory;
	}

	public IngridDocument selectAll(Class clazz) {
		IngridDocument document = new IngridDocument();
		IGenericDao<IEntity> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			List<IEntity> objects = dao.findAll();
			dao.commitTransaction();
			document.put(MODEL_INSTANCES, objects);
			document.putBoolean(MODEL_STATE, true);
		} catch (Exception e) {
			document.putBoolean(MODEL_STATE, false);
			document.put(MODEL_EXCEPTION, e.getMessage());
			dao.rollbackTransaction();
		}
		return document;
	}

	public IngridDocument insert(Class clazz, List<IEntity> objects) {
		IngridDocument document = new IngridDocument();
		IGenericDao<IEntity> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			for (IEntity object : objects) {
				dao.makePersistent(object);
			}
			dao.commitTransaction();
			document.putBoolean(MODEL_STATE, true);
		} catch (Exception e) {
			document.putBoolean(MODEL_STATE, false);
			document.put(MODEL_EXCEPTION, e.getMessage());
			dao.rollbackTransaction();
		}
		return document;
	}

	public IngridDocument delete(Class clazz, List<Serializable> ids) {
		IngridDocument document = new IngridDocument();
		IGenericDao<IEntity> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			for (Serializable id : ids) {
				IEntity byId = dao.getById(id);
				if (byId != null) {
					dao.makeTransient(byId);
				}
			}
			dao.commitTransaction();
			document.putBoolean(MODEL_STATE, true);
		} catch (Exception e) {
			document.putBoolean(MODEL_STATE, false);
			document.put(MODEL_EXCEPTION, e.getMessage());
			dao.rollbackTransaction();
		}
		return document;
	}

	public IngridDocument selectById(Class clazz, Serializable id) {
		IngridDocument document = new IngridDocument();
		IGenericDao<IEntity> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			Object byId = dao.getById(id);
			dao.commitTransaction();
			document.put(MODEL_INSTANCE, byId);
			document.putBoolean(MODEL_STATE, true);
		} catch (Exception e) {
			document.putBoolean(MODEL_STATE, false);
			document.put(MODEL_EXCEPTION, e.getMessage());
			dao.rollbackTransaction();
		}
		return document;
	}

	public IngridDocument update(Class clazz, List<IEntity> objects) {
		return insert(clazz, objects);
	}

}
