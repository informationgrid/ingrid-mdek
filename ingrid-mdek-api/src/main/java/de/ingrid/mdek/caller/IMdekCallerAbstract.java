package de.ingrid.mdek.caller;

/**
 * Abstract interface for all mdek job callers implementing common methods and data types.
 */
public abstract interface IMdekCallerAbstract {

	/** How much data to fetch from requested entity ? */
	public enum FetchQuantity {
		/** request only data to be exported */
		EXPORT_ENTITY,
		/** request all data to be displayed in IGE */
		EDITOR_ENTITY,
	}
}
