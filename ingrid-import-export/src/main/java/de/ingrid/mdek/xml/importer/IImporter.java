/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer;

import java.util.List;


/**
 * Interface for calling importer (e.g. from Job).
 */
public interface IImporter {

	/**
	 * Import the given data (import/export format) FROM SINGLE FILE (no list of data) ! 
	 * Updates existing or create new entities via callbacks.<br>
	 * When importing multiple files then call this one for every "file" and
	 * call countEntities initially for counting totals of all files.
	 * @param importData entities to import in import/export format
	 * @param userUuid calling user, needed for callbacks
	 */
	void importEntities(byte[] importData, String userUuid);

	/**
	 * Count entities in import data and set internal totals accordingly.
	 * Call this before any call to importEntities when importing multiple files !
	 * @param importData list of data (files) containing entities to import in import/export format
	 * @param userUuid calling user, needed for callbacks
	 */
	void countEntities(List<byte[]> importData, String userUuid);
}
