package de.ingrid.mdek.example;

import java.util.Random;

import de.ingrid.mdek.job.IJob;
import de.ingrid.utils.IngridDocument;

public class RandomJob implements IJob {

	private final Random _random;

	private final IJob _anotherJob;

	public RandomJob(IJob anotherJob) {
		_anotherJob = anotherJob;
		_random = new Random(System.currentTimeMillis());
	}

	public IngridDocument getResults() {
		IngridDocument document = new IngridDocument();
		document.put("nextInt", _random.nextInt());
		document.putAll(_anotherJob.getResults());
		return document;
	}

}
