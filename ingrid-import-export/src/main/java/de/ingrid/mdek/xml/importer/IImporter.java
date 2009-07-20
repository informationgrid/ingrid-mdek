package de.ingrid.mdek.xml.importer;


/**
 * Interface for calling importer (e.g. from Job).
 */
public interface IImporter {

	/**
	 * Import the given data (import/export format) and update existing or create new entities via callbacks.
	 * @param importData entities to import in import/export format
	 * @param userUuid calling user, needed for callbacks
	 */
	void importEntities(byte[] importData, String userUuid);
}
