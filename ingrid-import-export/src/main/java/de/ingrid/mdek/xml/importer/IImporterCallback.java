package de.ingrid.mdek.xml.importer;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;


/**
 * Interface for callbacks used by importer.
 */
public interface IImporterCallback {

	/**
	 * Write the given object meaning update existing object or create new one.
	 * @param objDocs object details of all instances of obj.
	 * 		If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz".
	 * @param userUuid calling user
	 */
	void writeObject(List<IngridDocument> objDocs, String userUuid);

	/**
	 * Write the given address meaning update existing address or create new one.
	 * @param addrDocs address details of all instances of addr.
	 * 		If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz".
	 * @param userUuid calling user
	 */
	void writeAddress(List<IngridDocument> addrDocs, String userUuid);

	/**
	 * Update basic information of import process.
	 * @param whichType address or object info ?
	 * @param numImported num imported entities so far
	 * @param totalNum total number to import
	 * @param userUuid calling user
	 */
	void writeImportInfo(IdcEntityType whichType, int numImported, int totalNum, String userUuid);

	/**
	 * Add a message to protocol of the import process (can be downloaded by user).
	 * @param message The message to write
	 * @param userUuid calling user
	 */
	void writeImportInfoMessage(String message, String userUuid);
}
