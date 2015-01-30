/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import java.util.List;

import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T01Object;



/**
 * Business DAO operations related to the <tt>SpatialRefValue</tt> entity.
 * 
 * @author Martin
 */
public interface ISpatialRefValueDao
	extends IGenericDao<SpatialRefValue> {

	/** Load SpatialRefValue according to given values. If not found create AND save it !
	 * NOTICE: map missing data (BBox etc.) after loading/creating bean ! 
	 * @param type type of spatial ref NEVER null
	 * @param nameValue term name. always pass (used for loading FREE spatial ref; set when creating new one !) 
	 * @param nameKey term key. always pass (used for loading FREE spatial ref; set when creating new one !)
	 * @param spRefSns according bean (or null if NO SNS spatial ref)
	 * @param objectId connected to this object (used for loading FREE spatial ref, else pass null).
	 * @return persisted SpatialRefValue (with Id)
	 */
	SpatialRefValue loadOrCreate(String type, String nameValue, Integer nameKey,
		SpatialRefSns spRefSns, Long objectId);

	/** Load SpatialRefValues according to given parameters. Passed type determines how to
	 * fetch.
	 * @param name pass if type FREI
	 * @param snsId pass if type GEO_THESAURUS
	 * @return list of SpatialRefValues or empty list if not found
	 */
	List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType type, String name, String snsId);

	/** get REFERENCED NON EXPIRED spatial reference values of given type(s).
	 * NOTICE: only returns spatial ref values REFERENCED by Objects/Addresses (e.g. NOT unused ones) !
	 * @param types pass types to fetch. Pass null or empty array if all types !
	 * @return list of spatial reference values.
	 * 	NOTICE: NOT distinct, e.g. same free spatial ref value may exist in multiple DIFFERENT SpatialRefValue !
	 */
	List<SpatialRefValue> getSpatialRefValues(SpatialReferenceType[] types);

	/** Return number of objects referencing the given spatial ref.*/
	long countObjectsOfSpatialRefValue(long idSpatialRefValue);

	/** Get all referencing objects of given spatial ref value */
	List<T01Object> getObjectsOfSpatialRefValue(long idSpatialRefValue);
	/** Get DISTINCT ids of objects referencing given spatial ref value */
	List<Long> getObjectIdsOfSpatialRefValue(long idSpatialRefValue);
	/** Get all spatial references of given spatial ref value */
	List<SpatialReference> getSpatialReferences(long searchtermValueId);
}
