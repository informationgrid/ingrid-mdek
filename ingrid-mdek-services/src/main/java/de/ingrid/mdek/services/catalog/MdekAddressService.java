package de.ingrid.mdek.services.catalog;

import java.util.List;

import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;

/**
 * Encapsulates access to address entity data.
 */
public class MdekAddressService {

	private IAddressNodeDao daoAddressNode;

	private static MdekAddressService myInstance;

	/** Get The Singleton */
	public static synchronized MdekAddressService getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekAddressService(daoFactory);
	      }
		return myInstance;
	}

	private MdekAddressService(DaoFactory daoFactory) {
		daoAddressNode = daoFactory.getAddressNodeDao();
	}

	/** Load address NODE with given uuid. Also prefetch concrete address instance in node if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param uuid address uuid
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @return node or null if not found
	 */
	public AddressNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		return daoAddressNode.loadByUuid(uuid, whichEntityVersion);
	}

	/**
	 * Fetches sub nodes (next level) of parent with given uuid. 
	 * Also prefetch concrete address instance in nodes if requested.
	 * <br>NOTICE: transaction must be active !
	 * @param parentUuid uuid of parent
	 * @param whichEntityVersion which address Version to prefetch in node, pass null IF ONLY NODE SHOULD BE LOADED 
	 * @param fetchSubNodesChildren also fetch children in fetched subnodes to determine whether leaf or not ?
	 * @return
	 */
	public List<AddressNode> getSubAddresses(String parentUuid, 
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		List<AddressNode> aNs = daoAddressNode.getSubAddresses(
				parentUuid, whichEntityVersion, fetchSubNodesChildren);
		return aNs;
	}

}
