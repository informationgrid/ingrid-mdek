package de.ingrid.mdek.job.tools;

import org.apache.log4j.Logger;

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
	 * Check Write Permission of given user on given object !
	 * @param objUuid
	 * @param userAddrUuid
	 * @return
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
	 * Check Write Permission of given user on given address !
	 * @param addrUuid
	 * @param userAddrUuid
	 * @return
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
