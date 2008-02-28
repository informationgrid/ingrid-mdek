package de.ingrid.mdek;


/**
 * Complete interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller extends
	IMdekCallerCommon, 
	IMdekCallerObject,
	IMdekCallerAddress,
	IMdekCallerQuery {
}
