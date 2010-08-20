package de.ingrid.mdek.xml.importer;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;


/**
 * Interface for callbacks used by importer.
 */
public interface IImporterCallback {

	/**
	 * Write the given object meaning update existing object or create new one.
	 * @param objDoc object details in doc
	 * @param userUuid calling user
	 */
	void writeObject(IngridDocument objDoc, String userUuid);

	/**
	 * Write the given address meaning update existing address or create new one.
	 * @param addrDoc address details in doc
	 * @param userUuid calling user
	 */
	void writeAddress(IngridDocument addrDoc, String userUuid);

	/**
	 * Update basic information of import process.
	 * @param whichType address or object info ?
	 * @param numImported num imported entities so far
	 * @param totalNum total number to import
	 * @param userUuid calling user
	 */
	void writeImportInfo(IdcEntityType whichType, int numImportedObjects, int numImportedAddresses, int totalNumObjects, int totalNumAddresses,
			String userUuid);

	/**
	 * Add a message to protocol of the import process (can be downloaded by user).
	 * @param message The message to write
	 * @param userUuid calling user
	 */
	void writeImportInfoMessage(String message, String userUuid);
}
