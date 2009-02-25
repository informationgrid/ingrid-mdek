package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;



/**
 * Business DAO operations related to the <tt>SysGenericKey</tt> entity.
 * 
 * @author Martin
 */
public interface ISysGenericKeyDao
	extends IGenericDao<SysGenericKey> {

	/** Get generic keys of given names AS LIST OF BEANS. PASS null if all generic keys ! */
	List<SysGenericKey> getSysGenericKeys(String[] keyNames);
}
