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
package de.ingrid.mdek.services.persistence.db.dao.hibernate;


/**
 * Consts concerning access to full index.
 */
public interface IFullIndexAccess {

	static String IDX_SEPARATOR = "|";  

	static String IDX_NAME_FULLTEXT = "full";
	static String IDX_NAME_PARTIAL = "partial";
	static String IDX_NAME_THESAURUS = "thesaurus";
	static String IDX_NAME_GEOTHESAURUS = "geothesaurus";

	static String IDX_VALUE_IS_INSPIRE_RELEVANT = "inspirerelevant" + IDX_SEPARATOR + "inspire relevant";
	static String IDX_VALUE_IS_OPEN_DATA = "opendata" + IDX_SEPARATOR + "open data";
	static String IDX_VALUE_HAS_ATOM_DOWNLOAD = "atom download";
	static String IDX_VALUE_IS_ADV_COMPATIBLE = "adv compatible";
}
