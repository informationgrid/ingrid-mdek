/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapperSecurity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.security.EntityPermission;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;
import de.ingrid.utils.IngridDocument;


/**
 * Handles permission checks concerning EDITING OF ENTITY (WRITE_SINGLE, WRITE_TREE).
 * Utilizes MdekWorkflowHandler for handling all workflow specific stuff (QUALITY_ASSURANCE). 
 */
public class MdekPermissionHandler {

	private static final Logger LOG = Logger.getLogger(MdekPermissionHandler.class);
	
	private IPermissionService permService;

	private MdekWorkflowHandler workflowHandler;

	protected IIdcUserDao daoIdcUser;

	protected DocToBeanMapperSecurity docToBeanMapperSecurity;

	private static MdekPermissionHandler myInstance;

	/** Different types of groups, e.g. used for granting permissions in special groups */
	public enum GroupType {
		ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION,
		ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_OBJECT,
		ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS,
		ALL_GROUPS
	}


	/** Get The Singleton */
	public static synchronized MdekPermissionHandler getInstance(IPermissionService permissionService,
			DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekPermissionHandler(permissionService, daoFactory);
	      }
		return myInstance;
	}

	private MdekPermissionHandler(IPermissionService permissionService, DaoFactory daoFactory) {
		this.permService = permissionService;

		workflowHandler = MdekWorkflowHandler.getInstance(permissionService, daoFactory);

		daoIdcUser = daoFactory.getIdcUserDao();

		docToBeanMapperSecurity = DocToBeanMapperSecurity.getInstance(daoFactory, permissionService);
	}

	/**
	 * Get permissions of user for an INITIAL object (not created yet !).
	 * NOTICE: Checks whether user has permissions to perform the STORE operation AND THROWS EXCEPTION IF NOT !
	 * @param initialObjDoc initial object data
	 * @param userUuid users address uuid
	 * @return list of permissions on initial object
	 */
	public List<Permission> getPermissionsForInitialObject(IngridDocument initialObjDoc, String userUuid) {

		// initial object uuid should always be null -> object not created yet !
		String objUuid = initialObjDoc.getString(MdekKeys.UUID);
		if (objUuid != null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}
		String parentUuid = initialObjDoc.getString(MdekKeys.PARENT_UUID);
		checkPermissionsForStoreObject(objUuid, parentUuid, userUuid);

		// user can store new object -> full access, we return "write-tree" permission
		List<Permission> perms = new ArrayList<Permission>();
		perms.add(PermissionFactory.getPermissionTemplateTree());
		
		return perms;
	}

	/**
	 * Get permissions of user for an INITIAL address (not created yet !).
	 * NOTICE: Checks whether user has permissions to perform the STORE operation AND THROWS EXCEPTION IF NOT !
	 * @param initialAddrDoc initial address data
	 * @param userUuid users address uuid
	 * @return list of permissions on initial address
	 */
	public List<Permission> getPermissionsForInitialAddress(IngridDocument initialAddrDoc, String userUuid) {

		// initial address uuid should always be null -> address not created yet !
		String addrUuid = initialAddrDoc.getString(MdekKeys.UUID);
		if (addrUuid != null) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}
		String parentUuid = initialAddrDoc.getString(MdekKeys.PARENT_UUID);
		checkPermissionsForStoreAddress(addrUuid, parentUuid, userUuid);

		// user can store new address -> full access, we return "write-tree" permission
		List<Permission> perms = new ArrayList<Permission>();
		perms.add(PermissionFactory.getPermissionTemplateTree());
		
		return perms;
	}

	/**
	 * Get "all" permissions of user for given object (ALSO INHERITED PERMISSIONS).
	 * @param objUuid uuid of Object Entity to check
	 * @param userAddrUuid users address uuid
	 * @param checkWorkflow false=workflow state is ignored<br>
	 * 	true=also take workflow into account (IF ENABLED), e.g. return no write permission if entity is in state "Q" and user is NOT QA !
	 * @return list of found permissions, may be inherited or directly set on object
	 */
	public List<Permission> getPermissionsForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow) {
		return getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow, null);
	}

	/**
	 * Get "all" permissions of user for given object (ALSO INHERITED PERMISSIONS).
	 * @param objUuid uuid of Object Entity to check
	 * @param userAddrUuid users address uuid
	 * @param checkWorkflow false=workflow state is ignored<br>
	 * 	true=also take workflow into account (IF ENABLED), e.g. return no write permission if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return list of found permissions, may be inherited or directly set on object
	 */
	private List<Permission> getPermissionsForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = new ArrayList<Permission>();
        EntityPermission epSingle = PermissionFactory.getSingleObjectPermissionTemplate(objUuid);
        EntityPermission epTree = PermissionFactory.getTreeObjectPermissionTemplate(objUuid);
        EntityPermission epSubNode = PermissionFactory.getSubNodeObjectPermissionTemplate(objUuid);
		
		// check workflow permission before entity write permission
		if (checkWorkflow) {
			// if no write permission due to workflow, check if at least permission to write/create subnodes !
			if (!workflowHandler.hasWorkflowPermissionForObject(objUuid, userAddrUuid)) {
				// check write tree independent from workflow
				if (permService.hasInheritedPermissionForObject(userAddrUuid, epTree,groupId)) {
					// we have permission for subtree, return dummy permission informing frontend
					perms.add(PermissionFactory.getDummyPermissionSubTree());
				}
				// also subnode permission for adding subnodes
				if (permService.hasPermissionForObject(userAddrUuid, epSubNode, groupId)) {
	                perms.add(PermissionFactory.getPermissionTemplateSubNode());
	            }
 
				return perms;
			}			
		}

		if (isCatalogAdmin(userAddrUuid)) {
			// full access, we return "write-tree" permission
			perms.add(PermissionFactory.getPermissionTemplateTree());

		} else {
			if (permService.hasPermissionForObject(userAddrUuid, epSingle, groupId)) {
				perms.add(PermissionFactory.getPermissionTemplateSingle());
			}
			if (permService.hasInheritedPermissionForObject(userAddrUuid,epTree, groupId)) {
				perms.add(PermissionFactory.getPermissionTemplateTree());
			}
            if (permService.hasPermissionForObject(userAddrUuid, epSubNode, groupId)) {
                perms.add(PermissionFactory.getPermissionTemplateSubNode());
            }
		}

		return perms;
	}

	/**
     * Get "all" permissions of user for given address (ALSO INHERITED PERMISSIONS).
	 * @param addrUuid uuid of Address Entity to check
	 * @param userAddrUuid users address uuid
	 * @param checkWorkflow false=workflow state is ignored<br>
	 * 	true=also take workflow into account (IF ENABLED), e.g. return no write permission if entity is in state "Q" and user is NOT QA !
	 * @return list of found Permissions, may be inherited or directly set on address
	 */
	public List<Permission> getPermissionsForAddress(String addrUuid, String userAddrUuid,
			boolean checkWorkflow) {
		return getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow, null);
	}

	/**
     * Get "all" permissions of user for given address (ALSO INHERITED PERMISSIONS).
	 * @param addrUuid uuid of Address Entity to check
	 * @param userAddrUuid users address uuid
	 * @param checkWorkflow false=workflow state is ignored<br>
	 * 	true=also take workflow into account (IF ENABLED), e.g. return no write permission if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return list of found Permissions, may be inherited or directly set on address
	 */
	private List<Permission> getPermissionsForAddress(String addrUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = new ArrayList<Permission>();
        EntityPermission epSingle = PermissionFactory.getSingleAddressPermissionTemplate(addrUuid);
		EntityPermission epTree = PermissionFactory.getTreeAddressPermissionTemplate(addrUuid);
        EntityPermission epSubNode = PermissionFactory.getSubNodeAddressPermissionTemplate(addrUuid);
		

		// check workflow permission before entity write permission
		if (checkWorkflow) {
			// if no write permission due to workflow, check if at least permission to write/create subnodes !
			if (!workflowHandler.hasWorkflowPermissionForAddress(addrUuid, userAddrUuid)) {
				// check write tree independent from workflow
				if (permService.hasInheritedPermissionForAddress(userAddrUuid, epTree, groupId)) {
					// we have permission for subtree, return dummy permission informing frontend
					perms.add(PermissionFactory.getDummyPermissionSubTree());
				}
				// also subnode permission for adding subnodes
	            if (permService.hasPermissionForAddress(userAddrUuid, epSubNode, groupId)) {
	                perms.add(PermissionFactory.getPermissionTemplateSubNode());
	            }

				return perms;
			}			
		}

        if (isCatalogAdmin(userAddrUuid)) {
            // full access, we return "write-tree" permission
            perms.add(PermissionFactory.getPermissionTemplateTree());

        } else {
            if (permService.hasPermissionForAddress(userAddrUuid, epSingle, groupId)) {
                perms.add(PermissionFactory.getPermissionTemplateSingle());
            }
            if (permService.hasInheritedPermissionForAddress(userAddrUuid,epTree, groupId)) {
                perms.add(PermissionFactory.getPermissionTemplateTree());
            }
            if (permService.hasPermissionForAddress(userAddrUuid, epSubNode, groupId)) {
                perms.add(PermissionFactory.getPermissionTemplateSubNode());
            }
        }		
		
		return perms;
	}

	public List<PermissionObj> getRemovedObjectPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<PermissionObj> removedPerms = new ArrayList<PermissionObj>();
		
		Set<PermissionObj> oldPerms = oldGrp.getPermissionObjs();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updatePermissionObjs(newGrpDoc, newGrp);
		Set<PermissionObj> newPerms = newGrp.getPermissionObjs();
		
		for (PermissionObj oldPerm : oldPerms) {
			boolean removed = true;
			for (PermissionObj newPerm : newPerms) {
				if (isEqualPermissionObj(oldPerm, newPerm)) {
					removed = false;
					break;
				}
			}
			if (removed) {
				removedPerms.add(oldPerm);
			}
		}
		
		return removedPerms;
	}

	public List<PermissionAddr> getRemovedAddressPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<PermissionAddr> removedPerms = new ArrayList<PermissionAddr>();
		
		Set<PermissionAddr> oldPerms = oldGrp.getPermissionAddrs();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updatePermissionAddrs(newGrpDoc, newGrp);
		Set<PermissionAddr> newPerms = newGrp.getPermissionAddrs();
		
		for (PermissionAddr oldPerm : oldPerms) {
			boolean removed = true;
			for (PermissionAddr newPerm : newPerms) {
				if (isEqualPermissionAddr(oldPerm, newPerm)) {
					removed = false;
					break;
				}
			}
			if (removed) {
				removedPerms.add(oldPerm);
			}
		}
		
		return removedPerms;
	}

	public List<IdcUserPermission> getRemovedUserPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<IdcUserPermission> removedPerms = new ArrayList<IdcUserPermission>();
		
		Set<IdcUserPermission> oldPerms = oldGrp.getIdcUserPermissions();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updateIdcUserPermissions(newGrpDoc, newGrp);
		Set<IdcUserPermission> newPerms = newGrp.getIdcUserPermissions();
		
		for (IdcUserPermission oldPerm : oldPerms) {
			boolean removed = true;
			for (IdcUserPermission newPerm : newPerms) {
				if (isEqualIdcUserPermission(oldPerm, newPerm)) {
					removed = false;
					break;
				}
			}
			if (removed) {
				removedPerms.add(oldPerm);
			}
		}
		
		return removedPerms;
	}

	public List<PermissionObj> getAddedObjectPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<PermissionObj> addedPerms = new ArrayList<PermissionObj>();
		
		Set<PermissionObj> oldPerms = oldGrp.getPermissionObjs();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updatePermissionObjs(newGrpDoc, newGrp);
		Set<PermissionObj> newPerms = newGrp.getPermissionObjs();
		
		for (PermissionObj newPerm : newPerms) {
			boolean added = true;
			for (PermissionObj oldPerm : oldPerms) {
				if (isEqualPermissionObj(oldPerm, newPerm)) {
					added = false;
					break;
				}
			}
			if (added) {
				addedPerms.add(newPerm);
			}
		}
		
		return addedPerms;
	}

	public List<PermissionAddr> getAddedAddressPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<PermissionAddr> addedPerms = new ArrayList<PermissionAddr>();
		
		Set<PermissionAddr> oldPerms = oldGrp.getPermissionAddrs();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updatePermissionAddrs(newGrpDoc, newGrp);
		Set<PermissionAddr> newPerms = newGrp.getPermissionAddrs();
		
		for (PermissionAddr newPerm : newPerms) {
			boolean added = true;
			for (PermissionAddr oldPerm : oldPerms) {
				if (isEqualPermissionAddr(oldPerm, newPerm)) {
					added = false;
					break;
				}
			}
			if (added) {
				addedPerms.add(newPerm);
			}
		}
		
		return addedPerms;
	}

	public List<IdcUserPermission> getAddedUserPermissionsOfGroup(IdcGroup oldGrp, IngridDocument newGrpDoc) {
		List<IdcUserPermission> addedPerms = new ArrayList<IdcUserPermission>();
		
		Set<IdcUserPermission> oldPerms = oldGrp.getIdcUserPermissions();
		
		IdcGroup newGrp = new IdcGroup();
		docToBeanMapperSecurity.updateIdcUserPermissions(newGrpDoc, newGrp);
		Set<IdcUserPermission> newPerms = newGrp.getIdcUserPermissions();
		
		for (IdcUserPermission newPerm : newPerms) {
			boolean added = true;
			for (IdcUserPermission oldPerm : oldPerms) {
				if (isEqualIdcUserPermission(oldPerm, newPerm)) {
					added = false;
					break;
				}
			}
			if (added) {
				addedPerms.add(newPerm);
			}
		}
		
		return addedPerms;
	}

	/** returns false if a passed permission is null. don't pass empty permissions. ignores group. */
	private boolean isEqualPermissionObj(PermissionObj perm1, PermissionObj perm2) {
		if (perm1 == null || perm2 == null) {
			return false;
		}
		if (perm1.getUuid().equals(perm2.getUuid()) &&
			perm1.getPermissionId().equals(perm2.getPermissionId())) {
			return true;				
		}
		return false;
	}

	/** returns false if a passed permission is null. don't pass empty permissions. ignores group. */
	private boolean isEqualPermissionAddr(PermissionAddr perm1, PermissionAddr perm2) {
		if (perm1 == null || perm2 == null) {
			return false;
		}
		if (perm1.getUuid().equals(perm2.getUuid()) &&
			perm1.getPermissionId().equals(perm2.getPermissionId())) {
			return true;				
		}
		return false;
	}

	/** returns false if a passed permission is null. don't pass empty permissions. ignores group. */
	private boolean isEqualIdcUserPermission(IdcUserPermission perm1, IdcUserPermission perm2) {
		if (perm1 == null || perm2 == null) {
			return false;
		}
		if (perm1.getPermissionId().equals(perm2.getPermissionId())) {
			return true;				
		}		
		return false;
	}

	/**
	 * Get "user permissions" of given user.
	 * @param userAddrUuid users address uuid
	 * @return list of found permissions
	 */
	public List<Permission> getUserPermissions(String userAddrUuid) {
		return getUserPermissions(userAddrUuid, null);
	}

	/**
	 * Get "user permissions" of given user.
	 * @param userAddrUuid users address uuid
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return list of found permissions
	 */
	private List<Permission> getUserPermissions(String userAddrUuid, Long groupId) {
		List<Permission> perms = new ArrayList<Permission>();
		
		Permission p = PermissionFactory.getPermissionTemplateCreateRoot();
		if (permService.hasUserPermission(userAddrUuid, p, groupId)) {
			perms.add(p);
		}
		p = PermissionFactory.getPermissionTemplateQA();
		if (permService.hasUserPermission(userAddrUuid, p, groupId)) {
			perms.add(p);
		}

		return perms;
	}

	/**
	 * Checks whether user is catalog admin AND THROW EXCEPTION IF NOT !
	 * @param userUuid users address uuid
	 */
	public void checkIsCatalogAdmin(String userUuid) {
		if (!isCatalogAdmin(userUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}
	}

	/** Is the passed User the catalog administrator ? */
	public boolean isCatalogAdmin(String userUuid) {
		return permService.isCatalogAdmin(userUuid);
	}

	/** Get Catalog Admin User */
	public IdcUser getCatalogAdminUser() {
		return permService.getCatalogAdminUser();
	}

	/**
	 * Checks whether user has permissions to perform the STORE operation AND THROW EXCEPTION IF NOT !
	 * @param objUuid uuid of object to store
	 * @param parentUuid uuid of parent of object, MAY ONLY BE PASSED IF NEW OBJECT
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForStoreObject(String objUuid, String parentUuid, String userUuid) {
		boolean isNewObject = (objUuid == null) ? true : false;
		boolean isNewRootNode = (isNewObject && parentUuid == null) ? true : false;

		if (isNewObject) {
			// has create permission ?
			if (isNewRootNode) {
				// has permission to create new root node ?
				checkCreateRootPermission(userUuid);					
			} else {
				// has permission to create sub node on parent ?					
				checkTreeOrSubNodePermissionForObject(parentUuid, userUuid, false);					
			}
		} else {
			// has write permission ?					
			checkWritePermissionForObject(objUuid, userUuid, true);					
		}
	}

	/**
	 * Checks whether user has permissions to perform the STORE operation AND THROWS EXCEPTION IF NOT !
	 * @param addrUuid uuid of address to store
	 * @param parentUuid uuid of parent of address, MAY ONLY BE PASSED IF NEW ADDRESS
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForStoreAddress(String addrUuid, String parentUuid, String userUuid) {
		boolean isNewAddress = (addrUuid == null) ? true : false;
		boolean isNewRootNode = (isNewAddress && parentUuid == null) ? true : false;

		if (isNewAddress) {
			// has create permission ?
			if (isNewRootNode) {
				// has permission to create new root node ?
				checkCreateRootPermission(userUuid);
			} else {
				// has permission to create sub node on parent ?
				checkTreeOrSubNodePermissionForAddress(parentUuid, userUuid, false);
			}
		} else {
			// has write permission ?
			checkWritePermissionForAddress(addrUuid, userUuid, true);
		}
	}

	/**
	 * Checks whether user has permissions to perform the PUBLISH operation AND THROW EXCEPTION IF NOT !
	 * @param objUuid uuid of object to publish
	 * @param parentUuid uuid of parent of object, MAY ONLY BE PASSED IF NEW OBJECT
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForPublishObject(String objUuid, String parentUuid, String userUuid) {
		// if workflow enabled: only QAs can publish
		workflowHandler.checkQAPermission(userUuid);

		checkPermissionsForStoreObject(objUuid, parentUuid, userUuid);
	}

	/**
	 * Checks whether user has permissions to perform the PUBLISH operation AND THROWS EXCEPTION IF NOT !
	 * @param addrUuid uuid of address to publish
	 * @param parentUuid uuid of parent of address, MAY ONLY BE PASSED IF NEW ADDRESS
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForPublishAddress(String addrUuid, String parentUuid, String userUuid) {
		// if workflow enabled: only QAs can publish
		workflowHandler.checkQAPermission(userUuid);

		checkPermissionsForStoreAddress(addrUuid, parentUuid, userUuid);
	}


	/**
	 * Checks whether user has permissions to perform the COPY operation AND THROWS EXCEPTION IF NOT !
	 * @param fromUuid uuid of object to move
	 * @param toUuid uuid of parent to move object to
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForCopyObject(String fromUuid, String toUuid, String userUuid) {
		if (toUuid == null) {
			// has permission to create new root node ?
			checkCreateRootPermission(userUuid);
		} else {
			// has permission to create sub node on parent ?					
			checkTreeOrSubNodePermissionForObject(toUuid, userUuid, false);					
		}
	}

	/**
	 * Checks whether user has permissions to perform the COPY operation AND THROWS EXCEPTION IF NOT !
	 * @param fromUuid uuid of address to move
	 * @param toUuid uuid of parent to move address to
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForCopyAddress(String fromUuid, String toUuid, String userUuid) {
		if (toUuid == null) {
			// has permission to create new root node ?
			checkCreateRootPermission(userUuid);
		} else {
			// has permission to create sub node on parent ?					
			checkTreeOrSubNodePermissionForAddress(toUuid, userUuid, false);					
		}
	}

	/**
	 * Checks whether user has permissions to perform the MERGE operation AND THROWS EXCEPTION IF NOT !
	 * @param parentUuid uuid of parent of sub nodes to merge to
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForMergeAddressToSubAddresses(String parentUuid, String userUuid) {
		// has permission to write on sub nodes of parent ?					
		checkTreeOrSubNodePermissionForAddress(parentUuid, userUuid, false);					
	}

	/**
	 * Checks whether user has permissions to perform the MOVE operation AND THROWS EXCEPTION IF NOT !
	 * @param fromUuid uuid of object to move
	 * @param toUuid uuid of parent to move object to
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForMoveObject(String fromUuid, String toUuid, String userUuid) {
		// has permission to remove from source (delete subnode) ?
		checkTreePermissionForObject(fromUuid, userUuid, true);
		
		// check permissions on target via copy check (are the same)
		checkPermissionsForCopyObject(fromUuid, toUuid, userUuid);

		// permissions ok !
		// we already remove a possible set WRITE_TREE perm of object to move to guarantee no
		// nested WRITE_TREE perms after move !
		// NO !!!!!!!!!!!!!!!!
		// user has now multiple groups and groups should keep their direct permissions when moving !!!
//		permService.revokeObjectPermission(userUuid,
//				PermissionFactory.getTreeObjectPermissionTemplate(fromUuid));
	}

	/**
	 * Checks whether user has permissions to perform the MOVE operation AND THROWS EXCEPTION IF NOT !
	 * @param fromUuid uuid of address to move
	 * @param toUuid uuid of parent to move address to
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForMoveAddress(String fromUuid, String toUuid, String userUuid) {
		// has permission to remove from source (delete subnode) ?
		checkTreePermissionForAddress(fromUuid, userUuid, true);
		
		// check permissions on target via copy check (are the same)
		checkPermissionsForCopyAddress(fromUuid, toUuid, userUuid);
		
		// permissions ok !
		// we already remove a possible set WRITE_TREE perm of address to move to guarantee no
		// nested WRITE_TREE perms after move !
		// NO !!!!!!!!!!!!!!!!
		// user has now multiple groups and groups should keep their direct permissions when moving !!!
//		permService.revokeAddressPermission(userUuid,
//				PermissionFactory.getTreeAddressPermissionTemplate(fromUuid));
	}

	/**
	 * Checks whether user has permissions to perform the DELETE WORKING COPY operation AND THROWS EXCEPTION IF NOT !
	 * @param uuid uuid of object to delete working copy
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForDeleteWorkingCopyObject(String uuid, String userUuid) {
		checkWritePermissionForObject(uuid, userUuid, true);
	}

	/**
	 * Checks whether user has permissions to perform the DELETE WORKING COPY operation AND THROWS EXCEPTION IF NOT !
	 * @param uuid uuid of address to delete working copy
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForDeleteWorkingCopyAddress(String uuid, String userUuid) {
		checkWritePermissionForAddress(uuid, userUuid, true);
	}

	/**
	 * Checks whether user has permissions to perform the FULL DELETE operation AND THROWS EXCEPTION IF NOT !
	 * @param uuid uuid of object to delete
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForDeleteObject(String uuid, String userUuid) {
		checkTreePermissionForObject(uuid, userUuid, true);
	}

	/**
	 * Checks whether user has permissions to perform the FULL DELETE operation AND THROWS EXCEPTION IF NOT !
	 * @param uuid uuid of address to delete
	 * @param userUuid users address uuid
	 */
	public void checkPermissionsForDeleteAddress(String uuid, String userUuid) {
		checkTreePermissionForAddress(uuid, userUuid, true);
	}

	/**
	 * Checks whether user has write permission on given object AND THROW EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 */
	public void checkWritePermissionForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow) {
		if (!hasWritePermissionForObject(objUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

    /**
     * Checks whether user has WRITE_TREE or WRITE_SUBTREE permission on given object AND THROWS EXCEPTION IF NOT !
     * (checks DIRECT/INHERITED PERMISSIONS for WITRE_TREE but ONLY INHERITED for WRITE_SUBTREE)!
     * @param objUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     */
    private void checkTreeOrSubNodePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
        if (!hasTreePermissionForObject(objUuid, userAddrUuid, checkWorkflow, null) &&
        		!hasSubNodePermissionForObject(objUuid, userAddrUuid, checkWorkflow)) {
            throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
        }       
    }

    /**
	 * Checks whether user has WRITE_TREE permission on given object AND THROWS EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 */
	private void checkTreePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasTreePermissionForObject(objUuid, userAddrUuid, checkWorkflow, null)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has write permission on given address AND THROW EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 */
	public void checkWritePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasWritePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

    /**
     * Checks whether user has WRITE_TREE or WRITE_SUBTREE permission on given address AND THROW EXCEPTION IF NOT !
     * (checks DIRECT/INHERITED PERMISSIONS for WITRE_TREE but ONLY INHERITED for WRITE_SUBTREE)!
     * @param addrUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     */
    private void checkTreeOrSubNodePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
        if (!hasTreePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow, null) && 
        		!hasSubNodePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow)) {
            throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
        }       
    }

    /**
	 * Checks whether user has WRITE_TREE permission on given address AND THROW EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 */
	private void checkTreePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasTreePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow, null)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has "CreateRoot" permission AND THROW EXCEPTION IF NOT !
	 */
	private void checkCreateRootPermission(String userAddrUuid) {
		if (!hasCreateRootPermission(userAddrUuid, null)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}


	/**
	 * Grant WriteTree Permission of given user on given object IF NOT ALREADY GRANTED.
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * <br>NOTICE: User has now MULTIPLE groups ! whichGroups determines whether Permission is granted
	 * on all groups or only on selected groups. This method is called e.g. when new top node was created,
	 * so we guarantee only the relevant groups are updated !
	 * @param objUuid  uuid of object to grant write-tree permission on
	 * @param userAddrUuid addrUuid of calling user
	 * @param whichGroups to which group should the permission be added, e.g. only to groups, where create-root
	 * permission is present. This way we add new permission only to relevant groups, when top node was created !
	 * @param permissionEntityUuid uuid of object or address in group for a special entity permission
	 * (determined by whichGroups). Pass null if no special uuid needed for determining groups (e.g. create-root
	 * permission is bound to group and not to entity). 
	 */
	public void grantTreePermissionForObject(String objUuid, String userAddrUuid,
			GroupType whichGroups, String permissionEntityUuid) {

		grantTreePermissionForEntity(objUuid, IdcEntityType.OBJECT, userAddrUuid, whichGroups, permissionEntityUuid);
	}

	/**
	 * Grant WriteTree Permission of given user on given address IF NOT ALREADY GRANTED.
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * <br>NOTICE: User has now MULTIPLE groups ! whichGroups determines whether Permission is granted
	 * on all groups or only on selected groups. This method is called e.g. when new top node was created,
	 * so we guarantee only the relevant groups are updated !
	 * @param addrUuid  uuid of address to grant write-tree permission on
	 * @param userAddrUuid addrUuid of calling user
	 * @param whichGroups to which group should the permission be added, e.g. only to groups, where create-root
	 * permission is present. This way we add new permission only to relevant groups, when top node was created !
	 * @param permissionEntityUuid uuid of object or address in group for a special entity permission
	 * (determined by whichGroups). Pass null if no special uuid needed for determining groups (e.g. create-root
	 * permission is bound to group and not to entity). 
	 */
	public void grantTreePermissionForAddress(String addrUuid, String userAddrUuid,
			GroupType whichGroups, String permissionEntityUuid) {

		grantTreePermissionForEntity(addrUuid, IdcEntityType.ADDRESS, userAddrUuid, whichGroups, permissionEntityUuid);
	}

	/** Grant write-tree permission for object or address ! accumulates functionality !*/
	private void grantTreePermissionForEntity(String entityUuid, IdcEntityType entityType, String userAddrUuid,
			GroupType whichGroups, String permissionEntityUuid) {

		if (isCatalogAdmin(userAddrUuid)) {
			return;
		}

		EntityPermission epToGrant;
		boolean alreadyGranted;

		if (entityType == IdcEntityType.OBJECT) {
			epToGrant = PermissionFactory.getTreeObjectPermissionTemplate(entityUuid);
			alreadyGranted = permService.hasInheritedPermissionForObject(userAddrUuid, epToGrant, null);
		} else {
			epToGrant = PermissionFactory.getTreeAddressPermissionTemplate(entityUuid);
			alreadyGranted = permService.hasInheritedPermissionForAddress(userAddrUuid, epToGrant, null);
		}

		if (!alreadyGranted) {

			// DETERMINE GROUPS TO ADD PERMISSION TO !
			// Default is null = all groups !
			List<Long> groupIds = null;
			if (whichGroups == GroupType.ONLY_GROUPS_WITH_CREATE_ROOT_PERMISSION) {
				// grant permission only on groups containing create-root permission !
				Permission createRootPerm = PermissionFactory.getPermissionTemplateCreateRoot();
				groupIds = permService.getGroupIdsContainingUserPermission(userAddrUuid, createRootPerm);

			} else if (whichGroups == GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_OBJECT) {
				// grant permission only on groups containing write-subnode permission on object !
				// create permission to search groups for
				EntityPermission searchEp = PermissionFactory.getSubNodeObjectPermissionTemplate(permissionEntityUuid);
				groupIds = permService.getGroupIdsContainingObjectPermission(userAddrUuid, searchEp);

			} else if (whichGroups == GroupType.ONLY_GROUPS_WITH_SUBNODE_PERMISSION_ON_ADDRESS) {
				// grant permission only on groups containing write-subnode permission on address !
				// create permission to search groups for
				EntityPermission searchEp = PermissionFactory.getSubNodeAddressPermissionTemplate(permissionEntityUuid);
				groupIds = permService.getGroupIdsContainingAddressPermission(userAddrUuid, searchEp);
			}

			if (entityType == IdcEntityType.OBJECT) {
				permService.grantObjectPermission(userAddrUuid, epToGrant, groupIds);
			} else {
				permService.grantAddressPermission(userAddrUuid, epToGrant, groupIds);
			}
		}
	}

	/**
	 * Check Write Permission of given user on given object and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	public boolean hasWritePermissionForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow) {
		return hasWritePermissionForObject(objUuid, userAddrUuid, checkWorkflow, null);
	}

	/**
	 * Check Write Permission of given user on given object and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasWritePermissionForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow, groupId);
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateSingle())) {
				return true;
			} else if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
            }
		}
		
		return false;
	}

	/**
	 * Check WRITE_TREE Permission of given user on given object and return "yes"/"no"
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasTreePermissionForObject(String objUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow, groupId);
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
            }
		}
		
		return false;
	}

    /**
     * Check WRITE_SUBNODE Permission of given user on given object and return "yes"/"no"
     * @param objUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
     * @return
     */
    public boolean hasSubNodePermissionForObject(String objUuid, String userAddrUuid,
    		boolean checkWorkflow) {
    	return hasSubNodePermissionForObject(objUuid, userAddrUuid, checkWorkflow, null);
    }

    /**
     * Check WRITE_SUBNODE Permission of given user on given object and return "yes"/"no"
     * @param objUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
     * @return
     */
    private boolean hasSubNodePermissionForObject(String objUuid, String userAddrUuid,
    		boolean checkWorkflow, Long groupId) {
        List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow, groupId);
        for (Permission p : perms) {
            if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateSubNode())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check any permission of given user on given object and return "yes"/"no"
     * (CHECKS ALSO INHERITED PERMISSIONS)!
     * @param objUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
     * @return
     */
    private boolean hasPermissionForObject(String objUuid, String userAddrUuid,
    		boolean checkWorkflow, Long groupId) {
        List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow, groupId);
        return (perms.size() > 0);
    }

	/**
	 * Check Write Permission of given user on given address and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	public boolean hasWritePermissionForAddress(String addrUuid, String userAddrUuid,
			boolean checkWorkflow) {
		return hasWritePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow, null);
	}

	/**
	 * Check Write Permission of given user on given address and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasWritePermissionForAddress(String addrUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow, groupId);		
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateSingle())) {
				return true;
			} else if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
            }
		}
		
		return false;
	}

	/**
	 * Check WRITE_TREE or WRITE_SUBTREE Permission of given user on given address and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasTreePermissionForAddress(String addrUuid, String userAddrUuid,
			boolean checkWorkflow, Long groupId) {
		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow, groupId);		
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

    /**
     * Check WRITE_SUBTREE Permission of given user on given address and return "yes"/"no" !
     * (CHECKS ALSO INHERITED PERMISSIONS)!
     * @param addrUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
     * @return
     */
    public boolean hasSubNodePermissionForAddress(String addrUuid, String userAddrUuid,
    		boolean checkWorkflow) {
    	return hasSubNodePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow, null);
    }

    /**
     * Check WRITE_SUBTREE Permission of given user on given address and return "yes"/"no" !
     * (CHECKS ALSO INHERITED PERMISSIONS)!
     * @param addrUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
     * @return
     */
    private boolean hasSubNodePermissionForAddress(String addrUuid, String userAddrUuid,
    		boolean checkWorkflow, Long groupId) {
        List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow, groupId);       
        for (Permission p : perms) {
            if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateSubNode())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check any permission of given user on given address and return "yes"/"no" !
     * (CHECKS ALSO INHERITED PERMISSIONS)!
     * @param addrUuid
     * @param userAddrUuid
     * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
     * @return
     */
    private boolean hasPermissionForAddress(String addrUuid, String userAddrUuid,
    		boolean checkWorkflow, Long groupId) {
        List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow, groupId);       
        return (perms.size() > 0);
    }
	
	/** Check whether user has given user permission and return "yes"/"no" ! */
	public boolean hasUserPermission(Permission userPermission, String userAddrUuid) {
		return hasUserPermission(userPermission, userAddrUuid, null);			
	}

	/** Check whether user has given user permission and return "yes"/"no" !
	 * @param userPermission
	 * @param userAddrUuid
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasUserPermission(Permission userPermission, String userAddrUuid, Long groupId) {
		return permService.hasUserPermission(userAddrUuid, userPermission, groupId);			
	}

	/** Check "QA" Permission of given user (ONLY if workflow enabled) and return "yes"/"no" !
	 * NOTICE: ALWAYS RETURNS TRUE IF WORKFLOW DISABLED !
	 */
	public boolean hasQAPermission(String userAddrUuid) {
		return workflowHandler.hasQAPermission(userAddrUuid);
	}

	/** Check "CreateRoot" Permission of given user and return "yes"/"no" !
	 * @param userAddrUuid
	 * @param groupId only search in this group. Pass null if  all groups of user should be taken into account !
	 * @return
	 */
	private boolean hasCreateRootPermission(String userAddrUuid, Long groupId) {
		return hasUserPermission(PermissionFactory.getPermissionTemplateCreateRoot(), userAddrUuid, groupId);
	}

	/** Get all users who have write access for the given object. Pass list of ALL groups !
	 * We check every group for write access via first user in group !!!
	 * @param objUuid the object
	 * @param allGroups list of ALL groups.
	 * @param checkWorkflow false=workflow state is ignored, only check of write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 * @return
	 */
	public List<IdcUser> getUsersWithWritePermissionForObject(String objUuid, List<IdcGroup> allGroups, boolean checkWorkflow) {
		return getUsersWithWritePermissionForEntity(objUuid, allGroups, IdcEntityType.OBJECT, checkWorkflow);
	}

	/** Get all users who have write access for the given address. Pass list of ALL groups !
	 * We check every group for write access via first user in group !!!
	 * @param addrUuid the address
	 * @param allGroups list of ALL groups.
	 * @param checkWorkflow false=workflow state is ignored, only check of write permissions on entity<br>
	 * 		true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
	 * @return
	 */
	public List<IdcUser> getUsersWithWritePermissionForAddress(String addrUuid, List<IdcGroup> allGroups, boolean checkWorkflow) {
		return getUsersWithWritePermissionForEntity(addrUuid, allGroups, IdcEntityType.ADDRESS, checkWorkflow);
	}

    /** Get all users who can be responsible user for a new object created in frontend.
     * @param objUuid the object
     * @param allGroups list of ALL groups.
     * @param checkWorkflow false=workflow state is ignored, only check of write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     * @param callingUserUuid uuid of calling user
     * @return
     */
    public List<IdcUser> getResponsibleUsersForNewObject(String objUuid, List<IdcGroup> allGroups,
    		boolean checkWorkflow, String callingUserUuid) {
        return getResponsibleUsersForNewEntity(objUuid, allGroups, IdcEntityType.OBJECT,
        		checkWorkflow, callingUserUuid);
    }
	
    /** Get all users who can be responsible user for a new address created in frontend.
     * @param parentAddrUuid the parent address where the new address is created underneath
     * @param allGroups list of ALL groups.
     * @param checkWorkflow false=workflow state is ignored, only check of write-tree permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     * @param callingUserUuid uuid of calling user
     * @return
     */
    public List<IdcUser> getResponsibleUsersForNewAddress(String parentAddrUuid, List<IdcGroup> allGroups,
    		boolean checkWorkflow, String callingUserUuid) {
        return getResponsibleUsersForNewEntity(parentAddrUuid, allGroups, IdcEntityType.ADDRESS,
        		checkWorkflow, callingUserUuid);
    }
	
    /** Get all users who have any permission for the given object. Pass list of ALL groups !
     * We check every group for permission via first user in group !!!
     * @param objUuid the object
     * @param allGroups list of ALL groups.
     * @param checkWorkflow false=workflow state is ignored, only check of write permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     * @return
     */
    public List<IdcUser> getUsersWithPermissionForObject(String objUuid, List<IdcGroup> allGroups, boolean checkWorkflow) {
        return getUsersWithPermissionForEntity(objUuid, allGroups, IdcEntityType.OBJECT, checkWorkflow);
    }
    
    /** Get all users who have any permission for the given address. Pass list of ALL groups !
     * We check every group for permission via first user in group !!!
     * @param addrUuid the address
     * @param allGroups list of ALL groups.
     * @param checkWorkflow false=workflow state is ignored, only check of write-tree permissions on entity<br>
     *      true=also take workflow into account (IF ENABLED), e.g. if entity is in state "Q" user has to be QA !
     * @return
     */
    public List<IdcUser> getUsersWithPermissionForAddress(String addrUuid, List<IdcGroup> allGroups, boolean checkWorkflow) {
        return getUsersWithPermissionForEntity(addrUuid, allGroups, IdcEntityType.ADDRESS, checkWorkflow);
    }
    
    
	private List<IdcUser> getUsersWithWritePermissionForEntity(String entityUuid,
			List<IdcGroup> allGroups,
			IdcEntityType entityType,
			boolean checkWorkflow) {

		List<IdcUser> retUsers = new ArrayList<IdcUser>();

		// get catAdmin to check whether a user is the catAdmin !
		IdcUser catAdmin = permService.getCatalogAdminUser();
		String catAdminUuid = catAdmin.getAddrUuid();

		// check every group for write access (via first user in group) and add all users of group if so !
		for (IdcGroup group : allGroups) {
			Long groupId = group.getId();
			List<IdcUser> gUsers = daoIdcUser.getIdcUsersByGroupId(groupId);
			boolean addGroupUsers = false;
			
			// as soon as a user (who is NOT catAdmin) has write access, we add all users of group !
			for (IdcUser gUser : gUsers) {
				// skip cat admin, has ALL PERMISSIONS !!!
				if (gUser.getAddrUuid().equals(catAdminUuid)) {
					continue;
				}
				if (entityType == IdcEntityType.OBJECT) {
					if (hasWritePermissionForObject(entityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
						addGroupUsers = true;
						break;
					}
				} else if (entityType == IdcEntityType.ADDRESS) {
					if (hasWritePermissionForAddress(entityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
						addGroupUsers = true;
						break;
					}
				}
			}
			
			if (addGroupUsers) {
				retUsers.addAll(gUsers);
			}
		}

		// if catAdmin not added yet, add him !
		boolean addCatAdmin = true;
		for (IdcUser retUser : retUsers) {
			if (retUser.getAddrUuid().equals(catAdminUuid)) {
				addCatAdmin = false;
				break;
			}
		}
		if (addCatAdmin) {
			retUsers.add(catAdmin);
		}

		// remove duplicate users ! user may have multiple groups, is present for every group
		List<Long> userIds = new ArrayList<Long>();
		Iterator<IdcUser> itr  = retUsers.iterator();
		while(itr.hasNext()) {
			IdcUser u = itr.next();
			if (!userIds.contains(u.getId())) {
				userIds.add(u.getId());
			} else {
				itr.remove();
			}
		}
		
		return retUsers;
	}
	
    private List<IdcUser> getResponsibleUsersForNewEntity(String parentEntityUuid,
            List<IdcGroup> allGroups,
            IdcEntityType entityType,
            boolean checkWorkflow,
            String callingUserUuid) {

        List<IdcUser> retUsers = new ArrayList<IdcUser>();

        // get catAdmin to check whether a user is the catAdmin !
        IdcUser catAdmin = permService.getCatalogAdminUser();
        String catAdminUuid = catAdmin.getAddrUuid();
        
        // get all groups of user. extract ids. needed for comparison !
        List<IdcGroup> userGroups = daoIdcUser.getGroupsOfUser(callingUserUuid);
        List<Long> userGroupIds = new ArrayList<Long>(userGroups.size());
        for (IdcGroup uG : userGroups) {
        	userGroupIds.add(uG.getId());
        }

        // check every group for write tree access (via first user in group) and add all users of group if so !
        for (IdcGroup group : allGroups) {
			Long groupId = group.getId();
            List<IdcUser> gUsers = daoIdcUser.getIdcUsersByGroupId(groupId);
            boolean addGroupUsers = false;
            
            // as soon as a user (who is NOT catAdmin) has write tree access, we add all users of group !
            for (IdcUser gUser : gUsers) {
                // skip cat admin, has ALL PERMISSIONS !!!
                if (gUser.getAddrUuid().equals(catAdminUuid)) {
                    continue;
                }
                if (entityType == IdcEntityType.OBJECT) {
                    if (hasTreePermissionForObject(parentEntityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                        addGroupUsers = true;
                        break;
                    }
                    if (hasSubNodePermissionForObject(parentEntityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                    	// only the users in the write-subnode group of the calling user get write-tree permission on the new node !
                    	if (userGroupIds.contains(groupId)) {
                            addGroupUsers = true;
                            break;
                    	}
                    }
                } else if (entityType == IdcEntityType.ADDRESS) {
                    if (hasTreePermissionForAddress(parentEntityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                        addGroupUsers = true;
                        break;
                    }
                    if (hasSubNodePermissionForAddress(parentEntityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                    	// only the users in the write-subnode group of the calling user get write-tree permission on the new node !
                    	if (userGroupIds.contains(groupId)) {
                            addGroupUsers = true;
                            break;
                    	}
                    }
                }
                // first user of group evaluated, go to next group !
                break;
            }
            
            if (addGroupUsers) {
                retUsers.addAll(gUsers);
            }
        }

        // if catAdmin not added yet, add him !
        boolean addCatAdmin = true;
        for (IdcUser retUser : retUsers) {
            if (retUser.getAddrUuid().equals(catAdminUuid)) {
                addCatAdmin = false;
                break;
            }
        }
        if (addCatAdmin) {
            retUsers.add(catAdmin);
        }

        // remove duplicate users ! user may have multiple groups, is present for every group
        List<Long> userIds = new ArrayList<Long>();
        Iterator<IdcUser> itr  = retUsers.iterator();
        while(itr.hasNext()) {
            IdcUser u = itr.next();
            if (!userIds.contains(u.getId())) {
                userIds.add(u.getId());
            } else {
                itr.remove();
            }
        }
        
        return retUsers;
    }	
    
    private List<IdcUser> getUsersWithPermissionForEntity(String entityUuid,
            List<IdcGroup> allGroups,
            IdcEntityType entityType,
            boolean checkWorkflow) {

        List<IdcUser> retUsers = new ArrayList<IdcUser>();

        // get catAdmin to check whether a user is the catAdmin !
        IdcUser catAdmin = permService.getCatalogAdminUser();
        String catAdminUuid = catAdmin.getAddrUuid();

        // check every group for write tree access (via first user in group) and add all users of group if so !
        for (IdcGroup group : allGroups) {
			Long groupId = group.getId();
            List<IdcUser> gUsers = daoIdcUser.getIdcUsersByGroupId(groupId);
            boolean addGroupUsers = false;
            
            // as soon as a user (who is NOT catAdmin) has write tree access, we add all users of group !
            for (IdcUser gUser : gUsers) {
                // skip cat admin, has ALL PERMISSIONS !!!
                if (gUser.getAddrUuid().equals(catAdminUuid)) {
                    continue;
                }
                if (entityType == IdcEntityType.OBJECT) {
                    if (hasPermissionForObject(entityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                        addGroupUsers = true;
                        break;
                    }
                } else if (entityType == IdcEntityType.ADDRESS) {
                    if (hasPermissionForAddress(entityUuid, gUser.getAddrUuid(), checkWorkflow, groupId)) {
                        addGroupUsers = true;
                        break;
                    }
                }
            }
            
            if (addGroupUsers) {
                retUsers.addAll(gUsers);
            }
        }

        // if catAdmin not added yet, add him !
        boolean addCatAdmin = true;
        for (IdcUser retUser : retUsers) {
            if (retUser.getAddrUuid().equals(catAdminUuid)) {
                addCatAdmin = false;
                break;
            }
        }
        if (addCatAdmin) {
            retUsers.add(catAdmin);
        }

        // remove duplicate users ! user may have multiple groups, is present for every group
        List<Long> userIds = new ArrayList<Long>();
        Iterator<IdcUser> itr  = retUsers.iterator();
        while(itr.hasNext()) {
            IdcUser u = itr.next();
            if (!userIds.contains(u.getId())) {
                userIds.add(u.getId());
            } else {
                itr.remove();
            }
        }
        
        return retUsers;
    }       
}
