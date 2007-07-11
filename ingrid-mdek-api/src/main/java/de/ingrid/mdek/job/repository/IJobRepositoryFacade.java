package de.ingrid.mdek.job.repository;

import de.ingrid.utils.IngridDocument;

public interface IJobRepositoryFacade {

	/**
	 * 
	 * necessary fields in document are:
	 * 
	 * IJobRepository.JOB_ID <br>
	 * IJobRepository.JOB_DESCRIPTION <br>
	 * IJobRepository.JOB_PERSIST <br>
	 * IJobRepository.JOB_METHODS <br>
	 */
	IngridDocument execute(IngridDocument document);

}
