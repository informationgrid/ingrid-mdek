package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

import de.ingrid.utils.IngridDocument;

public class AtomarModelPersister implements IAtomarModelPersister {

	private final IDaoFactory _daoFactory;

	public AtomarModelPersister(IDaoFactory daoFactory) {
		_daoFactory = daoFactory;
	}

	public IngridDocument selectAll(Class clazz) {
		IngridDocument document = new IngridDocument();
		IGenericDao<Serializable, Serializable> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			List<Serializable> objects = dao.findAll();
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

	public IngridDocument insert(Class clazz, List<Serializable> objects) {
		IngridDocument document = new IngridDocument();
		IGenericDao<Serializable, Serializable> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			for (Serializable object : objects) {
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
		IGenericDao<Serializable, Serializable> dao = _daoFactory.getDao(clazz);
		try {
			dao.beginTransaction();
			for (Serializable id : ids) {
				Serializable byId = dao.getById(id);
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
		IGenericDao<Serializable, Serializable> dao = _daoFactory.getDao(clazz);
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

	public IngridDocument update(Class clazz, List<Serializable> objects) {
		return insert(clazz, objects);
	}

}
