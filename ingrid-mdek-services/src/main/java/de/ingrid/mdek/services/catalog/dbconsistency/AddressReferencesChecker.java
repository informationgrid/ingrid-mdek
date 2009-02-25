package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;

/**
 * This class checks if Objects have invalid address references.
 * @author Michael Benz & Andre Wallat
 *
 */
public class AddressReferencesChecker implements ConsistencyChecker {

	private static final Logger LOG = Logger.getLogger(AddressReferencesChecker.class);

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
