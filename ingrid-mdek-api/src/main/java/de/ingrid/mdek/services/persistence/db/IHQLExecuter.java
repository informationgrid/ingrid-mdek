package de.ingrid.mdek.services.persistence.db;

import de.ingrid.utils.IngridDocument;

public interface IHQLExecuter {

	public static final String HQL_QUERIES = "hql_queries";

	public static final String HQL_EXCEPTION = "hql_exception";

	public static final String HQL_STATE = "hql_state";

	public static final String HQL_SELECT = "hql_select";

	public static final String HQL_UPDATE = "hql_update";

	public static final String HQL_DELETE = "hql_delete";

	public static final String HQL_RESULT = "hql_result";

	IngridDocument execute(IngridDocument document);
}
