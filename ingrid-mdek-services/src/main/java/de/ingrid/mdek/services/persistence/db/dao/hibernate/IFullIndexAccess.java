package de.ingrid.mdek.services.persistence.db.dao.hibernate;


/**
 * Consts concerning access to full index.
 */
public interface IFullIndexAccess {

	static String IDX_SEPARATOR = "|";  

	static String IDX_NAME_FULLTEXT = "full";  

	static String IDX_NAME_PARTIAL = "partial";  
}
