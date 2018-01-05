/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;



/**
 * Business DAO operations related to the <tt>SpatialRefSns</tt> entity.
 * 
 * @author Martin
 */
public interface ISpatialRefSnsDao
	extends IGenericDao<SpatialRefSns> {

	/** Load SpatialRefSns with given snsId. Returns null if not found.	 */
	SpatialRefSns load(String snsId);

	/** Load SpatialRefSns with given snsId. If not found create AND save it ! */
	SpatialRefSns loadOrCreate(String snsId);
}
