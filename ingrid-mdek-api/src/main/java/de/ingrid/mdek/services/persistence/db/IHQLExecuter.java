/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
