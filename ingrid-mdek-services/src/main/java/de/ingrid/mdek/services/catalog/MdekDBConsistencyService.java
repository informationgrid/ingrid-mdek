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
package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.catalog.dbconsistency.AddressHierarchyChecker;
import de.ingrid.mdek.services.catalog.dbconsistency.AddressReferencesChecker;
import de.ingrid.mdek.services.catalog.dbconsistency.ConsistencyChecker;
import de.ingrid.mdek.services.catalog.dbconsistency.ErrorReport;
import de.ingrid.mdek.services.catalog.dbconsistency.InfoAddressChecker;
import de.ingrid.mdek.services.catalog.dbconsistency.ObjectHierarchyChecker;
import de.ingrid.mdek.services.catalog.dbconsistency.TableAssociationsChecker;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates functions needed for database consistency checks
 */
public class MdekDBConsistencyService {

	private static final Logger LOG = Logger.getLogger(MdekDBConsistencyService.class);

	private static MdekDBConsistencyService myInstance;
	
	private MdekJobHandler jobHandler;

	private IConsistencyCheckerDao daoConsistencyChecker;
	
	List<ConsistencyChecker> consistencyCheckers = new ArrayList<ConsistencyChecker>(); 

	/** Get The Singleton */
	public static synchronized MdekDBConsistencyService getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekDBConsistencyService(daoFactory);
	      }
		return myInstance;
	}

	private MdekDBConsistencyService(DaoFactory daoFactory) {
		jobHandler = MdekJobHandler.getInstance(daoFactory);
		daoConsistencyChecker = daoFactory.getConsistencyCheckerDao();

		// Register consistency checkers
		consistencyCheckers.add(new ObjectHierarchyChecker(daoConsistencyChecker));
		consistencyCheckers.add(new AddressHierarchyChecker(daoConsistencyChecker));
		consistencyCheckers.add(new AddressReferencesChecker(daoConsistencyChecker));
		consistencyCheckers.add(new InfoAddressChecker(daoConsistencyChecker));
		consistencyCheckers.add(new TableAssociationsChecker(daoConsistencyChecker));
	}

	/** "logs" Start-Info of DBConsistency job IN MEMORY and IN DATABASE */
	public void startDBConsistencyJobInfo(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.ANALYZE, 0, 0, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
		jobHandler.startJobInfoDB(JobType.ANALYZE, startTime, null, userUuid);
	}
	
	/**
	 * "logs" End-Info in DBConsistency job information IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param userUuid calling user
	 */
	/** "logs" End-Info in DBConsistency information IN DATABASE */
	public void endDBConsistencyJobInfo(String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// then update job info in database
		jobHandler.updateJobInfoDB(JobType.ANALYZE, runningJobInfo, userUuid);
		// add end info
		jobHandler.endJobInfoDB(JobType.ANALYZE, userUuid);
	}
	
	public Map getDBConsistencyJobInfoDB(String userId) {
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.ANALYZE, userId);
		Map jobDetails = jobHandler.mapJobInfoDB(jobInfo);
		return convertJobInfo(jobDetails);
	}

	private Map convertJobInfo(Map<String, Object> jobDetails) {
		Map<String, Object> result = new HashMap<String, Object>();
		LOG.debug("Job Details: "+ jobDetails);

		for (String key : jobDetails.keySet()) {
			if (key.equals("errorList")) {
				List<ErrorReport> errorList = (List<ErrorReport>) jobDetails.get("errorList");
				List<IngridDocument> errorListDoc = new ArrayList<IngridDocument>();
				for (ErrorReport errorReport : errorList) {
					IngridDocument errorReportDoc = new IngridDocument();
					errorReportDoc.put(MdekKeys.VALIDATION_MESSAGE, errorReport.getMessage());
					errorReportDoc.put(MdekKeys.VALIDATION_SOLUTION, errorReport.getSolution());
					errorListDoc.add(errorReportDoc);
				}
				result.put(MdekKeys.VALIDATION_RESULT, errorListDoc);
			} else {
				result.put(key, jobDetails.get(key));
			}
		}


		return result;
	}

	public void analyze(String userId) {
		List<ErrorReport> errorList = new ArrayList<ErrorReport>();
		for (ConsistencyChecker checker : consistencyCheckers) {
			checker.run();
			List<ErrorReport> result = checker.getResult();
			if (null != result) {
				errorList.addAll(result);
			}

			Map<String, List<ErrorReport>> jobDetails = new HashMap<String, List<ErrorReport>>();
			jobDetails.put("errorList", errorList);
			jobHandler.updateRunningJob(userId, jobDetails);
		}
	}
}
