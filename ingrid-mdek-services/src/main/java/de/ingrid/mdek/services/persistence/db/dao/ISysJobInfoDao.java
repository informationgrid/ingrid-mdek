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
package de.ingrid.mdek.services.persistence.db.dao;

import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;



/**
 * Business DAO operations related to the <tt>SysJobInfo</tt> entity.
 * 
 * @author Martin
 */
public interface ISysJobInfoDao
	extends IGenericDao<SysJobInfo> {

	/**
	 * Get info about the passed Job/Job-Operation executed by the passed user.
	 * @param jobType what Job/Job-Operation
	 * @param userUuid the user who executed the job
	 * @return info about the last execution, null if not executed yet by this user !
	 */
	SysJobInfo getJobInfo(JobType jobType, String userUuid);
}
