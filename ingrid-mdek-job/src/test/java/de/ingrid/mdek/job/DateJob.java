package de.ingrid.mdek.job;

import java.util.Date;

import de.ingrid.utils.IngridDocument;

public class DateJob implements IJob {

	public IngridDocument getResults() {
		IngridDocument result = new IngridDocument();
		result.put("result", new Date(System.currentTimeMillis()));
		return result;
	}

}
