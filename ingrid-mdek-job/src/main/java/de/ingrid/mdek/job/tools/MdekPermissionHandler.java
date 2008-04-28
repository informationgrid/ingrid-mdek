package de.ingrid.mdek.job.tools;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
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
	 * Checks whether user has write permission on given object AND THROW EXCEPTION IF NOT !
	 */
	public void checkWritePermissionForObject(String objUuid, String userAddrUuid) {
		if (!hasWritePermissionForObject(objUuid, userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Check Write Permission of given user on given object and return "yes"/"no" !
	 */
	public boolean hasWritePermissionForObject(String objUuid, String userAddrUuid) {
		// check catalog Admin
		boolean hasPermission = permService.isCatalogAdmin(userAddrUuid);

		// check explicit permission for object
		if (!hasPermission) {
			hasPermission = permService.hasPermissionForObject(userAddrUuid, 
					PermissionFactory.getSingleObjectPermissionTemplate(objUuid));			
		}

		// check inherited permission for object
		if (!hasPermission) {
			hasPermission =	permService.hasInheritedPermissionForObject(userAddrUuid, 
				PermissionFactory.getTreeObjectPermissionTemplate(objUuid));
		}
		
		return hasPermission;
	}

	/**
	 * Grant WriteTree Permission of given user on given object.
	 */
	public void grantWriteTreePermissionForObject(String objUuid, String userAddrUuid) {
		permService.grantObjectPermission(userAddrUuid, 
			PermissionFactory.getTreeObjectPermissionTemplate(objUuid));
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
	 * Check Write Permission of given user on given address and return "yes"/"no" !
	 */
	public boolean hasWritePermissionForAddress(String addrUuid, String userAddrUuid) {
		// check catalog Admin
		boolean hasPermission = permService.isCatalogAdmin(userAddrUuid);

		// check explicit permission for address
		if (!hasPermission) {
			hasPermission = permService.hasPermissionForAddress(userAddrUuid, 
				PermissionFactory.getSingleAddressPermissionTemplate(addrUuid));			
		}

		// check inherited permission for address
		if (!hasPermission) {
			hasPermission =	permService.hasInheritedPermissionForAddress(userAddrUuid, 
				PermissionFactory.getTreeAddressPermissionTemplate(addrUuid));
		}
		
		return hasPermission;
	}

	/**
	 * Grant WriteTree Permission of given user on given address.
	 */
	public void grantWriteTreePermissionForAddress(String addrUuid, String userAddrUuid) {
		permService.grantAddressPermission(userAddrUuid, 
			PermissionFactory.getTreeAddressPermissionTemplate(addrUuid));
	}

	/**
	 * Checks whether user has "CreateRoot" permission AND THROW EXCEPTION IF NOT !
	 */
	public void checkCreateRootPermission(String userAddrUuid) {
		if (!hasCreateRootPermission(userAddrUuid)) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_PERMISSION));
		}		
	}

	/**
	 * Check "CreateRoot" Permission of given user and return "yes"/"no" !
	 */
	public boolean hasCreateRootPermission(String userAddrUuid) {
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
