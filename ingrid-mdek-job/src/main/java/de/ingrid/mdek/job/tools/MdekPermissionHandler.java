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
		// first check explicit permission for object
		boolean hasPermission = permService.hasPermissionForObject(userAddrUuid, 
			PermissionFactory.getSingleObjectPermissionTemplate(objUuid));
		if (!hasPermission) {
			// check inherited permission for object
			hasPermission =	permService.hasInheritedPermissionForObject(userAddrUuid, 
				PermissionFactory.getTreeObjectPermissionTemplate(objUuid));
		}
		
		return hasPermission;
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
		// first check explicit permission for address
		boolean hasPermission = permService.hasPermissionForAddress(userAddrUuid, 
			PermissionFactory.getSingleAddressPermissionTemplate(addrUuid));
		if (!hasPermission) {
			// check inherited permission for address
			hasPermission =	permService.hasInheritedPermissionForAddress(userAddrUuid, 
				PermissionFactory.getTreeAddressPermissionTemplate(addrUuid));
		}
		
		return hasPermission;
	}
}
