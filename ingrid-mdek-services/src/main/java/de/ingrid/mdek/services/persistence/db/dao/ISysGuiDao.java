package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SysGui;



/**
 * Business DAO operations related to the <tt>SysGui</tt> entity.
 * 
 * @author Martin
 */
public interface ISysGuiDao
	extends IGenericDao<SysGui> {

	/** Get sysgui elements with given ids AS LIST OF BEANS. PASS null if all sysguis ! */
	List<SysGui> getSysGuis(String[] guiIds);
}
