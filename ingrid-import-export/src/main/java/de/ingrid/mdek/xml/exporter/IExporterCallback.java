/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.xml.exporter;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.utils.IngridDocument;


/**
 * Interface for callbacks used by exporter.
 */
public interface IExporterCallback {

	/**
	 * Get total number of objects to export.
	 * @param objUuids object uuids to export
	 * @param whichVersion which version should be exported:<br>
	 * 		WORKING_VERSION: count only objects where working copy exists (working version != published version)<br>
	 * 		PUBLISHED_VERSION: count only objects where published version exists<br>
	 * 		ALL_VERSIONS: count all objects, no matter in which version. If different object
	 * 			versions exist object is counted only once !
	 * @param includeSubnodes also export subnodes
	 * @param userUuid calling user
	 * @return total number to export
	 */
	int getTotalNumObjectsToExport(List<String> objUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid);

	/**
	 * Get object details.
	 * @param objUuid uuid of object
	 * @param whichVersion which object version should be fetched:<br>
	 * 		WORKING_VERSION: fetch details of working version<br>
	 * 		PUBLISHED_VERSION: fetch details of published version<br>
	 * 		ALL_VERSIONS: fetch details of working and published version IF THEY DIFFER !
	 * @param userUuid calling user
	 * @return list of docs encapsulating requested versions ! WORK_STATE in each doc determines which version.
	 * 		May be of length 0 if version not set.
	 * 		Or length 2 if ALL_VERSIONS requested and working version differs from published version.
	 * 		Then working version is delivered first !
	 */
	List<IngridDocument> getObjectDetails(String objUuid,
			IdcEntityVersion whichVersion,
			String userUuid);

	/**
	 * Get uuids of sub objects (only next level)
	 * @param parentUuid uuid of parent
	 * @param whichVersion which sub objects should be fetched:<br>
	 * 		WORKING_VERSION: fetch only sub objects who have a working copy (working version != published version)<br>
	 * 		PUBLISHED_VERSION: fetch only sub objects who have a published version<br>
	 * 		ALL_VERSIONS: fetch all sub objects, no matter which versions they have.
	 * @param userUuid calling user
	 * @return list of uuids or empty list
	 */
	List<String> getSubObjects(String parentUuid,
			IdcEntityVersion whichVersion,
			String userUuid);

	/**
	 * Get total number of addresses to export.
	 * @param addrUuids address uuids to export
	 * @param whichVersion which version should be exported:<br>
	 * 		WORKING_VERSION: count only addresses where working copy exists (working version != published version)<br>
	 * 		PUBLISHED_VERSION: count only addresses where published version exists<br>
	 * 		ALL_VERSIONS: count all addresses, no matter in which version. If different address
	 * 			versions exist address is counted only once !
	 * @param includeSubnodes also export subnodes
	 * @param userUuid calling user
	 * @return total number to export
	 */
	int getTotalNumAddressesToExport(List<String> addrUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid);

	/**
	 * Get address details.
	 * @param addrUuid uuid of address
	 * @param whichVersion which address version should be fetched:<br>
	 * 		WORKING_VERSION: fetch details of working version<br>
	 * 		PUBLISHED_VERSION: fetch details of published version<br>
	 * 		ALL_VERSIONS: fetch details of working and published version IF THEY DIFFER !
	 * @param userUuid calling user
	 * @return list of docs encapsulating requested versions ! WORK_STATE in each doc determines which version.
	 * 		May be of length 0 if version not set.
	 * 		Or length 2 if ALL_VERSIONS requested and working version differs from published version.
	 * 		Then working version is delivered first !
	 */
	List<IngridDocument> getAddressDetails(String addrUuid,
			IdcEntityVersion whichVersion,
			String userUuid);

	/**
	 * Get uuids of sub addresses (only next level)
	 * @param parentUuid uuid of parent
	 * @param whichVersion which sub addresses should be fetched:<br>
	 * 		WORKING_VERSION: fetch only sub addresses who have a working copy (working version != published version)<br>
	 * 		PUBLISHED_VERSION: fetch only sub addresses who have a published version<br>
	 * 		ALL_VERSIONS: fetch all sub addresses, no matter which versions they have.
	 * @param userUuid calling user
	 * @return list of uuids or empty list
	 */
	List<String> getSubAddresses(String parentUuid,
			IdcEntityVersion whichVersion,
			String userUuid);

	/**
	 * Update basic information of export process.
	 * @param whichType address or object info ?
	 * @param numExported num exported entities so far
	 * @param totalNum total number to export
	 * @param userUuid calling user
	 */
	void writeExportInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid);

	/**
	 * Add a message to message information of export process.
	 * @param newMessage message to add
	 * @param userUuid calling user
	 */
	void writeExportInfoMessage(String newMessage, String userUuid);
}
