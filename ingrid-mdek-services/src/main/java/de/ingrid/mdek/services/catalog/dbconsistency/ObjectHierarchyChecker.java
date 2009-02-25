package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * This class checks if Objects have a valid father object.
 * @author Andre
 *
 */
public class ObjectHierarchyChecker implements ConsistencyChecker {

	private static final Logger LOG = Logger.getLogger(ObjectHierarchyChecker.class);

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

		for (ObjectNode objAdr : resultList) {
			String objUuid 	  = objAdr.getObjUuid();
			String parentUuid = objAdr.getFkObjUuid();
			Long objId		  = objAdr.getId();
			
			ErrorReport report = generateErrorReport(objUuid, parentUuid, objId);
			reportList.add(report);
		}

		LOG.debug(resultList.size() + " invalid entries found!");
	}
	
	private ErrorReport generateErrorReport(String objUuid, String parentUuid, Long objId) {
		return new ErrorReport(
				generateMessage(parentUuid, objUuid),
				generateSolution(parentUuid, objId));
	}

	private String generateSolution(String parentUuid, Long objId) {
		return "Sie koennen das Objekt mit folgendem HQL Query loeschen: " +
				"< delete ObjectNode where id = "+objId+" > " +
				"oder an einem anderen Knoten anhaengen mit:" +
				"< update versioned ObjectNode set fkAddrUuid = " +
				"'...' where id = "+objId+" >";
	}

	private String generateMessage(String parentUuid, String objUuid) {
		return "Das Objekt mit der Uuid '"+objUuid+"' " +
				"in der Tabelle 'ObjectNode' besitzt kein gueltiges " +
				"uebergeordnetes Objekt: [fk_obj_uuid: '"+parentUuid+"']";
	}
}
