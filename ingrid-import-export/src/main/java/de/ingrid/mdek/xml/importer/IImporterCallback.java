/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;


/**
 * Interface for callbacks used by importer.
 */
public interface IImporterCallback {

	/**
	 * Write the given object meaning update existing object or create new one.
	 * @param objDocs object details of all instances of obj.
	 * 		If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz".
	 * @param userUuid calling user
	 */
	void writeObject(List<IngridDocument> objDocs, String userUuid);

	/**
	 * Write the given address meaning update existing address or create new one.
	 * @param addrDocs address details of all instances of addr.
	 * 		If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz".
	 * @param userUuid calling user
	 */
	void writeAddress(List<IngridDocument> addrDocs, String userUuid);

	/**
	 * Update basic information of import process.
	 * @param whichType address or object info ?
	 * @param numImported num imported entities so far
	 * @param totalNum total number to import
	 * @param userUuid calling user
	 */
	void writeImportInfo(IdcEntityType whichType, int numImported, int totalNum, String userUuid);

	/**
	 * Add a message to protocol of the import process (can be downloaded by user).
	 * @param message The message to write
	 * @param userUuid calling user
	 */
	void writeImportInfoMessage(String message, String userUuid);
}
