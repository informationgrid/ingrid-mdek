package de.ingrid.mdek.services.catalog;

import java.util.Date;
import java.util.HashMap;

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
	public HashMap getImportInfoDB(String userUuid) {
		SysJobInfo jobInfo = jobHandler.getJobInfoDB(JobType.IMPORT, userUuid);
		return jobHandler.mapJobInfoDB(jobInfo);
	}
	/** "logs" Start-Info in Import information IN DATABASE */
	public void startImportInfoDB(String userUuid) {
		String startTime = MdekUtils.dateToTimestamp(new Date());

		jobHandler.startJobInfoDB(JobType.IMPORT, startTime, null, userUuid);
	}
	/** Update general info of Import job IN MEMORY and IN DATABASE */
	public void updateImportInfoDB(IdcEntityType whichType, int numImported, int totalNum,
			String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJob(userUuid, 
				jobHandler.createRunningJobDescription(JobType.IMPORT, numImported, totalNum, false));

		// then update job info in database
        HashMap details = MdekImportService.setUpImportJobInfoDetailsDB(whichType, numImported, totalNum);
		jobHandler.updateJobInfoDB(JobType.IMPORT, details, userUuid);
	}
	/** Add new Message to info of Import job IN MEMORY and IN DATABASE. */
	public void updateImportInfoDBMessages(String newMessage, String userUuid) {
		// first update in memory job state
		jobHandler.updateRunningJobMessages(userUuid, newMessage);

		// then update job info in database
		jobHandler.updateJobInfoDBMessages(JobType.IMPORT, newMessage, userUuid);
	}
	/** "logs" End-Info in Import information IN DATABASE */
	public void endImportInfoDB(String userUuid) {
		jobHandler.endJobInfoDB(JobType.IMPORT, userUuid);
	}

	/** Set up details of import to be stored in database. */
	public static HashMap setUpImportJobInfoDetailsDB(IdcEntityType whichType, int num, int totalNum) {
		// same as export
		return MdekExportService.setUpExportJobInfoDetailsDB(whichType, num, totalNum);
	}
}
