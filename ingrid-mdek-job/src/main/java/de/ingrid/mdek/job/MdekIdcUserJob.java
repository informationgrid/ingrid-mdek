package de.ingrid.mdek.job;

import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.security.IPermissionService;

/**
 * Encapsulates all Job functionality concerning USER MANAGEMENT (permissions etc.). 
 */
public class MdekIdcUserJob extends MdekIdcJob {

	IPermissionService userHandler;

	public MdekIdcUserJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionHandler) {
		super(logService.getLogger(MdekIdcUserJob.class), daoFactory);
		
		this.userHandler = permissionHandler;
	}
}
