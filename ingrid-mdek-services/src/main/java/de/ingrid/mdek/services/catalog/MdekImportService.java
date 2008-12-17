package de.ingrid.mdek.services.catalog;

import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for importer.
 */
public class MdekImportService implements IImporterCallback {

	private static MdekImportService myInstance;

	private MdekObjectService objectService;
	private MdekAddressService addressService;

	/** Get The Singleton */
	public static synchronized MdekImportService getInstance(DaoFactory daoFactory,
			MdekObjectService objectService,
			MdekAddressService addressService) {
		if (myInstance == null) {
	        myInstance = new MdekImportService(daoFactory, objectService, addressService);
	      }
		return myInstance;
	}

	private MdekImportService(DaoFactory daoFactory,
			MdekObjectService objectService,
			MdekAddressService addressService) {
		this.objectService = objectService;
		this.addressService = addressService;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeObject(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeObject(IngridDocument objDoc,
			String defaultObjectParentUuid,
			boolean publishImmediately,
			String userUuid) {

		// TODO: check whether parent exists ! else set default !
		
		if (publishImmediately) {
			// TODO: check mandatory data

		} else {
			
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeAddress(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeAddress(IngridDocument addrDoc,
			String defaultAddressParentUuid,
			boolean publishImmediately,
			String userUuid) {

		// TODO: check whether parent exists ! else set default !
		
		if (publishImmediately) {
			// TODO: check mandatory data

		} else {
			
		}
	}
}
