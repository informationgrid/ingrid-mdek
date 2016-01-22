/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
	/** Version of IGC catalogue needed. 
	 * NOTICE: IGE frontend checks this version against backend when this mdek-api is used in frontend (portal). */
	public static final String NEEDED_IGC_VERSION = "3.6.1.1";

	/** Key for fetching IGC Version from backend from SysGenericKey table */
	public static final String BACKEND_IGC_VERSION_KEY = "IDC_VERSION";
}
