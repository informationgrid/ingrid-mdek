/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.utils.IngridDocument;

public class BounceJob implements IJob {

	private Logger _logger;

	private String _data = "Default Data";
	private int _wait = 0;

	public BounceJob(ILogService logService) {
		_logger = logService.getLogger(BounceJob.class);
	}

	public IngridDocument getResults() {
		if (_logger.isInfoEnabled()) {
			_logger.info("return data in IngridDocument, NO wait");
		}

		// called when job is registered ! return current value WITHOUT WAIT !
        IngridDocument result = new IngridDocument();
        result.put("data", _data);

        return result;
	}

	public void setData(String data) {
		_data = data;
	}
	public void setWait(Integer wait) {
		_wait = wait;
	}

	public String getDataNoWait() {
		if (_logger.isInfoEnabled()) {
			_logger.info("NO wait !");
		}

        return _data;
	}

	public String getDataWithWait() {
		if (_logger.isInfoEnabled()) {
			_logger.info("getDataWithWait: WAITING, wait time:" + _wait);
		}

		doNothing(_wait);

        return _data;
	}

	public IngridDocument bounceDocWithWait(IngridDocument doc) {
		Integer wait = (Integer) doc.get("wait");
		
		if (wait == null) {
			wait = 0;
		}

		if (_logger.isInfoEnabled()) {
			_logger.info("bounceDocWithWait: wait=" + wait);
		}

		doNothing(wait);

        return doc;
	}
	
	private void doNothing(Integer sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            if (_logger.isEnabledFor(Level.WARN)) {
            	_logger.warn("Wait is interrupted.");
            }
        }		
	}
}
