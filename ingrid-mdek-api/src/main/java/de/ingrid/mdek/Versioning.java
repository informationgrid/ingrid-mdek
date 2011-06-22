package de.ingrid.mdek;

/**
 * Helper class encapsulating version of IGC needed (Schema).<br/>
 * NOTICE: Subclassed by import-export jar defining Version of Import/Export Format and mapper class ...
 */
public class Versioning {
	/** Version of IGC catalogue needed (Schema) */
	public static final String NEEDED_IGC_VERSION = "3.0.1";

	/** Key for fetching IGC Version from backend from SysGenericKey table */
	public static final String BACKEND_IGC_VERSION_KEY = "IDC_VERSION";
}
