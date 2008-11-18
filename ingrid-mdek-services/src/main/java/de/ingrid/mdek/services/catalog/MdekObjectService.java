package de.ingrid.mdek.services.catalog;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * Encapsulates access to object entity data.
 */
public class MdekObjectService {

	private IObjectNodeDao daoObjectNode;

	private static MdekObjectService myInstance;

	/** Get The Singleton */
	public static synchronized MdekObjectService getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekObjectService(daoFactory);
	      }
		return myInstance;
	}

	private MdekObjectService(DaoFactory daoFactory) {
		daoObjectNode = daoFactory.getObjectNodeDao();
	}

	/** Load object NODE with given uuid. Also prefetch concrete object instance in node if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param uuid object uuid
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		return daoObjectNode.loadByUuid(uuid, whichEntityVersion);
	}

	/**
	 * Fetches sub nodes (next level) of parent with given uuid. 
	 * Also prefetch concrete object instance in nodes if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which object Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	public List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		List<ObjectNode> oNs = daoObjectNode.getSubObjects(
				parentUuid, whichEntityVersion, fetchSubNodesChildren);
		return oNs;
	}
}
