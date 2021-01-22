/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;

/**
 * This class checks if Objects have invalid address references.
 * @author Michael Benz & Andre Wallat
 *
 */
public class AddressReferencesChecker implements ConsistencyChecker {

	private static final Logger LOG = LogManager.getLogger(AddressReferencesChecker.class);

	private IConsistencyCheckerDao hqlConsistencyChecker;
	private List<ErrorReport> reportList = new ArrayList<ErrorReport>();

	public AddressReferencesChecker(IConsistencyCheckerDao daoConsistencyChecker) {
		this.hqlConsistencyChecker = daoConsistencyChecker;
	}

	public List<ErrorReport> getResult() {
		return reportList;
	}

	public void run() {
		
		List<T012ObjAdr> resultList = hqlConsistencyChecker.checkAddressReferences();

		reportList.clear();

		for (T012ObjAdr entity : resultList) {
			String adrUuid 	 = entity.getAdrUuid();
			Long objAdrId 	 = entity.getId();
			Long objId 	 	 = entity.getObjId();

			ErrorReport report = generateErrorReport(adrUuid, objAdrId, objId);
			reportList.add(report);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(resultList.size() + " invalid entries found!");
		}
	}

	private ErrorReport generateErrorReport(String adrUuid, Long objAdrId, Long objId) {
		return new ErrorReport(
				generateMessage(adrUuid, objAdrId, objId),
				generateSolution(adrUuid, objAdrId, objId));
	}

	private String generateSolution(String adrUuid, Long objAdrId, Long objId) {
		return "Sie koennen den Verweis auf die ungueltige Adresse mit " +
				"folgendem HQL Query loeschen: " +
				"< delete T012ObjAdr where id = "+objAdrId+" >";
	}

	private String generateMessage(String adrUuid, Long objAdrId, Long objId) {
		return "Das Objekt mit der Id '"+objId+"' "+
			"enthaelt einen ungueltingen Addressverweis: " +
			"[objAdrId: '"+objAdrId+"', adrUuid: '"+adrUuid+"']";
	}
}
