/*
 * **************************************************-
 * ingrid-mdek-api
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
package de.ingrid.mdek;

/**
 * Helper class encapsulating version of IGC needed (Schema).<br/>
 * NOTICE: Subclassed by import-export jar defining Version of Import/Export Format and mapper class ...
 */
public class Versioning {
	/** The "major" version of the IGC catalog needed !<br>
	 * The version in the catalog does not have to be equal to this version but has to "start" with this version !<br>
	 * <b>E.g. if version set here is 3.6.2 and catalog version is 3.6.2_a the versions match.
	 * But strictly set to 3.6.2_a here if you need that version due to changes in the data structure !</b><br>
     * <b>NOTICE</b>: Also IGE frontend checks this version against IGE iPlug when this mdek-api is used in frontend (portal).
     * BUT also only checks if IGE iPlug version starts with this version ! */
	public static final String NEEDED_IGC_VERSION = "4.0.4";
    /** This is the version the catalog has to be updated to when the iPlug is started !<br>
     * This normally is the most current version of the catalog (the strategy of the udk importer executed). */
    public static final String UPDATE_TO_IGC_VERSION = "4.0.4_c";

	/** Key for fetching IGC Version from backend from SysGenericKey table */
	public static final String BACKEND_IGC_VERSION_KEY = "IDC_VERSION";
}
