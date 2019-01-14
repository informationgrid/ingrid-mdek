/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * This class checks that every Object contains an address
 * @author Andre Wallat
 *
 */
public class InfoAddressChecker implements ConsistencyChecker {

	private static final Logger LOG = LogManager.getLogger(InfoAddressChecker.class);

	private IConsistencyCheckerDao hqlConsistencyChecker;
	private List<ErrorReport> reportList = new ArrayList<ErrorReport>();
	
	public InfoAddressChecker(IConsistencyCheckerDao daoConsistencyChecker) {
		this.hqlConsistencyChecker = daoConsistencyChecker;
	}
	
	public List<ErrorReport> getResult() {
		return reportList;
	}

	public void run() {
		List<T01Object> resultList = hqlConsistencyChecker.checkInfoAddress();

		reportList.clear();

		for (T01Object entity : resultList) {
			String objUuid  = entity.getObjUuid();
			String title	= entity.getObjName();
			
			ErrorReport report = generateErrorReport(objUuid, title);
			reportList.add(report);
		}

		LOG.debug(resultList.size() + " invalid entries found!");
	}

	private ErrorReport generateErrorReport(String objUuid, String title) {
		return new ErrorReport(
				generateMessage(objUuid, title),
				generateSolution(objUuid, title));
	}

	private String generateSolution(String objUuid, String title) {
		return "Bitte fuegen Sie eine Adresse im Objekt " + objUuid +
			" (Titel: '"+title+"') ein!";
	}

	private String generateMessage(String objUuid, String title) {
		return "Das Objekt mit der Uuid '"+objUuid+"' " +
				"enthaelt keine Adresse!";
	}
	
}
