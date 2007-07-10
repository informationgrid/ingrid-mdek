package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

import de.ingrid.utils.IngridDocument;

public interface IAtomarModelPersister {

	public static final String MODEL_EXCEPTION = "model_exception";

	public static final String MODEL_STATE = "model_state";

	public static final String MODEL_INSTANCES = "model_instances";

	public static final String MODEL_INSTANCE = "model_instance";

	IngridDocument selectAll(Class clazz);

	IngridDocument selectById(Class clazz, Serializable id);

	IngridDocument insert(Class clazz, List<Serializable> objects);

	IngridDocument update(Class clazz, List<Serializable> objects);

	IngridDocument delete(Class clazz, List<Serializable> ids);
}
