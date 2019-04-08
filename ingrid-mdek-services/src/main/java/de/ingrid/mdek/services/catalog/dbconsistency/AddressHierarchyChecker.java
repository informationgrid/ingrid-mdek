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
import de.ingrid.mdek.services.persistence.db.model.AddressNode;

/**
 * This class checks if Objects have a valid father object.
 * @author Andre
 *
 */
public class AddressHierarchyChecker implements ConsistencyChecker {

	private static final Logger LOG = LogManager.getLogger(AddressHierarchyChecker.class);

	private IConsistencyCheckerDao hqlConsistencyChecker;
	private List<ErrorReport> reportList = new ArrayList<ErrorReport>();
	
	public AddressHierarchyChecker(IConsistencyCheckerDao daoConsistencyChecker) {
		this.hqlConsistencyChecker = daoConsistencyChecker;
	}
	
	public List<ErrorReport> getResult() {
		return reportList;
	}

	public void run() {
		
		List<AddressNode> resultList = hqlConsistencyChecker.checkAddressHierarchy();

		reportList.clear();

		for (AddressNode objAdr : resultList) {
			String adrUuid 	  = objAdr.getAddrUuid();
			String parentUuid = objAdr.getFkAddrUuid();
			Long addrId 	  = objAdr.getId();
			
			ErrorReport report = generateErrorReport(adrUuid, parentUuid, addrId);
			reportList.add(report);
		}

		LOG.debug(resultList.size() + " invalid entries found!");
	}

	private ErrorReport generateErrorReport(String addrUuid, String parentUuid, Long addrId) {
		return new ErrorReport(
				generateMessage(parentUuid, addrUuid),
				generateSolution(parentUuid, addrId));
	}

	private String generateSolution(String parentUuid, Long addrId) {
		return "Sie koennen die Adresse mit folgendem HQL Query loeschen: " +
				"< delete AddressNode where id = "+addrId+" > " +
				"oder an einem anderen Knoten anhaengen mit:" +
				"< update versioned AddressNode set fkAddrUuid = " +
				"'...' where id = "+addrId+" >";
	}

	private String generateMessage(String parentUuid, String addrUuid) {
		return "Das Objekt mit der Uuid '"+addrUuid+"' " +
				"in der Tabelle 'AddressNode' besitzt kein gueltiges " +
				"uebergeordnetes Objekt: [fk_obj_uuid: '"+parentUuid+"']";
	}
}
