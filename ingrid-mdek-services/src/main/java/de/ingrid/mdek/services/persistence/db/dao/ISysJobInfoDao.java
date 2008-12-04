package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.persistence.db.model.SysList;



/**
 * Business DAO operations related to the <tt>SysJobInfo</tt> entity.
 * 
 * @author Martin
 */
public interface ISysJobInfoDao
	extends IGenericDao<SysList> {

	/**
	 * Get info about the passed Job/Job-Operation executed by the passed user.
	 * @param jobType what Job/Job-Operation
	 * @param userUuid the user who executed the job
	 * @return info about the last execution, null if not executed yet by this user !
	 */
	SysJobInfo getJobInfo(JobType jobType, String userUuid);
}
