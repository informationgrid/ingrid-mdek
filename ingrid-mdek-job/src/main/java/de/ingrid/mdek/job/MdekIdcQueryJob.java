package de.ingrid.mdek.job;

import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;

/**
 * Encapsulates all Job functionality concerning QUERYING (inquiry of entities). 
 */
public class MdekIdcQueryJob extends MdekIdcJob {

	public MdekIdcQueryJob(ILogService logService,
			DaoFactory daoFactory) {
		super(logService.getLogger(MdekIdcQueryJob.class), daoFactory);
	}
}
