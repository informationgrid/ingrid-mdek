package de.ingrid.mdek.job;

import de.ingrid.mdek.job.IJob;
import de.ingrid.utils.IngridDocument;

public class DateJobService implements IJob {

	private final DateJob _job;

	public DateJobService(DateJob job) {
		_job = job;
	}

	public IngridDocument getResults() {
		return _job.getResults();
	}
}
