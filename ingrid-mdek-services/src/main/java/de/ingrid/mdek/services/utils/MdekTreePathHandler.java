package de.ingrid.mdek.services.utils;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;


/**
 * Handles management of treePath data in object/address nodes 
 */
public class MdekTreePathHandler {

	private static final Logger LOG = Logger.getLogger(MdekTreePathHandler.class);

	static private String NODE_SEPARATOR = "|";  

	private IObjectNodeDao daoObjectNode;
	private IAddressNodeDao daoAddressNode;

	private static MdekTreePathHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekTreePathHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekTreePathHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekTreePathHandler(DaoFactory daoFactory) {
		daoObjectNode = daoFactory.getObjectNodeDao();
		daoAddressNode = daoFactory.getAddressNodeDao();
	}

	/** Set tree path in node according to passed parent uuid. Returns new path. */
	public String setTreePath(ObjectNode node, String newParentUuid) {
		ObjectNode newParentNode = null;
		if (newParentUuid != null) {
			newParentNode = daoObjectNode.loadByUuid(newParentUuid, null);
			if (newParentNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
		}

		return setTreePath(node, newParentNode);
	}

	/** Set tree path in node according to passed parent uuid. Returns new path. */
	public String setTreePath(AddressNode node, String newParentUuid) {
		AddressNode newParentNode = null;
		if (newParentUuid != null) {
			newParentNode = daoAddressNode.loadByUuid(newParentUuid, null);
			if (newParentNode == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
		}

		return setTreePath(node, newParentNode);
	}

	/** Set tree path in node according to passed parent node. Returns new path. */
	public String setTreePath(ObjectNode node, ObjectNode newParentNode) {
		// NOTICE: node itself isn't part of its path ! top nodes have "" path !
		String path = "";
		if (newParentNode != null) {
			String parentPath = newParentNode.getTreePath();
			// parent path does NOT include parent node, add it
			path = addNodeToPath(parentPath, newParentNode.getObjUuid());
		}
		
		node.setTreePath(path);
		return path;
	}

	/** Set tree path in node according to passed parent node. Returns new path. */
	public String setTreePath(AddressNode node, AddressNode newParentNode) {
		// NOTICE: node itself isn't part of its path ! top nodes have "" path !
		String path = "";
		if (newParentNode != null) {
			String parentPath = newParentNode.getTreePath();
			// parent path does NOT include parent node, add it
			path = addNodeToPath(parentPath, newParentNode.getAddrUuid());
		}
		
		node.setTreePath(path);
		return path;
	}

	/** Updates the path of the given node after move operation. Pass the path to the old root and the path to the new root. */
	public String updateTreePathAfterMove(ObjectNode node, String rootPathBeforeMove, String rootPathAfterMove) {
		String path = "";
		if (rootPathBeforeMove.length() == 0) {
			// if old path is empty we have a former top node, add new root path at front
			path = rootPathAfterMove + node.getTreePath();
		} else {
			// else replace old root path with new root path
			path = node.getTreePath().replace(rootPathBeforeMove, rootPathAfterMove);
		}

		node.setTreePath(path);

		return path;
	}

	/** Updates the path of the given node after move operation. Pass the path to the old root and the path to the new root. */
	public String updateTreePathAfterMove(AddressNode node, String rootPathBeforeMove, String rootPathAfterMove) {
		String path = "";
		if (rootPathBeforeMove.length() == 0) {
			// if old path is empty we have a former top node, add new root path at front
			path = rootPathAfterMove + node.getTreePath();
		} else {
			// else replace old root path with new root path
			path = node.getTreePath().replace(rootPathBeforeMove, rootPathAfterMove);
		}

		node.setTreePath(path);

		return path;
	}

	/** Adds the given nodeUuid at the end of the given path and returns new path. */
	public String addNodeToPath(String path, String nodeUuid) {
		return path + translateToTreePathUuid(nodeUuid);
	}

	/** Adds the given nodeUuid at the end of the given path and returns new path. */
	static public String translateToTreePathUuid(String nodeUuid) {
		return NODE_SEPARATOR + nodeUuid + NODE_SEPARATOR;
	}
}
