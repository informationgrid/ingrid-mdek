package de.ingrid.mdek.job.tools;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;


/**
 * Encapsulates access to T03Catalogue.
 */
public class MdekCatalogHandler {

	private IGenericDao<IEntity> daoT03Catalogue;
	private T03Catalogue catalog = null;

	private static MdekCatalogHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekCatalogHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekCatalogHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekCatalogHandler(DaoFactory daoFactory) {
		daoT03Catalogue = daoFactory.getDao(T03Catalogue.class);
	}

	/** Get catalog. NOTICE: transaction must be active when called the first time ! */
	public T03Catalogue getCatalog() {
		// NEVER CACHE !!!!!! can be changed (name etc.) !!!
//		if (catalog == null) {
			catalog = (T03Catalogue) daoT03Catalogue.findFirst();
			if (catalog == null) {
				throw new MdekException(new MdekError(MdekErrorType.CATALOG_NOT_FOUND));
			}			
//		}

		return catalog;
	}

	/** Get language of catalog. NOTICE: transaction must be active when called the first time ! */
	public String getCatalogLanguage() {
		return getCatalog().getLanguageCode();
	}
}
