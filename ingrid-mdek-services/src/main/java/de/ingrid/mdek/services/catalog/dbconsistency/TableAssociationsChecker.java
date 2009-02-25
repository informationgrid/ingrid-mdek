package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ConsistencyCheckerDaoHibernate;
import de.ingrid.utils.IngridDocument;

/**
 * This class checks if several defined tables have valid references to other tables.
 * @author Andre
 *
 */
public class TableAssociationsChecker implements ConsistencyChecker {

	private static final Logger LOG = Logger.getLogger(TableAssociationsChecker.class);

	private IConsistencyCheckerDao hqlConsistencyChecker;
	private List<ErrorReport> reportList = new ArrayList<ErrorReport>();
	
	public TableAssociationsChecker(IConsistencyCheckerDao daoConsistencyChecker) {
		this.hqlConsistencyChecker = daoConsistencyChecker;
	}
	
	public List<ErrorReport> getResult() {
		return reportList;
	}

	public void run() {
		reportList.clear();
		
		List<IngridDocument> resultList = hqlConsistencyChecker.checkTableAssociations();
		
		for (IngridDocument element : resultList) {
			Long id 		  = (Long)   element.get(ConsistencyCheckerDaoHibernate.ELEMENT_ID);
			String tableName  = (String) element.get(ConsistencyCheckerDaoHibernate.TABLE_NAME);
			String foreignKey = (String) element.get(ConsistencyCheckerDaoHibernate.FOREIGN_KEY);
			
			ErrorReport report = new ErrorReport(generateMessage(id, tableName),
					generateSolution(id, tableName, foreignKey));
			reportList.add(report);
		}

		LOG.debug(reportList.size() + " invalid entries found!");
	}

	private String generateSolution(Long elementId, String table, String foreignKey) {
		return "Sie koennen den Eintrag mit folgendem HQL Query loeschen: " +
				"< delete "+table+" where id = "+elementId+" > " +
				"oder an einem anderen Knoten anhängen mit:" +
				"< update versioned "+table+" set "+foreignKey+" = '...' " +
				"where id = "+elementId+" >";
	}

	private String generateMessage(Long elementId, String table) {
		return "Ungültiger Verweis in Tabelle: " + table + " in Element mit id: " + elementId;
	}
}
