package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.HashMap;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for importer.
 */
public class MdekImportService implements IImporterCallback {

	private static MdekImportService myInstance;

	private MdekObjectService objectService;
	private MdekAddressService addressService;

	protected MdekJobHandler jobHandler;

	/** Get The Singleton */
	public static synchronized MdekImportService getInstance(DaoFactory daoFactory,
			IPermissionService permissionService) {
		if (myInstance == null) {
	        myInstance = new MdekImportService(daoFactory, permissionService);
	      }
		return myInstance;
	}

	private MdekImportService(DaoFactory daoFactory,
			IPermissionService permissionService) {
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);

		jobHandler = MdekJobHandler.getInstance(daoFactory);
	}

	// ----------------------------------- IImporterCallback START ------------------------------------

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeObject(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeObject(IngridDocument objDoc,
			String defaultObjectParentUuid,
			boolean publishImmediately,
			String userUuid) {

		// TODO: check whether parent exists ! else set default !
		
		if (publishImmediately) {
			// TODO: check mandatory data

		} else {
			
		}
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.importer.IImporterCallback#writeAddress(de.ingrid.utils.IngridDocument, java.lang.String, boolean, java.lang.String)
	 */
	public void writeAddress(IngridDocument addrDoc,
			String defaultAddressParentUuid,
			boolean publishImmediately,
			String userUuid) {

		// TODO: check whether parent exists ! else set default !
		
		if (publishImmediately) {
			// TODO: check mandatory data

		} else {
			
		}
	}

	// ----------------------------------- IImporterCallback END ------------------------------------

	/** Returns "logged" Import job information IN DATABASE.
	 * NOTICE: returns EMPTY HashMap if no job info ! */
	public HashMap getImportJobInfoDB(String userUuid) {
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.IMPORT, userUuid);
		return jobHandler.mapJobInfoDB(jobInfo);
	}
	/** "logs" Start-Info of Import job IN MEMORY and IN DATABASE */
	public void startImportJobInfo(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		// first update in memory job state
		IngridDocument runningJobInfo = 
			jobHandler.createRunningJobDescription(JobType.IMPORT, 0, 0, false);
		runningJobInfo.put(MdekKeys.JOBINFO_START_TIME, startTime);
		jobHandler.updateRunningJob(userUuid, runningJobInfo);
		
		// then update job info in database
		jobHandler.startJobInfoDB(JobType.IMPORT, startTime, null, userUuid);
	}
	/** Update general info of Import job IN MEMORY and IN DATABASE */
	public void updateImportJobInfo(IdcEntityType whichType, int numImported, int totalNum,
			String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid,
				jobHandler.createRunningJobDescription(JobType.IMPORT, whichType, numImported, totalNum, false));

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//        HashMap details = MdekImportService.setUpImportJobInfoDetailsDB(whichType, numImported, totalNum);
//		jobHandler.updateJobInfoDB(JobType.IMPORT, details, userUuid);
	}
	/** Add new Message to info of Import job IN MEMORY and IN DATABASE. */
	public void updateImportJobInfoMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobMessages(userUuid, newMessage);

		// then update job info in database
		// NO, only in memory and write at end because of performance issues !
//		jobHandler.updateJobInfoDBMessages(JobType.IMPORT, newMessage, userUuid);
	}

	/**
	 * "logs" End-Info in import job information IN DATABASE !<br>
	 * NOTICE: at job runtime we store all info in memory (running job info) and persist it now !
	 * @param userUuid calling user
	 */
	/** "logs" End-Info in Import information IN DATABASE */
	public void endImportJobInfo(String userUuid) {
		// get running job info (in memory)
		HashMap runningJobInfo = jobHandler.getRunningJobInfo(userUuid);
		
		// set up job details to be stored
		HashMap jobDetails = jobHandler.getJobInfoDetailsFromRunningJobInfo(
				runningJobInfo, true);

		// then update job info in database
		jobHandler.updateJobInfoDB(JobType.IMPORT, jobDetails, userUuid);
		// add end info
		jobHandler.endJobInfoDB(JobType.IMPORT, userUuid);
	}
}
