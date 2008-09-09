package de.ingrid.mdek.job.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekError.MdekErrorType;
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
	 * @return list of found permissions, may be inherited or directly set on object
	 */
	public List<Permission> getPermissionsForObject(String objUuid, String userAddrUuid) {
		List<Permission> perms = new ArrayList<Permission>();
		
		// check workflow permission before entity write permission
		if (!workflowHandler.hasWorkflowPermissionForObject(objUuid, userAddrUuid)) {
			return perms;
		}

		if (permService.isCatalogAdmin(userAddrUuid)) {
			// full access, we return "write-tree" permission
			perms.add(PermissionFactory.getPermissionTemplateTree());

		} else {
			// we return BOTH WRITE PERMISSIONS if set ! SHOULD NOT HAPPEN !
			if (permService.hasPermissionForObject(userAddrUuid, 
				PermissionFactory.getSingleObjectPermissionTemplate(objUuid)))
			{
				perms.add(PermissionFactory.getPermissionTemplateSingle());
			}
			if (permService.hasInheritedPermissionForObject(userAddrUuid, 
				PermissionFactory.getTreeObjectPermissionTemplate(objUuid)))
			{
				perms.add(PermissionFactory.getPermissionTemplateTree());
			}
		}

		return perms;
	}

	/**
	 * Get "all" permissions of user for given address (ALSO INHERITED PERMISSIONS).
	 * @param addrUuid uuid of Address Entity to check
	 * @param userAddrUuid users address uuid
	 * @return list of found permissions, may be inherited or directly set on address
	 */
	public List<Permission> getPermissionsForAddress(String addrUuid, String userAddrUuid) {
		List<Permission> perms = new ArrayList<Permission>();

		// check workflow permission before entity write permission
		if (!workflowHandler.hasWorkflowPermissionForAddress(addrUuid, userAddrUuid)) {
			return perms;
		}

		if (permService.isCatalogAdmin(userAddrUuid)) {
			// full access, we return "write-tree" permission
			perms.add(PermissionFactory.getPermissionTemplateTree());

		} else {
			// we return BOTH WRITE PERMISSIONS if set ! SHOULD NOT HAPPEN !
			if (permService.hasPermissionForAddress(userAddrUuid, 
					PermissionFactory.getSingleAddressPermissionTemplate(addrUuid)))
			{
				perms.add(PermissionFactory.getPermissionTemplateSingle());

			}
			if (permService.hasInheritedPermissionForAddress(userAddrUuid, 
					PermissionFactory.getTreeAddressPermissionTemplate(addrUuid)))
			{
				perms.add(PermissionFactory.getPermissionTemplateTree());
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
		List<Permission> perms = new ArrayList<Permission>();
		
		Permission p = PermissionFactory.getPermissionTemplateCreateRoot();
		if (permService.hasUserPermission(userAddrUuid, p)) {
			perms.add(p);
		}
		p = PermissionFactory.getPermissionTemplateQA();
		if (permService.hasUserPermission(userAddrUuid,p)) {
			perms.add(p);
		}

		return perms;
	}

	/**
	 * Checks whether user is catalog admin AND THROW EXCEPTION IF NOT !
	 * @param userUuid users address uuid
	 */
	public void checkIsCatalogAdmin(String userUuid) {
		if (!permService.isCatalogAdmin(userUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}
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
				checkTreePermissionForObject(parentUuid, userUuid, false);					
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
				checkTreePermissionForAddress(parentUuid, userUuid, false);
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
			checkTreePermissionForObject(toUuid, userUuid, false);					
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
			checkTreePermissionForAddress(toUuid, userUuid, false);					
		}
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
		permService.revokeObjectPermission(userUuid,
				PermissionFactory.getTreeObjectPermissionTemplate(fromUuid));
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
		permService.revokeAddressPermission(userUuid,
				PermissionFactory.getTreeAddressPermissionTemplate(fromUuid));
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
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 */
	public void checkWritePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasWritePermissionForObject(objUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has WRITE_TREE permission on given object AND THROWS EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 */
	private void checkTreePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasTreePermissionForObject(objUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has write permission on given address AND THROW EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 */
	public void checkWritePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasWritePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has WRITE_TREE permission on given address AND THROW EXCEPTION IF NOT !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 */
	private void checkTreePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		if (!hasTreePermissionForAddress(addrUuid, userAddrUuid, checkWorkflow)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}

	/**
	 * Checks whether user has "CreateRoot" permission AND THROW EXCEPTION IF NOT !
	 */
	private void checkCreateRootPermission(String userAddrUuid) {
		if (!hasCreateRootPermission(userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION_ON_ENTITY));
		}		
	}


	/**
	 * Grant WriteTree Permission of given user on given objectIF NOT ALREADY GRANTED.
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 */
	public void grantTreePermissionForObject(String objUuid, String userAddrUuid) {
		EntityPermission ep = PermissionFactory.getTreeObjectPermissionTemplate(objUuid);

		boolean alreadyGranted = permService.hasInheritedPermissionForObject(userAddrUuid, ep);
		if (!alreadyGranted) {
			permService.grantObjectPermission(userAddrUuid, ep);
		}
	}

	/**
	 * Grant WriteTree Permission of given user on given address IF NOT ALREADY GRANTED.
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 */
	public void grantTreePermissionForAddress(String addrUuid, String userAddrUuid) {
		EntityPermission ep = PermissionFactory.getTreeAddressPermissionTemplate(addrUuid);

		boolean alreadyGranted = permService.hasInheritedPermissionForAddress(userAddrUuid, ep);
		if (!alreadyGranted) {
			permService.grantAddressPermission(userAddrUuid, ep);			
		}
	}

	/**
	 * Delete all "direct" permissions for the given object (called when object is deleted ...).
	 */
	public void deletePermissionsForObject(String objUuid) {
		permService.deleteObjectPermissions(objUuid); 
	}

	/**
	 * Delete all "direct" permissions for the given address (called when address is deleted ...).
	 */
	public void deletePermissionsForAddress(String addrUuid) {
		permService.deleteAddressPermissions(addrUuid); 
	}

	/**
	 * Check Write Permission of given user on given object and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param objUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	public boolean hasWritePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
		// check workflow permission before entity write permission
		if (checkWorkflow) {
			if (!workflowHandler.hasWorkflowPermissionForObject(objUuid, userAddrUuid)) {
				return false;
			}			
		}

		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid);
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
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	private boolean hasTreePermissionForObject(String objUuid, String userAddrUuid, boolean checkWorkflow) {
		// check workflow permission before entity write permission
		if (checkWorkflow) {
			if (!workflowHandler.hasWorkflowPermissionForObject(objUuid, userAddrUuid)) {
				return false;
			}			
		}

		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid);
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check Write Permission of given user on given address and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	public boolean hasWritePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		// check workflow permission before entity write permission
		if (checkWorkflow) {
			if (!workflowHandler.hasWorkflowPermissionForAddress(addrUuid, userAddrUuid)) {
				return false;
			}			
		}

		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid);		
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
	 * Check WRITE_TREE Permission of given user on given address and return "yes"/"no" !
	 * (CHECKS ALSO INHERITED PERMISSIONS)!
	 * @param addrUuid
	 * @param userAddrUuid
	 * @param checkWorkflow false=workflow state is ignored, only check write permissions on entity<br>
	 * 		true=also take workflow into account, e.g. return false if entity is in state "Q" and user is NOT QA !
	 * @return
	 */
	private boolean hasTreePermissionForAddress(String addrUuid, String userAddrUuid, boolean checkWorkflow) {
		// check workflow permission before entity write permission
		if (checkWorkflow) {
			if (!workflowHandler.hasWorkflowPermissionForAddress(addrUuid, userAddrUuid)) {
				return false;
			}			
		}

		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid);		
		for (Permission p : perms) {
			if (permService.isEqualPermission(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/** Check whether user has given user permission and return "yes"/"no" ! */
	public boolean hasUserPermission(Permission userPermission, String userAddrUuid) {
		return permService.hasUserPermission(userAddrUuid, userPermission);			
	}

	/** Check "CreateRoot" Permission of given user and return "yes"/"no" ! */
	private boolean hasCreateRootPermission(String userAddrUuid) {
		return hasUserPermission(PermissionFactory.getPermissionTemplateCreateRoot(), userAddrUuid);
	}

	/** Get all users who have write access for the given object. Pass list of ALL groups !
	 * We check every group for write access via first user in group !!!
	 * @param objUuid the object
	 * @param allGroups list of ALL groups.
	 * @return
	 */
	public List<IdcUser> getUsersWithWritePermissionForObject(String objUuid, List<IdcGroup> allGroups) {
		return getUsersWithWritePermissionForEntity(objUuid, allGroups, IdcEntityType.OBJECT);
	}

	/** Get all users who have write access for the given address. Pass list of ALL groups !
	 * We check every group for write access via first user in group !!!
	 * @param addrUuid the address
	 * @param allGroups list of ALL groups.
	 * @return
	 */
	public List<IdcUser> getUsersWithWritePermissionForAddress(String addrUuid, List<IdcGroup> allGroups) {
		return getUsersWithWritePermissionForEntity(addrUuid, allGroups, IdcEntityType.ADDRESS);
	}

	private List<IdcUser> getUsersWithWritePermissionForEntity(String entityUuid,
			List<IdcGroup> allGroups,
			IdcEntityType entityType) {

		List<IdcUser> retUsers = new ArrayList<IdcUser>();

		// get catAdmin to check whether a user is the catAdmin !
		IdcUser catAdmin = permService.getCatalogAdmin();
		String catAdminUuid = catAdmin.getAddrUuid();

		// check every group for write access (via first user in group) and add all users of group if so !
		for (IdcGroup group : allGroups) {
			List<IdcUser> gUsers = daoIdcUser.getIdcUsersByGroupId(group.getId());
			boolean addGroupUsers = false;
			
			// as soon as a user (who is NOT catAdmin) has write access, we add all users of group !
			for (IdcUser gUser : gUsers) {
				// skip cat admin, has ALL PERMISSIONS !!!
				if (gUser.getAddrUuid().equals(catAdminUuid)) {
					continue;
				}
				if (entityType == IdcEntityType.OBJECT) {
					if (hasWritePermissionForObject(entityUuid, gUser.getAddrUuid(), true)) {
						addGroupUsers = true;
						break;
					}
				} else if (entityType == IdcEntityType.ADDRESS) {
					if (hasWritePermissionForAddress(entityUuid, gUser.getAddrUuid(), true)) {
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
		
		return retUsers;
	}
}
