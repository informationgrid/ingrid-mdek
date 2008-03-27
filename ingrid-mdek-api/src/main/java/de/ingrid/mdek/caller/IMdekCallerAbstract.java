package de.ingrid.mdek.caller;

/**
 * Abstract interface for all mdek job callers implementing common methods and data types.
 */
public abstract interface IMdekCallerAbstract {

	/** How much data to fetch from requested entity ? */
	// TODO implement other quantities of fetching object ?
	public enum Quantity {
		DETAIL_ENTITY // client: edit dialogue -> request maximum data
	}
}
