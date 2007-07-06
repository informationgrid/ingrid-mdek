package de.ingrid.mdek.services.date;

import java.util.Date;

import de.ingrid.mdek.job.IJob;
import de.ingrid.utils.IngridDocument;

public class SimpleDateJob implements IJob {

	public SimpleDateJob() {
		// default
	}

	public IngridDocument getResults() {
		IngridDocument document = new IngridDocument();
		document.put("date", new Date());
		return document;
	}

}
