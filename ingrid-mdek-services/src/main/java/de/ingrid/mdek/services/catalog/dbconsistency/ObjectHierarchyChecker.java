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
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * This class checks if objects have a valid parent object.
 * @author Andre
 *
 */
public class ObjectHierarchyChecker implements ConsistencyChecker {

	private static final Logger LOG = LogManager.getLogger(ObjectHierarchyChecker.class);

	private IConsistencyCheckerDao hqlConsistencyChecker;
	private List<ErrorReport> reportList = new ArrayList<ErrorReport>();
	
	public ObjectHierarchyChecker(IConsistencyCheckerDao daoConsistencyChecker) {
		this.hqlConsistencyChecker = daoConsistencyChecker;
	}
	
	public List<ErrorReport> getResult() {
		return reportList;
	}

	public void run() {

		List<ObjectNode> resultList = hqlConsistencyChecker.checkObjectHierarchy();
		
		reportList.clear();

		for (ObjectNode oNode : resultList) {
			String objUuid 	  = oNode.getObjUuid();
			String parentUuid = oNode.getFkObjUuid();
			Long oNodeId	  = oNode.getId();
			
			ErrorReport report = generateErrorReport(objUuid, parentUuid, oNodeId);
			reportList.add(report);
		}

		LOG.debug(resultList.size() + " invalid entries found!");
	}
	
	private ErrorReport generateErrorReport(String objUuid, String parentUuid, Long oNodeId) {
		return new ErrorReport(
				generateMessage(parentUuid, objUuid),
				generateSolution(parentUuid, oNodeId));
	}

	private String generateSolution(String parentUuid, Long oNodeId) {
		return "Sie koennen das Objekt mit folgendem HQL Query loeschen: " +
				"< delete ObjectNode where id = "+oNodeId+" > " +
				"oder an einem anderen Knoten anhaengen mit:" +
				"< update versioned ObjectNode set fkObjUuid = " +
				"'...' where id = "+oNodeId+" >";
	}

	private String generateMessage(String parentUuid, String objUuid) {
		return "Das Objekt mit der Uuid '"+objUuid+"' " +
				"besitzt kein gueltiges " +
				"uebergeordnetes Objekt: [fk_obj_uuid: '"+parentUuid+"']";
	}
}
