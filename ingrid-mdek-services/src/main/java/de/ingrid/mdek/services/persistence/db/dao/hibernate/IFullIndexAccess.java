package de.ingrid.mdek.services.persistence.db.dao.hibernate;


/**
 * Consts concerning access to full index.
 */
public interface IFullIndexAccess {

	static String IDX_SEPARATOR = "|";  

	static String IDX_NAME_FULLTEXT = "full";
	static String IDX_NAME_PARTIAL = "partial";
	static String IDX_NAME_THESAURUS = "thesaurus";
	static String IDX_NAME_GEOTHESAURUS = "geothesaurus";

	static String IDX_VALUE_IS_INSPIRE_RELEVANT = "inspirerelevant" + IDX_SEPARATOR + "inspire relevant";
	static String IDX_VALUE_IS_OPEN_DATA = "opendata" + IDX_SEPARATOR + "open data";
	static String IDX_VALUE_HAS_ATOM_DOWNLOAD = "atom download";
}
