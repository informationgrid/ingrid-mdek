package de.ingrid.mdek.job.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;


/**
 * Handles permission checks.
 */
public class MdekPermissionHandler {

	private static final Logger LOG = Logger.getLogger(MdekPermissionHandler.class);
	
	private IPermissionService permService;

	private static MdekPermissionHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekPermissionHandler getInstance(IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekPermissionHandler(permissionService);
	      }
		return myInstance;
	}

	private MdekPermissionHandler(IPermissionService permissionService) {
		this.permService = permissionService; 
	}

	/**
	 * Get "all" permissions of user for given object (ALSO INHERITED PERMISSIONS).
	 * @param objUuid uuid of Object Entity to check
	 * @param userAddrUuid users address uuid
	 * @return list of found permissions, may be inherited or directly set on object
	 */
	public List<Permission> getPermissionsForObject(String objUuid, String userAddrUuid) {
		List<Permission> perms = new ArrayList<Permission>();
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
	 * Checks whether user has permissions to perform the store operation AND THROW EXCEPTION IF NOT !
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
				checkTreePermissionForObject(parentUuid, userUuid);					
			}
		} else {
			// has write permission ?					
			checkWritePermissionForObject(objUuid, userUuid);					
		}
	}

	/**
	 * Checks whether user has permissions to perform the store operation AND THROWS EXCEPTION IF NOT !
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
				checkTreePermissionForAddress(parentUuid, userUuid);					
			}
		} else {
			// has write permission ?					
			checkWritePermissionForAddress(addrUuid, userUuid);					
		}
	}

	/**
	 * Checks whether user has write permission on given object AND THROW EXCEPTION IF NOT !
	 */
	public void checkWritePermissionForObject(String objUuid, String userAddrUuid) {
		if (!hasWritePermissionForObject(objUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Checks whether user has WRITE_TREE permission on given object AND THROWS EXCEPTION IF NOT !
	 */
	public void checkTreePermissionForObject(String objUuid, String userAddrUuid) {
		if (!hasTreePermissionForObject(objUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Checks whether user has write permission on given address AND THROW EXCEPTION IF NOT !
	 */
	public void checkWritePermissionForAddress(String addrUuid, String userAddrUuid) {
		if (!hasWritePermissionForAddress(addrUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Checks whether user has WRITE_TREE permission on given address AND THROW EXCEPTION IF NOT !
	 */
	public void checkTreePermissionForAddress(String addrUuid, String userAddrUuid) {
		if (!hasTreePermissionForAddress(addrUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Checks whether user has "CreateRoot" permission AND THROW EXCEPTION IF NOT !
	 */
	private void checkCreateRootPermission(String userAddrUuid) {
		if (!hasCreateRootPermission(userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}


	/**
	 * Grant WriteTree Permission of given user on given object.
	 */
	public void grantWriteTreePermissionForObject(String objUuid, String userAddrUuid) {
		permService.grantObjectPermission(userAddrUuid, 
			PermissionFactory.getTreeObjectPermissionTemplate(objUuid));
	}

	/**
	 * Grant WriteTree Permission of given user on given address.
	 */
	public void grantWriteTreePermissionForAddress(String addrUuid, String userAddrUuid) {
		permService.grantAddressPermission(userAddrUuid, 
			PermissionFactory.getTreeAddressPermissionTemplate(addrUuid));
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
	 */
	private boolean hasWritePermissionForObject(String objUuid, String userAddrUuid) {
		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid);
		
		for (Permission p : perms) {
			if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateSingle())) {
				return true;
			} else if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check WRITE_TREE Permission of given user on given object and return "yes"/"no" !
	 */
	private boolean hasTreePermissionForObject(String objUuid, String userAddrUuid) {
		List<Permission> perms = getPermissionsForObject(objUuid, userAddrUuid);
		
		for (Permission p : perms) {
			if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check Write Permission of given user on given address and return "yes"/"no" !
	 */
	private boolean hasWritePermissionForAddress(String addrUuid, String userAddrUuid) {
		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid);
		
		for (Permission p : perms) {
			if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateSingle())) {
				return true;
			} else if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check WRITE_TREE Permission of given user on given address and return "yes"/"no" !
	 */
	private boolean hasTreePermissionForAddress(String addrUuid, String userAddrUuid) {
		List<Permission> perms = getPermissionsForAddress(addrUuid, userAddrUuid);
		
		for (Permission p : perms) {
			if (permService.isEqualPermissions(p, PermissionFactory.getPermissionTemplateTree())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check "CreateRoot" Permission of given user and return "yes"/"no" !
	 */
	private boolean hasCreateRootPermission(String userAddrUuid) {
		// check catalog Admin
		boolean hasPermission = permService.isCatalogAdmin(userAddrUuid);

		// check user
		if (!hasPermission) {
			hasPermission = permService.hasUserPermission(userAddrUuid, 
				PermissionFactory.getPermissionTemplateCreateRoot());			
		}

		return hasPermission;
	}
}
