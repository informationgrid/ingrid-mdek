package de.ingrid.mdek.services.date;

import java.util.Date;

import org.apache.log4j.Logger;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.utils.IngridDocument;

public class SimpleDateJob implements IJob {

	private Logger _logger;

	public SimpleDateJob(ILogService logService) {
		_logger = logService.getLogger(SimpleDateJob.class);
	}

	public IngridDocument getResults() {
		if (_logger.isInfoEnabled()) {
			_logger.info("create new date");
		}
		IngridDocument document = new IngridDocument();
		document.put("date", new Date());
		return document;
	}

}
