/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
