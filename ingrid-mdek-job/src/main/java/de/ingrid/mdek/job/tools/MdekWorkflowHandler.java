package de.ingrid.mdek.job.tools;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;
import de.ingrid.utils.IngridDocument;


/**
 * Handles permission checks.
 */
public class MdekWorkflowHandler {

	private static final Logger LOG = Logger.getLogger(MdekWorkflowHandler.class);
	
	private IPermissionService permService;
	private MdekCatalogService catalogService;

	private IObjectNodeDao daoObjectNode;
	private IAddressNodeDao daoAddressNode;

	private static MdekWorkflowHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekWorkflowHandler getInstance(IPermissionService permissionService,
			DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekWorkflowHandler(permissionService, daoFactory);
	      }
		return myInstance;
	}

	private MdekWorkflowHandler(IPermissionService permissionService, DaoFactory daoFactory) {
		this.permService = permissionService;
		catalogService = MdekCatalogService.getInstance(daoFactory);

		daoObjectNode = daoFactory.getObjectNodeDao();
		daoAddressNode = daoFactory.getAddressNodeDao();
	}

	/**
	 * Checks whether user has permission to access the given object concerning its workflow state.
	 * e.g. when object is in state "Q" user has to have "QA" permission !<br>
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW NOT ENABLED !
	 */
	public boolean hasWorkflowPermissionForObject(String objUuid, String userAddrUuid) {
		// ok if workflow disabled 
		if (!catalogService.isWorkflowActivated()) {
			return true;
		}
		
		// ok if new object (not persisted yet) 
		ObjectNode oNode = null;
		if (objUuid != null) {
			oNode = daoObjectNode.loadByUuid(objUuid);			
		}
		if (oNode == null) {
			return true;			
		}

		// check whether user is allowed to edit object in workflow !
		WorkState oWorkState = EnumUtil.mapDatabaseToEnumConst(WorkState.class, 
				oNode.getT01ObjectWork().getWorkState());
		if (oWorkState == WorkState.QS_UEBERWIESEN) {
			if (!hasQAPermission(userAddrUuid)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Checks whether user has permission to access the given address concerning its workflow state.
	 * e.g. when object is in state "Q" user has to have "QA" permission !<br>
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW NOT ENABLED !
	 */
	public boolean hasWorkflowPermissionForAddress(String addrUuid, String userAddrUuid) {
		// ok if workflow disabled 
		if (!catalogService.isWorkflowActivated()) {
			return true;
		}
		
		// ok if new address (not persisted yet) 
		AddressNode aNode = null;
		if (addrUuid != null) {
			aNode = daoAddressNode.loadByUuid(addrUuid);			
		}
		if (aNode == null) {
			return true;			
		}

		// check whether user is allowed to edit address in workflow !
		WorkState aWorkState = EnumUtil.mapDatabaseToEnumConst(WorkState.class, 
				aNode.getT02AddressWork().getWorkState());
		if (aWorkState == WorkState.QS_UEBERWIESEN) {
			if (!hasQAPermission(userAddrUuid)) {
				return false;
			}
		}
		
		return true;
	}

	/** Checks whether user has permission editing given object concerning workflow AND THROW EXCEPTION IF NOT ! */
	public void checkWorkflowPermissionForObject(String objUuid, String userAddrUuid) {
		if (!hasWorkflowPermissionForObject(objUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/** Checks whether user has permission editing given address concerning workflow AND THROW EXCEPTION IF NOT ! */
	public void checkWorkflowPermissionForAddress(String addrUuid, String userAddrUuid) {
		if (!hasWorkflowPermissionForAddress(addrUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/** Check "QA" Permission of given user and return "yes"/"no" ! */
	private boolean hasQAPermission(String userAddrUuid) {
		return permService.hasUserPermission(userAddrUuid, PermissionFactory.getPermissionTemplateQA());			
	}

	/** Process the work state in the given client side representation of an entity which should be stored. */
	public void processWorkStateOnStore(IngridDocument entityDoc) {
		WorkState givenState = EnumUtil.mapDatabaseToEnumConst(WorkState.class, 
				entityDoc.getString(MdekKeys.WORK_STATE));

		// keep QA state unchanged
		if (givenState == WorkState.QS_RUECKUEBERWIESEN ||
			givenState == WorkState.QS_UEBERWIESEN) {
			return;
		}

		entityDoc.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
	}

	/** Process the work state in the given client side representation of an entity which should be published. */
	public void processWorkStateOnPublish(IngridDocument entityDoc) {
		entityDoc.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());
	}

	/** Process the work state in the given bean (Object/Address) which is the result of a copy operation. */
	public void processWorkStateOnCopy(IEntity entity) {
		Class clazz = entity.getClass();

		if (T01Object.class.isAssignableFrom(clazz)) {
			((T01Object)entity).setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());
		} else if (T02Address.class.isAssignableFrom(clazz)) {
			((T02Address)entity).setWorkState(WorkState.IN_BEARBEITUNG.getDbValue());
		}
	}
}
