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
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ConsistencyCheckerDaoHibernate;
import de.ingrid.utils.IngridDocument;

/**
 * This class checks if several defined tables have valid references to other tables.
 * @author Andre
 *
 */
public class TableAssociationsChecker implements ConsistencyChecker {

	private static final Logger LOG = LogManager.getLogger(TableAssociationsChecker.class);

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
			Long id 		  		= (Long)   element.get(ConsistencyCheckerDaoHibernate.ELEMENT_ID);
			String foreignKey 		= (String) element.get(ConsistencyCheckerDaoHibernate.FOREIGN_KEY);
			String tableName  		= (String) element.get(ConsistencyCheckerDaoHibernate.TABLE_NAME);
			String refTableName  	= (String) element.get(ConsistencyCheckerDaoHibernate.REF_TABLE_NAME);
			
			ErrorReport report = new ErrorReport(
					generateMessage(id, tableName, refTableName),
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

	private String generateMessage(Long elementId, String table, String refTable) {
		return "Ungültiger Verweis auf Tabelle "+refTable+" in Tabelle: " + table + " in Element mit id: " + elementId;
	}
}
