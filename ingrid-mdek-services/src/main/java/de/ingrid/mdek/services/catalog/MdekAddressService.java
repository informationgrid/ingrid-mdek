package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.List;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekFullIndexHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.mdek.services.utils.UuidGenerator;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates access to address entity data.
 */
public class MdekAddressService {

	private IAddressNodeDao daoAddressNode;
	private IT02AddressDao daoT02Address;

	private MdekTreePathHandler pathHandler;
	private MdekFullIndexHandler fullIndexHandler;
	private MdekPermissionHandler permissionHandler;

	protected BeanToDocMapper beanToDocMapper;
	protected DocToBeanMapper docToBeanMapper;

	private static MdekAddressService myInstance;

	/** Get The Singleton */
	public static synchronized MdekAddressService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekAddressService(daoFactory, permissionService);
	      }
		return myInstance;
	}

	private MdekAddressService(DaoFactory daoFactory, IPermissionService permissionService) {
		daoAddressNode = daoFactory.getAddressNodeDao();
		daoT02Address = daoFactory.getT02AddressDao();

		pathHandler = MdekTreePathHandler.getInstance(daoFactory);
		fullIndexHandler = MdekFullIndexHandler.getInstance(daoFactory);
		permissionHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);

		beanToDocMapper = BeanToDocMapper.getInstance(daoFactory);
		docToBeanMapper = DocToBeanMapper.getInstance(daoFactory);
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

	
	/** Returns true if given node has children. False if no children. */
	public boolean hasChildren(AddressNode node) {
    	return (node.getAddressNodeChildren().size() > 0) ? true : false;
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

	/**
	 * Store WORKING COPY of the address represented by the passed doc.<br>
	 * NOTICE: PARENT_UUID has to be set in doc when new address !
	 * @param aDocIn doc representing address
	 * @param userId user performing operation, will be set as mod-user
	 * @param checkPermissions true=check whether user has write permission<br>
	 * 		false=NO check on write permission ! working copy will be stored !
	 * @return uuid of stored address, will be generated if new address (no uuid passed in doc)
	 */
	public String storeWorkingCopy(IngridDocument aDocIn, String userId, boolean checkPermissions) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());

		String uuid = (String) aDocIn.get(MdekKeys.UUID);
		boolean isNewAddress = (uuid == null) ? true : false;
		// parentUuid only passed if new address !?
		String parentUuid = (String) aDocIn.get(MdekKeys.PARENT_UUID);

		// set common data to transfer to working copy !
		aDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		beanToDocMapper.mapModUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);
		// set current user as responsible user if not set !
		String respUserUuid = docToBeanMapper.extractResponsibleUserUuid(aDocIn);
		if (respUserUuid == null) {
			beanToDocMapper.mapResponsibleUser(userId, aDocIn, MappingQuantity.INITIAL_ENTITY);				
		}
		// set correct work state if necessary
		String givenWorkState = (String) aDocIn.get(MdekKeys.WORK_STATE);
		if (givenWorkState == null || givenWorkState.equals(WorkState.VEROEFFENTLICHT.getDbValue())) {
			aDocIn.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
		}

		// check permissions !
		if (checkPermissions) {
			permissionHandler.checkPermissionsForStoreAddress(uuid, parentUuid, userId);
		}

		if (isNewAddress) {
			// create new uuid
			uuid = UuidGenerator.getInstance().generateUuid();
			aDocIn.put(MdekKeys.UUID, uuid);
			// NOTICE: don't add further data, is done below when checking working copy !
		}
		
		// load node
		AddressNode aNode = daoAddressNode.getAddrDetails(uuid);
		if (aNode == null) {
			// create new node, also take care of correct tree path in node
			aNode = docToBeanMapper.mapAddressNode(aDocIn, new AddressNode());			
			pathHandler.setTreePath(aNode, parentUuid);
		}
		
		// get/create working copy
		// if no working copy then new address -> address and addressNode have to be created
		if (!hasWorkingCopy(aNode)) {
			// no working copy yet, create new address/node with BASIC data

			// set some missing data which may not be passed from client.
			// set from published version if existent
			T02Address aPub = aNode.getT02AddressPublished();
			if (aPub != null) {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, aPub.getCreateTime());				
			} else {
				aDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			}
			T02Address aWork = docToBeanMapper.mapT02Address(aDocIn, new T02Address(), MappingQuantity.BASIC_ENTITY);
			 // save it to generate id needed for mapping
			daoT02Address.makePersistent(aWork);
			
			// create/update node
			aNode.setAddrId(aWork.getId());
			aNode.setT02AddressWork(aWork);
			daoAddressNode.makePersistent(aNode);
		}

		// transfer detailed new data
		T02Address aWork = aNode.getT02AddressWork();
		docToBeanMapper.mapT02Address(aDocIn, aWork, MappingQuantity.DETAIL_ENTITY);

		// PERFORM CHECKS BEFORE STORING/COMMITTING !!!
		checkAddressNodeForStore(aNode);

		// store when ok
		daoT02Address.makePersistent(aWork);

		// UPDATE FULL INDEX !!!
		fullIndexHandler.updateAddressIndex(aNode);

		// grant write tree permission if not set yet (e.g. new root node)
		if (isNewAddress) {
			permissionHandler.grantTreePermissionForAddress(aNode.getAddrUuid(), userId);
		}
		
		return uuid;
	}

	/** Checks whether given Address has a working copy !
	 * @param node address to check represented by node !
	 * @return true=address has different working copy or not published yet<br>
	 * 	false=no working version, same as published version !
	 */
	public boolean hasWorkingCopy(AddressNode node) {
		Long workId = node.getAddrId(); 
		Long pubId = node.getAddrIdPublished(); 
		if (workId == null || workId.equals(pubId)) {
			return false;
		}
		
		return true;
	}

	/** Check whether passed node is valid for storing !
	 * (e.g. check free address conditions ...). Throws MdekException if not valid.
	 */
	private void checkAddressNodeForStore(AddressNode node)
	{
		if (node == null) {
			throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
		}

		String parentUuid = node.getFkAddrUuid();
		T02Address aWork = node.getT02AddressWork();
		AddressType aType = EnumUtil.mapDatabaseToEnumConst(AddressType.class, aWork.getAdrType());
		if (aType == AddressType.FREI) {
			if (parentUuid != null) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_PARENT));
			}
			if (hasChildren(node)) {
				throw new MdekException(new MdekError(MdekErrorType.FREE_ADDRESS_WITH_SUBTREE));
			}
		}
	}
}
