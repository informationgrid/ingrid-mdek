/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.utils;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;
import de.ingrid.utils.IngridDocument;


/**
 * Handles permission checks concerning Workflow (e.g. is user allowed to edit entity in special work state).
 */
public class MdekWorkflowHandler {

	private static final Logger LOG = LogManager.getLogger(MdekWorkflowHandler.class);
	
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

	/** Is Workflow enabled ? */
	private boolean isWorkflowEnabled() {
		return catalogService.isWorkflowEnabled();
	}

	/**
	 * Checks whether user has permission to access the given object concerning its workflow state.
	 * e.g. when object is in state "Q" user has to have "QA" permission !<br>
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW DISABLED !
	 */
	public boolean hasWorkflowPermissionForObject(String objUuid, String userAddrUuid) {
		// ok if workflow disabled 
		if (!isWorkflowEnabled()) {
			return true;
		}
		
		// ok if new object (not persisted yet) 
		ObjectNode oNode = null;
		if (objUuid != null) {
			oNode = daoObjectNode.loadByUuid(objUuid, IdcEntityVersion.WORKING_VERSION);			
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
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW DISABLED !
	 */
	public boolean hasWorkflowPermissionForAddress(String addrUuid, String userAddrUuid) {
		// ok if workflow disabled 
		if (!isWorkflowEnabled()) {
			return true;
		}
		
		// ok if new address (not persisted yet) 
		AddressNode aNode = null;
		if (addrUuid != null) {
			aNode = daoAddressNode.loadByUuid(addrUuid, IdcEntityVersion.WORKING_VERSION);			
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

	/** Checks whether user has "QA" permission (ONLY if workflow enabled) AND THROW EXCEPTION IF NOT ! */
	public void checkQAPermission(String userAddrUuid) {
		// if workflow disabled we don't care ! 
		if (!isWorkflowEnabled()) {
			return;
		}

		if (!hasQAPermission(userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_WORKFLOW_PERMISSION_ON_ENTITY));
		}		
	}

	/** Check "QA" Permission of given user (ONLY if workflow enabled) and return "yes"/"no" !
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW DISABLED !
	 */
	public boolean hasQAPermission(String userAddrUuid) {
		// ok if workflow disabled 
		if (!isWorkflowEnabled()) {
			return true;
		}

		return permService.hasUserPermission(userAddrUuid, PermissionFactory.getPermissionTemplateQA(), null);			
	}

	/** Process the given client side representation of an entity according to the operation. */
	public void processDocOnStore(IngridDocument entityDoc) {
		WorkState givenState = EnumUtil.mapDatabaseToEnumConst(WorkState.class, 
				entityDoc.getString(MdekKeys.WORK_STATE));

		// keep QA state unchanged
		if (givenState == WorkState.QS_RUECKUEBERWIESEN ||
			givenState == WorkState.QS_UEBERWIESEN) {
			return;
		}

		entityDoc.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
	}

	/** Process the given client side representation of an entity according to the operation. */
	public void processDocOnAssignToQA(IngridDocument entityDoc, String userAddrUuid) {
		entityDoc.put(MdekKeys.WORK_STATE, WorkState.QS_UEBERWIESEN.getDbValue());
		entityDoc.put(MdekKeys.ASSIGNER_UUID, userAddrUuid);
		entityDoc.put(MdekKeys.ASSIGN_TIME, MdekUtils.dateToTimestamp(new Date()));
	}

	/** Process the given client side representation of an entity according to the operation. */
	public void processDocOnReassignToAuthor(IngridDocument entityDoc, String userAddrUuid) {
		entityDoc.put(MdekKeys.WORK_STATE, WorkState.QS_RUECKUEBERWIESEN.getDbValue());
		entityDoc.put(MdekKeys.REASSIGNER_UUID, userAddrUuid);
		entityDoc.put(MdekKeys.REASSIGN_TIME, MdekUtils.dateToTimestamp(new Date()));
		
		// cancel "mark deleted" (may be set)
		entityDoc.put(MdekKeys.MARK_DELETED, MdekUtils.NO);
	}

	/** Process the given client side representation of an entity according to the operation. */
	public void processDocOnPublish(IngridDocument entityDoc) {
		entityDoc.put(MdekKeys.WORK_STATE, WorkState.VEROEFFENTLICHT.getDbValue());

		// cancel "mark deleted", is undone with this publish operation
		entityDoc.put(MdekKeys.MARK_DELETED, MdekUtils.NO);
		// cancel "expired", object is updated with this publish operation
		entityDoc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.INITIAL.getDbValue());
	}

	/** Process the given client side representation of an entity according to the operation. */
	public void processDocOnCopy(IngridDocument entityDoc) {
		// copied object/address is always in work state IN_BEARBEITUNG and NEVER marked deleted

		entityDoc.put(MdekKeys.WORK_STATE, WorkState.IN_BEARBEITUNG.getDbValue());
		entityDoc.put(MdekKeys.MARK_DELETED, MdekUtils.NO);
	}
}
