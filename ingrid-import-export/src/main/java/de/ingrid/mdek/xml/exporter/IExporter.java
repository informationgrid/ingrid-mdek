/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.exporter;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityVersion;


/**
 * Interface for calling exporter (e.g. from Job).
 */
public interface IExporter {

	/**
	 * Export given object(s) to XML file.
	 * @param objUuids uuids of objects to export
	 * @param whichVersion which version of objects should be exported:<br>
	 * 		WORKING_VERSION: only working copy, if no working copy, object is not exported !
	 * 			!!! Sub Objects with working copy underneath objects with NO working copy are NOT exported !!!<br>
	 * 		PUBLISHED_VERSION: only published version, if no published version, object is not exported !<br>
	 * 		ALL_VERSIONS: all versions of objects, so working copy (if exists) and published version (if exists) are exported !
	 * 			If no working copy (working version == published version) then only published one is exported ! 
	 * @param includeSubnodes also export subnodes ? true=yes
	 * @param userUuid calling user, needed for callbacks
	 * @return objects in export format
	 */
	byte[] exportObjects(List<String> objUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid);

	/**
	 * Export given address(s) to XML file.
	 * @param addrUuids uuids of addresses to export
	 * @param whichVersion which version of addresses should be exported:<br>
	 * 		WORKING_VERSION: only working copy, if no working copy, address is not exported !
	 * 			!!! Sub addresses with working copy underneath addresses with NO working copy are NOT exported !!!<br>
	 * 		PUBLISHED_VERSION: only published version, if no published version, address is not exported !<br>
	 * 		ALL_VERSIONS: all versions of addresses, so working copy (if exists) and published version (if exists) are exported !
	 * 			If no working copy (working version == published version) then only published one is exported ! 
	 * @param includeSubnodes also export subnodes ? true=yes
	 * @param userUuid calling user, needed for callbacks
	 * @return objects in export format
	 */
	byte[] exportAddresses(List<String> addrUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid);
}
