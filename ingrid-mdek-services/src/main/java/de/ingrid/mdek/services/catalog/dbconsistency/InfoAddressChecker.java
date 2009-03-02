package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;

/**
 * This class checks that every Object contains an address of type "Auskunft"
 * @author Andre Wallat
 *
 */
public class InfoAddressChecker implements ConsistencyChecker {

	private static final Logger LOG = Logger.getLogger(InfoAddressChecker.class);

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
		return "Bitte fügen Sie eine Auskunftsadresse im Objekt " + objUuid +
			" (Titel: '"+title+"') ein!";
	}

	private String generateMessage(String objUuid, String title) {
		return "Das Objekt mit der Uuid '"+objUuid+"' " +
				"enthaelt keine Datenauskunftsadresse!";
	}
	
}
