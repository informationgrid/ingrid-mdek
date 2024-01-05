/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.date;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.utils.IngridDocument;

@Service
public class SimpleDateJob implements IJob {

	private Logger _logger;

	@Autowired
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
