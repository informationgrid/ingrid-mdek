package de.ingrid.mdek.example;

import java.util.Random;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.utils.IngridDocument;

public class RandomJob implements IJob {

	private final Random _random;

	private final IJob _anotherJob;

	private String _name = "Unknown";

	public RandomJob(IJob anotherJob) {
		_anotherJob = anotherJob;
		_random = new Random(System.currentTimeMillis());
	}

	@SuppressWarnings("unchecked")
	public IngridDocument getResults() {
		IngridDocument result = new IngridDocument();

		IngridDocument document = new IngridDocument();
		document.put("nextInt", _random.nextInt());
		document.put("anotherJobResult", _anotherJob.getResults());

		result.put(IJobRepository.JOB_RESULT, document);
		return result;
	}

	public String sayHello() {
		return "Hello " + _name + ".";
	}

	public void setName(String name) {
		_name = name;
	}
}
