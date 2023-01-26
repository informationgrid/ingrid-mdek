/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
 * This class checks if addresses  have a valid parent address.
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

		for (AddressNode aNode : resultList) {
			String adrUuid 	  = aNode.getAddrUuid();
			String parentUuid = aNode.getFkAddrUuid();
			Long aNodeId 	  = aNode.getId();
			
			ErrorReport report = generateErrorReport(adrUuid, parentUuid, aNodeId);
			reportList.add(report);
		}

		LOG.debug(resultList.size() + " invalid entries found!");
	}

	private ErrorReport generateErrorReport(String addrUuid, String parentUuid, Long aNodeId) {
		return new ErrorReport(
				generateMessage(parentUuid, addrUuid),
				generateSolution(parentUuid, aNodeId));
	}

	private String generateSolution(String parentUuid, Long aNodeId) {
		return "Sie koennen die Adresse mit folgendem HQL Query loeschen: " +
				"< delete AddressNode where id = "+aNodeId+" > " +
				"oder an einem anderen Knoten anhaengen mit:" +
				"< update versioned AddressNode set fkAddrUuid = " +
				"'...' where id = "+aNodeId+" >";
	}

	private String generateMessage(String parentUuid, String addrUuid) {
		return "Die Adresse mit der Uuid '"+addrUuid+"' " +
				"besitzt keine gueltige " +
				"uebergeordnete Adresse: [fk_addr_uuid: '"+parentUuid+"']";
	}
}
