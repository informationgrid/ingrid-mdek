/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;



/**
 * Business DAO operations related to the <tt>SearchtermSns</tt> entity.
 * 
 * @author Martin
 */
public interface ISearchtermSnsDao
	extends IGenericDao<SearchtermSns> {

	/** Load SearchtermSns with given snsId. If not found create AND save it !
	 * @param snsId NEVER NULL ! used for loading term !
	 * @param gemetId GEMET ID of term. Ignored for loading term, but set when creating/updating term !
	 * @return persisted SearchtermSns (with Id)
	 */
	SearchtermSns loadOrCreate(String snsId, String gemetId);
}
